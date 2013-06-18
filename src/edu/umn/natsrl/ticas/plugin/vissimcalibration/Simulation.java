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
package edu.umn.natsrl.ticas.plugin.vissimcalibration;

import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.ITravelTimeListener;
import edu.umn.natsrl.vissimcom.VISSIMController;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Chongmyung Park
 */
public class Simulation extends Thread implements ITravelTimeListener {
    private String casefile;
    private int seed;
    private int samples = 0;
    private VISSIMController vc =new VISSIMController();
    private ISimEndSignal signalListener;
    private HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
    
    public Simulation(String casefile, int seed) {
        this.casefile = casefile;
        this.seed = seed;
    }
    
    public void run() {
        vc.initialize(casefile, seed,VISSIMVersion.VISSIM540x64,SimulationConfig.RunningInterval);
        this.signalListener.signalEnd(0);
        //vc.addTrafficListener(this);   
        
        int totalExecutionStep = vc.getTotalExecutionStep();
        samples = 0;
        while(true) {
            int steps = vc.run(30);
            samples++;
            if(vc.getSimStep() >= totalExecutionStep) {
                break;
            }
        }
        writeTTLog();
        this.signalListener.signalEnd(1);
    }


    public int getSamples() {
        return samples;
    }

    void setVissimVisible(boolean b) {
        try {
            vc.setVisible(b);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void setSignalListener(ISimEndSignal signalListener) {
        this.signalListener = signalListener;
    }

    @Override
    public void readTravelTime(String[] travelTimeIds, double[] travelTimes) {
        for(int i=0; i<travelTimeIds.length; i++) {
            String ttid = travelTimeIds[i];
            double tt = travelTimes[i];

            ArrayList<Double> ttData = this.travelTimes.get(ttid);
            if(ttData == null) {
                ttData = new ArrayList<Double>();
                this.travelTimes.put(ttid, ttData);
            }
            ttData.add(tt);
        }
    }
    
    private void writeTTLog() {
        try {           
            WritableWorkbook workbook = Workbook.createWorkbook(new File(FileHelper.getNumberedFileName("TT_LOG.xls")));
            WritableSheet sheet = workbook.createSheet("tt", 0);
            Iterator<String> itr = this.travelTimes.keySet().iterator();
            int col = 0;
            while(itr.hasNext()) {
                String key = itr.next();
                sheet.addCell(new Label(col, 0, key));
                ArrayList<Double> data = this.travelTimes.get(key);
                int row = 1;
                for(Double d : data) {
                    sheet.addCell(new Number(col, row++, d));
                }
                col++;
            }                        
            workbook.write();
            workbook.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
}
