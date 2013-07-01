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
 * Speed Variation
 * @author Chongmyung Park
 */
public class SV extends Evaluation {

    @Override
    protected void init() {
        this.name = "SV";
    }

    @Override
    protected void process() {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;        
        
        // get station speeds
        Vector<EvaluationResult> stationSpeed = Evaluation.getResult(StationSpeed.class, this.opts);
        
        // variable to store SV
//        EvaluationResult svResult = new EvaluationResult();
//        svResult.setName("Speed Variations");
        EvaluationResult[] eresult = new EvaluationResult[5];
        for(int i = 0; i<eresult.length;i++){
                eresult[i] = new EvaluationResult();
                String sname = "";
                switch(i){
                        case 0 :
                                sname = "average";
                                break;
                        case 1 :
                                sname = "variance";
                                break;
                        case 2 :
                                sname = "max";
                                break;
                        case 3 :
                                sname = "min";
                                break;
                        case 4 :
                                sname = "diff";
                                break;
                }
                eresult[i].setName(sname);
        }
        
        Period[] periods = this.opts.getPeriods();
        int idx = 0;
        
        // for all results, calculate SV
        for(EvaluationResult result : stationSpeed)
        {
            EvaluationResult res = EvaluationResult.copy(result);
            
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());            
            
            // skip average
            if(TITLE_AVERAGE.equals(res.getName())) continue;
            
            // adjust data
            preAdjustResult(res);

            // add timeline
            if(eresult[0].getColumnSize() == 0){ 
                    for(int i=0;i<eresult.length;i++)
                        eresult[i].addColumn(res.getColumn(0));
            }
            
            // calculate SV
            calculateSV(res, eresult);
            
        }                
        
        for(int i=0;i<eresult.length;i++){
                makeAverageRow(eresult[i]);
                // add result to result list
                this.results.add(eresult[i]);                               
        }
    }
    
    /**
     * Speed Variation
     * -> Average, Variance, Maximum, Minimum and difference for every time interval
     * 
     * Given EvaluationResult data format requirement :
     *   - no confidence column (if it is included, it is removed by 'preAdjustResult()')
     *   - no accumulated distance (if it is included, it is removed by 'preAdjustResult()')
     *   - virtual station required (if not included, it is created by 'preAdjustResult()')
     * 
     * @param res Speed variation evaluation result
     */
    protected EvaluationResult[] calculateSV(EvaluationResult res, EvaluationResult[] svr)
    {       
        // count value for data and station
        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();       
                
        // variables for result column
        ArrayList average = new ArrayList();
        ArrayList variance = new ArrayList();
        ArrayList max = new ArrayList();
        ArrayList min = new ArrayList();
        ArrayList diff = new ArrayList();
        
        // add label
        average.add(res.getName() + "_average");
        variance.add(res.getName() + "_variance");
        max.add(res.getName() + "_max");
        min.add(res.getName() + "_min");
        diff.add(res.getName() + "_difference");
        
        
        // for all data rows
        for(int r=res.ROW_DATA_START(); r<dataCount; r++)
        {        
            double sum = 0.0f;
            double maxSpeed = 0.0f;
            double minSpeed = 200f;
            double sumSquare = 0.0f;
            double count = 0f;
            
            // for all data columns
            for(int c=res.COL_DATA_START(); c<stationCount; c++)
            {
                double u = Double.parseDouble(res.get(c, r).toString());
                if(u > maxSpeed) maxSpeed = u;
                if(u < minSpeed) minSpeed = u;
                sumSquare += u*u;
                sum += u;
                count++;
            }
            
            // calculate metrics
            double av = sum/count;
            average.add(av);
            variance.add(sumSquare/count - av*av);
            max.add(maxSpeed);
            min.add(minSpeed);
            diff.add(maxSpeed-minSpeed);
        }
        
        // add column to result
        svr[0].addColumn(average);
        svr[1].addColumn(variance);
        svr[2].addColumn(max);
        svr[3].addColumn(min);
        svr[4].addColumn(diff);
        
        return svr;
    }

    /**
     * Adjust result to make required data
     */
    private EvaluationResult preAdjustResult(EvaluationResult res) {
        
        // remove confidence information from result
        this.removeConfidenceFromResult(res);
        
        // remove distance
        this.removeDistanceFromResult(res);
        
        // if res doesn't include virtual station, add it
        if(!res.useVirtualStation()) this.addVirtualStationToResult(res);
                
        return res;
    }

    /**
     * Add average row into result
     * @param res SV result
     */
    private void makeAverageRow(EvaluationResult res) {

        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();    
                
        res.getColumn(0).add("Average");
        
        double div = dataCount - res.ROW_DATA_START();
        
        for(int c=res.COL_DATA_START(); c<stationCount; c++)
        {
            double sum = 0.0f;
            for(int r=res.ROW_DATA_START(); r<dataCount; r++)
            {        
                sum += Double.parseDouble(res.get(c, r).toString());
            }
            res.getColumn(c).add(sum/div);
        }
    }

}
