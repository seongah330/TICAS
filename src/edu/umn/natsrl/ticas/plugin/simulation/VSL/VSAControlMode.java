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
package edu.umn.natsrl.ticas.plugin.simulation.VSL;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum VSAControlMode {
    TT_NOLIMIT("Travel Time_NEW"),
    TT_MODE("Travel Time_Old"),
    STATIC_MODE("STATIC MODE");
    
    String str = null;
    
    VSAControlMode(String s){
        str = s;
    }
    
    public boolean isSTATICMODE(){return this==STATIC_MODE;}
    public boolean isTTMODE(){return this==TT_MODE;}
    public boolean isTTNOLIMIT(){return this==TT_NOLIMIT;}
    
    @Override
    public String toString(){
        return str;
    }
}
