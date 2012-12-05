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
package edu.umn.natsrl.chart;

import info.monitorenter.gui.chart.ITracePainter;
import java.awt.Color;
import java.util.HashMap;
import java.util.TreeMap;


/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class TICASChartXY extends TICASChart implements ChartImpl {

    Double xMax = null;
    Double yMax = null;
    public TICASChartXY(){
        super();
        init();
    }
    public TICASChartXY(TreeMap<Integer,String> xformat, TreeMap<Integer,String> yformat){
        super(xformat,yformat);
        init();
    }
    
    @Override
    public void init() {
        super.setAxisTitle("", "");
    }
    
    private void setRangeData(double[] xdata, double[] ydata) {
        double xmax = getMaxValue(xdata);
        double ymax = getMaxValue(ydata);
        if(xdata != null && xMax == null || xmax > xMax){
            xMax = xmax;
        }
        if(ydata != null && yMax == null || ymax > yMax){
            yMax = ymax;
        }
        
        if(this.xformat == null)
            super.setXRange(0, (int)(TICASChartUtil.calculateStep(xMax, 10)+10));
        if(this.yformat == null)
            super.setYRange(0, (int)(TICASChartUtil.calculateStep(yMax, 10)+10));
    }

    private double getMaxValue(double[] datas) {
        double Maxdata = 0;
        for(double d : datas){
            if(Maxdata < d)
                Maxdata = d;
        }
        return Maxdata;
    }

    
    public void addXYGraph(double[] xdata, double[] ydata, String trName) {
        addXYGraph(xdata, ydata,trName,null,null);
    }
    public void addXYGraph(double[] xdata, double[] ydata, String trName, ITracePainter<?> pnt, Color color){
        setRangeData(xdata,ydata);
        super.addDataTrace(xdata, ydata, trName, pnt, color);
    }
}
