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
import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.Responser;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Chongmyung Park
 */
public abstract class DMSResponser extends Responser {

    public DMSResponser(byte[] buffer, OutputStream os) {
        super(buffer, os);
    }
    
    @Override
    public void doResponse() {
        ControllerDMSLite c = (ControllerDMSLite)this.ctrl;
        SimDMS dms = c.getDMS();
        if(dms == null) {
            SFIMExceptionHandler.handle("["+this.ctrl.getName()+"] NO DMS");
            return;
        }
        sendData(getResponseXML(dms));
                
        if(SFIMConfig.PRINT_PACKET) System.out.println("["+this.type+"] "+ getResponseXML(dms));
    }
    
    /**
     * write xml data to output stream of socket
     */
    protected void sendData(String xml)
    {
        if(xml == null || xml.isEmpty()) return;
        try {
            os.write(xml.getBytes());
            os.flush();
        } catch (IOException ex) {
            SFIMExceptionHandler.handle(ex);
        }
    }
    
    protected abstract String getResponseXML(SimDMS dms);

}
