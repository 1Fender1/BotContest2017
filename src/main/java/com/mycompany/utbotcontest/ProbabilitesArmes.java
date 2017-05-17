/*
 * Copyright (C) 2017 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.WeaponDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author Romain
 */
public class ProbabilitesArmes implements Comparable<ProbabilitesArmes> {
    private Bot mainBot;
    private UT2004ItemType nom;
    private double probabilite;
    private double poids;
    private int nbVictoire;
    private int nbDefaite;
    private int firingMode;
    private double distanceInf;
    private double distanceSup;
    private WeaponMemory memory = new WeaponMemory();
    
    public ProbabilitesArmes(UT2004ItemType nom, double proba, double poids, int nbV, int nbD, double dI, double dS) {
        this.nom = nom;
        this.probabilite = proba;
        this.poids = poids;
        this.nbVictoire = nbV;
        this.nbDefaite = nbD;
        this.distanceInf = dI;
        this.distanceSup = dS;
    }
    public static List<ProbabilitesArmes> referencesArmes = new ArrayList<ProbabilitesArmes>();
    public static List<ProbabilitesArmes> inventaireArmes = new ArrayList<ProbabilitesArmes>();
    
    private double epsilon = 0.2;
    private double randNumber = 0;
    private int max_value = 100;
    private int sizeInventaire = 0;
    
    //INITIALISATION DES PROBABILITES SELON LES ARMES
    public void initProbabilitesA() throws IOException {
        ProbabilitesArmes rocket = memory.getProba(UT2004ItemType.ROCKET_LAUNCHER.toString());
        ProbabilitesArmes flak = memory.getProba(UT2004ItemType.FLAK_CANNON.toString());
        ProbabilitesArmes lightning = memory.getProba(UT2004ItemType.LIGHTNING_GUN.toString());
        ProbabilitesArmes minigun = memory.getProba(UT2004ItemType.MINIGUN.toString());
        ProbabilitesArmes link = memory.getProba(UT2004ItemType.LINK_GUN.toString());
        ProbabilitesArmes assault = memory.getProba(UT2004ItemType.ASSAULT_RIFLE.toString());
        ProbabilitesArmes shock = memory.getProba(UT2004ItemType.SHOCK_RIFLE.toString());
        ProbabilitesArmes bio = memory.getProba(UT2004ItemType.BIO_RIFLE.toString());
        ProbabilitesArmes shield = memory.getProba(UT2004ItemType.SHIELD_GUN.toString());
        referencesArmes.add(rocket);
        referencesArmes.add(flak);
        referencesArmes.add(lightning);
        referencesArmes.add(minigun);
        referencesArmes.add(link);
        referencesArmes.add(assault);
        referencesArmes.add(shock);
        referencesArmes.add(bio);
        referencesArmes.add(shield);
        Collections.sort(referencesArmes);
        Collections.reverse(referencesArmes);
        //affichageListe();
    }
    
    @Override
    public String toString() {
        String info = "Nom : " + this.nom + " Probabilité : " + this.probabilite + " Poids : " + this.poids + " Victoires : " + this.nbVictoire + " Défaites : " + this.nbDefaite;
        return info;
    }
    
    public String toStringForMemory() {
        return nom + ";" + probabilite+ ";" + poids + ";" + nbVictoire + ";" + nbDefaite + ";" + distanceInf + ";" + distanceSup;
    }
    
    @Override
    public int compareTo(ProbabilitesArmes o) {
        return new Double(probabilite).compareTo(o.probabilite);
    }
    
    //INCREMENTE LE NOMBRE DE VICTOIRE DE L'ARME
    protected void nbVIncrement(String weapon) throws IOException {
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = referencesArmes.get(index);
        update.nbVictoire++;
        referencesArmes.set(index, update);
        memory.addInfo(update.getNom().toString(), update.toStringForMemory());
    }
    
    //INCREMENTE LE NOMBRE DE DEFAITE DE L'ARME
    protected void nbDIncrement(String weapon) throws IOException {
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = referencesArmes.get(index);
        update.nbDefaite++;
        referencesArmes.set(index, update);
        memory.addInfo(update.getNom().toString(), update.toStringForMemory());
    }
    
    //AFFICHAGE DE L'INVENTAIRE DU BOT
    protected void affichageInventaire() {
       Iterator<ProbabilitesArmes> it = inventaireArmes.iterator(); 
       while (it.hasNext()) {
           System.out.println("INVENTAIRE DU BOT = " + it.next());
       }
    }
    
    //RETOURNE L'INDEX DE L'ARME
    protected int indexWeapon (String Weapon) {
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(Weapon)) {
            index++;
        }
        return index;
    }
    
    //INVENTAIRE DU BOT
    protected void inventaireBot(Weaponry w) {
        List<ProbabilitesArmes> list = new ArrayList<ProbabilitesArmes>(new LinkedHashSet<ProbabilitesArmes>());
        Weapon wp = null;
        int index = 0;
        ProbabilitesArmes pa = null;
        for (Iterator i = w.getWeapons().entrySet().iterator(); i.hasNext();) {
            Map.Entry couple = (Map.Entry)i.next();
            wp = (Weapon) couple.getValue();     
            index = indexWeapon(wp.getGroup().getName());
            pa = referencesArmes.get(index);
            list.add(pa);  
            index = 0;
        }
        inventaireArmes.clear();
        inventaireArmes.addAll(list);
        setSize(inventaireArmes.size());
    }
    
    //CHOIX DE L'ARME
    protected UT2004ItemType choixArme(Weaponry w) {
        double randNumber = Math.random();
        ProbabilitesArmes pa = null;
        Random r = new Random();
        int randomWeapon;
        if (sizeInventaire == 0) {
           inventaireBot(w); 
        }
        if (sizeInventaire <= w.getWeapons().size()) {
            inventaireBot(w);
        }
        if (randNumber < epsilon) {
            randomWeapon = r.nextInt(w.getWeapons().size());
            //System.out.println("ARME AU HASARD = " + pa);
            pa = inventaireArmes.get(randomWeapon);
        }
        else {
           //affichageInventaire();
           Collections.sort(inventaireArmes);
           Collections.reverse(inventaireArmes);
           pa = inventaireArmes.get(0);
           //System.out.println("MEILLEURE ARME = " + pa);
        }
        return pa.nom;
    }
    
    //MAJ PROBABILITE DE L'ARME
    protected void updateProba(String weapon) throws IOException {
        ProbabilitesArmes pa;
        double variation = 0;
        int index = 0;
        int var = 0;
        boolean horsBorne = false;
        boolean dInf = false;
        double d = mainBot.getDistanceBotTarget();
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        if (d < referencesArmes.get(index).distanceInf) {
            horsBorne = true;
            dInf = true;
        }
        if (d > referencesArmes.get(index).distanceSup) {
            horsBorne = true;
            dInf = false;
        }
        variation = valDistanceBorne(dInf, d);
        if (horsBorne) {
            referencesArmes.get(index).probabilite = (referencesArmes.get(index).probabilite * referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire) / (referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire + referencesArmes.get(index).nbDefaite + variation);
        }
        else {
            referencesArmes.get(index).probabilite = (referencesArmes.get(index).probabilite * referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire) / (referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire + referencesArmes.get(index).nbDefaite);
        }
        memory.addInfo(referencesArmes.get(index).getNom().toString(), referencesArmes.get(index).toStringForMemory());
    }
    
    //DIFFERENCE ENTRE LA DISTANCE ET LA BORNE
    protected double valDistanceBorne(boolean inf, double distance) {
        double delta = 0;
        double deltaFinal = 0;
        if (inf) {
            delta = Math.abs(distance - distanceInf);
        }
        else {
            delta = Math.abs(distance - distanceSup);
        }
        deltaFinal = delta / (distanceInf + distanceSup);
        return deltaFinal;
    }

    //GETTER  
    public UT2004ItemType getNom() {
        return this.nom;
    }
    
    public double getProbabilite() {
        return this.probabilite;
    }
    
    public double getPoids() {
        return this.poids;
    }
    
    public int getNbVictoire() {
        return this.nbVictoire;
    }
    
    public int getNbDefaite() {
        return this.nbDefaite;
    }
    
    //SETTERS
    public void setSize(int nb) {
        this.sizeInventaire = nb;
    }
    
    public void setMainBot(Bot mB) {
        this.mainBot = mB;
    }
    
}
