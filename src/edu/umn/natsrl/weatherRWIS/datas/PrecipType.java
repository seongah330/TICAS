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
public enum PrecipType {
        None(0,"None","No precipitation"),
        Yes(1,"Yes","Precip occurring"),
        Rain(2,"Rain","Rain occurring"),
        Snow(3,"Snow","Snow occurring"),
        Mixed(4,"Mixed","Mixed precip"),
        Upper(5,"Upper","Upper"),
        Lower(6,"Lower","Lower"),
        Both(7,"Both","Both"),
        Light(8,"Light","Light Precip"),
        Light_Freezing(9,"Light Freezing","Light Freezing rain"),
        Freezing_Rain(10,"Freezing Rain","Freezing rain"),
        Sleet(11,"Sleet","ice pellets or sleet detected"),
        Hail(12,"Hail","Hail detected"),
        Lens_Dirty(28,"Lens_Dirty","Lens needs cleaning"),
        No_Com(29,"No Com","No Com"),
        Fault(30,"Fault","Sensor Fault"),
        Initialized(31,"Initialized","Initialized"),
        Other(41,"Other","(Ess) Some other type of Precip occurring"),
        Unidentified(42,"Unidentified","(Ess) Some unidentified type of Precip occurring"),
        Unknown(43,"Unknown","(Ess) It is not known if Precip is occurring"),
        Frozen(44,"Frozen","(Ess) Some type of frozen Precip occurring,sleet or freezing rain."),
        NO_DATA_0(255,"No Data","No Data"),
        NO_DATA_1(251,"No Data 1","No Data (251)"),
        NO_DATA_2(252,"No Data 2","No Data (252)"),
        NO_DATA_3(253,"No Data 3","No Data (253)"),
        NO_DATA_4(254,"No Data 4","No Data (254)");
        
        int src;
        String name;
        String desc;
        PrecipType(int _src, String _name, String _desc){
                src = _src;
                name = _name;
                desc = _desc;
        }
        
        public static PrecipType getType(int var){
                for(PrecipType p : PrecipType.values()){
                        if(p.getSrc() == var)
                                return p;
                }
                return PrecipType.Unknown;
        }
        
        public int getSrc(){
                return src;
        }
        
        public String getName(){
                return name;
        }
        public String getDescription(){
                return desc;
        }
}
