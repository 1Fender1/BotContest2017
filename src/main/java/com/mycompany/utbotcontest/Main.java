package com.mycompany.utbotcontest;

import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;

/**
 *
 * @author JB
 */
public class Main {
    
        public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM

        String host = "localhost";
        int port = 3000;

        if (args.length > 0)
        {
            host = args[0];
        }
        if (args.length > 1)
        {
            String customPort = args[1];
            try
            {
                port = Integer.parseInt(customPort);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid port. Expecting numeric. Resuming with default port: " + port);
            }
        }
        
        UT2004BotRunner runner = new UT2004BotRunner(Bot.class, "Runners", host, port);
        
        runner.setMain(true);
        runner.setName("Terminator");
        
        MatrixVisibility matrix = new MatrixVisibility();
        matrix.MatrixInitialized();
        //runner.setLogLevel(Level.OFF);
        runner.startAgents(1);
    }
    
}
