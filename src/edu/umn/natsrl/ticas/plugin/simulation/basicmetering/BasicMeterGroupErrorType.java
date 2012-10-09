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

import edu.umn.natsrl.ticas.plugin.fixedmetering.*;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum BasicMeterGroupErrorType {
    NOT_LOADED(1,"Data is not loaded."),
    ERROR_STRUCTURE(2,"Case file Structure is different from current Save File.\nDelete Data File"),
    NOSTATION(3,"There is No Station"),
    SUCCESS(99,"Success");
    
    int error_type = 99;
    String error_string;
    
    BasicMeterGroupErrorType(int i, String data){
        error_type = i;
        error_string = data;
    }
    
    public void setErrorMsg(String data){
        error_string = data;
    }
    
    @Override
    public String toString(){
        return error_string;
    }
    
    public boolean isNOT_LOADED(){return this == NOT_LOADED;}
    public boolean isERROR_STRUCTURE(){return this == ERROR_STRUCTURE;}
    public boolean isNOSTATION(){return this == NOSTATION;}
    public boolean isSUCCESS(){return this == SUCCESS;}
}
