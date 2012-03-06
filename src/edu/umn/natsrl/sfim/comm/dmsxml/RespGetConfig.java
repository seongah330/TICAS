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
public class RespGetConfig extends DMSResponser {
    private long id;
    
    public RespGetConfig(long id, byte[] buffer, OutputStream os) {
        super(buffer, os);
        this.id = id;     
        this.type = ResponserType.DMS_GET_CONFIG;
    }
    
    protected String getResponseXML(SimDMS dms)
    {
        DXMessage message = new DXMessage(DXElement.RSP_GET_CONFIG);
        message.put(DXElement.M_ID, this.id);
        message.put(DXElement.M_IS_VALID, dms.isValid);
        message.put(DXElement.M_ERR_MSG, dms.ErrMsg);
        message.put(DXElement.M_SIGN_ACCESS, dms.signAccess);
        message.put(DXElement.M_MODEL, dms.model);
        message.put(DXElement.M_MAKE, dms.make);
        message.put(DXElement.M_VERSION, dms.version);
        message.put(DXElement.M_TYPE, dms.dmsType);
        message.put(DXElement.M_H_BORDER, dms.horizBorder);
        message.put(DXElement.M_V_BORDER, dms.vertBorder);
        message.put(DXElement.M_H_PITCH, dms.horizPitch);
        message.put(DXElement.M_V_PITCH, dms.vertPitch);
        message.put(DXElement.M_SIGN_HEIGHT, dms.signHeight);
        message.put(DXElement.M_SIGN_WIDTH, dms.signWidth);
        message.put(DXElement.M_CHAR_HEIGHT_PIXEL, dms.characterHeightPixels);
        message.put(DXElement.M_CHAR_WIDTH_PIXEL, dms.characterWidthPixels);
        message.put(DXElement.M_SIGN_HEIGHT_PIXEL, dms.signHeightPixels);
        message.put(DXElement.M_SIGN_WIDTH_PIXEL, dms.signWidthPixels);
        return message.getMessage();
    }


}
