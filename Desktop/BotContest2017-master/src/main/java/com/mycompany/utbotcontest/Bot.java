package com.mycompany.utbotcontest;


import com.sun.javafx.geom.Edge;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.utils.flag.FlagListener;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
import cz.cuni.amis.pogamut.base3d.worldview.object.Velocity;
import cz.cuni.amis.pogamut.multi.communication.worldview.object.ILocalWorldObject;
import cz.cuni.amis.pogamut.multi.communication.worldview.object.ISharedWorldObject;
import cz.cuni.amis.pogamut.multi.communication.worldview.object.IStaticWorldObject;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.unreal.communication.worldview.map.Box;
import cz.cuni.amis.pogamut.unreal.communication.worldview.map.IUnrealMap;
import cz.cuni.amis.pogamut.unreal.communication.worldview.map.IUnrealMapInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.MapExport;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavigationGraphBuilder;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.Visibility;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.model.VisibilityMatrix;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004MapTweaks.IMapTweak;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.UT2004Draw;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemTypeTranslator;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.KillBot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Respawn;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.communication.translator.bot.BotFSM;
import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.ItemTranslator;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.map.UT2004Map;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public boolean souldEscape = true;

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
    
    private Evasion evasion;
    
    private Comportement comportement;
    
    //private MeshInit meshInit;
    private ProbabilitesArmes probaA;
    
    private NavigationMemory memory;
    
    private Player enemy = null;
    
    private boolean navigate = false;
    
    //Probabilite
    public String lastWeaponUsed = "AssaultRifle";
    public Player lastEnemy = null;
    public double distanceBotTarget = 0;
    
    protected Weapon wp = null;
        
    protected boolean killed = true;
    
    //RAYCASTING
    protected static final String FRONT = "frontRay";
    //protected static final String LEFT45 = "left45Ray";
    protected static final String LEFT90 = "left90Ray";
    //protected static final String RIGHT45 = "right45Ray";
    protected static final String RIGHT90 = "right90Ray";
    protected AutoTraceRay left, front, right, rayjump;
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
    
    public final String RAYJUMP="RAYJUMP";
    
    protected ILocated focus;
    
    private boolean isLearning = false;
    
    private double firstTimeLearning = System.currentTimeMillis();;
    
    private double secondTimeLearning = 0.0;
    
    
    private void testDraw()
    {
        for (Entry<UnrealId,NavPoint> navP1 : navPoints.getNavPoints().entrySet())
        {
            for (Entry<UnrealId, NavPointNeighbourLink> arc : navP1.getValue().getOutgoingEdges().entrySet())
            {
                if (fwMap.checkLink(arc.getValue()))
                {
                    NavPoint p1 = arc.getValue().getFromNavPoint();
                    NavPoint p2 = arc.getValue().getToNavPoint();
                    if ((p1.isLiftCenter() || p1.isLiftExit() || p2.isLiftJumpExit()) && (p2.isLiftCenter() || p2.isLiftExit() || p2.isLiftJumpExit()))
                    {
                        draw.drawLine(new Color(21, 96, 189), arc.getValue().getFromNavPoint().getLocation(), arc.getValue().getToNavPoint().getLocation());
                    }
                    else if (arc.getValue().isForceDoubleJump())
                    {
                        draw.drawLine(new Color(52, 201, 36), arc.getValue().getFromNavPoint().getLocation(), arc.getValue().getToNavPoint().getLocation());
                    }
                    else if (p1.isJumpDest() || p2.isJumpDest() || p1.isJumpSpot() || p2.isJumpSpot() || p1.isJumpPad() || p2.isJumpPad())
                    {
                        draw.drawLine(new Color(9, 82, 40), arc.getValue().getFromNavPoint().getLocation(), arc.getValue().getToNavPoint().getLocation());
                    }
                    else
                    {
                        draw.drawLine(new Color(255, 215, 0), arc.getValue().getFromNavPoint().getLocation(), arc.getValue().getToNavPoint().getLocation());
                    }
                }
            }
        }
    }
    
    
    private boolean isLearning() throws FileNotFoundException, FileNotFoundException, IOException
    {
        File conf = new File("./BotConfig.txt");
        
        if (!conf.exists())
        {
            System.out.println("Pas d'infos à récupérer car le fichier n'existe pas");
            return false;
        }
        FileInputStream readFile = new FileInputStream(conf);
        String ligneTemp = "";
        int temp;
        while ((temp = readFile.read()) != -1)
        {
            if ((char) temp != '\n')
            {
                ligneTemp += (char) temp;
            }
        }
        
        return ligneTemp.equalsIgnoreCase("true");
    }
    
    
    @Override
    public void mapInfoObtained() {
        mapTweaks.register(game.getMapName(), new IMapTweak() {
            @Override
            public void tweak(NavigationGraphBuilder builder) {
                navBuilder.setUsed(true);
                //Suppression des liens premettant au bot de descendre un ascenseur
                List<String> navP1Supp = new ArrayList<String>();
                List<String> navP2Supp = new ArrayList<String>();
                for (Entry<UnrealId,NavPoint> navP1 : navPoints.getNavPoints().entrySet())
                {
                    if (navP1.getValue().isLiftExit())
                    {
                        for (Entry<UnrealId, NavPointNeighbourLink> arc : navP1.getValue().getOutgoingEdges().entrySet()) {
                            NavPoint navP2 = arc.getValue().getToNavPoint();
                            if (navP2.getLocation().getZ() < navP1.getValue().getLocation().getZ() && navP2.isLiftCenter())
                            {
                                navP1Supp.add(navP1.getKey().getStringId());
                                navP2Supp.add(navP2.getId().getStringId());
                                
                                //navBuilder.removeEdge(navP1.getKey().getStringId(), navP2.getId().getStringId());
                            }
                        }
                    }
                }
                for (int i = 0; i < navP1Supp.size(); i++)
                {
                    navBuilder.removeEdge(navP1Supp.get(i), navP2Supp.get(i));
                }
                NavigationMemory memory = new NavigationMemory();
                try {
                    memory.fillNavPointsFromMemory(game.getMapName(), navPoints);
                } catch (IOException ex) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erreur dans l'initialisation des points à supprimer !");
                }
                if (memory.getFromNavPoints() != null)
                {
                    System.out.println("taille 1 : " + memory.getFromNavPoints().size() + " taille 2 : " + memory.getToNavPoints().size());
                    for (int i = 0; i < memory.getFromNavPoints().size(); i++)
                    {
                        System.out.println("Suppression du noeud : " + memory.getFromNavPoints().get(i).getId().getStringId() + " -> " + memory.getToNavPoints().get(i).getId().getStringId());
                        navBuilder.removeEdge(memory.getFromNavPoints().get(i).getId().getStringId(), memory.getToNavPoints().get(i).getId().getStringId());
                    }
                }
            }
        });
        //testAjoutNoeuds();
        fwMap.refreshPathMatrix();
        draw.clearAll();
        testDraw();
        
    	//navMeshModule.setReloadNavMesh(true); // tells NavMesh to reconstruct OffMeshPoints    	
    }
    

    @Override
    public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {
        //body.getConfigureCommands().setBotAppearance("HumanMaleA.EgyptMaleA");
        getInitializeCommand().setDesiredSkill(5);
        navBuilder.setUsed(true);
        
    }
    
    @Override
    public void prepareBot(UT2004Bot bot) {

        autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints
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
        
        try {
            //meshInit = new MeshInit(this, navBot, navMeshModule, levelGeometryModule);
            isLearning = isLearning();
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Problème de lecture du fichier de configuration");
        }
        System.out.println("Le bot apprend ? " + isLearning);
        //meshInit.initSpawn(gameInfo);
        navBot = new BotNavigation(this, /*meshInit*/null);
        try {
            stateRunAround = new RunAroundItem(this, navBot);
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        engage = new Engage(this, navBot);
        medkit = new Medkit(this, navBot, stateRunAround);
        pursue = new Pursue(this, navBot);
        hit = new Hit(this, navBot);
        evasion = new Evasion(this, navBot);
        VisibilityMatrix visibilityMatrix = new VisibilityMatrix(game.getMapName(), 10000);
        comportement = new Comportement(this);
        comportement.setNomComportement(comportement.agressif);
        probaA = new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 5, 0, 0, 0, 500);
        probaA.setMainBot(this);
        try {
            probaA.initProbabilitesA();
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (memory == null)
            memory = new NavigationMemory();
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 5);
        // settings for the rays
        boolean fastTrace = true;
        boolean floorCorrection = false; 
        boolean traceActor = false;

        getAct().act(new RemoveRay("All"));

        raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
        raycasting.createRay(RAYJUMP,  new Vector3d(1, 0, -0.5), rayLength, fastTrace, floorCorrection, traceActor);
        
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {
            @Override
            public void flagChanged(Boolean changedValue) {
                left = raycasting.getRay(LEFT90);
                front = raycasting.getRay(FRONT);
                right = raycasting.getRay(RIGHT90);
                rayjump=raycasting.getRay(RAYJUMP);
            }
        });
        raycasting.endRayInitSequence();
        getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));
        
        
    }
    
    
     //{@link PlayerKilled} listener that provides "frag" counting + is switches the state of the hunter.
    //@param event
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) throws IOException {
        if (event.getKiller().equals(info.getId())) {
            ++frags;
            if (lastEnemy != null) {
                distanceBotTarget = Math.abs(info.getLocation().getDistance2D(lastEnemy.getLocation()));
            }
            probaA.nbVIncrement(lastWeaponUsed);
            probaA.updateProba(lastWeaponUsed);
            if (comportement.getNomComportement().equals("Agressif")) {
                comportement.updateProbaAgressif(killed);
            }
            else {
                comportement.updateProbaAgressif(killed);
            }
        }
        if (enemy == null) {
            return;
        }
        if (enemy.getId().equals(event.getId())) {
            enemy = null;
        }
    }  
    
    public void reset() {
        comportement.setEnemyCount(0);
        comportement.listEnemy.clear();
    	navBot.setItem(null);
        enemy = null;
        //move.stopMovement();
        //navBot.stopNavigation();
        stateRunAround.setItemsToRunAround(null);
        engage.setEngage(false);
        pursue.setIsPursue(false);
        evasion.setIsEvading(false);
        hit.setIsHit(false);
        medkit.setIsHeal(false);        
    }
    
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    	log.info("I have just hurt other bot for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }
    
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    	log.info("I have just been hurt by " + event.getInstigator() + " for: " + event.getDamageType() + "[" + event.getDamage() + "]");
    }
    
    /*@EventListener (eventClass = HearNoise.class)
    protected void hearNoise (HearNoise event) {
        if (senses.getNoiseType().contains("XWeapons.")) {
            move.turnHorizontal(UnrealUtils.degreeToUnrealDegrees(event.getRotation().yaw));
            if (players.canSeeEnemies()) {
                setEnemy(info.getNearestVisiblePlayer());
            }
            else {
                if (comportement.isAgressif()) {
                    navBot.navigate(event.getRotation().toLocation());
                }
            }
        }
    }*/

    @Override
    public void logic() throws PogamutException {
        Alea pourcentChangeArme = new Alea();
        Alea pourcentChangeComportement = new Alea();
        
        if (isLearning)
        {
            secondTimeLearning = System.currentTimeMillis();
            if ((secondTimeLearning - firstTimeLearning) >= 50000)
            {
                firstTimeLearning = secondTimeLearning;
                System.out.println("Ca fait 50 seconde !");
                getAct().act(new Respawn());
                
            }
        }
        
        
        focus = navBot.botFocus();
        
        if (!raycasting.getAllRaysInitialized().getFlag()) {
            return;
        }
        sensorFront = front.isResult();
        sensorLeft90 = left.isResult();
        sensorRight90 = right.isResult();
        if (!weaponry.getCurrentWeapon().getGroup().getName().equals(lastWeaponUsed)) {
            lastWeaponUsed = weaponry.getCurrentWeapon().getGroup().getName();
        }
        if (pourcentChangeArme.pourcentDeChance(30) && weaponry.getWeapons().size() > 2 && (!info.isShooting() || !info.isSecondaryShooting())) {
            weaponry.changeWeapon(probaA.choixArme(weaponry));
        }
        if (players.canSeeEnemies())
        {
            enemy = players.getNearestVisibleEnemy();
            lastEnemy = enemy;
        }
        comportement.changeComportement(); 
        
        //
        if (souldEscape && enemy != null && info.isFacing(enemy, 70) && info.getHealth() < 75)
        {
           // engage.set
            engage.setEngage(false);
            pursue.setIsPursue(false);
            evasion.setIsEvading(true);
            hit.setIsHit(false);
            medkit.setIsHeal(false);  
            try {
                evasion.itemEvasion();
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        
        // 1) do you see enemy? 	-> go to PURSUE (start shooting / hunt the enemy)
        if (shouldEngage && enemy != null && info.isFacing(enemy, 70) && weaponry.hasLoadedWeapon()) {
            engage.setEngage(true);
            pursue.setIsPursue(false);
            evasion.setIsEvading(false);
            hit.setIsHit(false);
            medkit.setIsHeal(false);  
            try {
                enemy = engage.stateEngage();
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        
        // 2) are you shooting? 	-> stop shooting, you've lost your target
        if (info.isShooting() || info.isSecondaryShooting()) {
            getAct().act(new StopShooting());
        }
        
        // 3) are you being shot? 	-> go to HIT (turn around - try to find your enemy)
        if (senses.isBeingDamaged()) {
            engage.setEngage(false);
            pursue.setIsPursue(false);
            evasion.setIsEvading(false);
            hit.setIsHit(true);
            medkit.setIsHeal(false);  
            hit.stateHit();
            return;
        }
        
        //System.out.println("==========enemy " + enemy + " ===================");
        // 4) have you got enemy to pursue? -> go to the last position of enemy
        if (enemy != null && shouldPursue && weaponry.hasLoadedWeapon()) {  // !enemy.isVisible() because of 2)
            engage.setEngage(false);
            pursue.setIsPursue(true);
            evasion.setIsEvading(false);
            hit.setIsHit(false);
            medkit.setIsHeal(false);  
            try {
                pursue.statePursue();
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        engage.setEngage(false);
        pursue.setIsPursue(false);
        evasion.setIsEvading(false);
        hit.setIsHit(false);
        medkit.setIsHeal(false);   
       
        try {
            // 6) if nothing ... run around items
            stateRunAround.stateRunAroundItems();
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void botKilled(BotKilled event) {
        NavigationMemory memory = new NavigationMemory();
        try {
            memory.fillNavPointsFromMemory(game.getMapName(), navPoints);
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erreur dans l'initialisation des points à supprimer !");
        }
        if (memory.getFromNavPoints() != null)
        {
            System.out.println("taille 1 : " + memory.getFromNavPoints().size() + " taille 2 : " + memory.getToNavPoints().size());
            for (int i = 0; i < memory.getFromNavPoints().size(); i++)
            {
                System.out.println("Suppression du noeud : " + memory.getFromNavPoints().get(i).getId().getStringId() + " -> " + memory.getToNavPoints().get(i).getId().getStringId());
                navBuilder.removeEdge(memory.getFromNavPoints().get(i).getId().getStringId(), memory.getToNavPoints().get(i).getId().getStringId());
            }
        }
        //testAjoutNoeuds();
        fwMap.refreshPathMatrix();
        draw.clearAll();
        testDraw();
        
        if (lastEnemy != null) {
            distanceBotTarget = Math.abs(info.getLocation().getDistance2D(lastEnemy.getLocation()));
        }
        try {
            probaA.nbDIncrement(lastWeaponUsed);
            probaA.updateProba(lastWeaponUsed);
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        probaA.inventaireArmes.clear();
        probaA.setSize(0);
        if (comportement.getNomComportement().equals("Agressif")) {
            comportement.updateProbaAgressif(!killed);
        }
        else {
            comportement.updateProbaAgressif(!killed);
        }
        comportement.setEnemyCount(0);     
    	reset();
        
        
        

        
    }
    
    @ObjectClassEventListener(objectClass = Self.class, eventClass = WorldObjectUpdatedEvent.class)
    public void selfUpdated(WorldObjectUpdatedEvent<Self> event) throws IOException {
        if (engage != null && pursue != null && evasion != null && hit != null)
        {
            if((navBot.isNavigating() || navBot.isNavigatingToItem()) && navBot!=null && !engage.isEngage() && !pursue.isPursue() && !evasion.isEvading() && !hit.isHit()){
                navBot.navigate();
            }
        }
    }

    public boolean isRunningToPlayer() {
        return runningToPlayer;
    }

    public boolean isNavigate() {
        return navigate;
    }

    //GETTER
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
    
    public Player getLastEnemy() {
        return lastEnemy;
    }
    
    public double getDistanceBotTarget() {
        return distanceBotTarget;
    }
    
    public boolean getLeft90() {
        return left.isResult();
    }
    
    public boolean getRight90() {
        return right.isResult();
    }
    
    public int getPursueCount() {
        return pursueCount;
    }

    @Override
    public IUT2004Navigation getNavigation() {
        return navigation;
    }
    
    @Override
    public AdvancedLocomotion getMove() {
        return move;
    }

    @Override
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
    
    @Override
    public Visibility getVisibility()
    {
        return visibility;
    }
    
    //SETTERS
    public void setPursueCount(int pursueCount) {
        this.pursueCount = pursueCount;
    }
    
    public void setNavigate(boolean navigate) {
        this.navigate = navigate;
    }

    public void setEnemy(Player enemy) {
        this.enemy = enemy;
    }

    public void setPursue(boolean etat) {
        this.shouldPursue = etat;
    }
    
    public void setEngage(boolean etat) {
        this.shouldEngage = etat;
    }
    
    public void setMedkit(boolean etat) {
        this.shouldCollectHealth = etat;
    }  
    
    public void setEvasion(boolean etat) {
        this.souldEscape = etat;
    }

    public ILocated getFocus() {
        return focus;
    }
    
    public boolean getLearning()
    {
        return isLearning;
    }
    
    public void setFirstTimeLearning(double time)
    {
        firstTimeLearning = time;
    }
    
    public void setSecondTimeLearning(double time)
    {
        secondTimeLearning = time;
    }

    public double getFirstTimeLearning() {
        return firstTimeLearning;
    }

    public double getSecondTimeLearning() {
        return secondTimeLearning;
    }
    
    
    
}