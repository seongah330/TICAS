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
public enum SpeedAggregation {
    LAB(0,"Option1(Lab)"),
    TMC(1,"Option2(TMC)");
    
    public int src = 0;
    public String name;
    
    SpeedAggregation(int _src, String _name){
        src = _src;
        name = _name;
    }
    
    public static SpeedAggregation getTypebyID(int _src){
        for(SpeedAggregation sa : SpeedAggregation.values()){
            if(sa.getSRC() == _src){
                return sa;
            }
        }
        return null;
    }
    
    public int getSRC(){
        return src;
    }
    @Override
    public String toString(){
        return name;
    }
    
    public boolean isForLAB(){return this==LAB;}
    public boolean isForTMC(){return this==TMC;}
}
