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
package edu.umn.natsrl.evaluation;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class DVH extends Evaluation {

    @Override
    protected void init() {
        this.name = "DVH";
    }

    @Override
    protected void process() {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;        
        
        // get station speeds
        Vector<EvaluationResult> stationSpeed = Evaluation.getResult(StationSpeed.class, this.opts);
        
        // get VMT
        Vector<EvaluationResult> vmt = Evaluation.getResult(VMT.class, this.opts);             

        Period[] periods = this.opts.getPeriods();
        int idx = 0;
        
        // for all results, calculate DVH
        for(int i=0; i<stationSpeed.size(); i++)
        {            
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());
            
            EvaluationResult res = EvaluationResult.copy(stationSpeed.get(i)) ;
            EvaluationResult vmtResult = EvaluationResult.copy(findResult(vmt, res.getName()));
            
            // skip average
            if(TITLE_AVERAGE.equals(res.getName())) continue;
            
            // adjust data
            preAdjustResult(res);
            preAdjustResult(vmtResult);

            // calculate DVH
            calculateDVH(res, vmtResult);

            
            // add result to result list
            this.results.add(res);                        
        }                
       
        this.makeTotal();
    }
    
    /**
     * Delayed Vehicle Hour
     * Equation : DVH = (VMT / speed) - (VMT / speedLimit);
     * 
     * Given EvaluationResult data format requirement :
     *   - no confidence column (if it is included, it is removed by 'preAdjustResult()')
     *   - accumulated distance required (if not included, it is created by 'preAdjustResult()')
     *   - virtual station required (if not included, it is created by 'preAdjustResult()')
     * 
     * @param res DVH evaluation result
     */
    protected EvaluationResult calculateDVH(EvaluationResult res, EvaluationResult vmtResult)
    {              
        // count value for data and station
        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();
        
        Station[] stations = this.opts.getSection().getStations(detectorChecker);
        
        // variable to store DVH
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        
        // add time line to DVH data set
        ArrayList times = res.getColumn(0);
        data.add(times);
        
        
        // add traffic data column to DVH data set
        for(int c=1; c<stationCount; c++)
        {
            ArrayList stationData = new ArrayList();
            
            // add station name to station data to store DVH
            stationData.add(res.get(c, res.ROW_TITLE()));
            
            // add distance to station data to store DVH
            stationData.add(res.get(c, res.ROW_DISTANCE()));            
            
            // add station data to data set for DVH
            data.add(stationData);
        }       

        // add sum column to data
        ArrayList totalDVHData = new ArrayList();        
        // add station name as 'Total'
        totalDVHData.add("Total");
        // display distance as 0
        totalDVHData.add(0);  
        // add to data
        data.add(totalDVHData);
        

        double speedLimit = 0.0f;
        double vmt, speed, dvh;
        
        // traffic data is from row 2
        //   : row(0) = station name
        //   : row(1) = distance
        for(int r=2; r<dataCount; r++)
        {        
            double totalDVH = 0.0f;
            
            // for all stations that from column 1
            //   : column 0 : time line
            for(int c=1; c<stationCount; c++)
            {
                
                String stationName = res.get(c, 0).toString();
                
                // use flow data when station is not virtual
                if(!"-".equals(stationName) )
                {
                    stationName = EvalHelper.getStationNameFromTitle(stationName);
                    speedLimit = EvalHelper.getSpeedLimit(stations, stationName);
                } else {

                    // if it's virtual station and it's not a middle between up and down station
                    // use close station's speed limit of this virtual station
                    int prevSpeedLimit = 0, nextSpeedLimit = 0;
                    int cnt = 0;
                    
                    stationName = EvalHelper.getSamePreviousStation(c, res, vmtResult);
                    if(stationName != null) speedLimit = EvalHelper.getStation(stations, stationName).getSpeedLimit();
                    else {
                        stationName = EvalHelper.getSameNextStation(c, res, vmtResult);
                        if(stationName != null) speedLimit = EvalHelper.getStation(stations, stationName).getSpeedLimit();
                    }          
                    
                    // if it's virtual station and it's middle between up and down station
                    // use average speed limit
                    if(stationName == null) {

                        String prevStationName = EvalHelper.getPrevStation(c, res, vmtResult);                    
                        if(prevStationName != null) {
                            prevSpeedLimit = EvalHelper.getStation(stations, prevStationName).getSpeedLimit();
                            cnt++;
                        }
                        String nextStationName = EvalHelper.getNextStation(c, res, vmtResult);
                        if(nextStationName != null) {
                            nextSpeedLimit = EvalHelper.getStation(stations, nextStationName).getSpeedLimit();
                            cnt++;
                        }         
                        speedLimit = (prevSpeedLimit + nextSpeedLimit) / cnt;                    
                    }
                }                
                
                // get speed limit with column title
//                if(!NO_STATION.equals(res.get(c, res.ROW_TITLE())))
//                {
//                    String sname = EvalHelper.getStationNameFromTitle(res.get(c, res.ROW_TITLE()).toString());
//                    speedLimit = EvalHelper.getSpeedLimit(stations, sname);
//                }
                
                vmt = Double.parseDouble(vmtResult.get(c, r).toString());
                speed = Double.parseDouble(res.get(c, r).toString());
                
                if (speed > speedLimit) {
                    dvh = 0;
                }
                else {
                    // equation
                    dvh = (vmt / speed) - (vmt / speedLimit);
                }
                data.get(c).add(dvh);
                totalDVH += dvh;                
            }
            totalDVHData.add(totalDVH);
        }

        res.setData(data);
        res.setUseTotalColumn(true);
                
        return res;
    }

    /**
     * adjust result to make required data
     */
    private EvaluationResult preAdjustResult(EvaluationResult res) {
        
        // remove confidence information from result
        this.removeConfidenceFromResult(res);

        // if res doesn't include distance, add it
        if(!res.useAccumulatedDistance()) this.addDistanceToResult(res);
        
        // if res doesn't include virtual station, add it
        if(!res.useVirtualStation()) this.addVirtualStationToResult(res);
                
        return res;
    }


    /**
     * Returns speed limit of station
     * @param columnTitle column title, e.g. S910 (Crystal Lake Rd)
     * @return speed limit
     */
    protected double getSpeedLimit(String columnTitle)
    {
        Station[] stations = this.opts.getSection().getStations(this.detectorChecker);
        for(int i=0; i<stations.length; i++)
        {
            if(EvalHelper.getStationLabel(stations[i], detectorChecker).equals(columnTitle))
                return (double)stations[i].getSpeedLimit();
        }
        return 0;        
    }

}
