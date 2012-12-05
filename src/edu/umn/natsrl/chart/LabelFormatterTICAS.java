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
package edu.umn.natsrl.chart;

import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.labelformatters.ALabelFormatter;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class LabelFormatterTICAS extends ALabelFormatter implements IAxisLabelFormatter{
    
    TreeMap<Integer,String> ldata;
    public LabelFormatterTICAS(TreeMap<Integer,String> labeldata){
        ldata = labeldata;
    }
    @Override
    public String format(double d) {
        String result = ldata.get((int)d);
        System.out.println("format : "+result);
        if(result == null){
            return "";
        }else{
            return result;
        }
    }

    @Override
    public double getMinimumValueShiftForChange() {
        return 1;
    }

    @Override
    public double getNextEvenValue(double d, boolean bln) {
        double result;
        final double divisor = 1;
        if(bln){
            result = Math.ceil(d * divisor) / divisor;
        }else{
            result = Math.floor(d * divisor) / divisor;
        }
        return result+1;
    }

    @Override
    public Number parse(String string) throws NumberFormatException {
        for(Integer s : ldata.keySet()){
            String data = ldata.get(s);
            if(data != null){
                if(data.equals(string)){
                    return s;
                }
            }
        }
        
        return 0;
    }
    
}
