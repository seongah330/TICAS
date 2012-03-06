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

package edu.umn.natsrl.sfim.comm.dmsxml;

import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.comm.CommLink;
import edu.umn.natsrl.sfim.comm.Communicator;
import edu.umn.natsrl.sfim.comm.Responser;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Communication protocol class for DMS control
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class CommDMSXml extends Communicator {


    /**
     * constructor
     * @param socket
     */
    public CommDMSXml(Socket socket, CommLink commlink) {
        super(socket, commlink);
        //System.out.println(commlink.getLinkName() + " (DMSXml) is created...");
    }

    @Override
    protected void doResponse(byte[] buffer, OutputStream os) {        
        String xml = new String(buffer);
        try {                                  
            
            // get request type
            String reqType = DXHelper.getMsgType(xml);
            
            if(reqType == null) {
                //System.out.println("CommDMSXml>"+this.getCommLink().getLinkName() + " : no data");
                this.interrupt();
                return;
            }
            
            // get message id
            long id = DXHelper.getLong(xml, DXElement.M_ID);
            
//            System.out.println(reqType+" : " + xml);
            Responser rsp = null;
            
            // dispatch responser
            if(reqType.equals(DXElement.REQ_GET_CONFIG.tag)) rsp = new RespGetConfig(id, buffer, os);
            else if(reqType.equals(DXElement.REQ_GET_STATUS.tag)) rsp = new RespGetStatus(id, buffer, os);
            else if(reqType.equals(DXElement.REQ_SET_MSG.tag)) rsp = new RespSetMsg(id, buffer, os);
            else {
                System.out.println("Unknown Request : " + reqType + "\n---> " + xml);
                return;
            }
            
            manager.signalResponse(rsp.getResponseType());
            
//            if(rsp.getResponseType().isGET()) manager.signalGetRequest(this, rsp.getName());
            
            // get controller by pin number
            // but dms comm_link has just one controller
            ControllerDMSLite ctrl = (ControllerDMSLite)this.controllers.get(SFIMConfig.DMS_PIN_RANGE_START);                        
            if(ctrl == null || rsp == null) return;
            
            // doResponse
            ctrl.doResponse(rsp);
            
        } catch (Exception ex) {
            //SFIMExceptionHandler.handle(ex);
//            System.out.println("Exception from DMSXml.doResponse");
//            System.out.println(xml);
            ex.printStackTrace();
        }
    }    
}
