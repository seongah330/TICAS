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

import edu.umn.natsrl.infra.infraobjects.Station;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEStation {
    String id;
    int groupid;
    
    private String Label;
    private double speedLimit;
    private ArrayList<Station> station = new ArrayList<Station>();
    private String startStation;
    private String endStation;
    private int TotalStation = 0;
    
    private double[] u = null;
    private double[] k = null;
    private double[] q = null;
    private int qavgCount = 0;
    private int kavgCount = 0;
    private int uavgCount = 0;
    
    public SRTEStation(String _id, int _groupid){
        id = _id;
        groupid = _groupid;
    }
    public SRTEStation(String _id, int _groupid, Station _station){
        id = _id;
        groupid = _groupid;
        speedLimit = _station.getSpeedLimit();
        
        if(groupid == -1)
            Label = _station.getLabel();
        else
            Label = id;

        AddData(_station);
    }
    
    private void updateState(Station _station) {
        if(station.size() <= 0){
            startStation = _station.getStationId();
            endStation = _station.getStationId();
        }else
            endStation = _station.getStationId();
        
        if(groupid != -1)
            id = startStation+"-"+endStation;
        
        station.add(_station);
    }
    
    public String getLabel(){
        return Label;
    }
    public double getSpeedLimit(){
        return speedLimit;
    }
    public String getStationId(){
        return id;
    }

    public void AddData(Station station) {
        if(hasData(station.getSpeed())){
            if(u == null)
                u = getData(station.getSpeed());
            else
                u = AddData(u,station.getSpeed());
            uavgCount++;
        }
        
        if(hasData(station.getDensity())){
            if(k == null)
                k = getData(station.getDensity());
            else
                k = AddData(k,station.getDensity());
            kavgCount++;
        }
        
        if(hasData(station.getAverageLaneFlow())){
            if(q == null)
                q = getData(station.getAverageLaneFlow());
            else
                q = AddData(q,station.getAverageLaneFlow());
            qavgCount++;
        }
        
        TotalStation++;
        updateState(station);

    }
    
    private double[] AddData(double[] origin, double[] addition) {
        if(origin.length != addition.length)
            return origin;
        
        double[] temp = new double[origin.length];
        for(int i=0;i<origin.length;i++){
            double odata = origin[i] == -1 ? 0 : origin[i];
            double adata = addition[i] == -1 ? 0 : addition[i];
            temp[i] = odata+adata;
        }
        return temp;
        
    }
    
    /**
     * has Data
     * @param data
     * @return 
     */
    private boolean hasData(double[] data){
        for(double d : data){
            if(d > 0)
                return true;
        }
        return false;
    }
    
    private double[] getData(double[] data){
        double[] temp = new double[data.length];
        for(int i=0;i<data.length;i++){
            temp[i] = data[i];
        }
        return temp;
    }

    public void SyncAverage() {
        if(u == null || q == null || k == null)
            return;
        
        if(uavgCount > 0)
            u = avgData(u,uavgCount);
        if(qavgCount > 0)
            q = avgData(q,qavgCount);
        if(kavgCount > 0)
            k = avgData(k,kavgCount);
    }

    private double[] avgData(double[] data, int avgCount) {
        double[] temp = new double[data.length];
        if(avgCount == 0)
            return data;
        
        for(int i=0;i<data.length;i++)
            temp[i] = data[i] / avgCount;
        
        return temp;
    }
    
    public void printAllData(){
        for(int i=0;i<q.length;i++){
            System.out.println("q:"+q[i]+" - k:"+k[i]+" - u:"+u[i]);
        }
    }

    String getAvgCount() {
        return "TotalStation-"+this.TotalStation+" q-"+this.qavgCount+", k-"+this.kavgCount+", u-"+this.uavgCount;
    }

    double[] getSpeed() {
        return u;
    }

    double[] getAverageLaneFlow() {
        return q;
    }

    double[] getDensity() {
        return k;
    }
    int getTotalStation(){
        return this.TotalStation;
    }

    

    
}
