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

package edu.umn.natsrl.infra.types;

import edu.umn.natsrl.infra.InfraConstants;

/**
 *
 * @author Chongmyung Park
 */
public enum TrafficType {
        VOLUME(0, ".v30", 1,-1),
        OCCUPANCY(1, ".o30", 2,-1),
        SCAN(2, ".c30", 2,-1),
        SPEED_FOR_MICROWAVE(3, ".s30", 1,-1),
        DENSITY(4, ".c30", 2,-1),
        SPEED(5, null, 0,-1),
        SPEEDFORSTATION(7, null, 0,-1),
        FLOW(6, null, 0,-1),
        FLOWFORAVERAGE(8,null,0,-1),
        AVERAGEFLOW(9,null,0,-1),
        TMCWeatherType(10,".pt60",1,(InfraConstants.SAMPLES_PER_DAY/2)),
        TMCRainFall(11,".pr60",2,(InfraConstants.SAMPLES_PER_DAY/2)),
        UNKNOWN(99, null, 0,-1);
        
        int id;
        String ext;
        int sampleSize;
        int samples_per_day = 0;
        TrafficType(int id, String ext, int sampleSize, int _samples_per_day) {
            this.id = id;
            this.ext = ext;
            this.sampleSize = sampleSize;
            this.samples_per_day = _samples_per_day;
        }

        public int getTrafficTypeId() {
            return this.id;
        }

        public static TrafficType getTrafficType(int id) {
            for(TrafficType tt : TrafficType.values()) {
                if(tt.id == id) return tt;
            }
            return UNKNOWN;
        }

        public String getExtension() {
            return this.ext;
        }

        public int getSampleSize() {
            return this.sampleSize;
        }
        
        public int getSamplePerDay(){
            if(samples_per_day == -1)
                return InfraConstants.SAMPLES_PER_DAY;
            else
                return samples_per_day;
        }
        
        
        public boolean isSpeed() { return this == SPEED; }
        public boolean isSppedForStation(){return this == SPEEDFORSTATION;}
        public boolean isDensity() { return this == DENSITY; }
        public boolean isFlow() { return this == FLOW; }
        public boolean isFlowForAverage() { return this == FLOWFORAVERAGE; }
        public boolean isAverageFlow() { return this == AVERAGEFLOW; }
        public boolean isScan() { return this == SCAN; }
        public boolean isVolume() { return this == VOLUME; }
        public boolean isOccupancy() { return this == OCCUPANCY; }
        public boolean isTMCWeatherType() { return this == TMCWeatherType; }
        public boolean isTMCRainFall() { return this == TMCRainFall; }
}
