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
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 *
 * @author Romain
 */
public class ProbabilitesArmes implements Comparable<ProbabilitesArmes> {
    private UT2004ItemType nom;
    private double probabilite;
    private double poids;
    private int nbVictoire;
    private int nbDefaite;
    
    public ProbabilitesArmes(UT2004ItemType nom, double proba, double poids, int nbV, int nbD) {
        this.nom = nom;
        this.probabilite = proba;
        this.poids = poids;
        this.nbVictoire = nbV;
        this.nbDefaite = nbD;
    }
    public static List<ProbabilitesArmes> referencesArmes = new ArrayList<ProbabilitesArmes>();
    public static List<ProbabilitesArmes> inventaireArmes = new ArrayList<ProbabilitesArmes>();
    
    private double epsilon = 0.2;
    private double randNumber = 0;
    
    public void initProbabilitesA() {
        ProbabilitesArmes rocket = new ProbabilitesArmes(UT2004ItemType.ROCKET_LAUNCHER, 0.7, 0.1, 0, 0);
        ProbabilitesArmes flak = new ProbabilitesArmes(UT2004ItemType.FLAK_CANNON, 0.7, 0.2, 0, 0);
        ProbabilitesArmes lightning = new ProbabilitesArmes(UT2004ItemType.LIGHTNING_GUN, 0.7, 0.2, 0, 0);
        ProbabilitesArmes minigun = new ProbabilitesArmes(UT2004ItemType.MINIGUN, 0.6, 0.3, 0, 0);
        ProbabilitesArmes link = new ProbabilitesArmes(UT2004ItemType.LINK_GUN, 0.6, 0.4, 0, 0);
        ProbabilitesArmes assault = new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 0.5, 4, 7);
        ProbabilitesArmes shock = new ProbabilitesArmes(UT2004ItemType.SHOCK_RIFLE, 0.4, 0.5, 0, 0);
        ProbabilitesArmes bio = new ProbabilitesArmes(UT2004ItemType.BIO_RIFLE, 0.4, 0.5, 0, 0);
        ProbabilitesArmes shield = new ProbabilitesArmes(UT2004ItemType.SHIELD_GUN, 0.4, 0.5, 0, 0);
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
    
    //INCREMENTE LE NOMBRE DE VICTOIRE DE L'ARME
    public void nbVIncrement(String weapon) {
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = referencesArmes.get(index);
        update.nbVictoire++;
        referencesArmes.set(index, update);
    }
    
    //INCREMENTE LE NOMBRE DE DEFAITE DE L'ARME
    public void nbDIncrement(String weapon) {
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = referencesArmes.get(index);
        update.nbDefaite++;
        referencesArmes.set(index, update);
    }
    
    @Override
    public String toString() {
        String info = "Nom : " + this.nom + " Probabilité : " + this.probabilite + " Poids : " + this.poids + " Victoires : " + this.nbVictoire + " Défaites : " + this.nbDefaite;
        return info;
    }
    
    @Override
    public int compareTo(ProbabilitesArmes o) {
        return new Double(probabilite).compareTo(o.probabilite);
    }
    
     public void inventaireBot(Weaponry w) {
        Weapon wp = null;
        int index = 0;
        ProbabilitesArmes pa = null;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        for (Iterator i = w.getWeapons().entrySet().iterator(); i.hasNext();) {
            Map.Entry couple = (Map.Entry)i.next();
            wp = (Weapon) couple.getValue();       
            while (it.hasNext() && !it.next().nom.getGroup().getName().equals(wp.getGroup().getName())) {
                index++;
            }
            pa = referencesArmes.get(index);
            inventaireArmes.add(pa);  
        }
    }
    
    //CHOIX DE L'ARME
    public UT2004ItemType choixArme(Weaponry w) {
        double randNumber = Math.random();
        ProbabilitesArmes pa = null;
        Random r = new Random();
        int randomWeapon;
        inventaireBot(w);
        if (randNumber < epsilon) {
            randomWeapon = r.nextInt(inventaireArmes.size());
            pa = inventaireArmes.get(randomWeapon);
        }
        else {
           Collections.sort(inventaireArmes);
           Collections.reverse(inventaireArmes);
           pa = inventaireArmes.get(0);
        }
        return pa.nom;
    }
    
    //MAJ PROBABILITE DE L'ARME
    public void updateProba(String weapon) {
        ProbabilitesArmes pa;
        double newP;
        int index = 0;
        Iterator<ProbabilitesArmes> it = referencesArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        referencesArmes.get(index).probabilite = (referencesArmes.get(index).probabilite * referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire) / (referencesArmes.get(index).poids + referencesArmes.get(index).nbVictoire + referencesArmes.get(index).nbDefaite);
    }
    
}
