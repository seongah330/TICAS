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
 * Vehicle Hour Traveled
 * @author Chongmyung Park
 */
public class VHT extends Evaluation {

    @Override
    protected void init() {
        this.name = "VHT";
    }

    @Override
    protected void process() {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;
        
        // get VMT
        Vector<EvaluationResult> vmt = Evaluation.getResult(VMT.class, this.opts);
        
        // get station speeds
        Vector<EvaluationResult> stationSpeed = Evaluation.getResult(StationSpeed.class, this.opts);
                
        Period[] periods = this.opts.getPeriods();
        int idx = 0;
        
        // for all results, calculate DVH
        for(int i=0; i<stationSpeed.size(); i++)
        {
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());
            
            EvaluationResult res = EvaluationResult.copy(stationSpeed.get(i));
            EvaluationResult vmtResult = EvaluationResult.copy(findResult(vmt, res.getName()));
            
            // skip average
            if(TITLE_AVERAGE.equals(res.getName())) continue;
            
            // adjust data
            preAdjustResult(res);
            preAdjustResult(vmtResult);

            // calculate DVH
            calculateVHT(res, vmtResult);

            
            // add result to result list
            this.results.add(res);                        
        }                
       
        this.makeTotal();
    }
    
    /**
     * Vehicle Hour Traveled
     * Equation : VHT = VMT / speed
     * 
     * Given EvaluationResult data format requirement :
     *   - no confidence column (if it is included, it is removed by 'preAdjustResult()')
     *   - accumulated distance required (if not included, it is created by 'preAdjustResult()')
     *   - virtual station required (if not included, it is created by 'preAdjustResult()')
     * 
     * @param res VHT evaluation result
     */
    protected EvaluationResult calculateVHT(EvaluationResult res, EvaluationResult vmtResult)
    {              
        // count value for data and station
        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();
        
        // variable to store VHT
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        
        // add time line to VHT data set
        ArrayList times = res.getColumn(0);
        data.add(times);
        
        
        // add traffic data column to DVH data set
        for(int c=1; c<stationCount; c++)
        {
            ArrayList stationData = new ArrayList();
            
            // add station name to station data to store VHT
            stationData.add(res.get(c, res.ROW_TITLE()));
            
            // add distance to station data to store VHT
            stationData.add(res.get(c, res.ROW_DISTANCE()));            
            
            // add station data to data set for VHT
            data.add(stationData);
        }       

        // add sum column to data
        ArrayList totalVHTData = new ArrayList();        
        // add station name as 'Total'
        totalVHTData.add("Total");
        // display distance as 0
        totalVHTData.add(0);  
        // add to data
        data.add(totalVHTData);
        

        double vmt, speed, vht;
        
        // traffic data is from row 2
        //   : row(0) = station name
        //   : row(1) = distance
        for(int r=2; r<dataCount; r++)
        {        
            double totalVHT = 0.0f;
            
            // for all stations that from column 1
            //   : column 0 : time line
            for(int c=1; c<stationCount; c++)
            {
                vmt = Double.parseDouble(vmtResult.get(c, r).toString());
                speed = Double.parseDouble(res.get(c, r).toString());                
                
                // equation
                vht = (vmt / speed);
                
                data.get(c).add(vht);
                totalVHT += vht;                
            }
            totalVHTData.add(totalVHT);
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
