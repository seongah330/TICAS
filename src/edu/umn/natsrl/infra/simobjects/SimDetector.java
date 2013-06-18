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

package edu.umn.natsrl.infra.simobjects;

import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class SimDetector extends SimObject {

    private int detectorId;
    public Detector detector;
    final int FEET_PER_MILE = 5280;
    final int MAX_SCANS = 1800;
    
    private Vector<Double> volume = new Vector<Double>();
    private Vector<Double> speed = new Vector<Double>();
    private Vector<Double> flow = new Vector<Double>();
    private Vector<Double> occupancy = new Vector<Double>();
    private Vector<Double> density = new Vector<Double>();
    private Vector<Double> scan = new Vector<Double>();
    
    private Date startTime;
    
    //for AVG
    private int avgCnt = 0;
    
    public SimDetector(Detector detector) {
        this.detector = detector;
        this.id = detector.getId();
        this.detectorId = detector.getDetectorId();
        this.type = SimDeviceType.DETECTOR;
        this.avgCnt = 0;
    }
    
    public int getAvg(){
        return avgCnt == 0 ? 1 : avgCnt;
    }
    /**
     *
     * @return 30s volume from saved data
     */
    public double getRecentVolume()
    {
        if(this.volume.isEmpty()) return 0;
        return this.volume.lastElement() / getAvg();
    }
    
    public double getRecentVolume(int idx)
    {
        if(this.volume.isEmpty()) return 0;
        return this.volume.get(idx) / getAvg();
    }    

    /**
     *
     * @return 30s flow from saved data
     */
    public double getRecentFlow()
    {
        if(this.flow.isEmpty()) return 0;
        return this.flow.lastElement() / getAvg();
    }

    /**
     *
     * @return 30s speed from saved data
     */
    public double getRecentSpeed()
    {
        if(this.speed.isEmpty()) return 0;
        return this.speed.lastElement() / getAvg();
    }

    /**
     *
     * @return 30s density from saved data
     */
    public double getRecentDensity()
    {
        if(this.density.isEmpty()) return 0;
        return this.density.lastElement() / getAvg();
    }
    
    public double getRecentDensity(int idx)
    {
        if(this.density.isEmpty()) return 0;
        return this.density.get(idx) / getAvg();
    }   
    
    /**
     *
     * @return 30s density from saved data
     */
    public double getRecentScan()
    {
        if(this.scan.isEmpty()) return 0;
        return this.scan.lastElement() / getAvg();
    }
    
    public double getRecentScan(int idx)
    {
        if(this.scan.isEmpty()) return 0;
        return this.scan.get(idx) / getAvg();
    }     

    /**
     * this is for simulation scan data
     * @return 30s scan data calculated from density
     */
    public double getRecentScanFromDensity() {
        return calculateScan(getRecentDensity()) / getAvg();
    }   
    
    public double getRecentScanFromDensity(int idx) {
        return calculateScan(getRecentDensity(idx)) / getAvg();
    }      
    
    private Double calculateScan(double k) {
        double s = 0;
        double fieldlength = this.detector.getFieldLength();
        
        s = ( k * fieldlength  / FEET_PER_MILE * MAX_SCANS );
        s = ( s > MAX_SCANS ? MAX_SCANS : s);
        return s / getAvg();
    }

    public Vector<Double> getSpeed() {
        if(avgCnt == 0)
            return this.speed;
        else{
            return getAvgData(this.speed);
        }
    }

    public Vector<Double> getDensity() {        
        if(avgCnt == 0)
            return this.density;
        else{
            return getAvgData(this.density);
        }
    }

    public Vector<Double> getFlow() {
        if(avgCnt == 0)
            return this.flow;
        else{
            return getAvgData(this.flow);
        }
    }

    public Vector<Double> getOccupancy() {
        if(avgCnt == 0)
            return this.occupancy;
        else{
            return getAvgData(this.occupancy);
        }
    }

    public Vector<Double> getScan() {
        if(avgCnt == 0)
            return this.scan;
        else{
            return getAvgData(this.scan);
        }
    }

    public Vector<Double> getVolume() {
        if(avgCnt == 0)
            return this.volume;
        else{
            return getAvgData(this.volume);
        }
    }

    public double getData(TrafficType trafficType) {
        return getData(trafficType, 0);
    }
    
    /**
     * Returns data before given prevStep time step
     */    
    public double getData(TrafficType trafficType, int prevStep) {
        Vector<Double> data = dispatchData(trafficType);
        int idx = data.size()-prevStep-1;
        if(idx < 0 || idx > data.size()-1) return -1;
        return data.get(idx);
    }    

    private Vector<Double> dispatchData(TrafficType trafficType) {
        if (trafficType.isSpeed()) {
            return this.getSpeed();
        }
        if (trafficType.isDensity()) {
            return this.getDensity();
        }
        if (trafficType.isFlow() || trafficType.isAverageFlow()) {
            return this.getFlow();
        }
        if (trafficType.isOccupancy()) {
            return this.getOccupancy();
        }
        if (trafficType.isVolume()) {
            return this.getVolume();
        }
        if (trafficType.isScan()) {
            return this.getScan();
        }
        return null;
    }
//    private Vector<Double> dispatchData(TrafficType trafficType) {
//        if (trafficType.isSpeed()) {
//            return this.speed;
//        }
//        if (trafficType.isDensity()) {
//            return this.density;
//        }
//        if (trafficType.isFlow()) {
//            return this.flow;
//        }
//        if (trafficType.isOccupancy()) {
//            return this.occupancy;
//        }
//        if (trafficType.isVolume()) {
//            return this.volume;
//        }
//        if (trafficType.isScan()) {
//            return this.scan;
//        }
//        return null;
//    }
    
    public int getDetectorId() {
        return detectorId;
    }

    @Override
    public void reset() {
        volume.clear();
        speed.clear();
        flow.clear();
        occupancy.clear();
        density.clear();
        scan.clear();
        avgCnt = 0;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setTrafficData(TrafficType trafficType, double[] values) {
        Vector<Double> target = null;
        if(trafficType.isDensity()) target = this.density;
        else if(trafficType.isFlow()) target = this.flow;
        else if(trafficType.isOccupancy()) target = this.occupancy;
        else if(trafficType.isScan()) target = this.scan;
        else if(trafficType.isSpeed()) target = this.speed;
        else if(trafficType.isVolume()) target = this.volume;
        
        target.clear();
        for(double v : values) target.add(v);
    }
    
    public void addTrafficData(TrafficType trafficType, double[] values) {
        Vector<Double> target = null;
        if(trafficType.isDensity()) target = this.density;
        else if(trafficType.isFlow()) target = this.flow;
        else if(trafficType.isOccupancy()) target = this.occupancy;
        else if(trafficType.isScan()) target = this.scan;
        else if(trafficType.isSpeed()) target = this.speed;
        else if(trafficType.isVolume()) target = this.volume;
        
        System.err.println(this.id+" - vcnt : "+values.length);
        int cnt = 0;
        for(double v : values){
            if((trafficType.isDensity()&&avgCnt==0) || (!trafficType.isDensity()&&avgCnt==1))
                target.add(v);
            else{
                double temp = target.get(cnt);
                target.set(cnt, v+temp);
                cnt ++;
            }
        }
        if(trafficType.isDensity())
            avgCnt ++;
    }
    

    public void shrink(int len) {
        Object[] data = {volume, speed, flow, occupancy, density, scan };
        for(Object o : data) shrink((Vector<Double>)o, len);
    }
    
    private void shrink(Vector<Double> data, int len) {
        while(data.size() > len) {
            data.remove(data.size()-1);
        }
    }

    public void addData(double v, double q, double u, double k, double occupancy) {
        double scanData = calculateScan(k);
        double occData = scanData / InfraConstants.MAX_SCANS * 100;
        
        if(occupancy > 0) {
            occData = occupancy;
            scanData = occData * InfraConstants.MAX_SCANS / 100;
            k = occupancy * 5280 / this.detector.getFieldLength() / 100;
            u = q / k;
        }
        this.volume.add(v);
        this.flow.add(q);
        this.speed.add(u);
        this.density.add(k);
        this.scan.add(scanData);
        this.occupancy.add(occData);
    }
    
    public void addRealDatatoSim(double v, double q, double u, double k, double occupancy) {
        double scanData = calculateScan(k);
        this.volume.add(v);
        this.flow.add(q);
        this.speed.add(u);
        this.density.add(k);
        this.scan.add(scanData);
        this.occupancy.add(occupancy);
    }
    
    //for Debug
    public void addDataForDebug(double v, double q, double u, double k, double occupancy) {
        
        double scanData = Math.round(calculateScan(k));
//         double scanData = calculateScan(k);
        double occData = scanData / InfraConstants.MAX_SCANS * 100;
        
        if(occupancy > 0) {
            occData = occupancy;
            scanData = occData * InfraConstants.MAX_SCANS / 100;
            k = occupancy * 5280 / this.detector.getFieldLength() / 100;
            
            u = q / k;
        }
        
         //for debug
        if(k>0){
//            double s = ( k * this.detector.getFieldLength() / 5280 * 1800 );
//            short ss = (short)Math.round(s);
            k = ((double)scanData)*5280/(this.detector.getFieldLength()*1800);
            u = q==0 ? 0 : q/k;
        }else
            u = 0;
        
        this.volume.add(v);
        this.flow.add(q);
        this.speed.add(u);
        this.density.add(k);
        this.scan.add(scanData);
        this.occupancy.add(occData);        
    }

    private Vector<Double> getAvgData(Vector<Double> data) {
//        System.out.println("avgcnt : "+getAvg());
        Vector<Double> tmpv = new Vector<Double>();
        for(int i=0;i<data.size();i++){
            double temp = data.get(i);
            tmpv.add((double)Math.round(temp/getAvg()));
        }
        System.err.println(this.id+ " : orgin-"+data.size()+", re-"+tmpv.size());
        return tmpv;
    }
    
}
