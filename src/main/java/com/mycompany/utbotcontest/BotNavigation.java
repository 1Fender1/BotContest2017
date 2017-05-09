package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.astar.UT2004AStar;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.stuckdetector.AccUT2004DistanceStuckDetector;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.StopWatch;
import cz.cuni.amis.utils.collections.MyCollections;
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
    
    private IUT2004Navigation navigation;
    
    private UT2004AStar navigationAS;
    
    private int indexNavAS;
    
    List<NavPoint> cheminAS;
    
    boolean navigatingToItem=false; 
    boolean navigating=false; 
    
    private Alea alea;
    
    private AdvancedLocomotion move;
    
    private long debut=0;
    
    private final long tempsStrafe=700;
        
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
        move = mainBot.getMove();
        levelGeometryModule = mainBot.getLevelGeometryModule();
        
        navPoints = mainBot.getNavPoints();
        nmNav = mainBot.getNmNav();
        navigation = mainBot.getNavigation();
        navigationAS= new UT2004AStar(bot);
        indexNavAS=0;
        nmNav.getPathExecutor().addStuckDetector(stuckDetector);
        alea = new Alea();
        //this.meshInit = meshInit;
    }
    
    public void botFocus()
    {
        if (navigation.isNavigating())
        {
            try{
                List<ILocated> chemin = navigation.getPathExecutor().getPath();
                int element = navigation.getPathExecutor().getPathElementIndex();
                int tailleChemin = navigation.getPathExecutor().getPath().size();
                Alea a = new Alea();
                List<ILocated> debutChemin = new ArrayList<ILocated>();
                for (int i = 0; i < element; i++)
                {
                    debutChemin.add(chemin.get(i));
                }
                List<ILocated> finChemin = new ArrayList<ILocated>();
                for (int i = element; i < tailleChemin; i++)
                {
                    debutChemin.add(chemin.get(i));
                }
                if (a.pourcentDeChance(70))
                {
                    navigation.setFocus(MyCollections.getRandom(finChemin));
                }
                else
                {
                    navigation.setFocus(MyCollections.getRandom(debutChemin));
                }
            }catch(NullPointerException npe)
            {
                System.out.println("Erreur sur la navigation");
            }
        }
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

    
    /*public boolean reachable(Item item)
    { 
        Location botLoc = bot.getLocation();
        Location itemLoc = item.getLocation();
        System.out.println("BUGGGGG");
        if(botLoc!=null && itemLoc != null){
            List<ILocated> chemin = navigation.getPathPlanner().computePath(navigation.getNearestNavPoint(botLoc), navigation.getNearestNavPoint(itemLoc)).get();
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
        return false;
    }*/
    
    public boolean reachable(ILocated item)
    {
        Location botLoc = bot.getLocation();
        Location itemLoc = item.getLocation();
        List<ILocated> chemin = navigation.getPathPlanner().computePath(navigation.getNearestNavPoint(botLoc), navigation.getNearestNavPoint(itemLoc)).get();
        if (chemin == null)
        {
            return false;
        }
        else
        {   
            return true;
        }
        
    }
    
    
    public boolean isNavigatingToItem(){
        return navigatingToItem;
    }
    
    public boolean isNavigating(){
        return navigating;
    }
    
    public void mouvement(List<NavPoint> chemin){
        if(System.currentTimeMillis()-debut>tempsStrafe){
            move.moveContinuos();
            mouvementAleatoire();
            debut = System.currentTimeMillis();
        }
        else{
            move.moveTo(cheminAS.get(indexNavAS));
        }
    }
    
    public boolean navigate() {
        if(isNavigating()){
            if(navPoints.getNearestNavPoint().equals(cheminAS.get(indexNavAS))){
                if(cheminAS.size()-1==indexNavAS){
                    if(100<info.getDistance(cheminAS.get(indexNavAS))){
                        System.out.println("je suis a plus de 10cm de mon objectif : " + cheminAS.get(indexNavAS).getId().getStringId());
                        mouvement(cheminAS);
                    }
                    else{
                        navigatingToItem=false;
                        navigating=false;
                    }
                }
                else{
                    indexNavAS++;
                    mouvement(cheminAS);
                    
                }
            }
            else{
                mouvement(cheminAS);
            }
        }
        else{
            Location botLoc = bot.getLocation();
            cheminAS = navigationAS.computePath(navigation.getNearestNavPoint(botLoc), navPoints.getRandomNavPoint()).get();
             if(cheminAS!=null){
                indexNavAS=0;
                mouvement(cheminAS);
                navigatingToItem=false;
                navigating=true;
            }
            
        }
        return true;
            
    }
    
    
    
    public boolean navigate(ILocated item){
        if(isNavigatingToItem()){
            navigate();
        }
        else{
            Location botLoc = bot.getLocation();
            Location itemLoc = item.getLocation();
            cheminAS = navigationAS.computePath(navigation.getNearestNavPoint(botLoc), navigation.getNearestNavPoint(itemLoc)).get();
            if(cheminAS!=null){
                indexNavAS=0;
                mouvement(cheminAS);
                navigatingToItem=true;
                navigating=true;
            }
        }
        /*if (navigation.isNavigating()) return false;
        
        if(reachable(item)){
            navigation.navigate(item);
        }       */
    	return false;
    }
    
    /*public boolean navigate(Item item) {
        
        if (navigation.isNavigating()) return false;
         
        if(reachable(item)){
            navigation.navigate(item);
        }
        
    	return false;
    }

    public boolean navigate(Player item) {

    if (navigation.isNavigating()) return false;
    
    if(reachable(item)){
        navigation.navigate(item);
    }

    return false;
}*/
  
    public void mouvementAleatoire(){
        if(isNavigating()){
            /*if(stuckDetector.isStuck()){
                handleStuck();
            }
            else{
                List<ILocated> chemin = navigation.getPathExecutor().getPath();
                int index = navigation.getPathExecutor().getPathElementIndex();
                if(index>=0 && index+1<chemin.size()){
                    NavPoint currentNP = navigation.getNearestNavPoint(chemin.get(index));
                    NavPoint nextNP = navigation.getNearestNavPoint(chemin.get(index+1));
                    if(currentNP.isLiftCenter() || currentNP.isJumpSpot() || currentNP.isInvSpot() || nextNP.isLiftCenter()){
                         
                        if(currentNP.isLiftCenter() || nextNP.isLiftCenter()){
                            System.out.println("-------------- lift ---------");
                            handleLift(chemin, nextNP.isLiftCenter()); 
                        }
                        else{
                            System.out.println("-------------- jump --------");
                        }
                    }
                    else{
                        dansA=false;*/
                        double distanceStrafe = 100;
                            
                            if(/*700<System.currentTimeMillis()-debut &&*/ alea.pourcentDeChance(100.0)){
                                if(alea.uneChanceSur(2)){
                                    move.strafeRight(distanceStrafe);
                                }
                                else{
                                    move.strafeLeft(distanceStrafe);
                                }
                            }
                    
                
            
        }
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
