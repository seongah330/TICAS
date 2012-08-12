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

package edu.umn.natsrl.ticas.plugin.traveltimeIndexer;

import edu.umn.natsrl.evaluation.EvalHelper;
import edu.umn.natsrl.evaluation.Evaluation;
import edu.umn.natsrl.evaluation.EvaluationOption;
import edu.umn.natsrl.evaluation.EvaluationResult;
import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.evaluation.OptionType;
import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.NumUtil;
import edu.umn.natsrl.util.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.Label;
import jxl.write.Number;

/**
 *
 * @author Chongmyung Park
 */
public class TravelTimeIndexer {
    private List<Period> periods;
    private Interval dataInterval;
    private Interval evalInterval;
    private Interval ttInterval;
    private final Section section;
    private String[] volumeAnalysisTargetStations;
    private List<EvalData> evaluationResults = new ArrayList<EvalData>();
    EvaluationOption opt = new EvaluationOption();
    private final double freeflowTT;
    private TMO tmo = TMO.getInstance();
    private Infra infra;
    private List<String> toExceptDate = new ArrayList<String>();
    
    public TravelTimeIndexer(Section section, 
            List<Period> periods, 
            String[] targetStations, 
            Interval dataInterval, 
            Interval evalInterval, 
            Interval ttInterval,
            double freeflowTT) {
        
        this.section = section;
        this.periods = periods;
        this.dataInterval = dataInterval;
        this.evalInterval = evalInterval;
        this.ttInterval = ttInterval;
        this.volumeAnalysisTargetStations = targetStations;
        this.freeflowTT = freeflowTT;
        this.infra = tmo.getInfra();
    }
        
    public void run() {
        
        Evaluation.clearCache();
        
        System.out.println("Starting evaluation....");
        Period p = periods.get(0);
        
        opt.setSection(section);
        opt.setPeriods(periods);              
        opt.setStartEndTime(p.start_hour, p.start_min, p.end_hour, p.end_min);
        opt.setInterval(Interval.I30SEC);
        opt.addOption(OptionType.WITHOUT_VIRTUAL_STATIONS);
        
        // detector checker
        opt.setDetectChecker(new IDetectorChecker() {
            @Override
            public boolean check(Detector d) {
                if(d.isHov()) return false; 
                if(d.isAuxiliary()) return false; 
                if(d.isWavetronics()) return false;
                if(d.isAbandoned()) return false;
                return true;
            }
        });

        opt.addOption(OptionType.FIXING_MISSING_DATA);

        try {
            // speed / accel
            opt.setInterval(this.dataInterval);
            Evaluation speed = Evaluation.createEvaluate(OptionType.STATION_SPEED, opt);   
            speed.setPrintDebug(false);
            speed.doEvaluate();
            //speed.saveExcel(".");
            
            Evaluation accel = Evaluation.createEvaluate(OptionType.STATION_ACCEL, opt);        
            accel.setPrintDebug(false);
            accel.doEvaluate();
            
            // tt
            opt.setInterval(this.ttInterval);
            Evaluation tt = Evaluation.createEvaluate(OptionType.EVAL_TT, opt);
            tt.setPrintDebug(false);
            tt.doEvaluate();

            processSpeed(speed);
            processAcceleration(accel);
            processTravelTime(tt);
            
            opt.setInterval(this.evalInterval);
            
            // eval
            opt.removeOption(OptionType.WITHOUT_VIRTUAL_STATIONS);
            Evaluation vmt = Evaluation.createEvaluate(OptionType.EVAL_VMT, opt);  
            vmt.setPrintDebug(false);
            vmt.doEvaluate();
            
            Evaluation dvh = Evaluation.createEvaluate(OptionType.EVAL_DVH, opt);
            dvh.setPrintDebug(false);
            dvh.doEvaluate();
            
            Evaluation lvmt = Evaluation.createEvaluate(OptionType.EVAL_LVMT, opt);
            lvmt.setPrintDebug(false);
            lvmt.doEvaluate();
            
            processTotal(vmt);
            processTotal(lvmt);
            processTotal(dvh);
//            processTotal()
            processTravelTimeIndex(tt, vmt);
            
           // volume
            if(this.volumeAnalysisTargetStations != null && this.volumeAnalysisTargetStations.length > 0 && !this.volumeAnalysisTargetStations[0].isEmpty()) {
                opt.setInterval(Interval.I1HOUR);
                Evaluation flow = Evaluation.createEvaluate(OptionType.STATION_TOTAL_FLOW, opt);
                flow.setPrintDebug(false);
                flow.doEvaluate();
                processFlow(flow);
            }               
            
            //printResults();            
            saveResults();
            
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fail to evaulate");
        }
    }
    
    /**
     * Calculate average max speed
     * @param speed 
     */
    private void processSpeed(Evaluation speed)
    {
        List<EvaluationResult> results = speed.getResult();
        EvalData evalData = new EvalData("Average of Max Speed Difference");
        
        // iterate result of each day
        for(EvaluationResult res : results) {
            
            if(res.isStatistic()) continue;
            
            res = speed.removeVirtualStationFromResult(res);
            String title = res.getName();
            List<Double> maxSpeedDiff = new ArrayList<Double>();

            
            // iterate row
            for(int row=res.ROW_DATA_START(); row<res.getRowSize(0); row++) {            
                
                double maxSpeedDiffAtSameTimeLine = 1000;
                // iterate colume
                for(int col=res.COL_DATA_START(); col<res.getColumnSize()-1; col++) {
                    Double upStationSpeed = Double.parseDouble(res.get(col, row).toString());
                    Double downStationSpeed = Double.parseDouble(res.get(col+1, row).toString());
                    double diff = downStationSpeed - upStationSpeed;
                    if(diff >= 0) continue;
                    if(diff < maxSpeedDiffAtSameTimeLine) {
                        maxSpeedDiffAtSameTimeLine = diff;
                    }
                }
                if(maxSpeedDiffAtSameTimeLine < 0) maxSpeedDiff.add(maxSpeedDiffAtSameTimeLine);
                else System.out.println(res.getName() + " : max speed diff at same time line > 0 (time=" + res.get(0, row) +", value=" + maxSpeedDiffAtSameTimeLine + ")");
            }                
            String date = getDateString(title);
            evalData.add(date, average(maxSpeedDiff));
        }
        
        this.evaluationResults.add(evalData);
     
    }
    
    /**
     * Calculate total flow of given station for every hour (member variable)
     * @param totalFlow 
     */
    private void processFlow(Evaluation totalFlow)
    {
        List<EvaluationResult> results = totalFlow.getResult();
        Period period = this.periods.get(0);
        int totalHour = period.end_hour - period.start_hour;
        
        HashMap<String, List<EvalData>> evalDataMap = new HashMap<String, List<EvalData>>();
        int hour = period.start_hour + 1;
        for(int j=0; j<this.volumeAnalysisTargetStations.length; j++) {
            List<EvalData> evalDataList = new ArrayList<EvalData>();
            for(int i=0; i<totalHour; i++) {
                evalDataList.add(new EvalData("Total Flow of " + this.volumeAnalysisTargetStations[j] + " for " + (hour+i-1)+ ":00 ~ " + (hour+i) + ":00"));
            }
            
            evalDataMap.put(this.volumeAnalysisTargetStations[j], evalDataList);
        }        
        
        // iterate result of each day
        for(EvaluationResult res : results) {
                        
            if(res.isStatistic()) continue;
            
            res = totalFlow.removeVirtualStationFromResult(res);
            String title = res.getName();
            String date = getDateString(title);
            
            // iterate colume
            for(int col=res.COL_DATA_START(); col<res.getColumnSize(); col++) {            
                
                String stationLabel = res.get(col, 0).toString();
                
                boolean isTarget = false;
                String targetStationName = "";
                for(int j=0; j<this.volumeAnalysisTargetStations.length; j++) {
                    if(stationLabel.contains(this.volumeAnalysisTargetStations[j])) {
                        isTarget = true;
                        targetStationName = this.volumeAnalysisTargetStations[j];
                        break;
                    }
                }
                if(!isTarget) continue;
                    
                // iterate row
                int hourIdx = 0;
                List<EvalData> evalDataList = evalDataMap.get(targetStationName);
                for(int row=res.ROW_DATA_START(); row<res.getRowSize(0); row++) {                            
                    Double value = Double.parseDouble(res.get(col, row).toString());
                    EvalData evalData = evalDataList.get(hourIdx++);
                    evalData.add(date, value);
                }
                                
            }                
            
        }
        
        for(List<EvalData> evalDataList : evalDataMap.values()) {
            this.evaluationResults.addAll(evalDataList);
        }
     
    }    
    
    /**
     * Calculate max, average, count, mile hour....
     * @param accel 
     */
    private void processAcceleration(Evaluation accel) 
    {
        List<EvaluationResult> results = accel.getResult();
        
        EvalData evalMaxDeccel = new EvalData("Average of Max Deccelleration");
        EvalData evalAveDeccel = new EvalData("Average of Average Deccelleration");
        EvalData evalAveAccel = new EvalData("Average of Average Acceleration");
        
        EvalData evalAveDeccel_1500 = new EvalData("Count of Acceleration (-1500 ~)");
        EvalData evalAveDeccel_1500_2500 = new EvalData("Count of Acceleration (-1500 ~ -2500)");
        EvalData evalAveDeccel_2500_3500 = new EvalData("Count of Acceleration (-2500 ~ -3500)");
        EvalData evalAveDeccel_3500_4500 = new EvalData("Count of Acceleration (-3500 ~ -4500)");        
        EvalData evalAveDeccel_4500 = new EvalData("Count of Acceleration (-4500 ~)");
        
        EvalData evalMileHour_1500 = new EvalData("Mile Hour (sum of mile hour over -1500)");
        EvalData evalMileHour_2500 = new EvalData("Mile Hour (sum of mile hour over -2500)");
        EvalData evalMileHour_3500 = new EvalData("Mile Hour (sum of mile hour over -3500)");       
        EvalData evalMileHour_4500 = new EvalData("Mile Hour (sum of mile hour over -4500)");
        
        // iterate result of each day
        for(EvaluationResult res : results) {
            
            if(res.isStatistic()) continue;
            
            res = accel.removeVirtualStationFromResult(res);
            String title = res.getName();
            
            List<Double> maxDeccel = new ArrayList<Double>();
            List<Double> avgDeccel = new ArrayList<Double>();
            List<Double> avgAccel = new ArrayList<Double>();
            
            double count_1500 = 0;
            double count_1500_2500 = 0;
            double count_2500_3500 = 0;
            double count_3500_4500 = 0;
            double count_4500 = 0;
            
            double mileHour_1500 = 0;
            double mileHour_2500 = 0;
            double mileHour_3500 = 0;
            double mileHour_4500 = 0;
            
            // iterate row
            for(int row=res.ROW_DATA_START(); row<res.getRowSize(0); row++) {            
                
                double maxDeccelAtSameTimeLine = 1000000;
                List<Double> deccelAtSameTime = new ArrayList<Double>();
                List<Double> accelAtSameTime = new ArrayList<Double>();
                
                // iterate colume
                // first station's accel = 0
                for(int col=res.COL_DATA_START()+1; col<res.getColumnSize(); col++) {
                    
                    Double a = Double.parseDouble(res.get(col, row).toString());                                        
                    if(a < 0 && a < maxDeccelAtSameTimeLine) {
                        maxDeccelAtSameTimeLine = a;
                    }
                    
                    // only decceleration
                    if(a < 0) deccelAtSameTime.add(a);
                    if(a > 0) accelAtSameTime.add(a);
                    
                    // over decceleration count
                    if(a <= -1500) count_1500++;
                    if(a < -1500 && a >= -2500) count_1500_2500++;
                    if(a < -2500 && a >= -3500) count_2500_3500++;
                    if(a < -3500 && a >= -4500) count_3500_4500++;
                    if(a < -4500) count_4500++;
                    
                    // mile hour
                    String stationName = EvalHelper.getStationNameFromTitle(res.get(col, 0).toString());
                    Station station = infra.getStation(stationName);
                    double distanceInMile = (double)station.getDistanceToUpstreamStation(this.section.getName()) / InfraConstants.FEET_PER_MILE;
                    
                    // FIXME : is it correct that dividing distance with 60 ?
                    double mileHour = distanceInMile/(3600D / this.dataInterval.second);
                    
                    if(a <= -1500) mileHour_1500 += mileHour;
                    if(a <= -2500) mileHour_2500 += mileHour;
                    if(a <= -3500) mileHour_3500 += mileHour;
                    if(a <= -4500) mileHour_4500 += mileHour;
                    
                }                
                
                if(maxDeccelAtSameTimeLine < 0) maxDeccel.add(maxDeccelAtSameTimeLine);
                else maxDeccelAtSameTimeLine = 0;
                
                avgDeccel.add(average(deccelAtSameTime));
                avgAccel.add(average(accelAtSameTime));
                
            }              
            
            String date = getDateString(title);
            evalMaxDeccel.add(date, average(maxDeccel));           
            evalAveDeccel.add(date, average(avgDeccel));           
            evalAveAccel.add(date, average(avgAccel));            
            evalAveDeccel_1500.add(date, count_1500);           
            evalAveDeccel_1500_2500.add(date, count_1500_2500);            
            evalAveDeccel_2500_3500.add(date, count_2500_3500);            
            evalAveDeccel_3500_4500.add(date, count_3500_4500);            
            evalAveDeccel_4500.add(date, count_4500);
            evalMileHour_1500.add(date, mileHour_1500);            
            evalMileHour_2500.add(date, mileHour_2500);
            evalMileHour_3500.add(date, mileHour_3500);            
            evalMileHour_4500.add(date, mileHour_4500);            
        }
        
        this.evaluationResults.add(evalMaxDeccel);
        this.evaluationResults.add(evalAveDeccel);
        this.evaluationResults.add(evalAveAccel);
        
        this.evaluationResults.add(evalAveDeccel_1500);
        this.evaluationResults.add(evalAveDeccel_1500_2500);
        this.evaluationResults.add(evalAveDeccel_2500_3500);
        this.evaluationResults.add(evalAveDeccel_3500_4500);
        this.evaluationResults.add(evalAveDeccel_4500);
        
        this.evaluationResults.add(evalMileHour_1500);
        this.evaluationResults.add(evalMileHour_2500);
        this.evaluationResults.add(evalMileHour_3500);
        this.evaluationResults.add(evalMileHour_4500);    

    }
    
    /**
     * Get total value from given evaluation
     * @param eval
     * @param periods 
     */
    private void processTotal(Evaluation eval)
    {
        List<EvaluationResult> results = eval.getResult();
        EvalData evalData = new EvalData(eval.getName());
        
        // last sheet is total sheet
        EvaluationResult res = results.get(results.size()-1);
        res = eval.removeVirtualStationFromResult(res);
        
        for(int i=res.COL_DATA_START(); i<res.getColumnSize(); i++) {
            List column = res.getColumn(i);
            String title = column.get(0).toString();
            Double total = Double.parseDouble(column.get(res.getRowSize(i)-1).toString());            
            String date = getDateString(title);
            evalData.add(date, total);
        }
        
        this.evaluationResults.add(evalData);
    }
    
    /**
     * Get average travel time for given result
     * @param tt
     * @param periods 
     */
    private void processTravelTime(Evaluation tt)
    {
        List<EvaluationResult> results = tt.getResult();
        EvalData evalData = new EvalData("Average Travel Time");
        
        // last sheet is total sheet
        EvaluationResult res = results.get(results.size()-1);

        for(int col=res.COL_DATA_START(); col<res.getColumnSize()-1; col++) {
            double avgTT = Double.parseDouble(res.get(col, res.getRowSize(col)-1).toString());
            String title = res.get(col, 0).toString();
            String date = getDateString(title);
            evalData.add(date, avgTT);
        }
        
        this.evaluationResults.add(evalData);        
    }    
    
    private void processTravelTimeIndex(Evaluation tt, Evaluation vmt) {
        List<EvaluationResult> ttResults = tt.getResult();        
        List<EvaluationResult> vmtResults = vmt.getResult();
        
        // free flow travel time
        double freeFlow_speedLimit_TT = getFreeFlowTravelTimeBySpeedLimit();
        
        EvalData monthlyAverageVMT = new EvalData("Monthly Average VMT");
        
        // Beffere Index with 85percentile travel time and 15 percentile free flow travel time
        EvalData BI_85p = new EvalData("Buffer Index (85p)");                
        EvalData BI_90p = new EvalData("Buffer Index (90p)");
        EvalData BI_95p = new EvalData("Buffer Index (95p)");
                
        // Planning Index
        EvalData PI_85p_15p_FFT = new EvalData("Planing Index (85p, FreeFlowTT=15p)");        
        EvalData PI_90p_15p_FFT = new EvalData("Planing Index (90p, FreeFlowTT=15p)");
        EvalData PI_95p_15p_FFT = new EvalData("Planing Index (95p, FreeFlowTT=15p)");        
        
        EvalData PI_85p_UserFFT = new EvalData("Planing Index (85p, UserFreeFlowTT="+this.freeflowTT+"min)");        
        EvalData PI_90p_UserFFT = new EvalData("Planing Index (90p, UserFreeFlowTT="+this.freeflowTT+"min)");
        EvalData PI_95p_UserFFT = new EvalData("Planing Index (95p, UserFreeFlowTT="+this.freeflowTT+"min)");       
        
        EvalData PI_85p_SLFFT = new EvalData("Planing Index (85p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)");        
        EvalData PI_90p_SLFFT = new EvalData("Planing Index (90p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)");
        EvalData PI_95p_SLFFT = new EvalData("Planing Index (95p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)");            
        
        // Travel Time Index
        EvalData TTI_15p_FFT = new EvalData("Travel Time Index (FreeFlowTT=15p)");        
        EvalData TTI_UserFFT = new EvalData("Travel Time Index (UserFreeFlowTT="+this.freeflowTT+"min)");        
        EvalData TTI_SLFFT = new EvalData("Travel Time Index (SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)");        
        
        
        // total VMT for each day and month
        EvaluationResult totalVMTResult = vmtResults.get(vmtResults.size()-1);
        HashMap<String, Double> totalMonthVMT = new HashMap<String, Double>();
        HashMap<String, Double> totalDayVMT = new HashMap<String, Double>();
        HashMap<String, Integer> monthVMTCount = new HashMap<String, Integer>();
        for(int c=totalVMTResult.COL_DATA_START(); c<totalVMTResult.getColumnSize(); c++)
        {
            String month = getMonthString(totalVMTResult.get(c, 0).toString());
            String date = getDateString(totalVMTResult.get(c, 0).toString());
            int lastRow = totalVMTResult.getRowSize(c)-1;
            Double total = totalMonthVMT.get(month);
            double totalOfDay = Double.parseDouble(totalVMTResult.get(c, lastRow).toString());
            
            totalDayVMT.put(date, totalOfDay);
            Integer cnt = monthVMTCount.get(month);
            if(cnt == null) cnt = 1;
            else cnt++;
            monthVMTCount.put(month, cnt);
            
            if(total == null) {
                totalMonthVMT.put(month, totalOfDay);
            } else {
                totalMonthVMT.put(month, totalOfDay+total);                
            }
        }     
        
        for(String month : totalMonthVMT.keySet()) {
            Double total = totalMonthVMT.get(month);
            Integer count = monthVMTCount.get(month);
            monthlyAverageVMT.add(month, total/count);
        }
        
        // iterate results (all days)
        for(int i=0; i<ttResults.size(); i++) {
            
            EvaluationResult ttResult = ttResults.get(i);
            
            if(ttResult.isStatistic()) continue;
            
            EvaluationResult vmtResult = null;
            for(EvaluationResult res : vmtResults)
            {
                if(res.getName().equals(ttResult.getName())) {
                    vmtResult = res;
                }
            }
            
            if(vmtResult == null) {
                System.out.println("  - ERROR!! No VMT Result for TT Index (date=" + ttResult.getName()+")");                
                continue;
            }
            
            List<Double> aDayTravelTimes = new ArrayList<Double>();
            String title = ttResult.getName();
            
            

            // iterate row
            for(int row=ttResult.ROW_DATA_START(); row<ttResult.getRowSize(0); row++) {                                
                Double value = Double.parseDouble(ttResult.get(ttResult.getColumnSize()-1, row).toString());
                aDayTravelTimes.add(value);
            }
            
            // average travel time
            double average_TT = average(aDayTravelTimes);
                        
            double freeFlow_15p_TT = percentile(aDayTravelTimes, 15.0);
            double freeFlow_user_TT = this.freeflowTT;
            
            // percentile
            double percentile_85_TT = percentile(aDayTravelTimes, 85.0);
            double percentile_90_TT = percentile(aDayTravelTimes, 90.0);
            double percentile_95_TT = percentile(aDayTravelTimes, 95.0);
            
            // buffer index for each percentile
            double bufferIndex85 = ( percentile_85_TT - average_TT ) / average_TT;
            double bufferIndex90 = ( percentile_90_TT - average_TT ) / average_TT;
            double bufferIndex95 = ( percentile_95_TT - average_TT ) / average_TT;
                        
            double planingIndex8515 = percentile_85_TT / freeFlow_15p_TT;
            double planingIndex9015 = percentile_90_TT / freeFlow_15p_TT;
            double planingIndex9515 = percentile_95_TT / freeFlow_15p_TT;           
            
            double planingIndex85user = percentile_85_TT / freeFlow_user_TT;
            double planingIndex90user = percentile_90_TT / freeFlow_user_TT;
            double planingIndex95user = percentile_95_TT / freeFlow_user_TT;  
            
            double planingIndex85SL = percentile_85_TT / freeFlow_speedLimit_TT;
            double planingIndex90SL = percentile_90_TT / freeFlow_speedLimit_TT;
            double planingIndex95SL = percentile_95_TT / freeFlow_speedLimit_TT;              
            
            double travelTimeIndex_15p = average_TT / freeFlow_15p_TT;
            double travelTimeIndex_User = average_TT / freeFlow_user_TT;
            double travelTimeIndex_SL = average_TT / freeFlow_speedLimit_TT;
            
            
            String date = getDateString(title);
            String month = getMonthString(title);
            
            // Remove these dates if planning index < 0
            if(planingIndex85SL < 1 || planingIndex90SL < 1 || planingIndex95SL < 1) {
                double v = totalDayVMT.get(date);
                double mv = totalMonthVMT.get(month);
                mv -= v;
                totalMonthVMT.put(month, mv);
                totalDayVMT.remove(date);
                toExceptDate.add(date);
                continue;
            }
            if(freeFlow_user_TT < 0) {
                planingIndex85user = -1;
                planingIndex90user = -1;
                planingIndex95user = -1;
                travelTimeIndex_User = -1;
            }
            
            

            BI_85p.add(date, bufferIndex85);
            BI_90p.add(date, bufferIndex90);
            BI_95p.add(date, bufferIndex95);            
            
            PI_85p_15p_FFT.add(date, planingIndex8515);
            PI_90p_15p_FFT.add(date, planingIndex9015);
            PI_95p_15p_FFT.add(date, planingIndex9515);

            PI_85p_UserFFT.add(date, planingIndex85user);
            PI_90p_UserFFT.add(date, planingIndex90user);
            PI_95p_UserFFT.add(date, planingIndex95user);
            
            PI_85p_SLFFT.add(date, planingIndex85SL);
            PI_90p_SLFFT.add(date, planingIndex90SL);
            PI_95p_SLFFT.add(date, planingIndex95SL);
            
            TTI_15p_FFT.add(date, travelTimeIndex_15p);
            TTI_UserFFT.add(date, travelTimeIndex_User);
            TTI_SLFFT.add(date, travelTimeIndex_SL);
        }
        
        this.evaluationResults.add(monthlyAverageVMT);
        
        this.evaluationResults.add(BI_85p);
        processAverageIndex("Buffer Index Average (85p)", BI_85p, totalDayVMT, totalMonthVMT);
        
        this.evaluationResults.add(BI_90p);
        processAverageIndex("Buffer Index Average (90p)", BI_90p, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(BI_95p);
        processAverageIndex("Buffer Index Average (95p)", BI_95p, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(PI_85p_15p_FFT);
        processAverageIndex("Planning Index Average (85p, FreeFlowTT=15p)", PI_85p_15p_FFT, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(PI_90p_15p_FFT);
        processAverageIndex("Planning Index Average (90p, FreeFlowTT=15p)", PI_90p_15p_FFT, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(PI_95p_15p_FFT);        
        processAverageIndex("Planning Index Average (95p, FreeFlowTT=15p)", PI_95p_15p_FFT, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(PI_85p_UserFFT);
        processAverageIndex("Planning Index Average (85p, UserFreeFlowTT="+this.freeflowTT+"min)", PI_85p_UserFFT, totalDayVMT, totalMonthVMT);        
        
        this.evaluationResults.add(PI_90p_UserFFT);
        processAverageIndex("Planning Index Average (90p, UserFreeFlowTT="+this.freeflowTT+"min)", PI_90p_UserFFT, totalDayVMT, totalMonthVMT);                
        
        this.evaluationResults.add(PI_95p_UserFFT);                        
        processAverageIndex("Planning Index Average (95p, UserFreeFlowTT="+this.freeflowTT+"min)", PI_95p_UserFFT, totalDayVMT, totalMonthVMT);                
        
        this.evaluationResults.add(PI_85p_SLFFT);
        processAverageIndex("Planning Index Average (85p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)", PI_85p_SLFFT, totalDayVMT, totalMonthVMT);                
        
        this.evaluationResults.add(PI_90p_SLFFT);
        processAverageIndex("Planning Index Average (90p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)", PI_90p_SLFFT, totalDayVMT, totalMonthVMT);                        

        this.evaluationResults.add(PI_95p_SLFFT);
        processAverageIndex("Planning Index Average (95p, SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)", PI_95p_SLFFT, totalDayVMT, totalMonthVMT);                                        

        this.evaluationResults.add(TTI_15p_FFT);
        processAverageIndex("Travel Time Index Average (FreeFlowTT=15p)", TTI_15p_FFT, totalDayVMT, totalMonthVMT);
        
        this.evaluationResults.add(TTI_UserFFT);
        processAverageIndex("Travel Time Index Average (UserFreeFlowTT="+this.freeflowTT+"min)", TTI_UserFFT, totalDayVMT, totalMonthVMT);
        
        this.evaluationResults.add(TTI_SLFFT);        
        processAverageIndex("Travel Time Index Average (SL-based FreeFlowTT="+freeFlow_speedLimit_TT+"min)", TTI_SLFFT, totalDayVMT, totalMonthVMT);
        
    }    
    
    private void processAverageIndex(String name, EvalData evalData, HashMap<String, Double> totalDayVMT, HashMap<String, Double> totalMonthVMT) {

        EvalData averageIndex = new EvalData(name);                
        
        Set<String> monthnames = new TreeSet<String>(totalMonthVMT.keySet());
        
        for(String month : monthnames)
        {
            //System.out.println(" + processAverageIndex : " + month);
            double totalVMTofMonth = totalMonthVMT.get(month);
            double totalIndex = 0;
            int count = 0;
            for(int i=0; i<evalData.dates.size(); i++) {                
                String date = evalData.dates.get(i);
                String month_of_day = date.substring(0, 7);
                if(!month_of_day.equals(month)) continue;
                double data = evalData.data.get(i);
                double totalVMTofDay = totalDayVMT.get(date);
                totalIndex += data * totalVMTofDay;
                //System.out.println("    - targetMonth="+month+", date="+date+", month="+month_of_day+"VMTofDay="+totalVMTofDay+", VMTofMonth="+totalVMTofMonth+", totalIndex="+totalIndex);
                count++;                
            }
            if(totalIndex < 0) averageIndex.add(month, -1D);
            else averageIndex.add(month, totalIndex/totalVMTofMonth);
        }
        this.evaluationResults.add(averageIndex);        
    }    
    
    /**
     * Return average of list
     * @param data
     * @return 
     */
    private double average(List<Double> data)
    {
        double total = 0;
        for(Double d : data)
        {
            total += d;
        }
        return total/data.size();
    }    
    
    /**
     * Print results on console
     */
    private void printResults() {
        for(EvalData ed : evaluationResults) {
            System.out.println("[ " + ed.name + " ] ---------------------");
            for(int i=0; i<ed.data.size(); i++) {
                String date = ed.dates.get(i);
                double data = ed.data.get(i);
                System.out.println("   " + date + " => " + data);
            }           
        }        
    }

    /**
     * Save results into EXCEL file
     */
    private void saveResults() {
        try {
            String workbookFile = FileHelper.getNumberedFileName("VSL Evaluation.xls");
            WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));
            
            Period p = this.periods.get(0);
            String startDate = String.format("%4d-%02d-%02d", p.start_year, p.start_month, p.start_date);
            String hours = String.format("%d - %d", p.start_hour, p.end_hour);
            p = this.periods.get(periods.size()-1);
            String endDate = String.format("%4d-%02d-%02d", p.start_year, p.start_month, p.start_date);
            
            int col = 0;            
            int row = 0;            
            WritableSheet infoSheet = workbook.createSheet("meta", 1);
            infoSheet.addCell(new Label(col++, row, "Section"));
            infoSheet.addCell(new Label(col--, row++, this.section.getName() + " : " + this.section.getRoutes()));
            infoSheet.addCell(new Label(col++, row, "Dates"));
            infoSheet.addCell(new Label(col--, row++, startDate + " ~ " + endDate));
            infoSheet.addCell(new Label(col++, row, "Hours"));
            infoSheet.addCell(new Label(col--, row++, hours));            
            infoSheet.addCell(new Label(col++, row, "Interval(Speed,Accel,Flow)"));
            infoSheet.addCell(new Label(col--, row++, this.dataInterval.toString()));
            infoSheet.addCell(new Label(col++, row, "Interval(VMT/DVH)"));
            infoSheet.addCell(new Label(col--, row++, this.evalInterval.toString()));            
            infoSheet.addCell(new Label(col++, row, "Interval(TT)"));            
            infoSheet.addCell(new Label(col--, row++, this.ttInterval.toString()));
            infoSheet.addCell(new Label(col++, row, "Volume Analisys Target Station"));
            infoSheet.addCell(new Label(col--, row++, StringUtil.join(this.volumeAnalysisTargetStations, ",")));
            infoSheet.addCell(new Label(col++, row, "Free Flow Travel Time by User"));
            infoSheet.addCell(new Label(col--, row++, this.freeflowTT + "min"));
            
            
            WritableSheet sheet = workbook.createSheet("data", 0);
            col = 0;            
            row = 0;
            System.out.println(evaluationResults.size());
            for(EvalData ed : evaluationResults) {
                int colIdx = 0;
                
                // print evaluation name
                sheet.addCell(new Label(colIdx++, row, ed.name));
//                System.out.print(ed.name+" : "+ed.dates.size());
                // print dates
                for(int i=0; i<ed.dates.size(); i++) {
                    String date = ed.dates.get(i);
                    double data = 0;
                    if(ed.data.size() == ed.dates.size())
                        data = ed.data.get(i); 
                    
//                    System.out.print(" ,"+data+"("+date+")");
//                    if(this.toExceptDate.contains(date)) continue;
                    sheet.addCell(new Label(colIdx, row, date));
                    sheet.addCell(new Number(colIdx++, row+1, NumUtil.roundUp(data, 2)));                                        
                }                
                row += 2;
//                System.out.println();

            }
            workbook.write();
            workbook.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return percentile
     * @param aDayTravelTimes
     * @param percentile
     * @return 
     */
    private Double percentile(List<Double> aDayTravelTimes, Double percentile) {
        Collections.sort(aDayTravelTimes);
        int percentileIndex = (int)Math.round(aDayTravelTimes.size() * percentile / 100) -1;
        return aDayTravelTimes.get(percentileIndex);
    }

    /**
     * Return travel time in minute calculated with speed limit
     * @return 
     */
    private double getFreeFlowTravelTimeBySpeedLimit() {
        
        double tt = 0;
        Station[] stations = this.section.getStations();
        for(int i=0; i<stations.length-1; i++)
        {
            Station s = stations[i];
            int speedLimit = s.getSpeedLimit();
            double distance = this.section.getDistanceToDownStation(s) / InfraConstants.FEET_PER_MILE;
            tt += speedLimit * distance;
        }
        return NumUtil.roundUp(tt/60, 2);
    }

    private String getDateString(String title)
    {
        String date = title.substring(0, 8);
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
    }
    
    private String getMonthString(String title)
    {
        String date = title.substring(0, 8);
        return date.substring(0, 4) + "-" + date.substring(4, 6);
    }    

    
    /**
     * Class for evaluation result data 
     */
    class EvalData {
        String name;
        List<String> dates = new ArrayList<String>();
        List<Double> data = new ArrayList<Double>();

        public EvalData(String name) {
            this.name = name;
        }     
        
        public void add(String date, Double value)
        {
            this.dates.add(date);
            this.data.add(value);
        }
    }
}
