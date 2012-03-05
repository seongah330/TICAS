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
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class StationAccel extends Evaluation {

    @Override
    protected void init() {
        this.name = "Station Acceleration";
    }
    
    @Override
    protected final void process() throws OutOfMemoryError {        
      
        // caching, (if cached alread, just return)
        if(!caching()) return;
             
        // get station density
        Vector<EvaluationResult> stationSpeed = Evaluation.getResult(StationSpeed.class, this.opts);        
        
        // get section that selected from TICAS GUI
        Section section = this.opts.getSection();
        
        // get stations including the section
        Station[] stations = section.getStations(this.detectorChecker);        
        
        Period[] periods = this.opts.getPeriods();
        
        int idx = 0;   
        int startIdx = 0;
        if(stationSpeed.size() > 1) startIdx = 1;
        for(int i=startIdx; i<stationSpeed.size(); i++)
        {
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());            
            
            EvaluationResult res = EvaluationResult.copy(stationSpeed.get(i));
            res = this.removeVirtualStationFromResult(res);
            EvaluationResult accelRes = EvaluationResult.copy(stationSpeed.get(i));
            accelRes = this.removeVirtualStationFromResult(accelRes);
            
            // add first station data (all of data are 0)
            for(int r=accelRes.ROW_DATA_START(); r<res.getRowSize(0); r++) {
                accelRes.set(accelRes.COL_DATA_START(), r, 0D);
            }
                        
            for(int c=res.COL_DATA_START()+1; c<res.getColumnSize(); c++)
            {
                int stationIdx = c-res.COL_DATA_START();
                for(int r=res.ROW_DATA_START(); r<res.getRowSize(c); r++)
                {
                    double u1 = Double.parseDouble(res.get(c-1, r).toString());
                    double u2 = Double.parseDouble(res.get(c, r).toString());
                    double distance = TMO.getDistanceInMile(stations[stationIdx-1], stations[stationIdx]);
                    double accel = getAcceleration(u1, u2, distance);
                    accelRes.set(c, r, accel);
                }
            }
            this.results.add(accelRes);
        }        
        hasResult = true;
    }    
   
    
    private double getAcceleration(double u1, double u2, double distanceInMile) {
        return (u2*u2 - u1*u1)/(2*distanceInMile);        
    }
}
