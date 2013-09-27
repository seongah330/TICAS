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
package edu.umn.natsrl.ticas.plugin.srte2;

import java.util.Date;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEData {
        Date datetime;
        
        public double[] data_origin;   // original 5 min speed data
        public double[] data_smoothed;   // smoothed 5 min speed data
        public double[] data_quant;   // 5 min speed data after quantization
    
        public double[] u_Avg_origin;
        public double[] u_Avg_smoothed;
        public double[] u_Avg_quant;

        public double[] k_smoothed;
        public double[] k_origin;
        public double[] k_quant;

        public double[] q_origin;
        public double[] q_smoothed;
        public double[] q_quant;

        public double[] tt_origin;
        public double[] tt_smoothed;
        public double[] tt_quant;
        
        public SRTEData(SRTESection selectedStation){
                //Speed Setting
                data_origin = selectedStation.getSpeed();
                data_smoothed = selectedStation.getSmoothedSpeed();
                data_quant = selectedStation.getQuantSpeed();

                //Flow setting
                q_origin = selectedStation.getAverageLaneFlow();
                q_smoothed = selectedStation.getSmoothedAverageLaneFlow();
                q_quant = selectedStation.getQuantAverageLaneFlow();

                // density setting
                k_origin = selectedStation.getDensity();
                k_smoothed = selectedStation.getSmoothedDensity();
                k_quant = selectedStation.getQuantDensity();

                //Average u Data
                u_Avg_origin = selectedStation.getAverageSpeed();
                u_Avg_smoothed = selectedStation.getSmoothedAverageSpeed();
                u_Avg_quant = selectedStation.getQuantAverageSpeed();
                //TT
                tt_origin = selectedStation.getTravelTime();
                tt_smoothed = selectedStation.getSmoothedTravelTime();
                tt_quant = selectedStation.getQuantTravelTime();
        }
}
