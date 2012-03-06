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
import java.util.Vector;

/**
 * Traffic data set for a station that includes StationNode
 * @see StationNode
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class StationDataSet {

    private String stationId;
    private Vector<StationNode> _data;

    public StationDataSet() {
        this._data = new Vector<StationNode>();
    }

    public StationDataSet(String sid) {
        this();
        this.stationId = sid;
    }

    public float getVolume()
    {
        StationNode sn = _data.lastElement();
        if(sn == null) return 0;
        return sn.getVolume();
    }

    public float getOccupancy()
    {
        StationNode sn = _data.lastElement();
        if(sn == null) return 0;
        return sn.getOccupancy();
    }

    public int getFlow()
    {
        StationNode sn = _data.lastElement();
        if(sn == null) return 0;
        return sn.getFlow();
    }

    public int getSpeed()
    {
        StationNode sn = _data.lastElement();
        if(sn == null) return 0;
        return sn.getSpeed();
    }

    public Date getDate()
    {
        StationNode sn = _data.lastElement();
        if(sn == null) return null;
        return sn.getDate();
    }

    public float[] getAllVolume()
    {
        int size = this._data.size();
        float[] volume = new float[size];
        for(int i=0; i<size; i++)
        {
            StationNode sn = _data.get(i);
            if(sn == null) continue;
            volume[i] = sn.getVolume();
        }
        return volume;
    }

    public float[] getAllOccupancy()
    {
        int size = this._data.size();
        float[] occupancy = new float[size];
        for(int i=0; i<size; i++)
        {
            StationNode sn = _data.get(i);
            if(sn == null) continue;
            occupancy[i] = sn.getOccupancy();
        }
        return occupancy;
    }

    public int[] getAllFlow()
    {
        int size = this._data.size();
        int[] flow = new int[size];
        for(int i=0; i<size; i++)
        {
            StationNode sn = _data.get(i);
            if(sn == null) continue;
            flow[i] = sn.getFlow();
        }
        return flow;
    }

    public int[] getAllSpeed()
    {
        int size = this._data.size();
        int[] speed = new int[size];
        for(int i=0; i<size; i++)
        {
            StationNode sn = _data.get(i);
            if(sn == null) continue;
            speed[i] = sn.getSpeed();
        }
        return speed;
    }

    public Date[] getAllDate()
    {
        int size = this._data.size();
        Date[] dates = new Date[size];
        for(int i=0; i<size; i++)
        {
            StationNode sn = _data.get(i);
            if(sn == null) continue;
            dates[i] = sn.getDate();
        }
        return dates;
    }

    public void addData(StationNode node)
    {
        this._data.add(node);
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getStationId() {
        return stationId;
    }

    public Vector<StationNode> getData() {
        return _data;
    }

    public StationNode getLatestStationNode() {
        if(_data.size() > 0) return this._data.lastElement();
        else return null;
    }

}
