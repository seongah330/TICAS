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

import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Chongmyung Park
 */
public class SimRNode extends SimObject {
    
    protected RNode rnode;
    protected HashMap<String, SimDetector> detectors = new HashMap<String, SimDetector>();
    SimObjects simObjects = SimObjects.getInstance();
    
    public SimRNode(RNode rnode) {
        this.rnode = rnode;
        for(SimDetector sd : getDetectors(null)) {
            this.detectors.put(sd.getId(), sd);
        }
    }

    public ArrayList<SimDetector> getDetectors(IDetectorChecker dc) {        
        Detector[] dets = this.rnode.getDetectors(dc);
//        SimDetector[] simDets = new SimDetector[dets.length];
        ArrayList<SimDetector> simDets = new ArrayList<SimDetector>();
        for(int i=0; i<dets.length; i++) {
//            SimDetector tempsim = simObjects.getDetectorWithoutNull("D"+dets[i].getDetectorId());
            SimDetector tempsim = simObjects.getDetectorWithoutNull(""+dets[i].getDetectorId());
            if(tempsim != null)
                simDets.add(tempsim);
        }
        return simDets;
    }
        
    public double getData(IDetectorChecker checker, TrafficType type) {
        return getData(checker, type, 0);
    }
    
    /**
     * Returns data before given period time step
     */
    public double getData(IDetectorChecker checker, TrafficType type, int period) {

        double sum = 0;
        int idx = 0;
        int validCount = 0;
        for(SimDetector d : this.detectors.values())
        {
            if(checker != null && !checker.check(d.detector)) continue;
            double value = d.getData(type, period);

            if(d.detector.isAbandoned() || !d.detector.isStationOrCD()) continue;
            if(value <= 0) continue;
            sum += value;
            //debug
//            if("S316".equals(this.rnode.getStationId())) {
//            if(type.isDensity()) {
//                System.out.println("  - "+this.rnode.getStationId()+" : " + d.getId() + "'s q("+period+"), length("+d.detector.isAbandoned()+")"
//                        + "(isstation:"+d.detector.isStationOrCD()+")"
//                        + " = " + value
//                        + " u : " + d.getData(TrafficType.SPEED,period)
//                         + " q : " + d.getData(TrafficType.FLOW,period)
//                        + " s : " + d.getData(TrafficType.SCAN,period)
//                        + " s : " + d.detector.getFieldLength());
//            }
//            }
            validCount++;
        }        
        
        if(validCount > 0) {
                if(type.isFlow() || type.isVolume())
                    return sum;
                else
                    return sum/validCount;
        }
        else return -1;
    }
    
    /**
     * Returns data before given period time step
     */
    public double getDataForDebug(IDetectorChecker checker, TrafficType type) {
        return getDataForDebug(checker, type, 0);
    }
    public double getDataForDebug(IDetectorChecker checker, TrafficType type, int period) {

        double sum = 0;
        int idx = 0;
        int validCount = 0;
        for(SimDetector d : this.detectors.values())
        {
            if(checker != null && !checker.check(d.detector)) continue;
            double value = 0;
            if(type.isDensity() || type.isSpeed()){
                value = d.getData(TrafficType.DENSITY, period);
                if(value <= 0)
                    value = 0;
                else{
                    double s = ( value * d.detector.getFieldLength()  / 5280 * 1800 );
                    short ss = (short)Math.round(s);
                    double rek = ss*5280/(d.detector.getFieldLength()*1800);

                    if(type.isDensity())
                        value = rek;
                    else if(type.isSpeed())
                        value = d.getData(TrafficType.FLOW,period) == 0 ? 0 : d.getData(TrafficType.FLOW,period) / rek;
    //                if("S302".equals(this.rnode.getStationId())) {
    //                    System.out.println("  - S302 : " + d.getId() + "'s q("+period+"), length("+d.detector.getFieldLength()+") = " + value + "("+s+":"+ss+")");
    //                }
//                    if(d.detector.getStation() != null && type.isSpeed())
//                        System.out.println(d.detector.getStation().getStationId()+":" + d.getId() + "'s q("+period+"), length("+d.detector.getFieldLength()+") = " + value);
                }
            }else
                value = d.getData(type, period);
            
            if(value <= 0) continue;
            sum += value;
            validCount++;
        }        
        
        if(validCount > 0) {
            /*modify by soobin Jeon 2012/01/23
             * Volume Data is not applied to division.
             */
//            if(type.isVolume() || type.isFlow())
//                return sum;//validCount;
//            else
                return sum/validCount;
        }
        else return -1;
    }
    
    @Override
    public void reset() {
        detectors.clear();
    }
    
}
