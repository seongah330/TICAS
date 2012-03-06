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

import com.inzoom.comjni.ComJniException;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimConfig;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.sfim.SFIMSectionHelper.EntranceState;
import edu.umn.natsrl.sfim.SFIMSectionHelper.StationState;
import edu.umn.natsrl.sfim.comm.CommLink;
import edu.umn.natsrl.sfim.comm.CommProtocol;
import edu.umn.natsrl.sfim.comm.Controller;
import edu.umn.natsrl.sfim.comm.InfoCommLink;
import edu.umn.natsrl.sfim.comm.InfoController;
import edu.umn.natsrl.sfim.comm.ResponserType;
import edu.umn.natsrl.sfim.comm.mndot.Controller170;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.Logger;
import edu.umn.natsrl.vissimcom.ITravelTimeListener;
import edu.umn.natsrl.vissimcom.VISSIMController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.jawin.COMException;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class SFIMManager implements ITravelTimeListener {

    /** real instance **/
    private static SFIMManager managerInstance;
    private Vector<InfraObject> infraObjects = new Vector<InfraObject>();
    /** all comm_links are managed by this */
    private Vector<CommLink> commLinks = new Vector<CommLink>();
    /** VISSIM simulation controller */
    private VISSIMController vc = new VISSIMController();
    /** all controllers are managed by this */
    private Vector<Controller> controllerList = new Vector<Controller>();
    /** VISSIM case file path */
    private String vissimCaseFile;
    /** clock updater **/
    private SFIMSyncServer syncServer;
    /** SFIMPanel reference **/
    private SFIMPanel sfimPanel;

    /** Section Helper (to get entrance information) **/
    private SFIMSectionHelper sectionHelper;
    /** for logging **/
    private HashMap<String, ArrayList<Double>> travelTimes = new HashMap<String, ArrayList<Double>>();
    private HashMap<SimMeter, Float> shouldUpdateRedTime = new HashMap<SimMeter, Float>();
    private HashMap<SimMeter, Byte> shouldUpdateMeterRate = new HashMap<SimMeter, Byte>();
    /** total execution time and samples of VISSIM **/
    private int totalExecutionTime = 0;
    private int totalSamples;
    /** simply used variables **/
    private Date startTime;
    private int detectControllerCount = 0;
    private int signalCount = 0;
    private int planId;
    private int samples = 0;
    private boolean useMetering = true;
    private boolean useVSA = true;
    private boolean fRunningTimer = false;
    private boolean showVehicles = true;
    private boolean isAlgorithmDone = true;
    private boolean done = false;
    private boolean DEBUG = false;
    private int randomSeed = 0;
    private boolean meteringStarted = false;
    private Calendar simStartTime;
    private int simDuration;
    ArrayList<Controller170> meteringControllers = new ArrayList<Controller170>();
    private int runDelay = 10;   // 10 ms
    private Logger meteringStartAndStopLog;
    
    /**
     * Constructor
     */
    public SFIMManager() {
        SFIMManager.managerInstance = this;
    }

    /**
     * Singleton (not strictly)
     * @return 
     */
    public static SFIMManager getInstance() {
        if (managerInstance == null) {
            new SFIMManager();
        }
        return managerInstance;
    }

    /**
     * Start!!
     */
    public void start(Section section, Calendar simStartTime, int simDuration) {
        this.simStartTime = simStartTime;
        this.simDuration = simDuration;        
        syncServer = new SFIMSyncServer();        
        syncServer.initialize(section, this.simStartTime);
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmm");        
        String time = format.format(new Date());        
        meteringStartAndStopLog = new Logger("metering_log_sfim ("+time+")", "csv");
    }

    public IDetectorChecker dc = new IDetectorChecker() {

        @Override
        public boolean check(Detector d) {
            if (d.isAbandoned() || d.isHov()) {
                return false;
            }
            return true;
        }
    };    
    
    /**
     * Run simulation
     * VISSIM simulation is not executed when running simulation
     * (fRunningTimer flag is used for this)
     */
    private void runSim() {

        if (fRunningTimer || done) {
            return;
        }
        fRunningTimer = true;
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                try {

//                    // update meter red time
//                    if (!shouldUpdateRedTime.isEmpty()) {
//                        HashMap<SimMeter, Float> updatedMeterRedTimes = null;
//                        synchronized (shouldUpdateRedTime) {
//                            updatedMeterRedTimes = (HashMap<SimMeter, Float>) shouldUpdateRedTime.clone();
//                            shouldUpdateRedTime.clear();
//                        }
//                        SimMeter[] updatedMeters = updatedMeterRedTimes.keySet().toArray(new SimMeter[updatedMeterRedTimes.size()]);
//                        for (SimMeter meter : updatedMeters) {
//                            meter.setRedTime(updatedMeterRedTimes.get(meter));
//                        }
//                    }

//                    // update meter rate
//                    if (!shouldUpdateMeterRate.isEmpty()) {
//                        HashMap<SimMeter, Byte> updatedMeterRates = null;
//                        synchronized (shouldUpdateMeterRate) {
//                            updatedMeterRates = (HashMap<SimMeter, Byte>) shouldUpdateMeterRate.clone();
//                            shouldUpdateMeterRate.clear();
//                        }
//                        SimMeter[] updatedMeters = updatedMeterRates.keySet().toArray(new SimMeter[updatedMeterRates.size()]);
//                        for (SimMeter meter : updatedMeters) {
//                            byte rate = updatedMeterRates.get(meter);
//                            if(rate == SimConfig.METER_RATE_CENTRAL) meteringStartAndStopLog.println(vc.getCurrentTime()+","+meter.getId() + ",start");
//                            else meteringStartAndStopLog.println(vc.getCurrentTime()+","+meter.getId() + ",stop");
//                            meter.setRate(rate);
//                        }
//                    }

                    System.out.println("[" + (samples+1) + "] Start to run simulation ....");
                    long st = new Date().getTime();

                    int step = vc.run(SFIMConfig.DEFAULT_TIME_UNIT);
                    samples++;
                    signalCount = 0;
                    long runTime = (new Date().getTime() - st) / 1000;
                    System.out.println("[" + samples + "] Finish running simulation for [" + runTime + "] seconds. (" + step + ")");

                    syncServer.sendPhase2Time();
//                    System.err.println("  + Send phase 2 msg");

                    if (samples >= totalSamples) {
                        System.out.println("[" + getTimeString() + "] Simulation has been done");
                        writeTTLog();
                        sfimPanel.signalSimulationEnd();
                        done = true;
                        return;
                    }
                    
                    for(StationState s : sectionHelper.getStationStates()) {
                        double u = s.station.getData(dc, TrafficType.SPEED);
                        double k = s.station.getData(dc, TrafficType.DENSITY);
                        double q = s.station.getData(dc, TrafficType.FLOW);
                        double v = s.station.getData(dc, TrafficType.VOLUME);
                        System.out.println(String.format("  " + s.station.getId() + " : u=%.1f, k=%.1f, q=%.1f, v=%.1f", u, k, q,v));
                    }
                    
                    for (EntranceState e : sectionHelper.getEntranceStates()) {
                        if (e.meter == null) {
                            continue;
                        }
                        e.updateState();
                        System.out.println(
                                "  !! " + e.meter.getId()
                                + ", flow!=" + String.format("%.2f", e.getFlow())
                                + ", demand=" + String.format("%.2f", e.getDemand())
                                + ", rate=" + String.format("%.2f", e.meter.getReleaseRate()));
//                                + ", volume =" + String.format("%.2f", e.getFlow()));
                    }                   
                } catch (Exception ex) {
                    SFIMExceptionHandler.handle(ex);
                    System.out.println("[" + getTimeString() + "] Fail to run simulation");
                } finally {
                    fRunningTimer = false;
                }

            }
        }, runDelay);
    }

    /**
     * SFIMSyncServer call this method
     * when getting algorithm done message from IRIS
     */
    public void algorithmDoneInIRIS() {
        isAlgorithmDone = true;
        runSim();
    }

    /**
     * Receive signal from all CommMnDot instances
     *   and run simulation for IIMConfig.DEFAULT_TIME_UNIT(=30 simulation sec)
     */
    public synchronized void signalResponse(ResponserType rType) {
        if (!rType.equals(ResponserType.MNDOT_GET_30S_DATA)) {
            return;
        }
        signalCount++;
        if (signalCount == 1 && isAlgorithmDone) {
            this.syncServer.sendPhase1Time();
            isAlgorithmDone = false;
        }
    }

    @Override
    /**
     * Implementation of ITravelTimeListener
     *   - save time travels from VISSIMController
     * @param travelTimeIds
     * @param travelTimes 
     */
    public void readTravelTime(String[] travelTimeIds, double[] travelTimes) {
        for (int i = 0; i < travelTimeIds.length; i++) {
            String ttid = travelTimeIds[i];
            double tt = travelTimes[i];

            ArrayList<Double> ttData = this.travelTimes.get(ttid);
            if (ttData == null) {
                ttData = new ArrayList<Double>();
                this.travelTimes.put(ttid, ttData);
            }
            debugLn("TravelTime : " + ttid + " = " + String.format("%.2f", tt));
            ttData.add(tt);
        }
    }

    /**
     * Save Time Travel Log
     */
    private void writeTTLog() {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(FileHelper.getNumberedFileName("IRIS_TT_LOG.xls")));
            WritableSheet sheet = workbook.createSheet("tt", 0);
            Iterator<String> itr = this.travelTimes.keySet().iterator();
            int col = 0;
            while (itr.hasNext()) {
                String key = itr.next();
                sheet.addCell(new Label(col, 0, key));
                ArrayList<Double> data = this.travelTimes.get(key);
                int row = 1;
                for (Double d : data) {
                    sheet.addCell(new Number(col, row++, d));
                }
                col++;
            }

            int r = 0;
            WritableSheet configSheet = workbook.createSheet("configurations", 1);
            configSheet.addCell(new Label(0, r++, "CaseFile"));
            configSheet.addCell(new Label(0, r++, "RandomSeed"));
            configSheet.addCell(new Label(0, r++, "Section"));
            configSheet.addCell(new Label(0, r++, "Use Metering"));
            configSheet.addCell(new Label(0, r++, "Use VSL"));

            r = 0;
            configSheet.addCell(new Label(1, r++, this.vissimCaseFile));
            configSheet.addCell(new Number(1, r++, this.randomSeed));
            configSheet.addCell(new Label(1, r++, this.sfimPanel.getSection().getName()));
            configSheet.addCell(new Label(1, r++, new Boolean(this.useMetering).toString()));
            configSheet.addCell(new Label(1, r++, new Boolean(this.useVSA).toString()));

            workbook.write();
            workbook.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Make comm_links to communicate with IRIS
     * @param activeCommLinkInfo checked comm_links in view interface (IIMSView)
     */
    public void makeCommLinks(Vector<InfoCommLink> activeCommLinkInfo, IRISDB_3_140 idb) {

        for (InfoCommLink cli : activeCommLinkInfo) {

            // new comm link
            CommLink comm = new CommLink(cli.name, cli.serverPort, cli.protocol);

            debugLn("Creating CommLink : " + cli.name + " (" + cli.serverPort + ", " + cli.protcolName + ")");
            System.out.println("Creating CommLink : " + cli.name + " (" + cli.serverPort + ", " + cli.protcolName + ")");
            // get controller info of the commlink from IRIS database
            Vector<InfoController> vci = idb.loadControllers(cli.name);

            for (InfoController ci : vci) {

                // comm_protocol (MnDot 5bit or DMSXML)
                CommProtocol protocol = CommProtocol.getCommProtocol(cli.protocol);

                // new controller
                Controller ctrl = Controller.createController(protocol, ci.drop);
                ctrl.setIO(ci.IOs);
                debugLn("   Creating Controller : " + ci.name);

                // count controller that has detector
                if (ci.hasDetector()) {
                    detectControllerCount++;
                }

                ctrl.setName(ci.name);
                ctrl.set(new Object[]{ci, vc, planId});
//                System.out.println("test!!");
                debug("    IOs : ");
                for (String io : ci.IOs) {
                    debug(io + ", ");
                }
                debugLn("");

                // add controller to commlink
                comm.addController(ctrl);

                // add controller to manager
                controllerList.add(ctrl);
            }
            comm.start();
            // add commlink to commlink list
            commLinks.add(comm);
        }


        StringBuilder meters = new StringBuilder();
        StringBuilder dmss = new StringBuilder();
        StringBuilder dets = new StringBuilder();
        Vector<String> meterObjs = new Vector<String>();
        Vector<String> detectorObjs = new Vector<String>();

        // make infra object list
        for (InfraObject obj : this.infraObjects) {
            if (obj == null) {
                continue;
            }
            if (obj.getInfraType().isDetector()) {
                dets.append(obj.getId() + SFIMConfig.newline);
                detectorObjs.add(String.format("%d", ((Detector) obj).getDetectorId()));
            } else if (obj.getInfraType().isMeter()) {
                meters.append(obj.getId() + SFIMConfig.newline);
                meterObjs.add(obj.getId());
            } else if (obj.getInfraType().isDMS()) {
                dmss.append(obj.getId() + SFIMConfig.newline);
            }
        }
        
        //3.140 updating
        // update time plan in IRIS
        System.out.println("Update timing plain : " + this.planId);
        idb.updateTimingPlans(meterObjs.toArray(new String[meterObjs.size()]), this.planId, this.useMetering, this.simStartTime, this.simDuration);

        if (DEBUG) {
            System.out.println("Number of controller that has detector : " + this.detectControllerCount);
            try {
                FileHelper.writeTextFile(dets.toString(), "list_detector.txt");
                FileHelper.writeTextFile(dmss.toString(), "list_dms.txt");
                FileHelper.writeTextFile(meters.toString(), "list_meter.txt");
            } catch (Exception ex) {
            }
        }

        syncServer.start();
    }

    /**
     * initialize simulation
     * set simulation period, data collection interval, COM initialization,
     * run simulation for 1sec (see comments in the code line)
     */
    public void simulationInitialize() {
        try {

            // param 1 : case file path
            // param 2 : signal control id for detector (see case file)
            // param 3 : random seed (if -1, use random seed of case file)
//            System.err.println("tests1");
            vc.initialize(vissimCaseFile, 1, randomSeed);
            vc.initializeTravelTimeMeasuring();
            vc.addTravelTimeListener(this);
            // param 1 : signal control id for metering (see case file)
            vc.initializeMetering(100);
            this.totalExecutionTime = vc.getTotalExecutionStep();
            this.totalSamples = totalExecutionTime / 300;
            this.samples = 0;
        } catch (Exception ex) {
            SFIMExceptionHandler.handle(ex);
        }
    }

    /**
     * Set vehicle and road visibility
     * @param b 
     */
    public void setVissimVisible(boolean b) {
        try {
            vc.setVisible(b);
            showVehicles = b;
        } catch (COMException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return true if vehicle and road are shown, else false
     * @return 
     */
    public boolean isShowVehicles() {
        return showVehicles;
    }

    /**
     * initial execution of simulation
     */
    public long simulationInitialRun() {
        try {
            long st = new Date().getTime();
            
            //soobin jeon modify
            vc.run(SFIMConfig.DEFAULT_TIME_UNIT);
            this.totalExecutionTime = vc.getTotalExecutionStep();

            debugLn(" Total Execution Time : " + this.totalExecutionTime);

            // trick to show frame before VISSIM
            SFIMFrame.getInstance().setAlwaysOnTop(true);
            SFIMFrame.getInstance().setAlwaysOnTop(false);

            return (new Date().getTime() - st) / 1000;

        } catch (Exception ex) {
            SFIMExceptionHandler.handle(ex);
            System.out.println("[" + getTimeString() + "] Fail to run VIISIM initially");
            return 0;
        }
    }

    /**
     * set VISSIM case file
     * @param caseFile VISSIM case file path
     */
    public void setCaseFile(String caseFile) {
        this.vissimCaseFile = caseFile;
    }

    /**
     * Set random seed for VISSIM
     * @param randomSeed 
     */
    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * set timing plan (metering algorithm) to simulate
     * it's selected by view interface (see SFIMView)
     */
    public void setTimingPlan(int planId) {
        this.planId = planId;
    }

    /**
     * Stop all threads and vissim
     */
    public void stop() {
        try {
            sfimPanel.restoreOutput();

            IRISController.stopIRIS();
            this.syncServer.close();
            this.meteringStartAndStopLog.writeLog();
            
            vc.stop();
            vc.close();

            // close all comm_links
            for (CommLink c : CommLink.getCommlinkList()) {
                c.terminate();
            }

        } catch (Exception ex) {
        }
    }

    //////////////////////////////////////
    /// Getter and Setter
    //////////////////////////////////////
    /**
     * Add infra object to list
     * @param obj 
     */
    public void addInfraObject(InfraObject obj) {
        this.infraObjects.add(obj);
    }

    /**
     * Return timestamp to string
     * @return 
     */
    private String getTimeString() {
        return getTimeString(-1);
    }

    /**
     * Return timestamp to string
     * @return 
     */
    private String getTimeString(long timestamp) {
        Calendar c = Calendar.getInstance();
        if (timestamp > 0) {
            c.setTimeInMillis(timestamp);
        }
        return String.format("%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    /**
     * Return comm_link
     * @param name
     * @return 
     */
    public CommLink getCommLink(String name) {
        for (CommLink comm : commLinks) {
            if (comm.getLinkName().equals(name)) {
                return comm;
            }
        }
        return null;
    }

    /**
     * Return VISSIMController
     * @return 
     */
    public VISSIMController getSimulationControl() {
        return vc;
    }

    /**
     * Set SFIMPanel reference
     * @param sfimPanel 
     */
    public void setSfimPanel(SFIMPanel sfimPanel) {
        this.sfimPanel = sfimPanel;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public int getSamples() {
        return samples;
    }    
    
    public Period getPeriod() {
        if (this.startTime == null || samples == 0) {
            return null;
        }

        int duration = samples * 30;
        Calendar c = Calendar.getInstance();

        c.setTime(startTime);
        c.set(Calendar.SECOND, 0);
        Date sTime = c.getTime();

        c.add(Calendar.SECOND, duration);
        Date eTime = c.getTime();

        return new Period(sTime, eTime, 30);
    }

    public void setUseMetering(boolean useMetering) {
        this.useMetering = useMetering;
    }

    public void setUseVSA(boolean useVSA) {
        this.useVSA = useVSA;
    }

    public boolean isUseMetering() {
        return useMetering;
    }

    public boolean isUseVSA() {
        return useVSA;
    }

    public Logger getMeterLog() {
        return meteringStartAndStopLog;
    }

    public void setSectionHelper(SFIMSectionHelper sectionHelper) {
        this.sectionHelper = sectionHelper;
    }

    private void debugLn(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }

    public void setMeterRedTime(SimMeter meter, float realRedTime) {
        synchronized (shouldUpdateRedTime) {
            shouldUpdateRedTime.put(meter, realRedTime);
        }
    }

    public void setMeterRate(Controller170 ctrl, SimMeter meter, byte rate) {
        synchronized (shouldUpdateMeterRate) {
            shouldUpdateMeterRate.put(meter, rate);
        }
        if (rate == SimConfig.METER_RATE_CENTRAL) {
            if (!meteringStarted) {
                System.out.println("-- Metering Started --");
            }
            meteringStarted = true;
            runDelay = 2000;
        }
    }

}
