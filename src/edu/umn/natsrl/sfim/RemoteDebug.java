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

package edu.umn.natsrl.sfim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Chongmyung Park
 */
public class RemoteDebug extends Thread {

    private int port;
    private ServerSocket serverSocket;
    private DebugServer comm;
    private OutputStream sos;
    private PrintStream ps;

    public RemoteDebug(OutputStream sos, int port) {
        this.sos = sos;
        this.port = port;
    }

    public void setPritStream(PrintStream ps) {
        this.ps = ps;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("IRIS is connected to Debug Server (" + port + ")");
                try {
                    this.comm.socket.close();
                } catch (Exception ex) {
                }
                this.comm = new DebugServer(socket);
                this.comm.start();
            }
        } catch (Exception ex) {
            try {
                if(serverSocket != null) serverSocket.close();
                this.ps = null;
                this.sos = null;
                //System.out.println("ERROR : Debug Server");
            } catch (IOException ex1) {
            }
        }
    }

    public void flush() {
        comm.flush();
    }

    void close() {
        try {
            this.serverSocket.close();
            this.comm.socket.close();
            ps = null;
            this.sos = null;
        } catch (Exception ex) {}
    }

    /**
     * server thread for one connection
     */
    class DebugServer extends Thread {

        private Socket socket;

        public DebugServer(Socket socket) {
            this.socket = socket;
        }

        synchronized public void run() {
            InputStream is = null;
            try {
                is = socket.getInputStream();
            } catch (Exception ex) {
            }

            while (true) {
                try {
                    if (ps != null) {
                        ps.write(is.read());
                    } else {
                        if(sos != null) sos.write(is.read());
                    }
                } catch (IOException ex) {
                }
            }
        }

        private void flush() {
            try {
                if (ps != null) ps.flush();
                sos.flush();
            } catch (IOException ex) {}
        }
    }
}