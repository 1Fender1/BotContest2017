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

import static com.mycompany.utbotcontest.ProbabilitesArmes.referencesArmes;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Romain
 */
public class Comportement {
    private Bot mainBot;
    private String nomComportement;
    protected int frags;
    protected int deaths;
    protected double probaAgressif;
    protected double probaDefensif;
    
    public Comportement(Bot mainBot) {
        this.mainBot = mainBot;
        this.nomComportement = "";
        this.frags = mainBot.frags;
        this.deaths = mainBot.deaths;
        this.probaAgressif = 0.5;
        this.probaDefensif = 0.5;
    }
    
    //LES 2 COMPORTEMENTS
    protected String agressif = "Agressif";
    protected String defensif = "Defensif";
    
    //NOMBRE D'ENNEMI EN VUE
    protected int enemyCount = 0;
    public List<Player> listEnemy = new ArrayList<Player>();
    
    //RATIO DU BOT FRAGS / DEATHS
    protected double ratioBot() {
        setFrags();
        setDeaths();
        double ratio;
        if (deaths == 0) {
            return ratio = frags;
        }
        else {
            return ratio = frags / deaths;
        }  
    }
    
    //COMPTE LE NOMBRE D'ENNEMI EN VU
    protected void enemyVisisbleCount() {
        Player pl = null;
        if (mainBot.getPlayers().canSeeEnemies()) {
            for (Iterator i = mainBot.getPlayers().getEnemies().entrySet().iterator(); i.hasNext();) {
                Map.Entry couple = (Map.Entry)i.next();
                pl = (Player) couple.getValue();
                listEnemy.add(pl);
            }
        }
        setEnemyCount(listEnemy.size());
    }
    
    //CHANGEMENT DE COMPORTEMENT
    protected void switchC() {
        if (getNomComportement().equals(agressif)) {
            setNomComportement(defensif);
        }
        else {
            setNomComportement(agressif);
        }
    }
    
    //MAJ PROBABILITE AGRESSIF
    protected void updateProbaAgressif(boolean killed) {
        double newP = 0;
        setFrags();
        setDeaths();
        if (killed) {
            newP = (probaAgressif * frags) / 10 * (frags + deaths);
            probaAgressif += newP;
        }
        else {
            newP = (probaAgressif * frags) / 10 * (frags + deaths);
            probaAgressif -= newP;
        }
    }
    
    //MAJ PROBABILITE DEFENSIF
    protected void updateProbaDefensif(boolean killed) {
        double newP = 0;
        setFrags();
        setDeaths();
        if (killed) {
            newP = (probaDefensif * frags) / 10 * (frags + deaths);
            probaDefensif += newP;
        }
        else {
            newP = (probaDefensif * frags) / 10 * (frags + deaths);
            probaDefensif -= newP;
        }
    }
    
    //CHANGEMENT DE COMPORTEMENT 
    protected void changeComportement() {
        double ratio = ratioBot();
        enemyVisisbleCount();
        Alea a = new Alea();
        double rand;
        if (enemyCount > 1 && !mainBot.getInfo().hasUDamage() && mainBot.getSenses().isBeingDamaged()) {
            nomComportement = defensif;
        }
        else {
            if (mainBot.getInfo().getHealth() <= mainBot.healthLevel && mainBot.getInfo().getArmor() <= 75) {
                setNomComportement(defensif);
            }
            //EN FONCTION DE SON RATIO
            else {
                if (ratio > 1.5) {
                    setNomComportement(defensif);
                }
                if (ratio < 0.5) {
                    setNomComportement(agressif);
                }
                if (ratio > 0.5 && ratio < 1.5) {
                    if (probaAgressif > probaDefensif) {
                        setNomComportement(agressif);
                    }
                    else {
                        setNomComportement(defensif);
                    }
                }
            }
        }
        changeEtat();
        mainBot.getLog().info("ETAT = " + nomComportement);
    }
    
    //CHANGE LES VARIABLES BOOLENNES DES DIFFERENTS ETATS SELON LE COMPORTEMENT
    protected void changeEtat() {
        if (isAgressif()) {
            mainBot.setEngage(true);
            mainBot.setPursue(true);
            mainBot.setEvasion(false);
        }
        else {
            mainBot.setEngage(false);
            mainBot.setPursue(false);
            mainBot.setEvasion(true);
        }
    }
    
    //RETOURNE SI LE BOT EST AGRESSIF OU DEFENSIF
    public boolean isAgressif() {
        if (getNomComportement().equals(agressif)) {
            return true;
        }
        return false;
    }
    
    //GETTERS
    public String getNomComportement() {
        return nomComportement;
    }
    
    public int getEnemyCount() {
        return enemyCount;
    }
    
    //SETTERS
    public void setNomComportement(String nom) {
        this.nomComportement = nom;
    }

    public void setEnemyCount(int val) {
        this.enemyCount = val;
    }
    
    public void setFrags() {
        this.frags = mainBot.frags;
    }
    
    public void setDeaths() {
        this.frags = mainBot.deaths;
    }

}
