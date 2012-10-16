/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
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
package edu.umn.natsrl.ticas.plugin.fixedmetering;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.ticas.plugin.metering.MeteringSectionHelper;
import edu.umn.natsrl.ticas.plugin.metering.MeteringSectionHelper.StationState;
import edu.umn.natsrl.ticas.plugin.metering.Simulation;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.ComError;
import edu.umn.natsrl.vissimcom.IStepListener;
import edu.umn.natsrl.vissimcom.ITravelTimeListener;
import edu.umn.natsrl.vissimcom.VISSIMController;
import edu.umn.natsrl.vissimcom.VISSIMHelper;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.jawin.COMException;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class FixedSimulation extends Thread implements IStepListener, ITravelTimeListener{
    private Section section;
    private String caseFile;
    private int seed;
    private VISSIMController vc;
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<FixedMeter> fixedmeter = new ArrayList<FixedMeter>();
    private ArrayList<SimMeter> meters = new ArrayList<SimMeter>();
    private ArrayList<SimDetector> detectors = new ArrayList<SimDetector>();
    private HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
    private ISimEndSignal signalListener;            
    private int samples = 0;    
    
    private int simDuration = 0;
    
    public MeteringSectionHelper sectionHelper;
    
    private VISSIMVersion version;
    
    private boolean isStop = false;
    public FixedSimulation(String caseFile, int seed, Section section, VISSIMVersion v){
        try{
            this.caseFile = caseFile;
            this.seed = seed;
            this.section = section;
            version = v;
            
            loadSignalGroupFromCasefile(this.caseFile);
            detectors = loadDetectorFromCasefile(this.caseFile);
            this.simDuration = VISSIMHelper.loadSimulationDuration(this.caseFile);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run(){
        isStop = false;
        System.out.print("Starting VISSIM Simulator......");
        vc = new VISSIMController();
        ComError ce = ComError.getErrorbyID(vc.initialize(caseFile,seed,version));
        if(!ce.isCorrect()){
            this.signalListener.signalEnd(ce.getErrorType());
            this.vc.stop();
            this.vc.close();
            return;
        }
        
        System.out.println("Ok");
        
        System.out.print("VISSIM initializing.......");
        vc.initializeMetering(100);
        vc.initializeTravelTimeMeasuring();
        vc.addTravelTimeListener(this);
        vc.addStepListener(1, this);
        this.signalListener.signalEnd(-1);
        System.out.println("Ok");
        
         int totalExecutionStep = vc.getTotalExecutionStep();
        int totalSamples = totalExecutionStep / 300;
        System.out.println("total Running Time : " +totalExecutionStep+", "+totalSamples);
        samples = 0;
        long sTime = new Date().getTime();
        sectionHelper = new MeteringSectionHelper(section, meters, detectors);
        ArrayList<StationState> stationStates = sectionHelper.getStationStates();
        int simcount=0; //30sec interval
        int runInterval = 30;
        while(true){
            if(isStop)
                break;
            System.out.println("\nData Log (Total Sec : "+simcount+")");
            setMeterRate(simcount);
            simcount+=runInterval;
            vc.run(runInterval);
            //for debuging
            for (int i = 0; i < stationStates.size(); i++) {
                System.out.println(stationStates.get(i).id + " : q="+stationStates.get(i).getFlow()
                        + " k=" +String.format("%.1f", stationStates.get(i).getDensity())
                        + " u=" + String.format("%.1f", stationStates.get(i).getSpeed())
                        + " v=" + stationStates.get(i).getVolume());
            }
            samples++;
            if(samples >= totalSamples) {
//                metering.writeLog();
                break;
            }
        }
        
        int elapsedSeconds = (int) ( ( new Date().getTime() - sTime ) / 1000 );
        int h = elapsedSeconds / 3600;
        int m = ( elapsedSeconds % 3600 ) / 60;
        int s = ( elapsedSeconds % 3600 ) % 60;
        System.out.println("Simulation has been done (run time="+String.format("%02d", h)+":"+String.format("%02d", m)+":"+String.format("%02d", s)+")");
//        writeTTLog();
        this.signalListener.signalEnd(0);
        
        this.vc.stop();
        this.vc.close();
    }
    
    public void simulationStop(){
        isStop = true;
        vc.stop();
        vc.close();
    }
    
    
    /**
     * Loads signal group list from VISSIM case file
     * @param contents
     * @return 
     */
    private void loadSignalGroupFromCasefile(String caseFile) throws IOException {        
        ArrayList<String> sgs = VISSIMHelper.loadSignalGroupsFromCasefile(caseFile);
        
        if(sgs == null){
            System.out.println("Cannot find signal head(meter) in case file");
            System.exit(-1);
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
            if(sd.getId() != null){
                if(isDual) sd.setMeterType(SimMeter.MeterType.DUAL);
                else sd.setMeterType(SimMeter.MeterType.SINGLE);

                meters.add(sd);
            }
        }
    }

    private int fineDuration(String regx, String contents) {
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(contents);
        
        while (matcher.find()) {
            String dname = matcher.group(1).trim();
            if (!dname.isEmpty()) {
                System.out.println("SIGNAL : " + dname);
                return Integer.parseInt(dname);
            }
        }
        
        return -1;
    }
    
    public ArrayList<SimMeter> getSimMeter(){
        return meters;
    }
    
    void setVissimVisible(boolean b){
        try {
            vc.setVisible(b);
        } catch (COMException ex) {
            ex.printStackTrace();
        }
    }
    
    private void writeTTLog() {
        try {
            String prefix = "NEW_";
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
            int r=0;
            configSheet.addCell(new Label(1, r++, "No Metering"));
            configSheet.addCell(new Label(0, r++, "Case File"));
            configSheet.addCell(new Label(0, r++, "Random Seed"));
            r = 1;                
            configSheet.addCell(new Label(1, r++, this.caseFile));
            configSheet.addCell(new Number(1, r++, this.seed));                
            
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

    @Override
    public void runStepListener() {
        for(SimMeter meter : meters){
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
    
    public void setSignalListener(ISimEndSignal signalListener) {
        this.signalListener = signalListener;
    }

    private void setMeterRate(int sample){
        if(this.fixedmeter.isEmpty()){
            setRate(1718);
            return;
        }
//        System.out.println("Signal Rate");
        for(FixedMeter m : fixedmeter){
            m.setFixedRate(sample);
//            System.out.println(m.meter.getId() + " : "+m.getLastRate());
        }
    }
    private void setRate(double rnext) {
        for(SimMeter meter : meters){
            double lastRate = rnext;
            float redTime = calculateRedTime(meter,rnext);
            redTime = Math.round(redTime * 10) / 10f;
            meter.setRate((byte)1);
            meter.setRedTime(redTime);
        }
    }
    /**
         * Return red time that converted from rate
         * @param rate
         * @return red time in seconds
         */
        private float calculateRedTime(SimMeter meter, double rate) {
            float cycle = 3600 / (float)rate;
            return Math.max(cycle - meter.GREEN_YELLOW_TIME, MeteringConfig.MIN_RED_TIME);
        }

    void setVISSIMVersion(VISSIMVersion selectedItem) {
        version = selectedItem;
    }

    private ArrayList<SimDetector> loadDetectorFromCasefile(String caseFile) {
        ArrayList<SimDetector> simDets = new ArrayList<SimDetector>();
        ArrayList<String> dets = VISSIMHelper.loadDetectorsFromCasefile(caseFile);
        for(String det : dets) {
            simDets.add(simObjects.getDetector(det));
        }
        return simDets;
    }
    
    public static interface ISimEndSignal {
        public void signalEnd(int code);
    }
    
    public int getSamples() {
        return samples;
    }
    
    public int getDuration(){
        return this.simDuration;
    }
    
    public void setFixedMeter(ArrayList<FixedMeter> m){
        fixedmeter = m;
    }
    public void setRandomSeed(int s){
        this.seed = s;
    }
}
