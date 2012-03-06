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

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.w3c.dom.Document;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class StationDataReader {
    
    private int DATA_READ_INTERVAL = 30;  // second
    private int DATA_READ_RETRY_DELAY = 2;    // second

    private DataLoader sdr;
    private Vector<StationDataSet> dataList;
    private ArrayList<String> stationList;
    private Timer timer;
    private int dataCount = 0;
    private boolean isReady;
    private Date prevDate;
    
    public StationDataReader() {
        this.sdr = new DataLoader();
        this.dataList = new Vector<StationDataSet>();
        this.stationList = new ArrayList<String>();
        this.isReady = false;
        this.prevDate = null;
    }

    /**
     * <pre>
     * Gets data using DataLoader 
     * Don't call this method directly.
     * It should be called by internal timer task.
     * </pre>
     */
    public boolean collectData()
    {
        if(!isReady) ready();
        
        boolean isAdded = false;
        try {
            Document xmlDoc = this.sdr.xmlLoad();
            Date currentTimestamp = this.sdr.getLastDate();
            if(currentTimestamp == null) {
                System.out.println("No Timestamp");
                return false;
            }
            if(currentTimestamp.equals(prevDate)) {
                System.out.printf("Same Date Data\n");
                return false;
            }

            prevDate = currentTimestamp;


            int stationCount = stationList.size();
            for (int i=0; i<stationCount; i++) {
                String sid = stationList.get(i);
                StationNode sn = sdr.getStationData(xmlDoc, sid);
                if(sn == null) {
                    System.out.println("StationNode is null : " + sid);
                    continue;
                }
                addData(sid, sn);
                isAdded = true;
            }

            if(isAdded) {
                dataCount++;
                return true;
            } else {
                System.out.println("Not added");
                return false;
            }
            
        } catch (Exception ex) {
            System.out.println("StationDataReader > Parsing Exception");
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Adds data to station data set list
     * @param stationId station id
     * @param sn station node data including speed, occupancy, flow and volume
     */
    private void addData(String stationId, StationNode sn)
    {
        for(StationDataSet sds : dataList)
        {
            if(sds.getStationId().equals(stationId)) {
                sds.addData(sn);
                return;
            }
        }
    }

    /**
     * Pre-process to collect data
     * All data is to initialize.
     */
    private void ready()
    {
        dataList.removeAllElements();
        for(String sid : this.stationList)
        {
            dataList.add(new StationDataSet(sid));
        }
        isReady = true;
        dataCount = 0;
    }

    /**
     * Adds station id for collecting data
     * @param stationId
     */
    public void addStationId(String stationId)
    {
        stationList.add(stationId);
    }

    /**
     * Adds station id for collecting data
     * @param stationId
     */
    public void addStationId(int stationId)
    {
        stationList.add(Integer.toString(stationId));
    }

    /**
     * Sets data load interval (default = 30s)
     * @param DataLoadInterval data load interval (second)
     */
    public void setDataLoadInterval(int DataLoadInterval) {
        this.DATA_READ_INTERVAL = DataLoadInterval;
    }

    /**
     * Sets retry delay when failing to load data (defalut = 2s)
     * @param RetryDelay retry delay (second)
     */
    public void setRetryDelay(int RetryDelay) {
        this.DATA_READ_RETRY_DELAY = RetryDelay;
    }

    public int getStationCount()
    {
        return this.stationList.size();
    }

    public ArrayList<String> getStationList() {
        return stationList;
    }


    public float getVolume(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return 0;
        return dataset.getVolume();
    }


    /**
     *
     * @param stationId
     * @return volume data array
     */
    public float[] getAllVolume(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return null;
        return dataset.getAllVolume();
    }


    public float getOccupancy(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return 0;
        return dataset.getOccupancy();
    }

    /**
     *
     * @param stationId
     * @return occupancy data array
     */
    public float[] getAllOccupancy(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return null;
        return dataset.getAllOccupancy();
    }

    public int getFlow(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return 0;
        return dataset.getFlow();
    }

    /**
     *
     * @param stationId
     * @return flow data array
     */
    public int[] getAllFlow(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return null;
        return dataset.getAllFlow();
    }

    public int getSpeed(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return 0;
        return dataset.getSpeed();
    }

    /**
     *
     * @param stationId
     * @return speed data array
     */
    public int[] getAllSpeed(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return null;
        return dataset.getAllSpeed();
    }

    public Date getDate(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) return null;
        return dataset.getDate();
    }

    /**
     *
     * @param stationId
     * @return speed data array
     */
    public Date[] getAllDate(String stationId)
    {
        StationDataSet dataset = getStationDataSet(stationId);
        if(dataset == null) {
            System.out.println("DataSet is null");
            return null;
        }
        return dataset.getAllDate();
    }

    /**
     *
     * @param stationId
     * @return station data set corresponding station
     */
    private StationDataSet getStationDataSet(String stationId)
    {
        for(StationDataSet sds : dataList)
        {
            if(sds.getStationId().equals(stationId)) {
                return sds;
            }
        }
        return null;
    }

    public StationNode getLatestStationNode(String stationId)
    {
        for(StationDataSet sds : dataList)
        {
            if(sds.getStationId().equals(stationId)) {
                return sds.getLatestStationNode();
            }
        }
        return null;
    }

    public int getDataCount()
    {
        return this.dataCount;
    }

    private class DataGetter extends TimerTask
    {
        StationDataReader sr;

        public DataGetter(StationDataReader sr) {
            this.sr = sr;
        }

        @Override
        public void run() {
            sr.collectData();
        }
    }
}
