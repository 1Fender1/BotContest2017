package com.mycompany.utbotcontest;

import static com.mycompany.utbotcontest.ProbabilitesArmes.referencesArmes;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JB
 */
public class RunAroundItem {
    
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
    
    private ProbabilitesArmes probaA;
    
    private final double radius = 500;
    
    public RunAroundItem(Bot mainBot, BotNavigation navBot)
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
        
        this.probaA = mainBot.getProbaArmes();
    }
    
    
    
    
     //Listes qui vont contenir les différents items selon leur categorie.
      protected List<Item> listWeapon = new ArrayList<Item>();
      protected List<Item> listArmor = new ArrayList<Item>();
      protected List<Item> listHealth = new ArrayList<Item>();
      protected List<Item> listAmmo = new ArrayList<Item>();
      protected Item damage = null;
      protected double min;
      
      
      //Ajout des items dans leur liste respective
      protected void addItemsInList(List<Item> listI){
        int a = 0;
        int w = 0;
        int h = 0;
        int am = 0;
        int ad = 0;
        int d = 0;
        
        for(Item elem : listI){
            for(ItemType itemType : ItemType.Category.WEAPON.getTypes()){
                if(elem.getType() == itemType){
                   listWeapon.add(elem);
                   w++;
                }
            }
            for(ItemType itemType : ItemType.Category.HEALTH.getTypes()){
                if(elem.getType() == itemType){
                   listHealth.add(elem);
                   h++;
                }
            }
            for(ItemType itemType : ItemType.Category.ARMOR.getTypes()){
                if(elem.getType() == itemType){
                   listArmor.add(elem);
                   a++;
                }
            }
            for(ItemType itemType : ItemType.Category.AMMO.getTypes()){
                if(elem.getType() == itemType){
                   listAmmo.add(elem);
                   am++;
                }
            }
            if (elem.getType() == UT2004ItemType.U_DAMAGE_PACK){
              damage = elem;
              d++;
            }
        }
        System.out.println("Armes:   "+w);
        System.out.println("Vie:   "+h);
        System.out.println("armure:   "+a);
        System.out.println("munition:   "+am);
        System.out.println("adrenaline:   "+ad);
        System.out.println("damage:   "+d);
      }
      
      //Si on a besoin de vie, alors on sélectionne l'item le plus proche dans la liste vie
      protected Item selectItemInListHealth(List<Item> listH){
          Item itHealth = null;
          if(info.getHealth() < 120){
                min = info.getLocation().getDistance(listH.get(0).getLocation());
                for(Item vie : listH){
                    if(info.getHealth() < 100 && info.getLocation().getDistance(vie.getLocation()) <= min){
                        itHealth = vie;
                        min = info.getLocation().getDistance(vie.getLocation());
                    }
                    if(info.getHealth() >= 100 && vie.getType() == UT2004ItemType.MINI_HEALTH_PACK && info.getLocation().getDistance(vie.getLocation()) <= min){
                        itHealth = vie;
                        min = info.getLocation().getDistance(vie.getLocation());
                    }
                }
            }
            return itHealth;
      }
      
      //On sélectionne l'item correspondant à l'arme préférée du bot
      protected Item selectItemInListWeapon(List<Item> listW){
          Item itWeapon = null;
          //On va chercher l'arme ayant la proba la plus élevée
          min = referencesArmes.get(0).getProbabilite();
          for(ProbabilitesArmes pa : referencesArmes)
            for(Item weap : listW){
              if(weap.getType().getName().equals(pa.getNom().getName()) && pa.getProbabilite()>=min){
                  itWeapon = weap;
                  min = pa.getProbabilite();
              }
          }
          //On va chercher l'arme préferée du bot (config de départ)
          /*if(!weaponry.hasWeapon(weaponPrefs.getGeneralPrefs().getPrefs().get(0).getWeapon())){
            for(Item weap : listW){
                if(weap.getType() == weaponPrefs.getGeneralPrefs().getPrefs().get(0).getWeapon() ){
                    itWeapon = weap;
                }
            }
          }*/
          return itWeapon;
      }
      
      //Si on a besoin d'armure, alors on sélectionne l'item correspondant à l'armure la plus proche
      protected Item selectItemInListArmor(List<Item> listA){
          Item itArmor = null;
          if(info.getArmor() < 100){
            min = info.getLocation().getDistance(listA.get(0).getLocation());
            for(Item armor : listA){
                if(info.getArmor() < 50 && info.getLocation().getDistance(armor.getLocation()) <= min){
                    itArmor = armor;
                    min = info.getLocation().getDistance(armor.getLocation());
                }else{
                    if(armor.getType() == UT2004ItemType.SUPER_SHIELD_PACK && info.getLocation().getDistance(armor.getLocation()) <= min){
                        itArmor = armor;
                        min = info.getLocation().getDistance(armor.getLocation());
                    }
                }
            }
        }
          return itArmor;
      }
      
      //On sélectionne l'item correspondant aux munitions que le bot a besoin
      protected Item selectItemInListAmmo(List<Item> listAm){
          Item itAmmo = null;
          min = info.getLocation().getDistance(listAmmo.get(0).getLocation());
          for(Item ammo : listAm){
              if(weaponry.hasLoadedWeapon(weaponry.getWeaponForAmmo(ammo.getType())) && weaponry.getAmmo(ammo.getType()) < weaponry.getMaxAmmo(ammo.getType())
                      && info.getLocation().getDistance(ammo.getLocation()) <= min){
                    itAmmo = ammo;
                    min = info.getLocation().getDistance(ammo.getLocation());
              }
          }
          return itAmmo;
      }
      
      protected Item selectItemInListAdrenaline(List<Item> listAd){
          Item itAdrenaline = null;
              min = info.getLocation().getDistance(listAd.get(0).getLocation());
              for(Item ad: listAd){
                  if(info.getLocation().getDistance(ad.getLocation()) <= min)
                    itAdrenaline = ad;
              }
          return itAdrenaline;
      }
      
    protected Item getNextItem (List<Item> listH,List<Item> listW,List<Item> listA,List<Item> listAm){
       Item item = null;
        if(weaponry.getWeapons().size() == 2 && !listW.isEmpty())
            return selectItemInListWeapon(listW);
        else if(info.getHealth() <= 100 && !listH.isEmpty())
            return selectItemInListHealth(listH);
        else if(info.getArmor() <= 50 && !listA.isEmpty())
            return selectItemInListArmor(listA);
        else if(!listAm.isEmpty())
            return selectItemInListAmmo(listAm);
        else if(damage != null && navBot.reachable(damage))
            return damage;
        else if(!listW.isEmpty()){
            min = info.getLocation().getDistance(listW.get(0).getLocation());
            for(Item it : listW){
                if(info.getLocation().getDistance(it.getLocation()) <= min){
                    item = it;
                    min = info.getLocation().getDistance(it.getLocation());
                }
            }
            return item;
        }
        else return null;
    }
    
    
    private Item existNearInterstingItem(List<Item> intersting)
    {
        List<Item> listItem = new ArrayList<Item>();
        
        for (Item item : intersting)
        {
            if (item.getLocation().getDistance(info.getLocation()) <= radius)
            {
                listItem.add(item);
            }
        }
        addItemsInList(listItem);
        Item itemProche = getNextItem (listHealth,listWeapon,listArmor,listAmmo);
        listHealth.clear(); listWeapon.clear(); listArmor.clear(); listAmmo.clear();
        if (itemProche != null)
        {
            System.out.println("Item proche : " + itemProche.toString());
        }
        return itemProche;        
    }
    

    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
protected List<Item> itemsToRunAround = null;

    protected void stateRunAroundItems() {
        //log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        bot.getBotName().setInfo("RUN AROUND ITEM");

        if (nmNav.isNavigatingToItem()) return;
        
        List<Item> interesting = new ArrayList<Item>();
        
        // ADD WEAPONS
        for (ItemType itemType : ItemType.Category.WEAPON.getTypes()) {
        	if (!weaponry.hasLoadedWeapon(itemType)) 
                    interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        
        // ADD ARMORS
        for (ItemType itemType : ItemType.Category.ARMOR.getTypes()) {
            if (info.getArmor() < game.getMaxLowArmor())
            {
                interesting.addAll(items.getSpawnedItems(itemType).values());
            }
            else if (info.getArmor() < game.getMaxArmor())
            {
                interesting.addAll(items.getSpawnedItems(UT2004ItemType.SUPER_SHIELD_PACK).values());
            }
        }
        // ADD QUADS
        interesting.addAll(items.getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
        
        // ADD HEALTHS
        for (ItemType itemType : ItemType.Category.HEALTH.getTypes())
        {
            if (info.getHealth() < 100) {
        	interesting.addAll(items.getSpawnedItems(itemType).values());
            }
            else if (info.getHealth() >= 100 && info.getHealth() < game.getMaxHealth())
            {
                interesting.addAll(items.getSpawnedItems(UT2004ItemType.MINI_HEALTH_PACK).values());
            }
        }
        
        //ADD AMMO
        for (ItemType itemType : ItemType.Category.AMMO.getTypes())
        {
            if (weaponry.hasLoadedWeapon(weaponry.getWeaponForAmmo(itemType)) && (weaponry.getAmmo(itemType) < weaponry.getMaxAmmo(itemType)))
                interesting.addAll(items.getSpawnedItems(itemType).values());
        }
        
       // Item item = MyCollections.getRandom(tabooItems.filter(interesting));
       Item item;
       item = existNearInterstingItem(interesting);
       if (item == null)
       {
            addItemsInList(interesting);
            item = getNextItem (listHealth,listWeapon,listArmor,listAmmo);
            listHealth.clear(); listWeapon.clear(); listArmor.clear(); listAmmo.clear();
       }
       else
       {
           System.out.println("Je vais chercher l'item le plus proche !");
       }
       min = 0; damage = null;
        
        if (item == null) {
            log.warning("NO ITEM TO RUN FOR!");
            if (nmNav.isNavigating()) return;
            bot.getBotName().setInfo("RANDOM NAV");
            navBot.navigate();
        } else {
            navBot.setItem(item);
            log.info("RUNNING FOR: " + item.getType().getName());
            bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
            navBot.navigate(item);
        }        
    }

    public void setItemsToRunAround(List<Item> itemsToRunAround) {
        this.itemsToRunAround = itemsToRunAround;
    }
    
    
}
