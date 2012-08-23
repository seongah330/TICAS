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
public enum WeatherDevice {
    WS35W25(1,"WS35W25"),
    WS94W38(2, "WS94W38"),
    WS94W40(3, "WS94W40"),
    WS94W42(4, "WS94W42");
    
    int sid;
    String name;
    WeatherDevice(int _sid, String _name){
        sid = _sid;
        name = _name;
    }
    
    public String getName(){
        return name;
    }
    
    public int getId(){
        return sid;
    }
    
    @Override
    public String toString(){
        return name;
    }
}
