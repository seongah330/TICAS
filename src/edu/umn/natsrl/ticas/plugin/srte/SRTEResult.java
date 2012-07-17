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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEResult {
    public String sectionName;
    public Calendar cs,ce,cb;
    public Period period;
    /**
     * @deprecated
     */
    public int srst; // speed decline point for 5 min data
    /**
     * @deprecated
     */
    public int lst; // low speed point for 5 min data
    /**
     * @deprecated
     */
    public int rst; // speed recovery point for 5 min data
    /**
     * @deprecated
     * */
    public ArrayList<Integer> srt = new ArrayList<Integer>(); // stable speed point for 5 min data
    
    /**
     * new Algorithm
     */
    public Station station;
    /**
     * PatternType
     */
    public PatternType pType;
    private ArrayList<ResultPoint> point = new ArrayList<ResultPoint>();
    private ResultPoint currentPoint = new ResultPoint();
    private HashMap<Integer,SpeedMap> speedStempList = new HashMap<Integer,SpeedMap>();
    private HashMap<Integer,Double> speedData = new HashMap<Integer,Double>();

    private ArrayList<ResultRCRAccPoint> RCRAccPoint = new ArrayList<ResultRCRAccPoint>();
    public double[] data_origin;   // original 5 min speed data
    public double[] data_smoothed;   // smoothed 5 min speed data
    public double[] data_quant;   // 5 min speed data after quantization
    
    public double[] u_Avg_origin;
    public double[] u_Avg_smoothed;
    public double[] u_Avg_quant;

    public double[] k_smoothed;
    public double[] k_origin;
    public double[] k_quant;
    
    public double[] q_origin;
    public double[] q_smoothed;
    public double[] q_quant;
    
    public int[] phases;
    public List<String> msgs = new ArrayList<String>();
    public double SpeedLimit;
    /**
     * 
     */
    public static class ResultRCRAccPoint implements Comparable{
        int point = -1;
        double data = -1;
        
        public double getData(){
            return data;
        }

        @Override
        public int compareTo(Object o) {
            ResultRCRAccPoint cdata = (ResultRCRAccPoint)o;
            if(data == cdata.getData())
                return 0;
            else if(data < cdata.getData())
                return 1;
            else 
                return -1;
        }
    }
    public static class SpeedMap{
        public int time = -1;
        public double speed = -1;
        public double beforeSpeed = -1;
        private double interval = -1;
        private double tempStep = -1;
        SpeedMap(int ti, double sp, double bs,double ts,double inter){
            time = ti;
            speed = sp;
            beforeSpeed = bs;
            interval = inter;
            tempStep = ts;
        }
        public int getKeyTimeMin(){
            double minterval = interval / 60;
            
            if(minterval == 0)
                return 0;
            
            double currentTime = time * minterval;
            double beforeTime = (time-1) * minterval;
            double TimeGap = currentTime - beforeTime;
//            System.out.println("\nCTime:"+currentTime+" bTime:"+beforeTime+" TGap:"+TimeGap);
            double SpeedGap = (int)speed - (int)beforeSpeed;
            double SpeedStep = (int)tempStep - (int)beforeSpeed;
            
            if((int)tempStep == (int)speed)
                return (int)currentTime;
            if(currentTime == 0 || SpeedGap <= 0)
                return (int)currentTime;
            
            double SplitTime = TimeGap / SpeedGap;
//            System.out.println("SplitTime : " + SplitTime + " SpeedStep :" + SpeedStep);
            return (int)(SplitTime * SpeedStep + beforeTime);
        }
        public double getKeyTimeStep(){
            int keyTime = this.getKeyTimeMin();
            return keyTime * 60 / interval;
        }
    }
    
    public SRTEResult() {
        
    }

    public void setResult(int sdp, int lsp, int srp) {
        this.srst = sdp;
        this.lst = lsp;
        this.rst = srp;
    }
    public void setTime(Calendar CS,Calendar CE,Calendar CB){
        cs = CS;
        ce = CE;
        cb = CB;
    }
    public Calendar getStartTime(){
        return cs;
    }
    
    public Calendar getEndTime(){
        return ce;
    }
    
    public Calendar getBareLaneTime(){
        return cb;
    }
    public String getStartTimetoString(){
        if(cs == null)
            return null;
        else
            return getDatetoString(cs);
    }
    public String getEndTimetoString(){
        if(ce == null)
            return null;
        else
            return getDatetoString(ce);
    }
    public String getBareLaneTimetoString(){
        if(cb == null)
            return null;
        else
            return getDatetoString(cb);
    }
    public int getStartTimeStep(){
        if(cs == null)
            return -1;
        else
            return this.getTimeStep(period, cs);
    }
    public int getEndTimeStep(){
        if(ce == null)
            return -1;
        else
            return this.getTimeStep(period, ce);
    }
    public int getBareLaneTimeStep(){
        if(cb == null)
            return -1;
        else
            return this.getTimeStep(period, cb);
    }

    /**
     * @deprecated
     * */
    public void addSRT(int srt)
    {
        this.srt.add(srt);
    }

    void addLog(String msg) {
        addLog(msg,true);
    }
    void addLog(String msg,boolean entrance) {
        if(entrance)
            System.out.println(msg);
        else
            System.out.print(msg);
        this.msgs.add(msg);
    }

    private String getDatetoString(Calendar c) {
        if(c == null)
            return null;
        
        SimpleDateFormat dateformatter = new SimpleDateFormat("d MMM, yyyy 'at' HH:mm:ss");
        return dateformatter.format(c.getTime());
    }
    public void setPeriod(Period p){
        period = p;
    }
    public String getPeriodtoString(){
        if(period == null)
            return null;
        return period.getPeriodString();
    }
    
//    public List<int> getSpeedDataList(){
//        
//    }

    void addPoint(ResultPoint rp) {
        point.add(rp);
        if(currentPoint.isEmpty()){
            System.out.println("clst :"+currentPoint.lst+" plst:"+rp.lst+"-Emplty");
            currentPoint = rp.clone();
            return;
        }
        for(ResultPoint pn : point){
            System.out.println("clst :"+currentPoint.lst+" plst:"+pn.lst);

            /**
             * reset LST Location
             * Most Low Speed during LST's
             */
            if(this.data_smoothed[this.currentPoint.lst] >= this.data_smoothed[pn.lst]){
                currentPoint.setLST(pn.lst);
                System.out.println("clst :"+currentPoint.lst+" plst:"+pn.lst + "gotit");
            }
            
            /**
             * reset RST Location
             * The closest Point that End Time
             */
            int dCurrentRST = this.currentPoint.rst - this.getEndTimeStep();
            int dPnRST = pn.rst - getEndTimeStep();
            if(dCurrentRST > dPnRST)
                currentPoint.setRST(currentPoint.rst);
            else
                currentPoint.setRST(pn.rst);
            
        }
    }
    
    public Collection<ResultPoint> getPoint(){
        return point;
    }

    public ResultPoint getcurrentPoint() {
        return currentPoint;
    }
    
    /**
     * Add Speed Data
     * @param key
     * @param data 
     */
    public void AddSpeedData(int key, SpeedMap sm){
        speedStempList.put(key,sm);
    }

    public HashMap<Integer,SpeedMap> getSpeedList() {
        return this.speedStempList;
    }
    
    public Collection<Integer> getSpeedKeyList(){
        List<Integer> list = new ArrayList();
        Iterator<Integer> itr = speedStempList.keySet().iterator();
        while(itr.hasNext()){
            list.add(itr.next());
        }
        Collections.sort(list);
        return list;
    }
    
    private String getTime(Period p, int count){
        int tgap = p.interval/60;
        
        Calendar c = Calendar.getInstance();
        
        c.set(p.start_year, p.start_month-1, p.start_date, p.start_hour, p.start_min);
        for(int i=0; i<=count; i++) c.add(Calendar.MINUTE, tgap);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hour, min);
    }
    
    private int getTimeStep(Period p, Calendar cu){
        int tgap = p.interval/60;
        int count = 0;
        Calendar c = Calendar.getInstance();
        c.set(p.start_year, p.start_month-1, p.start_date, p.start_hour, p.start_min);
        while(true){
            c.add(Calendar.MINUTE, tgap);
            if(c.getTimeInMillis() > cu.getTimeInMillis())
                break;
            
            count++;
            
        }

        return count;
    }
    
    void AddRCRAccPoint(ResultRCRAccPoint AccPoint) {
        this.RCRAccPoint.add(AccPoint);
    }
    
    ArrayList<ResultRCRAccPoint> getRCRAccPointList(){
        return RCRAccPoint;
    }
    
    @Override
    public String toString(){
        return this.station.getLabel()+"("+station.getStationId()+")";
    }
    
    
}
