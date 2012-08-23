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

package edu.umn.natsrl.infra;

import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.types.TrafficType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class DetectorDataReader extends DataReader implements DataReaderImpl{
    
    private int MAX_SCANS = InfraConstants.MAX_SCANS;
    private int FEET_PER_MILE = InfraConstants.FEET_PER_MILE;
    
    private Detector detector;

    private Vector<Double> volume = new Vector<Double>();
    private Vector<Double> speed = new Vector<Double>();
    private Vector<Double> density = new Vector<Double>();
    private Vector<Double> scan = new Vector<Double>();      

    // constructor
    public DetectorDataReader(Detector detector, Period period) {
        super(period,detector.getId());
        this.detector = detector;
        dataMap.put(TrafficType.VOLUME, volume);
        dataMap.put(TrafficType.SPEED_FOR_MICROWAVE, speed);
        dataMap.put(TrafficType.DENSITY, density);
        dataMap.put(TrafficType.SCAN, scan);
    }

    // you must call to load traffic data after making instance
    @Override
    public void load() {
        
        for(Vector<Double> d : dataMap.values()) d.clear();

        Vector<Double> scan = loadLocalFiles(TrafficType.SCAN);
        addDensityData(scan);

        loadLocalFiles(TrafficType.VOLUME);
        
//        System.out.print(detector.getLabel());
        if(detector.isWavetronics())
            System.out.println(" node : " + detector.getRNode().getLabel());
        if(detector.isWavetronics()) {
            loadLocalFiles(TrafficType.SPEED_FOR_MICROWAVE);
        }    
        isLoaded(true);
    }

    
    /**
     * Returns traffic data array
     * @param trafficType
     * @return 
     */
    @Override
    public double[] read(TrafficType trafficType)
    {
        if(!isLoaded) load();
        Vector<Double> data = dataMap.get(trafficType);
        double[] pdata = new double[data.size()];
        for(int i=0;i<pdata.length; i++) pdata[i] = data.get(i);
        return pdata;
    }
    
    private void addDensityData(Vector<Double> scanData)
    {
        density.clear();
        for(Double d : scanData) {
            density.add(d / MAX_SCANS * FEET_PER_MILE / this.detector.getFieldLength());
        }        
    }        
}
