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
package edu.umn.natsrl.ticas.plugin.simulation.VSL;

import edu.umn.natsrl.chart.TICASChartXY;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLChartXY extends TICASChartXY{
    public VSLChartXY(TreeMap<Integer,String> xformat, TreeMap<Integer, String> yformat){
        super(xformat,yformat);
    }
    
    public void AddStationSpeedGraph(TreeMap<Integer,VSLStationState> stations, String name){
        double[] xdata = new double[stations.size()];
        double[] ydata = new double[stations.size()];
        int cnt = 0;
        for(Integer key : stations.keySet()){
            xdata[cnt] = key;
            ydata[cnt] = stations.get(key).getAggregateRollingSpeed();
            System.out.println("StationData - "+xdata[cnt] + " : " + ydata[cnt]);
            cnt++;
        }
        super.addXYGraph(xdata, ydata, name);
        super.addXYGraph(xdata, ydata, name+"DISC",new TracePainterDisc(),Color.blue);
    }
    
    public void AddDMSSpeedGraph(TreeMap<Integer,DMSImpl> dmss, String name){
        double[] xdata = new double[dmss.size()];
        double[] ydata = new double[dmss.size()];
        int cnt = 0;
        for(Integer key : dmss.keySet()){
            xdata[cnt] = key;
            ydata[cnt] = dmss.get(key).getSpeedLimit();
            cnt++;
        }
        super.addXYGraph(xdata, ydata, name, new TracePainterDisc(), Color.black);
    }
}
