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
package edu.umn.natsrl.ticas.plugin.rampmeterevaluator;

import edu.umn.natsrl.infra.Period;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterUtil {
    public static String getTime(Period p, int count,int Interval){
        int tgap = 0;
        tgap = Interval;
        
        Calendar c = Calendar.getInstance();
        c.set(p.start_year, p.start_month-1, p.start_date, p.start_hour, p.start_min);
        for(int i=0; i<=count; i++) c.add(Calendar.SECOND, tgap);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hour, min);
    }
    
    public static Date getDateTime(Period p, int count,int Interval){
        int tgap = 0;
        tgap = Interval;
        
        Calendar c = Calendar.getInstance();
        c.set(p.start_year, p.start_month-1, p.start_date, p.start_hour, p.start_min);
        for(int i=0; i<=count; i++) c.add(Calendar.SECOND, tgap);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        
        return c.getTime();
    }
}
