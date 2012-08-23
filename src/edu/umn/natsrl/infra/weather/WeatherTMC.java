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
package edu.umn.natsrl.infra.weather;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.infra.weather.type.WeatherDevice;
import edu.umn.natsrl.infra.weather.type.WeatherType;
import javax.swing.JOptionPane;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class WeatherTMC extends Weather implements WeatherImpl{
    public double[] type;
    public double[] rainfall;
    
    private double avgRainFall;
    
    private WeatherDevice wdevice;
    
    public WeatherTMC(WeatherDevice device, Period p){
        super(p);
        wdevice = device;
    }

    @Override
    public void load() {
        if(period.interval % 30 != 0){
            JOptionPane.showMessageDialog(null, "Interval must be the multiples of 30");
            return;
        }
        
        WeatherDataReader wdr = new WeatherDataReader(period,wdevice.getName());
        type = wdr.read(TrafficType.TMCWeatherType);
        rainfall = wdr.read(TrafficType.TMCRainFall);
//        for(int i=0;i<type.length;i++){
//            System.out.println("type["+i+"] : "+type[i]+", rain : "+rainfall[i]);
//        }
        setOption();
        isLoaded(true);
    }
    
    @Override
    public void setOption(){
        double total = type.length;
        double AccCount = 0;
        double TotalrainFall = 0;
        double RainFallCount = 0;
        double wType = 0;
        for(int i=0;i<type.length;i++){
            if(type[i] != -1){
                AccCount++; //set Accurary
                /**
                 * set Weather Type
                **/
                if(type[i] > wType)
                    wType = type[i];
            }
            
            if(type[i] > 0){
                TotalrainFall += rainfall[i];
                RainFallCount ++;
            }
        }
        
        /**
         * Calculate Accurary
         */
        double calculate = total == 0 ? 0 : (AccCount / total) * 100;
        if(calculate > ACCURARY_PER)
            super.setAccurary(true);
        else
            super.setAccurary(false);

        super.Accurary = calculate;
        
        /**
         * set AvgRainFall
         */
        avgRainFall = RainFallCount == 0 ? 0 : (TotalrainFall / RainFallCount);
        /**
         * set Weather Type
         */
        setWeatherType(WeatherType.getWeatherType((int)wType));
    }
    
    public double getAvgRainFall(){
        return avgRainFall;
    }
    
    
    
    
}
