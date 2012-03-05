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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEAlgorithm {

    private Section section;
    private Period period;
    private SRTEConfig config;

    /**
     * read data and run SRTE process
     * @return
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public SRTEResult start() throws OutOfMemoryError, Exception
    {
        SRTEResult result;       
        Station[] stations = this.section.getStations();                
        //read data
        System.out.print("Loading Data.............");
        long st = new Date().getTime();
        section.loadData(this.period);
        long et = new Date().getTime();
        System.out.println(" (OK : " + (et-st) + "ms)");

        //saveData();

        //make route-wide average data
        double[] avgSpeedData = getAverageSpeed(stations);
        double[] avgDensityData = getAverageDensity(stations);

        // create algorithm process instance
        SRTEProcess process = new SRTEProcess(section, period, avgSpeedData, avgDensityData, config);

        // run
        result = process.stateDecision();

        // print out result
        presentResult(result);

        System.out.println("SRTE Algorithm for Station has been done!!");        
        
        return result;
    }

    public void extractData() throws Exception
    {
        section.loadData(this.period);
        saveData();
    }

    /**
     * adjust period
     *  - Starting period is 2 hours earlier than the snow starting time
     *  - Ending period is 6 hours later than the snow ending time  
     * @param section
     * @param period
     */
    public void setSection(Section section, Period period) {
        this.section = section;
        this.period = period;
        this.period.addStartHour(-1* SRTEConfig.DATA_READ_EXTENSION_BEFORE_SNOW);
        this.period.addEndHour(SRTEConfig.DATA_READ_EXTENSION_AFTER_SNOW);
    }    

    private void presentResult(SRTEResult result) {
        try {
            String workbookFile = getFileName("SRTEResult ("+this.period.getPeriodString()+")", "xls");
            WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));        
            int sheet_count = 0;                   
            int idx = 0;
            int colIdx = 1;
            
            // summary sheet //////////////////////////////////////////////////////
            WritableSheet sheet = workbook.createSheet("Summary", sheet_count++);

            sheet.addCell(new Label(colIdx++, 0, "SRST"));
            sheet.addCell(new Label(colIdx++, 0, "U(SRST)"));                
            sheet.addCell(new Label(colIdx++, 0, "LST"));
            sheet.addCell(new Label(colIdx++, 0, "U(LST)"));                
            sheet.addCell(new Label(colIdx++, 0, "RST"));
            sheet.addCell(new Label(colIdx++, 0, "U(RST)"));                
            sheet.addCell(new Label(colIdx++, 0, "SRT"));                                         
            sheet.addCell(new Label(colIdx++, 0, "U(SRT)"));                                           

            colIdx = 0;
            sheet.addCell(new Label(colIdx++, idx+1, result.name));
            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.srst)));
            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.srst]));
            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.lst)));
            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.lst]));
            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.rst)));
            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.rst]));
            if(result.srt.size() > 0) {
                for(int i=0; i<result.srt.size(); i++) {
                    sheet.addCell(new Label(colIdx++, idx+1+i, getTime(result.srt.get(i))));
                    sheet.addCell(new Number(colIdx--, idx+1+i, result.data_smoothed[result.srt.get(i)]));
                }
                colIdx++;
            } else {
                sheet.addCell(new Label(colIdx++, idx+1, ""));
                sheet.addCell(new Label(colIdx++, idx+1, ""));                    
            }

            colIdx += 2;
            int newCol = colIdx;

            sheet.addCell(new Label(colIdx++, 0, "SRST"));
            sheet.addCell(new Label(colIdx++, 0, "LST"));
            sheet.addCell(new Label(colIdx++, 0, "RST"));
            sheet.addCell(new Label(colIdx++, 0, "SRT"));   

            colIdx = newCol;
            sheet.addCell(new Number(colIdx++, idx+1, result.srst));
            sheet.addCell(new Number(colIdx++, idx+1, result.lst));
            sheet.addCell(new Number(colIdx++, idx+1, result.rst));
            if(result.srt.size() > 0) {
                for(int i=0; i<result.srt.size(); i++) {
                    sheet.addCell(new Number(colIdx, idx+1+i, result.srt.get(i)));
                }
            }

            
            // data sheet //////////////////////////////////////////////////////
            colIdx = 0;
            sheet = workbook.createSheet("Data", sheet_count++);
            addData(sheet, colIdx++, "Times", this.period.getTimelineJustTime());
            addData(sheet, colIdx++, "U", result.data_origin);
            addData(sheet, colIdx++, "SU", result.data_smoothed);
            addData(sheet, colIdx++, "QU", result.data_quant);
            addData(sheet, colIdx++, "Phases", result.phases);
            colIdx++;
            addData(sheet, colIdx++, "K", result.k_origin);
            addData(sheet, colIdx++, "SK", result.k_smoothed);
            addData(sheet, colIdx++, "QK", result.k_quant);
            
            colIdx++;colIdx++;
            
            Station[] stations = this.section.getStations();
            addData(sheet, colIdx++, "Avg", result.data_origin);
            for(int i=0;i<stations.length; i++)
            {
                addData(sheet, colIdx++, "U-"+stations[i].toString(), stations[i].getSpeed());
                addData(sheet, colIdx++, "K-"+stations[i].toString(), stations[i].getDensity());
                addData(sheet, colIdx++, "Q-"+stations[i].toString(), stations[i].getFlow());
                addData(sheet, colIdx++, "O-"+stations[i].toString(), stations[i].getOccupancy());
            }
            

            colIdx = 0;
            sheet = workbook.createSheet("Log", sheet_count++);
            addData(sheet, colIdx++, "Messages", result.msgs);            
                        
            workbook.write();
            workbook.close();

            // open result excel file
            //Desktop.getDesktop().open(new File(workbookFile));



        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void addData(WritableSheet sheet, int column, String label, double[] data)
    {
        try {
            sheet.addCell(new Label(column, 0, label));
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, r+1, data[r]));
            }
        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void addData(WritableSheet sheet, int column, String label, int[] data)
    {
        try {
            sheet.addCell(new Label(column, 0, label));
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, r+1, data[r]));
            }
        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    private void addData(WritableSheet sheet, int column, String label, String[] data)
    {
        try {
            sheet.addCell(new Label(column, 0, label));
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Label(column, r+1, data[r]));
            }
        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    private void addData(WritableSheet sheet, int column, String label, List<String> data)
    {
        try {
            sheet.addCell(new Label(column, 0, label));
            for(int r=0; r<data.size(); r++)
            {
                sheet.addCell(new Label(column, r+1, data.get(r)));
            }
        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }     
    
    private String getTime(int count)
    {
        Calendar c = Calendar.getInstance();
        
        c.set(period.start_year, period.start_month, period.start_date, period.start_hour, period.start_min);
        for(int i=0; i<=count; i++) c.add(Calendar.MINUTE, 15);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hour, min);
    }
    
    private String getFileName(String name, String ext) {
        String filepath = name + "." + ext;        
        int count = 0;
        while (true) {
            File file = new File(filepath);
            if (!file.exists()) {
                break;
            }
            filepath = name + " (" + (++count) + ")" + "." + ext;
        }

        return filepath;
    }
    
    private double[] getAverageSpeed(Station[] stations) {
        
        double[][] speedData = new double[stations.length][];
        
        for(int i=0; i<stations.length; i++) {
            speedData[i] = stations[i].getSpeed();
        }
        
        int len = speedData[0].length;
        double[] avg = new double[len];
        for(int i=0; i<len; i++)
        {
            double sum = 0.0;
            int validCount = 0;
            for(int j=0; j<stations.length; j++)
            {
                double d = speedData[j][i];
                if(d > 0) {
                    sum += d;
                    validCount++;
                }
            }
            if(validCount > 0) avg[i] = sum / validCount;
            else avg[i] = -1;
        }
        
        return avg;
    }   
    
    private double[] getAverageDensity(Station[] stations) {

        double[][] densityData = new double[stations.length][];
        
        for(int i=0; i<stations.length; i++) {
            densityData[i] = stations[i].getDensity();
        }

        int len = densityData[0].length;
        double[] avg = new double[len];
        
        for(int i=0; i<len; i++)
        {
            double sum = 0.0;
            int validCount = 0;
            for(int j=0; j<stations.length; j++)
            {
                double d = densityData[j][i];
                if(d > 0) {
                    sum += d;
                    validCount++;
                }
            }
            if(validCount > 0) avg[i] = sum / validCount;
            else avg[i] = -1;
        }

        
        return avg;
    }

    public void setConfig(SRTEConfig config) {
        this.config = config;
    }


    private void saveData() throws Exception {
        System.out.print("Saving Data.........");
        int sheetIdx = 0;
        String filename = this.getFileName("trafficData ("+this.period.getPeriodString()+")", "xls");
        Station[] stations = this.section.getStations();
        String[] times = this.period.getTimelineJustTime();

        int colIdx = 0;

        WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));

        WritableSheet sheet = workbook.createSheet("speed", sheetIdx++);

        this.addData(sheet, colIdx++, "", times);
        double[] au = getAverageSpeed(stations);
        this.addData(sheet, colIdx++, "Average", au);
        this.addData(sheet, colIdx++, "Smoothed", smoothing(au));
        for(Station s : stations) {
            this.addData(sheet, colIdx++, s.toString(), s.getSpeed());
        }

        colIdx = 0;
        sheet = workbook.createSheet("density", sheetIdx++);
        this.addData(sheet, colIdx++, "", times);
        double[] ad = getAverageDensity(stations);
        this.addData(sheet, colIdx++, "Average", ad);
        this.addData(sheet, colIdx++, "Smoothed", smoothing(ad));
        for(Station s : stations) {
            this.addData(sheet, colIdx++, s.toString(), s.getDensity());
        }

        colIdx = 0;

        workbook.write();
        workbook.close();

        System.out.println(" (OK)");
        
        // automatic open file
        //Desktop.getDesktop().open(new File(filename));
    }

    private double[] smoothing(double[] data) {
        int i = 0;
        int j = 0;
        double tot = 0;
        int SMOOTHING_FILTERSIZE = this.config.getInt("SMOOTHING_FILTERSIZE");
        double[] filteredData = new double[data.length];

        for (i = 0; i < SMOOTHING_FILTERSIZE; i++) {
            for (j = 0; j < SMOOTHING_FILTERSIZE; j++) {
                tot += data[j];
            }
            filteredData[i] = tot / SMOOTHING_FILTERSIZE;
            tot = 0;
        }

        for (i = SMOOTHING_FILTERSIZE; i <= data.length - SMOOTHING_FILTERSIZE; i++) {
            for (j = i - SMOOTHING_FILTERSIZE; j < i + SMOOTHING_FILTERSIZE; j++) {
                tot += data[j];
            }
            filteredData[i] = tot / (2 * SMOOTHING_FILTERSIZE);
            tot = 0;
        }

        for (i = data.length - SMOOTHING_FILTERSIZE; i <= data.length - 1; i++) {
            filteredData[i] = filteredData[data.length - (SMOOTHING_FILTERSIZE)];
        }

        return filteredData;
    }
}
