package com.mycompany.utbotcontest;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObject;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
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
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectDisappearedEvent;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.ChangeWeapon;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.Iterator;
import java.util.Map;
import javax.vecmath.Vector3d;


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
    private ProbabilitesArmes probaA;
    
    private Player enemy = null;
    
    private boolean navigate = false;
    
    //Probabilite
    public String lastWeaponUsed = "AssaultRifle";
    
    //RAYCASTING
    protected static final String FRONT = "frontRay";
    //protected static final String LEFT45 = "left45Ray";
    protected static final String LEFT90 = "left90Ray";
    //protected static final String RIGHT45 = "right45Ray";
    protected static final String RIGHT90 = "right90Ray";
    protected AutoTraceRay left, front, right;
    private boolean first = true;
    private boolean raysInitialized = false;
    @JProp
    protected boolean sensorLeft90 = false;
    @JProp
    protected boolean sensorRight90 = false;
    @JProp
    protected boolean sensorFront = false;
    @JProp
    protected boolean moving = false;
    @JProp
    protected boolean sensor = false;
    
    
    
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
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);                
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);        
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHIELD_GUN, false);
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
        probaA = new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 0.5, 0, 0);
        probaA.initProbabilitesA();
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 5);
        // settings for the rays
        boolean fastTrace = true;        // perform only fast trace == we just need true/false information
        boolean floorCorrection = false; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
        boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well
        
        // 1. remove all previous rays, each bot starts by default with three
        // rays, for educational purposes we will set them manually
        getAct().act(new RemoveRay("All"));

        // 2. create new rays
        //raycasting.createRay(LEFT45,  new Vector3d(1, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLength, fastTrace, floorCorrection, traceActor);
        //raycasting.createRay(RIGHT45, new Vector3d(1, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        // note that we will use only three of them, so feel free to experiment with LEFT90 and RIGHT90 for yourself
        raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);

        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {
            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                left = raycasting.getRay(LEFT90);
                front = raycasting.getRay(FRONT);
                right = raycasting.getRay(RIGHT90);
            }
        });
        // have you noticed the FlagListener interface? The Pogamut is often using {@link Flag} objects that
        // wraps some iteresting values that user might respond to, i.e., whenever the flag value is changed,
        // all its listeners are informed

        // 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is        
        raycasting.endRayInitSequence();
        // IMPORTANT:
        // The most important thing is this line that ENABLES AUTO TRACE functionality,
        // without ".setAutoTrace(true)" the AddRay command would be useless as the bot won't get
        // trace-lines feature activated
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));
        // FINAL NOTE: the ray initialization must be done inside botInitialized method or later on inside
        //             botSpawned method or anytime during doLogic method
    }
    
    /*@ObjectClassEventListener(eventClass = WorldObjectUpdatedEvent.class, objectClass = Weaponry.class)
    protected void weaponChangeUpdated(WorldObjectUpdatedEvent<Weaponry> event) {
		// greet player when he appears
    }*/
    
     //{@link PlayerKilled} listener that provides "frag" counting + is switches the state of the hunter.
    //@param event
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
            probaA.nbVIncrement(lastWeaponUsed);
            probaA.updateProba(lastWeaponUsed);
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
    }   
    
    public void reset() {
        probaA.inventaireArmes.clear();
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
    
    @ObjectClassEventListener(eventClass = WorldObjectAppearedEvent.class, objectClass = IncomingProjectile.class)
	protected void incomingProjectile(WorldObjectAppearedEvent<IncomingProjectile> event) {
            Location origine = event.getObject().getOrigin();
            Vector3d d = event.getObject().getDirection();
            d.negate();
            if (info.getLocation().asVector3d().equals(d)) {
                if (sensorLeft90 && sensorRight90) {
                    move.doubleJump();
                }
                if (sensorLeft90) {
                    move.strafeRight(60);
                    move.jump();
                }
                if (sensorRight90) {
                    move.strafeRight(60);
                    move.jump();
                }
            }
        }
    
    
    protected Weapon wp = null;
    
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
        if (info.isAdrenalineFull()) {
            comboAdrenaline();
        }
        if (!weaponry.getCurrentWeapon().getGroup().getName().equals(lastWeaponUsed)) {
            lastWeaponUsed = weaponry.getCurrentWeapon().getGroup().getName();
        }
        if (weaponry.getWeapons().size() > 2 && !players.canSeeEnemies()) {
            weaponry.changeWeapon(probaA.choixArme(weaponry));
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
                //medkit.stateFlee();
            return;
        }

        // 6) if nothing ... run around items
        stateRunAround.stateRunAroundItems();

    }
    
    protected void comboAdrenaline() {
        if (info.getHealth() <= healthLevel) {
            combo.performDefensive();
        }
        else {
            if (shouldEngage || shouldPursue) {
                combo.performBerserk();
            }
            else {
                combo.performInvisible();
            }
        }
    }
    
    
    @Override
    public void botKilled(BotKilled event) {
        probaA.nbDIncrement(lastWeaponUsed);
        probaA.updateProba(lastWeaponUsed);
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

    public AdvancedLocomotion getMove() {
        return move;
    }

    public CompleteBotCommandsWrapper getBody() {
        return body;
    }
    
    public ProbabilitesArmes getProbaArmes(){
        return probaA;
    }

    public boolean isNavigate() {
        return navigate;
    }

    public void setNavigate(boolean navigate) {
        this.navigate = navigate;
    }
    
    
    
}