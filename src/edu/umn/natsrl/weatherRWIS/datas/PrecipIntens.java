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
public enum PrecipIntens {
        None(0,"None","No Intensity"),
        Reserved(1,"Reserved","Reserved"),
        Light(2,"Light","Light"),
        Moderate(3,"Moderate","Moderate"),
        Heavy(4,"Heavy","Heavy"),
        Reserved_1(5,"Reserved_1","Reserved_1"),
        Reserved_2(6,"Reserved_2","Reserved_2"),
        Initialized(7,"Initialized","Initialized"),
        Slight(10,"Slight","(Ess)  Slight precipitation is occurring."),
        Other(11,"Other","(Ess) Other"),
        Unknown(12,"Unknown","(Ess) intensity is not known"),
        No_Data_251(251,"No Data (251)","No Data (251)"),
        No_Data_252(252,"No Data (252)","No Data (252)"),
        No_Data_253(253,"No Data (253)","No Data (253)"),
        No_Data_254(254,"No Data (254)","No Data (254)"),
        No_Data(255,"No Data","No Data");
        
        int src;
        String name;
        String desc;
        PrecipIntens(int _src, String _name, String _desc){
                src = _src;
                name = _name;
                desc = _desc;
        }
        
        public static PrecipIntens getType(int var){
                for(PrecipIntens p : PrecipIntens.values()){
                        if(p.getSrc() == var)
                                return p;
                }
                return PrecipIntens.Unknown;
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
