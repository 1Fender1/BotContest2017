package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.drawing.NavMeshDraw;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import java.io.File;

/**
 *
 * @author JB
 */
public class MeshInit {
    
    private boolean speak = false;
    private final boolean drawNavMesh = true;
    private final boolean drawOffMeshLinks = false; // false
    private int errorMsg = 0;
    private boolean navMeshDrawn = false; 
    private double waitForMesh = 0;
    private double waitingForMesh = 0;
    private boolean offMeshLinksDrawn = false;
    private double waitForOffMeshLinks = 0;
    private double waitingForOffMeshLinks = 0;
    private int sayState = -2;
    private double sayNext = 0;
    private Bot mainBot;
    private BotNavigation navBot;
    private NavMeshModule navMeshModule;
    private NavMeshDraw navMeshDraw;
    private LevelGeometryModule levelGeometryModule;
    
    
    public MeshInit(Bot mainBot, BotNavigation navBot, NavMeshModule navMeshModule, LevelGeometryModule levelGeometryModule)
    {
        this.mainBot = mainBot;
        this.navBot = navBot;
        this.navMeshModule = navMeshModule;
        this.levelGeometryModule = levelGeometryModule;
    }
    
    
    
    public boolean speak() {
        sayNext -= mainBot.getInfo().getTimeDelta();
        if (sayNext > 0) return true;

        // SAY PHRASE AND SETUP NEXT STATE

        sayNext = 8;
        switch(sayState) {
        case -2: say("Done, now sit and watch how I can navigate around!"); sayNext = 3; break;
        case -1: mainBot.setNavigate(true); break;
        case 0: say("Notice how I'm NOT using navigation graph that comes with UT2004."); break;
        case 1: say("Yeah, that's right, I DO NOT NEED IT!"); break;
        case 2: say("Whooo that feels great!"); break;
        case 3: say("You know, the freedom to move around like a human."); sayNext = 14; break;
        case 4: say("..."); sayNext = 1.5; break;
        case 5: if (navMeshModule.getNavMesh().getOffMeshPoints().isEmpty()) {
                        sayState = 17;
                        return true;
                }
                say("Well, but I'm not honest with you."); sayNext = 4; break;
        case 6: say("I do need the navigation graph from time to time."); sayNext = 7; break;
        case 7:
                say("Let me show you.");
                mainBot.getNmNav().stopNavigation();
                sayNext = 1;
                mainBot.setNavigate(false);
                
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
                mainBot.getNmNav().stopNavigation();
                navBot.setRaycasting(true);
                break;
        case 19:
                speak = false;
                break;
        }
        ++sayState;
        return true;
    }
	
    public boolean drawNavMesh() { 
        if (!navMeshDrawn) {
            navMeshDrawn = true;
            say("Drawing NavMesh...");
            navMeshModule.getNavMeshDraw().clearAll();
            navMeshModule.getNavMeshDraw().draw(true, true);
            say("Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");

            waitForMesh = navMeshModule.getNavMesh().getPolys().size() / 40 - 2.5;
            waitingForMesh = -mainBot.getInfo().getTimeDelta();
        }

        if (waitForMesh > 0) {
            waitForMesh -= mainBot.getInfo().getTimeDelta();
            waitingForMesh += mainBot.getInfo().getTimeDelta();
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
	
    public boolean drawOffMeshLinks() { 		
        if (!offMeshLinksDrawn) {
            offMeshLinksDrawn = true;

            if (navMeshModule.getNavMesh().getOffMeshPoints().isEmpty()) {
                    say("Ha! There are no off-mesh points / links within this map!");
                    return true;
            }

            say("Drawing OffMesh Links...");
            navMeshModule.getNavMeshDraw().draw(false, true);
            say("Okey, drawing commands issued, now we have to wait a bit till it gets drawn completely...");    		
            waitForOffMeshLinks = navMeshModule.getNavMesh().getOffMeshPoints().size() / 10;
            waitingForOffMeshLinks = -mainBot.getInfo().getTimeDelta();
        }

        if (waitForOffMeshLinks > 0) {
            waitForOffMeshLinks -= mainBot.getInfo().getTimeDelta();
            waitingForOffMeshLinks += mainBot.getInfo().getTimeDelta();
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

    void say(String text) {
        if (speak) { 
            mainBot.getBody().getCommunication().sendGlobalTextMessage(text);
        }
        mainBot.getLog().info("SAY: " + text);
    }
    
    
    public void initSpawn(GameInfo gameInfo)
    {
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
    }

    public int getErrorMsg() {
        return errorMsg;
    }
    
    public void setErrorMsg(int msg) {
        errorMsg = msg;
    }

    public boolean isSpeak() {
        return speak;
    }

    public boolean isDrawNavMesh() {
        return drawNavMesh;
    }

    public boolean isDrawOffMeshLinks() {
        return drawOffMeshLinks;
    }

    public boolean isNavMeshDrawn() {
        return navMeshDrawn;
    }

    public double getWaitForMesh() {
        return waitForMesh;
    }

    public double getWaitingForMesh() {
        return waitingForMesh;
    }

    public boolean isOffMeshLinksDrawn() {
        return offMeshLinksDrawn;
    }

    public double getWaitForOffMeshLinks() {
        return waitForOffMeshLinks;
    }

    public double getWaitingForOffMeshLinks() {
        return waitingForOffMeshLinks;
    }

    public int getSayState() {
        return sayState;
    }

    public double getSayNext() {
        return sayNext;
    }

    public Bot getMainBot() {
        return mainBot;
    }

    public BotNavigation getNavBot() {
        return navBot;
    }

    public NavMeshModule getNavMeshModule() {
        return navMeshModule;
    }

    public NavMeshDraw getNavMeshDraw() {
        return navMeshDraw;
    }

    public LevelGeometryModule getLevelGeometryModule() {
        return levelGeometryModule;
    }
    
    
}
