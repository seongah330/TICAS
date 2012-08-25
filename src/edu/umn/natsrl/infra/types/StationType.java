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
package edu.umn.natsrl.infra.types;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum StationType {
    ORIGINSTATION("S",1),
    TEMPSTATION_T("T",100000),
    TEMPSTATION_ST("ST",10000);
    
    String sType;
    int plot;
    StationType(String _stype, int _plot){
        sType = _stype;
        plot = _plot;
    }
    
    String getTypeName(){
        return sType;
    }
    
    static StationType getStationType(String _id){
        for(StationType t : StationType.values()){
            if(!t.isOriginStation() && _id.contains(t.getTypeName())){
                return t;
            }
        }
        
        return ORIGINSTATION;
    }
    
    public static int getStationIDbyType(String _id){
        StationType type = StationType.getStationType(_id);
        String[] split = _id.split(type.getTypeName());
        if(split.length > 0){
            int value = Integer.parseInt(split[split.length-1]);
            while(value < type.plot){
                value *= 10;
            }
            return value;
        }
        else return 0;
        
    }
    
    public static int getOriginStationIDbyType(String _id){
        StationType type = StationType.getStationType(_id);
        String[] split = _id.split(type.getTypeName());
        if(split.length > 0){
            return Integer.parseInt(split[split.length-1]);
        }
        else return 0;
    }
    
    boolean isOriginStation(){return this == ORIGINSTATION;}
    boolean isTempStation_ST(){return this == TEMPSTATION_ST;}
    boolean isTempStation_T(){return this == TEMPSTATION_T;}
}
