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
package edu.umn.natsrl.vissimctrl;

import com.inzoom.comjni.ComJniException;
import com.inzoom.comjni.Variant;
import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.LaneType;
import edu.umn.natsrl.util.FileHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jcw.VISSIM_COMSERVERLib.IDesiredSpeedDecision;
import jcw.VISSIM_COMSERVERLib.IDesiredSpeedDecisions;
import jcw.VISSIM_COMSERVERLib.IDetector;
import jcw.VISSIM_COMSERVERLib.IDetectors;
import jcw.VISSIM_COMSERVERLib.INet;
import jcw.VISSIM_COMSERVERLib.ISignalController;
import jcw.VISSIM_COMSERVERLib.ISignalControllers;
import jcw.VISSIM_COMSERVERLib.ISignalGroup;
import jcw.VISSIM_COMSERVERLib.ISignalGroups;
import jcw.VISSIM_COMSERVERLib.ISimulation;
import jcw.VISSIM_COMSERVERLib.ITravelTime;
import jcw.VISSIM_COMSERVERLib.ITravelTimes;
import jcw.VISSIM_COMSERVERLib.IVissim;
import jcw.VISSIM_COMSERVERLib.Vissim;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class VISSIMController {

    /** Parameter */
    private String caseFile;
    private int randomSeed;
    private int travelTimeInterval;
    private boolean useTravelTimeMeasuring = false;
    /** VISSIM objects */
    private IVissim vissim;
    private ISimulation simulation;
    private INet net;
    private ISignalControllers signalControllers;
    private ISignalController signalControllerForDetector;
    private IDetectors detectors;
    private IDetector[] detectorList;
    private Detector[] queueDetectorList;
    private int[] speedLimits;
    private IDesiredSpeedDecisions desiredSpeedDecisions;
    private HashMap<String, IDesiredSpeedDecision> desiredSpeedDecisionList = new HashMap<String, IDesiredSpeedDecision>();
    private ITravelTimes travelTimes;
    private ITravelTime[] travelTimeList;
    /** Simulation Setting */
    private final int DEFAULT_SC_FOR_DETECTOR = 1;
    private final int RUNNING_STEP = 30; // runStepListener for 30 second     
    private final int FLOW_CONSTANT = 3600 / RUNNING_STEP;    // flow is calculated by volume for runningStep * flowConstant
    private final int DEFAULT_TIME_TRAVEL_INTERVAL = 5 * 60;    // 5min
    private int simResolution;
    private int simPeriod;
    private int simStep = 0; // this val keeps execution step
    private int totalExecutionStep; // how many step does simulation need to be executed?
    /** Information Storage */
    private HashMap<String, ISignalGroup> meterTable = new HashMap<String, ISignalGroup>();
    private DetectorData[] detectorData;
    // these arrays are used for notifing to listener temporarily
    String[] travelTimeNames;
    double[] travelTimeValues;
    int[] detectorIds;
    int[] detectorVolumes;
    int[] detectorFlows;
    float[] detectorSpeeds;
    float[] detectorDensities;
    /** Event Listener */
    private Vector<ITrafficListener> readTrafficListeners = new Vector<ITrafficListener>();
    private Vector<ITravelTimeListener> readTravelTimeListeners = new Vector<ITravelTimeListener>();
    private HashMap<Integer, Vector<IStepListener>> vcStepListeners = new HashMap<Integer, Vector<IStepListener>>();
    private SimObjects simObjects = SimObjects.getInstance();
    int threadStep = 30;
    private CollectDataThread[] collectThreads;

    public VISSIMController() {
    }

    // automatically runStepListener 
    public void autoRun() {
        while (run(RUNNING_STEP) > 0) {
            if (this.simStep >= this.totalExecutionStep) {
                break;
            }
        }
    }

    /**
     * runStepListener for given duration that should be multiple of 30
     * @param runTime time in second to runStepListener
     */
    public int run(int runTime) {

        // duration must be multiple of 30
        if (runTime <= 0 || runTime % RUNNING_STEP != 0) {
            return -1;
        }
        try {
            // simulation step that should be executed
            int runToThisStep = runTime * this.simResolution + this.simStep;
            if (runToThisStep > totalExecutionStep) {
                runToThisStep = totalExecutionStep;
            }
            float second = 0;
            int step;

            // loop for running
            for (step = simStep; step <= runToThisStep; step++, simStep++) {
                second = (float) step / simResolution;

                // Second = step / resolution
                simulation.runSingleStep();

                // every 30s routine
                if (second % RUNNING_STEP == 0) {

                    /********************************
                     * read traffic data
                     *********************************/
                    // for all detectors    
                    for (int i = 0; i < this.detectorList.length; i++) {
                        // calculate traffic data
                        int detector_id = this.detectorList[i].getID();
                        int v = this.detectorData[i].getVolume();
                        int q = v * FLOW_CONSTANT;

                        double u = (v == 0 ? speedLimits[i]+5 : this.detectorData[i].getSpeed() / v);
                        double k = (u <= 0 ? 0 : q / u);

                        double occupancy = -1;
                        if (queueDetectorList[i] != null) {
//                            Detector d = TMO.getInstance().getInfra().getDetector("D" + this.detectorList[i].getID());
                            double total_occupancy = this.detectorData[i].getOccupancy();
                            occupancy = Math.min(total_occupancy / RUNNING_STEP * 100, 100);
//                            double scanData = occupancy * InfraConstants.MAX_SCANS / 100;
//                            double sk = occupancy * 5280 / d.getFieldLength() / 100;                            
//                            double su = q / sk;
//                            System.out.println("v="+v+", u="+u+", k="+k+", occ=" + occupancy + ", scan="+scanData + ", u="+su+", k="+sk);
                        }

                        if (k > 300) {
                            System.out.println("WARNING!! VISSIMController > too high density : " + k);
                        }

                        SimDetector simDetector = simObjects.getDetector("D" + detector_id);
                        simDetector.addData(v, q, u, k, occupancy);

                    }

                    // reset detector data of for past 30s
                    for (int i = 0; i < this.detectorList.length; i++) {
                        this.detectorData[i].reset();
                    }

                } // end of every 30s routine

                /************************************************
                 * read travel data every travel time interval
                 ************************************************/
                if (useTravelTimeMeasuring && !this.readTravelTimeListeners.isEmpty() && step % (travelTimeInterval * simResolution) == 0) {

                    // for all travel times                       
                    for (int i = 0; i < this.travelTimeList.length; i++) {
                        travelTimeNames[i] = this.travelTimeList[i].getName();
                        // get travel time from starting to this second
                        travelTimeValues[i] = this.travelTimeList[i].getResult(second, "TRAVELTIME", "", 0).toDouble();
                    }

                    // notify traffic data to listener
                    for (ITravelTimeListener l : readTravelTimeListeners) {
                        if (l != null) {
                            l.readTravelTime(travelTimeNames, travelTimeValues);
                        }
                    }
                }

                // Read data using threads (Java COM is very slow)
                for (int i = 0, k=0; i < this.detectorList.length; i += threadStep, k++) {
                    int to = Math.min(i + threadStep - 1, this.detectorList.length - 1);
                    collectThreads[k] = new CollectDataThread(i, to);
                    collectThreads[k].start();
                }
            
                for (int i = 0; i < collectThreads.length; i++) {
                    try {
                        collectThreads[i].join();
                    } catch (InterruptedException ex) {}
                }
                
                //System.out.println("  * All threads are joined");

                // read detector data from VISSIM every step 
//                for (int i = 0; i < this.detectorList.length; i++) {
//
//                    if(queueDetectorList[i] != null) {
//                        detectorData[i].addOccupancy(detectorList[i].getAttValue("OCCUPANCY").getDouble());
//                    }
//                    
//                    // if any vehicle was over the detector
//                    if (detectorList[i].getAttValue("IMPULSE").getInt() == 1) {
//                        detectorData[i].addVolume();
//                        detectorData[i].addSpeed(detectorList[i].getAttValue("SPEED").getDouble());
//                    }
//
//                }

                // execute all VSSIM step listeners
                for (int freq : this.vcStepListeners.keySet()) {
                    if (step % freq == 0) {
                        for (IStepListener l : vcStepListeners.get(freq)) {
                            l.runStepListener();
                        }
                    }
                }

            } // for : main loop


            return simStep;

        } catch (ComJniException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    class CollectDataThread extends Thread {
        int fromIdx;
        int toIdx;
        String name;
        
        public CollectDataThread(int fromIdx, int toIdx) {
            this.fromIdx = fromIdx;
            this.toIdx = toIdx;
            this.name = fromIdx + " ~ " + toIdx;
        }

        public void run() {

            // read detector data from VISSIM every step 
            for (int i = fromIdx; i <= toIdx; i++) {
                try {
                    if (queueDetectorList[i] != null) {
                        detectorData[i].addOccupancy(detectorList[i].getAttValue("OCCUPANCY").getDouble());
                    }

                    // if any vehicle was over the detector
                    if (detectorList[i].getAttValue("IMPULSE").getInt() == 1) {
                        detectorData[i].addVolume();
                        detectorData[i].addSpeed(detectorList[i].getAttValue("SPEED").getDouble());
                    }
                } catch (ComJniException ex) {
                }

            }
        }
    }

    /**
     * stop simulation and close vissim
     */
    public void stop() {
        try {
            this.simulation.stop();
        } catch (ComJniException ex) {
        }

    }

    public void close() {
        try {
            this.vissim.exit();
        } catch (ComJniException ex) {
        }

    }

    /**
     * Set light of meter to green or red
     */
    public void setMeterStatus(String meterName, MeterLight status) {
        try {
            ISignalGroup sg = meterTable.get(meterName);
            if (sg == null) {
                return;
            }
            sg.setAttValue("STATE", new Variant(status.id));
        } catch (ComJniException ex) {
            ex.printStackTrace();
        }
    }

    /** Set VSA to DesiredSpeedDecision */
    public void setVSA(String dmsName, VSA vsa) {
        try {
            if (vsa.getDSDNumber() < 0) {
                System.out.println("  - Warnning!! VISSIMController.setVSA() : can not find DesiredSpeedDistribution for VSA");
                return;
            }

            // desired speed decision name rule : L35WN27_1
            // alternative speed decision name rule : L35WN27_1A
            //   * alternative speed decision is only-in-simulation-DMS for 2-step vsa in simulation
            //     (assume : driver increase speed after passing DMS
            IDesiredSpeedDecision dsd = this.desiredSpeedDecisionList.get(dmsName);
            IDesiredSpeedDecision dsd_alt = this.desiredSpeedDecisionList.get(dmsName + "A");

            if (dsd == null) {
                System.out.println("  - Warnning!! VISSIMController.setVSA() : can not find DMS(" + dmsName + ")");
                return;
            }
            System.out.println("  - " + dmsName + " : vsa=" + vsa.speed);
            Variant vsaNumber = new Variant(vsa.getDSDNumber());
            Variant avsaNumber = new Variant(vsa.getAltDSDNumber());

            for (VehicleClass v : VehicleClass.values()) {
                dsd.setAttValue1("DESIREDSPEED", new Variant(v.getVehicleClassId()), vsaNumber);
                if (dsd_alt != null) {
                    dsd_alt.setAttValue1("DESIREDSPEED", new Variant(v.getVehicleClassId()), avsaNumber);
                }
            }
        } catch (ComJniException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * initialize with given case file
     * set signal controller for detector to DEFAULT_SC_FOR_DETECTOR(1) and
     * use random seed of case file
     */
    public void initialize(String casefile) {
        initialize(casefile, this.DEFAULT_SC_FOR_DETECTOR, -1);
    }

    /**
     * initialize with only given random seed
     * set signal controller for detector to DEFAULT_SC_FOR_DETECTOR(1)
     * @param randomSeed 
     */
    public void initialize(String casefile, int randomSeed) {
        initialize(casefile, 1, randomSeed);
    }

    /**
     * initialize with given signal control id for detector and random seed
     * @param signalControlIdForDetector
     * @param randomSeed 
     */
    public void initialize(String casefile, int signalControlIdForDetector, int randomSeed) {
        try {
            // set casefile
            this.caseFile = casefile;

            // get vissim
            vissim = new Vissim();
            vissim.loadNet(this.caseFile);
            simulation = vissim.getSimulation();
            net = vissim.getNet();

            // get signal controllers
            signalControllers = net.getSignalControllers();
            signalControllerForDetector = signalControllers.getSignalControllerByNumber(signalControlIdForDetector);

            // set resolution and period
            simResolution = simulation.getResolution();
            simPeriod = (int) simulation.getPeriod();

            // how many execution steps do we need?
            totalExecutionStep = (int) simPeriod * simResolution;

            // set random seed
            if (randomSeed > 0) {
                simulation.setRandomSeed(randomSeed);
                this.randomSeed = randomSeed;
            } else {
                this.randomSeed = simulation.getRandomSeed();
            }

            // make desired speed decision list
            desiredSpeedDecisions = net.getDesiredSpeedDecisions();
            desiredSpeedDecisionList.clear();
            for (int i = 1; i <= desiredSpeedDecisions.getCount(); i++) {
                IDesiredSpeedDecision d = desiredSpeedDecisions.getItem(new Variant(i));
                desiredSpeedDecisionList.put(d.getName(), d);
            }
            // make detector list as hashmap
            detectors = signalControllerForDetector.getDetectors();
            detectorList = new IDetector[detectors.getCount()];
            queueDetectorList = new Detector[detectors.getCount()];
            detectorData = new DetectorData[detectors.getCount()];
            speedLimits = new int[detectorList.length];

            Infra infra = TMO.getInstance().getInfra();
            for (int i = 1; i <= detectors.getCount(); i++) {
                IDetector d = detectors.getItem(new Variant(i));
                detectorList[i - 1] = d;
                detectorData[i - 1] = new DetectorData();

                Detector det = infra.getDetector("D" + d.getID());
                if (det.getLaneType() == LaneType.QUEUE) {
                    queueDetectorList[i - 1] = det;
                } else {
                    queueDetectorList[i - 1] = null;
                }

                if (det.getStation() != null) {
                    speedLimits[i - 1] = det.getStation().getSpeedLimit();
                } else {
                    speedLimits[i - 1] = 0;
                }
            }

            // make travel time list as hash map
            travelTimes = net.getTravelTimes();
            travelTimeList = new ITravelTime[travelTimes.getCount()];
            for (int i = 1; i <= travelTimes.getCount(); i++) {
                travelTimeList[i - 1] = travelTimes.getItem(new Variant(i));
            }

            travelTimeNames = new String[this.travelTimeList.length];
            travelTimeValues = new double[this.travelTimeList.length];
            detectorIds = new int[this.detectorList.length];
            detectorVolumes = new int[this.detectorList.length];
            detectorFlows = new int[this.detectorList.length];
            detectorSpeeds = new float[this.detectorList.length];
            detectorDensities = new float[this.detectorList.length];

            initializeFromCaseFile();
            
            int threadN = this.detectorList.length / threadStep;
            if(this.detectorList.length % threadStep != 0) threadN++;
            this.collectThreads = new CollectDataThread[threadN];

            simulation.runSingleStep();
            simStep++;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * make ramp meter table (signal group) 
     * @param signalControlStartNum signal control start number for metering
     */
    public void initializeMetering(int signalControlStartNum) {
        try {
            // make metering table
            for (int i = signalControlStartNum; i < signalControlStartNum + this.signalControllers.getCount(); i++) {
                try {
                    ISignalController sc = signalControllers.getSignalControllerByNumber(i);
                    if (sc == null) {
                        continue;
                    }
                    ISignalGroups sgs = sc.getSignalGroups();
                    for (int k = 1; k <= sgs.getCount(); k++) {
                        ISignalGroup sg = sgs.getSignalGroupByNumber(k);
                        meterTable.put(sg.getName(), sg);
                        setMeterStatus(sg.getName(), MeterLight.GREEN);
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * initialize for travel time measuring
     * with default interval (5min)
     */
    public boolean initializeTravelTimeMeasuring() {
        return initializeTravelTimeMeasuring(this.DEFAULT_TIME_TRAVEL_INTERVAL);
    }

    /**
     * initialize for travel time measuring
     * @param second travel time measuring interval
     */
    public boolean initializeTravelTimeMeasuring(int second) {
        if (second < 0 || second % 30 != 0) {
            return false;
        }
        this.travelTimeInterval = second;
        this.useTravelTimeMeasuring = true;
        return true;
    }

    /**
     * CAUTION!!
     * if VISSIM case file format is changed,
     * regular expression of this method must be changed.
     * @brief this method reads VISSIM case file and extract desired speed distribution and vehicle class information
     */
    private void initializeFromCaseFile() throws IOException {

        String text = FileHelper.readTextFile(this.caseFile);

        // VSA setting
        String regx = "DESIRED_SPEED (.*?)NAME \"(.*?)\"  (.*?)";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(text);
        while (matcher.find()) {
            int did = Integer.parseInt(matcher.group(1).trim());
            String dname = matcher.group(2);
            VSA.setDSDNumber(dname, did);
        }

        // Vehicle Class setting
        regx = "VEHICLE_CLASS  ([0-9]+)\\r\\n     NAME          \"(.*?)\"(.*?)";
        p = Pattern.compile(regx);
        matcher = p.matcher(text);
        while (matcher.find()) {
            int vid = Integer.parseInt(matcher.group(1).trim());
            String vname = matcher.group(2);
            VehicleClass.setVehicleClassId(vname, vid);
        }
    }

//    /**
//     * @deprecated 
//     * @param resolution
//     * @throws ComJniException 
//     */
//    public void setResolution(int resolution) throws ComJniException {
//        // update resolution
//        float ratio = (float) resolution / this.simResolution;
//        this.simulation.setResolution(resolution);
//        this.totalExecutionStep = simPeriod * resolution;
//        simStep *= ratio;
//        this.simResolution = resolution;
//    }

    public int getResolution() {
        return this.simResolution;
    }

    /**
     * Show or hide vehicles and load on the VISSIM GUI
     */
    public void setVisible(boolean b) throws ComJniException {
        // hide or show vehicles
        this.vissim.getGraphics().setAttValue("VISUALIZATION", new Variant(b));

        // hide or show road
        if (b) {
            this.vissim.getGraphics().setAttValue("DISPLAY", new Variant(0)); // hide
        } else {
            this.vissim.getGraphics().setAttValue("DISPLAY", new Variant(2));   // show
        }
        this.vissim.getGraphics().redraw();
    }

    /*********************************
     * Setter and Getter
     **********************************/
    public void addTrafficListener(ITrafficListener readTrafficListener) {
        this.readTrafficListeners.add(readTrafficListener);
    }

    public void addTravelTimeListener(ITravelTimeListener readTravelTimeListener) {
        this.readTravelTimeListeners.add(readTravelTimeListener);
    }

    /**
     * 
     * @param interval interval in step
     * @param stepListner IStepListener instance
     */
    public void addStepListener(int interval, IStepListener stepListner) {
        Vector<IStepListener> listeners = this.vcStepListeners.get(interval);
        if (listeners == null) {
            listeners = new Vector<IStepListener>();
            listeners.add(stepListner);
            this.vcStepListeners.put(interval, listeners);
        } else {
            listeners.add(stepListner);
        }
    }

    /**
     * Return case file
     * @return 
     */
    public String getCaseFile() {
        return caseFile;
    }

    public HashMap<String, ISignalGroup> getMeterTable() {
        return meterTable;
    }

    /**
     * @return random seed of simulation
     */
    public int getRandomSeed() {
        return randomSeed;
    }

    /**
     * @return simulation period in second
     */
    public int getSimPeriod() {
        return simPeriod;
    }

    /**
     * @return simulation resolution (time steps / simulation second)
     */
    public int getSimResolution() {
        return simResolution;
    }

    /**
     * @return current execution step (second * resolution)
     */
    public int getSimStep() {
        return simStep;
    }

    /**
     * @return total execution step (period * resolution)
     */
    public int getTotalExecutionStep() {
        return totalExecutionStep;
    }

    /**
     * @return interval to get travel time in second
     */
    public int getTravelTimeInterval() {
        return travelTimeInterval;
    }

    /**
     * Return current time in second
     * @return 
     */
    public float getCurrentTime() {
        return (float) this.simStep / this.simResolution;
    }

    /**
     * Class for saving detector data for 30s temporarily
     */
    private class DetectorData {

        private int volume = 0;
        private double speed = 0;
        private double occupancy = 0;
        private double pc = 0;  // previous occupancy

        public void addSpeed(double u) {
            if (u < 0.1 || u > 200) {
                u = 0;
            }
            this.speed += u;
        }

        public void addOccupancy(double c) {
            if (c == 0D) {
                this.occupancy += pc;
            }
            pc = c;
        }

        public void addVolume() {
            this.volume++;
        }

        public void reset() {
            this.volume = 0;
            this.speed = 0;
            this.occupancy = 0;
        }

        public double getSpeed() {
            return speed;
        }

        public int getVolume() {
            return volume;
        }

        public double getOccupancy() {
            return occupancy + pc;
        }
    }
}
