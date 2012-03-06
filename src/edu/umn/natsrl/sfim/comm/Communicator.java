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

import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.SFIMManager;
import edu.umn.natsrl.sfim.comm.mndot.CommMnDot;
import edu.umn.natsrl.sfim.comm.mndot.MnDotHelper;
import edu.umn.natsrl.sfim.comm.dmsxml.CommDMSXml;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class Communicator extends Thread {



    protected Socket socket;
    protected OutputStream os;
    protected InputStream is;
    protected byte[] buffer;
    protected HashMap<Integer, Controller> controllers;
    protected SFIMManager manager = SFIMManager.getInstance();
    protected MnDotHelper parser = new MnDotHelper();
    protected CommLink commlink;
    protected CommProtocol protocol;

    /**
     * constructor
     * @param socket client socket to communicate with IRIS
     * @param commlink comm_link that includes this communicator
     */
    public Communicator(Socket socket, CommLink commlink) {
        this.socket = socket;
        this.commlink = commlink;
        this.protocol = this.commlink.getProtocol();
    }

    /**
     * set controller list
     * @param controllers controllers included in this communicator
     */
    public void setControllers(HashMap<Integer, Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     * create Communicator according to protocol
     * @param protocol CommProtocol (MnDot, DMSXml)
     * @param socket client socket of comm_link
     * @return Communicator CommMnDot or CommDMSXml
     */
    public static Communicator createComm(CommProtocol protocol, Socket socket, CommLink commlink) {
        if (protocol.isMnDot()) {
            return new CommMnDot(socket, commlink);
        } else if (protocol.isDMSLite()) {
            return new CommDMSXml(socket, commlink);
        } else {
            try {
                socket.close();
            } catch (IOException ex) {
                SFIMExceptionHandler.handle(ex);
            } finally {
                return null;
            }
        }
    }

    @Override
    /**
     * MnDot server thread
     */
    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (Exception ex) {
            terminate();
            return;
        }

        while (!isInterrupted()) {
            try {
                //socket.setSoTimeout(60 * 1000);
                // buffer for request from IRIS
                buffer = new byte[4096];
                is.read(buffer);
                doResponse(Arrays.copyOfRange(buffer, 0, buffer.length), os);
                if(this.socket.isClosed()) return;
            } catch (Exception ex) {
//                ex.printStackTrace();
                terminate();
                return;
            }
        }        
    }

    /**
     * Stop this thread
     */
    public void terminate() {
        try {
            socket.close();
        } catch (IOException ex) {}
    }

    /**
     * @return CommProtocol of this
     */
    public CommProtocol getProtocol() {
        return protocol;
    }

    /**
     * @param protocol CommProtocol of this
     */
    public void setProtocol(CommProtocol protocol) {
        this.protocol = protocol;
    }

    abstract protected void doResponse(byte[] buffer, OutputStream os);

    public CommLink getCommLink() {
        return this.commlink;
    }
    
}
