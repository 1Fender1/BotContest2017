package com.mycompany.utbotcontest;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.utils.exception.PogamutException;


@AgentScoped
@Deprecated
public class Bot extends UT2004BotModuleController {

    @JProp
    public boolean shouldEngage = true;

    @JProp
    public boolean shouldPursue = true;

    @JProp
    public boolean shouldRearm = true;

    @JProp
    public boolean shouldCollectHealth = true;

    @JProp
    public int healthLevel = 75;

    @JProp
    public int frags = 0;

    @JProp
    public int deaths = 0;
    
    private int pursueCount = 0;
    
    protected boolean runningToPlayer = false;

    //@param config information about configuration
    //@param init information about configuration
  
    private UT2004PathAutoFixer autoFixer;
    
    private static int instanceCount = 0;

    private BotNavigation navBot;
    
    private RunAroundItem stateRunAround;
    
    private Engage engage;
    
    private Medkit medkit;
    
    private Pursue pursue;
    
    private Hit hit;
    
   private MeshInit meshInit;
    
    private Player enemy = null;
    
    private boolean navigate = false;
    
    @Override
    public void mapInfoObtained() {
    	navMeshModule.setReloadNavMesh(true); // tells NavMesh to reconstruct OffMeshPoints    	
    }
    

    @Override
    public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
        body.getConfigureCommands().setBotAppearance("HumanMaleA.EgyptMaleA");
        getInitializeCommand().setDesiredSkill(5);
    }
    
    @Override
    public void prepareBot(UT2004Bot bot) {

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        navigation.getState().addListener(new FlagListener<NavigationState>() {
            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (navBot.getItem() != null) {
                            navBot.getTabooItems().add(navBot.getItem(), 10);
                        }
                        reset();
                        break;

                    case TARGET_REACHED:
                        reset();
                        break;
                }
            }
        });

        // DEFINE WEAPON PREFERENCES
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);                
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);        
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, false);        
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
    }

    //@return
    @Override
    public Initialize getInitializeCommand() {
        // just set the name of the bot and his skill level, 1 is the lowest, 7 is the highest
    	// skill level affects how well will the bot aim
        return new Initialize().setName("T80" + (instanceCount++)).setDesiredSkill(5);
    }
    
        @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {

        meshInit = new MeshInit(this, navBot, navMeshModule, levelGeometryModule);

        meshInit.initSpawn(gameInfo);
        
        navBot = new BotNavigation(this, meshInit);
        stateRunAround = new RunAroundItem(this, navBot);
        engage = new Engage(this, navBot);
        medkit = new Medkit(this, navBot, stateRunAround);
        pursue = new Pursue(this, navBot);
        hit = new Hit(this, navBot);
    }
    

     //{@link PlayerKilled} listener that provides "frag" counting + is switches the state of the hunter.
    //@param event
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
    }   
    
    public void reset() {
    	navBot.setItem(null);
        enemy = null;
        move.stopMovement();
        stateRunAround.setItemsToRunAround(null);
    }
    
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    	log.info("I have just hurt other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }
    
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    	log.info("I have just been hurt by other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }

    @Override
    public void logic() throws PogamutException {
        
        if (!navMeshModule.isInitialized()) {
            // Check logs for information, where the bot expects NavMesh file to be present...
            meshInit.setErrorMsg(meshInit.getErrorMsg() + 1);
            meshInit.say("NAV MESH NOT INITIALIZED - SEE STARTUP LOGS FOR MORE INFO... (" + (meshInit.getErrorMsg() + ")"));
            return;
    	}
    	    	
    	if (meshInit.isDrawNavMesh()) {
    		if (!meshInit.drawNavMesh()) return;
    	}
    	
    	if (!meshInit.isSpeak() && meshInit.isDrawOffMeshLinks()) {
    		if (!meshInit.drawOffMeshLinks()) return;
    	}
    	
    	if (meshInit.isSpeak()) {
    		if (!meshInit.speak()) return;
    	} else {
    		navigate = true;
    	}
        
        
        // mark that another logic iteration has began
        if (shouldEngage && players.canSeeEnemies() && weaponry.hasLoadedWeapon()) {
            engage.stateEngage();
            return;
        }

        // 2) are you shooting? 	-> stop shooting, you've lost your target
        if (info.isShooting() || info.isSecondaryShooting()) {
            getAct().act(new StopShooting());
        }

        // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
        if (senses.isBeingDamaged()) {
            hit.stateHit();
            return;
        }

        // 4) have you got enemy to pursue? -> go to the last position of enemy
        if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
            pursue.statePursue();
            return;
        }

        // 5) are you hurt?			-> get yourself some medKit
        if (shouldCollectHealth && info.getHealth() < healthLevel) {
            medkit.stateMedKit();
            return;
        }

        // 6) if nothing ... run around items
        stateRunAround.stateRunAroundItems();

    }

    
    @Override
    public void botKilled(BotKilled event) {
    	reset();
    }

    public NavMeshNavigation getNmNav() {
        return nmNav;
    }

    @Override
    public LevelGeometryModule getLevelGeometryModule() {
        return levelGeometryModule;
    }

    @Override
    public UT2004Bot getBot() {
        return bot;
    }
    
    @Override
    public AgentInfo getInfo() {
        return info;
    }

    public UT2004Draw getDraw() {
        return draw;
    }

    @Override
    public Weaponry getWeaponry() {
        return weaponry;
    }

    @Override
    public WeaponPrefs getWeaponPrefs() {
        return weaponPrefs;
    }

    @Override
    public Items getItems() {
        return items;
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public LogCategory getLog() {
        return log;
    }

    public Player getEnemy() {
        return enemy;
    }

    @Override
    public ImprovedShooting getShoot() {
        return shoot;
    }

    @Override
    public Players getPlayers() {
        return players;
    }

    public boolean isRunningToPlayer() {
        return runningToPlayer;
    }

    public void setPursueCount(int pursueCount) {
        this.pursueCount = pursueCount;
    }

    public int getPursueCount() {
        return pursueCount;
    }

    @Override
    public AdvancedLocomotion getMove() {
        return move;
    }

    @Override
    public CompleteBotCommandsWrapper getBody() {
        return body;
    }

    public boolean isNavigate() {
        return navigate;
    }

    public void setNavigate(boolean navigate) {
        this.navigate = navigate;
    }

}
