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

/**
 *
 * @author Chongmyung Park
 */
public class WriteMeterRedTime extends MnDotResponser {

    public WriteMeterRedTime() {
        this.type = ResponserType.MNDOT_SET_METER_REDTIME;
    }

    @Override
    public void response() {

        int pin = getPin();

        setRedTime(pin);

        try {
            byte[] message;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(3);
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(helper.get170ControllerDropCat(this.dataBuffer));
            dos.writeByte(0);
            dos.writeByte(0);//temporary data for checksum

            message = bos.toByteArray();
            message[message.length - 1] = helper.checkSum(message);
            os.write(message);
            os.flush();

            if (printPacket) {
                if (pin == SFIMConfig.METER_1_PIN) {
                    helper.printResPacket(message, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2));
                }
                if (pin == SFIMConfig.METER_2_PIN) {
                    helper.printResPacket(message, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2));
                }
            }

        } catch (Exception e) {
            SFIMExceptionHandler.handle(e);
        }

    }

    /**
     * set red time to meter
     * @param pin
     * @param buffer
     */
    private void setRedTime(int pin) {

        Controller170 c = (Controller170) this.ctrl;
        SimMeter[] meters = c.getMeters();


//                int redTime = helper.get16BitfromBCD(this.dataBuffer[SFIMConfig.OFF_PAYLOAD + 2], this.dataBuffer[SFIMConfig.OFF_PAYLOAD + 3]);
        SimMeter meter = meters[pin - SFIMConfig.RAMPMETER_PIN_RANGE];

        //byte[] payload = new byte[]{dataBuffer[SFIMConfig.OFF_SET_REQUEST_PAYLOAD], dataBuffer[SFIMConfig.OFF_SET_REQUEST_PAYLOAD+1]};
        //int redTime = helper.decodeBCD(payload);
        int redTime = helper.decodeBCD(dataBuffer, SFIMConfig.OFF_SET_REQUEST_PAYLOAD, SFIMConfig.OFF_SET_REQUEST_PAYLOAD+1);

        if (meter == null || redTime < 0) {
            if (meter == null) {
                System.out.println("  -  !! meter == null");
            } else if (redTime < 0) {
                System.out.println("  - !! meter = " + meter.getId() + ", redTime = " + redTime);
            }
            if (meter == null && redTime < 0) {
                System.out.println("  - !! redTime < 0");
            }
            return;
        }
        float realRedTime = (float) redTime / 10;

        // add log
        //meterLog.println(ctime + "," + meter.getId() + "," + realRedTime);

        //            System.out.println("  Set redtime for " + meter.getId() + " : " + realRedTime);
        //            System.out.println("[Write.setRedTime("+meter.getId()+")] : " + redTime + " (calculated : " + ( (3600 / redTime) - 2 ) + ")");
        //System.out.println("  - get set-red-time msg : " + meter.getId() + ", " + String.format("%.2f", realRedTime));

//                helper.printReqPacket(dataBuffer);
        if (realRedTime > 0) {
            if(meter.status != SimConfig.METER_STATUS_CENTRAL) {          
                Logger meterStartStopLog = manager.getMeterLog();                
                meterStartStopLog.println(manager.getSamples()+","+ meter.getId() + ",start");
                System.out.println("Meter Started --------- : " + meter.getId());
            }            
            meter.setRedTime(realRedTime);            
        }


    }

    private int getPin() {

        int pin = SFIMConfig.METER_1_PIN;

        if (this.addr == SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2)
                || this.addr == SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE) {
            pin = SFIMConfig.METER_2_PIN;
        }

        return pin;
    }
}
