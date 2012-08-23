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

import edu.umn.natsrl.infra.DataReader;
import edu.umn.natsrl.infra.DataReaderImpl;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.Vector;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class WeatherDataReader extends DataReader implements DataReaderImpl {
    
    private Vector<Double> type = new Vector<Double>();
    private Vector<Double> rainfall = new Vector<Double>();
    
    public WeatherDataReader(Period p, String name){
        super(p,name);
        dataMap.put(TrafficType.TMCWeatherType,type);
        dataMap.put(TrafficType.TMCRainFall,rainfall);
    }

    @Override
    public void load() {
        for(Vector<Double> d : dataMap.values()) d.clear();
        loadLocalFiles(TrafficType.TMCWeatherType);
        loadLocalFiles(TrafficType.TMCRainFall);
        
        isLoaded(true);
    }

    @Override
    public double[] read(TrafficType trafficType) {
        if(!isLoaded) load();
        
        Vector<Double> data = dataMap.get(trafficType);
        double[] pdata = new double[data.size()];
        for(int i=0;i<pdata.length;i++) pdata[i] = data.get(i);
        return pdata;
    }
    
}
