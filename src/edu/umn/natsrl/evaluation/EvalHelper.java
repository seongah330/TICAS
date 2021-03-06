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

/*
 * EvalHelper.java
 *
 * Created on Jun 16, 2011, 11:30:50 AM
 */
package edu.umn.natsrl.evaluation;

import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.types.StationType;
import java.util.ArrayList;

/**
 *
 * @author Chongmyung Park
 */
public class EvalHelper {
    
    final static int FEET_PER_MILE = 5280;
    
    /**
     * Return upstream station name that has same speed and density value of given virtual station
     * @param col virtual station column in evaluation result
     * @param uRes speed results including virtual stations
     * @param kRes density results including virtual stations
     * @return station name
     */
    public static String getSamePreviousStation(int col, EvaluationResult uRes, EvaluationResult kRes) {
        if(col <= uRes.COL_DATA_START()) return null;        
        int row = uRes.ROW_DATA_START();
        for(int c=col-1; c>=uRes.COL_DATA_START(); c--) {
            String sName = uRes.get(c, uRes.ROW_TITLE()).toString();
            if("-".equals(sName)) continue;
            
            if( uRes.get(col, row).equals(uRes.get(c, row)) && kRes.get(col, row).equals(kRes.get(c, row))) 
                return getStationNameFromTitle(uRes.get(c, uRes.ROW_TITLE()).toString());
            else {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Return downstream station name that has same speed and density value of given virtual station
     * @param col virtual station column in evaluation result
     * @param uRes speed results including virtual stations
     * @param kRes density results including virtual stations
     * @return station name
     */    
    public static String getSameNextStation(int col, EvaluationResult uRes, EvaluationResult kRes) {
        if(col >= uRes.getColumnSize()-1) return null;
        int row = uRes.ROW_DATA_START();
        for(int c=col+1; c<uRes.getColumnSize(); c++) {
            String sName = uRes.get(c, uRes.ROW_TITLE()).toString();
            if("-".equals(sName)) continue;            
            if( uRes.get(col, row).equals(uRes.get(c, row)) && kRes.get(col, row).equals(kRes.get(c, row))) 
                return getStationNameFromTitle(uRes.get(c, uRes.ROW_TITLE()).toString());
            else return null;
        }
        return null;
    }    

    public static String getPrevStation(int col, EvaluationResult uRes, EvaluationResult kRes) {
        if(col <= uRes.COL_DATA_START()) return null;
        for(int c=col-1; c>=uRes.COL_DATA_START(); c--) {
            String sName = uRes.get(c, uRes.ROW_TITLE()).toString();
            if("-".equals(sName)) continue;
            return getStationNameFromTitle(uRes.get(c, uRes.ROW_TITLE()).toString());
        }
        return null;
    }    
    
    public static String getNextStation(int col, EvaluationResult uRes, EvaluationResult kRes) {
        if(col >= uRes.getColumnSize()-1) return null;

        for(int c=col+1; c<uRes.getColumnSize(); c++) {
            String sName = uRes.get(c, uRes.ROW_TITLE()).toString();
            if("-".equals(sName)) continue;            
            return getStationNameFromTitle(uRes.get(c, uRes.ROW_TITLE()).toString());

        }
        return null;
    }    
    
    /**
     * Return station that has given name
     * @param stations station array
     * @param name station name to find
     * @return station
     */
    public static Station getStation(Station[] stations, String name) {
        for(Station s : stations) {
            if(s.getStationId().equals(name)) return s;
        }
        return null;
    }

    /**
     * Return interpolated flow data
     * @param row row in flow data
     * @param stationName target station name
     * @param stations station array
     * @param dc detector checker
     * @return interpolated flow
     */
    public static double interpolateFlow(int row, String stationName, Station[] stations, IDetectorChecker dc) {
        
        Station nextStation = null, prevStation = null;
        for(int i=0; i<stations.length; i++) {
            Station s = stations[i];
            if(stationName.equals(s.getStationId())) {
                if(i > 0) prevStation = stations[i-1];
                if(i < stations.length-1) nextStation = stations[i+1];
                break;
            }
        }
        
        double[] nextFlow=null, prevFlow=null;
        if(prevStation != null) prevFlow = prevStation.getFlow(dc);
        if(nextStation != null) nextFlow = nextStation.getFlow(dc);
        
        if(prevStation != null && nextStation != null) return ( prevFlow[row] + nextFlow[row] ) / 2;
        else if(prevStation == null) return nextFlow[row];
        else if(nextStation == null) return prevFlow[row];
        else return -1;
    }
    
    /*
     * soobin Jeon 02/14/2012
     */
    public static double interpolateFlowtoZero(){
        return 0;
    }
    
    /**
     * Returns station label that will be used in EvaulationResult
     * @param rnode
     * @param checker
     * @return 
     */    
    public static String getStationLabel(RNode rnode, IDetectorChecker checker) {        
        String label;
        int AuxiliaryLanes = getAuxiliaryLanes(rnode,checker);
        label = rnode.getLabel() + " (" + rnode.getStationId() + ") " + getLanes(rnode, checker);
        if(AuxiliaryLanes > 0)
            label += "(Aux="+AuxiliaryLanes+")";
        label += " lanes";
        return label;
    }    
    
    
    public static int getLanes(String stationName, Station[] stations, IDetectorChecker dc) {
        for(Station s : stations) {
            if(stationName.equals(s.getStationId())) {
                return getLanes(s, dc);
            }
        }
        return -1;
    }
    
    /**
     * Return number of lane except missing detector, which confidence is less than 50
     * @param rnode roadway node
     * @param dc detector checker
     * @return number of lane
     */
    /*
     * modify soobin Jeon 02/15/2012
     */
    public static int getLanes(RNode rnode, IDetectorChecker dc) {
        int lanes = 0;
        for(Detector d : rnode.getDetectors(dc)) {
            if(!d.isMissing()){
                lanes++;
            }
//            lanes++;
        }
        if(lanes == 0) return rnode.getLanes(dc);
        return lanes;
    }
    public static int getAuxiliaryLanes(RNode rnode, IDetectorChecker dc){
        int lanes = 0;
        for(Detector d : rnode.getDetectors(dc)) {
            if(!d.isMissing() && d.isAuxiliary()){
                lanes++;
            }
//            lanes++;
        }
        return lanes;
    }
    
    /**
     * Return detector list except missing detector, which confidence is less than 50
     * @param rnode
     * @param dc
     * @return 
     */
    /*  
     * modify soobin Jeon 02/15/2012
     */
    public static Detector[] getDetectorsWithoutMissing(RNode rnode, IDetectorChecker dc) {
        Detector[] dets = rnode.getDetectors(dc);
        ArrayList<Detector> availableDets = new ArrayList<Detector>();
        for(Detector d : dets) {
            if(d.isMissing()) continue;
            availableDets.add(d);
        }
        return availableDets.toArray(new Detector[availableDets.size()]);
    }

    /**
     * Rounds up number with precise
     * @param n number
     * @param precision dropped as a shortcut to rounding the value
     * @return number, round-up the given number
     */
    public static double roundUp(double n, int precision) {
        return (double) (Math.round(n * Math.pow(10, precision)) / Math.pow(10, precision));
    }    
    
    /**
     * Returns distance in mile to downstream station of given station
     * @param station Station
     * @return distance in mile
     */
    public static double getDistance(String sectionName, Station station) {
        return roundUp((double) station.getDistanceToDownstreamStation(sectionName) / FEET_PER_MILE, 1);
    }

    /**
     * Return station name such as 'S43'
     * @param title
     * @return 
     */
    public static String getStationNameFromTitle(String title) {
        String index = StationType.getStationNameFromTitle(title,"(");
        return title.substring(title.indexOf(index)+1, title.indexOf(")"));        
    }

    /**
     * Returns speed limit of station
     * @param stations station list
     * @param stationName name of station
     * @return speed limit
     */
    public static double getSpeedLimit(Station stations[], String stationName)
    {
        return getStation(stations, stationName).getSpeedLimit();
    }    
    
}
