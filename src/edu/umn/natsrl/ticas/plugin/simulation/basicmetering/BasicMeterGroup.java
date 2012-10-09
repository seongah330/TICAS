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
package edu.umn.natsrl.ticas.plugin.simulation.basicmetering;

import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.ticas.Simulation.SectionHelper;
import edu.umn.natsrl.ticas.Simulation.SectionHelper.EntranceState;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicMeterGroup {
    private ArrayList<BasicMeter> meters;
    private String FilePath;
    PropertiesWrapper prop;
    
    private String METERCOUNT = "METER_COUNT";
    private String METERID = "_METER_ID_";
    private String DATA = "_DATA_";
    private String _UPSTREAM = "_UPSTREAM_";
    private String _DOWNSTREAM = "_DOWNSTREAM_";
    
    private String SAVE_NAME;
    public static final String SAVE_FILE_EXTEND = ".tibmg";
    
    public BasicMeterGroupErrorType errortype = BasicMeterGroupErrorType.NOT_LOADED;
    
    public BasicMeterGroup(){;}
    public BasicMeterGroup(String filepath, ArrayList<BasicMeter> m){
        meters = m;
        FilePath = filepath;
        SAVE_NAME = getFileName(FilePath);
        System.out.println(SAVE_NAME);
        errortype = BasicMeterGroupErrorType.SUCCESS;
    }
    public BasicMeterGroup(PropertiesWrapper _prop, SectionHelper sh){
        this.prop = _prop.clone();
        int MeterCount = prop.getInteger(METERCOUNT);
        
        ArrayList<EntranceState> esMeter = sh.getEntranceStates(true);
        /**
         * Structure Check
         */
        if(MeterCount != esMeter.size()){
            errortype = BasicMeterGroupErrorType.ERROR_STRUCTURE;
            return;
        }
        
        meters = new ArrayList<BasicMeter>();
        
        for(int i=0;i<MeterCount;i++){
            BasicMeter tempMeter = null;
            String tMeterID = prop.get(this.METERID+i);
            
            boolean isload = false;
            for(EntranceState smeter : esMeter){
                if(smeter.getID().equals(tMeterID)){
                    BasicMeterConfig bcfg = new BasicMeterConfig();
                    
                    double[] cfgdatas = prop.getDoubleArray(this.DATA+i);
                    bcfg.setDatas(cfgdatas);
                    
                    tempMeter = new BasicMeter(smeter,bcfg);
                    
                    String uSID = prop.get(this._UPSTREAM+i);
                    String dSID = prop.get(this._DOWNSTREAM+i);
                    
                    tempMeter.associateStationStream(uSID, dSID, sh);
                    if(tempMeter.getError().isSUCCESS())
                        isload = true;
                }
            }
            
            if(!isload){
                errortype = BasicMeterGroupErrorType.ERROR_STRUCTURE;
                return;
            }
            
            meters.add(tempMeter);
        }
        
        errortype = BasicMeterGroupErrorType.SUCCESS;
    }
    
    public void Save(){
        prop = new PropertiesWrapper();
        prop.put(METERCOUNT,meters.size()); //set MeterCount
        
        /**
         * set InsertList
         */
        int count = 0;
        for(BasicMeter meter : meters){
            prop.put(METERID+count,meter.meter.getId());
            prop.put(DATA+count,meter.getConfigDatas());
            prop.put(_UPSTREAM+count, meter.getUpStreamStationID());
            prop.put(_DOWNSTREAM+count, meter.getDownStreamStationID());
            count++;
        }
        
        prop.save(getFileName(FilePath));
    }
    
    public ArrayList<SimMeter> getSimMeters(){
        ArrayList<SimMeter> sMeter = new ArrayList<SimMeter>();
        for(BasicMeter bmeter : this.meters){
            sMeter.add(bmeter.meter);
        }
        return sMeter;
    }
    public ArrayList<BasicMeter> getBasicMeters(){
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
        fullpath += ffs[0]+BasicMeterGroup.SAVE_FILE_EXTEND;
        return fullpath;
    }
//    public static ArrayList<Integer> getIntervalList(String _path){
//        try{
//            PropertiesWrapper prop = PropertiesWrapper.load(getFileName(_path));
//            ArrayList<Integer> ilist = new ArrayList<Integer>();
//            for(String itvlist : prop.getStringArray(BasicMeterGroup.INTERVALLIST))
//                ilist.add(Integer.parseInt(itvlist));
//            return ilist;
//        }catch(Exception e){
////            e.printStackTrace();
//            return new ArrayList<Integer>();
//        }
//    }
    public static BasicMeterGroup load(String _path, SectionHelper sh){
        try{
            PropertiesWrapper prop = PropertiesWrapper.load(getFileName(_path));
            return new BasicMeterGroup(prop,sh);
        }catch(Exception e){
//            e.printStackTrace();
            return new BasicMeterGroup();
        }
    }
    
}
