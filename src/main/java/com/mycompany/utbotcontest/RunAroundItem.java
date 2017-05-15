package com.mycompany.utbotcontest;

import static com.mycompany.utbotcontest.ProbabilitesArmes.inventaireArmes;
import static com.mycompany.utbotcontest.ProbabilitesArmes.referencesArmes;
import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo.GameInfoUpdate;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPointNeighbourLink;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    
    private Alea probaAlea = new Alea();
    
    private final int nbTypeItems = 5;
    
    private final double tauxProbaArme = 0.5;
    
    TabooSet<Item> tabooItems;
    
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
        
        tabooItems = new TabooSet<Item>(bot);
        
    }
    
    
    
    
     //Listes qui vont contenir les différents items selon leur categorie.
      protected List<Item> listWeapon = new ArrayList<Item>();
      protected List<Item> listArmor = new ArrayList<Item>();
      protected List<Item> listHealth = new ArrayList<Item>();
      protected List<Item> listAmmo = new ArrayList<Item>();
      protected Item damage = null;
      protected double min;
      
      public int[] harmonisationProba()
      {
        
        int[] tabProba = new int[nbTypeItems];

        tabProba[0] = probaHealth();
        tabProba[1] = probaWeapon();
        tabProba[2] = probaArmor();
        tabProba[3] = probaAmmo();
        tabProba[4] = probaUDamage();


        int overValues = 0;
        int temp = 1;
        int repartition;

        for (int i = 0; i < nbTypeItems; i++)
        {
            overValues += tabProba[i];
        }
        overValues -= 100;
        repartition = Math.abs(overValues/nbTypeItems);

        if (overValues > 0)
        {
            while (temp > 0 && repartition > 0)
            {
                temp = 0;
                for (int i = 0; i < nbTypeItems; i++)
                {
                    if (tabProba[i] < repartition)
                    {
                        tabProba[i] = 0;
                    }
                    else
                    {
                        tabProba[i] -= repartition;
                    }
                    temp += tabProba[i];
                }
                temp -= 100;
                repartition = Math.abs(temp/nbTypeItems);

            }

            if (temp > 0)
            {   boolean ok = false;
                while (!ok)
                {
                    Random r = new Random();
                    int valTemp = r.nextInt(nbTypeItems);
                    if (tabProba[valTemp]- temp >= 0)
                    {
                        tabProba[valTemp] -= temp;
                        ok = true;
                    }
                }
            }
        }
        else if (overValues < 0)
        {
            while (temp > 0 && repartition > 0)
            {
                temp = 0;
                for (int i = 0; i < nbTypeItems; i++)
                {
                    if (tabProba[i] + repartition > 100)
                    {
                        tabProba[i] = 100;
                    }
                    else
                    {
                        tabProba[i] += repartition;
                    }
                    temp += tabProba[i];
                }
                temp -= 100;
                repartition = Math.abs(temp/nbTypeItems);

            }

            if (temp > 0)
            {
                Random r = new Random();
                tabProba[r.nextInt(nbTypeItems)] += temp;
            }
        }

        return tabProba;
      }
      
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
      
      ////////////////////////
      //SELECT ITEM IN LIST//
      ///////////////////////
      
      //Si on a besoin de vie, alors on sélectionne l'item le plus proche dans la liste vie
    protected Item selectItemInListHealth(List<Item> listH){
        Item itHealth = null;
        if (listH.isEmpty())
            return null;
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
        return itHealth;
    }
      
      //On sélectionne l'item correspondant à l'arme préférée du bot
      protected Item selectItemInListWeapon(List<Item> listW){
          Item itWeapon = null;
          if (listW.isEmpty())
              return null;
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
            if (listA.isEmpty())
                return null;
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
            return itArmor;
        }
      
      //On sélectionne l'item correspondant aux munitions que le bot a besoin
      protected Item selectItemInListAmmo(List<Item> listAm){
          Item itAmmo = null;
          if (listAm.isEmpty())
              return null;
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

      
      //////////////////////////
      //PROBABIITE DE L'EVENT//
      /////////////////////////
      protected int probaHealth(){
          return (100-((info.getHealth()*100)/mainBot.getGame().getMaxHealth()));
      }
      
      protected int probaArmor(){
          int pourcentShield;
          if(!items.getAllItems(UT2004ItemType.SUPER_SHIELD_PACK).isEmpty())
              pourcentShield = (info.getArmor()*100)/150;
          else
              pourcentShield = (info.getArmor()*100)/50;
          return (100-pourcentShield);
      }
      
      protected int probaWeapon(){
          float nbWeapon=0;
          float compteurArme = 0;
          for(ProbabilitesArmes pa : referencesArmes){
              if(pa.getProbabilite() >= tauxProbaArme){
                for(ItemType weap : ItemType.Category.WEAPON.getTypes()){
                          if(mainBot.getWeaponry().hasWeapon(weap) && weap.getGroup().toString().equals(pa.getNom().getGroup().toString())){
                              nbWeapon++;
                          }
                }
                compteurArme++;
              }
          }
          return (int)(100 - ((nbWeapon/compteurArme)*100));
      }
      
      protected int probaAmmo(){
          float sommeMaxAmmo = 0;
          float sommeCurrentAmmo = 0;
          if(inventaireArmes.isEmpty())
              return 0;
            for(ProbabilitesArmes pa: referencesArmes){
                if(pa.getProbabilite() >= tauxProbaArme){
                    for(ItemType ammoType : ItemType.Category.AMMO.getTypes()){
                          if(mainBot.getWeaponry().hasAmmo(ammoType) && ammoType.getGroup().toString().equals(pa.getNom().getGroup().toString())){
                              sommeMaxAmmo += weaponry.getMaxAmmo(ammoType);
                              sommeCurrentAmmo += weaponry.getAmmo(ammoType);
                          }
                    }
                }
            }
            return (int)(100 - ((sommeCurrentAmmo/sommeMaxAmmo)*100));
      }
      
      protected int probaUDamage(){
          return 0;
      }
      
    protected Item getNextItem (List<Item> listH,List<Item> listW,List<Item> listA,List<Item> listAm){
       
        Alea gen = new Alea();
        
        Item item = null;
        
        int[] probaRechercheItemTemp = harmonisationProba();
        double[] probaRechercheItem = new double[nbTypeItems];
        
        for (int i = 0; i < nbTypeItems; i++)
        {
            probaRechercheItem[i] = (double)probaRechercheItemTemp[i]/100;
        }
        
        int typeItem = gen.indiceAlea(probaRechercheItem);
        
        //0 Health
        //1 Weapons
        //2 Armors
        //3 Ammos
        //4 UDammage
        System.out.println("+++++++TypeItem : "+ typeItem + "+++++++++++");
        
        switch (typeItem)
        {
            case 0 : item = selectItemInListHealth(listH); break;
            case 1 : item = selectItemInListWeapon(listW); break;
            case 2 : item = selectItemInListArmor(listA); break;
            case 3 : item = selectItemInListAmmo(listAm); break;
            case 4 : item = damage; break;
            default : break;
        }
        
        
        if (probaAmmo() == 0 && probaArmor() == 0 && probaHealth() == 0 && probaUDamage() == 0 && probaWeapon() == 0)
            return null;
        
        return item;
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
        

        
        for (Item item : listItem)
        {
            if (item.getType().getCategory().toString().equals("HEALTH") && probaHealth() > 0)
            {
                return item;
            }
            if (item.getType().getCategory().toString().equals("ARMOR") && probaArmor() > 0)
            {
                return item;
            }
            if (item.getType().getCategory().toString().equals("WEAPON") && probaWeapon() > 0)
            {
                return item;
            }
            if (item.getType().getCategory().toString().equals("AMMO") && probaAmmo() > 0)
            {
                return item;
            }
            if (item.getType().getCategory().toString().equals("OTHER") && probaUDamage() > 0)
            {
                return item;
            }
        }
        
        return null;        
    }
    
public void copySetToList(Set s, List l){
    Object[] tab=s.toArray();
    l.clear();
    for(int i =0; i<s.size(); i++){
        l.add(tab[i]);
    }
}
    
    ////////////////////////////
    // STATE RUN AROUND ITEMS //
    ////////////////////////////
protected List<Item> itemsToRunAround = null;

    protected void stateRunAroundItems() {
        //log.info("Decision is: ITEMS");
        //config.setName("Hunter [ITEMS]");
        System.out.println("isNavigatingToItem : " + navBot.isNavigatingToItem());
        if (navBot.isNavigatingToItem() && navBot.getItem() != null)
        {
            bot.getBotName().setInfo("ITEM: " + navBot.getItem().getType().getName() + "");
            if(navBot.isStuck()){
                log.info("ADD TABOO: " + navBot.getItem().getType().getName());
                tabooItems.add(navBot.getItem(), 180);
            }
        }
        else
        {
            bot.getBotName().setInfo("RUN AROUND ITEM");
        }
       
        if (navBot.isNavigatingToItem()) return;
        
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
       
       
       //Item item = MyCollections.getRandom(tabooItems.filter(interesting));7
       copySetToList(tabooItems.filter(interesting),interesting);
       Item item = null;
       item = existNearInterstingItem(interesting);
       navBot.setItem(item);
       if (item == null)
       {
            addItemsInList(interesting);
            item = getNextItem (listHealth,listWeapon,listArmor,listAmmo);
            listHealth.clear(); listWeapon.clear(); listArmor.clear(); listAmmo.clear();
            navBot.setItem(item);
       }
       else
       {
           System.out.println("Je vais chercher l'item le plus proche !");
       }
       
       
       min = 0; damage = null; /*indice = 0;
       for(int i =0; i<5; i++){
           tabProba[i] = 0;
           tabItem[i] = null;
       }*/
        if (item == null) {
        	log.warning("NO ITEM TO RUN FOR!");
        	if (mainBot.getNavigation().isNavigatingToItem()) return;
        	bot.getBotName().setInfo("RANDOM NAV");
                navBot.navigate();
        } else {
            /*for(Map.Entry<UnrealId, NavPointNeighbourLink> entry : item.getNavPoint().getIncomingEdges().entrySet()){
                //navBuilder.modifyNavPoint(entry.getValue().getFromNavPoint().getId().getStringId()).removeEdgeTo("DM-1on1-Irondust.PathNode28");
                mainBot.getNavBuilder().removeEdge(item.getNavPointId().getStringId(),entry.getValue().getFromNavPoint().getId().getStringId());
            }*/
            navBot.setItem(item);
            log.info("RUNNING FOR: " + item.getType().getName());
            bot.getBotName().setInfo("ITEM: " + item.getType().getName() + "");
            if(!navBot.navigate(item)){
                log.info("ADD TABOO: " + item.getType().getName());
                tabooItems.add(item, 180);
            }
        }        
    }

    public void setItemsToRunAround(List<Item> itemsToRunAround) {
        this.itemsToRunAround = itemsToRunAround;
    }
}
