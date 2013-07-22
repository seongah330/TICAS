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

import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.plugin.srte2.SMOOTHING;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTESection {
    String id;
    
    private String Label;
    private double speedLimit;
    private int TotalStation = 0;
    private ArrayList<Station> station = new ArrayList<Station>();
    private String startStation;
    private String endStation;
    
    private double[] u = null;
    private double[] k = null;
    private double[] q = null;
    
    private double[] data_smoothed;   // smoothed 5 min speed data
    private double[] data_quant;   // 5 min speed data after quantization
    
    private double[] u_Avg_origin;
    private double[] u_Avg_smoothed;
    private double[] u_Avg_quant;

    private double[] k_smoothed;
    private double[] k_quant;
    
    private double[] q_smoothed;
    private double[] q_quant;
    
    private int qavgCount = 0;
    private int kavgCount = 0;
    private int uavgCount = 0;
    
    private int SMOOTHING_FILTERSIZE = 1;
    private int QUANTIZATION_THRESHOLD = 2;
    private SMOOTHING sfilter;
    
    private boolean hasData = true;
    
    public SRTESection(String _id,SRTEConfig config){
        id = _id;
        // config setting
        this.SMOOTHING_FILTERSIZE = config.getInt("SMOOTHING_FILTERSIZE");
        this.QUANTIZATION_THRESHOLD = config.getInt("QUANTIZATION_THRESHOLD");
        sfilter = SMOOTHING.getSmooth(config.getInt(SRTEConfig.SMOOTHINGOPTION));
        calcFilter();
    }
    public SRTESection(String _id, Station _station,SRTEConfig _config){
        this(_id,_config);
        speedLimit = _station.getSpeedLimit();
        
        Label = id;

        AddData(_station);
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
    
    private void updateState(Station _station) {
        if(station.size() <= 0){
            startStation = _station.getStationId();
            endStation = _station.getStationId();
        }else
            endStation = _station.getStationId();
        
        if(endStation != null)
                id = startStation+"-"+endStation;
        
        station.add(_station);
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
    
    public void SyncAverage() {
        if(u == null || q == null || k == null)
            return;
        if(uavgCount > 0)
            u = avgData(u,uavgCount);
        if(qavgCount > 0)
            q = avgData(q,qavgCount);
        if(kavgCount > 0)
            k = avgData(k,kavgCount);
        
        calcFilter();
    }
    
    private double[] avgData(double[] data, int avgCount) {
        double[] temp = new double[data.length];
        if(avgCount == 0)
            return data;
        
        for(int i=0;i<data.length;i++)
            temp[i] = data[i] / avgCount;
        
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
    
    double[] getSmoothedSpeed(){
            return this.data_smoothed;
    }
    
    double[] getQuantSpeed(){
            return this.data_quant;
    }

    double[] getAverageLaneFlow() {
        return q;
    }
    
    double[] getSmoothedAverageLaneFlow(){
            return q_smoothed;
    }
    
    double[] getQuantAverageLaneFlow(){
            return q_quant;
    }

    double[] getDensity() {
        return k;
    }
    
    double[] getSmoothedDensity(){
            return k_smoothed;
    }
    
    double[] getQuantDensity(){
            return k_quant;
    }
    
    double[] getAverageSpeed(){
            return this.u_Avg_origin;
    }
    
    double[] getSmoothedAverageSpeed(){
            return this.u_Avg_smoothed;
    }
    
    double[] getQuantAverageSpeed(){
            return this.u_Avg_quant;
    }
    
    int getTotalStation(){
        return this.TotalStation;
    }
    
    public boolean hasData(){
            return hasData;
    }

    /**
     * Moving average method
     * example, Data at 10:30 is averaged from 10:15 to 10:45 (45min averaged data)
     * @param data 15 min route-wide data
     * @return array of smoothed data
     */
    private double[] smoothing(double[] data) {
        int i = 0;
        int j = 0;
        double tot = 0;
        double[] filteredData = new double[data.length];

        if(sfilter == null)
            return data;
        
        if(sfilter.isNOSMOOTHING())
            return data;
        
        if(sfilter.isSecond()){
            for(i=0;i<data.length;i++){
                if(i==0){
                    filteredData[i] = data[i];
                    continue;
                }
                filteredData[i] = (data[i-1] + data[i])/2;
            }
        }else{
            if(sfilter.isDefault())
                SMOOTHING_FILTERSIZE = sfilter.getCount();
            
            for (i = 0; i < SMOOTHING_FILTERSIZE; i++) {
                filteredData[i] = data[SMOOTHING_FILTERSIZE - 1];
            }
            // 0 ~ 11
            for (i = SMOOTHING_FILTERSIZE; i < data.length - SMOOTHING_FILTERSIZE; i++) {
                for (j = i - SMOOTHING_FILTERSIZE; j <= i + SMOOTHING_FILTERSIZE; j++) {
                    tot += data[j];
                }
                filteredData[i] = tot / (2 * SMOOTHING_FILTERSIZE + 1);
                tot = 0;
            }

            for (i = data.length - SMOOTHING_FILTERSIZE; i <= data.length - 1; i++) {
                filteredData[i] = filteredData[data.length - SMOOTHING_FILTERSIZE - 1];
            }
        }

        return filteredData;
    }

    /**
     * Quantization : the process of approximating the continuous set of values in the speed data with a finite set of values
     *  - Find out approximate points of speed change
     *  - example, threshold 2 mi/h,
     *      -filtered data: 56,56,56,55,54,53,52,51,50,50,49,49,46,46
     *      -Output       : 56,56,56,56,54,54,52,52,50,50,50,50,46,46
     *
     * @param filteredData 45min route-wide average data
     * @return
     */
    private double[] quantization(double[] filteredData, Integer TH) {
        int stick = 0;
        double[] qData = new double[filteredData.length];
        for (int i = 0; i < 3; ++i) {
            stick += filteredData[i];
        }
        stick = stick / 3;

        for (int i = 0; i <= filteredData.length - 1; i++) {
            if ((Math.abs(stick - filteredData[i]) > TH)) {
                stick = (int) Math.round(filteredData[i]);
            }
            qData[i] = stick;
        }
        return qData;
    }
    private double[] quantization(double[] filteredData) {
        return quantization(filteredData,QUANTIZATION_THRESHOLD);
    }

        private void calcFilter() {
                if(getSpeed() == null){
                        hasData = false;
                        return;
                }
                //Flow
                q_smoothed = smoothing(q);
                q_quant = quantization(q_smoothed);
                
                //Density
                k_smoothed = smoothing(k);
                k_quant = quantization(k_smoothed);
                
                //speed
                data_smoothed = smoothing(u);
                data_quant = quantization(data_smoothed);
                
                //average u
                u_Avg_origin = SRTEUtil.CalculateSmoothedSpeed(q, k);
                u_Avg_smoothed = SRTEUtil.CalculateSmoothedSpeed(q_smoothed, k_smoothed);
                u_Avg_quant = quantization(u_Avg_smoothed);
        }

    
}
