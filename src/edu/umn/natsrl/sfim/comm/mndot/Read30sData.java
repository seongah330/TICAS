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

import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.ResponserType;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Chongmyung Park
 */
public class Read30sData  extends MnDotResponser {

    public Read30sData() {
        this.type = ResponserType.MNDOT_GET_30S_DATA;
    }
    
    @Override
    public void response() {
        
        Controller170 c = (Controller170)this.ctrl;
        SimDetector[] detectors = c.getDetectors();
        int messageLength = dataBuffer[SFIMConfig.OFF_GET_REQUEST_PAYLOAD_LENGTH];

        byte[] message;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(messageLength+3);
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(helper.get170ControllerDropCat(dataBuffer));
            dos.writeByte(messageLength);
            byte volume = 0;
            short scandata = 0;
            
            for (int i = 0; i < detectors.length; i++) {
                SimDetector d = detectors[i];
                if (d == null) {
                    volume = 0;
                } else {
                    volume = (byte)d.getRecentVolume();
                }
                dos.writeByte(volume); //volume data
            }
            
            for (int i = 0; i < detectors.length; i++) {
                SimDetector d = detectors[i];
                if (d == null) {
                    scandata = 0;
                } else {
                    scandata = (short)d.getRecentScan();
                }
                byte[] scan = ByteBuffer.allocate(2).putShort(scandata).array();
                dos.write(scan); //scan data
            }
            dos.writeByte(0); //temporary data for checksum
            message = bos.toByteArray();
            message[message.length - 1] = helper.checkSum(message);

            os.write(message);
            os.flush();
            
        } catch (IOException e) {
            SFIMExceptionHandler.handle(e);
        }
    }

}
