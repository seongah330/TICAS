/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.ticas.plugin.srte.SRTEChartView.DataType;
import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyManualTicks;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterLine;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEChartLine extends SRTEChart implements ImplSRTEChart{
    public SRTEChartLine(){
        super();
        init();
    }
    
    @Override
    public void init() {
        setAxisTitle("Time Interval","Speed");
        setXRange(0,0);
        setYRange(0,80);
        chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
    }
    
    public void setSpeedData(HashMap<Integer,Boolean> point, HashMap<Integer,Boolean> time, HashMap<Integer,Boolean> bare, 
            HashMap<Integer,Boolean> srt, HashMap<Integer,Boolean> RCRp, HashMap<Integer,Boolean> Keyp, 
            double[] datas, DataType datatype){
        double Maxdata = 0;
        for(double d : datas){
            if(Maxdata < d)
                Maxdata = d;
        }
        
        this.setYRange(0,(int)(SRTEUtil.calculateStep(Maxdata,10)+20));
        setAxisTitle("Time Interval",datatype.name);
        
        this.AddDataTrace(datas,datatype.toString());
        this.AddPointDataTrace(point, datas, "Points", true, new TracePainterVerticalBar(2, chart),Color.BLUE);
        this.AddPointDataTrace(time, datas, "Time Information", true, new TracePainterVerticalBar(3, chart),Color.GRAY);
        this.AddPointDataTrace(bare, datas, "BareLane Point", true, new TracePainterVerticalBar(3, chart),Color.GREEN);
        this.AddPointDataTrace(srt, datas, "SRT Points", true, new TracePainterVerticalBar(3, chart),Color.MAGENTA);
        this.AddPointDataTrace(RCRp, datas, "RCR Points", true, new TracePainterVerticalBar(3, chart),Color.RED);
        this.AddPointDataTrace(Keyp, datas, "Key Points", true, new TracePainterVerticalBar(3, chart),Color.yellow);
    }
}
