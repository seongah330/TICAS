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
import org.krm.tg.DataSeries;
import org.krm.tg.XYDataSeries;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class LiveXYGraph extends LiveGraph {
    
    protected String xDataType;
    protected String yDataType;

    public LiveXYGraph() {
        this.lineVisibility = false;
    }

    public LiveXYGraph(String xDataType, String yDataType) {
        this();
        this.xDataType = xDataType;
        this.yDataType = yDataType;
    }

    protected void addValue(XYDataSeries ds, StationNode station) {
        ds.addValueXY(getXData(station), getYData(station));
    }
    protected String getTitle() {
        return "Live " + this.xDataType + "-" + this.yDataType + " Graph";
    }
    protected String getXLegend(){
        return this.xDataType;
    }
    protected String getYLegend(){
        return this.yDataType;
    }
    protected String getHintTemplate(){
        return this.xDataType+": {0}\n"+this.yDataType+":{1}";
    }
    protected int getXSeriesType(){
        return DataSeries.NUMBER_SERIES;
    }
    protected int getYSeriesType(){
        return DataSeries.NUMBER_SERIES;
    }
    
    
    abstract protected float getXData(StationNode station);
    abstract protected float getYData(StationNode station);

}
