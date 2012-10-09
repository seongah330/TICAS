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
package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.ticas.Simulation.SectionHelper.EntranceState;
import edu.umn.natsrl.ticas.Simulation.SectionHelper.StationState;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.ComError;
import edu.umn.natsrl.vissimcom.IStepListener;
import edu.umn.natsrl.vissimcom.ITravelTimeListener;
import edu.umn.natsrl.vissimcom.VISSIMController;
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
public class Simulation extends Thread implements IStepListener, ITravelTimeListener{
    private Section section;
    private String caseFile;
    private int seed;
    private VISSIMController vc;
    private SimObjects simObjects = SimObjects.getInstance();
    private ArrayList<SimMeter> meters = new ArrayList<SimMeter>();
    private ArrayList<SimDetector> detectors = new ArrayList<SimDetector>();
    private HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
    
    ArrayList<StationState> stationStates;
    ArrayList<EntranceState> entrancestates;
    
    private ISimEndSignal signalListener;            
    private int samples = 0;    
    
    private int simDuration = 0;
    
    public SectionHelper sectionHelper;
    
    private VISSIMVersion version;
    
    private boolean isStop = false;
    
    private int DebugInterval = 30;
    
    private boolean isDebug_StationInfo = true;
    private boolean isDebug_EntranceInfo = true;
    
    public Simulation(String caseFile, int seed, Section section, VISSIMVersion v){
        try{
            this.caseFile = caseFile;
            this.seed = seed;
            this.section = section;
            version = v;
            
            loadSignalGroupFromCasefile(this.caseFile);
            loadDetectorsFromCasefile(this.caseFile);
            loadSimulationDuration(this.caseFile);
            sectionHelper = new SectionHelper(section, detectors,meters);

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
        int simcount=0; //30sec interval
        int runInterval = 30;
        
        /**
         * Running Initialize
         */
        RunningInitialize();
        
        while(true){
            if(isStop)
                break;
            
            ExecuteBeforeRun();
            vc.run(runInterval);
            ExecuteAfterRun();
            
            simcount+=runInterval;
            if(isInterval(simcount)){
                System.out.println("\nData Log (Total Sec : "+simcount+")");
                DebugMassage();
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
    
    protected void RunningInitialize() {
        stationStates = sectionHelper.getStationStates();
        entrancestates = sectionHelper.getEntranceStates();
    }
    
    protected void ExecuteBeforeRun() {
        
    }

    protected void ExecuteAfterRun() {
        updateEntranceStates();
    }
    
    protected void DebugMassage(){
        DisplayStationState();
        DisplayMeterState();
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
            if(sd.getId() != null){
                if(isDual) sd.setMeterType(SimMeter.MeterType.DUAL);
                else sd.setMeterType(SimMeter.MeterType.SINGLE);

                meters.add(sd);
            }
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
            System.out.println("Detectors : D"+ det);
        }
    }
    private void loadSimulationDuration(String caseFile) throws IOException {        
        String contents = FileHelper.readTextFile(caseFile);
        
        if (contents == null || contents.isEmpty()) {
            System.out.println("Cannot find signal head(meter) in case file");
            System.exit(-1);
        }
        String[] regx = {"SIMULATION_DURATION ([0-9]+)","SIMULATION_DURATION  ([0-9]+)"};
        int duration = -1;
        // get detector id from text
        for(String reg : regx){
            duration = fineDuration(reg,contents);
            if(duration > 0)
                break;
        }
        
        this.simDuration = duration;
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
    
    public void setVissimVisible(boolean b){
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

    public void setVISSIMVersion(VISSIMVersion selectedItem) {
        version = selectedItem;
    }

    private boolean isInterval(int count) {
        return count % DebugInterval == 0;
    }

    private void updateEntranceStates() {
        if(entrancestates.isEmpty())
            return;
        
        for(EntranceState e : entrancestates){
            e.updateState();
        }
    }

    public void DisplayMeterState() {
        if(this.isDebug_EntranceInfo){
            System.err.println("clearlog");
            System.err.println("=====Metering State======================");
            for(int i=0;i<entrancestates.size();i++){
                EntranceState es = entrancestates.get(i);
                if(es.hasMeter())
                    System.err.println(es.meter.getId() + " : " + "Queue Demand="+es.getQueueVolume()+", Passage="+es.getPassageVolume()+", Rate="+es.getCurrentRate());
            }
        }
    }

    public void DisplayStationState() {
        if(this.isDebug_StationInfo){
            //for Station debuging
            for (int i = 0; i < stationStates.size(); i++) {
                System.out.println(stationStates.get(i).id + " : T_Q="+String.format("%.1f",stationStates.get(i).getFlow())
                        + " A_Q="+String.format("%.1f",stationStates.get(i).getAverageFlow(0, this.getDebugIntervalIndex()))
                        + " k=" +String.format("%.1f", stationStates.get(i).getAverageDensity(0,getDebugIntervalIndex()))
                        + " u=" + String.format("%.1f", stationStates.get(i).getAverageSpeed(0, getDebugIntervalIndex()))
                        + " v=" + stationStates.get(i).getTotalVolume(0, getDebugIntervalIndex()));
            }
        }
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
    
    public void setRandomSeed(int s){
        this.seed = s;
    }
    public void setDebugInterval(int t){
        this.DebugInterval = t;
    }
    public int getDebugIntervalIndex(){
        return this.DebugInterval / 30;
    }
    public void setDebugStationInfo(boolean is){
        this.isDebug_StationInfo = is;
    }
    public void setDebugEntranceInfo(boolean is){
        this.isDebug_EntranceInfo = is;
    }
    
    //Metering
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
        return Math.max(cycle - meter.GREEN_YELLOW_TIME, SimulationConfig.MIN_RED_TIME);
    }
}
