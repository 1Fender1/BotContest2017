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
       
        protected void statePursue() {
            //log.info("Decision is: PURSUE");
            bot.getBotName().setInfo("PURSUE");
            mainBot.setPursueCount(mainBot.getPursueCount() + 1);
            if (mainBot.getPursueCount() > 30) {
                mainBot.reset();
            }
            if (enemy != null) {
                if (bot.getLocation().getDistance(enemy.getLocation()) >= 600) {
                    navBot.navigate(enemy);
                    navBot.setItem(null);
                }
                else {
                    mainBot.getEngage().stateEngage();
                }
            } else {
                navBot.navigate(mainBot.getNavigation().getLastTargetPlayer());
            }
    } 
}
