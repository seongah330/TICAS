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

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEChartXY extends SRTEChart implements ImplSRTEChart {

    public SRTEChartXY(){
        super();
    }
    
    @Override
    public void init() {
        super.setAxisTitle("K", "Q");
    }
    
    public void setXYGraph(double[] xdata, double[] ydata, DataType datatype){
        setRangeData(xdata,ydata);
        
        super.addDataTrace(xdata, ydata,datatype);
    }

    private void setRangeData(double[] xdata, double[] ydata) {
        double xMax = getMaxValue(xdata);
        double yMax = getMaxValue(ydata);
        
        super.setXRange(0, (int)(SRTEUtil.calculateStep(xMax, 10)+10));
        super.setYRange(0, (int)(SRTEUtil.calculateStep(yMax, 10)+10));
    }

    private double getMaxValue(double[] datas) {
        double Maxdata = 0;
        for(double d : datas){
            if(Maxdata < d)
                Maxdata = d;
        }
        return Maxdata;
    }
}
