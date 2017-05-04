package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JB
 */
public class Medkit {
    
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
    
    private RunAroundItem runAround;

    public Medkit(Bot mainBot, BotNavigation navBot, RunAroundItem runAround)
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
        
        this.runAround = runAround;
    }
    

    protected void stateMedKit() {
        //log.info("Decision is: MEDKIT");
        Item item = items.getPathNearestSpawnedItem(ItemType.Category.HEALTH);
        if (item == null) {
        	log.warning("NO HEALTH ITEM TO RUN TO => ITEMS");
                runAround.stateRunAroundItems();
        } else {
        	bot.getBotName().setInfo("MEDKIT");
                navBot.navigate(item);
        	navBot.setItem(item);
        }
    }
    
    protected void stateRepli() {
        List<Item> listeHealth = new ArrayList<Item>();
        Item it = null;
        for (Iterator i = items.getAllItems(ItemType.Category.HEALTH).entrySet().iterator(); i.hasNext();) {
            Map.Entry couple = (Map.Entry)i.next();
            it = (Item) couple.getValue();
            listeHealth.add(it);
        }
        //weaponry.changeWeapon(UT2004ItemType.SHIELD_GUN);
    }
    
    protected void stateFuite() {
        Item hp = null;
        Item armor = null;
        double distanceH;
        double distanceA;
        bot.getBotName().setInfo("FUITE");
        hp = items.getNearestSpawnedItem(ItemType.Category.HEALTH);
        armor = items.getNearestSpawnedItem(ItemType.Category.ARMOR);
        if (armor == null) {
            navBot.navigate(hp);
            navBot.setItem(hp);
        }
        else {
            distanceH = info.getLocation().getDistance(hp.getLocation());
            distanceA = info.getLocation().getDistance(armor.getLocation());
            if (distanceA < distanceH && info.getArmor() <= 150 && hp != null) {
                if (info.getArmor() >= 50 && armor.getAmount() == 50) {
                    armor = items.getNearestSpawnedItem(UT2004ItemType.SUPER_SHIELD_PACK);
                }
                navBot.navigate(armor);
                navBot.setItem(armor);
            }
            else {
                navBot.navigate(hp);
                navBot.setItem(hp);
            }
        }
        mainBot.setEngage(true);
        mainBot.setPursue(true);
    }
    
}
