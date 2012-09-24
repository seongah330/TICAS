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

package edu.umn.natsrl.infra.infraobjects;

import edu.umn.natsrl.infra.DetectorDataReader;
import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.AdjustType;
import edu.umn.natsrl.infra.types.DetectorType;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.infra.types.LaneType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park
 */
public class Detector extends InfraObject implements Comparable {

    private int detector_id;
    private LaneType laneType = LaneType.NONE;
    transient private Period period;
    private RNode r_node;
    private String cachePath = InfraConstants.CACHE_DETDATA_DIR;
    private double confidence = -1;
    transient private Vector<Double> volume = new Vector<Double>();
    transient private Vector<Double> speed = new Vector<Double>();
    transient private Vector<Double> flow = new Vector<Double>();
    transient private Vector<Double> occupancy = new Vector<Double>();
    transient private Vector<Double> density = new Vector<Double>();
    transient private Vector<Double> scan = new Vector<Double>();
    transient private static DetectorCache detectorCache = new DetectorCache();
    
    DetectorType dType;
    
    public Detector(Element element) {
        super(element);
        this.laneType = LaneType.get(getProperty(InfraProperty.category));
        this.infraType = InfraType.DETECTOR;
//        this.detector_id = Integer.parseInt(this.id.substring(1));  // D123 -> 123        
        String tempID = this.getProperty(InfraProperty.name);
        this.detector_id = DetectorType.getDetectorIDbyType(tempID);
        dType = DetectorType.getDetectorType(tempID);
        
        //if T Detector -> add "D" -- it's like as Station 'T'
        if(dType.isTempDetector()){
            this.id = "D"+tempID;
        }else
            this.id = tempID;
    }
       
    /**
     * Read traffic data from cache or remote data
     * @param p
     */
    public void loadData(Period p, boolean simmode) throws OutOfMemoryError {
        loadData(p,simmode,null);
    }
    public void loadData(Period p, boolean simmode,SimObjects sobj) throws OutOfMemoryError {
        this.clear();
        
        this.period = p;
        
        if(simmode) {
            if(sobj == null)
                this.fillSimulationData();
            else
                this.fillSimulationData(sobj);
            return;
        }                
                
//        if(this.loadCache()) return;
                
        this.confidence = -1;

        if (p.interval % 30 != 0) {
            JOptionPane.showMessageDialog(null, "Interval must be the multiples of 30");
        }
        
        DetectorDataReader ddr = new DetectorDataReader(this, p);
        ddr.load();
        double[] scan_data = ddr.read(TrafficType.SCAN);
        double[] volume_data = ddr.read(TrafficType.VOLUME);
        double[] density_data = ddr.read(TrafficType.DENSITY);
        
        this.adjustTrafficData(volume_data, scan_data, density_data);
        /*
         * modify soobin Jeon 02/17/2012
         * not correct.. wavetronics speed calculate... perhaps...
         */
        if (this.isWavetronics()) {
            double[] speed_data = ddr.read(TrafficType.SPEED_FOR_MICROWAVE);
            if (speed_data != null) {
                this.speed.clear();
                for (double d : speed_data) {
                    this.speed.add(d);
                }
            }
        }
        
        this.saveCache();
    }
    /**
     * Adjusts traffic data
     *   - make volume properly
     *   - make speed data
     *   - make scan data
     * @param volume_data
     * @param scan_data
     * @param density_data
     */
    private void adjustTrafficData(double[] volume_data, double[] scan_data, double[] density_data) {
        if (volume_data == null || scan_data == null || density_data == null) {
            return;
        }
        this.clear();

        int validCount = 0;

        for (int i = 0; i < volume_data.length; i++) {
            // volume
            double v = Math.min(volume_data[i], InfraConstants.MAX_VOLUME);
            
            // flow
            double q = v * InfraConstants.SAMPLES_PER_HOUR;
            
            // scan
            double c = Math.min(scan_data[i], InfraConstants.MAX_SCANS);
            
            // occupancy
            double o = c / InfraConstants.MAX_SCANS * 100;
            
            // density
            double k = density_data[i];

            // speed
            double u = Math.min(q / k, InfraConstants.MAX_SPEED);
            
            
            // check if density is valid
            if(k < InfraConstants.DENSITY_THRESHOLD) {
                k = InfraConstants.MISSING_DATA;
            }
            
            /*
             * mod
             */
            if(q <= 0){
                q = o = k = u = InfraConstants.MISSING_DATA;
            }
            else if (k <= 0) {
                o = k = u = InfraConstants.MISSING_DATA;
            } else if(scan_data[i] > InfraConstants.MAX_SCANS){
                o = k = u = InfraConstants.MISSING_DATA;
            } else {
                validCount++;
            }
            
            /*
             * Modify to upper Code instead of this code
             * Green Signal Detector is not influenced by density Data
             * 02/07/2012 by soobin Jeon
             */
//            if (q <= 0 || k <= 0) {
//                q = o = k = u = InfraConstants.MISSING_DATA;
//            } else {
//                validCount++;
//            }
            
            this.flow.add(q);
            this.occupancy.add(o);
            this.speed.add(u);
            this.density.add(k);
            this.volume.add(volume_data[i]);
            this.scan.add(scan_data[i]);
//            if(this.detector_id == 131 || this.detector_id == 132 || this.detector_id == 7032)
//                System.out.println("     " + getId() + " : U="+u + ", Q="+volume_data[i]+", K="+k+", SCAN=" + scan_data[i] + ", FIELD=" +getFieldLength());
        }
        
    }

    
    /**
     * Make data fit to interval
     * @param data
     * @return 
     */
    public Vector<Double> adjustInterval(Vector<Double> data, AdjustType atype) {
        int interval = this.period.interval / 30;
        Vector<Double> aData = new Vector<Double>();

        for (int i = 0; i < data.size(); i += interval) {
            double sum = 0.0;
            double validCount = 0;

            for (int j = i; j < i + interval; j++) {
                double v = data.get(j);
                if (v > 0) {
                    sum += v;
                    validCount++;
                }else if(atype.IsDensityWithStation())
                    validCount++;
            }
            
            /*
             * modify soobin Jeon 02/14/2012
             */
            if (validCount > 0) {
                if(atype.IsAverage() || atype.IsDensityWithStation())
                    aData.add(sum / validCount);
                else if(atype.IsCumulative())
                    aData.add(sum);
                else if(atype.IsFlow())
                    aData.add(sum/interval);
            } else {
                aData.add(-1.0);
            }
        }
        return aData;
    }
    
    
    
    
    

    /**
     * Resets memory
     */
    public void clear() {
        if (this.speed == null) {
            this.volume = new Vector<Double>();
            this.speed = new Vector<Double>();
            this.flow = new Vector<Double>();
            this.occupancy = new Vector<Double>();
            this.density = new Vector<Double>();
            this.scan = new Vector<Double>();
        }        
        this.flow.clear();
        this.occupancy.clear();
        this.speed.clear();
        this.density.clear();
        this.volume.clear();
        this.scan.clear();
    }

    /**
     * Fill simulation data to real data array
     * @param d 
     */
    public void fillSimulationData(){
        fillSimulationData(null);
    }
    public void fillSimulationData(SimObjects sobj) {
        clear();
        SimDetector d;
        if(sobj == null)
            d = SimObjects.getInstance().getDetector(this.id);
        else
            d = sobj.getDetector(this.id);
        
         
        
        
        int samples = period.getTimeline().length * (period.interval / 30);        
//        System.out.println(this.id + ".fillSimulationData() : size=" + d.getSpeed().size() + ", times=" + samples);
        
        if(d.getSpeed().size() == 0) {
            Double[] dummy = new Double[samples];
            Arrays.fill(dummy, -1D);
            List dummyList = Arrays.asList(dummy);
            this.speed.addAll(dummyList);
            this.flow.addAll(dummyList);
            this.occupancy.addAll(dummyList);
            this.density.addAll(dummyList);
            this.volume.addAll(dummyList);
            this.scan.addAll(dummyList);            
            return;
        }
        
//        for(double dd : d.getSpeed()){
//            System.out.print(dd+",");
//        }
//        System.out.println();
        
//        addData(speed, d.getSpeed(), samples);
//        addData(flow, d.getFlow(), samples);
//        addData(occupancy, d.getOccupancy(), samples);
//        addData(density, d.getDensity(), samples);
//        addData(volume, d.getVolume(), samples);
//        addData(scan, d.getScan(), samples);
        
        addData(speed, d.getSpeed());
        addData(flow, d.getFlow());
        addData(occupancy, d.getOccupancy());
        addData(density, d.getDensity());
        addData(volume, d.getVolume());
        addData(scan, d.getScan());
    }
    
    private void addData(Vector<Double> dst, Vector<Double> src, int samples) {
//        System.out.println("Add data : size="+src.size()+", samples="+samples);
        for(int i=0; i<samples; i++) {
                dst.add(src.get(i));
//            else
//                dst.add((double)0);
        }
    }
    
    private void addData(Vector<Double> dst, Vector<Double> src) {
//        System.out.println("Add data : size="+src.size()+", samples="+samples);
        for(int i=0; i<src.size(); i++) {
                dst.add(src.get(i));
//            else
//                dst.add((double)0);
        }
    }

    /////////////////////////////////////
    // Getter
    /////////////////////////////////////
    public boolean isHov() {
        return "H".equals(getCategory()) || "HT".equals(getCategory());
    }

    public int getDetectorId() {
        return this.detector_id;
    }

    public double getFieldLength() {
        double f = getPropertyDouble(InfraProperty.field);
        if(f < 0) return 22;
        return f;
    }

    public void setRNode(RNode r_node) {
        this.r_node = r_node;
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    public boolean isWavetronics() {
        /*
         * Modify 
         * soobin Jeon 02/13/2012
         */
        if (this.r_node == null)
            return false;
        else
            return this.r_node.isWavetronicsStation();
        //return false;
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getDensity() {
        return toDoubleArray(adjustInterval(density,AdjustType.DensityWithStation));
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getFlow() {
        return toDoubleArray(adjustInterval(flow,AdjustType.Flow));
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getOccupancy() {
        return toDoubleArray(adjustInterval(occupancy,AdjustType.DensityWithStation));
    }
    
    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getScan() {
        return toDoubleArray(adjustInterval(scan,AdjustType.Average));
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getSpeed() {
        return toDoubleArray(adjustInterval(speed,AdjustType.Average));
    }
    
    /*
     * modify soobin Jeon 02/13/2012
     */
    public double[] getVolume() {
        return toDoubleArray(adjustInterval(volume,AdjustType.Cumulative));
    }

    public double[] getData(TrafficType trafficType) {
        return toDoubleArray(dispatchData(trafficType));
    }
    
    
    public Vector<Double> getDataVector(TrafficType trafficType) {
        return (Vector<Double>)dispatchData(trafficType).clone();
    }    

    public LaneType getLaneType() {
        return this.laneType;
    }

    /*
     * modify soobin Jeon 02/13/2012
     */
    private Vector<Double> dispatchData(TrafficType trafficType) {
        if (trafficType.isSpeed()) {
            return adjustInterval(speed,AdjustType.Average);
        }
        if (trafficType.isSppedForStation()){
            return speed;//adjustInterval(speed,AdjustType.SpeedWithStation);
        }
        if (trafficType.isDensity()) {
            return adjustInterval(density,AdjustType.DensityWithStation);
        }
        if (trafficType.isFlow() || trafficType.isFlowForAverage() || trafficType.isAverageFlow()) { //modify soobin Jeon 02/15/2012
            return adjustInterval(flow,AdjustType.Flow);
        }
        if (trafficType.isOccupancy()) {
            return adjustInterval(occupancy,AdjustType.DensityWithStation);
        }
        if (trafficType.isVolume()) {
            return adjustInterval(volume,AdjustType.Cumulative);
        }
        if (trafficType.isScan()) {
            return adjustInterval(scan,AdjustType.Average);
        }
        return null;
    }

    public String[] getTimeLine() {
        return this.period.getTimeline();
    }

    public Station getStation() {
        if (this.r_node == null || !this.r_node.isStation()) {
            return null;
        }

        return (Station) r_node;
    }

    public RNode getRNode() {
        return r_node;
    }

    public int getDataSize() {
        return this.speed.size();
    }

    public int getLane() {        
        return Math.min(getPropertyInt(InfraProperty.lane), 0);
    }

    public int compareTo(Object o) {
        return (getLane() - ((Detector) o).getLane());
    }

    public boolean isAbandoned() {
        return "t".equals(getProperty(InfraProperty.abandoned));
    }
    public boolean isStationOrCD(){
        return ("".equals(getCategory()) || "CD".equals(getCategory()));
//                return this.laneType.isStationOrCD();
//        return "t".equals(getProperty(InfraProperty.))
    }
    
    public String getCategory() {
        String cat = getProperty(InfraProperty.category);
        return (cat == null ? "" : cat);
    }

    public double getConfidence() {
        if (this.confidence < 0) {
            int validCount = 0;
            for (double u : this.speed) {
                if (u > 0) {
                    validCount++;
                }
            }
            this.confidence = (double) validCount / this.speed.size() * 100;
        }
        return this.roundUp(this.confidence, 1);
    }

    public boolean isMissing() {
        this.confidence = getConfidence();
        if (confidence < 50) {
//            System.out.println("confidence : " + confidence);
            return true;
        } else {
            return false;
        }
    }

    public boolean isAuxiliary() {
        return ("A".equals(getCategory()));
    }

    
    

    /**
     * Saves traffic data to disk
     */
    private void saveCache() {

        Detector.detectorCache.putData(this, period);
    }
    
    /**
     * @deprecated 
     */
    private void saveCache_File() {
        try {
            new File(cachePath).mkdir();
            saveCacheFile(cachePath + File.separator + this.id + "_volume_" + period.getPeriodString(), volume);
            saveCacheFile(cachePath + File.separator + this.id + "_scan_" + period.getPeriodString(), scan);
            saveCacheFile(cachePath + File.separator + this.id + "_flow_" + period.getPeriodString(), flow);
            saveCacheFile(cachePath + File.separator + this.id + "_speed_" + period.getPeriodString(), speed);
            saveCacheFile(cachePath + File.separator + this.id + "_density_" + period.getPeriodString(), density);
            saveCacheFile(cachePath + File.separator + this.id + "_occupancy_" + period.getPeriodString(), occupancy);
        } catch (Exception ex) {
        }        
    }

    /**
     * Saves traffic data to disk
     * @deprecated 
     * @param filepath
     * @param data
     * @throws Exception 
     */
    private void saveCacheFile(String filepath, Vector<Double> data) throws Exception {
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        StringBuilder sb = new StringBuilder(data.size());
        for (Double d : data) {
            sb.append(d.toString() + ",");
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    /**
     * Loads detector cache
     * @return is it loaded?
     * @throws OutOfMemoryError 
     */
    private boolean loadCache() throws OutOfMemoryError {
        if(detectorCache == null) {
            detectorCache = new DetectorCache();
            return false;
        }
        if(!detectorCache.hasCache(this.getId(), period, TrafficType.VOLUME)) return false;
        
        this.clear();
        
        volume.addAll(detectorCache.getData(this.getId(), period, TrafficType.VOLUME));
        speed.addAll(detectorCache.getData(this.getId(), period, TrafficType.SPEED));
        flow.addAll(detectorCache.getData(this.getId(), period, TrafficType.FLOW));
        occupancy.addAll(detectorCache.getData(this.getId(), period, TrafficType.OCCUPANCY));
        density.addAll(detectorCache.getData(this.getId(), period, TrafficType.DENSITY));
        scan.addAll(detectorCache.getData(this.getId(), period, TrafficType.SCAN));
        
        return true;
    }
    
    /**
     * @deprecated 
     * @return 
     */
    private boolean loadCache_File() {        
        try {
            this.clear();
            new File(cachePath).mkdir();
            if (loadCacheFile(cachePath + File.separator + this.id + "_volume_" + period.getPeriodString(), volume)
                    && loadCacheFile(cachePath + File.separator + this.id + "_scan_" + period.getPeriodString(), scan)
                    && loadCacheFile(cachePath + File.separator + this.id + "_flow_" + period.getPeriodString(), flow)
                    && loadCacheFile(cachePath + File.separator + this.id + "_speed_" + period.getPeriodString(), speed)
                    && loadCacheFile(cachePath + File.separator + this.id + "_density_" + period.getPeriodString(), density)
                    && loadCacheFile(cachePath + File.separator + this.id + "_occupancy_" + period.getPeriodString(), occupancy)) {
                return true;
            } else {
                return false;
            }
        } catch (OutOfMemoryError ex) {
            System.out.println("Exception Occured : " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            return false;
        }        
    }

    /**
     * Loads a detector cache file
     * @deprecated 
     * @param filepath
     * @param data
     * @return
     * @throws Exception 
     */
    private boolean loadCacheFile(String filepath, Vector<Double> data) throws Exception {
        if (!new File(filepath).exists()) {
            return false;
        }
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader reader = new BufferedReader(new FileReader(filepath));

        char[] chars = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(chars)) > -1) {
            sb.append(String.valueOf(chars, 0, numRead));
        }
        reader.close();
        String[] arr = sb.toString().split(",");
        for (String s : arr) {
            data.add(Double.parseDouble(s));
        }
        return true;
    }
    
    public String getLabel() {
        return this.property.getProperty("label");
    }
    
    public String getOriginId(){
        if(dType.isTempDetector())
            return this.id.split("D")[1];
        else
            return this.id;
    }
}
