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

import edu.umn.natsrl.infra.DataLoadOption;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.section.SectionHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class MRF extends Evaluation {
    
    SectionHelper sectionHelper;
            
    @Override
    protected void init() {
        this.name = "Mainline and Ramp Flow rates";
        //this.DO_NOT_SAVE = true;
    }
    
    @Override
    protected final void process() throws OutOfMemoryError {        
        
        // caching, (if cached alread, just return)
        if(!caching()) return;        
        
        // get section that selected from TICAS GUI
        Section section = this.opts.getSection().clone();
        
        RNode[] rnodes = section.getRNodesWithExitEntrance(this.detectorChecker);
        RNode lastStation = rnodes[rnodes.length-1];
        
        ArrayList<RNode> downEntranceAndExit = getDownstreamEntranceAndExit(lastStation);
        for(RNode rn : downEntranceAndExit) {
            section.addRNode(rn);
        }

        sectionHelper = new SectionHelper(section);
        rnodes = section.getRNodesWithExitEntrance(this.detectorChecker);
        
        EvaluationResult summaryResult = new EvaluationResult();
        summaryResult.setName("Summary");
        
        // for all periods
        for (Period period : this.opts.getPeriods()) 
        {           
            if(printDebug) System.out.println("      - " + period.getPeriodString());
            
            // data load from all detectors in the section
            section.loadData(period, DataLoadOption.setSimulationMode(opts.getSimulationInterval()));                 
            
            // variable to save results
            EvaluationResult res = new EvaluationResult();
            
            // result name that is used sheet name in excel or a part of file name in csv
            res.setName(period.getPeriodString());
            
            // make time line list (at first column)
            ArrayList times = new ArrayList();
            
            // time line's first element is "", 
            // because first row is for station name, not a data
            times.add("");
            times.addAll(Arrays.asList(this.opts.getPeriods()[0].getTimeline()));

            // add time line to first column
            res.addAll(res.COL_TIMELINE(), times);
            
            // add time line to summary result
            if(summaryResult.getColumnSize() ==0) summaryResult.addColumn(times);
            
            // for all stations
            for (int col = 0; col < rnodes.length; col++) 
            {
                // retrieve station
                RNode rn = rnodes[col];

                if(rn.getDetectors(detectorChecker).length == 0) {
                    System.out.println("        !! " + rn.getId() + " has no detector!!");
                    continue;
                }
                
                rn.loadData(period, DataLoadOption.setSimulationMode(opts.getSimulationInterval()));
                
                // get data from rnode
                double[] data = this.getTrafficData(rn);

                // variable to save traffic data
                ArrayList stationData = new ArrayList();
                
                // add station name at first element
                stationData.add(getTitle(rn));

                // for all time series traffic data
                for (int row = 0; row < data.length; row++) 
                {
                    double d = (data[row] < 1 ? -1 : data[row]);
                    stationData.add(d);

                } // <!- end of loop for all time series data
                
                // add station data to result
                res.addColumn(stationData);
                
            } // <!- end of loop for all stations
            
            // add entrance and exit total column
            int startCol = res.COL_DATA_START();
            int startRow = res.ROW_DATA_START();
            ArrayList entranceTotalCol = new ArrayList();
            entranceTotalCol.add("Entrance Total");
            ArrayList exitTotalCol = new ArrayList();
            exitTotalCol.add("Exit Total");
            
            // calcuate sum of entrance and exit flows
            for(int r=startRow; r<res.getRowSize(0); r++)
            {
                double entranceTotal = 0.0f;
                double exitTotal = 0.0f;
                for(int c=startCol; c<res.getColumnSize(); c++)
                {
                    if(rnodes[c-startCol].isEntrance()) entranceTotal += Double.parseDouble(res.get(c, r).toString());
                    if(rnodes[c-startCol].isExit()) exitTotal += Double.parseDouble(res.get(c, r).toString());
                }
                if(entranceTotal < 1) entranceTotal = -1;
                if(exitTotal < 1) exitTotal = -1;
                entranceTotalCol.add(entranceTotal);
                exitTotalCol.add(exitTotal);
            }
            
            // add entrance and exit total column
            res.addColumn(entranceTotalCol);
            res.addColumn(exitTotalCol);
            
            // add entrance and exit total column to summary result
            ArrayList entColForSummary = (ArrayList)entranceTotalCol.clone();
            ArrayList extColForSummary = (ArrayList)exitTotalCol.clone();
            entColForSummary.set(0, period.getPeriodString() + "_Entrance");
            extColForSummary.set(0, period.getPeriodString() + "_Exit");
            summaryResult.addColumn(entColForSummary);
            summaryResult.addColumn(extColForSummary);
            
            this.results.add(res);            
            
        } // <!- end of loop for all periods
        
        this.results.add(summaryResult);
    }    
   
    /**
     * Returns total flow
     * @param rn RNode
     * @return total flow array
     */
    protected double[] getTrafficData(final RNode rn) {
        
        if(rn.isEntrance()) {
            if(this.opts.hasOption(OptionType.USE_INPUT_FLOW_FOR_MRF)) {
                return sectionHelper.getEntrance((Entrance)rn).getRampDemand();
            } else {
                return sectionHelper.getEntrance((Entrance)rn).getRampFlowNew();
            }
        }
        
        if(rn.isExit()) {
            return sectionHelper.getExit((Exit)rn).getRampExitFlow();
        }
        
        if(rn.isStation()) {
            return rn.getTotalFlow(detectorChecker);
        }

        return null;
    }

    /**
     * Returns column title according to type
     * @param rnode RNode 
     * @return column title
     */
    private String getTitle(RNode rnode) {
        if(rnode.isStation()) return "<Station>_" + rnode.getLanes() + "_" + rnode.getStationId();
        if(rnode.isEntrance()) return "<Entrance>_" + rnode.getLabel();
        if(rnode.isExit()) return "<Exit>_" + rnode.getLabel();
        return "-";
    }

//    /**
//     * Returns r_node list which have available detectors
//     * @param rnodes
//     * @return rnode list
//     */
//    private ArrayList<RNode> rnodeFilter(RNode[] rnodes)
//    {
//        ArrayList<RNode> rnodeList = new ArrayList<RNode>();
//        for(RNode rn : rnodes)
//        {
//            if(rn.getDetectors(detectorChecker).length > 0) rnodeList.add(rn);
//        }
//        return rnodeList;
//        //return rnodeList.toArray(new RNode[rnodeList.size()]);
//        
//    }

    private ArrayList<RNode> getDownstreamEntranceAndExit(RNode lastStation) {
        
        ArrayList<RNode> rnodes = new  ArrayList<RNode>();
        Corridor c = lastStation.getCorridor();        
        Vector<RNode> rnodeList = c.getRnodes();

        boolean foundLastStation = false;
        boolean addedEntrance = false;
        boolean addedExit = false;
        
        for(RNode rn : rnodeList) {
            
            if(rn.equals(lastStation)) {
                foundLastStation = true;
                continue;
            }
            
            if(foundLastStation) {
                if(rn.isStation()) {
                    break;
                }
                if(rn.isEntrance()) {
                    rnodes.add(rn);
                    addedEntrance = true;                    
                }
                if(rn.isExit()) {
                    rnodes.add(rn);
                    addedExit = true;
                }                
            }
            
            if(addedEntrance && addedExit) break;
        }
        return rnodes;
    }
    
}
