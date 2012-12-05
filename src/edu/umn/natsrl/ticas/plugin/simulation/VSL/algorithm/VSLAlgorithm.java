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
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLStationState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLAlgorithm{
    
    ArrayList<VSLStationState> stationstates;
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
    
    public VSLAlgorithm(ArrayList<VSLStationState> _stationstate, Section _section){
        stationstates = _stationstate;
        section = _section;
    }
    
    public void Process(){
        FindBottleneck();
        setDMS();
    }

    /**
     * Find Bottleneck
     */
    private void FindBottleneck() {
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
        System.out.println("setDMS");
        for(DMSImpl dms : cdms){
            System.out.println(dms.getId()+"("+dms.getMilePoint(section.getName())+")==");
            System.out.println("findStation");
            VSStationFinder vss_finder = new VSStationFinder(dms.getMilePoint(section.getName()));
            findStation(vss_finder);
            
            //Check VSA state
            
            //set VSS State
            Integer setSpeed = null;
            if(vss_finder.foundVSS()){
                Integer lim = vss_finder.getSpeedLimit();
                System.out.print("--FNST : "+vss_finder.getVSS().getID()+", spdlimit : "+lim);
                if(lim != null){
                    Double a = vss_finder.calculateSpeedAdvisory();
                    System.out.print(", speedadvisory : "+a);
                    if(a != null){
                        a = Math.max(a, getMinDisplay());
                        int sa = round5Mph(a);
                        if(sa < lim && sa <= getMaxDisplay()){
                            setSpeed =sa;
                        }else{
                            setSpeed = null;
                        }
                    }
                    if(setSpeed != null){
                        System.out.println(", speed : "+setSpeed);
                    }else{
                        System.out.println(", speed : null");
                    }
                }
            }
            
            dms.setVSA(setSpeed); //not Implements
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
}
