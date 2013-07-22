/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
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
package edu.umn.natsrl.ticas.plugin.srte2;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.plugin.srte2.SRTEResult.ResultRCRAccPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEProcess {

//    private int SMOOTHING_FILTERSIZE = 1;
//    private int QUANTIZATION_THRESHOLD = 2;
    private int STEADY_TIMELSP = 8;
    
    /**
    * Constant Value
    * SRTC Speed Interval Max Value
    */
    final double MaximumSpeedInterval = 10;
    
    /**
    * set MinSpeed
    * Minimum SRTF Value
    */
    final double MinSpeed = 55;
        
    private SRTEResult result = new SRTEResult();
    private TimeEvent timeevent;
    private final Section section;
    private Period period;
    private SRTESection selectedStation;
//    private SMOOTHING sfilter;
    private SRTEConfig config;
    
    //Check data for proccesing
    private boolean hasData = true;

    public SRTEProcess(Section section, Period period,SRTESection station, SRTEConfig config,TimeEvent te) {
        // config setting
//        this.SMOOTHING_FILTERSIZE = config.getInt("SMOOTHING_FILTERSIZE");
//        this.QUANTIZATION_THRESHOLD = config.getInt("QUANTIZATION_THRESHOLD");
//        sfilter = SMOOTHING.getSmooth(config.getInt(SRTEConfig.SMOOTHINGOPTION));
        this.selectedStation = station;
        this.section = section;
        this.period = period;
        timeevent = te;
        this.config = config;
        
        //result base sett
        result.station = selectedStation;
        result.sectionName = this.section.getName();

        //set SpeedLimit
        result.SpeedLimit = station.getSpeedLimit();
        
        result.setTime(te.getStartTime(), te.getEndTime(), te.getBareLaneRegainTime());
        result.setPeriod(period);
        
        //Speed Setting
        result.data_origin = selectedStation.getSpeed();
        result.data_smoothed = selectedStation.getSmoothedSpeed();
        result.data_quant = selectedStation.getQuantSpeed();
        
        //Flow setting
        result.q_origin = selectedStation.getAverageLaneFlow();
        result.q_smoothed = selectedStation.getSmoothedAverageLaneFlow();
        result.q_quant = selectedStation.getQuantAverageLaneFlow();
        
        // density setting
        result.k_origin = selectedStation.getDensity();
        result.k_smoothed = selectedStation.getSmoothedDensity();
        result.k_quant = selectedStation.getQuantDensity();
        
        //Average u Data
        result.u_Avg_origin = selectedStation.getAverageSpeed();
        result.u_Avg_smoothed = selectedStation.getSmoothedAverageSpeed();
        result.u_Avg_quant = selectedStation.getQuantAverageSpeed();
        
        //TT
        result.tt_origin = selectedStation.getTravelTime();
        result.tt_smoothed = selectedStation.getSmoothedTravelTime();
        result.tt_quant = selectedStation.getQuantTravelTime();

    }
    /**
     * Main algorithm process
     *  - smoothing => Quantization => find SRST / LST / RST / SRT
     * @return
     */
    public SRTEResult stateDecision() {
        if(!selectedStation.hasData())
            return result;
        
        
        
        return result;
    }

    
    /**
     * get TimeStemp
     * @param periodsec Time second
     * @return 
     */
    private int getTimestemp(int periodsec) {
        int timestemp = period.interval == 0 ? 0 : periodsec / period.interval;
        timestemp = timestemp == 0 ? 1 : timestemp;
        return timestemp;
    }
}
