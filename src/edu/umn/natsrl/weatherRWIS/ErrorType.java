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
package edu.umn.natsrl.weatherRWIS;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum ErrorType {
        DATABASE_LOAD("Database Load error"),
        DATABASE_NOT_DATE("There is no data in selected date"),
        PERIOD_REWIND("Start Date is slower than end Date"),
        NONE("NO ERROR");
        
        String msg;
        
        ErrorType(String _msg){
                msg = _msg;
        }
        
        public String toString(){
                return msg;
        }
        
}
