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

import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLConfig;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSStationFinder {
    static private int getControlThreshold(){
        return -1 * VSLConfig.VSL_CONTROL_THRESHOLD;
    }
    
    /**
     * Mile point to search for VSS
     */
    protected final Double ma;
    
    /**
     * upstream station
     */
    protected VSLStationState su;
    
    /**
     * upstream mile point
     */
    protected Double mu;
    
    /**
     * downstream station
     */
    protected VSLStationState sd;
    
    /**
     * downstream mile point
     */
    protected Double md;
    
    /**
     * Found VSS
     */
    protected VSLStationState vss;
    
    /**
     * Mile Point at found vss
     */
    protected Double vss_mp;
    
    public VSStationFinder(Double m){
        ma = m;
    }
    
    public boolean check(Double m, VSLStationState s){
        if(m < ma){
            su = s;
            mu = m;
        }else if(md == null || md > m){
            sd = s;
            md = m;
        }
        
        if((vss_mp == null || vss_mp > m) && s.isBottleneckFor(m - ma)){
            vss = s;
            vss_mp = m;
        }
        return false;
    }
    
    /** Check if a valid VSS was found */
    public boolean foundVSS() {
            return vss != null;
    }
    
    public VSLStationState getVSS(){
        return vss;
    }
    
    /** Get the speed limit */
    public Integer getSpeedLimit() {
            if(su != null && sd != null){
                    return Math.min(su.getSpeedLimit(), sd.getSpeedLimit());
            }
            else if(su != null){
                    return su.getSpeedLimit();
            }
            else if(sd != null){
                    return sd.getSpeedLimit();
            }
            else{
                    return null;
            }
    }
    
    /** Calculate the advisory speed */
    public Double calculateSpeedAdvisory() {
        if(vss != null && vss_mp != null) {
            Double spd = vss.getAggregateRollingSpeed();
            if(spd > 0){
                return calculateSpeedAdvisory(spd, vss_mp - ma);
            }
        }
        return null;
    }
    
    /** Calculate a speed advisory.
    * @param spd Average speed at VSS.
    * @param d Distance upstream of station.
    * @return Speed advisory. */
    protected Double calculateSpeedAdvisory(double spd, double d){
        if(d > 0){
            int acc = -getControlThreshold();
            double s2 = spd * spd + 2.0 * acc * d;
//            System.out.print(", acc="+acc+", s2="+s2);
            if(s2 < 0){
                return null;
            }
//            System.out.print(", adv="+(double)Math.sqrt(s2));
            return (double)Math.sqrt(s2);
        }else{
            return spd;
        }
    }
    
    /** Debug the finder */
    public void debug() {
            double a = calculateSpeedAdvisory();
            System.out.println("adv: " + a +
                        ", upstream: " + su +
                        ", downstream: " + sd +
                        ", vss: " + vss +
                        ", speed: " + getSpeed() +
                        ", limit: " + getSpeedLimit());
    }
    
    /** Get the speed*/
    protected Double getSpeed(){
        if(su != null && sd != null){
            double u0 = su.getAggregateRollingSpeed();
            double u1 = sd.getAggregateRollingSpeed();
            if(u0 > 0 && u1 > 0){
                return Math.min(u0,u1);
            }
            if(u0 > 0){
                return u0;
            }
            if(u1 > 0){
                return u1;
            }
        }
        return null;
    }
}
