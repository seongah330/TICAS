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
package edu.umn.natsrl.ticas.plugin.rtchart.reader;

import java.util.Date;

/**
 * Set of station, timestamp and traffic data for just one xml file
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class StationNode {

    private String id;
    private float occupancy;
    private float volume;
    private int speed;
    private int flow;
    private Date timestamp;

    public StationNode(String id, Date timestamp, float volume, float occupancy, int flow, int speed) {
        this.id = id;
        this.occupancy = occupancy;
        this.volume = volume;
        this.flow = flow;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    public float getVolume() {
        return volume;
    }

    public String getId() {
        return id;
    }

    public int getSpeed() {
        return speed;
    }

    public float getOccupancy() {
        return occupancy;
    }

    public int getFlow() {
        return flow;
    }

    public Date getDate() {
        return timestamp;
    }

}
