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

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSStationFinderNew extends VSStationFinder{
    public VSStationFinderNew(Double m){
        super(m);
    }

    /**
     * Updated
     * @param m
     * @param s
     * @return 
     */
    @Override
    public boolean check(Double m, VSLStationState s) {
//        System.out.print(s.getID()+"-"+s.getMilePoint()+", "+m+", ma="+ma);
        if(m < ma){
            su = s;
            mu = m;
        }else if(md == null || md > m){
            sd = s;
            md = m;
        }
        
        if((vss_mp == null || vss_mp < m) && s.isBottleneckFor(m - ma)){
            vss = s;
            vss_mp = m;
        }
//        if(su != null)
//            System.out.print("su:"+su.getID());
//        if(sd != null)
//            System.out.print(", sd:"+sd.getID());
//        if(vss != null)
//            System.out.print(", vss:"+vss.getID());
//        if(vss_mp != null)
//            System.out.println(", vssmp:"+vss_mp);
//        else
//            System.out.println();
        return false;
    }
    
    /** Calculate a speed advisory.
    * @param spd Average speed at VSS.
    * @param d Distance upstream of station.
    * @return Speed advisory. */
    @Override
    protected Double calculateSpeedAdvisory(double spd, double d){
        if(d > 0){
            int acc = -vss.getControlThreshold();
            double s2 = spd * spd + 2.0 * acc * d;
//            System.out.print(", acc="+acc+", spd="+spd+", spd*spd="+(spd*spd)+", d="+d+" s2="+s2+", set vss : "+vss.getID());
            if(s2 < 0){
                return null;
            }
//            System.out.println(", adv="+(double)Math.sqrt(s2));
            return (double)Math.sqrt(s2);
        }else{
            return spd;
        }
    }
//    /** Calculate the advisory speed */
//    @Override
//    public Double calculateSpeedAdvisory() {
//        Double splimit = null;
//        if(vss != null && vss_mp != null) {
//            Double spd = vss.getAggregateRollingSpeed();
////            System.out.print(", sppd : "+spd);
//            if(spd > 0){
//                splimit = calculateSpeedAdvisory(spd, vss_mp - ma);
//            }
//        }
//        
//        if(splimit == null){
//            return null;
//        }
//        
//        Double sdSpeed = sd.getAggregateRollingSpeed();
//        if(!sd.getID().equals(vss.getID()) &&
//            sdSpeed < splimit){
//            splimit = null;
//        }
//        
//        return splimit;
//    }
}
