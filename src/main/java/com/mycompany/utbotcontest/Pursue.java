package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.io.IOException;
import java.util.HashSet;

/**
 *
 * @author JB
 */
public class Pursue {
    
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
    
    private boolean isPursue = false;

    public Pursue(Bot mainBot, BotNavigation navBot)
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
       
    protected void statePursue() throws IOException {
        //log.info("Decision is: PURSUE");
        bot.getBotName().setInfo("PURSUE");
        enemy = mainBot.getEnemy();
        mainBot.setPursueCount(mainBot.getPursueCount() + 1);
        if (mainBot.getPursueCount() > 30) {
            mainBot.reset();
        }
        if (enemy != null) {
        	bot.getBotName().setInfo("PURSUE");
                mainBot.getNavPoints().getNearestNavPoint(enemy.getLocation());
        	navBot.navigate(enemy.getLocation());
                mainBot.getNavigation().setContinueTo(enemy.getLocation());
                mainBot.getNavigation().setFocus(enemy.getLocation());
        	navBot.setItem(null);
        } else {
            navBot.setItem(null);
            enemy = null;
            mainBot.getMove().stopMovement();
        }
    } 

    public boolean isPursue() {
        return isPursue;
    }

    public void setIsPursue(boolean isPursue) {
        this.isPursue = isPursue;
    }
    
    
}
