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

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class TT_RealTime extends Evaluation {

    @Override
    protected void init() {
        this.name = "TT";
    }

    @Override
    protected void process() {
        
        if(!caching()) return;
        
        // get station speeds
        StationSpeed stationSpeedEvaluator = new StationSpeed();
        stationSpeedEvaluator.setOptions(opts);
        stationSpeedEvaluator.process();
        Vector<EvaluationResult> stationSpeed = stationSpeedEvaluator.getResult();
        
        // for all results, calculate TT
        for(EvaluationResult res : stationSpeed)
        {
            // skip average
            if(TITLE_AVERAGE.equals(res.getName())) continue;
            
            // adjust data
            preAdjustResult(res);

            // calculate TT
            calculateTT(res);

            
            // add result to result list
            this.results.add(res);                        
        }                
       
        makeTravelTimeAverage();

    }
    
    /**
     * Travel Time
     * Equation : TT(A and B) = SUM(Li/Ui)
     *   - There are i virtual stations between station A and B
     * 
     * Given EvaluationResult data format requirement :
     *   - no confidence column (if it is included, it is removed by 'preAdjustResult()')
     *   - accumulated distance required (if not included, it is created by 'preAdjustResult()')
     *   - virtual station required (if not included, it is created by 'preAdjustResult()')
     * 
     * @param res TT evaluation result
     */
    protected EvaluationResult calculateTT(EvaluationResult res)
    {       
        // count value for data and station
        int dataCount = res.getRowSize(0);
        int stationCount = res.getColumnSize();
        
        // variable to store TT
        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
        
        // add time line to TT data set
        ArrayList times = res.getColumn(res.COL_TIMELINE());
        //times.add("Average");
        data.add(times);
        
        
        // add traffic data column to TT data set
        for(int c=res.COL_DATA_START(); c<stationCount; c++)
        {
            ArrayList stationData = new ArrayList();
            
            // add station name to station data to store TT
            stationData.add(res.get(c, res.ROW_TITLE()));
            
            // add distance to station data to store TT
            stationData.add(res.get(c, res.ROW_DISTANCE()));            
            
            // add station data to data set for TT
            data.add(stationData);
        }       

       // traffic data is from col 1, row 2
        double[] totalCol = new double[stationCount-1];
        
        for(int r=res.ROW_DATA_START(); r<dataCount; r++)
        {        
            // for all stations that from column 1
            //   : column 0 => time line
            for(int c=res.COL_DATA_START(); c<stationCount; c++)
            {
                // TT in minute : 0.1 mile / U * 60
                double TTBetweenStations = VIRTUAL_DISTANCE_IN_MILE / (Double)(res.get(c, r)) * SECONDS_PER_HOUR / 60;
                // add travel time that is from start station to upstream node to current travel time
                if(c > 1) TTBetweenStations += (Double)(data.get(c-1).get(r));
                totalCol[c-1] += TTBetweenStations;
                data.get(c).add(TTBetweenStations);
            }
        }
        
        // Add average row to times and data
        data.get(0).add("Average");
        for(int c=1; c<stationCount; c++)
        {
            totalCol[c-1] /= (dataCount - 2);
            data.get(c).add(totalCol[c-1]);
        }
        
        res.setData(data);
        
        return res;
    }

    /**
     * Adjust result to make required data
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
     * Make TT average result
     */
    private void makeTravelTimeAverage() {

        ArrayList times = this.results.get(0).getColumn(0);
        
        // variable to save average
        EvaluationResult average = new EvaluationResult();
        
        // set statistic sheet
        average.setIsStatistic(true);
        
        // set result name
        average.setName("Travel Time");
        
        // add time line at first column
        average.addColumn((ArrayList)times.clone());
                
        // for all results
        for(EvaluationResult res : this.results)
        {     
            // add to total data set
            ArrayList lastData = (ArrayList)res.getColumn(res.getColumnSize()-1).clone();
            lastData.set(0, res.getName());
            average.addColumn(lastData);
        }
        
        ArrayList averageColumn = new ArrayList();
        averageColumn.add("Average");
        averageColumn.add(0);
        
        // make average column
        for(int r=2; r<average.getRowSize(0); r++)
        {
            double total = 0.0f;
            for(int c=1; c<average.getColumnSize(); c++)
            {
                total += (Double)(average.get(c, r));
            }
            averageColumn.add(total/(average.getColumnSize()-1));
        }
        
        // add average column to average data set
        average.addColumn(averageColumn);
        
        // add average data at last element of results
        this.results.add(average);
        
    }


}
