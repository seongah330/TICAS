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
package edu.umn.natsrl.ticas.plugin.simulation.basicmetering;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum BasicMeterMethod {
    FLOWRATEBASED("Ri,t = R(1),t without Queue Density Consideration"),
    DENSITYBASED("Ri,t = R(2),t without Queue Density Consideration"),
    QUEUEDENSITYBASED1("if Kq,t <= Kq,d then Ri,t = R(1),t, else R(3),t"),
    QUEUEDENSITYBASED2("if Kq,t <= Kq,d then Ri,t = R(2),t, else R(3),t");
    
    String optiontype;
    
    BasicMeterMethod(String t){
        optiontype = t;
    }
    
    @Override
    public String toString(){
        return optiontype;
    }
    
    public boolean isFLOWRATEBASED(){return FLOWRATEBASED == this;}
    public boolean isDENSITYBASED(){return DENSITYBASED == this;}
    public boolean isQUEUEDENSITYBASED1(){return QUEUEDENSITYBASED1 == this;}
    public boolean isQUEUEDENSITYBASED2(){return QUEUEDENSITYBASED2 == this;}
}
