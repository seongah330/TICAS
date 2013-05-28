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
package edu.umn.natsrl.weatherRWIS.datas;

import edu.umn.natsrl.infra.Period;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class Atmospheric extends WeatherData implements WeatherDataImpl{
        ArrayList<Date> periodDate = new ArrayList();
        ArrayList<Date> Comparedate = new ArrayList();
        ArrayList<Short> temperature = new ArrayList();
        ArrayList<Short> maxTemp = new ArrayList();
        ArrayList<Short> minTemp = new ArrayList();
        ArrayList<Short> Rh = new ArrayList();
        ArrayList<PrecipType> PcType = new ArrayList();  //fix me
        ArrayList<PrecipIntens> pcIntens = new ArrayList(); //fix me
        ArrayList<Integer> pcAccum = new ArrayList();
        ArrayList<Integer> pcAccum1Hour = new ArrayList();
        ArrayList<Integer> pcAccum3Hour = new ArrayList();
        ArrayList<Integer> pcAccum6Hour = new ArrayList();
        ArrayList<Integer> pcAccum12Hour = new ArrayList();
        ArrayList<Integer> Pc10MinAccum = new ArrayList();
        ArrayList<Short> WndSpdAvg = new ArrayList();
        ArrayList<WndDirection> WndDirAvg = new ArrayList();//fix me
        
        public Atmospheric(int _id) {
                super("AtmosphericHist",_id);
        }

        /**
         * Load Data
         * @param p 
         */
        @Override
        public void loadData(Period p) {
                try {
                        clear();
                        ArrayList<HashMap> datas = loadData(p, true);
                        for(HashMap<String, Object> map : datas){
                                periodDate.add((Date)map.get("period"));
                                Comparedate.add((Date)map.get(AtmosphericType.Date.getColumnName()));
                                temperature.add((Short)map.get(AtmosphericType.Temperature.getColumnName()));
                                maxTemp.add((Short)map.get(AtmosphericType.maxTemp.getColumnName()));
                                minTemp.add((Short)map.get(AtmosphericType.minTemp.getColumnName()));
                                Rh.add((Short)map.get(AtmosphericType.Rh.getColumnName()));
                                pcAccum.add((Integer)map.get(AtmosphericType.pcAccum.getColumnName()));
                                pcAccum1Hour.add((Integer)map.get(AtmosphericType.pcAccum1Hour.getColumnName()));
                                pcAccum3Hour.add((Integer)map.get(AtmosphericType.pcAccum3Hour.getColumnName()));
                                pcAccum6Hour.add((Integer)map.get(AtmosphericType.pcAccum6Hour.getColumnName()));
                                pcAccum12Hour.add((Integer)map.get(AtmosphericType.pcAccum12Hour.getColumnName()));
                                Pc10MinAccum.add((Integer)map.get(AtmosphericType.pc10MinAccum.getColumnName()));
                                WndSpdAvg.add((Short)map.get(AtmosphericType.WndSpdAvg.getColumnName()));
                                PcType.add(PrecipType.getType((Short)map.get(AtmosphericType.PcType.getColumnName())));
                                pcIntens.add(PrecipIntens.getType((Short)map.get(AtmosphericType.PcIntens.getColumnName())));
                                
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(Atmospheric.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        /**
         * get Temperature
         * @return 
         */
        public Double[] getTemperature(){
                return AdjustData(ShorttoDoubleArray(temperature),AtmosphericType.Temperature);
        }
        
        public Double[] getMaxTemp(){
                return AdjustData(ShorttoDoubleArray(maxTemp),AtmosphericType.maxTemp);
        }
        
        public Double[] getMinTemp(){
                return AdjustData(ShorttoDoubleArray(minTemp),AtmosphericType.minTemp);
        }
        
        public Double[] getRh(){
                return AdjustData(ShorttoDoubleArray(Rh),AtmosphericType.Rh);
        }
        
        public Double[] getPcAccum(){
                return AdjustData(InttoDoubleArray(pcAccum),AtmosphericType.pcAccum);
        }
        
        public Double[] getPcAccum1Hour(){
                return AdjustData(InttoDoubleArray(pcAccum1Hour),AtmosphericType.pcAccum1Hour);
        }
        
        public Double[] getPcAccum3Hour(){
                return AdjustData(InttoDoubleArray(pcAccum3Hour),AtmosphericType.pcAccum3Hour);
        }
        
        public Double[] getPcAccum6Hour(){
                return AdjustData(InttoDoubleArray(pcAccum6Hour),AtmosphericType.pcAccum6Hour);
        }
        
        public Double[] getPcAccum12Hour(){
                return AdjustData(InttoDoubleArray(pcAccum12Hour),AtmosphericType.pcAccum12Hour);
        }
        
        public Double[] getPc10MinAccum(){
                return AdjustData(InttoDoubleArray(Pc10MinAccum),AtmosphericType.pc10MinAccum);
        }
        
        public Double[] getWndSpdAvg(){
                return AdjustData(ShorttoDoubleArray(WndSpdAvg),AtmosphericType.WndSpdAvg);
        }
        
        public ArrayList<PrecipType> getPcType(){
                return PcType;
        }
        
        public String[] getPcTypetoString(){
                String[] pt = new String[PcType.size()];
                for(int i = 0 ; i < PcType.size() ; i++){
                        pt[i] = PcType.get(i).getName();
                }
                return pt;
        }
        
        public ArrayList<PrecipIntens> getPcIntens(){
                return pcIntens;
        }
        
        public ArrayList<Date> getPeriodDates(){
                return periodDate;
        }
        
        public ArrayList<Date> getCompareDates(){
                return Comparedate;
        }

        /**
         * Clear All Atmospheric Data
         */
        @Override
        public void clear() {
                periodDate.clear();
                Comparedate.clear();
                temperature.clear();
                maxTemp.clear();
                minTemp.clear();
                Rh.clear();
                PcType.clear();
                pcIntens.clear();
                pcAccum.clear();
                Pc10MinAccum.clear();
                WndSpdAvg.clear();
                WndDirAvg.clear();
                clearData();
        }

        private Double[] AdjustData(Double[] data, AtmosphericType atype) {
                Double[] tdata = new Double[data.length];
                if(atype.isTemperature() || atype.ismaxTemp() || atype.isminTemp()){
                        for(int i=0;i<data.length;i++){
                                tdata[i] = data[i] / 100;
                        }
                        return tdata;
                }else if(atype.ispc10MinAccum() || atype.ispcAccum() || atype.ispcAccum12Hour()
                        || atype.ispcAccum1Hour() || atype.ispcAccum3Hour() || atype.ispcAccum6Hour()){
                        for(int i=0;i<data.length;i++){
                                tdata[i] = data[i] * 0.025;
                        }
                        return tdata;
                }else{
                        return data;
                }
                
        }
}
