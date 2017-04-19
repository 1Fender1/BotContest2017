package com.mycompany.utbotcontest;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.utils.collections.MyCollections;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometry.RaycastResult;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.utils.StopWatch;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.exception.PogamutInterruptedException;


/**
 * Example of Simple Pogamut bot, that randomly walks around the map. Bot is
 * incapable of handling movers so far. 
 * 
 * <p><p> 
 * The crucial method to read
 * through is {@link Bot#botInitialized(GameInfo, ConfigChange, InitedMessage)},
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
public class Bot extends UT2004BotModuleController {

 
    /**
     * How fast should we move? Interval <0, 1>.
     */
    private final float moveSpeed = 0.6f;
    
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

    
	// ======
	// PARAMS
	// ======
	
    private boolean speak            = false;
    private final boolean drawNavMesh      = true;
    private final boolean drawOffMeshLinks = false;
    private boolean raycasting = false;
    
    // =======
    // RUNTIME
    // =======

    private int errorMsg = 0;
    
	private boolean navMeshDrawn = false;    
    private double waitForMesh = 0;
    private double waitingForMesh = 0;
    
    private boolean offMeshLinksDrawn = false;
    private double waitForOffMeshLinks = 0;
    private double waitingForOffMeshLinks = 0;
    
    
    private int sayState = -2;
    private double sayNext = 0;
    
    private boolean navigate = false;
    
    private List<Location> raycastLocations = new ArrayList<Location>();

    @Override
    public void mapInfoObtained() {
    	// YOU CAN USE navBuilder IN HERE
    	
    	// IN WHICH CASE YOU SHOULD UNCOMMENT FOLLOWING LINE AFTER EVERY CHANGE
    	//navMeshModule.setReloadNavMesh(true); // tells NavMesh to reconstruct OffMeshPoints    	
    }
    
    
    
    
    @Override
    public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
        // initialize rays for raycasting
        body.getConfigureCommands().setBotAppearance("HumanMaleA.EgyptMaleA");
        getInitializeCommand().setDesiredSkill(5);
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
        return new Initialize().setName("T80" + (instanceCount++)).setDesiredSkill(5);
    }
    
        @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
    	if (navMeshModule.isInitialized()) {
    		say("Hello! Prepare to be amazed with NavMesh!");
    	} else {
    		say("NavMesh failed to initialize :-(");
    		String map = gameInfo.getLevel();
    		say("Current map: " + map);
    		File meshFile = new File("meshes/" + map + ".navmesh");
    		if (meshFile.exists()) {
    			say("NavMesh file does not exist at: " + meshFile.getAbsolutePath());
    			say("Provide NavMesh file and rerun the bot.");
    		} else {
    			say("NavMesh file DOES exist at: " + meshFile.getAbsolutePath());
    			say("But there is probably some bogus in there... try to renegerate / fix and rerun the bot.");
    		}
    	}
    	
    	//UNCOMMENT FOR DETAILED INFO ABOUT NAVIGATION:
    	//nmNav.setLogLevel(Level.FINER);
    }
    
    private boolean speak() {
    	sayNext -= info.getTimeDelta();
    	if (sayNext > 0) return true;
    	
    	// SAY PHRASE AND SETUP NEXT STATE
    	
    	sayNext = 8;
		switch(sayState) {
		case -2: say("Done, now sit and watch how I can navigate around!"); sayNext = 3; break;
		case -1: navigate = true; break;
		case 0: say("Notice how I'm NOT using navigation graph that comes with UT2004."); break;
		case 1: say("Yeah, that's right, I DO NOT NEED IT!"); break;
		case 2: say("Whooo that feels great!"); break;
		case 3: say("You know, the freedom to move around like a human."); sayNext = 14; break;
		case 4: say("..."); sayNext = 1.5; break;
		case 5:
			if (navMeshModule.getNavMesh().getOffMeshPoints().size() == 0) {
				sayState = 17;
				return true;
			}
			say("Well, but I'm not honest with you."); sayNext = 4; break;
		case 6: say("I do need the navigation graph from time to time."); sayNext = 7; break;
		case 7:
			say("Let me show you.");
			nmNav.stopNavigation();
			sayNext = 1;
			navigate = false;
			break;
		case 8:
			if (!offMeshLinksDrawn)	say("I actually need it to compute off-mesh links, see?");
			if (!drawOffMeshLinks()) {
				sayNext = 0;
				sayState = 8;
				return false;
			}			
			break;
		case 9: say("But I'm using them to make my NavMesh navigation even cooler!"); break;
		case 10: say("I LOVE IT!"); break;
		case 11: say("Send Kudos to Bohuslav Machac, former student of Charles University in Prague, for tinkering my navigation code out!"); sayNext = 20; break;
		case 12: say("Still here?"); sayNext = 2; break;
		case 13: say("You might like to know that we have a tool chain that can generate NavMesh out of any UT2004 map."); sayNext = 5; break;
		case 14: say("So you can run me on custom maps as well!"); sayNext = 3; break;
		case 15: say("Check out: svn://artemis.ms.mff.cuni.cz/pogamut/trunk/project/Addons/UT2004NavMeshTools"); sayNext = 3; break;
		case 16: say("And send some Kudos to Mikko Mononen for creating Recast as well!"); sayNext = 10; break;
		case 17:
			if (levelGeometryModule.isInitialized()) {
				say("Hey, I've just realized that I have info about the geometry of the map as well!");
				sayNext = 3;
			} else {
				say("Sadly I do not have info about the map geometry with me right now, so I cannot show you may raycasting abilities...");
				speak = false;
			}
			break;
		case 18: 
			say("Cool! That means I can do some raycasting as well!");
			nmNav.stopNavigation();
			raycasting = true;
			break;
		case 19:
			speak = false;
			break;
		}
		++sayState;
		return true;
	}
    
    private boolean reachable(Item item)
    {
        Location botLoc = bot.getLocation();
        Location itemLoc = item.getLocation();
        
        
        List<ILocated> chemin = nmNav.getPathPlanner().computePath(botLoc, itemLoc).get();
        if (chemin == null)
        {
            tabooItems.add(item);
            return false;
        }
        else
        {   
            System.out.println("chemin");
            for (ILocated loc : chemin)
            {
                System.out.println(loc);
            }
            return true;
        }
        //System.out.println("dernier point du parcour : " + chemin.get(chemin.size()).getLocation().toString() + " point visé" + item.getLocation().toString());
        //return (chemin.get(chemin.size()).getLocation().equals(item.getLocation()));
    }
    
        private boolean reachable(ILocated item)
        {
            Location botLoc = bot.getLocation();
            Location itemLoc = item.getLocation();


            List<ILocated> chemin = nmNav.getPathPlanner().computePath(botLoc, itemLoc).get();

            if (chemin == null)
            {
                return false;
            }
            else
            {   
                System.out.println("chemin");
                for (ILocated loc : chemin)
                {
                    System.out.println(loc);
                }
                return true;
            }
       
        //System.out.println("dernier point du parcour : " + chemin.get(chemin.size()).getLocation().toString() + " point visé" + item.getLocation().toString());
        //return (chemin.get(chemin.size()).getLocation().equals(item.getLocation()));
    }
    
    private boolean navigate() {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    	
    	if (nmNav.isNavigating()) 
            return false;
    	
    	if (raycasting) {
            raycast();
    	}
    	
    	NavPoint np = navPoints.getRandomNavPoint();
        
        if (!reachable(np))
            return false;
        
        nmNav.navigate(np);
    	
    	return false;
    }
    
     private boolean navigate(ILocated item) {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    	
    	if (nmNav.isNavigating()) return false;
    	
    	if (raycasting) {
    		raycast();
    	}
    	
        
        
    	if (!reachable(item))
            return false;
    	
        nmNav.navigate(item);
        
    	return false;
    }
    
    private boolean navigate(Item item) {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    	
    	if (nmNav.isNavigating()) return false;
    	
    	if (raycasting) {
    		raycast();
    	}
    	
        
        
    	if (!reachable(item))
            return false;
    	
        nmNav.navigate(item);
        
    	return false;
    }
    
    

	private void raycast() {
    	if (!levelGeometryModule.isInitialized()) return;
    	
    	nmNav.stopNavigation();
    	
		double distance = Double.POSITIVE_INFINITY;
		for (Location loc : raycastLocations) {
			double d = loc.getDistance(info.getLocation());
			if (d < distance) distance = d;
		}
		if (distance < 1500) return;
		raycastLocations.add(info.getLocation());
		
		double raycastDistance = 500;
		
		say("Let's do some raycasting from here (client-side of course) up to " + ((int)Math.round(raycastDistance)) + " UT units distance.");
		
		int[] directions = new int[]{-1, 0, 1};
		
		List<RaycastResult> results = new ArrayList<RaycastResult>(); 
		
		StopWatch watch = new StopWatch();
		watch.start();
		
		int raycastCount = 0;
		
		for (int x : directions) {
			for (int y : directions) {
				for (int z : directions) {
					// RULE OUT INVALID DIRECTIONS
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					// DO RAYCAST
					++raycastCount;
					Location rayVector = new Location(x,y,z).getNormalized().scale(raycastDistance);
					Location rayFrom = info.getLocation();
					Location rayTo = info.getLocation().add(rayVector);
					results.add(levelGeometryModule.getLevelGeometry().raycast(rayFrom, rayTo));
				}	
			}	
		}
		
		watch.stop();
		
		for (RaycastResult raycast : results) {
			if (raycast.hit) {
				draw.drawLine(Color.RED, raycast.from, raycast.hitLocation);
				draw.drawCube(Color.ORANGE, raycast.hitLocation, 8);
				draw.drawPolygon(Color.orange, levelGeometryModule.getLevelGeometry().getTriangle(raycast.hitTriangle));
			} else {
				draw.drawLine(Color.BLUE, raycast.from, raycast.to);
				draw.drawCube(Color.CYAN, raycast.to, 8);
			}
		}
		
		say(raycastCount + " raycasts in " + watch.timeStr());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new PogamutInterruptedException(e, this);
		}	
	}

	
	
	private boolean drawNavMesh() { 
		if (!navMeshDrawn) {
    		navMeshDrawn = true;
    		say("Drawing NavMesh...");
    		navMeshModule.getNavMeshDraw().clearAll();
    		navMeshModule.getNavMeshDraw().draw(true, false);
    		say("Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");
    		
    		waitForMesh = navMeshModule.getNavMesh().getPolys().size() / 40 - 2.5;
    		waitingForMesh = -info.getTimeDelta();
    	}
		
		if (waitForMesh > 0) {
    		waitForMesh -= info.getTimeDelta();
    		waitingForMesh += info.getTimeDelta();
    		if (waitingForMesh > 2) {
    			waitingForMesh = 0;
    			say(((int)Math.round(waitForMesh)) + "s...");
    		}
    		if (waitForMesh > 0) {
    			return false;
    		}    		
    	}
		
		return true;
	}
	
	private boolean drawOffMeshLinks() { 		
		if (!offMeshLinksDrawn) {
			offMeshLinksDrawn = true;
			
			if (navMeshModule.getNavMesh().getOffMeshPoints().size() == 0) {
				say("Ha! There are no off-mesh points / links within this map!");
				return true;
			}
			
			say("Drawing OffMesh Links...");
    		navMeshModule.getNavMeshDraw().draw(false, true);
    		say("Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");    		
    		waitForOffMeshLinks = navMeshModule.getNavMesh().getOffMeshPoints().size() / 10;
    		waitingForOffMeshLinks = -info.getTimeDelta();
    	}
		
		if (waitForOffMeshLinks > 0) {
			waitForOffMeshLinks -= info.getTimeDelta();
			waitingForOffMeshLinks += info.getTimeDelta();
    		if (waitingForOffMeshLinks > 2) {
    			waitingForOffMeshLinks = 0;
    			say(((int)Math.round(waitForOffMeshLinks)) + "s...");
    		}
    		if (waitForOffMeshLinks > 0) {
    			return false;
    		}    		
    	}
		
		return true;
	}

	private void say(String text) {
		if (speak) { 
			body.getCommunication().sendGlobalTextMessage(text);
		}
		log.info("SAY: " + text);
	}

    
    
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
    
    
    /**
     * Resets the state of the Hunter.
     */
    protected void reset() {
    	item = null;
        enemy = null;
        move.stopMovement();
        //interesting = null;
        itemsToRunAround = null;
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
        
          	if (!navMeshModule.isInitialized()) {
    		// Check logs for information, where the bot expects NavMesh file to be present...
    		say("NAV MESH NOT INITIALIZED - SEE STARTUP LOGS FOR MORE INFO... (" + (++errorMsg + ")"));
    		return;
    	}
    	    	
    	if (drawNavMesh) {
    		if (!drawNavMesh()) return;
    	}
    	
    	if (!speak && drawOffMeshLinks) {
    		if (!drawOffMeshLinks()) return;
    	}
    	
    	if (speak) {
    		if (!speak()) return;
    	} else {
    		navigate = true;
    	}
        
        
        // mark that another logic iteration has began
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
        stateRunAroundItems();

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

        boolean shooting = false;
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
                //nmNav.navigate(enemy);
                navigate(enemy);
                runningToPlayer = true;
            }
        } else {
            runningToPlayer = false;
            //move.stopMovement();
            nmNav.stopNavigation();
        }
        
        item = null;
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
                //nmNav.navigate(enemy);
                navigate(enemy);
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
                //nmNav.navigate(item);
                navigate(item);
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
        if (nmNav.isNavigatingToItem()) return;
        
        List<Item> interesting = new ArrayList<Item>();
        
        // ADD WEAPONS
        for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
        	if (!weaponry.hasLoadedWeapon(itemType)) 
                    interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        // ADD ARMORS
        for (ItemType itemType : ItemType.Category.ARMOR.getTypes()) {
            if (info.getArmor() < 50)
            {
                interesting.addAll(items.getSpawnedItems(UT2004ItemType.SHIELD_PACK).values());
            }
            else
            {
                interesting.addAll(items.getSpawnedItems(UT2004ItemType.SUPER_SHIELD_PACK).values());
            }
        }
        // ADD QUADS
        interesting.addAll(items.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
        
        // ADD HEALTHS
        
        for (ItemType itemType : ItemType.Category.HEALTH.getTypes())
        {
            if (info.getHealth() < 100) {
        	interesting.addAll(items.getSpawnedItems(UT2004ItemType.HEALTH_PACK).values());
            }
            else if (info.getHealth() >= 100 && info.getHealth() < 199)
            {
                interesting.addAll(items.getSpawnedItems(UT2004ItemType.MINI_HEALTH_PACK).values());
            }
        }

        Item item = MyCollections.getRandom(tabooItems.filter(interesting));

        
        if (item == null) {
        	log.warning("NO ITEM TO RUN FOR!");
        	if (nmNav.isNavigating()) return;
        	bot.getBotName().setInfo("RANDOM NAV");
                //nmNav.navigate(navPoints.getRandomNavPoint().getLocation());
                navigate();
        } else {
        	this.item = item;
        	log.info("RUNNING FOR: " + item.getType().getName());
        	bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
                //nmNav.navigate(item);
                navigate(item);
        }        
    }

    ////////////////
    // BOT KILLED //
    ////////////////
    @Override
    public void botKilled(BotKilled event) {
    	reset();
    }

}
