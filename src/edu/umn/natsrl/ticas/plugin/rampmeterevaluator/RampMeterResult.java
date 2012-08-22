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
import edu.umn.natsrl.infra.Section;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterResult {
    private Section section;
    private Period period;
    private RampMeterEvaluatorMode mode;
    private ArrayList<RampMeterNode> Ramps;
    
    RampMeterResult(Section section, Period p, RampMeterEvaluatorMode mode) {
        this.section = section;
        period = p;
        this.mode = mode;
    }
    
    public void setResult(ArrayList<RampMeterNode> ramp){
        Ramps = ramp;
    }
    
    public Period getPeriod(){
        return period;
    }
    public String getSectionName(){
        return section.getName();
    }
    public String getPeriodtoString(){
        return period.getPeriodStringWithoutTime();
    }
    public String getPeriodtoHString(){
        return period.getPeriodStringHWithoutTime();
    }
    public ArrayList<RampMeterNode> getRamps(){
        return Ramps;
    }
    
    
}