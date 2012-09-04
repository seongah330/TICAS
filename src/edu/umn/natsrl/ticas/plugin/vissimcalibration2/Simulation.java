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

/*
 * Simulation.java
 *
 * Created on Jun 8, 2011, 9:55:26 AM
 */
package edu.umn.natsrl.ticas.plugin.vissimcalibration2;

import com.inzoom.comjni.ComJniException;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.IStepListener;
import edu.umn.natsrl.vissimcom.ITravelTimeListener;
import edu.umn.natsrl.vissimcom.VISSIMController;
//import edu.umn.natsrl.vissimctrl.IStepListener;
//import edu.umn.natsrl.vissimctrl.ITravelTimeListener;
//import edu.umn.natsrl.vissimctrl.VISSIMController;
import edu.umn.natsrl.ticas.plugin.vissimcalibration2.MeteringSectionHelper.StationState;
import edu.umn.natsrl.vissimcom.ComError;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.jawin.COMException;

/**
 *
 * @author Chongmyung Park
 */
public class Simulation extends Thread implements IStepListener, ITravelTimeListener {
    
    private Section section;
    private String caseFile;
    private int seed;
    private VISSIMController vc;
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters = new ArrayList<SimMeter>();
    private ArrayList<SimDetector> detectors = new ArrayList<SimDetector>();
    private HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
    private ISimEndSignal signalListener;            
    private int samples = 0;    
    private boolean noMetering = true;
    private VISSIMVersion version;
    
    private boolean isStop = false;
    public Simulation(String caseFile, int seed, Section section, boolean noMetering, VISSIMVersion v) {
        try {
            this.caseFile = caseFile;
            this.seed = seed;
            this.section = section;
            this.noMetering = noMetering;
            version = v;
            loadSignalGroupFromCasefile(this.caseFile);
            loadDetectorsFromCasefile(this.caseFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    
    
    @Override
    public void run() {        
        
        vc = new VISSIMController();
        
        ComError ce = ComError.getErrorbyID(vc.initialize(caseFile,seed,version));
        if(!ce.isCorrect()){
            this.signalListener.signalEnd(ce.getErrorType());
            this.vc.stop();
            this.vc.close();
            return;
        }
        
        vc.initializeMetering(100);
        vc.initializeTravelTimeMeasuring();
        vc.addTravelTimeListener(this);
        vc.addStepListener(1, this);
        this.signalListener.signalEnd(-1);
        
        int totalExecutionStep = vc.getTotalExecutionStep();
        int totalSamples = totalExecutionStep / 300;
        Metering metering = new Metering(section, meters, detectors);
        samples = 0;
        long sTime = new Date().getTime();
        
        ArrayList<StationState> stationStates = metering.sectionHelper.getStationStates();
        ArrayList<double[]> logvalues = new ArrayList();
        int setTime = 0;
        
        //init logvalue
        for(int i=0;i<stationStates.size();i++){
            double[] logvalue = new double[4];
            for(int z=0;z<logvalue.length;z++)
                logvalue[z] = 0;
            logvalues.add(logvalue);
        }
        
        while(true) {
            if(isStop)
                break;
            vc.run(30);
                
                StringBuilder ulog = new StringBuilder();
                setTime = (samples+1)*30;
                
                /*modify by soobin Jeon 2012/01/23
                 * input Simulation Log data
                 */
                for (int i = 0; i <stationStates.size(); i++) {
                    StationState s = stationStates.get(i);
                    logvalues.get(i)[0] += s.getSpeed();
                    logvalues.get(i)[1] += s.getDensity();
                    logvalues.get(i)[2] += s.getFlow();
                    logvalues.get(i)[3] += s.getVoulume();
                    //logvalues.get(i)[2] = logvalues.get(i)[3] * 3600 / MeteringConfig.getInterval(); //flow rate

                    if(setTime%MeteringConfig.getInterval() == 0){
                        //calculate log data
                        for(int z=0;z<logvalues.get(i).length;z++){
                            if(z != 3){
                                System.out.print(MeteringConfig.getInterval() + ", "+30+", "+MeteringConfig.getInterval()/30+"  ");
                                System.out.println(logvalues.get(i)[z]);
                                logvalues.get(i)[z] /= (MeteringConfig.getInterval()/30);
                            }
                        }
                        
                        ulog.append(s.id+"("+String.format("Speed:%.1fmile/h Density:%.1f Flow:%.1f, Volume:%.1f", logvalues.get(i)[0],logvalues.get(i)[1],logvalues.get(i)[2],logvalues.get(i)[3])+")\n");
                    }
                }
                
                if(setTime%MeteringConfig.getInterval() == 0){
                    System.out.println("30 Sec Data Log (Total Sec : "+setTime+")");
                    System.out.println(ulog.toString());
                    System.out.println("--------------------------------------------");
                    //reset logvalue
                    for(int k=0;k<logvalues.size();k++){
                        for(int z=0;z<logvalues.get(k).length;z++)
                            logvalues.get(k)[z] = 0;
                    }
                }
                //simObjects.
            samples++;
            
            if(samples >= totalSamples) {
                break;
            }
        }
        
        int elapsedSeconds = (int) ( ( new Date().getTime() - sTime ) / 1000 ) ;
        int h = elapsedSeconds / 3600;
        int m = ( elapsedSeconds % 3600 ) / 60;
        int s = ( elapsedSeconds % 3600 ) % 60;
        System.out.println("Simulation has been done (run time="+String.format("%02d", h)+":"+String.format("%02d", m)+":"+String.format("%02d", s)+")");
        writeTTLog();
        this.signalListener.signalEnd(1);
        
        this.vc.stop();
        this.vc.close();
    }
    
    public void simulationStop(){
        isStop = true;
        vc.stop();
        vc.close();
    }
    
    @Override
    /**
     * routine executed every simulation step by VISSIMController
     */
    public void runStepListener() {
        for(SimMeter meter : meters)
        {
            updateRampmeter(meter);
        }
    }

    /**
     * update lamp of ramp meter every simulation step
     * @param meter
     */
    private void updateRampmeter(SimMeter meter) {
        if (meter == null || !meter.isEnabled()) return;        
        meter.setCurrentSimTime(vc.getCurrentTime());
        meter.updateLamp(vc);
    }

    public int getSamples() {
        return samples;
    }
    
    void setVissimVisible(boolean b) {
        try {
            vc.setVisible(b);
        } catch (COMException ex) {
            ex.printStackTrace();
        }
    }        

    void setSignalListener(ISimEndSignal signalListener) {
        this.signalListener = signalListener;
    }    
    
    /**
     * Loads signal group list from VISSIM case file
     * @param contents
     * @return 
     */
    private void loadSignalGroupFromCasefile(String caseFile) throws IOException {        
        String contents = FileHelper.readTextFile(caseFile);
        
        if (contents == null || contents.isEmpty()) {
            System.out.println("Cannot find signal head(meter) in case file");
            System.exit(-1);
        }

        ArrayList<String> sgs = new ArrayList<String>();

        // get detector id from text
        String regx = "SIGNAL_GROUP ([0-9]+)  NAME \"(.*?)\"";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(contents);
        while (matcher.find()) {
            String dname = matcher.group(2).trim();
            if (!dname.isEmpty()) {
//                System.out.println("SIGNAL : " + dname);
                sgs.add(dname);
            }
        }

        for(String signal : sgs) {
            String name = signal;
            boolean isDual = false;
            if(name.contains("_L")) {
                name = name.split("_")[0];
                isDual = true;
            }
            if(name.contains("_R")) continue;            
            
            SimMeter sd = simObjects.getMeter(name);
            if(isDual) sd.setMeterType(SimMeter.MeterType.DUAL);
            else sd.setMeterType(SimMeter.MeterType.SINGLE);
            
            meters.add(sd);
        }
    }

    /**
     * Loades detector list from VISSIM casefile
     * @param contents
     * @return 
     */
    private void loadDetectorsFromCasefile(String caseFile) throws IOException {
        
        String contents = FileHelper.readTextFile(caseFile);
        
        if (contents == null || contents.isEmpty()) {
            return;
        }
        ArrayList<String> dets = new ArrayList<String>();

        // get detector id from text
        String regx = "DETECTOR (.*?) NAME";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(contents);
        while (matcher.find()) {
            String dname = matcher.group(1);
            if (!dname.isEmpty()) {
                dets.add(dname.trim());
            }
        }
        
        for(String det : dets) {
            detectors.add(simObjects.getDetector(""+det));
//            System.out.println("Detectors : D"+ det);
        }
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
            //System.out.println("TravelTime : " + ttid + " = " + String.format("%.2f", tt));
            ttData.add(tt);
        }
    }

    private void writeTTLog() {
        try {
            String prefix = "NEW_";
            if(this.noMetering) prefix = "NO_";
            
            WritableWorkbook workbook = Workbook.createWorkbook(new File(getFileName(prefix+"TT_LOG", "xls")));
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
            
            
            WritableSheet configSheet = workbook.createSheet("configurations", 1);
            if(MeteringConfig.UseMetering) {
                int r=0;
                configSheet.addCell(new Label(0, r++, "Kjam"));
                configSheet.addCell(new Label(0, r++, "Kc"));
                configSheet.addCell(new Label(0, r++, "Kd Rate"));
                configSheet.addCell(new Label(0, r++, "Kd"));
                configSheet.addCell(new Label(0, r++, "Kb"));
                configSheet.addCell(new Label(0, r++, "Ab"));
                configSheet.addCell(new Label(0, r++, "Max Waiting Time"));
                configSheet.addCell(new Label(0, r++, "Max Waiting Time for Freeway-to-Freeway Entrance"));            
                configSheet.addCell(new Label(0, r++, "Use Metering"));
                configSheet.addCell(new Label(0, r++, "Use Coordination"));
                configSheet.addCell(new Label(0, r++, "Case File"));
                configSheet.addCell(new Label(0, r++, "Random Seed"));

                r=0;
                configSheet.addCell(new Number(1, r++, MeteringConfig.Kjam));
                configSheet.addCell(new Number(1, r++, MeteringConfig.Kc));
                configSheet.addCell(new Number(1, r++, MeteringConfig.Kd_Rate));
                configSheet.addCell(new Number(1, r++, MeteringConfig.Kd));
                configSheet.addCell(new Number(1, r++, MeteringConfig.Kb));
                configSheet.addCell(new Number(1, r++, MeteringConfig.Ab));
                configSheet.addCell(new Number(1, r++, MeteringConfig.MAX_WAIT_TIME_MINUTE));
                configSheet.addCell(new Number(1, r++, MeteringConfig.MAX_WAIT_TIME_MINUTE_F2F));
                configSheet.addCell(new Label(1, r++, new Boolean(MeteringConfig.UseMetering).toString()));
                configSheet.addCell(new Label(1, r++, new Boolean(MeteringConfig.UseCoordination).toString()));
                configSheet.addCell(new Label(1, r++, MeteringConfig.CASE_FILE));
                configSheet.addCell(new Number(1, r++, MeteringConfig.RANDOM_SEED));
            } else {
                int r=0;
                configSheet.addCell(new Label(1, r++, "No Metering"));
                configSheet.addCell(new Label(0, r++, "Case File"));
                configSheet.addCell(new Label(0, r++, "Random Seed"));
                r = 1;                
                configSheet.addCell(new Label(1, r++, MeteringConfig.CASE_FILE));
                configSheet.addCell(new Number(1, r++, MeteringConfig.RANDOM_SEED));                
            }
            
            workbook.write();
            workbook.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String getFileName(String name, String ext) {
        String filepath = name + "." + ext;
        int count = 0;
        while (true) {
            File file = new File(filepath);
            if (!file.exists()) {
                break;
            }
            filepath = name + " (" + (++count) + ")" + "." + ext;
        }

        return filepath;
    }    

    public static interface ISimEndSignal {
        public void signalEnd(int code);
    }

    
}
