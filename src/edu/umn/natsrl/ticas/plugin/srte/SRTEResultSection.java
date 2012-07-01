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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.infra.Period;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEResultSection {
    SRTEResult[] results;
    TimeEvent eventSection;
    Calendar cs,ce,cb;
    Period period;
    public SRTEResultSection(SRTEResult[] re,TimeEvent te,Period p){
        results = re;
        period = p;
        setConfig(te);
    }

    void setConfig(TimeEvent te) {
        eventSection = te;
        cs = te.getStartTime();
        ce = te.getEndTime();
        cb = te.getBareLaneRegainTime();
    }

    public SRTEResult[] getResultData() {
        return results;
    }

    String getSectionName() {
        if(eventSection == null)
            return "";
        
        return eventSection.getSectionName();
    }

    public String getStartTimetoString(){
        if(cs == null)
            return null;
        else
            return getDatetoString(cs);
    }
    public String getEndTimetoString(){
        if(ce == null)
            return null;
        else
            return getDatetoString(ce);
    }
    public String getBareLaneTimetoString(){
        if(cb == null)
            return null;
        else
            return getDatetoString(cb);
    }
    
     private String getDatetoString(Calendar c) {
        if(c == null)
            return null;
        
        SimpleDateFormat dateformatter = new SimpleDateFormat("d MMM, yyyy 'at' HH:mm:ss");
        return dateformatter.format(c.getTime());
    }

    String getPeriodtoString() {
        if(period == null)
            return null;
        
        return period.getPeriodString();
    }
    
    @Override
    public String toString(){
        if(this.eventSection == null)
            return null;
        return eventSection.toString();
    }
    
    
}
