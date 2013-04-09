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
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class StationAverageLaneFlow extends Evaluation {

    @Override
    protected void init() {
        this.name = "Station Average Lane Flow";
    }     
    
    @Override
    protected void process() {
        
        // caching, (if cached alread, just return)
        if(!caching()) return;

        Vector<EvaluationResult> stationSpeed = Evaluation.getResult(StationSpeed.class, this.opts);
        Vector<EvaluationResult> stationDensity = Evaluation.getResult(StationDensity.class, this.opts);
        
        Period[] periods = this.opts.getPeriods();
        int idx = 0;
        int periodIdx = 0;
        Station[] stations = this.opts.getSection().getStations(detectorChecker);
        for(int i=0; i<stationSpeed.size(); i++)
        {
            if(printDebug && idx < periods.length) System.out.println("      - " + periods[idx++].getPeriodString());                        
            if(stationSpeed.get(i).isStatistic()) continue;
            
            this.opts.getSection().loadData(periods[periodIdx++], this.simulationMode);            
            
            EvaluationResult res = EvaluationResult.copy(stationSpeed.get(i));
            EvaluationResult dRes = EvaluationResult.copy(stationDensity.get(i));
            EvaluationResult uRes = EvaluationResult.copy(res);
            
            double[] flow = null;  
            /*
             * modify soobin Jeon 02/14/2012
             */
            int lanes = 0;
            
            for(int c=res.COL_DATA_START(); c<res.getColumnSize(); c++)
            {
                boolean useFlow = false;
                String stationName = res.get(c, 0).toString();                

                // use flow data when station is not virtual
                if(!"-".equals(stationName) )
                {
                    stationName = EvalHelper.getStationNameFromTitle(stationName);
                    useFlow = true;
                    /*
                     * modify soobin Jeon 02/15/2012
                     */
                    flow = EvalHelper.getStation(stations, stationName).getFlowForAverageLaneFlow(detectorChecker);
                } else {
                    // use flow data when station is virtual and it must same with data of prev or next station
                    // according to distance
                    // we can know which station is used by checking speed and density data
                    stationName = EvalHelper.getSamePreviousStation(c, uRes, dRes);
                    /*
                     * modify soobin Jeon 02/15/2012
                     */
                    if(stationName != null) flow = EvalHelper.getStation(stations, stationName).getFlowForAverageLaneFlow();
                    else {
                        stationName = EvalHelper.getSameNextStation(c, uRes, dRes);
                        /*
                         * modify soobin Jeon 02/15/2012
                         */
                        if(stationName != null) flow = EvalHelper.getStation(stations, stationName).getFlowForAverageLaneFlow();
                    }
                    if(stationName != null) useFlow = true;
                }
                for(int r=res.ROW_DATA_START(), row=0; r<res.getRowSize(c); r++, row++)
                {
                    /*
                     * modify soobin Jeon 02/14/2012
                     */
                    if(stationName != null){
                        int l = EvalHelper.getLanes(stationName,stations,detectorChecker);
                        if(l>0) lanes = l;
                    }
                    
                    if(useFlow) {
                        double q = flow[row];
                        
                        if(q < 0 && this.opts.hasOption(OptionType.FIXING_MISSING_DATA)) {
                            // interpolate with prev and next station flow data if flow data is missing
                            q = EvalHelper.interpolateFlow(row, stationName, stations, detectorChecker);
                        }else if(q <= 0 && this.opts.hasOption(OptionType.FIXING_MISSING_DATA_ZERO)){
                            q = EvalHelper.interpolateFlowtoZero();
                        }
                        /*
                         * modify soobin Jeon 02/14/2012
                         */
                        res.set(c, r,q/lanes);
//                        res.set(c, r, q);
                    } else {
                        double u = Double.parseDouble(res.get(c, r).toString());
                        double k = Double.parseDouble(dRes.get(c, r).toString());

                        /*
                         * modify soobin Jeon 02/14/2012
                         */
                        if(u > 0 && k > 0) res.set(c, r, (int)(EvalHelper.roundUp(u*k, 0)));
                        else {
                            res.set(c, r, -1);
                        }
                    }
                }
            }
            this.results.add(res);
        }
        makeAverage();
    }            
}
