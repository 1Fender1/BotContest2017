package com.mycompany.utbotcontest;

import java.util.Random;

/**
 *
 * @author HUGO
 */
public class Alea {
    static Random rand=new Random();
    public static boolean pourcent(double f){
        if(rand.nextInt(100)<f)
            return true;
        else
            return false;
    }
    public static boolean chancsur(int n){
        if(rand.nextInt(n)<1)
            return true;
        else
            return false;
    }
}
