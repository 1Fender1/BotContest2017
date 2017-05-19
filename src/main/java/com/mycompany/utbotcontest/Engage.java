package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author JB
 */
public class Engage {
    
    private Bot mainBot;
    
    private UT2004Bot bot;
    
    private AgentInfo info;
    
    private NavPoints navPoints;
    
    private NavMeshNavigation nmNav;
    
    private BotNavigation navBot;
    
    private Weaponry weaponry;
    
    private WeaponPrefs weaponPrefs;
    
    private Items items;
    
    private Game game;
    
    private LogCategory log;
    
    private Player enemy;
    
    private boolean runningToPlayer;
    
    private double longueurStrafe = UnrealUtils.CHARACTER_COLLISION_RADIUS * 10;
    
    private boolean isEngage = false;
    
    public Engage(Bot mainBot, BotNavigation navBot)
    {
        this.mainBot = mainBot;
        
        this.bot = mainBot.getBot();
        
        this.info = mainBot.getInfo();
        
        this.navPoints = mainBot.getNavPoints();
        
        this.nmNav = mainBot.getNmNav();
        
        this.weaponPrefs = mainBot.getWeaponPrefs();
        
        this.weaponry = mainBot.getWeaponry();
        
        this.navBot = navBot;
        
        this.items = mainBot.getItems();
        
        this.game = mainBot.getGame();
        
        this.log = mainBot.getLog();
        
        this.enemy = mainBot.getEnemy();
        
        this.runningToPlayer = mainBot.isRunningToPlayer();
    }
    
    
    public Player stateEngage() throws IOException {
        //log.info("Decision is: ENGAGE");
        //config.setName("Hunter [ENGAGE]");
        bot.getBotName().setInfo("ENGAGE");
        
        Random random = new Random();
        
        boolean shooting = false;
        double distance = Double.MAX_VALUE;
        mainBot.setPursueCount(0);
        
        // 1) pick new enemy if the old one has been lost
        if (enemy == null || !enemy.isVisible()) {
            // pick new enemy
            enemy = mainBot.getPlayers().getNearestVisiblePlayer(mainBot.getPlayers().getVisibleEnemies().values());
            mainBot.setEnemy(enemy);
            if (enemy == null) {
                log.info("Can't see any enemies... ???");
                return enemy;
            }
        }

        // 2) stop shooting if enemy is not visible
        if (!enemy.isVisible()) {
            if (info.isShooting() || info.isSecondaryShooting()) {
                mainBot.getAct().act(new StopShooting());
            }
            runningToPlayer = false;
        } else {
            /*navBot.setNavigating(false);
            navBot.setNavigatingToItem(false);*/
            // 2) or shoot on enemy if it is visible
            distance = info.getLocation().getDistance(enemy.getLocation());
            mainBot.getShoot().shoot(enemy);
            log.info("Shooting at enemy!!!");
            shooting = true;
            int movement = random.nextInt(3);
            //0 -> strafeLeft
            //1 -> strafeRight
            //2 -> jump
            switch(movement){
                case 0: if(mainBot.getLeft90()) 
                            mainBot.getMove().strafeRight(longueurStrafe,enemy.getLocation());
                        else
                            mainBot.getMove().strafeLeft(longueurStrafe,enemy.getLocation());
                        break;
                case 1: if(mainBot.getRight90())
                            mainBot.getMove().strafeLeft(longueurStrafe,enemy.getLocation());
                        else
                            mainBot.getMove().strafeRight(longueurStrafe,enemy.getLocation());
                        break;
                case 2: int randX = (random.nextInt()%250) + 25;
                        int randY = (random.nextInt()%250) + 25;
                        boolean signeX = random.nextBoolean();
                        boolean signeY = random.nextBoolean();
                        Location loc = info.getLocation();
                        if(signeX)
                            loc.setX((double)randX);
                        else
                            loc.setX((double)-randX);
                        if(signeY)
                            loc.setY((double)randY);
                        else
                            loc.setY((double)-randY);
                        mainBot.getMove().moveTo(loc);
                        mainBot.getMove().jump();
                        break;
                default: break;
            }
        }

        // 3) if enemy is far or not visible - run to him
        int decentDistance = Math.round(random.nextFloat() * 800) + 200;
        if (!enemy.isVisible() || !shooting || decentDistance < distance) {
            if (!runningToPlayer) {
                if (distance > 1500) {
                    navBot.navigate(enemy);
                    runningToPlayer = true;
                }                
            }
            navBot.setNavigating(false);
            navBot.setNavigatingToItem(false);
        } else {
            runningToPlayer = false;
            navBot.setNavigating(false);
            navBot.setNavigatingToItem(false);
            navBot.setNavigating(false);
            navBot.setNavigatingToItem(false);
        }
        
        navBot.setItem(null);
        return enemy;
    }

    public boolean isEngage() {
        return isEngage;
    }
    
    public void setEngage(boolean engage)
    {
        isEngage = engage;
    }
    
    
}
