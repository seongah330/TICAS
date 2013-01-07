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
package edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum VSLVersion {
    NEWVERSION(1,"New Version"),
    OLDVERSION(2,"Old Version");

    int vid;
    String vlabel;
    
    VSLVersion(int _vid, String _vlabel){
        vid = _vid;
        vlabel = _vlabel;
    }
    
    public static VSLVersion getVSLVersion(int id){
        for(VSLVersion vv : VSLVersion.values()){
            if(vv.getSID() == id){
                return vv;
            }
        }
        
        return null;
    }
    
    public boolean isNewVersion(){return this==NEWVERSION;}
    public boolean isOldVersion(){return this==OLDVERSION;}
    
    @Override
    public String toString(){
        return vlabel;
    }
    
    public int getSID(){
        return vid;
    }
}
