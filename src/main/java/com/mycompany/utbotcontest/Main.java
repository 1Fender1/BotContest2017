package com.mycompany.utbotcontest;

import com.sun.glass.ui.Window;
import cz.cuni.amis.pogamut.base.communication.connection.exception.ConnectionException;
import cz.cuni.amis.pogamut.base.component.bus.event.BusAwareCountDownLatch.BusStoppedInterruptedException;
import cz.cuni.amis.pogamut.base.component.exception.ComponentCantStartException;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author JB
 */
public class Main {
    
        private final String host = "-host";
        private final String port = "-p";
        private final String nomv = "-nomv";
        private final String help = "-h";
    
    
        private void printUsage()
        {
            System.out.println("Use : ");
            System.out.println(help + " for more usage");
            System.out.println(nomv);
            System.out.println("[" + host + "]" + "[" + nomv + "]");
            System.out.println("[" + host + "]" + " host " + "[" + port + "]" + " port " + "[" + nomv + "]");
        }
    
    
    
        public boolean isArgsOk(String args[])
        {
            boolean containPort = false;
            boolean containHost = false;
            boolean containNomv = false;
            boolean containHelp = false;
            
            if (args.length > 5)
            {
                printUsage();
                return false;
            }
            
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            containPort = arguments.contains(port);
            containHost = arguments.contains(host);
            containNomv = arguments.contains(nomv);
            containHelp = arguments.contains(help);
            
            try{
                if (containPort)
                {
                    String temp = arguments.get(arguments.lastIndexOf(port) + 1);
                    if (containPort && (temp.equals(host) || temp.equals(port) || temp.equals(nomv) || temp.equals(help)))
                    {
                        printUsage();
                        return false;
                    }
                }
                if (containHost)
                {
                    String temp = arguments.get(arguments.lastIndexOf(host) + 1);
                    if (containHost && (temp.equals(host) || temp.equals(port) || temp.equals(nomv) || temp.equals(help)))
                    {
                        printUsage();
                        return false;
                    }
                }
            }catch(IndexOutOfBoundsException e)
            {
                printUsage();
                return false;
            }
            
            if (containHelp && args.length > 1)
            {
                printUsage();
                return false;
            }
            
            if (containNomv && !args[args.length-1].equals(nomv))
            {
                printUsage();
                return false;
            }
            
            if (containPort && !containHost)
            {
                printUsage();
                return false;
            }

            
           return true;
        }
        
        public String getHost(String args[])
        {
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            if (!arguments.contains(host))
                return null;
            else
            {
                for (int i = 0; i < arguments.size(); i++)
                {
                    if (arguments.get(i).equals(host))
                    {
                        try
                        {
                            return arguments.get(i+1);
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            printUsage();
                            return null;
                        }
                    }
                }
            }
            return null;
        }
        
        public int getPort(String args[])
        {
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            if (!arguments.contains(port))
                return -1;
            else
            {
                for (int i = 0; i < arguments.size(); i++)
                {
                    if (arguments.get(i).equals(port))
                    {
                        try
                        {
                            int port = Integer.parseInt(arguments.get(i + 1));
                            return port;
                        }
                        catch (NumberFormatException e)
                        {
                            System.out.println("Invalid port. Expecting numeric. Resuming with default port: " + port);
                        }
                        catch (IndexOutOfBoundsException e2)
                        {
                            printUsage();
                            return -1;
                        }
                    }
                }
            }
            return -1;
        }
    
        
        public boolean containPort(String args[])
        {
            boolean containPort = false;
            
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            containPort = arguments.contains(port);
            return containPort;
        }
        
        public boolean containHost(String args[])
        {
            boolean containHost = false;
            
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            containHost = arguments.contains(host);
            return containHost;
        }
        
        public boolean containNomv(String args[])
        {
            boolean containNomv = false;
            
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            containNomv = arguments.contains(nomv);
            return containNomv;
        }
       
        public boolean containHelp(String args[])
        {
            boolean containHelp = false;
            
            List<String> arguments = new ArrayList<String>();
            arguments.addAll(Arrays.asList(args));
            
            containHelp = arguments.contains(help);
            return containHelp;
        }
        
        
        public static void main(String args[]) throws PogamutException {
        // wrapped logic for bots executions, suitable to run single bot in single JVM

        Main main = new Main();
        
        if (!main.isArgsOk(args))
            return;
        
        boolean isMatrixEnable = false;
        String hostVal = "localhost";
        int portVal = 3000;

        if (main.containHelp(args))
        {
            main.printUsage();
            return;
        }

        if (main.containHost(args))
        {
            hostVal = main.getHost(args);
        }
       
        if (main.containPort(args))
        {
            portVal = main.getPort(args);
        }
        
       
        System.out.println("Connexion à : " + hostVal + " " + portVal);
        /*if (!main.containNomv(args))
        {
            MatrixVisibility matrix = new MatrixVisibility();
            matrix.MatrixInitialized();
        }*/
        
        while (true)
        {
            try
            {
                UT2004BotRunner runner = new UT2004BotRunner(Bot.class, "Runners", hostVal, portVal);
                runner.setMain(true);
                runner.setLogLevel(Level.OFF);
                runner.setConsoleLogging(false);
                runner.startAgents(1);
                Thread.sleep(1500);
            }
            catch (ComponentCantStartException e)
            {
                Throwable cause = e.getCause();
                if (cause instanceof ConnectionException)
                {
                    System.out.println("Connection to server failed... retrying");
                    e.printStackTrace();
                }
                else if (cause instanceof BusStoppedInterruptedException)
                {
                    e.printStackTrace();
                    System.out.println("Aborting...");
                    break;
                }
                else
                {
                    e.printStackTrace();
                    System.out.println("Some other cause for ComponentCantStartException... retrying");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("Some other exception... retrying");
            }
        }    
    }
    
}
