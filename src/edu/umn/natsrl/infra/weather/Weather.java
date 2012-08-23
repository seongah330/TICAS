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
import edu.umn.natsrl.infra.weather.type.WeatherType;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class Weather {
    protected final int ACCURARY_PER = 80;
    
    protected Period period;
    
    protected boolean isLoaded = false;
    private boolean isAccurary = false;
    
    protected WeatherType weatherType = null;
    protected double Accurary = 0;


    public Weather(Period p){
        period = p;
    }
    protected void setWeatherType(WeatherType type){
        weatherType = type;
    }
    protected void isLoaded(boolean is){
        isLoaded = is;
    }
    protected void setAccurary(boolean acc){
        isAccurary = acc;
    }
    public WeatherType getWeatherType(){
        return weatherType;
    }
    public boolean isLoaded(){
        return isLoaded;
    }
    public boolean isAccurary(){
        return isAccurary;
    }
    public double getAccurary(){
        return Accurary;
    }
    public Period getPeriod(){
        return period;
    }
}
