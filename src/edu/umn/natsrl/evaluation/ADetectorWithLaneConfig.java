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
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Chongmyung Park
 */
public abstract class ADetectorWithLaneConfig extends Evaluation {
       
    @Override
    abstract protected void init();
    abstract protected double[] getTrafficData(Detector s);
    
    @Override
    protected void process() throws OutOfMemoryError {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;
        
        // get section that selected from TICAS GUI
        Section section = this.opts.getSection();
        
        // get stations including the section
        Station[] stations = section.getStations(this.detectorChecker);
        
        // for all periods
        for (Period period : this.opts.getPeriods()) 
        {            
            if(printDebug) System.out.println("      - " + period.getPeriodString());
            
            // data load from all detectors in the section
            section.loadData(period, this.simulationMode);                 
            
            // variable to save results
            EvaluationResult res = new EvaluationResult();            
            
            // result name that is used sheet name in excel or a part of file name in csv
            res.setName(period.getPeriodString());
                        
            // original time line
            String[] timeline = this.opts.getPeriods()[0].getTimeline();
            
            // count of traffic data
            // it is same to times.size
            int dataSize = timeline.length;
            
            // max lanes of stations at each time
            int[] maxLanes = new int[dataSize];
            for(int row = 0; row<dataSize; row++)
            {                
                for(int col = 0; col<stations.length; col++)
                {
                    int lanes = stations[col].getLanes(detectorChecker);
                    if(lanes > maxLanes[row]) maxLanes[row] = lanes;
                }
            }

            // add "-" to time line to make space to save detector data
            ArrayList times = new ArrayList();
            times.add("");
            for(int row = 0; row<dataSize; row++)
            {
                times.add(timeline[row]);
                
                for(int i=1; i<maxLanes[row]; i++)
                {
                    times.add("-");
                }
            }
            
            // add time line to first column
            res.addAll(res.COL_TIMELINE(), times);           

            int column = 1;
            
            // for all stations
            for (int col = 0; col < stations.length; col++) 
            {
                // retrieve station
                Station s = stations[col];
                               
                Detector[] detectors = EvalHelper.getDetectorsWithoutMissing(s, detectorChecker);

                // sort by detector's lane number (Detector class is comparable)
                Arrays.sort(detectors);
            
                
                //Detector[] detectors = detectorFilter(ds);
                
                // add station name to head
                res.add(column, EvalHelper.getStationLabel(stations[col], detectorChecker));
                    
                // for all time series traffic data
                for (int row = 0; row < dataSize; row++) 
                {                                           
                    // for all detectors on the station
                    for(Detector detector : detectors) 
                    {                       
                        // get data from detector
                        // this 'getTrafficData' method is implemented child-class
                        double[] data = getTrafficData(detector);

                        // add data to station data
                        res.add(column, data[row]);

                    }   // <!- end of loop for all detectors

                    // if station's lanes is less than max lanes at this time
                    // add blank data "" to data
                    int extraDataSize = maxLanes[row]-detectors.length;
                    for(int i=0; i<extraDataSize; i++) 
                    {
                        res.add(column, "");
                    }                   
                                        
                } // <!- end of loop for all time series data                        
                       
                // increase column
                column++;
                        
            } // <!- end of loop for all stations
            
            this.addDistanceToResult(res);
            
            this.results.add(res);
                        
        } // <!- end of loop for all periods
        
        makeAverage();
        
    }


    
}
