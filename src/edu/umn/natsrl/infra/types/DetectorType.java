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
public enum DetectorType {
    ORIGINDETECTOR("",1),
    TEMPDETECTOR("T",10000),
    TEMPDETECTOR_DT("DT",100000);
    
    String sType;
    int plot;
    
    DetectorType(String _stype,int _plot){
        sType = _stype;
        plot = _plot;
    }
    
    public static DetectorType getDetectorType(String _id){
        if(_id == null)
            return ORIGINDETECTOR;
        for(DetectorType t : DetectorType.values()){
            if(!t.isOriginDetector() && _id.contains(t.getTypeName())){
                return t;
            }
        }
        
        return ORIGINDETECTOR;
    }
    
    public static int getDetectorIDbyType(String _id){
        DetectorType type = DetectorType.getDetectorType(_id);
        if(type.isOriginDetector())
            return Integer.parseInt(_id);
        else{
            String[] split = _id.split(type.getTypeName());
            if(split.length > 0)
                return Integer.parseInt(split[split.length-1]) * type.plot;
            else
                return 0;
        }
    }
    
    String getTypeName(){
        return sType;
    }
    
    public boolean isOriginDetector(){return this == ORIGINDETECTOR;}
    public boolean isTempDetector(){
        if(this == TEMPDETECTOR || this == TEMPDETECTOR_DT)
            return this == TEMPDETECTOR;
        else
            return false;
    }
}
