package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Rotate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.Random;

/**
 *
 * @author JB
 */
public class Hit {
    
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
    
    private AdvancedLocomotion move;
    
    private boolean turn;
    
    private boolean isHit = false;
    

    public Hit(Bot mainBot, BotNavigation navBot)
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
        
        this.move = mainBot.getMove();
        
        this.turn = true;
    }
        
        protected void stateHit() {
            //log.info("Decision is: HIT");
            //bot.getBotName().setInfo("HIT");
            navBot.setNavigating(false);
            navBot.setNavigatingToItem(false);
            mainBot.getNavigation().stopNavigation();
            mainBot.getAct().act(new Rotate().setAmount(32000));
            if (mainBot.getPlayers().canSeeEnemies()) {
                this.enemy = mainBot.getPlayers().getNearestVisibleEnemy();
                mainBot.setEnemy(enemy);
                move.turnTo(enemy);
            }
            else {
                return; 
            }
        }

    public boolean isHit() {
        return isHit;
    }
        
    public void setIsHit(boolean hit)
    {
        isHit = hit;
    }
}
