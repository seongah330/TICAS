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

import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.ResponserType;
import edu.umn.natsrl.vissimcom.VSA;
import java.io.OutputStream;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park
 */
public class RespSetMsg extends DMSResponser {

    long id;
    
    public RespSetMsg(long id, byte[] buffer, OutputStream os) {
        super(buffer, os);
        this.id = id;
        this.type = ResponserType.DMS_SET_MSG;
    }

    @Override
    public void doResponse() {
        try {
            
            ControllerDMSLite c = (ControllerDMSLite)this.ctrl;
            SimDMS dms = c.getDMS();
            if(dms == null) {
                SFIMExceptionHandler.handle("["+this.ctrl.getName()+"] NO DMS");
                return;
            }            
            
            String xml = new String(this.dataBuffer);        
            
            // retrieve value from xml string and set to dms
            int vsa = DXHelper.getVSA(xml, DXElement.M_MSG_TXT);            
            if(vsa > 0) {
                manager.getSimulationControl().setVSA(dms.getId(), VSA.getVSA(vsa));
                dms.updateVSA(vsa);
            }
            
            //manager.signalResponse(type);
//            System.out.println("  - Set VSA for " + dms.getId() + " : " + vsa);
            
            dms.MsgText = DXHelper.getString(xml, DXElement.M_MSG_TXT);
            dms.Bitmap = DXHelper.getString(xml, DXElement.M_BITMAP);
            dms.ActPriority = DXHelper.getInt(xml, DXElement.M_ACT_PRIORITY);
            dms.RunPriority = DXHelper.getInt(xml, DXElement.M_RUN_PRIORITY);
            dms.UseOnTime = DXHelper.getBoolean(xml, DXElement.M_USE_ON_TIME);
            dms.OnTime = DXHelper.getString(xml, DXElement.M_ON_TIME);
            dms.UseOffTime = DXHelper.getBoolean(xml, DXElement.M_USE_OFF_TIME);
            dms.OffTime = DXHelper.getString(xml, DXElement.M_OFF_TIME);
            dms.DisplayTimeMS = DXHelper.getInt(xml, DXElement.M_DISPLAY_TIME);

            sendData(getResponseXML(dms));
//            System.out.println("["+this.type+"] "+ getResponseXML(dms));            
            
        } catch (Exception ex) {
            SFIMExceptionHandler.handle(ex);
        }
        
    }

    @Override
    protected String getResponseXML(SimDMS dms) {
        DXMessage message = new DXMessage(DXElement.RSP_SET_MSG);        
        message.put(DXElement.M_ID, this.id);  // long
        message.put(DXElement.M_IS_VALID, dms.isValid);   // boolean
        message.put(DXElement.M_ERR_MSG, dms.ErrMsg);  // string
        message.put(DXElement.M_MSG_AVAILABLE, dms.MsgTextAvailable);  // boolean
        message.put(DXElement.M_MSG_TXT, dms.MsgText); // string
        message.put(DXElement.M_ACT_PRIORITY, dms.ActPriority);   // int (4 => VSL)
        message.put(DXElement.M_RUN_PRIORITY, dms.RunPriority);  // int (4 => VSL)
        message.put(DXElement.M_OWNER, dms.Owner);  // string
        message.put(DXElement.M_USE_ON_TIME, dms.UseOnTime);    // boolean
        message.put(DXElement.M_ON_TIME, dms.OnTime);   // date (yyyy-MM-dd'T'HH:mm:ss'Z')
        message.put(DXElement.M_USE_OFF_TIME, dms.UseOffTime);  // boolean
        message.put(DXElement.M_OFF_TIME, dms.OffTime); // date (2011-01-11T10:10:10Z)
        message.put(DXElement.M_DISPLAY_TIME, dms.DisplayTimeMS);  // int
        message.put(DXElement.M_USE_BITMAP, dms.UseBitmap);  // boolean
        message.put(DXElement.M_BITMAP, dms.Bitmap);    // string
        return message.getMessage();
    }
    
    private String getString(Element ele, String tag)
    {
        return ele.getElementsByTagName(tag).item(0).getTextContent();
    }

    private long getLong(Element ele, String tag) {
        return Long.parseLong(getString(ele, tag));
    }


}
