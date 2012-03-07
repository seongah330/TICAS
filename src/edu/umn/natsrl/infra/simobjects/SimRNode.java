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

    public SimDetector[] getDetectors(IDetectorChecker dc) {        
        Detector[] dets = this.rnode.getDetectors(dc);
        SimDetector[] simDets = new SimDetector[dets.length];
        for(int i=0; i<dets.length; i++) {
            simDets[i] = simObjects.getDetector("D"+dets[i].getDetectorId());
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
            if(value <= 0) continue;
            sum += value;
//            if("S33".equals(this.rnode.getStationId()) && type.isFlow()) {
//                System.out.println("  - S33 : " + d.getId() + "'s q("+period+") = " + value);
//            }
            validCount++;
        }        
        
        if(validCount > 0) {
//            if("S911".equals(this.rnode.getStationId()) && type.isSpeed()) {
//                System.out.println("  - S911's speed = " + (sum/validCount));
//            }
            /*modify by soobin Jeon 2012/01/23
             * Volume Data is not applied to division.
             */
            if(type.isVolume() || type.isFlow())
                return sum;//validCount;
            else
                return sum/validCount;
        }
        else return -1;
    }
    
    @Override
    public void reset() {
        detectors.clear();
    }
    
}
