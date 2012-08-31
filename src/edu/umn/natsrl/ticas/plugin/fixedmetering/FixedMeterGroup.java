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
package edu.umn.natsrl.ticas.plugin.fixedmetering;

import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class FixedMeterGroup {
    private ArrayList<FixedMeter> meters;
    private ArrayList<Integer> intervalList = new ArrayList<Integer>();
    private String FilePath;
    PropertiesWrapper prop;
    
    private String METERCOUNT = "METER_COUNT";
    private String METERID = "_METER_ID_";
    private String RATEDATA = "_RATE_DATA_";
    public static final String INTERVALLIST = "INTERVAL_LIST";
    
    private String SAVE_NAME;
    public static final String SAVE_FILE_EXTEND = ".ticas";
    
    private int currentInterval = 30;
    
    public FixedMeterGroupErrorType errortype = FixedMeterGroupErrorType.NOT_LOADED;
    
    public FixedMeterGroup(){;}
    public FixedMeterGroup(String filepath, ArrayList<FixedMeter> m, int itv){
        meters = m;
        FilePath = filepath;
        currentInterval = itv;
        SAVE_NAME = getFileName(FilePath);
        System.out.println(SAVE_NAME);
        intervalList = FixedMeterGroup.getIntervalList(FilePath);
        errortype = FixedMeterGroupErrorType.SUCCESS;
    }
    public FixedMeterGroup(PropertiesWrapper _prop,int cInterval, ArrayList<SimMeter> simMeters){
        this.prop = _prop.clone();
        currentInterval = cInterval;
        int MeterCount = prop.getInteger(METERCOUNT);
        
        /**
         * Structure Check
         */
        if(MeterCount != simMeters.size()){
            errortype = FixedMeterGroupErrorType.ERROR_STRUCTURE;
            return;
        }
        /**
         * set Interval List
         */
        for(String itvlist : prop.getStringArray(INTERVALLIST)){
            this.intervalList.add(Integer.parseInt(itvlist));
        }
        
        //find Interval
        boolean isinterval = false;
        for(int interval : intervalList){
            if(interval == cInterval)
                isinterval = true;
        }
        
        if(!isinterval)
            return;
        
        meters = new ArrayList<FixedMeter>();
        
        for(int i=0;i<MeterCount;i++){
            FixedMeter tempMeter = null;
            String tMeterID = prop.get(currentInterval+this.METERID+i);
            
            boolean isload = false;
            for(SimMeter smeter : simMeters){
                if(smeter.getId().equals(tMeterID)){
                    tempMeter = new FixedMeter(smeter,currentInterval);
                    isload = true;
                }
            }
            
            if(!isload){
                errortype = FixedMeterGroupErrorType.ERROR_STRUCTURE;
                return;
            }
            
            tempMeter.setRateList(prop.getStringList(currentInterval+RATEDATA+i));
            meters.add(tempMeter);
        }
        
        errortype = FixedMeterGroupErrorType.SUCCESS;
    }
    
    public void Save(){
        prop = new PropertiesWrapper();
        prop.put(METERCOUNT,meters.size()); //set MeterCount
        
        /**
         * ReWrite Another Interval
         */
        int cnt = 0;
        for(int itv : intervalList){
            if(itv != this.currentInterval){
//                System.out.println("Interval: "+itv);
                FixedMeterGroup mGroup = FixedMeterGroup.load(FilePath, itv, this.getSimMeters());
                if(mGroup.errortype == FixedMeterGroupErrorType.SUCCESS)
                    Save(itv,mGroup.meters);
                else
                    intervalList.remove(cnt);
            }
            cnt++;
        }
        
        /**
         * insert new interval
         */
        if(!isCurrentInterval()){
            intervalList.add(currentInterval);
        }
        
        /**
         * insert New Interval Data
         */
        Save(this.currentInterval,this.meters);
        
        /**
         * set InsertList
         */
        prop.put(FixedMeterGroup.INTERVALLIST,getIntervalList());
        
        prop.save(getFileName(FilePath));
    }
    
    private void Save(int ITV, ArrayList<FixedMeter> meters){
        int count = 0;
        for(FixedMeter meter : meters){
            prop.put(ITV+METERID+count,meter.meter.getId());
            prop.put(ITV+RATEDATA+count,meter.getRateLists());
            count++;
        }
    }
    
    public List<String> getIntervalList(){
        List<String> l = new ArrayList<String>();
        for(Integer i : this.intervalList)
                l.add(String.valueOf(i));
        
        return l;
    }
    
    private boolean isCurrentInterval(){
        for(int itv : this.intervalList){
            if(itv == this.currentInterval)
                return true;
        }
        return false;
    }
    
    public ArrayList<SimMeter> getSimMeters(){
        ArrayList<SimMeter> sMeter = new ArrayList<SimMeter>();
        for(FixedMeter fmeter : this.meters){
            sMeter.add(fmeter.meter);
        }
        return sMeter;
    }
    public ArrayList<FixedMeter> getFixedMeters(){
        return this.meters;
    }
    
    /**
     * get File Name
     * @param fp
     * @return 
     */
    public static String getFileName(String fp){
        String[] fs = fp.split("\\\\");
        String[] ffs = fs[fs.length-1].split("\\.");
        if(ffs.length <= 0)
            return "null";
        
        String fullpath="";
        for(int i=0;i<fs.length-1;i++){
            fullpath += fs[i]+"\\";
        }
        fullpath += ffs[0]+FixedMeterGroup.SAVE_FILE_EXTEND;
        return fullpath;
    }
    public static ArrayList<Integer> getIntervalList(String _path){
        try{
            PropertiesWrapper prop = PropertiesWrapper.load(getFileName(_path));
            ArrayList<Integer> ilist = new ArrayList<Integer>();
            for(String itvlist : prop.getStringArray(FixedMeterGroup.INTERVALLIST))
                ilist.add(Integer.parseInt(itvlist));
            return ilist;
        }catch(Exception e){
//            e.printStackTrace();
            return new ArrayList<Integer>();
        }
    }
    public static FixedMeterGroup load(String _path, int itv, ArrayList<SimMeter> meters){
        try{
            PropertiesWrapper prop = PropertiesWrapper.load(getFileName(_path));
            return new FixedMeterGroup(prop,itv,meters);
        }catch(Exception e){
//            e.printStackTrace();
            return new FixedMeterGroup();
        }
    }
    
}
