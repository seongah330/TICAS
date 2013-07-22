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
package edu.umn.natsrl.ticas.plugin.srte2;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum SMOOTHING{
        NOSMOOTHING(0,"NO SMOOTHING",0),
        SECOND(1,"2 Window Filtering",2),
        DEFAULT(2,"3 Window Filtering",1),
        MORE(3,"More..",0);

        int Num = 2;
        String desc = "";
        int count = 0;

        SMOOTHING(int icn, String desc, int cnt){
            Num = icn;
            this.desc = desc;
            count = cnt;
        }

        public static SMOOTHING getSmooth(int idx){
            for(SMOOTHING i : SMOOTHING.values())
                if(i.getIndex() == idx) return i;
            return null;
        }

        public int getCount(){return count;}
        public int getIndex(){return Num;}
        public boolean isNOSMOOTHING(){return this==NOSMOOTHING;}
        public boolean isSecond(){return this==SECOND;}
        public boolean isDefault(){return this==DEFAULT;}
        public boolean isMore(){return this==MORE;}

        @Override
        public String toString(){
            return desc;
        }
}
