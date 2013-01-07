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
package edu.umn.natsrl.ticas.plugin.rampmeterevaluator;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.Date;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterNode {
    private Period period;
    private Entrance entrance;
    private RampMeter meter;
    private int realinterval = 30;
    public int Tlength = 0;
    
    private String RampStart;
    private String RampEnd;
    private boolean isRampMeter=false;
    
    /**
     * default Data
     */
    private double[] queue_flow;
    private double[] queue_volume;
    private double[] passage_flow;
    private double[] passage_volume;
    
    private double[] c_queue_flow;
    private double[] c_queue_volume;
    private double[] c_passage_flow;
    private double[] c_passage_volume;
    
    Detector green;
    private double[] green_vol;
    private double[] green_flow;
    
    /**
     * Interval
     */
    private double[] Rqueue_flow;
    private double[] Rqueue_volume;
    private double[] Rpassage_flow;
    private double[] Rpassage_volume;
    
    private double[] Rc_queue_flow;
    private double[] Rc_queue_volume;
    private double[] Rc_passage_flow;
    private double[] Rc_passage_volume;
    
    private double[] Rgreen_vol;
    private double[] Rgreen_flow;
    
    private double[] queueLength_f;
    private double[] queueLength_v;
    
    private double[] waitTime;
    
    
    
    private boolean isProcessed = false;
    private boolean isSet = false;
    private Date RampStartDate;
    private Date RampEndDate;
    
    RampMeterNode(SectionHelper.EntranceState rNode, Period p,int _interval) {
        entrance = (Entrance)rNode.getRNode();
        meter = rNode.getMeter();
        period = p;
        realinterval = _interval;
    }

    /**
     * set Deafult Data
     * @param rampDemandOld queue Flow
     * @param rampDemandOld0 queue Volume
     * @param rampFlowNew Passage Flow
     * @param rampVolume  Passage Volume
     * @param green green data
     */
    void setDefaultData(double[] rampDemandOld, double[] rampDemandOld0, double[] rampFlowNew, double[] rampVolume, Detector green) {
        int length = period.getTimeline().length;
        queue_flow = rampDemandOld;
        queue_volume = rampDemandOld0;
        passage_flow = rampFlowNew;
        passage_volume = rampVolume;
        
        c_queue_flow = new double[length];
        c_queue_volume = new double[length];
        c_passage_flow = new double[length];
        c_passage_volume = new double[length];
        
        green_vol = new double[length];
        green_flow = new double[length];
        this.green = green;
        if(green != null){
            green_vol = green.getVolume();
            green_flow = green.getFlow();
        }
        if(period.interval != realinterval)
            length = length / (realinterval/period.interval);
        
        /**
         * interval data
         */
        Rqueue_flow = new double[length];
        Rqueue_volume = new double[length];
        Rpassage_flow = new double[length];
        Rpassage_volume = new double[length];
        
        Rc_queue_flow = new double[length];
        Rc_queue_volume = new double[length];
        Rc_passage_flow = new double[length];
        Rc_passage_volume = new double[length];
        
        Rgreen_vol = new double[length];
        Rgreen_flow = new double[length];
        
        queueLength_f = new double[length];
        queueLength_v = new double[length];

        waitTime = new double[length];
        
        Tlength = length;
        
        
//        period.setInterval(realinterval);
        isSet = true;
    }

    /**
     * calculate Cumulative Value, Wait Time and QUeue Storage
     */
    void process() {
//        printState();
        
        if(!isSet)
            return;
        if(!isActive())
            return;
        
        try{
            calculateCulmulative(queue_flow,passage_flow,c_queue_flow,c_passage_flow);
            calculateCulmulative(queue_volume,passage_volume,c_queue_volume,c_passage_volume);
            
            /**
             * set Interval Data
             */
            Rqueue_flow = AvgDataInterval(queue_flow,Rqueue_flow.length);
            Rqueue_volume = SyncDataInterval(queue_volume,Rqueue_volume.length);
            Rpassage_flow = AvgDataInterval(passage_flow,Rpassage_flow.length);
            Rpassage_volume = SyncDataInterval(passage_volume,Rpassage_volume.length);
            Rgreen_flow = AvgDataInterval(green_flow,Rgreen_flow.length);
            Rgreen_vol = SyncDataInterval(green_vol,Rgreen_vol.length);
            
            calculateCulmulative(Rqueue_flow,Rqueue_volume,Rc_queue_flow,Rc_passage_flow);
            calculateCulmulative(Rqueue_volume,Rpassage_volume,Rc_queue_volume,Rc_passage_volume);
            
            //RampMeter Check
            RampAvailableCheck();
            /**
             * set WaitTime, Storage
             */
            if(isMeter()){
                queueLength_f = calculateStorage(c_queue_volume,c_passage_volume,queueLength_f.length);
                queueLength_v = calculateStorage(c_queue_volume,c_passage_volume,queueLength_v.length);
                waitTime = getWaitTime(c_queue_volume,c_passage_volume,waitTime.length);
            }
            isProcessed = true;
        }catch(Exception e){
            e.printStackTrace();
            isProcessed = false;
        }
    }

    /**
     * 
     * @param input original input data
     * @param output original output data
     * @param ci culmulative input data
     * @param co culmulative output data
     */
    private void calculateCulmulative(double[] input, double[] output, double[] ci, double[] co) {
        for(int i=0;i<input.length;i++){
            if(i != 0){
                ci[i] = input[i] >= 0 ? input[i] + ci[i-1] : ci[i-1];
                co[i] = output[i] >= 0 ? output[i] + co[i-1] : co[i-1];
            }else{
                ci[i] = input[i] >= 0 ? input[i] : 0;
                co[i] = output[i] >= 0 ? output[i] : 0;
            }
            
            /**
            * if Queue > Passage -> init data
            */
            if(ci[i] < co[i]){
                ci[i] = 0;
                co[i] = 0;
            }
        }
    }
    public void printState(){
        System.out.println("- state : queue-"+isQueue()+ ", passage-"+isPassage() +", meter-"+isMeter());
    }
    public void printFlow(){
        printdata(queue_flow,passage_flow,c_queue_flow,c_passage_flow);
    }
    public void printVolume(){
        printdata(queue_volume,passage_volume,c_queue_volume,c_passage_volume);
    }
    public void printdata(double[] input, double[] output, double[] ci, double[] co){
        if(!this.isProcessed)
            return;
        
        System.out.println(getLabel() + "("+getId()+") - state : "+isQueue());
        
        for(int i=0;i<input.length;i++){
            System.out.println("Queue :"+input[i]+" - "+ci[i] + " ====== Passage : " +output[i] + " - " +co[i] + "==> waitTime : "+this.waitTime[i]);
        }
    }
    
    /**
     * Calculate Queue Storage
     * @param input
     * @param output
     * @param storage 
     */
    private double[] calculateStorage(double[] input, double[] output, int length) {
        double[] temp = new double[input.length];
        for(int i=0;i<input.length;i++)
            temp[i] = input[i] - output[i];
        
        return SyncDataInterval(temp,length);
    }
    
    /**
     * get WaitTime
     * @param input cumulative input
     * @param output cumulative output
     * @return 
     */
    private double[] getWaitTime(double[] input, double[] output,int length) {
        double[] wait = new double[input.length];
        int maxWaitTimeIndex = 16;
        BoundedSampleHistory cumulativeDemand = new BoundedSampleHistory(maxWaitTimeIndex+1);
        for(int i=0;i<output.length;i++){
            double Cf_current = output[i];
            cumulativeDemand.push(input[i]);
            wait[i] = CalculateWaitTime(cumulativeDemand,Cf_current,maxWaitTimeIndex);
        }
        return AvgDataInterval(wait,length);
    }

    /**
     * Calculate WaitTime
     * @param cumulativeDemand
     * @param Cf_current
     * @param maxWaitTimeIndex
     * @return 
     */
    private double CalculateWaitTime(BoundedSampleHistory cumulativeDemand, double Cf_current, int maxWaitTimeIndex) {
        int STEP_SECONDS = period.interval;
        // current cumulative passage flow
        if(Cf_current == 0)
            return 0;
        
        if(cumulativeDemand.size() - 1 < maxWaitTimeIndex-1)
                return 0;

        for(int i = 0; i < cumulativeDemand.size(); i++) {
                double queue = cumulativeDemand.get(i);
                if(i == 0 && queue == Cf_current)
                    return 0;
                if(queue < Cf_current) {
                        double bf_queue =
                                cumulativeDemand.get(i - 1);
                        double qd = bf_queue - queue;
                        if(qd != 0) {
                                double IF_time = STEP_SECONDS *
                                        (Cf_current - queue) / qd;
                                return  STEP_SECONDS * i -
                                        IF_time;

                        } else{
                                return 0;
                        }
                }
        }
        return (maxWaitTimeIndex) * (STEP_SECONDS);
    }
    
    public String getLabel(){
        if(isMeter())
            return this.meter.getLabel();
        else
            return entrance.getLabel();
    }

    public String getId() {
        if(isMeter())
            return this.meter.getId();
        else
            return null;
    }
    public boolean isPassage(){
        if(entrance.getPassage() != null)
            return true;
        else return false;
    }
    public boolean isQueue(){
        if(!entrance.getQueue().isEmpty())
            return true;
        else
            return false;
    }
    
    public boolean isMeter(){
        if(meter != null)
            return true;
        else
            return false;
    }
    public boolean isActive(){
        return isQueue() && isMeter();
    }
    
    public double[] getQueue(TrafficType type){
        if(!isQueue())
            return null;
        
        if(type.isFlow())
            return this.Rqueue_flow;
        else
            return this.Rqueue_volume;
    }
    
    public double[] getPassage(TrafficType type){
        if(!isPassage())
            return null;
        
        if(type.isFlow())
            return this.Rpassage_flow;
        else
            return this.Rpassage_volume;
    }
    
    public double[] getCumulativeQueue(TrafficType type){
        if(!isQueue())
            return null;
        
        if(type.isFlow())
            return this.Rc_queue_flow;
        else
            return this.Rc_queue_volume;
    }
    
    public double[] getCumulativePassage(TrafficType type){
        if(!isPassage())
            return null;
        
        if(type.isFlow())
            return this.Rc_passage_flow;
        else
            return this.Rc_passage_volume;
    }
    
    public double[] getGreenData(TrafficType type){
        if(!isActive())
            return null;
        if(type.isFlow())
            return this.Rgreen_flow;
        else
            return this.Rgreen_vol;
    }
    
    public double[] getDefaultQueue(TrafficType type){
        if(!isQueue())
            return null;
        
        if(type.isFlow())
            return this.queue_flow;
        else
            return this.queue_volume;
    }
    
    public double[] getDefaultPassage(TrafficType type){
        if(!isPassage())
            return null;
        
        if(type.isFlow())
            return this.passage_flow;
        else
            return this.passage_volume;
    }
    
    public double[] getDefaultCumulativeQueue(TrafficType type){
        if(!isQueue())
            return null;
        
        if(type.isFlow())
            return this.c_queue_flow;
        else
            return this.c_queue_volume;
    }
    
    public double[] getDefaultCumulativePassage(TrafficType type){
        if(!isPassage())
            return null;
        
        if(type.isFlow())
            return this.c_passage_flow;
        else
            return this.c_passage_volume;
    }
    
    public double[] getDefaultGreenData(TrafficType type){
        if(!isActive())
            return null;
        if(type.isFlow())
            return this.green_flow;
        else
            return this.green_vol;
    }
    
    
    
    public double[] getWaitTime(){
        if(!isActive())
            return null;
        
        return this.waitTime;
    }
    
    public double[] getQueueStorage(TrafficType type){
        if(!isActive())
            return null;
        if(type.isFlow())
            return this.queueLength_f;
        else
            return this.queueLength_v;
    }
    
    public String getRampStartTime(){
        return this.RampStart;
    }
    public String getRampEndTime(){
        return this.RampEnd;
    }
    public Date getRampStartDate(){
        return this.RampStartDate;
    }
    public Date getRampEndDate(){
        return this.RampEndDate;
    }
    public boolean isRampMeteringActive(){
        return this.isRampMeter;
    }

    private double[] SyncDataInterval(double[] temp, int length) {
        double[] data = new double[length];
        int cnt = 1;
        int i = 0;
        double R_data = 0;
        for(double d : temp){
            if(d > 0)
                R_data += d;
            if(cnt % (realinterval/period.interval) == 0){
                data[i++] = R_data;
                R_data = 0;
            }
            cnt ++;
        }
        return data;
    }
    
    /**
     * Caution : Average is available in Metering Time, it should be make in option( is Metering Time or Total Time)
     * @deprecated 
     * @param temp
     * @param length
     * @return 
     */
    private double[] AvgDataInterval(double[] temp, int length) {
        double[] data = new double[length];
        int cnt = 1;
        int i = 0;
        double R_data = 0;
        double reduce_cnt = 0;
        for(double d : temp){
            if(d > 0 && this.green_vol[cnt-1] > 0){
                R_data += d;
            }else{
                reduce_cnt ++;
            }
            if(cnt % (realinterval/period.interval) == 0){
                double avg = R_data <= 0 ? 0 : R_data / ((realinterval/period.interval)-reduce_cnt);
                data[i++] = avg;
                R_data = 0;
                reduce_cnt = 0;
            }
            cnt ++;
        }
        return data;
    }

    private void RampAvailableCheck() {
        double[] data = this.Rgreen_vol;
        int sIdx = 0;
        int eIdx = 0;
        boolean isStart=false;
        for(int i=0;i<data.length;i++){
            if(!isStart && data[i] > 0){
                isStart = true;
                isRampMeter = true;
                sIdx = i;
            }
            
            if(isStart && data[i] > 0)
                eIdx = i;
        }
        RampStart = RampMeterUtil.getTime(period, sIdx, realinterval);
        RampEnd = RampMeterUtil.getTime(period, eIdx, realinterval);
        RampStartDate = RampMeterUtil.getDateTime(period, sIdx, realinterval);
        RampEndDate = RampMeterUtil.getDateTime(period, eIdx, realinterval);
        System.out.println("- Ramp Active : sIdx : "+RampStart+"("+sIdx+")"+" eIdx : "+RampEnd+"("+eIdx+")");
    }
    
    public void setRealInterval(int interval){
        this.realinterval = interval;
    }

    

    
    
    

    
    
}
