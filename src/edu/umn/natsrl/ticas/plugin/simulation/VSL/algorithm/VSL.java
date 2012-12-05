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
package edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm;

import java.util.Vector;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public abstract class VSL {
//    protected VissimController vc;
    protected String  description;
    protected int nStation;
    protected double[] U;
    protected double[] Uav;
    protected double[] Uag;
    protected double[] Kag;
    protected double[] Aag;
//    protected MsgDialog logViewer;
    public int VSS_HISTORY_BUF_SIZE = 10;
    
    protected double[][] calVSLHistory;   /// VSL(calVSL) buffer history for whole simulation time
    protected double[][] accelerationHistory;
    protected int[][] vssHistory;
    protected int[][] vsdHistory;
    
    //public int[][] stationVSLHistory;  /// VSL buffer history for whole simulation time
    //public double[][] stationSpeed1MinHistory; /// 1 minutes average station speed history for whole simulation time
    //protected int[][] stationFlow1MinHistory; /// 1 minutes average station flow history for whole simulation time
    //protected double[][] stationDensity1MinHistory;   /// 1 minutes average station density history for whole simulation time
    protected boolean useBackControl;
    protected boolean hasBottleneck = false;

    // Section Elements
    protected String sectionName;
//    protected Vector<Station> stationList;
//    protected Vector<DMS> dmsList;
//    protected Vector<VSLZone> vslZoneList;
//    protected SimInfo simInfo;
    public int totalCount;
    public double[][] realData;
    public double[][] realDensityData;
    public double[][] usedStationSpeed;
    public Vector<String> times = new Vector<String>(); // for emulation

    public VSL() {     }
 
    abstract public void doVSL() throws Exception;
//    abstract public Station Stations(int stationSeq);
    abstract public String getDescription();
//    abstract public DMS DMSs(int dmsSeq);
    abstract public double vslForVISSIM(int vssSeq, int vsdSeq);
    abstract public double StationSpeed(int stationSeq);
}
