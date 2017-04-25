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
        
        if(rand.nextInt(100)<f)
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
    
    
    
    //test function to debug
    public void test ()
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
    
    public static void main (String[] args)
    {
        Alea r = new Alea();
        r.test();
    }
}
