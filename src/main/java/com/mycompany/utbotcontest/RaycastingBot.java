package com.mycompany.utbotcontest;

import javax.vecmath.Vector3d;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerFactory;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.cuni.amis.pogamut.ut2004.bot.*;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SetSkin;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.ArrayList;
import java.util.List;

/**
 * Example of Simple Pogamut bot, that randomly walks around the map. Bot is
 * incapable of handling movers so far. 
 * 
 * <p><p> 
 * The crucial method to read
 * through is {@link RaycastingBot#botInitialized(GameInfo, ConfigChange, InitedMessage)},
 * it will show you how to set up ray-casting.
 * 
 * <p><p>
 * We recommend you to try this bot on DM-TrainingDay or DM-Albatross or DM-Flux2.
 * 
 * <p><p>
 * Note that this is a bit deprecated way to do raycasting as we have more advanced approach via "geometry-at-client", see {@link LevelGeometryModule} 
 * and checkout svn://artemis.ms.mff.cuni.cz/pogamut/trunk/project/Main/PogamutUT2004Examples/35-ManualBot that contains hints how to do raycasting client-side.
 * 
 *
 * @author Ondrej Burkert
 * @author Rudolf Kadlec aka ik
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
@Deprecated
public class RaycastingBot extends UT2004BotModuleController {

    // Constants for rays' ids. It is allways better to store such values
    // in constants instead of using directly strings on multiple places of your
    // source code
    protected static final String FRONT = "frontRay";
    protected static final String LEFT45 = "left45Ray";
    //protected static final String LEFT90 = "left90Ray";
    protected static final String RIGHT45 = "right45Ray";
    //protected static final String RIGHT90 = "right90Ray";
    
    private AutoTraceRay left, front, right;
    
    /**
     * Flag indicating that the bot has been just executed.
     */
    private boolean first = true;
    private boolean raysInitialized = false;
    /**
     * Whether the left45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#LEFT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorLeft45 = false;
    /**
     * Whether the right45 sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#RIGHT45} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorRight45 = false;
    /**
     * Whether the front sensor signalizes the collision. (Computed in the
     * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
     * ray.
     */
    @JProp
    private boolean sensorFront = false;
    /**
     * Whether the bot is moving. (Computed in the doLogic())
     */
    @JProp
    private boolean moving = false;
    /**
     * Whether any of the sensor signalize the collision. (Computed in the
     * doLogic())
     */
    @JProp
    private boolean sensor = false;
    /**
     * How much time should we wait for the rotation to finish (milliseconds).
     */
    @JProp
    private int turnSleep = 250;
    /**
     * How fast should we move? Interval <0, 1>.
     */
    private float moveSpeed = 0.6f;
    /**
     * Small rotation (degrees).
     */
    @JProp
    private int smallTurn = 30;
    /**
     * Big rotation (degrees).
     */
    @JProp
    private int bigTurn = 90;
    
        /**
     * boolean switch to activate engage behavior
     */
    @JProp
    public boolean shouldEngage = true;
    /**
     * boolean switch to activate pursue behavior
     */
    @JProp
    public boolean shouldPursue = true;
    /**
     * boolean switch to activate rearm behavior
     */
    @JProp
    public boolean shouldRearm = true;
    /**
     * boolean switch to activate collect health behavior
     */
    @JProp
    public boolean shouldCollectHealth = true;
    /**
     * how low the health level should be to start collecting health items
     */
    @JProp
    public int healthLevel = 75;
    /**
     * how many bot the hunter killed other bots (i.e., bot has fragged them /
     * got point for killing somebody)
     */
    @JProp
    public int frags = 0;
    /**
     * how many times the hunter died
     */
    @JProp
    public int deaths = 0;

    /**
     * The bot is initialized in the environment - a physical representation of
     * the bot is present in the game.
     *
     * @param config information about configuration
     * @param init information about configuration
     */
    
    @Override
    public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
        // initialize rays for raycasting
        body.getConfigureCommands().setBotAppearance("HumanMaleA.EgyptMaleA");
        
        final int rayLengthMove = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 10);
        // settings for the rays
        boolean fastTrace = true;        // perform only fast trace == we just need true/false information
        boolean floorCorrection = true; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
        boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well

        // 1. remove all previous rays, each bot starts by default with three
        // rays, for educational purposes we will set them manually
        getAct().act(new RemoveRay("All"));

        // 2. create new rays
        raycasting.createRay(LEFT45,  new Vector3d(1, -1, 0), rayLengthMove, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLengthMove, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT45, new Vector3d(1, 1, 0), rayLengthMove, fastTrace, floorCorrection, traceActor);
        // note that we will use only three of them, so feel free to experiment with LEFT90 and RIGHT90 for yourself
       // raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLengthMove, fastTrace, floorCorrection, traceActor);
       // raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLengthMove, fastTrace, floorCorrection, traceActor);


        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {

            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                left = raycasting.getRay(LEFT45);
                front = raycasting.getRay(FRONT);
                right = raycasting.getRay(RIGHT45);
            }
        });
        // have you noticed the FlagListener interface? The Pogamut is often using {@link Flag} objects that
        // wraps some iteresting values that user might respond to, i.e., whenever the flag value is changed,
        // all its listeners are informed

        // 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is        
        raycasting.endRayInitSequence();

        // change bot's default speed
        config.setSpeedMultiplier(moveSpeed);

        // IMPORTANT:
        // The most important thing is this line that ENABLES AUTO TRACE functionality,
        // without ".setAutoTrace(true)" the AddRay command would be useless as the bot won't get
        // trace-lines feature activated
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));

        // FINAL NOTE: the ray initialization must be done inside botInitialized method or later on inside
        //             botSpawned method or anytime during doLogic method
    }
     /**
     * Used internally to maintain the information about the bot we're currently
     * hunting, i.e., should be firing at.
     */
    protected Player enemy = null;
    /**
     * Item we're running for. 
     */
    protected Item item = null;
    /**
     * Taboo list of items that are forbidden for some time.
     */
    protected TabooSet<Item> tabooItems = null;
    
    private UT2004PathAutoFixer autoFixer;
    
	private static int instanceCount = 0;

    /**
     * Bot's preparation - called before the bot is connected to GB2004 and
     * launched into UT2004.
     */
    
        /**
     * {@link PlayerKilled} listener that provides "frag" counting + is switches
     * the state of the hunter.
     *
     * @param event
     */
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
    
    private void deplacementNat()
    {
        if (!raycasting.getAllRaysInitialized().getFlag()) {
            return;
        }

        // once the rays are up and running, move according to them

        sensorFront = front.isResult();
        sensorLeft45 = left.isResult();
        sensorRight45 = right.isResult();

        // is any of the sensor signalig?
        sensor = sensorFront || sensorLeft45 || sensorRight45;

        if (!sensor) {
            // no sensor are signalizes - just proceed with forward movement
            goForward();
            return;
        }

        // some sensor/s is/are signaling

        // if we're moving
        if (moving) {
            // stop it, we have to turn probably
            move.stopMovement();
            moving = false;
        }

        // according to the signals, take action...
        // 8 cases that might happen follows
        if (sensorFront) {
            if (sensorLeft45) {
                if (sensorRight45) {
                    // LEFT45, RIGHT45, FRONT are signaling
                    move.turnHorizontal(bigTurn);
                } else {
                    // LEFT45, FRONT45 are signaling
                    move.turnHorizontal(smallTurn);
                }
            } else {
                if (sensorRight45) {
                    // RIGHT45, FRONT are signaling
                    move.turnHorizontal(-smallTurn);
                } else {
                    // FRONT is signaling
                    move.turnHorizontal(smallTurn);
                }
            }
        } else {
            if (sensorLeft45) {
                if (sensorRight45) {
                    // LEFT45, RIGHT45 are signaling
                    goForward();
                } else {
                    // LEFT45 is signaling
                    move.turnHorizontal(smallTurn);
                }
            } else {
                if (sensorRight45) {
                    // RIGHT45 is signaling
                    move.turnHorizontal(-smallTurn);
                } else {
                    // no sensor is signaling
                    goForward();
                }
            }
        }
        return;
    }
    
    @Override
    public void prepareBot(UT2004Bot bot) {
        tabooItems = new TabooSet<Item>(bot);

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

        // listeners        
        navigation.getState().addListener(new FlagListener<NavigationState>() {

            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (item != null) {
                            tabooItems.add(item, 10);
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
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);        
        weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
    }

    /**
     * Here we can modify initializing command for our bot.
     *
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        // just set the name of the bot and his skill level, 1 is the lowest, 7 is the highest
    	// skill level affects how well will the bot aim
        //return new Initialize().setName("Hunter-" + (++instanceCount)).setDesiredSkill(5);
        return new Initialize().setName("Terminator");
    }

    /**
     * Resets the state of the Hunter.
     */
    protected void reset() {
    	item = null;
        enemy = null;
        move.stopMovement();
        itemsToRunAround = null;
        deplacementNat();
    }
    
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    	log.info("I have just hurt other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }
    
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    	log.info("I have just been hurt by other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }

    /**
     * Main method that controls the bot.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() throws PogamutException {
        // mark that another logic iteration has began
        log.info("--- Logic iteration ---");

        // if the rays are not initialized yet, do nothing and wait for their initialization 
        deplacementNat();
        // 1) do you see enemy? -> go to PURSUE (start shooting / hunt the enemy)
        if (shouldEngage && players.canSeeEnemies() && weaponry.hasLoadedWeapon()) {
            stateEngage();
            return;
        }

        // 2) are you shooting? 	-> stop shooting, you've lost your target
        if (info.isShooting() || info.isSecondaryShooting()) {
            getAct().act(new StopShooting());
        }

        // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
        if (senses.isBeingDamaged()) {
            this.stateHit();
            return;
        }

        // 4) have you got enemy to pursue? -> go to the last position of enemy
        if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
            this.statePursue();
            return;
        }

        // 5) are you hurt?			-> get yourself some medKit
        if (shouldCollectHealth && info.getHealth() < healthLevel) {
            this.stateMedKit();
            return;
        }

        // 6) if nothing ... run around items
       // stateRunAroundItems();

        // HOMEWORK FOR YOU GUYS:
        // Try to utilize LEFT90 and RIGHT90 sensors and implement wall-following behavior!
    }
    
    protected boolean runningToPlayer = false;

    /**
     * Fired when bot see any enemy. <ol> <li> if enemy that was attacked last
     * time is not visible than choose new enemy <li> if enemy is reachable and the bot is far - run to him
     * <li> otherwise - stand still (kind a silly, right? :-)
     * </ol>
     */
    protected void stateEngage() {
        //log.info("Decision is: ENGAGE");
        //config.setName("Hunter [ENGAGE]");

       /* boolean shooting = false;
        double distance = Double.MAX_VALUE;
        pursueCount = 0;
        

        // 1) pick new enemy if the old one has been lost
        if (enemy == null || !enemy.isVisible()) {
            // pick new enemy
            enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
            if (enemy == null) {
                log.info("Can't see any enemies... ???");
                return;
            }
        }

        // 2) stop shooting if enemy is not visible
        if (!enemy.isVisible()) {
	        if (info.isShooting() || info.isSecondaryShooting()) {
                // stop shooting
                getAct().act(new StopShooting());
            }
            runningToPlayer = false;
        } else {
        	// 2) or shoot on enemy if it is visible
	        distance = info.getLocation().getDistance(enemy.getLocation());
	        if (shoot.shoot(weaponPrefs, enemy) != null) {
	            log.info("Shooting at enemy!!!");
	            shooting = true;
	        }
        }

        // 3) if enemy is far or not visible - run to him
        int decentDistance = Math.round(random.nextFloat() * 800) + 200;
        if (!enemy.isVisible() || !shooting || decentDistance < distance) {
            if (!runningToPlayer) {
                move.moveTo(enemy);
                runningToPlayer = true;
            }
        } else {
            runningToPlayer = false;
            move.stopMovement();
        }
        
        item = null;*/
       enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
       deplacementNat();
       move.moveTo(enemy.getLocation());
       System.out.println("jump");
    }

    ///////////////
    // STATE HIT //
    ///////////////
    protected void stateHit() {
        //log.info("Decision is: HIT");
        bot.getBotName().setInfo("HIT");
        if (move.isRunning()) {
            move.moveContinuos();
            move.jump(0.4);
            move.stopMovement();
            item = null;
        }
        getAct().act(new Rotate().setAmount(32000));
    }

    //////////////////
    // STATE PURSUE //
    //////////////////
    /**
     * State pursue is for pursuing enemy who was for example lost behind a
     * corner. How it works?: <ol> <li> initialize properties <li> obtain path
     * to the enemy <li> follow the path - if it reaches the end - set lastEnemy
     * to null - bot would have seen him before or lost him once for all </ol>
     */
    protected void statePursue() {
        //log.info("Decision is: PURSUE");
        ++pursueCount;
        if (pursueCount > 10) {//30 au depart
            reset();
        }
        if (enemy != null) {
        	bot.getBotName().setInfo("PURSUE");
        	move.moveTo(enemy);
        	item = null;
        } else {
        	reset();
        }
    }
    protected int pursueCount = 0;

    //////////////////
    // STATE MEDKIT //
    //////////////////
    protected void stateMedKit() {
        //log.info("Decision is: MEDKIT");
        Item item = items.getPathNearestSpawnedItem(ItemType.Category.HEALTH);
        if (item == null) {
        	log.warning("NO HEALTH ITEM TO RUN TO => ITEMS");
        	stateRunAroundItems();
        } else {
        	bot.getBotName().setInfo("MEDKIT");
        	move.moveTo(item);
        	this.item = item;
        }
    }

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
    protected List<Item> itemsToRunAround = null;

    protected void stateRunAroundItems() {
        //log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        
        if (navigation.isNavigatingToItem()) return; // -----------?
        
        List<Item> interesting = new ArrayList<Item>();
        
        // ADD WEAPONS
        for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
        	if (!weaponry.hasLoadedWeapon(itemType)) interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        // ADD ARMORS
        for (ItemType itemType : ItemType.Category.ARMOR.getTypes()) {
        	interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        // ADD QUADS
        interesting.addAll(items.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
        // ADD HEALTHS
        if (info.getHealth() < 100) {
        	interesting.addAll(items.getSpawnedItems(UT2004ItemType.HEALTH_PACK).values());
        }
        
        Item item = MyCollections.getRandom(tabooItems.filter(interesting)); // à modfier completement
        if (item == null) {
        	log.warning("NO ITEM TO RUN FOR!");
        	if (move.isRunning()) return;
        	bot.getBotName().setInfo("RANDOM NAV");
        	move.moveTo(navPoints.getRandomNavPoint());
        } else {
        	this.item = item;
        	log.info("RUNNING FOR: " + item.getType().getName());
        	bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
        	move.moveTo(item); 	
        }        
    }

    ////////////////
    // BOT KILLED //
    ////////////////
    @Override
    public void botKilled(BotKilled event) {
    	reset();
    }

    /**
     * Simple method that starts continuous movement forward + marking the
     * situation (i.e., setting {@link RaycastingBot#moving} to true, which
     * might be utilized later by the logic).
     */
    protected void goForward() {
        move.moveContinuos();
        moving = true;
    }

    public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM

        
        //new UT2004BotRunner(RaycastingBot.class, "Terminator").setMain(true).startAgent();
        
        String host = "localhost";//"locahost"
        int port = 3000;

        if (args.length > 0)
        {
            host = args[0];
        }
        if (args.length > 1)
        {
            String customPort = args[1];
            try
            {
                port = Integer.parseInt(customPort);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid port. Expecting numeric. Resuming with default port: " + port);
            }
        }
        
        UT2004BotRunner runner = new UT2004BotRunner(RaycastingBot.class, "SuperTeam", host, port);
        runner.setMain(true);
        runner.setName("Terminator");
        //runner.setLogLevel(Level.OFF);
        runner.startAgent();
    }
}
