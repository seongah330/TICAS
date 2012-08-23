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
package edu.umn.natsrl.infra.weather.type;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum WeatherType {
    DRY(0,"Dry"),
    RAIN(1,"Rain"),
    MIXED(2,"Rain&Snow"),
    SNOW(3,"Snow"),
    HAIN(4,"Hailstone");
    
    int id;
    String name;
    WeatherType(int _id, String _name){
        id = _id;
        name = _name;
    }
    public static WeatherType getWeatherType(int id){
        for(WeatherType t : WeatherType.values()){
            if(t.getId() == id)
                return t;
        }
        return null;
        
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    
    @Override
    public String toString(){
        return name;
    }
    
    public boolean isDRY(){return this==DRY;}
    public boolean isRAIN(){return this==RAIN;}
    public boolean isMIXED(){return this==MIXED;}
    public boolean isSNOW(){return this==SNOW;}
    public boolean isHAIN(){return this==HAIN;}
}
