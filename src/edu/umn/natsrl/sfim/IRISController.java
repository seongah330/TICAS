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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class IRISController {
    
    static Socket socket;
    static InetAddress addr;
    
    /**
     * start IRIS server
     */
    public static void startIRIS()
    {
        try {
            InetAddress thisIp = InetAddress.getLocalHost();
            String ip = thisIp.getHostAddress();        
            Properties prop = new Properties();
            prop.put("Command", "123");
            prop.put("Simulation Host Address", ip);
            prop.put("Simulation Host Port", String.format("%d", SFIMConfig.TIMESYNC_PORT));
            write(prop);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * stop IRIS server
     */
    public static void stopIRIS()
    {
        try {
            Properties prop = new Properties();
            prop.put("Command", "456");
            write(prop);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static void write(Properties prop)
    {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            prop.storeToXML(byteArrayOutputStream, "SFIM Server Setting");
            ByteBuffer bf = ByteBuffer.allocate(byteArrayOutputStream.size()+1);
            bf.put(byteArrayOutputStream.toByteArray());            
            write(bf.array());
        } catch (IOException ex) {
            ex.printStackTrace();            
        }
    }
    
    private static void write(byte[] data) {
        try {

            int port = SFIMConfig.IRIS_CONTROLLER_PORT;            
            addr = InetAddress.getByName(SFIMConfig.IRIS_SERVER_ADDR);
            SocketAddress sockaddr = new InetSocketAddress(addr, port);
            
            socket = new Socket();
            socket.connect(sockaddr, 10*1000);
            
            OutputStream os = socket.getOutputStream();
            os.write(data);            
            os.flush();            
            
        } catch (Exception ex) {
            //ex.printStackTrace();
        }           
    }
}
