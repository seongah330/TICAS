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

/**
 * DMS XML Message Class
 * @author Chongmyung Park
 */
public class DXMessage {
    StringBuilder message;
    DXElement type;

    public DXMessage(DXElement type) {
        this.type = type;
        message = new StringBuilder("<"+DXElement.DMSXML+"><"+type+">");        
    }
    
    /**
     * get xml string to be used by response
     */
    public String getMessage()
    {
        message.append("</"+type+"></"+DXElement.DMSXML+">");
        return message.toString();
    }
    
    /**
     * put xml element to message
     */
    public void put(DXElement tag, Object value) {
        message.append("<"+tag+">"+value.toString()+"</"+tag+">");
    }       
}
