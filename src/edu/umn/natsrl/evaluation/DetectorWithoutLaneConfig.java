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
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Chongmyung Park
 */
public class DetectorWithoutLaneConfig extends Evaluation {
        
    protected void init()
    {
        this.name = "Each Detector Data";
    }
    
    @Override
    protected void process() throws OutOfMemoryError {

        // caching, (if cached alread, just return)
        if(!caching()) return;        
        
        // get section that selected from TICAS GUI
        Section section = this.opts.getSection();
        
        // get stations including the section
        RNode[] rnodes = section.getRNodesWithExitEntrance(this.detectorChecker);
        
        // for all periods
        for (Period period : this.opts.getPeriods()) 
        {            
            if(printDebug) System.out.println("      - " + period.getPeriodString());
            
            // data load from all detectors in the section
            section.loadData(period, this.dataLoadOption);                 
            
            // variable to save results
            EvaluationResult res = new EvaluationResult();            
            
            // result name that is used sheet name in excel or a part of file name in csv
            res.setName(period.getPeriodString());
            
            res.setDataStartRow(3);
                        
            // original time line
            String[] timeline = this.opts.getPeriods()[0].getTimeline();
            ArrayList times = new ArrayList();
            times.add("");
            times.add(this.TITLE_CONFIDENCE);
            times.add("Data Type");
            times.addAll(Arrays.asList(timeline));
            
            
            // add time line to first column
            res.addColumn(times);
            
            // for all stations
            for (int col = 0; col < rnodes.length; col++) 
            {
                // retrieve station
                RNode rn = rnodes[col];                
                
                Detector[] detectors = rn.getDetectors();                
                Arrays.sort(detectors);
                
                // add data to station data
                for(Detector det : detectors) 
                {
                    //if(this.opts.hasOption(OptionType.DETECTOR_FLOW)) {
                    if(this.opts.hasOption(OptionType.DETECTOR_FLOW)) {                        
                        ArrayList detectorColumn = new ArrayList();
                        detectorColumn.add(getTitle(det));
                        detectorColumn.add(det.getConfidence());
                        detectorColumn.add("Flow");
                        for(double d : det.getFlow()) detectorColumn.add(d);
                        res.addColumn(detectorColumn);
                    }                    
                    if(this.opts.hasOption(OptionType.DETECTOR_DENSITY)) {
                        ArrayList detectorColumn = new ArrayList();
                        detectorColumn.add(getTitle(det));
                        detectorColumn.add(det.getConfidence());
                        detectorColumn.add("Density");
                        for(double d : det.getDensity()) detectorColumn.add(d);
                        res.addColumn(detectorColumn);
                    }
                    if(this.opts.hasOption(OptionType.DETECTOR_OCCUPANCY)) {
                        ArrayList detectorColumn = new ArrayList();
                        detectorColumn.add(getTitle(det));
                        detectorColumn.add(det.getConfidence());
                        detectorColumn.add("Occupancy");
                        for(double d : det.getOccupancy()) detectorColumn.add(d);
                        res.addColumn(detectorColumn);
                    }                    
                   if(this.opts.hasOption(OptionType.DETECTOR_SPEED)) {
                        ArrayList detectorColumn = new ArrayList();
                        detectorColumn.add(getTitle(det));
                        detectorColumn.add(det.getConfidence());
                        detectorColumn.add("Speed");
                        for(double d : det.getSpeed()) detectorColumn.add(d);
                        res.addColumn(detectorColumn);
                    }                       
                }                                                  
                        
            } // <!- end of loop for all stations
                        
            
            this.results.add(res);
                        
        } // <!- end of loop for all periods
        
        makeAverage();
    }

    private String getTitle(Detector det) {
        Station s = det.getStation();
        if(s != null) {
            return det.getId() + "<"+s.getStationId()+">_Station";
        }
        if(det.getRNode().isEntrance()) return det.getId() + "<Entrance>_" + det.getLaneType();
        if(det.getRNode().isExit()) return det.getId() + "<Exit>_" + det.getLaneType();
        return "-";
    }    
    
}
