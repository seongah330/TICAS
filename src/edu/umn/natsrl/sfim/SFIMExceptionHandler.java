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

import java.net.BindException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class SFIMExceptionHandler {

    public SFIMExceptionHandler() {}

    public static void handle(Exception ex)
    {
        ex.printStackTrace();
        
        if(ex instanceof BindException) return;
        else if(ex instanceof SocketTimeoutException) return;
        else if (ex instanceof ConnectException) {
            JOptionPane.showMessageDialog(null, "Disconnected from IRIS server.\nIt will be terminated!!");
            System.exit(1);
        }
        
//        /// TODO : Exception Handling -> Show message with dialog
        ex.printStackTrace();
//        JOptionPane.showMessageDialog(null, ex.toString());
        //System.out.println(ex.getMessage());
    }

    public static void handle(String msg)
    {
        /// TODO : Exception Handling -> Show message with dialog
        System.out.println(msg);
    }
}
