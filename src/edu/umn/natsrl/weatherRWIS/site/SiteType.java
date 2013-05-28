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
package edu.umn.natsrl.weatherRWIS.site;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum SiteType {
        CAMPBELLSCIENTIFIC(1,"Campbell Scientific"),
        CLIMATRONICS(2, "Climatronics"),
        COASTAL(3, "Coastal"),
        KAVOURAS(4, "Kavouras"),
        SSI(5, "SSI"),
        VAISALA(6, "Vaisala");
        
        int sid;
        String name;
        
        SiteType(int _sid, String _name){
                sid = _sid;
                name = _name;
        }
        
        public static SiteType getType(String val){
            for(SiteType ty : SiteType.values()){
                    if(ty.name.equals(val))
                            return ty;
            }
            
            return null;
    }
}
