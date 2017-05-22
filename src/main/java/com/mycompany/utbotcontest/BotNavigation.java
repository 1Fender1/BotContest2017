package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Raycasting;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.StopWatch;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutInterruptedException;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    protected TabooSet<Item> tabooItems;

    AccUT2004DistanceStuckDetector stuckDetector;


    private final boolean stuckBotWaiting = false;
    private final boolean stuckEnabled = true;

    private List<Location> raycastLocations = new ArrayList<Location>();
     
    private boolean raycasting = false;
    
    private Bot mainBot;
    
    private UT2004Bot bot;
    
    private LevelGeometryModule levelGeometryModule;
    
    private AgentInfo info;
    
    private UT2004Draw draw;
    
    private NavPoints navPoints;
    
    private MeshInit meshInit;
    
    private IUT2004Navigation navigation; 
    
    private int indexNavAS;
    
    private List<ILocated> chemin;
    
    private float radius = 100;
    
    private float radiusLift = 50;
    
    private float height = 100;
    
    boolean navigatingToItem=false; 
    boolean navigating=false; 
    
    private Alea alea;
    
    private AdvancedLocomotion move;
    
    private long debut=0;
    
    private final long tempsStrafe=700;
    
    private Raycasting raycast;
        
    private double notMovingSince;
    
    private NavigationMemory memory;
    
    public BotNavigation(Bot mainBot, MeshInit meshInit)
    {
        this.mainBot = mainBot;
        
        raycast=mainBot.getRaycasting();
        
        bot = mainBot.getBot();
        
        stuckDetector = new AccUT2004DistanceStuckDetector(bot);
        stuckDetector.setBotWaiting(stuckBotWaiting);
        stuckDetector.setEnabled(stuckEnabled);
        
        info = mainBot.getInfo();
        draw = mainBot.getDraw();
        move = mainBot.getMove();
        levelGeometryModule = mainBot.getLevelGeometryModule();
        
        navPoints = mainBot.getNavPoints();
        navigation = mainBot.getNavigation();
        indexNavAS=0;
        navigation.getPathExecutor().addStuckDetector(stuckDetector);
        alea = new Alea();
        //this.meshInit = meshInit;
        memory = new NavigationMemory();
    }
    
    
    
    public ILocated botFocus()
    {
        if (isNavigating())
        {
            try{
                int element = indexNavAS;
                int tailleChemin = chemin.size();
                Alea a = new Alea();
                List<ILocated> debutChemin = new ArrayList<ILocated>();
                for (int i = 0; i < element; i++)
                {
                    debutChemin.add(chemin.get(i));
                }
                List<ILocated> finChemin = new ArrayList<ILocated>();
                for (int i = element; i < tailleChemin; i++)
                {
                    finChemin.add(chemin.get(i));
                }
                if (a.pourcentDeChance(70))
                {
                    return MyCollections.getRandom(finChemin);
                }
                else
                {
                    return MyCollections.getRandom(debutChemin);
                }
            }catch(NullPointerException npe)
            {
                System.out.println("Erreur sur la navigation");
            }
        }
        return null;
    }

    
    private void raycast() {
    	if (!levelGeometryModule.isInitialized()) return;
    	
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
    
    public void stopNavigation(){
        navigating=false;
        navigatingToItem=false;
    }
    
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
    private Location stuckLocation = null;
    public boolean isStuck() throws IOException{
        if(isNavigating()){
            if(stuckLocation==null){
                stuckLocation=info.getLocation();
                notMovingSince=System.currentTimeMillis();
            }
            else{
                if(notMovingSince==0){
                    notMovingSince=System.currentTimeMillis();
                }
                else{
                    if(System.currentTimeMillis()-notMovingSince>800){
                        if(stuckLocation.getDistance(info.getLocation())<75){
                            if (indexNavAS > 0)
                                memory.addInfo(mainBot.getGame().getMapName(), navPoints.getNearestNavPoint(chemin.get(indexNavAS-1)).getId().getStringId(), navPoints.getNearestNavPoint(chemin.get(indexNavAS)).getId().getStringId());
                            if(chemin!=null && indexNavAS<chemin.size()){
                                navigation.setFocus(chemin.get(indexNavAS));
                                ILocated vue = mainBot.getFocus();
                                if (vue != null)
                                    move.strafeTo(chemin.get(indexNavAS), vue);
                                else
                                    move.moveTo(chemin.get(indexNavAS));
                            }
                            move.doubleJump(0.35,700);
                            if(chemin!=null && indexNavAS<chemin.size()){
                                navigation.setFocus(chemin.get(indexNavAS));
                                ILocated vue = mainBot.getFocus();
                                if (vue != null)
                                    move.strafeTo(chemin.get(indexNavAS), vue);
                                else
                                    move.moveTo(chemin.get(indexNavAS));
                            }
                            System.out.println("stopNavig");
                            navigatingToItem=false;
                            navigating=false;
                            return true;
                        }
                        else{
                            stuckLocation=info.getLocation();
                            notMovingSince=System.currentTimeMillis();
                        }
                    }
                }
                
            }
            /*if(info.getVelocity().x<5 && info.getVelocity().y<5 && info.getVelocity().z<5){
                if(notMovingSince==0){
                    notMovingSince=System.currentTimeMillis();
                }
                else{
                    if(System.currentTimeMillis()-notMovingSince>800){
                        System.out.println("stopNavig");
                        navigatingToItem=false;
                        navigating=false;
                        return true;
                    }
                }
            }*/
        }
        return false;
    }
    
    private void mouvFocus(){
        ILocated vue = mainBot.getFocus();
        if (vue != null)
                move.strafeTo(chemin.get(indexNavAS), vue);
            else
                move.moveTo(chemin.get(indexNavAS));
    }
    
    public void mouvement(){
        if(indexNavAS>0){
            NavPointNeighbourLink link = navPoints.getNearestNavPoint(chemin.get(indexNavAS)).getIncomingEdges().get(navPoints.getNearestNavPoint(chemin.get(indexNavAS-1)).getId());  
            if(link!=null && link.getNeededJump()!=null){
                mouvFocus();
                move.doubleJump(0.35,700);
                mouvFocus();
                /*else{
                    if(mainBot.rayjump.isResult()){
                        //move.turnTo(chemin.get(indexNavAS));
                        mouvFocus();
                        move.doubleJump(0.35,700);
                        mouvFocus();
                    }
                    else{
                        //move.turnTo(chemin.get(indexNavAS));
                        mouvFocus();
                    }
                }*/
            }
            else{
                if(navPoints.getNearestNavPoint(chemin.get(indexNavAS).getLocation()).isLiftCenter() || navPoints.getNearestNavPoint(chemin.get(indexNavAS-1).getLocation()).isLiftCenter()){
                    notMovingSince=System.currentTimeMillis();
                    if(Math.abs(info.getLocation().getDistanceZ(chemin.get(indexNavAS).getLocation()))<50){
                        mouvFocus();
                    }
                }
                else{
                    mouvFocus();
                }
            }
            
        }
        else{
            //move.turnTo(chemin.get(indexNavAS));
            mouvFocus();
        }
    }
   
    public boolean navigate() throws IOException {
        if(isNavigating() && (isNavigatingToItem() || !isStuck())){
            if(chemin!=null && indexNavAS<chemin.size()){
                if(navPoints.getNearestNavPoint(chemin.get(indexNavAS).getLocation()).isJumpSpot() || navPoints.getNearestNavPoint(chemin.get(indexNavAS).getLocation()).isJumpDest() || navPoints.getNearestNavPoint(chemin.get(indexNavAS).getLocation()).isLiftCenter() || navPoints.getNearestNavPoint(chemin.get(indexNavAS).getLocation()).isInvSpot()){
                    if(info.getLocation().getDistance2D(chemin.get(indexNavAS).getLocation()) < radiusLift && Math.abs(info.getLocation().getDistanceZ(chemin.get(indexNavAS).getLocation()))<height){
                        if(chemin.size()-1==indexNavAS){
                            mouvement();
                            navigatingToItem=false;
                            navigating=false;
                        }
                        else{
                            indexNavAS++;
                            mouvement();
                        }
                    }
                    else{
                        mouvement();
                    }
                }
                else{
                    if(info.getLocation().getDistance2D(chemin.get(indexNavAS).getLocation()) < radius && Math.abs(info.getLocation().getDistanceZ(chemin.get(indexNavAS).getLocation()))<height){
                        if(chemin.size()-1==indexNavAS){
                            navigatingToItem=false;
                            navigating=false;
                        }
                        else{
                            indexNavAS++;
                            mouvement();
                        }
                    }
                    else{
                        mouvement();
                    }
                }
            }
            else{
                
                navigatingToItem=false;
                navigating=false;
            }
        }
        else{
            Location botLoc = bot.getLocation();
            //cheminAS = navigationAS.computePath(navigation.getNearestNavPoint(botLoc), navPoints.getRandomNavPoint()).get();
            chemin = navigation.getPathPlanner().computePath(navigation.getNearestNavPoint(botLoc), navPoints.getRandomNavPoint()).get();
            notMovingSince=0;
            if(chemin!=null){
                indexNavAS=0;
                mouvement();
                navigatingToItem=false;
                navigating=true;
            }
        }
        return true;
    }
    
    
    
    public boolean navigate(ILocated neededItem) throws IOException{
        if(isNavigatingToItem()){
            navigate();
        }
        else{
            Location botLoc = bot.getLocation();
            Location itemLoc = neededItem.getLocation();
            //cheminAS = navigationAS.computePath(navigation.getNearestNavPoint(botLoc), navigation.getNearestNavPoint(itemLoc)).get();
            chemin = navigation.getPathPlanner().computePath(navigation.getNearestNavPoint(botLoc), navigation.getNearestNavPoint(itemLoc)).get();
            if(chemin!=null){
                notMovingSince=0;
                indexNavAS=0;
                mouvement();
                navigatingToItem=true;
                navigating=true;
            }
            else{
                return false;
            }
        }
    	return true;
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

    public void setNavigatingToItem(boolean navigatingToItem) {
        this.navigatingToItem = navigatingToItem;
    }

    public void setNavigating(boolean navigating) {
        this.navigating = navigating;
    }

    
  
}
