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
public enum SiteGroupType {
    COMPATIBILITY(1, "Compatibility", "This group provides backwards compatibility to the Syid table."),
    DISPLAY(2, "Display", "This group contains sites which are grouped for display on Scan Web"),
    SCANX(3, "ScanX", "This group contains sites which are Scanx weather sites");
    
    int sid;
    String name;
    String discription;
    
    SiteGroupType(int _sid, String _name, String _Disc){
            sid = _sid;
            name = _name;
            discription = _Disc;
    }
    
    public static SiteGroupType getType(String val){
            for(SiteGroupType ty : SiteGroupType.values()){
                    if(ty.name.equals(val))
                            return ty;
            }
            
            return null;
    }
}
