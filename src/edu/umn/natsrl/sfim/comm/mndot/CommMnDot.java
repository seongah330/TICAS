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
import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.comm.CommLink;
import edu.umn.natsrl.sfim.comm.Communicator;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * Communication protocol class for metering and sending detector data
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class CommMnDot extends Communicator {

    private HashMap<String, Class> respMap = new HashMap<String, Class>();

    /**
     * respond according to cat of data packet
     * @param buffer Received packet buffer
     * @param os OutputStream
     */
    @Override
    protected void doResponse(byte[] buffer, OutputStream os) {
        try {
            // parse information from packet
            int drop = parser.getDrop(buffer);
            int cat = parser.getCat(buffer);
            int addr = parser.getMemoryAddress(buffer);

            // get controller according to drop
            Controller170 ctrl = (Controller170) this.controllers.get(drop);
            
//            if(addr == 834)
//                System.err.println("Response ADDR 843 :  addr="+addr+", drop="+drop+", cat="+cat);
//            else
//                System.err.println("Response ADDR :  addr="+addr+", drop="+drop+", cat="+cat);
            if (ctrl == null) {
                System.out.println(" => No corresponding controller to response : addr="+addr+", drop="+drop+", cat="+cat);
                return;
            }

            // get responser according to cat and addr
            Class klass = respMap.get(getRespKey(cat, addr));
            if (klass == null) {
//                System.err.println("Unknown Request : addr="+addr+", drop="+drop+", cat="+cat);
                return;
            }
            
            // make responser instance and set drop, cat, addr, buffer and output stream
            MnDotResponser rsp = (MnDotResponser) klass.newInstance();
            rsp.set(drop, cat, addr, buffer, os);
                        
            // call doResponse
            ctrl.doResponse(rsp);

            manager.signalResponse(rsp.getResponseType());            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * constructor
     * @param socket Client socket to communicate with IRIS server
     */
    public CommMnDot(Socket socket, CommLink commlink) {
        super(socket, commlink);
        
        // regist response classes to map        
        // read responser
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_DATA_BUFFER_30_SECOND), Read30sData.class);
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_STATUS), ReadMeterStatus.class);
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2)), ReadRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE), ReadRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2)), ReadRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_READ_MEMORY, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE), ReadRedTime.class);

        // write responser
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_REMOTE_RATE), WriteMeterRate.class);
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_RAMP_METER_DATA + SFIMConfig.OFF_MEM_REMOTE_RATE + SFIMConfig.OFF_MEM_METER_2), WriteMeterRate.class);
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2)), WriteMeterRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_METER_1_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE), WriteMeterRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2)), WriteMeterRedTime.class);
        respMap.put(getRespKey(SFIMConfig.CAT_WRITE_MEMORY, SFIMConfig.ADDR_METER_2_TIMING_TABLE + SFIMConfig.ADDR_OFF_RED_TIME + (SimConfig.METER_RATE_CENTRAL * 2) + SFIMConfig.ADDR_OFF_PM_TIMING_TABLE), WriteMeterRedTime.class);
    }
    
    /**
     * Return key for response map
     * @param cat
     * @param addr
     * @return key
     */
    protected final String getRespKey(int cat, int addr) {
        return String.format("%d-%d", cat, addr);
    }
}
