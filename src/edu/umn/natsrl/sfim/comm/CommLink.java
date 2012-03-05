/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea) 
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

package edu.umn.natsrl.sfim.comm;

import edu.umn.natsrl.sfim.SFIMManager;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class CommLink extends Thread {
    
    private static Vector<CommLink> commlinkList = new Vector<CommLink>();
    
    private Vector<Communicator> communicatorList = new Vector<Communicator>();    
    
    private String name;    // comm_link name (to identify)
    private int port;
    private CommProtocol protocol;
    private ServerSocket serverSocket;
    private HashMap<Integer, Controller> controllers = new HashMap<Integer, Controller>();

    private SFIMManager manager = SFIMManager.getInstance();
    
    /**
     * constructor
     * @param name comm_link name
     * @param port comm_link port
     * @param protocol CommProtocol (MnDot, DMSXml)
     */
    public CommLink(String name, int port, int protocol) {
        //setDaemon(true);
        this.name = name;
        this.port = port;
        this.protocol = CommProtocol.getCommProtocol(protocol);
        commlinkList.add(this);
//        System.out.println("===> CommLink : " + this.name + " is created ---");
    }

    /**
     * save controller to list
     * @param ctrl Controller
     */
    public void addController(Controller ctrl) {
        this.controllers.put(ctrl.getDrop(), ctrl);
    }
    
    public Controller[] getControllers() {
        int size = this.controllers.size();
        return this.controllers.values().toArray(new Controller[size]);
    }

    @Override
    /**
     * comm_link server thread
     */
    public void run() {
        Communicator comm = null;
        Socket socket = null;     
        
        try {
            serverSocket = new ServerSocket(this.port);
            serverSocket.setReuseAddress(true);

            while (!isInterrupted()) {
                try {
                    serverSocket.setSoTimeout(5*1000);
                    socket = serverSocket.accept();
                    socket.setReuseAddress(true);
                    comm = Communicator.createComm(this.protocol, socket, this);
                    communicatorList.add(comm);                    
                    comm.setControllers(controllers);
                    comm.start();

                } catch (SocketTimeoutException ex) {
                    if (isInterrupted()) {
                        terminate();
                        return;
                    }

                }

            }
        } catch (Exception ex) {
            System.out.println("===> CommLink : " + this.name + " is disconnected ---");
//            ex.printStackTrace();
            terminate();
        }
    }

    /**
     * Stop this thread and communicator included this commlink
     */
    public void terminate() {        
        try {
            for(Communicator c : this.communicatorList) {
                c.terminate();
            }
            serverSocket.close();
        } catch (Exception ex) {}
    }

    /**
     * @return CommLink Name
     */
    public String getLinkName() {
        return this.name;
    }

    /**
     * @return Protocol (see CommProtocol)
     */
    public CommProtocol getProtocol() {
        return protocol;
    }

    public static Vector<CommLink> getCommlinkList() {
        return commlinkList;
    }
    
}
