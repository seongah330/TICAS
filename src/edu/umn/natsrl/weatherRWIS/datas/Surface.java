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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class Surface extends WeatherData implements WeatherDataImpl{
        ArrayList<SurfaceCondition> Condition = new ArrayList();
        ArrayList<Short> Temp = new ArrayList();
        ArrayList<Short> ChemicalFactor = new ArrayList();
        ArrayList<Short> ChemicalPercent = new ArrayList();
        ArrayList<Short> Depth = new ArrayList();
        ArrayList<Short> IcePercent = new ArrayList();
        
        public Surface(int siteid){
                super("SurfaceHist",siteid);
        }

        @Override
        public void loadData(Period p) {
                try {
                        clear();
                        ArrayList<HashMap> datas = loadData(p, true);
                        for(HashMap<String, Object> map : datas){
                                Condition.add(SurfaceCondition.getType((Short)map.get(SurfaceType.Condition.getColumnName())));
                                Temp.add((Short)map.get(SurfaceType.Temperature.getColumnName()));
                                ChemicalFactor.add((Short)map.get(SurfaceType.Chemical.getColumnName()));
                                ChemicalPercent.add((Short)map.get(SurfaceType.ChemicalPct.getColumnName()));
                                Depth.add((Short)map.get(SurfaceType.Depth.getColumnName()));
                                IcePercent.add((Short)map.get(SurfaceType.IcePct.getColumnName()));
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(Atmospheric.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        public ArrayList<SurfaceCondition> getCondition(){
                return Condition;
        }
        
        public Double[] getTemperature(){
                return AdjustData(ShorttoDoubleArray(Temp),SurfaceType.Temperature);
        }
        
        public Double[] getChemicalFactors(){
                return AdjustData(ShorttoDoubleArray(ChemicalFactor),SurfaceType.Chemical);
        }
        
        public Double[] getChemicalPercents(){
                return AdjustData(ShorttoDoubleArray(ChemicalPercent),SurfaceType.ChemicalPct);
        }
        
        public Double[] getDepths(){
                return AdjustData(ShorttoDoubleArray(Depth),SurfaceType.Depth);
        }
        
        public Double[] getIcePercents(){
                return AdjustData(ShorttoDoubleArray(IcePercent),SurfaceType.IcePct);
        }
        
        private Double[] AdjustData(Double[] data, SurfaceType atype) {
                Double[] tdata = new Double[data.length];
                if(atype.isTemperature() || atype.isFrzTemp()){
                        for(int i=0;i<data.length;i++){
                                tdata[i] = data[i] / 100;
                        }
                        return tdata;
                }else{
                        return data;
                }
                
        }
        
        @Override
        public void clear() {
                clearData();
        }
}
