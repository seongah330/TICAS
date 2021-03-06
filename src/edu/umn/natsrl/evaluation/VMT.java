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
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class VMT extends Evaluation {

    @Override
    protected void init() {
        this.name = "VMT";
    }    
    
    @Override
    protected void process() {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;
        
        // get station flows
        Vector<EvaluationResult> stationFlows = Evaluation.getResult(StationTotalFlow.class, this.opts);
        
        Period[] periods = this.opts.getPeriods();
        int idx = 0;        
        // for all results, calculate VMT
        for(EvaluationResult result : stationFlows)
        {
            EvaluationResult res = EvaluationResult.copy(result);
            
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());            
            
            // skip average
            if(TITLE_AVERAGE.equals(res.getName())) continue;
            
            // adjust data
            preAdjustResult(res);

            // calculate VMT
            calculateVMT(res);

            
            // add result to result list
            this.results.add(res);                        
        }                
        
        this.makeTotal();

    }
    
    /**
     * Vehicle Miles Traveled (Trips per vehicle X miles per trip)
     * Equation : VMT = total flow of station(v/h) * interval(hour) * 0.1(distance in mile);
     * 
     * Given EvaluationResult data format requirement :
     *   - no confidence column (if it is included, it is removed by 'preAdjustResult()')
     *   - accumulated distance required (if not include, it is created by 'preAdjustResult()')
     *   - virtual station required (if not included, it is created by 'preAdjustResult()')
     * 
     * @param res total flow evaluation result
     */
    protected EvaluationResult calculateVMT(EvaluationResult res)
    {
        // interval       
        int interval = this.opts.getInterval().second;        
        
        // count value for data and station
        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();
        
        // variable to store VMT
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        
        // add time line to VMT data set
        data.add(res.getColumn(res.COL_TIMELINE()));
        
        // add traffic data column to VMT data set
        for(int c=1; c<stationCount; c++)
        {
            ArrayList stationData = new ArrayList();
            
            // add station name to station data to store VMT
            stationData.add(res.get(c, res.ROW_TITLE()));
            
            // add distance to station data to store VMT
            stationData.add(res.get(c, res.ROW_DISTANCE()));            
            
            // add station data to data set for VMT
            data.add(stationData);
        }
        
        // add sum column to data
        ArrayList totalVMTData = new ArrayList();        
        // add station name as 'Total'
        totalVMTData.add("Total");
        // display distance as 0
        totalVMTData.add(0);  
        // add to data
        data.add(totalVMTData);
        

       // traffic data is from row 2
        //   : row(0) = station name
        //   : row(1) = distance
        for(int r=2; r<dataCount; r++)
        {        
            double totalVMT = 0.0f;

            // for all stations that from column 1
            //   : column 0 : time line
            for(int c=1; c<stationCount; c++)
            {
                double vmt = Double.parseDouble(res.get(c, r).toString()) * ((double)interval/SECONDS_PER_HOUR) * VIRTUAL_DISTANCE_IN_MILE;
                data.get(c).add(vmt);
                totalVMT += vmt;
            }
            totalVMTData.add(totalVMT);
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
      
}
