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

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum RampMeterEvaluatorMode {
    FlowData("Analysis_Output Files(Green,Storage,WaitTime)_DataType(Flow)",1,1),
    VolumeData("Analysis_Output Files(Green,Storage,WaitTime)_DataType(Volume)",2,1),
    RampBasedFlow("RampBased Data Extraction_Output File(Single File)_DataType(Flow)",1,3),
    RampBasedVolume("RampBased Data Extraction_Output File(Single File)_DataType(Volume)",2,3),
//    RampBasedAFlow("Flow(RampBased_Analysis)",1,4),
//    RampBasedAVolume("Volume(RampBased_Analysis)",2,4),
    EachFlowData("Each Date Data Extraction_Output Files(Each Date)_DataType(Flow)",1,2),
    EachVolumeData("Each Date Data Extraction_Output Files(Each Date)_DataType(Volume)",2,2);
    
    
    private String name = null;
    private int outputmode;
    private int daymode;
    RampMeterEvaluatorMode(String name, int output, int day){
        this.name = name;
        outputmode = output;
        daymode = day;
    }
    
    public String toString(){
        return name;
    }
    
    public boolean isFlow(){
        return outputmode == 1;
    }
    public boolean isVolume(){
        return outputmode == 2;
    }
    public boolean isDayModeEachDays(){
        return daymode == 2;
    }
    public boolean isDayModeAllDays(){
        return daymode == 1;
    }
    public boolean isDayModeRampBased(){
        return daymode == 3;
    }
    public boolean isDayModeRampBasedAnalyisis(){
        return daymode == 4;
    }
}
