package com.mycompany.utbotcontest;

import java.util.Random;

/**
 *
 * @author HUGO
 */
public class Alea {
    Random rand;
    
    public Alea(){
        rand=new Random();
    }
    public boolean pourcentage(double f){
        if(rand.nextInt(100)<f)
            return true;
        else
            return false;
    }
    public boolean chancsur(int n){
        if(rand.nextInt(n)<1)
            return true;
        else
            return false;
    }
    
    
    public int indiceAleatoire(double tab[]){
        int i=0;
        double r=Math.random();
        double somme=0.0;
        while(r>=somme){
            somme=tab[i]+somme;
            i++;
        }
        return (i-1);
    }
    
    public void testIndiceAleatoire(){
        double tab[]={0.10,0.75,0.15};
        int tabResult[]={0,0,0};
        for(int i =0; i<10000; i++){
            tabResult[indiceAleatoire(tab)]++;
        }
        System.out.println("Resultat attendu/resultat obtenu : 1000/"+tabResult[0]+"   7500/"+tabResult[1]+"   1500/"+tabResult[2]);
    }
    
}

