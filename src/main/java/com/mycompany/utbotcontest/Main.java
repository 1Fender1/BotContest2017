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

import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;

/**
 *
 * @author JB
 */
public class Main {
    
        public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM

        String host = "localhost";//"locahost"
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
        
        UT2004BotRunner runner = new UT2004BotRunner(RaycastingBot.class, "SuperTeam", host, port);
        runner.setMain(true);
        runner.setName("Terminator");
        //runner.setLogLevel(Level.OFF);
        runner.startAgent();
    }
    
}
