/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
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

package edu.umn.natsrl.util;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class NumUtil {
    
    /**
     * Rounds up number with precise
     * @param n number
     * @param precision dropped as a shortcut to rounding the value
     * @return number, round-up the given number
     */
    public static double roundUp(double n, int precision) {
        return (double) (Math.round(n * Math.pow(10, precision)) / Math.pow(10, precision));
    }
    
    public static double SecondToMin(double asec){
        double min = 0;
        double sec = 0;
        
        min = (int)(asec / 60);
        sec = (int)(asec % 60);
        
        return min + (sec/100);
    }
    
    public static String SecondToStringMin(double asec){
        double min = 0;
        double sec = 0;
        
        min = (int)(asec / 60);
        sec = (int)(asec % 60);
        
        String time = (int)min + ":"+(int)sec;
        return time;
    }
}
