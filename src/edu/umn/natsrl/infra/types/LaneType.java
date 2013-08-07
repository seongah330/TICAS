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

import java.util.LinkedList;

/**
 * Road lane type enumeration.   The ordinal values correspond to the records
 * in the iris.lane_type look-up table.
 *
 * @author Douglas Lau
 * @editor Chongmyung Park
 */
public enum LaneType {
        
	/** Undefined lane type (0) */
	NONE(" ", "", 0, 0),

	/** Mainline lane type (1) */
	MAINLINE("Mainline", "", 4 * 3600, 3 * 60),

	/** Auxiliary lane type (2) */
	AUXILIARY("Auxiliary", "A", 24 * 3600, 3 * 60),

	/** Collector/Distributor lane type (3) */
	CD_LANE("CD Lane", "CD", 4 * 3600, 3 * 60),

	/** Reversible lane type (4) */
	REVERSIBLE("Reversible", "R", 72 * 3600, 3 * 60),

	/** Merge lane type (5) */
	MERGE("Merge", "M", 12 * 3600, 20 * 60),

	/** Queue detector lane type (6) */
	QUEUE("Queue", "Q", 12 * 3600, 30 * 60),

	/** Exit lane type (7) */
	EXIT("Exit", "X", 8 * 3600, 20 * 60),

	/** Meter bypass (HOV) lane type (8) */
	BYPASS("Bypass", "B", 72 * 3600, 20 * 60),

	/** Passage lane type (9) */
	PASSAGE("Passage", "P", 12 * 3600, 20 * 60),

	/** Velocity (mainline) lane type (10) */
	VELOCITY("Velocity", "V", 4 * 3600, 3 * 60),

	/** Omnibus (ok, bus) lane type (11) */
	OMNIBUS("Omnibus", "O", 72 * 3600, 20 * 60),

	/** Green count lane type (12) */
	GREEN("Green", "G", 72 * 3600, 20 * 60),

	/** Wrong way (exit) lane type (13) */
	WRONG_WAY("Wrong Way", "Y", 8 * 3600, 20 * 60),

	/** High-Occupancy-Vehicle (HOV) lane type (14) */
	HOV("HOV", "H", 8 * 3600, 3 * 60),

	/** High Occupancy / Toll (HOT) lane type (15) */
	HOT("HOT", "HT", 8 * 3600, 3 * 60),

	/** Dynamic shoulder lane type (16) */
	SHOULDER("Shoulder", "D", 72 * 3600, 3 * 60);

	/** Create a new lane type */
	private LaneType(String d, String s, int nht, int lot) {
		description = d;
		suffix = s;
		no_hit_threshold = nht;
		lock_on_threshold = lot;
	}
        
	/** Description */
	public final String description;

	/** Suffic (for detector labels) */
	public final String suffix;

	/** No hit threshold (seconds) */
	public final int no_hit_threshold;

	/** Lock on threshold (seconds) */
	public final int lock_on_threshold;

	/** Get the string description of the lane type */
	public String toString() {
		return description;
	}

	/** Check if the lane type is a mainline lane */
	public boolean isMainline() {
		return this == MAINLINE ||
		       this == AUXILIARY ||
		       this == CD_LANE ||
		       this == REVERSIBLE ||
		       this == VELOCITY ||
		       this == HOV ||
		       this == HOT ||
		       this == SHOULDER;
	}

	/** Check if the lane type is a station lane */
	public boolean isStation() {
		return this == MAINLINE;
	}

	/** Check if the lane type is a CD lane */
	public boolean isCD() {
		return this == CD_LANE;
	}    
        
	/** Check if the lane type is a station or CD lane */
	public boolean isStationOrCD() {
		return isStation() || isCD();
	}

	/** Check if the lane type is a ramp lane */
	public boolean isRamp() {
		return this == MERGE ||
		       this == QUEUE ||
		       this == EXIT ||
		       this == BYPASS ||
		       this == PASSAGE ||
		       this == OMNIBUS;
	}

	/** Check if the lane type is an on-ramp */
	public boolean isOnRamp() {
		return this == MERGE ||
		       this == QUEUE ||
		       this == BYPASS ||
		       this == PASSAGE ||
		       this == OMNIBUS ||
		       this == GREEN;
	}

	/** Check if the lane type is an off-ramp */
	public boolean isOffRamp() {
		return this == EXIT;
	}

	/** Check if the lane type is velocity */
	public boolean isVelocity() {
		return this == VELOCITY;
	}

        public static LaneType getbyDesc(String dcode) {
            for(LaneType lt: LaneType.values()) {
                if(dcode.isEmpty()) return LaneType.MAINLINE;
                if(lt.description.toLowerCase().equals(dcode.toLowerCase())) return lt;
            }
            return NONE;
        }
        
        public static LaneType get(String dcode) {
            for(LaneType lt: LaneType.values()) {
                if(dcode.isEmpty()) return LaneType.MAINLINE;
                if(lt.suffix.toLowerCase().equals(dcode.toLowerCase())) return lt;
            }
            return NONE;
        }

	/** Get a lane type from an ordinal value */
	static public LaneType fromOrdinal(short o) {
		for(LaneType lt: LaneType.values()) {
			if(lt.ordinal() == o)
				return lt;
		}
		return NONE;
	}

	/** Get an array of lane type descriptions */
	static public String[] getDescriptions() {
		LinkedList<String> d = new LinkedList<String>();
		for(LaneType lt: LaneType.values())
			d.add(lt.description);
		return d.toArray(new String[0]);
	}

}
