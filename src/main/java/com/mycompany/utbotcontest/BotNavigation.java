package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.AccUT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.StopWatch;
import cz.cuni.amis.utils.exception.PogamutInterruptedException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JB
 */
public class BotNavigation {
    
    /**
    * Item we're running for. 
    */
    protected Item item = null;
    /**
    * Taboo list of items that are forbidden for some time.
    */
    protected TabooSet<Item> tabooItems = null;

    AccUT2004DistanceStuckDetector stuckDetector;


    private final boolean stuckBotWaiting = false;
    private final boolean stuckEnabled = true;

    private List<Location> raycastLocations = new ArrayList<Location>();
     
    private boolean raycasting = false;

    private NavMeshNavigation nmNav;
    
    private Bot mainBot;
    
    private UT2004Bot bot;
    
    private LevelGeometryModule levelGeometryModule;
    
    private AgentInfo info;
    
    private UT2004Draw draw;
    
    private NavPoints navPoints;
    
    private MeshInit meshInit;
        
    public BotNavigation(Bot mainBot, MeshInit meshInit)
    {
        this.mainBot = mainBot;
        
        bot = mainBot.getBot();
        tabooItems = new TabooSet<Item>(bot);
        stuckDetector = new AccUT2004DistanceStuckDetector(bot);
        stuckDetector.setBotWaiting(stuckBotWaiting);
        stuckDetector.setEnabled(stuckEnabled);
        
        info = mainBot.getInfo();
        draw = mainBot.getDraw();
        
        levelGeometryModule = mainBot.getLevelGeometryModule();
        
        navPoints = mainBot.getNavPoints();
        nmNav = mainBot.getNmNav();
        
        nmNav.getPathExecutor().addStuckDetector(stuckDetector);
        
        this.meshInit = meshInit;
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
		
		meshInit.say("Let's do some raycasting from here (client-side of course) up to " + ((int)Math.round(raycastDistance)) + " UT units distance.");
		
		int[] directions = new int[]{-1, 0, 1};
		
		List<LevelGeometry.RaycastResult> results = new ArrayList<LevelGeometry.RaycastResult>(); 
		
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
		
		for (LevelGeometry.RaycastResult raycast : results) {
                    if (raycast.hit) {
                        draw.drawLine(Color.RED, raycast.from, raycast.hitLocation);
                        draw.drawCube(Color.ORANGE, raycast.hitLocation, 8);
                        draw.drawPolygon(Color.orange, levelGeometryModule.getLevelGeometry().getTriangle(raycast.hitTriangle));
                    } else {
                        draw.drawLine(Color.BLUE, raycast.from, raycast.to);
                        draw.drawCube(Color.CYAN, raycast.to, 8);
                    }
		}
		
		meshInit.say(raycastCount + " raycasts in " + watch.timeStr());
		
		try {
                    Thread.sleep(3000);
		} catch (InterruptedException e) {
                    throw new PogamutInterruptedException(e, this);
		}	
	}

	
    
    
    public boolean reachable(Item item)
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
            return true;
        }
    }
    
    public boolean reachable(ILocated item)
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
            return true;
        }

}
    
    public boolean navigate() {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
        
    	/*if (stuckDetector.isStuck())
        {
            stuckDetector.reset();
            System.out.println("---------J'essaie d'atteindre ce foutu point ! --------------");
            return false;
        }*/

        
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
    
    public boolean navigate(ILocated item) {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    	//.setBotTarget(item);
        /*if (stuckDetector.isStuck())
        {
            stuckDetector.reset();
            System.out.println("---------J'essaie d'atteindre ce foutu point ! --------------");
            return false;
        }*/
        
    	if (nmNav.isNavigating()) return false;
    	
    	if (raycasting) {
    		raycast();
    	}
    	
        
        
    	if (!reachable(item))
            return false;
    	
        nmNav.navigate(item);
        
    	return false;
    }
    
    public boolean navigate(Item item) {
    	//body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    	/*stuckDetector.setBotTarget(item);
        if (stuckDetector.isStuck())
        {
            stuckDetector.reset();
            System.out.println("---------J'essaie d'atteindre ce foutu point ! --------------");
            return false;
        }*/
        
    	if (nmNav.isNavigating()) return false;
    	
    	if (raycasting) {
    		raycast();
    	}
        
    	if (!reachable(item))
            return false;
    	
        nmNav.navigate(item);
        
    	return false;
    }

    public boolean navigate(Player item) {
    //body.getCommunication().sendGlobalTextMessage("SPEED: " + info.getVelocity().size());
    /*stuckDetector.setBotTarget(item);
    if (stuckDetector.isStuck())
    {
        stuckDetector.reset();
        System.out.println("---------J'essaie d'atteindre ce foutu point ! --------------");
        return false;
    }*/

    if (nmNav.isNavigating()) return false;

    if (raycasting) {
        raycast();
    }

    if (!reachable(item))
        return false;

    nmNav.navigate(item);

    return false;
}
        
        
    
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public TabooSet<Item> getTabooItems() {
        return tabooItems;
    }

    public void setRaycasting(boolean raycasting) {
        this.raycasting = raycasting;
    }

    
  
}
