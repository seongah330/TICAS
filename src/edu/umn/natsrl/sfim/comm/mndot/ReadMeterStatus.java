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

package edu.umn.natsrl.sfim.comm.mndot;

import edu.umn.natsrl.infra.simobjects.SimConfig;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.ResponserType;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class ReadMeterStatus  extends MnDotResponser {
    
    public ReadMeterStatus() {
        this.type = ResponserType.MNDOT_GET_METER_STATUS;
    }
    
    @Override
    public void response() {
        Controller170 c = (Controller170)this.ctrl;
        SimMeter[] meters = c.getMeters();        
        int messageLength = dataBuffer[SFIMConfig.OFF_GET_REQUEST_PAYLOAD_LENGTH];

        byte[] message;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(messageLength+3);
        DataOutputStream dos = new DataOutputStream(bos);

        SimMeter meter1 = meters[0];
        SimMeter meter2 = meters[1];
        
        SimMeter dummy = new SimMeter();
        dummy.remoteRate = SimConfig.METER_RATE_FLASH;
        dummy.currentRate = SimConfig.METER_RATE_FLASH;
        dummy.status = SimConfig.METER_STATUS_FLASH;
        dummy.setId("dummy");
        if (meter1 == null || !meter1.isEnabled()) {
            meter1 = dummy;
        }
        if (meter2 == null || !meter2.isEnabled()) {
            meter2 = dummy;
        }

        try {
            dos.writeByte(helper.get170ControllerDropCat(this.dataBuffer));
            dos.writeByte(messageLength);

            //ramp1 data
            dos.writeByte(meter1.status); //ramp meter status
            dos.writeByte(meter1.currentRate); //current metering rate
            dos.writeByte(meter1.greenCount30Sec); //green count for 30sec
            dos.writeByte(meter1.remoteRate); //remote rate (from center)
            dos.writeByte(meter1.policePanel); //police panel
            dos.writeByte(meter1.greenCount5Min); //green count for 5min

            //ramp2 data
            dos.writeByte(meter2.status); //ramp meter status
            dos.writeByte(meter2.currentRate); //current metering rate
            dos.writeByte(meter2.greenCount30Sec); //green count for 30sec
            dos.writeByte(meter2.remoteRate); //remote rate (from center)
            dos.writeByte(meter2.policePanel); //police panel
            dos.writeByte(meter2.greenCount5Min); //green count for 5min
            
            //check sum
            dos.writeByte(0); //temporary data for checksum
            message = bos.toByteArray();
            message[message.length - 1] = helper.checkSum(message);

            os.write(message);
            os.flush();

            if(meter1 != null) meter1.updateStatusCount();
            if(meter2 != null) meter2.updateStatusCount();
            
//            System.out.println("   => send meter status to IRIS : " + meter1.getId() + "("+meter1.status+")" +", " + meter2.getId()+ "("+meter2.status+")");
            if(printPacket) {                        
                System.out.print("["+this.type+"] ");
                helper.printResPacket(message, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_STATUS);
            }

        } catch (IOException e) {
            SFIMExceptionHandler.handle(e);
        }
    }

}
