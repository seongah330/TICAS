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
public enum SurfaceType {
        Date("DtTm"),
        Temperature("Temperature"),
        Condition("Cond"),
        FrzTemp("FrzTemp"),
        Chemical("Chem"),
        ChemicalPct("ChemPct"),
        Depth("Depth"),
        IcePct("IcePct");
        
        String cname;
        SurfaceType(String _cname){
                cname = _cname;
        }
        
        public String getColumnName(){
                return cname;
        }
        
        public boolean isTemperature(){return this == Temperature;}
        public boolean isCondition(){return this == Condition;}
        public boolean isFrzTemp(){return this == FrzTemp;}
        public boolean isChemical(){return this == Chemical;}
        public boolean isChemicalPct(){return this == ChemicalPct;}
        public boolean isDepth(){return this == Depth;}
        public boolean isIcePct(){return this == IcePct;}
}
