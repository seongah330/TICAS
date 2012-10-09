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

import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.ticas.Simulation.SimRampMeter;
import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class FixedMeter extends SimRampMeter{
    protected int interval = 30;

    public FixedMeter(SimMeter _meter, int timeInterval){
        super(_meter);
        interval = timeInterval;
    }
    
    /**
     * Add Rate
     * @param _data 
     */
    public void AddRate(double _data){
        rate.add(_data);
    }
    /**
     * Clear Rate
     */
    public void ClearRate(){
        rate.clear();
    }
    
    public List<String> getRateLists(){
        List<String> l = new ArrayList<String>();
        for(double d : rate){
            l.add(String.valueOf(d));
        }
        return l;
    }
    public void setRateList(List<String> lists){
        ClearRate();
        for(String s : lists)
            rate.add(Double.parseDouble(s));
    }
    /**
     * set Rate
     * @param currentStep 
     */
    public void setFixedRate(int step) {
        double rnext = 0;
        int currentStep = step / interval;
        
        if(currentStep >= rate.size()) {
            rnext = getLastRate();
        }
        else {
            rnext = rate.get(currentStep);
        }
        
        setRate(rnext);
    }
    
    public double getLastRate(){
        if(lastRate == 0)
            return SimulationConfig.MAX_RATE;
        else
            return lastRate;
        
    }
}
