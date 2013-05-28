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
public enum SurfaceCondition {
        Com_Init(0,"Com Init","Data not yet available"),
        Com_Fail(1,"Com Fail","Communications down"),
        No_Report_2(2,"No Report (2)","No report from sensor"),
        Dry_3(3,"Dry (3)","No moisture on pavement"),
        Wet_4(4,"Wet (4)","Moisture on pavement"),
        Chemical_Wet_5(5,"Chemical Wet (5)","Moisture mixed with anti-icer"),
        Snow_Ice_Warning_6(6,"Snow/Ice Warning (6)","Freezing on pavement"),
        Damp_7(7,"Damp (7)","Absorbsion"),
        Damp_8(8,"Damp (8)","Absorbsion"),
        Damp_9(9,"Damp (9)","Condensation on pavement"),
        Frost_10(10,"Frost (10)","Freezing condensation"),
        Damp_11(11,"Damp (11)","Absorb. (Dew)"),
        Frost_12(12,"Frost (12)","Freezing condensation"),
        Snow_Ice_Warn_13(13,"Snow/Ice Warn (13)","Freezing on pavement"),
        Com_Rest_14(14,"Com Rest (14)","Communication restored"),
        No_Report(32,"No Report","No report from sensor"),
        Dry(33,"Dry","No moisture on pavement"),
        Wet(34,"Wet","Moisture on pavement"),
        Snow_Ice_Watch(35,"Snow/Ice Watch","Potential freeze condition"),
        Chemical_Wet(36,"Chemical Wet","Mositure mixed with anti-icer"),
        Slush(37,"Slush","Slush"),
        Snow_Ice_Warning(38,"Snow/Ice Warning","Freeze conditions"),
        Damp(39,"Damp","Damp"),
        Frost(40,"Frost","Frost"),
        Wet_Below_Freezing(41,"Wet Below Freezing","Moisture detected below freezing"),
        Wet_Above_Freezing(42,"Wet Above Freezing","Moisture detected above freezing"),
        Black_Ice_Warning(43,"Black Ice Warning","Possible ice condition due to fog"),
        No_Report_33(52,"No Report","No report from sensor"),
        Dry_33(53,"Dry","No moisture on pavement"),
        Wet_33(54,"Wet","Moisture on pavement"),
        Snow_Ice_Watch_33(55,"Snow/Ice Watch","Potential freeze condition"),
        Chemical_Wet_33(56,"Chemical Wet","Mositure mixed with anti-icer"),
        Slush_33(57,"Slush","Slush"),
        Snow_Ice_Warning_33(58,"Snow/Ice Warning","Freeze conditions"),
        Damp_33(59,"Damp","Damp"),
        Frost_33(60,"Frost","Frost"),
        Wet_Below_Freezing_33(61,"Wet Below Freezing","Moisture detected below freezing"),
        Wet_Above_Freezing_33(62,"Wet Above Freezing","Moisture detected above freezing"),
        Black_Ice_Warning_33(63,"Black Ice Warning","Possible ice condition due to fog"),
        Other(101,"Other","ESS Surface Status code,Other (0)."),
        Error(102,"Error","ESS Surface Status code,Error (1)."),
        Dry_ESS(103,"Dry","ESS Surface Status code,Dry()."),
        Trace_Moisture(104,"Trace Moisture","ESS Surface Status code,Trace Moisture()."),
        Wet_ESS(105,"Wet","ESS Surface Status code,Wet()."),
        Chemically_Wet_ESS(106,"Chemically Wet","ESS Surface Status code,Chemically Wet()."),
        Ice_Warning_ESS(107,"Ice Warning","ESS Surface Status code,Ice Warning()."),
        Ice_Watch_ESS(108,"Ice Watch","ESS Surface Status code,Ice Watch()."),
        Snow_Warning_ESS(109,"Snow Warning","ESS Surface Status code,Snow Warning()."),
        Snow_Watch_ESS(110,"Snow Watch","ESS Surface Status code,Snow Watch()."),
        Absorption_ESS(111,"Absorption","ESS Surface Status code,Absorption()."),
        Dew_ESS(112,"Dew","ESS Surface Status code,Dew()."),
        Frost_ESS(113,"Frost","ESS Surface Status code,Frost()."),
        Absorption_at_Dewpoint_ESS(114,"Absorption at Dewpoint","ESS Surface Status code,Absorption at Dewpoin()."),
        Null1(251,"null","null"),
        Null2(252,"null","null"),
        Null3(253,"null","null"),
        Null4(254,"null","null"),
        Null5(255,"null","null");
        
        int src;
        String name;
        String desc;
        
        SurfaceCondition(int _src, String _name, String _desc){
                src = _src;
                name = _name;
                desc = _desc;
        }
        
        public static SurfaceCondition getType(int var){
                for(SurfaceCondition p : SurfaceCondition.values()){
                        if(p.getSrc() == var)
                                return p;
                }
                return SurfaceCondition.No_Report;
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
