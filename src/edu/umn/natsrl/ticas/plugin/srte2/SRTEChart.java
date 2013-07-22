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
package edu.umn.natsrl.ticas.plugin.srte2;

import edu.umn.natsrl.ticas.plugin.srte2.SRTEChartView.DataType;
import info.monitorenter.gui.chart.*;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyManualTicks;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;
import java.awt.Color;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEChart {
    Chart2D chart;
    IAxis axisX;
    IAxis axisY;
    public SRTEChart(){
        initChart();
    }
    
    private void initChart() {
        chart = new Chart2D();
        axisX = chart.getAxisX();
        axisY = chart.getAxisY();
        axisX.setPaintGrid(true);
        axisX.setPaintScale(true);
        axisY.setPaintGrid(true);
        axisY.setPaintScale(true);
        
    }
    
    public Chart2D getChart(){
        return chart;
        
    }

    /**
     * set XRange
     * @param min
     * @param max 
     */
    protected void setXRange(double min, double max) {
        IAxis axisX = chart.getAxisX();
        axisX.setRangePolicy(new RangePolicyFixedViewport(new Range(min, max)));
        axisX.setAxisScalePolicy(new AxisScalePolicyManualTicks());
        setStickSpacing(axisX,5);
//        axisX.scaleTrace(new TracePoint2D());
        
    }

    /**
     * set YRange
     * @param min
     * @param max 
     */
    protected void setYRange(double min, double max) {
        IAxis axisY = chart.getAxisY();
        axisY.setRangePolicy(new RangePolicyFixedViewport(new Range(min, max)));
        axisY.setStartMajorTick(true);
        setStickSpacing(axisY,5);
    }

    /**
     * set AxisTitle
     * @param x Axis X Title
     * @param y Axis Y Title
     */
    public void setAxisTitle(String x, String y) {
        axisX.getAxisTitle().setTitle(x);
        axisY.getAxisTitle().setTitle(y);
    }

    /**
     * add Y Value Trace
     * @param datas
     * @param Name 
     */
    public void AddDataTrace(double[] datas, String Name) {
        AddDataTrace(datas,Name,true);
    }
    public void AddDataTrace(double[] datas, String Name, boolean isHighlight) {
        AddDataTrace(datas,Name,isHighlight,null);
    }
    public void AddDataTrace(double[] datas, String Name, boolean isHighlight, ITracePainter<?> pnt) {
        AddPointDataTrace(null,datas,Name,isHighlight,pnt);
    }
    public void  AddPointDataTrace(HashMap<Integer,Boolean> point, double[] datas, String Name, boolean isHighlight){
        AddPointDataTrace(point,datas,Name,isHighlight,null);
    }
    public void AddPointDataTrace(HashMap<Integer,Boolean> point, double[] datas, String Name, boolean isHighlight, ITracePainter<?> pnt){
        AddPointDataTrace(point,datas,Name,isHighlight,null,null);
    }
    /**
     * Add Y Value Trace
     * @param point
     * @param datas
     * @param Name
     * @param isHighlight
     * @param pnt
     * @param color 
     */
    public void AddPointDataTrace(HashMap<Integer,Boolean> point, double[] datas, String Name, boolean isHighlight, ITracePainter<?> pnt, Color color){
        ITrace2D trace = new Trace2DSimple();

        chart.addTrace(trace);

        if(color == null)
            trace.setColor(Color.RED);
        else
            trace.setColor(color);
        
        trace.setName(Name);

        if(pnt != null){
            trace.setTracePainter(pnt);
//            trace.setTracePainter(new TracePainterVerticalBar(4, chart));
        }
        
        if(isHighlight){
            // Feature: turn on highlighting: Two steps enable it on the chart and set a highlighter for the trace: 
            Set<IPointPainter<?>> highlighters = trace.getPointHighlighters();
            highlighters.clear();
            trace.addPointHighlighter(new PointPainterDisc(10));
            chart.enablePointHighlighting(true);
        }
        
        if(point == null || point.size() == 0)
            intputTraceData(datas,trace);
        else
            intputTraceData(point,datas,trace);
//        ITracePainter<?>
        setXRange(0,datas.length);
    }
    
    /**
     * input X Y Data
     * @param xdata
     * @param ydata
     * @param datatype 
     */
    void addDataTrace(double[] xdata, double[] ydata, DataType datatype) {
        ITrace2D trace = new Trace2DSimple();

        chart.addTrace(trace);
        trace.setName(datatype.name);

        // Feature: turn on highlighting: Two steps enable it on the chart and set a highlighter for the trace: 
        Set<IPointPainter<?>> highlighters = trace.getPointHighlighters();
        highlighters.clear();
        trace.addPointHighlighter(new PointPainterDisc(10));
        chart.enablePointHighlighting(true);
        
        intputTraceData(xdata,ydata,trace);
//        ITracePainter<?>
    }

    protected void intputTraceData(double[] datas, ITrace2D trace) {
        trace.removeAllPoints();
        for(int i=0;i<datas.length;i++){
            trace.addPoint(i,datas[i]);
        }
    }
    
    /**
     * intput X Y Data
     * @param xdata
     * @param ydata
     * @param trace 
     */
    protected void intputTraceData(double[] xdata, double[] ydata, ITrace2D trace) {
        trace.removeAllPoints();
        for(int i=0;i<xdata.length;i++){
            trace.addPoint(xdata[i],ydata[i]);
        }
    }

    protected void intputTraceData(HashMap<Integer,Boolean> point, double[] datas, ITrace2D trace) {
        trace.removeAllPoints();
        for(int i=0;i<datas.length;i++){
            if(point.get(i) != null)
                trace.addPoint(i,datas[i]);
        }
    }
    
    /**
     * Remove All Trace
     */
    public void removeAllTrace(){
        chart.removeAllTraces();
    }

    private void setStickSpacing(IAxis axis, int i) {
        axis.setMajorTickSpacing(i);
        axis.setMinorTickSpacing(i);
    }
}
