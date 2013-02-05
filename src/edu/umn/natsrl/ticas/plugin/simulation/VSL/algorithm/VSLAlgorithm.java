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
package edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.ticas.Simulation.StationState;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLAlgorithm{
    
    private boolean isDebug = true;
    private int processCnt = 0;
    VSLVersion vslversion;
    ArrayList<VSLStationState> stationstates;
    TreeMap<Double, VSLStationState> StationMap;
    Section section;
    
    /** Get the minimum speed to display for advisory */
    static private double getMinDisplay() {
            return VSLConfig.VSL_MIN_SPEED;
    }

    /** Get the maximum speed to display for advisory */
    static private double getMaxDisplay() {
            return VSLConfig.VSL_MAX_SPEED;
    }

    /** Round up to the nearest 5 mph */
    static private int round5Mph(double mph) {
            return (int)Math.round(mph / 5) * 5;
    }
    
    public VSLAlgorithm(ArrayList<VSLStationState> _stationstate, Section _section, VSLVersion vv, TreeMap<Double, VSLStationState> _map){
        stationstates = _stationstate;
        section = _section;
        vslversion = vv;
        StationMap = _map;
    }
    
    public void Process(){
        Debug();
        //Execute FindBottleneck according to VSL version
        if(vslversion.getSID() > 2){
            FindBottleneck();
        }else{
            FindBottleneck_old();
        }
        setDMS();
        processCnt ++;
    }
    
    private void FindBottleneck(){
        final TreeMap<Double, VSLStationState> upstream = 
                new TreeMap<Double, VSLStationState>();
        for(VSLStationState vs : StationMap.values()){
            if(vs.getAggregateRollingSpeed() > 0){
                upstream.put(vs.getMilePoint(), vs);
                vs.calculateBottleneck(vs.getMilePoint(), upstream);
                vs.calculateControlThreshold(vs.getMilePoint(),upstream);
            }else{
                vs.clearBottleneck();
            }
        }
    }

    /**
     * Find Bottleneck
     */
    private void FindBottleneck_old() {
        for(VSLStationState vs : stationstates){
            if(vs.getAggregateRollingSpeed() > 0){
                vs.calculateBottleneck();
            }else{
                vs.clearBottleneck();
            }
        }
    }

    /**
     * Adjust VSS to DMS
     * set DMA speed limit
     */
    private void setDMS() {
        List<DMSImpl> cdms = section.getDMS();
        for(DMSImpl dms : cdms){
            VSStationFinder vss_finder = vslversion.getVSStationFinder(dms.getMilePoint(section.getName()));
            double aVSA = 0;
//            System.out.println("Find Station for DMS"+"-"+dms.getId());
            
            findStation(vss_finder);
//            System.out.print(dms.getId() + " : ");
            //Check VSA state
            //set VSS State
            Integer setSpeed = null;
            if(vss_finder.foundVSS()){
//                System.out.println(" - Found!!");
                Integer lim = vss_finder.getSpeedLimit();
                if(lim != null){
                    Double a = vss_finder.calculateSpeedAdvisory();
                    aVSA = a;
                    if(a != null){
                        a = Math.max(a, getMinDisplay());
                        int sa = round5Mph(a);
                        if(sa < lim && sa <= getMaxDisplay()){
                            setSpeed =sa;
                        }else{
                            setSpeed = null;
                        }
                    }
                }
            }
            
            dms.setVSA(setSpeed);
            dms.setActualVSA(aVSA);
        }
    }
    
    private VSLStationState findStation(VSStationFinder finder){
        for(VSLStationState s : stationstates){
            Double m = s.getMilePoint();
            if(m == null){
                continue;
            }
            //check Station info
            if(true){
                if(s != null && finder.check(m, s)){
                    return s;
                }
            }
        }
        return null;
    }

    private void Debug() {
        if(!isDebug)
            return;
        System.out.println();
        System.out.println("Step-"+this.processCnt+".... Processing..");
    }
}
