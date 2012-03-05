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
package edu.umn.natsrl.sfim;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Properties;

/**
 *
 * @author Chongmyung Park
 */
public class SFIMSyncServer extends Thread {

    private Calendar simTime;
    private String corridorName;
    private String stationStart;
    private String stationEnd;
    private String meterStart;
    private String meterEnd;
    private String corridorDir;

    public enum CTYPE {

        PHASE0((byte) 99),
        PHASE1((byte) 1), PHASE2((byte) 2);
        public byte id;

        private CTYPE(byte id) {
            this.id = id;
        }
    };
    private int port = SFIMConfig.TIMESYNC_PORT;
    private ServerSocket serverSocket;
    private TimeCommServer comm;

    public SFIMSyncServer() {
    }

    void initialize(Section section, Calendar simTime) {
        this.corridorName = section.getRNodes().get(0).getCorridor().getRoute();
        this.corridorDir = section.getRNodes().get(0).getCorridor().getDirection().toString();
        Station[] stations = section.getStations();
        this.stationStart = stations[0].getStationId();
        this.stationEnd = stations[stations.length - 1].getStationId();
        SectionHelper sectionHelper = new SectionHelper(section);
        for (EntranceState es : sectionHelper.getEntranceStates()) {
            RampMeter meter = es.getMeter();
            if (meter != null) {
                this.meterEnd = meter.getId();
                if (this.meterStart == null) {
                    this.meterStart = meter.getId();
                }
            }
        }

        this.simTime = simTime;
    }

    void sendPhase1Time() {
        if (this.comm != null) {
            this.comm.sendTimestamp(CTYPE.PHASE1, System.currentTimeMillis());
        }
    }

    void sendPhase2Time() {
        if (this.comm != null) {
            this.comm.sendTimestamp(CTYPE.PHASE2, System.currentTimeMillis());
        }
    }

    public void run() {
        try {
            System.out.println("Time Server started...");
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("IRIS is connected to TimeSync Server (" + port + ")");
                try {
                    this.comm.socket.close();
                } catch (Exception ex) {
                }
                this.comm = new TimeCommServer(socket);
                this.comm.start();
            }
        } catch (Exception ex) {
            System.out.println("ERROR : Simulation Time Server (in TimeComm2)");
        }
    }

    public void close() {
        try {
            serverSocket.close();
            this.comm.socket.close();
        } catch (IOException ex) {
        }
    }

    /**
     * server thread for one connection
     */
    class TimeCommServer extends Thread {

        private Socket socket;
        private InputStream is;
        private OutputStream os;

        public TimeCommServer(Socket socket) throws IOException {
            this.socket = socket;
            is = socket.getInputStream();
            os = socket.getOutputStream();
            sendSettings();
        }

        private void sendSettings() throws IOException {
            System.out.println("TimeSyncServer - initial time=" + simTime.getTime().toString());
            Properties prop = new Properties();
            prop.put("Initial Time", String.format("%d", simTime.getTimeInMillis()));
            prop.put("Corridor Name", corridorName);
            prop.put("Corridor Direction", corridorDir);
            prop.put("Start Station Name", stationStart);
            prop.put("End Station Name", stationEnd);
            prop.put("Start Meter Name", meterStart);
            prop.put("End Meter Name", meterEnd);
            prop.put("Print Station Info", SFIMConfig.DEBUG_IRIS_STATION.toString());
            prop.put("Print Meter Info", SFIMConfig.DEBUG_IRIS_METER.toString());
            prop.put("Remote Out Debug Port", String.format("%d", SFIMConfig.REMOTE_OUT_PORT));
            prop.put("Remote Err Debug Port", String.format("%d", SFIMConfig.REMOTE_ERR_PORT));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            prop.storeToXML(byteArrayOutputStream, "SFIM Setting");
            ByteBuffer bf = ByteBuffer.allocate(byteArrayOutputStream.size()+1);
            bf.put(CTYPE.PHASE0.id);
            bf.put(byteArrayOutputStream.toByteArray());
            write(bf.array());
            
//            // DEBUG
//            Properties dp = new Properties();
//            bf.get();
//            byte[] data = new byte[bf.remaining()];
//            bf.get(data);
//            String settingXML = new String(data);
//            System.out.println("=== Settings ===");
//            System.out.println(settingXML);            
        }

        public void sendTimestamp(CTYPE phase, long timestamp) {
            ByteBuffer buf = ByteBuffer.allocate(9);
            buf.put(phase.id);
            buf.putLong(timestamp);
            write(buf.array());
        }

        private void write(byte[] data) {
            try {
                os.write(data);
                os.flush();
            } catch (Exception ex) {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        synchronized public void run() {
            InputStream is = null;
            byte[] buffer = new byte[1024];

            try {
                is = socket.getInputStream();
                while (!isInterrupted()) {
                    is.read(buffer);
                    ByteBuffer buf = ByteBuffer.wrap(buffer);
                    byte code = buf.get();
                    if (code == 3) {
                        SFIMManager.getInstance().algorithmDoneInIRIS();
                    }
                }

            } catch (Exception ex) {
            }
        }
    }
}
