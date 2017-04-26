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

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    static List<ProbabilitesArmes> listeArmes = new ArrayList<ProbabilitesArmes>();
    
    private double epsilon = 0.2;
    private double randNumber = 0;
    
    private static void initProbaWeapon() {
        ProbabilitesArmes rocket = new ProbabilitesArmes(UT2004ItemType.ROCKET_LAUNCHER, 0.7, 0.1, 0, 0);
        ProbabilitesArmes flak = new ProbabilitesArmes(UT2004ItemType.FLAK_CANNON, 0.7, 0.2, 0, 0);
        ProbabilitesArmes lightning = new ProbabilitesArmes(UT2004ItemType.LIGHTNING_GUN, 0.7, 0.2, 0, 0);
        ProbabilitesArmes minigun = new ProbabilitesArmes(UT2004ItemType.MINIGUN, 0.6, 0.3, 0, 0);
        ProbabilitesArmes link = new ProbabilitesArmes(UT2004ItemType.LINK_GUN, 0.6, 0.4, 0, 0);
        ProbabilitesArmes assault = new ProbabilitesArmes(UT2004ItemType.ASSAULT_RIFLE, 0.5, 0.5, 4, 7);
        ProbabilitesArmes shock = new ProbabilitesArmes(UT2004ItemType.SHOCK_RIFLE, 0.4, 0.5, 0, 0);
        ProbabilitesArmes bio = new ProbabilitesArmes(UT2004ItemType.BIO_RIFLE, 0.4, 0.5, 0, 0);
        ProbabilitesArmes shield = new ProbabilitesArmes(UT2004ItemType.SHIELD_GUN, 0.4, 0.5, 0, 0);
        listeArmes.add(rocket);
        listeArmes.add(flak);
        listeArmes.add(lightning);
        listeArmes.add(minigun);
        listeArmes.add(link);
        listeArmes.add(assault);
        listeArmes.add(shock);
        listeArmes.add(bio);
        listeArmes.add(shield);
        Collections.sort(listeArmes);
        Collections.reverse(listeArmes);
    }
    
    //GETTER  
    public static UT2004ItemType getNom(ProbabilitesArmes pa) {
        return pa.nom;
    }
    public double getProbabilite(ProbabilitesArmes pa) {
        return pa.probabilite;
    }
    public double getPoids(ProbabilitesArmes pa) {
        return pa.poids;
    }
    public int getNbVictoire(ProbabilitesArmes pa) {
        return pa.nbVictoire;
    }
    public int getNbDefaite(ProbabilitesArmes pa) {
        return pa.nbDefaite;
    }
    
    //INCREMENTE LE NOMBRE DE VICTOIRE DE L'ARME
    public static void nbVIncrement(String weapon) {
        int index = 0;
        Iterator<ProbabilitesArmes> it = listeArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = listeArmes.get(index);
        update.nbVictoire++;
        listeArmes.set(index, update);
    }
    
    //INCREMENTE LE NOMBRE DE DEFAITE DE L'ARME
    public static void nbDIncrement(String weapon) {
        int index = 0;
        Iterator<ProbabilitesArmes> it = listeArmes.iterator();
        while (it.hasNext() && !it.next().nom.getGroup().getName().equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = listeArmes.get(index);
        update.nbDefaite++;
        listeArmes.set(index, update);
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
    
    //CHOIX DE L'ARME
    public UT2004ItemType choixArme() {
        randNumber = Math.random();
        ProbabilitesArmes pa;
        Random r = new Random();
        int randomWeapon;
        if (randNumber < epsilon) {
            randomWeapon = r.nextInt(9);
            pa = listeArmes.get(randomWeapon);
        }
        else {
           Collections.sort(listeArmes);
           Collections.reverse(listeArmes);
           pa = listeArmes.get(0);
        }
        return pa.nom;
    }
    
    //MAJ PROBABILITE DE L'ARME
    public static void updateProba(String weapon, int nb) {
        ProbabilitesArmes pa;
        double newP;
        int index = 0;
        Iterator<ProbabilitesArmes> it = listeArmes.iterator();
        while (it.hasNext() && !it.next().nom.equals(weapon)) {
            index++;
        }
        ProbabilitesArmes update = listeArmes.get(index);
        //PERDU => BAISSE DE LA PROBABILITE
        if (nb == 0) {
            newP = (update.probabilite * update.poids + update.nbVictoire) / (update.poids + update.nbVictoire + update.nbDefaite);
        }
        //GAGNE => AUGMENTATION DE LA PROBABILITE
        else {
            newP = (update.probabilite * update.poids + update.nbVictoire) / (update.poids + update.nbVictoire + update.nbDefaite);    
        }
        update.probabilite = newP;
    }
    
    public static void main(String args[]) {
        initProbaWeapon();
        //
        //CODE A TESTER
        //
        //updateProba(UT2004ItemType.ASSAULT_RIFLE, 1);
        Collections.sort(listeArmes);
        Collections.reverse(listeArmes);
        /*Iterator<ProbaArmes> it = listeArmes.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().nom.getGroup().getName());
        }*/
        
    }
}
