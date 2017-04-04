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

import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnection;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectListener;
import cz.cuni.amis.pogamut.base.component.bus.IComponentBus;
import cz.cuni.amis.pogamut.base.utils.logging.IAgentLogger;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BeginMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.UT2004WorldView;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerFactory;
import cz.cuni.amis.pogamut.ut2004.server.IUT2004Server;
import cz.cuni.amis.pogamut.ut2004.server.impl.UT2004Server;
import cz.cuni.amis.utils.exception.PogamutException;
/**
 *
 * @author JB
 */
public class CustomControlServer extends UT2004Server implements IUT2004Server {

    private double currentUTTime;

    /*
     * BeginMessage listener - we get current server time here.
     */
    IWorldEventListener<BeginMessage> myBeginMessageListener = new IWorldEventListener<BeginMessage>() {
        @Override
        public void notify(BeginMessage event) {
            currentUTTime = event.getTime();
            System.out.println("Begin: " + event.toString());
        }
    };

    /*
     * Player listener - we simply print out all player messages we receive.
     */
    IWorldObjectListener<Player> myPlayerListener = new IWorldObjectListener<Player>() {
        public void notify(IWorldObjectEvent<Player> event) {
            System.out.println("Player: " + event.getObject().toString());
        }
    };

    //@Inject
    public CustomControlServer(UT2004AgentParameters params, IAgentLogger agentLogger, IComponentBus bus, SocketConnection connection, UT2004WorldView worldView, IAct act) {
        super(params, agentLogger, bus, connection, worldView, act);
    }


    /*
     * Our custom hook, where we initialize our listeners. Note that UT2004Server class
     * is just stub, so no helper methods are present (no prepareBot, botInitialized, etc
     as with Bot classes)
     */
    public void initialize() {
        getWorldView().addEventListener(BeginMessage.class, myBeginMessageListener);
        getWorldView().addObjectListener(Player.class, myPlayerListener);
        System.out.println("ControlConnection initialized.");
    }


    /**
     * This method is called when the server is started either from IDE or from command line.
     * It connects the server to the game.
     * @param args
     */
   /* public static void main(String args[]) throws PogamutException {
        //creating agent parameters - setting module name and connection adress
        UT2004AgentParameters params = new UT2004AgentParameters();
        params.setAgentId(new AgentId("ControlConnection"));
        params.setWorldAddress(new SocketConnectionAddress("127.0.0.1", 3001));

        //create module that tells guice it should instantiate OUR (this) class
        CustomControlServerModule module = new CustomControlServerModule();

        //creating pogamut factory
        UT2004ServerFactory fac = new UT2004ServerFactory(module);
        CustomControlServer cts = (CustomControlServer) fac.newAgent(params);

        //starting the connection - connecting to the server
        cts.start();
        //launching our custom method
        cts.initialize();
    }*/
}	
