package com.mycompany.utbotcontest;

import java.util.Random;

/**
 *
 * @author HUGO
 */
public class Alea {
    
    private final Random rand;
    
    
    public Alea () {
        
        rand = new Random();
        
    }
    
    //return true with a probability of f in percent
    public boolean pourcentDeChance(double f) {
        
        if(Math.random()*100<f)
            return true;
        else
            return false;
    }
    
    //return true with a probability of  1 time on n
    public boolean uneChanceSur(int n) {
        
        if(rand.nextInt(n)<1)
            return true;
        else
            return false;
    }
    
    public int indiceAlea(double tabProba[]){//Genere aleatoirement un indice pour un tableau ou chaque case est une probabilité sur 1
        double r=Math.random();
        double somme = 0.0;
        int i =0;
        while(r>=somme){
            somme=somme+tabProba[i];
            i++;
        }
        return i-1;
    }
    
    //test function to debug
    public void testPourcentDeChance()
    {
        boolean[] tabVal = new boolean[10000];
        int nbTrue = 0;
        boolean temp;
        for (int i = 0 ; i < 10000; i++)
        {
            temp = pourcentDeChance(75);
            tabVal[i] = temp;
            if (temp)
                nbTrue++;
        }
        System.out.println("il y a " + nbTrue + " true");
    }
    
     public void testIndiceAlea()
    {
        double tabVal[] ={0.35,0.15,0.24,0.26};
        
        int[] tabRes =new int[4];
        for (int i = 0 ; i < 100000; i++)
        {
            tabRes[indiceAlea(tabVal)]++;
        }
        System.out.println("Res obtenu/Res Attendu : " + tabRes[0] + "/35000 ;" + tabRes[1] + "/15000 ; "+ tabRes[2] + "/24000 ; " + tabRes[3] + "/26000");
    }
    
    public static void main (String[] args)
    {
        Alea r = new Alea();
        r.testPourcentDeChance();
    }
}
