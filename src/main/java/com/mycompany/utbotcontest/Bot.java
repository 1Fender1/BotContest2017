package com.mycompany.utbotcontest;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
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
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
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
    
    //private MeshInit meshInit;
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

        //meshInit = new MeshInit(this, navBot, navMeshModule, levelGeometryModule);

        //meshInit.initSpawn(gameInfo);
        navBot = new BotNavigation(this, /*meshInit*/null);
        stateRunAround = new RunAroundItem(this, navBot);
        engage = new Engage(this, navBot);
        medkit = new Medkit(this, navBot, stateRunAround);
        pursue = new Pursue(this, navBot);
        hit = new Hit(this, navBot);
        probaA = new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 0.5, 0, 0);
        probaA.initProbabilitesA();
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 5);
        // settings for the rays
        boolean fastTrace = true;
        boolean floorCorrection = false; 
        boolean traceActor = false;

        getAct().act(new RemoveRay("All"));

        raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLength, fastTrace, floorCorrection, traceActor);

        raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);


        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {
            @Override
            public void flagChanged(Boolean changedValue) {
                left = raycasting.getRay(LEFT90);
                front = raycasting.getRay(FRONT);
                right = raycasting.getRay(RIGHT90);
            }
        });
        raycasting.endRayInitSequence();
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));

    }
    
    
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
    

    protected Weapon wp = null;
    
    @Override
    public void logic() throws PogamutException {
        Alea pourcentChangeArme = new Alea();
        navBot.botFocus();
        navBot.mouvementAleatoire();
         if (!raycasting.getAllRaysInitialized().getFlag()) {
            return;
        }
        sensorFront = front.isResult();
        sensorLeft90 = left.isResult();
        sensorRight90 = right.isResult();
        sensor = sensorFront || sensorLeft90 || sensorRight90;
        if (!weaponry.getCurrentWeapon().getGroup().getName().equals(lastWeaponUsed)) {
            lastWeaponUsed = weaponry.getCurrentWeapon().getGroup().getName();
        }
        if (pourcentChangeArme.pourcentDeChance(30) && weaponry.getWeapons().size() > 2 && (info.isShooting() || info.isSecondaryShooting())) {
            weaponry.changeWeapon(probaA.choixArme(weaponry));
        }
        
        if (players.canSeeEnemies())
        {
            enemy = players.getNearestVisibleEnemy();
        }
        
        // mark that another logic iteration has began
        if (shouldEngage && enemy != null && players.canSeeEnemies() && info.isFacing(enemy, 45) && weaponry.hasLoadedWeapon()) {
            enemy = engage.stateEngage();
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

        //System.out.println("==========enemy " + enemy + " ===================");
        // 4) have you got enemy to pursue? -> go to the last position of enemy
        if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
            pursue.statePursue();
            return;
        }
        
        // 5) are you hurt?			-> get yourself some medKit
        if (shouldCollectHealth && info.getHealth() < healthLevel) {
                if (info.getArmor() == 0 && info.getHealth() <= 50) {
                    shouldPursue = false;
                    shouldEngage = false;
                    medkit.stateFuite();
                }
                medkit.stateMedKit();
            return;
        }

        // 6) if nothing ... run around items
        stateRunAround.stateRunAroundItems();

    }
    
    
    
    @Override
    public void botKilled(BotKilled event) {
        probaA.nbDIncrement(lastWeaponUsed);
        probaA.updateProba(lastWeaponUsed);
        probaA.inventaireArmes.clear();
        probaA.setSize(0);
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

    public IUT2004Navigation getNavigation() {
        return navigation;
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

    public Engage getEngage() {
        return engage;
    }
    
    public Pursue getPursue() {
        return pursue;
    }
    
    public boolean isNavigate() {
        return navigate;
    }

    public void setNavigate(boolean navigate) {
        this.navigate = navigate;
    }

    public void setEnemy(Player enemy) {
        this.enemy = enemy;
    }
    
    public boolean getLeft90() {
        return left.isResult();
    }
    
    public boolean getRight90() {
        return right.isResult();
    }
    
    public void setPursue(Boolean etat) {
        this.shouldPursue = etat;
    }
    
    public void setEngage(Boolean etat) {
        this.shouldEngage = etat;
    }
    
    public void setMedkit(Boolean etat) {
        this.shouldCollectHealth = etat;
    }    
    
}