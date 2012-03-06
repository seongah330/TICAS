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
import edu.umn.natsrl.sfim.comm.ResponserType;
import java.io.OutputStream;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class RespGetStatus extends DMSResponser {
    private long id;
    
    public RespGetStatus(long id, byte[] buffer, OutputStream os) {
        super(buffer, os);
        this.id = id;        
        this.type = ResponserType.DMS_GET_STATUS;
    }

    
    protected String getResponseXML(SimDMS dms)
    {
        DXMessage message = new DXMessage(DXElement.RSP_GET_STATUS);        
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

}
