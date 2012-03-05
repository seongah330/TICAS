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

/**
 *
 * @author Chongmyung Park
 */
public enum TrafficType {
        VOLUME(0, ".v30", 1),
        OCCUPANCY(1, ".o30", 2),
        SCAN(2, ".c30", 2),
        SPEED_FOR_MICROWAVE(3, ".s30", 1),
        DENSITY(4, ".c30", 2),
        SPEED(5, null, 0),
        SPEEDFORSTATION(7, null, 0),
        FLOW(6, null, 0),
        FLOWFORAVERAGE(8,null,0),
        UNKNOWN(99, null, 0);
        
        int id;
        String ext;
        int sampleSize;

        TrafficType(int id, String ext, int sampleSize) {
            this.id = id;
            this.ext = ext;
            this.sampleSize = sampleSize;
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
        
        public boolean isSpeed() { return this == SPEED; }
        public boolean isSppedForStation(){return this == SPEEDFORSTATION;}
        public boolean isDensity() { return this == DENSITY; }
        public boolean isFlow() { return this == FLOW; }
        public boolean isFlowForAverage() { return this == FLOWFORAVERAGE; }
        public boolean isScan() { return this == SCAN; }
        public boolean isVolume() { return this == VOLUME; }
        public boolean isOccupancy() { return this == OCCUPANCY; }
}
