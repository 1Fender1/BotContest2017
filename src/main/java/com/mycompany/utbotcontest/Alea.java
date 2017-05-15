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
        int i;
        double r;
        double somme = 0.0;
        for(i=0; i<tabProba.length; i++){
            somme=tabProba[i]+somme;
        }
        r=Math.random()*somme;
        i =0;
        somme=0;
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
        double tabVal[] = {0.35,0.15,0.24,0.26};
        
        /*int[] tabRes = new int[4];
        for (int i = 0 ; i < 100000; i++)
        {
            tabRes[indiceAlea(tabVal)]++;
        }
        System.out.println("Res obtenu/Res Attendu : " + tabRes[0] + "/35000 ;" + tabRes[1] + "/15000 ; "+ tabRes[2] + "/24000 ; " + tabRes[3] + "/26000");*/
        int nbIndice0 = 0, nbIndice1 = 0, nbIndice2 = 0, nbIndice3 = 0, temp, nbVal;
        for (int i = 0; i < 1000000; i++)
        {
            temp = indiceAlea(tabVal);
            switch (temp)
            {
                case 0: nbIndice0++;
                    break;
                case 1: nbIndice1++;
                    break;
                case 2: nbIndice2++;
                    break;
                case 3: nbIndice3++;
                    break;
                default: break;
            }       
        }
        nbVal = nbIndice0 + nbIndice1 + nbIndice2 + nbIndice3;
        System.out.println("Valeurs experimentales");
        System.out.println("prob0 " + (float)nbIndice0/nbVal + " prob1 " + (float)nbIndice1/nbVal + " prob2 " + (float)nbIndice2/nbVal + " prob 3 " + (float)nbIndice3/nbVal);
        System.out.println("Valeurs théoriques");
        System.out.println("prob 0 0.35 prob1 0.15 prob2 0.24 prob3 0.26");
    }
    
    public static void main (String[] args)
    {
        Alea r = new Alea();
        r.testIndiceAlea();
    }
}
