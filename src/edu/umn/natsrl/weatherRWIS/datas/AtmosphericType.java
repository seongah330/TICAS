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

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum AtmosphericType {
        Date("DtTm","date","date"),
        Temperature("Temperature","temperature","Celsius"),
        maxTemp("MaxTemp","max temperature","Celsius"),
        minTemp("MinTemp","min temperature","Celsius"),
        Rh("Rh","humidity","percent"),
        PcType("PcType","precipitation Type","recipitation type"),
        PcIntens("PcIntens", "precipitation Intensity","precipitation intensity"),
        pcAccum("PcPast24Hours","precipitation 24Hour Accum","millimeters"),
        pcAccum1Hour("PcPast1Hours","precipitation 1Hour Accum","millimeters"),
        pcAccum3Hour("PcPast3Hours","precipitation 3Hour Accum","millimeters"),
        pcAccum6Hour("PcPast6Hours","precipitation 6Hour Accum","millimeters"),
        pcAccum12Hour("PcPast12Hours","precipitation 12Hour Accum","millimeters"),
        pc10MinAccum("pc10MinAccum","Pc10MinAccum","millimeters"),
        WndSpdAvg("WndSpdAvg","Wind Speed","km/h"),
        WndDirAvg("WndDirAvg","Wind Direction","compass degrees");
        
        String cname;
        String desc;
        String measure;
        AtmosphericType(String _cname,String _desc, String _measure){
                cname = _cname;
                desc = _desc;
                measure = _measure;
        }
        
        public String getColumnName(){
                return cname;
        }
        
        public String getName(){
                return desc;
        }
        
        public String getMeasure(){
                return measure;
        }
        
        public boolean Date(){return this == Date;}
        public boolean isTemperature(){return this == Temperature;}
        public boolean ismaxTemp(){return this == maxTemp;}
        public boolean isminTemp(){return this == minTemp;}
        public boolean isRh(){return this == Rh;}
        public boolean isPcType(){return this == PcType;}
        public boolean isPcIntens(){return this == PcIntens;}
        public boolean ispcAccum(){return this == pcAccum;}
        public boolean ispcAccum1Hour(){return this == pcAccum1Hour;}
        public boolean ispcAccum3Hour(){return this == pcAccum3Hour;}
        public boolean ispcAccum6Hour(){return this == pcAccum6Hour;}
        public boolean ispcAccum12Hour(){return this == pcAccum12Hour;}
        public boolean ispc10MinAccum(){return this == pc10MinAccum;}
        public boolean isWndSpdAvg(){return this == WndSpdAvg;}
        public boolean isWndDirAvg(){return this == WndDirAvg;}
}
