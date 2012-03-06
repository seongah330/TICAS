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
package edu.umn.natsrl.ticas.plugin.rtchart;

import edu.umn.natsrl.ticas.plugin.rtchart.reader.StationNode;
import java.util.Date;
import org.krm.tg.DataSeries;
import org.krm.tg.XYDataSeries;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class LiveTimelineGraph extends LiveGraph {

    private String dataType;
    
    public LiveTimelineGraph() {

    }

    public LiveTimelineGraph(String dataType) {
        this.dataType = dataType;   // Speed, Flow, Volume...
    }

    protected void addValue(XYDataSeries ds, StationNode station) {
        long tDate = station.getDate().getTime();
        ds.addValueXY(new Date(tDate), new Double(getData(station)));        
    }
    protected String getTitle() {
        return "Live " + this.dataType + " Graph";
    }
    protected String getXLegend(){
        return "Time";
    }
    protected String getYLegend(){
        return this.dataType;
    }
    protected String getHintTemplate(){
        return "Time: {0}\n"+this.dataType+":{1}";
    }
    protected int getXSeriesType(){
        return DataSeries.DATE_SERIES;
    }
    protected int getYSeriesType(){
        return DataSeries.NUMBER_SERIES;
    }

    abstract protected float getData(StationNode station);


}
