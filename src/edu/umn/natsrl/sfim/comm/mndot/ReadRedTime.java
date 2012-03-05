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
import java.nio.ByteBuffer;

/**
 *
 * @author Chongmyung Park
 */
public class ReadRedTime  extends MnDotResponser {

    public ReadRedTime() {
        this.type = ResponserType.MNDOT_GET_METER_RATE;
    }

    @Override
    public void response() {

        int pin = getPin();
        
        Controller170 c = (Controller170)this.ctrl;
        SimMeter[] meters = c.getMeters();        
        
        int msLength = (0 | dataBuffer[4]) + 3;

        byte[] message;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(msLength);
        DataOutputStream dos = new DataOutputStream(bos);

        SimMeter meter = meters[pin-SFIMConfig.RAMPMETER_PIN_RANGE_START];
        if(meter == null || !meter.isEnabled()) {
            return;
        }
        try {
            dos.writeByte(helper.get170ControllerDropCat(this.dataBuffer));
            dos.writeByte(msLength - 3);

            
            short redTime = (short)(meter.getRedTime() * 10);            
            dos.write(ByteBuffer.allocate(2).putShort(redTime).array());

            dos.writeByte(0); //temporary data for checksum
            message = bos.toByteArray();
            message[message.length - 1] = helper.checkSum(message);

            os.write(message);
            os.flush();
//            System.out.println("   => send red time to IRIS : " + meter.getId() +", " + redTime);
            if(printPacket) {            
                System.out.print("["+this.type+"] redtime="+redTime +" : ");
                if(pin == SFIMConfig.METER_1_PIN) helper.printResPacket(message, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2));
                if(pin == SFIMConfig.METER_2_PIN) helper.printResPacket(message, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2));
            }

        } catch (Exception e) {
            SFIMExceptionHandler.handle(e);
        }

    }

    /**
     * Return pin according to address
     * @return 
     */
    private int getPin()
    {
        int pin = SFIMConfig.METER_1_PIN;
        
        if(this.addr == SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) ||
           this.addr == SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE) {
            pin = SFIMConfig.METER_2_PIN;
        }
        
        return pin;
    }
    
}
