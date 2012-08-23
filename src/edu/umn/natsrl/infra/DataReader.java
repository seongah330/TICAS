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
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class DataReader {
    
    private String TRAFFIC_DATA_URL = InfraConstants.TRAFFIC_DATA_URL;
    private String CACHE_PATH = InfraConstants.CACHE_TRAFDATA_DIR;
    private String FAIL_PATH = InfraConstants.CACHE_DETFAIL_DIR;
    
    public File roadFile;

    protected Period period;
    protected String filename;

    protected boolean isLoaded = false;
    
    protected HashMap<TrafficType, Vector<Double>> dataMap = new HashMap<TrafficType, Vector<Double>>();

    // constructor
    public DataReader(Period period, String _filename) {
        this.filename = _filename;
        this.period = period;
        (new File(CACHE_PATH)).mkdir();
        (new File(FAIL_PATH)).mkdir();
    }

    /**
     * read detector data from local cache
     * @param detector_id target detector id
     * @param trafficType traffic type
     * @return traffic data vector
     */
    public Vector<Double> loadLocalFiles(TrafficType trafficType) {
        
        Calendar c = Calendar.getInstance();
        c.setTime(period.startDate);
        int days = period.getHowManyDays();
        Vector<Double> data = new Vector<Double>();
                
        for (int i = 0; i < days; i++) {
            Vector<Double> dataFor1Day = readFile((Calendar)c.clone(), trafficType);
            if(dataFor1Day == null || dataFor1Day.size() != trafficType.getSamplePerDay()) {
                Double[] missing = new Double[trafficType.getSamplePerDay()];
                Arrays.fill(missing, -1D);
                dataFor1Day = new Vector<Double>(Arrays.asList(missing));
            }
            data.addAll(dataFor1Day);            
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        data = trimByPeriod(trafficType,data);
        dataMap.get(trafficType).addAll(data);
        return data;
    }
    
    /**
     * @see http://data.dot.state.mn.us/dds/Traffic.html
     * @param filename local cache file path
     * @param trafficType traffic type (volume, occupancy, scan, speed for microwave sensor)
     * @return original traffic data
     */
    private Vector<Double> readFile(Calendar c, TrafficType trafficType) {
        try {
            DataInputStream dis = null;
            File localFile = new File(this.getCachFilePath(c, trafficType));
            File failFile = new File(this.getFailFilePath(c, trafficType));
            
            // if local file doesn't exists or local file's length is not full size
            // and if it's not failed detector
            // read remote file
            if( (!localFile.exists() || localFile.length() < trafficType.getSampleSize() * trafficType.getSamplePerDay()) && !failFile.exists()) {
                return readRemoteFile(c, trafficType);
            }
            
            dis = new DataInputStream(new FileInputStream(localFile));
            
            // read data from local file
            return readData(dis, trafficType, c, false);
            
        } catch (FileNotFoundException ex) {
            return null;
        }
    }    
        
    /**
     * read traffic data from IRIS server and
     * write to caching folder
     * @param detector_id target detector id
     * @param c Calendar object to corresponding the date
     * @param trafficType traffic type
     */
    private Vector<Double> readRemoteFile(Calendar c, TrafficType trafficType) {
        try {
            String remoteFile = this.getRemoteDataFilePath(c, trafficType);
            DataInputStream dis = null;       

            // delete fail log
            new File(this.getFailFilePath(c, trafficType)).delete();

            String localFile = this.getCachFilePath(c, trafficType);
            
            File cache = new File(localFile);            
            if ( (cache.exists() && cache.length()>0) ) {
                return null;
            }            
            
            // create cache folder
            this.getCacheDirPath(c);
            // read from IRIS server
            URL rFile = new URL(remoteFile);
            dis = new DataInputStream(new BufferedInputStream(rFile.openStream()));
            // read data from remote file
            return readData(dis, trafficType, c, true);            
            
        } catch (Exception ex) {
            try {
                // create fail log
                new File(this.getFailFilePath(c, trafficType)).createNewFile();
            } catch (IOException ex1) {
                return null;
            }
        }
        return null;        
    }


    /**
     * Reads data from URL or local file
     * @param is
     * @param trafficType
     * @param c
     * @param writeCache
     * @return 
     */
    private Vector<Double> readData(InputStream is, TrafficType trafficType, Calendar c, boolean writeCache)
    {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(is);
            int sampleSize = trafficType.getSampleSize();
            int filesize = sampleSize * trafficType.getSamplePerDay();
            byte[] readData = new byte[filesize];
            int offset = 0;
            int numRead = 0;

            // read data into temp data array
            while (offset < readData.length && (numRead = dis.read(readData, offset, readData.length - offset)) >= 0) {
                offset += numRead;
            }
            
            readData = Arrays.copyOfRange(readData, 0, offset);

            // store data according to read unit (1byte or 2byte)
            Vector<Double> data = new Vector<Double>();
            for (int i = 0; i < readData.length; i++) {

                // sample size of .c30 and .o30 is 2byte
                if (sampleSize == 2) {
                    double value = ((readData[i] << 8)&0x0000ff00) + (readData[++i]&0x000000ff);
                    data.add(value);

                // sample size of .v30 and .s30 is 1byte
                } else {
                    double value = (double)readData[i];
                    data.add(value);
                }
            }
            
            if(writeCache) new DataReader.CachingThread(c, readData, trafficType).start();
            
            dis.close();
            return data;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    class CachingThread extends Thread {
        Calendar c;
        byte[] data;
        TrafficType trafficType;

        public CachingThread(Calendar c, byte[] data, TrafficType trafficType) {
            this.c = c;
            this.data = data;
            this.trafficType = trafficType;
        }
        
        @Override
        public void run()
        {
            // don't cache if today, because today's data is not full data
            if(checkToday()) return;
            
            OutputStream outStream = null;
            try {
                String localFile = getCachFilePath(c, trafficType);
                outStream = new BufferedOutputStream(new FileOutputStream(localFile));
                outStream.write(data, 0, data.length);
                outStream.flush();
                outStream.close();
            } catch (Exception ex) {                
                ex.printStackTrace();
                try {
                    if(outStream != null) {
                        outStream.flush();
                        outStream.close();
                    }
                } catch (IOException ex1) { ex1.printStackTrace();}
            }
        }
        
        private boolean checkToday() {
            Calendar today = Calendar.getInstance(); // today
            if (today.get(Calendar.YEAR) == c.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR)) {            
                return true;
            } else return false;
        }
    }
    
    /**
     * get subset of data for period
     * @param data traffic data array
     * @return subset of data
     */
    private Vector<Double> trimByPeriod(TrafficType trafficType, Vector<Double> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        int interval = (InfraConstants.SAMPLES_PER_DAY/trafficType.getSamplePerDay())*30;
        int start_index = (period.start_hour * 3600 / interval) + (period.start_min * 60 / interval);
        int end_index = (period.end_hour * 3600 / interval) + (period.end_min * 60 / interval);

        for(int i=0; i<this.period.getHowManyDays()-1; i++)
        {
            end_index += trafficType.getSamplePerDay();
        }
        
        Vector<Double> trimData = new Vector<Double>();
        for(int i=start_index, k=0; i<end_index; i++, k++)
        {
            trimData.add(data.get(i));
        }

        return trimData;
    }

    private String getDateDir(Calendar c)
    {
        DecimalFormat df = new DecimalFormat("00");
        return c.get(Calendar.YEAR) + File.separator + c.get(Calendar.YEAR) + df.format(c.get(Calendar.MONTH) + 1) + df.format(c.get(Calendar.DATE));
    }

    private String getCacheDirPath(Calendar c)
    {
        String cachePath = this.CACHE_PATH + File.separator + getDateDir(c);               
        new File(this.CACHE_PATH + File.separator + c.get(Calendar.YEAR)).mkdir();
        new File(cachePath).mkdir();        
        return cachePath;
    }

    private String getFailDirPath(Calendar c)
    {
        String cacheFailPath = this.FAIL_PATH + File.separator + getDateDir(c);
        new File(this.FAIL_PATH + File.separator + c.get(Calendar.YEAR)).mkdir();
        new File(cacheFailPath).mkdir();
        
        return cacheFailPath;
    }    
    
    private String getCachFilePath(Calendar c, TrafficType type)
    {
        return this.getCacheDirPath(c) + File.separator + this.filename + type.getExtension();
    }
    
    private String getFailFilePath(Calendar c, TrafficType type)
    {
        return this.getFailDirPath(c) + File.separator + this.filename + type.getExtension();
    }    

    private String getRemoteDataFilePath(Calendar c, TrafficType type)
    {
        DecimalFormat df = new DecimalFormat("00");
        String dir = c.get(Calendar.YEAR) + df.format(c.get(Calendar.MONTH) + 1) + df.format(c.get(Calendar.DATE));
        return this.TRAFFIC_DATA_URL + "/" + c.get(Calendar.YEAR) + "/"+ dir + "/"+ this.filename + type.getExtension();
    }
    protected void isLoaded(boolean isloaded){
        isLoaded = isloaded;
    }
}
