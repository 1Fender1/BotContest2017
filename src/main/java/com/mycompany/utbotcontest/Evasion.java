package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.hideandseek.bot.UT2004BotHSController;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


/**
 *
 * @author JB
 */
public class Evasion extends UT2004BotHSController<UT2004Bot> {
    
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
    
    private boolean runningToPlayer;
    
    private boolean strafeL = false;
    
    private List<NavPoint> allNavPoints = null;
    
    private boolean isEvading = false;
    
    public Evasion(Bot mainBot, BotNavigation navBot)
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
        
        this.runningToPlayer = mainBot.isRunningToPlayer();
        
        this.allNavPoints = getAllNavPoints();
    }
    
    
    private List<Player> getEnnemies()
    {
        List<Player> enemies = new ArrayList<Player>();
        
        for (Entry<UnrealId, Player> enemy : mainBot.getPlayers().getVisibleEnemies().entrySet())
        {
            enemies.add(enemy.getValue());
        }
        
        return enemies;
    }
    
    //retrun all NavPoint of the map
    private List<NavPoint> getAllNavPoints()
    {
        Map<UnrealId, NavPoint> navPointTemp = mainBot.getNavPoints().getNavPoints();
        List<NavPoint> navPoint = new ArrayList<NavPoint>();
        for (Entry<UnrealId, NavPoint> element : navPointTemp.entrySet())
        {
            navPoint.add(element.getValue());
        }
        return navPoint;
    }
    
    
    private List<NavPoint> getVisibleNavPoints()
    {
        List<NavPoint> visibleNavPoints = new ArrayList<NavPoint>();
        
        for (Entry<UnrealId, NavPoint> element : mainBot.getNavPoints().getVisibleNavPoints().entrySet())
        {
            visibleNavPoints.add(element.getValue());
        }
        return visibleNavPoints;
    }
    
    //return higher location
    private Location getHigher(Location loc1, Location loc2)
    {
        if (loc1.getZ() < loc2.getZ())
            return loc2;
        else
            return loc1;        
    }
    
    //return hidden navPoints from all enemies
    private List<NavPoint> getHiddenNavPoints()
    {
        boolean isHidden = true;
        List<NavPoint> hiddenNavPoints = new ArrayList<NavPoint>();
        for (NavPoint navP : this.allNavPoints)
        {
            for (Player enemy : getEnnemies())
            {
                Location loc1 = navP.getLocation();
                Location loc2 = enemy.getLocation();
                Double higherZ = getHigher(loc1, loc2).getZ();
                loc1 = loc1.setZ(higherZ);
                loc2 = loc2.setZ(higherZ);
                if (mainBot.getVisibility().isVisible(loc1, loc2))
                {
                    isHidden = false;
                }
            }
            if (isHidden)
            {
                hiddenNavPoints.add(navP);
            }
            isHidden = true;
        }
        
        return hiddenNavPoints;
    }
    
    
    private List<Item> getEvasionItems()
    {
        List<Item> evasionItems = new ArrayList<Item>();
        List<Item> listTemp = new ArrayList<Item>();
        
        for (Entry<UnrealId, Item> element : mainBot.getItems().getAllItems().entrySet())
        {
            if (element.getValue().getDescriptor().getItemCategory().toString().equals("HEALTH") || element.getValue().getDescriptor().getItemCategory().toString().equals("HEALTH"))
                listTemp.add(element.getValue());
        }
       
        List<NavPoint> hiddenNavPoint = getHiddenNavPoints();
        
        for (Item item : listTemp)
        {
            if (hiddenNavPoint.contains(item.getNavPoint()))
            {
                evasionItems.add(item);
            }
        }
        
        return evasionItems;
    }
    
    private Item getRandomEvasionItem()
    {
        return MyCollections.getRandom(getEvasionItems());
    }
    
    private Item getNearestEvasionItem()
    {
        double distance = Double.MAX_VALUE;
        Item itemRes = null;
        
        for (Item item : getEvasionItems())
        {
            double newDistance = item.getLocation().getDistance(info.getLocation());
            if (distance > newDistance)
            {
                distance = newDistance;
                itemRes = item;
            }
        }
        
        return itemRes;
    }
    
    
    private NavPoint getBestEvasionItem()
    {
        
        if (mainBot.getEnemy() == null)
        {
            return getRandomEvasionItem().getNavPoint();
        }
        
        for (Item item : getEvasionItems())
        {
            double newDistance = item.getLocation().getDistance(info.getLocation());
            
            if (!isPathNearEnemy(mainBot.getEnemy(), item.getNavPoint()))
            {
                return item.getNavPoint();
            }
        }
        return getRandNavPointToHide();

    }
    
    
    //return if the path compute from the bot to the destination is bringing closer enemy
    private boolean isPathNearEnemy(Player enemy, NavPoint destination)
    {
        List<ILocated> path = null;
        
        double distanceBotEnemy = enemy.getLocation().getDistance(info.getLocation());
        path = mainBot.getNavigation().getPathPlanner().computePath(mainBot.getNavPoints().getNearestNavPoint(enemy.getLocation()), destination).get();
        
        for (ILocated elem : path)
        {
            if (distanceBotEnemy > enemy.getLocation().getDistance(elem.getLocation()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isHide()
    {      
        for (Entry<UnrealId, Player> enemy : mainBot.getPlayers().getEnemies().entrySet())
        {
            if (mainBot.getVisibility().isVisible(enemy.getValue().getLocation()))
            {
                return false;
            }
        }
        return true;
    }
    
    private NavPoint getRandNavPointToHide()
    {
        return MyCollections.getRandom(getHiddenNavPoints());
    }
    
    public void randomEvasion()
    {
        if (mainBot.getPlayers().canSeeEnemies())
        {
            mainBot.getNavigation().setFocus(mainBot.getEnemy());
            if (!mainBot.getNavigation().isNavigatingToNavPoint())
            {
                navBot.navigate(getRandNavPointToHide());
            }
        }
    }
    
    public void itemEvasion()
    {
        bot.getBotName().setInfo("Evasion");
        if (mainBot.getPlayers().canSeeEnemies())
        {
            isEvading = true;
            Alea rand = new Alea();
            mainBot.getNavigation().setFocus(mainBot.getEnemy());
            if (!mainBot.getNavigation().isNavigatingToNavPoint())
            {
                NavPoint item = getBestEvasionItem();
                navBot.navigate(item);
            }
            mainBot.getShoot().shoot(mainBot.getPlayers().getNearestVisibleEnemy());
            if (rand.pourcentDeChance(25))
            {
                mainBot.getMove().jump();
            }
        }
        else if (mainBot.getNavigation().isNavigating())
        {
            navBot.botFocus();
        }
        if (isHide())
        {
            isEvading = false;
        }
    }
    
    public boolean isEvading()
    {
        return isEvading;
    }
    
}
