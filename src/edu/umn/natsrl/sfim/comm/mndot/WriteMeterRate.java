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
import edu.umn.natsrl.util.Logger;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class WriteMeterRate extends MnDotResponser {

    public WriteMeterRate() {
        this.type = ResponserType.MNDOT_SET_METER_RATE;
    }

    @Override
    public void response() {

        int pin = getPin();

        setRate(pin);

        try {
            byte[] message;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(3);
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(helper.get170ControllerDropCat(this.dataBuffer));
            dos.writeByte(0);
            dos.writeByte(0); //temporary data for checksum
            message = bos.toByteArray();
            message[message.length - 1] = helper.checkSum(message);
            os.write(message);
            os.flush();
            
            if(printPacket) {
                if (pin == SFIMConfig.METER_1_PIN) {
                    helper.printResPacket(message, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_REMOTE_RATE);
                }
                if (pin == SFIMConfig.METER_2_PIN) {
                    helper.printResPacket(message, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_REMOTE_RATE + SFIMConfig.OFF_MEM_METER_2);
                }
            }
        } catch (IOException ex) {
            SFIMExceptionHandler.handle(ex);
        }
    }

    /**
     * set rate to meter
     * @param pin
     * @param buffer
     */
    private void setRate(int pin) {

        Controller170 c = (Controller170) this.ctrl;
        SimMeter[] meters = c.getMeters();

        try {
            //byte rate = (byte) helper.get8BitfromBCD(this.dataBuffer[4]);
            byte rate = (byte)helper.decodeBCD(this.dataBuffer[SFIMConfig.OFF_SET_REQUEST_PAYLOAD]);
            SimMeter meter = meters[pin - SFIMConfig.RAMPMETER_PIN_RANGE];
            if (meter == null) {
                return;
            }
            if(rate != meter.currentRate) {
                
                System.out.println("  - Set meter rate for " + meter.getId()+" : "+this.type+" -> " + rate);
           
                Logger meterStartStopLog = manager.getMeterLog();                
                if(rate == SimConfig.METER_RATE_FLASH || rate == SimConfig.METER_RATE_FORCED_FLASH) {
                    meterStartStopLog.println(manager.getSamples()+","+ meter.getId() + ",stop");
                }
                else {
                    meterStartStopLog.println(manager.getSamples()+","+ meter.getId() + ",start");
                }
            }
            
            meter.setRate(rate);

        } catch (Exception e) {
            SFIMExceptionHandler.handle(e);
        }
    }

    private int getPin() {
        int pin = SFIMConfig.METER_1_PIN;

        if (this.addr == SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_REMOTE_RATE + SFIMConfig.OFF_MEM_METER_2) {
            pin = SFIMConfig.METER_2_PIN;
        }

        return pin;
    }
}
