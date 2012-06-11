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
/*
 * CumulativeRampGUI.java
 *
 * Created on Apr 21, 2011, 3:45:47 PM  //Congmyung Park
 * Modification on Jan 20, 2012, 10:09:01 PM //Soobin Jeon
 */
package edu.umn.natsrl.ticas.plugin.cumulative;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.NumUtil;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import jxl.Workbook;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Chongmyung Park
 */
public class CumulativeRampGUI extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    private boolean buttoncheck = false;
    private boolean IsSmoothing = false;
    private boolean IsMinRate = false;
    private boolean IsStr = false; //Input error correction algorithm (this have problem)
    private int smoothingTimeIdx = 0;
    
    private PrintStream backupOut;
    private PrintStream backupErr;
    
    /** Creates new form SimulationExampleGUI */
    public CumulativeRampGUI(PluginFrame simFrame) {
        initComponents();
        init();
        simFrame.setSize(700, 700);
    }

    private void init() {
        IsStr = false;
        DateChecker dc = DateChecker.getInstance();

        // date checker
        this.natsrlCalendar.setDateChecker(dc);

        // interval
        for (Interval i : Interval.values()) {
            this.cbxInterval.addItem(i);
        }

        // duration
        this.cbxDuration.addItem("Select");
        for (int i = 1; i <= 32; i++) {
            this.cbxDuration.addItem(i);
        }
        
        loadSection();
    }
    
    /*
     * Read Data
     */
    private void readData(Section section, Vector<Period> periods, IDetectorChecker dc) throws Exception {

        String filename = FileHelper.getNumberedFileName(section.getName() + ".xls");
        WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
        SectionHelper sectionHelper = new SectionHelper(section);
        ArrayList<EntranceState> entrances = sectionHelper.getEntranceStates();

        int sheetNum = 0;
        for (Period p : periods) {
            System.out.println("["+sheetNum+"] "+p.getPeriodStringWithoutTime());
            System.out.println("- Data Loading..");
            section.loadData(p, false);
            System.out.println("- Load Complete.");
            WritableSheet sheet = workbook.createSheet(p.getPeriodStringWithoutTime(), sheetNum++);
            int col = 0;
            
            addData(sheet,col++,new String[]{"",""},p.getTimeline());
            
            for (EntranceState es : entrances) {
                if (es.getRNode() == null) {
                    continue;
                }
                System.out.println("input : " + es.getRNode().getLabel() + "(Calculating..)");
                col = writeCumulativeSum(sheet, es, col,p);
                
                /*col+= 2;
                if(this.cbxvol.isSelected())
                    col += 4;
                if(this.cbxcumul.isSelected())
                    col += 4;*/
            }
            System.out.println("["+sheetNum+"] "+p.getPeriodStringWithoutTime() + " [Complete]");
        }

        
        System.out.println("Writing Excel Files..");
        workbook.write();
        workbook.close();
        System.out.println("Complete.");
        ButtonCheck();
        
        Desktop.getDesktop().open(new File(filename));
    }
    
    /*
     * Calculate Cumulative flowRate
     */
    private void CalculateCumulative(double[] input, double[] output, double[] ci, double[] co, double[] gap){
        if(IsStr){
            LimitedQueue<Double> gaptemp = new LimitedQueue<Double>(8);
            for (int i = 0; i < input.length; i++) {
                
                if(i != 0){
                    ci[i] = input[i] >= 0 ? input[i] + ci[i-1] : ci[i-1];
                    co[i] = output[i] >= 0 ? output[i] + co[i-1] : co[i-1];
                }else{
                    ci[i] = input[i] >= 0 ? input[i] : 0;
                    co[i] = output[i] >= 0 ? output[i] : 0;
                }
                if(i>8){
                    ci[i] = gaptemp.getAverage(0,8) >= 1 ? ci[i] + (int)(gaptemp.getAverage(0, 8)*1) : ci[i];
                }
                /*
                 *  if(gaptemp.getAverage(0,8) >= 1)
                        ci[i] = ci[i] + (int)(gaptemp.getAverage(0, 8)*1);
                    else if(gaptemp.getAverage(0,8) <= -1){
                        System.out.println("("+i+")-average : " + gaptemp.getAverage(0, 8) + "ci : " + ci[i]);
                        ci[i] = ci[i] + (int)(gaptemp.getAverage(0, 8)*1);
                        System.out.println("ci : "+ci[i]);
                        
                    }
                 */
                gaptemp.push(co[i]-ci[i]);
                
                //calculate Gap
                gap[i] = Math.abs(ci[i]-co[i]);
                
            }
        }else{
            double[] temp = new double[2];
            for (int i = 0; i < input.length; i++) {
                if(IsSmoothing){
                    temp[0] = 0;
                    temp[1] = 0;
                    for(int k=0;k<smoothingTimeIdx;k++){
                        if((i-k) < 0){
                            temp[0] += 0;
                            temp[1] += 0;
                        }else{
                            temp[0] += input[i-k] >= 0 ? input[i-k] : 0;
                            temp[1] += output[i-k] >=0 ? output[i-k] : 0;
                        }
                    }                    
                    if(i != 0){
                        ci[i] = temp[0] == 0 ? ci[i-1] : (temp[0] / smoothingTimeIdx) + ci[i-1];
                        co[i] = temp[1] == 0 ? co[i-1] : (temp[1] / smoothingTimeIdx) + co[i-1];                    
                    }else{
                        ci[i] = temp[0] == 0 ? 0 : (temp[0] / smoothingTimeIdx);
                        co[i] = temp[1] == 0 ? 0 : (temp[1] / smoothingTimeIdx);                    
                    }
                }else{
                    if(i != 0){
                        ci[i] = input[i] >= 0 ? input[i] + ci[i-1] : ci[i-1];
                        co[i] = output[i] >= 0 ? output[i] + co[i-1] : co[i-1];
                    }else{
                        ci[i] = input[i] >= 0 ? input[i] : 0;
                        co[i] = output[i] >= 0 ? output[i] : 0;
                    }
                }
                //calculate Gap
                gap[i] = Math.abs(ci[i]-co[i]);
            }
        }
    }
    
    /*
     * Calculate Culmulative Volume Data
     */
    private void Calculatevolume(double[] input, double[] output, double[] ci, double[] co,double interval){
        double calvol = 120 / (interval/30);
        //System.out.println("calvol : " + calvol);
        for (int i = 0; i < input.length; i++) {
            if(i != 0){
                ci[i] = input[i] >= 0 ? (input[i]/calvol) : input[i];
                co[i] = output[i] >= 0 ? (output[i]/calvol) : output[i];
            }else{
                ci[i] = input[i]/calvol;
                co[i] = output[i]/calvol;
            }
        }
    }
    
    private double CalculateWaitingTime(BoundedSampleHistory cumulativeDemand, double Cf_current){
        int STEP_SECONDS = 30;
        int maxWaitTimeIndex = 7;
        // current cumulative passage flow
        if(cumulativeDemand.size() - 1 < maxWaitTimeIndex-1)
                return 0;

        for(int i = 0; i < cumulativeDemand.size(); i++) {
                double queue = cumulativeDemand.get(i);
                if(queue < Cf_current) {
                        if(i == 0){
                                return 0;
                        }
                        double bf_queue =
                                cumulativeDemand.get(i - 1);
                        double qd = bf_queue - queue;
                        if(qd != 0) {
                                double IF_time = STEP_SECONDS *
                                        (Cf_current - queue) / qd;
                                return  STEP_SECONDS * i -
                                        IF_time;

                        } else{
                                return 0;
                        }
                }
        }
        return maxWaitTimeIndex * (STEP_SECONDS + 1);
    }
    /*
     * Caculate Waiting Time
     */
    private void CalculateWaitingTime(double[] ci, double[] co, double[] waitingtime, int interval){
        //calculate waiting time
            
            BoundedSampleHistory cumulativeDemand = new BoundedSampleHistory((240/30));
            for(int z=0;z<co.length;z++){
                  double Cf_current = co[z];
                  cumulativeDemand.push(ci[z]);
                  waitingtime[z] = CalculateWaitingTime(cumulativeDemand, Cf_current);
//                if((ci[i] > co[i]) && i > 0){
//                    
//                    //else{
//                        double  outputvalue = co[i];
//                        double outputidx = (i+1) * interval;
//                        double[] startdot = new double[2]; //start dot
//                        double[] enddot = new double[2]; // end dot
//
//                        /*System.out.print("debug : outputvalue = " + outputvalue
//                                + " outputidx = " + outputidx);*/
//
//                        //Find input line
//                        /*for(int z = i; z>=0; z--){
//                            if(ci[z] <= outputvalue){
//                                startdot[0] = (z+1) * interval;
//                                startdot[1] = ci[z];
//                                enddot[0] = (z+2) * interval;
//                                enddot[1] = ci[z+1];
//                                break;
//                            }
//                        }*/
//                        startdot[0] = outputidx-interval;
//                        startdot[1] = ci[i-1];
//                        enddot[0] = outputidx;
//                        enddot[1] = ci[i];
//
//                        if(startdot[0] == 0 && startdot[1] == 0 && enddot[0] == 0 && enddot[1] == 0){
//                            //System.out.println("Not found compare equation");
//                            waitingtime[i] = 0;
//                        }else if(startdot[1] == outputvalue && startdot[0] == outputidx-interval){
//                           // System.out.println("30sec error");
//                            waitingtime[i] = 0;
//                        }else if(startdot[1] > outputvalue && this.Cbxequation.isSelected()){ //if input equatoin doesn't match of inputvalue//input start dot is upper than outputvalue.
//                         //   System.out.print("debug : outputvalue = " + outputvalue
//                         //       + " outputidx = " + outputidx);
//                          //  System.out.print(" startdot = (" + startdot[0] + ","+startdot[1] +")"
//                          //          + "enddot = (" + enddot[0] + ","+enddot[1] +")");
//                          //  System.out.println("input equation error");
//                        }/*else if(i != 0 && (ci[i] == ci[i-1] && co[i] == co[i-1]))
//                            waitingtime[i] = 0;*/
//                        else{
//                            /*System.out.print(" startdot = (" + startdot[0] + ","+startdot[1] +")"
//                                    + "enddot = (" + enddot[0] + ","+enddot[1] +")");*/
//                            //calculate line equation
//                            //S0, E0 = x
//                            //S1, E1 = y
//
//
//                            double alpha = 0;
//                            double constant = 0;
//                            double ex = 0;
//                            double ey = 0;
//
//                            alpha = (enddot[1] - startdot[1]) / (enddot[0] - startdot[0]); // alpha = (E0 - S0) / (E1 - S1)
//                            constant = startdot[1] - (alpha * startdot[0]); // constant = y - ax
//
//                            //calculate simultaneous equations input equation and output equation
//                            //ex = (y-constant) / alpha
//                            //ey = (alpha * ex) + constant
//                            ex = alpha == 0 ? outputidx : (outputvalue - constant) / alpha;
//                            ey = (alpha * ex) + constant;
//
//                            waitingtime[i] = outputidx - ex;
//
//                            /*System.out.println(" ex(y=" + alpha + "x+"+constant +")"
//                                    + " result(" + ex + ","+ey +")"
//                                    + " waitingtime = output - ex("+waitingtime[i]+"="+outputidx+"-"+ex+")");*/
//                            //waitingtime[i] = 1;
//                        }
//                    //}
//                }
//                else
//                    waitingtime[i] = 0;
            }
    }
    
    /*
     * Calculate Minimum Rate
     */
    private void CalculateMinRate(double[] ci, double[] co, double[] minrate, int interval){
        int min4agoIdx = 2;//(8 / (interval / 30)) - 1;
        int MAX_RED_TIME = 13;
        
        for(int i=0;i<ci.length;i++){
            if(i>=7){
                minrate[i] = ci[i-min4agoIdx] - co[i];
                if(minrate[i] <= 0)
                    minrate[i] = 3600 / (MAX_RED_TIME + 2);
            }else
                minrate[i] = co[i];
            
        }
    }
    
    
    private void writeCuluativeSum(WritableSheet sheet, EntranceState es, int[] array, int interval, double[] input, double[] output, double[] green, String svalue,Period p,double[][] Gaps, double[][] CTime){
        
        Entrance entrance = (Entrance) es.getRNode();
        ArrayList<Detector> queue = entrance.getQueue();
        
        double[] waitingtime = null;
        double[] MinRate = null;
        double[] ci = null;
        double[] co = null;
        double[] gap = null;
        int colcount = 0;
        //row => 2
        array[1] = 2;
        try {
            /*System.out.println("input : " + es.getRNode().getLabel());
            sheet.addCell(new Label(array[0], 0, es.getRNode().getLabel() + "(" + (es.getMeter() != null ? es.getMeter().getId() : "Not Meter") + ")"));*/
            
            sheet.addCell(new Label(array[0]+colcount++, 1, "input("+svalue+")" + (entrance.getQueue().isEmpty() ? " (no-queue-detector)" : "")));
            sheet.addCell(new Label(array[0]+colcount++, 1, "output("+svalue+")"));
            
            if(this.cbxGreen.isSelected() && green != null)
                sheet.addCell(new Label(array[0]+colcount++, 1, "Green"));
            
            if(this.CbxWithWaitingTiming.isSelected()){
                sheet.addCell(new Label(array[0]+colcount++, 1, "Waiting Time(sec)"));
                sheet.addCell(new Label(array[0]+colcount++, 1, "Waiting Time(min)"));
            }
            
            if(this.IsMinRate)
                sheet.addCell(new Label(array[0]+colcount++, 1, "Min Rate"));
            
            //init cumulative array value
            if(output != null){
                if(this.CbxWithWaitingTiming.isSelected())
                    waitingtime= new double[output.length];
                if(IsMinRate)
                    MinRate = new double[output.length];
                
                ci = new double[input.length];
                co = new double[output.length];
                gap = new double[input.length];
            }
            
            /*
             * calculate cumulative input & output with queue size
             * double tempci = 0;
            int queuemaxgap = 0;
            if(queue.size() > 1){
                for(int k = 0; k< queue.size()-1; k++){
                    int gap = Math.abs(Integer.parseInt(queue.get(k).getId().split("D")[1]) - Integer.parseInt(queue.get(k+1).getId().split("D")[1]));
                    queuemaxgap = queuemaxgap < gap ? gap : queuemaxgap ;
                }
            }
            for (int i = 0; i < input.length; i++) {
                if(i != 0){
                    
                        if(queuemaxgap > 1)
                            tempci = input[i] / queue.size();
                        else
                            tempci = input[i];
                   
                    ci[i] = input[i] >= 0 ? tempci + ci[i-1] : ci[i-1];
                    co[i] = output[i] >= 0 ? output[i] + co[i-1] : co[i-1];
                }else{
                    ci[i] = input[i] >= 0 ? input[i] : 0;
                    co[i] = output[i] > 0 ? output[i] : 0;
                }
            }*/
            
            CalculateCumulative(input,output,ci,co,gap);
            if(this.CbxWithWaitingTiming.isSelected() && !entrance.getQueue().isEmpty())
                CalculateWaitingTime(ci,co,waitingtime,interval);
            if(IsMinRate && !entrance.getQueue().isEmpty())
                CalculateMinRate(ci,co,MinRate,interval);
            
            double waitavg = 0;
            
            for (int i = 0; i < input.length; i++) {
                colcount = 0;
                sheet.addCell(new Number(array[0] + colcount++, array[1], ci[i]));
                sheet.addCell(new Number(array[0] + colcount++, array[1], co[i]));
                if(this.cbxGreen.isSelected() && green != null){
                    sheet.addCell(new Number(array[0] + colcount++, array[1], green[i]));
                }
                if(this.CbxWithWaitingTiming.isSelected()){
                    sheet.addCell(new Number(array[0] + colcount++, array[1], waitingtime[i]));
                    sheet.addCell(new DateTime(array[0] + colcount++, array[1]++, new Date((long)waitingtime[i]*1000)));
                    waitavg += waitingtime[i];
                }else if(IsMinRate){
                    sheet.addCell(new Number(array[0] + colcount++, array[1]++, MinRate[i]));
                }else{
                    array[1]++;
                }
                
            }
            
            if(this.CbxWithWaitingTiming.isSelected()){
                sheet.addCell(new Number(array[0]+colcount-2, array[1], waitavg/waitingtime.length));
                sheet.addCell(new DateTime(array[0]+colcount-1, array[1]++, new Date((long)(waitavg/waitingtime.length)*1000)));
            }
            
            
            
            array[0] += colcount;
            
            
            /* Calculate Average Gap and Time
             * This was Make to Debuging Input & Output Gap.
             */
            if(svalue.equals("Vol")){
                int[] STime = {25200,54000};
                int[] ETime = {30600,64800};
                /*int AMSTime = 25200; //7:00
                int AMETime = 30600; //08:30
                int PMSTime = 54000; //15:00
                int PMETime = 64800; //18:00*/
                int GapTime = 1800; //30min
                
                
                int startsec = ((p.start_min*60) + (p.start_hour*3600));
                int endsec = ((p.end_min*60) + (p.end_hour*3600));
                
                //double[][] Gaps = new double[2][4];
                //double[] AMGap = new double[2];
                //double[] PMGap = new double[2];
                //double[][] CTime = new double[2][2];
                /*double AMTime = 0;
                double PMTIme = 0;*/
                
                boolean[] TCheck = {false,false};
                //boolean AMCheck = false;
                //boolean PMCheck = false;
                
                //System.out.println("startsec = " + startsec + " endsec = " + endsec + "AMSTIme = " + (STime[0]-GapTime) + " AMETime = " + (ETime[0] + GapTime));
                
                //Time Check
                for(int i = 0;i<STime.length;i++){
                    if(startsec < (STime[i] - GapTime) && endsec > (ETime[i] + GapTime)) //AM Metering Time is not in Time range
                        TCheck[i] = true;
                }
                
                //System.out.println("AC = " + TCheck[0] + " PC = " + TCheck[1]);
                
                for(int i = 0; i<STime.length;i++){
                    int startidx = 0;
                    int endidx = 0;
                    if(TCheck[0] == true){
                        startidx = (STime[i]-GapTime)/interval - 1;
                        endidx = (ETime[i]+GapTime)/interval;
                        STime[i] = STime[i]/interval - 1;
                        ETime[i] = ETime[i]/interval;
                        //System.out.println("AMSTimeidx = "+STime[i] +" AMETimeidx = "+ETime[i]);
                        //Calculate Time
                        int cnt = 0;
                        for(int k = STime[i];k<ETime[i];k++){
                            CTime[i][0] += waitingtime[k];
                            if(waitingtime[k] != 0)
                                cnt ++;
                        }
                        CTime[i][1] = CTime[i][0] == 0 ? 0 : CTime[i][0]/cnt;
                        
                        //System.out.println("AMSTimeidx = "+STime[i] +" AMETimeidx = "+ETime[i]+" Total AMTime = "+CTime[i][0]+" FORCNT = " + cnt + "AMCNT = " + (ETime[i]-STime[i]) + "AVG Time = " + (CTime[i][1]));
                        cnt = 0;
                        for(int k = startidx;k<=STime[i];k++){
                            Gaps[i][0] += gap[k];
                                cnt ++;
                        }
                        Gaps[i][1] = Gaps[i][0] == 0 ? 0 : Gaps[i][0]/cnt;
                        //System.out.println("startidx = "+startidx +" AMSTIme = "+STime[i]+" Total AMGap[0] = "+Gaps[i][0]+" FORCNT = " + cnt + "AMCNT = " + (STime[i]-startidx+1) + "AVG Time = " + (Gaps[i][1]));
                        cnt = 0;
                        for(int k = ETime[i]-1;k<endidx;k++){
                            Gaps[i][2] += gap[k];
                                cnt ++;
                        }
                        Gaps[i][3] = Gaps[i][2] == 0 ? 0 : Gaps[i][2]/cnt;
                        //System.out.println("eidx = "+endidx +"eTIme = "+ETime[i]+" Total AMGap[0] = "+Gaps[i][2]+" FORCNT = " + cnt + "AMCNT = " + (ETime[i]-endidx+1) + "AVG Time = " + (Gaps[i][3]));

                        //if(currentTime == )
                    }
                }                
            }
                
            /*double cci = 0;
            double cco = 0;
            for (int i = 0; i < input.length; i++) {

                if (input[i] >= 0 && output[i] >= 0) {
                    cci += input[i];
                    cco += output[i];
                }
                sheet.addCell(new Number(column, row, cci));
                sheet.addCell(new Number(column+1, row, cco));
                sheet.addCell(new Number(column+2, row++, waitingtime[i]));
            }*/
        }catch(Exception e){
            
        }
    }
    private int writeCumulativeSum(WritableSheet sheet, EntranceState es, int column, Period period) {
        
        Entrance entrance = (Entrance) es.getRNode();
        ArrayList<Detector> queue = entrance.getQueue();
        Detector passage = entrance.getPassage();
        Detector merge = entrance.getMerge();
        Detector bypass = entrance.getBypass();
        Detector green = entrance.getGreen();
        
        /*
         * Detector datas don't correct
         * Th 62 -> Th100 Entrance -> D5321(x), D3614(o)
         */
        double[] input = es.getRampDemandOld();//getRampDemandbyD3614();
        double[] output = es.getRampFlowNew();
        double[] vinput = null;
        double[] voutput = null;
        
        /*
         * Gap Averrage
         * Gaps[0] = AM
         * Gaps[1] = PM
         * Gaps[k][0] = Start Range(30min)
         * Gaps[k][1] = Start Range Cnt
         * Gaps[k][2] = End Range(30min)
         * Gaps[k][3] = End Range Cnt
         */
        double[][] Gaps = new double[2][4];
        /*
         * Time Average
         * CTime[0] = AM
         * CTime[1] = PM
         * CTime[k][0] = Time Value
         * CTime[k][1] = Time Cnt
         */
        double[][] CTime = new double[2][2];
        
        if (input == null || output == null) {
            System.err.println(es.getRNode() + "'s data is null");
            return column;
        }
        
        int row = 2;
        int[] array = {column,row};
        
        try {
            sheet.addCell(new Label(array[0], 0, es.getRNode().getLabel() + "("+ period.getPeriodString() +")" + "(" + (es.getMeter() != null ? es.getMeter().getId() : "No Meter") + ")"));
        }catch(Exception e){e.printStackTrace();}
        
        if(this.cbxvol.isSelected()){
            vinput = new double[input.length];
            voutput = new double[output.length];
            Calculatevolume(input,output,vinput,voutput,period.interval);
            writeCuluativeSum(sheet,es,array,period.interval,vinput,voutput,green == null ? null : green.getVolume(),"Vol",period,Gaps,CTime);
        }
        if(this.cbxcumul.isSelected())
            writeCuluativeSum(sheet,es,array,period.interval,input,output,green == null ? null : green.getFlow(),"Flow",period,Gaps,CTime);
        
        try {
            sheet.addCell(new Label(array[0]-1, 0, es.getRNode().getLabel() + "("+ period.getPeriodString() +")" + "(" + (es.getMeter() != null ? es.getMeter().getId() : "Not Meter") + ")"));
        }catch(Exception e){e.printStackTrace();}
        //write labels
        
        try{  
            StringBuilder q = new StringBuilder();
            for (Detector d : queue) {
                q.append(d.getId() + ", ");
            }
            row = array[1];
            row++;
            
            
            sheet.addCell(new Label(column, row, "Queue"));
            sheet.addCell(new Label(column+1, row++, queue.isEmpty() ? "-" : q.toString()));
            
            
            sheet.addCell(new Label(column, row, "Passage"));
            sheet.addCell(new Label(column+1, row++, (passage == null ? "-" : passage.getId())));            
            
            sheet.addCell(new Label(column, row, "Merge"));
            sheet.addCell(new Label(column+1, row++, (merge == null ? "-" : merge.getId())));     
            
            sheet.addCell(new Label(column, row, "Green"));
            sheet.addCell(new Label(column+1, row++, (green == null ? "-" : green.getId())));              
            
            sheet.addCell(new Label(column, row, "ByPass"));
            sheet.addCell(new Label(column+1, row++, (bypass == null ? "-" : bypass.getId())));               
            
            
            sheet.addCell(new Label(column, row, es.getRNode().getLabel()));
            sheet.addCell(new Label(column+1, row, (es.getMeter() != null ? es.getMeter().getId() : "No Meter")));
            sheet.addCell(new Label(column+2, row++, (es.getMeter() != null ? "1" : "0")));
            sheet.addCell(new Label(column, row, "AM Start Gap"));
            sheet.addCell(new Number(column+1, row++,(Gaps[0][1])));
            sheet.addCell(new Label(column, row, "AM End Gap"));
            sheet.addCell(new Number(column+1, row++,(Gaps[0][3])));
            sheet.addCell(new Label(column, row, "PM Start Gap"));
            sheet.addCell(new Number(column+1, row++,(Gaps[1][1])));
            sheet.addCell(new Label(column, row, "PM End Gap"));
            sheet.addCell(new Number(column+1, row++,(Gaps[1][3])));
            sheet.addCell(new Label(column, row, "AM TimeAvg"));
            sheet.addCell(new Number(column+1, row++,(CTime[0][1])));
            sheet.addCell(new Label(column, row, "PM TimeAvg"));
            sheet.addCell(new Number(column+1, row++,(CTime[1][1])));
            
            
            
        } catch (Exception ex) {
        }
        //System.out.println(array[0]);
        return array[0]+2;
    }

    private void readData() throws Exception {

        // detector checker
        IDetectorChecker dc = new IDetectorChecker() {

            @Override
            public boolean check(Detector d) {
                if (d.isAbandoned()) {
                    return false;
                }
                return true;
            }
        };

        //If Smooting
        if(this.IsSmoothing)
            this.smoothingTimeIdx = Integer.parseInt(this.txtSmoothingTimes.getText());
        
        // period
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        Calendar c1, c2;
        
        if(selectedDates.length == 0){
            JOptionPane.showMessageDialog(this, "Select Calendar");
            ButtonCheck();
            return;
        }
            
        redirectOutput(); //text redirect

        Vector<Period> periods = new Vector<Period>();
        int interval = ((Interval) this.cbxInterval.getSelectedItem()).second;
        int start_hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        int start_min = Integer.parseInt(this.cbxStartMin.getSelectedItem().toString());
        int end_hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        int end_min = Integer.parseInt(this.cbxEndMin.getSelectedItem().toString());


        for (Calendar date : selectedDates) {
            c1 = (Calendar) date.clone();
            c2 = (Calendar) date.clone();

            c1.set(Calendar.HOUR, start_hour);
            c1.set(Calendar.MINUTE, start_min);
            c1.set(Calendar.SECOND, 0);

            c2.set(Calendar.HOUR, end_hour);
            c2.set(Calendar.MINUTE, end_min);
            c2.set(Calendar.SECOND, 0);


            if (this.cbxDuration.getSelectedIndex() > 0) {
                c2.set(Calendar.HOUR, start_hour);
                c2.set(Calendar.MINUTE, start_min);
                c2.add(Calendar.HOUR, (Integer) this.cbxDuration.getSelectedItem());
            } else {
                c2.set(Calendar.HOUR, end_hour);
                c2.set(Calendar.MINUTE, end_min);
            }

            periods.add(new Period(c1.getTime(), c2.getTime(), interval));
        }

        if(this.cbxSections.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Select Route");
            ButtonCheck();
            return;
        }
        Section section = (Section) this.cbxSections.getSelectedItem();
        readData(section, periods, dc);
        
        
        restoreOutput();
        
    }

    /**
     * Change times status according to selected duration
     */
    private void selectDuration() {
        if (this.cbxDuration.getSelectedIndex() == 0) {
            this.cbxEndHour.setEnabled(true);
            this.cbxEndMin.setEnabled(true);
        } else {
            this.cbxEndHour.setEnabled(false);
            this.cbxEndMin.setEnabled(false);
        }
    }

    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        if (sm == null) {
            return;
        }
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());
        this.cbxSections.removeAllItems();
        this.cbxSections.addItem("Select the route");
        for (Section s : this.sections) {
            this.cbxSections.addItem(s);
        }
    }
    
    void ButtonCheck(){
        if(!buttoncheck){
            btnRead.setText("Calculating...");
            btnRead.setEnabled(false);
            buttoncheck = true;
        }else{
            btnRead.setText("Read");
            btnRead.setEnabled(true);   
            buttoncheck = false;
        }
            
    }
    
    /**
     * Redirect output into log box
     */
    public void redirectOutput() {
        backupOut = System.out;
        backupErr = System.err;
        // redirect System.out and System.err to log textbox
        StringOutputStream sos = new StringOutputStream(this.textarea);
        System.setOut(new PrintStream(sos));
        System.setErr(new PrintStream(sos));
    }

    /**
     * Resotre output
     */
    public void restoreOutput() {
        System.setOut(backupOut);
        System.setErr(backupErr);
    }
    
    /**
     * String Output Stream class for output redirection
     */
    public class StringOutputStream extends OutputStream {
        JTextArea logText;

        public StringOutputStream(JTextArea logText) {
            this.logText = logText;
        }
        
        @Override
        public void write(int b) throws IOException {
            updateLog(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            updateLog(new String(b, off, len));
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        private synchronized void updateLog(final String text) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    logText.append(text);
                }
            });
        }
    }
    private void addData(WritableSheet sheet, int column, String[] labels, String[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Label(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Class : Limited Queue
     * @param <T> 
     */
    class LimitedQueue<T> {

        /** Storage limit */
        int limit;
        
        /** Linked list to store data */
        Queue<T> queue = new LinkedList<T>();

        /**
         * Construct
         * @param limit storage limit
         */
        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        /**
         * Add data
         * @param obj
         * @return 
         */
        public boolean push(T obj) {
            boolean res = this.queue.offer(obj);
            if (this.queue.size() > this.limit) {
                this.queue.poll();
            }
            return res;
        }

        /**
         * Return head data
         * @return T data
         */
        public T head() {
            return this.queue.poll();
        }

        /**
         * Return tail data
         * @return T data
         */
        public T tail() {
            return this.get(0);
        }

        /**
         * Return data at given index (in reversed direction)
         *   e.g. get(0) : most recent data
         * @return T data
         */
        public T get(int index) {

            int idx = this.queue.size() - 1;
            for (T obj : this.queue) {
                if (index == idx) {
                    return obj;
                }
                idx--;
            }
            return null;
        }

        /**
         * Clear storage
         */
        public void clear() {
            this.queue.clear();
        }

        /**
         * Return current storage size
         * @return queue size
         */
        public int size() {
            return this.queue.size();
        }

        /**
         * Return average data
         * @param fromIndex start index
         * @param length length to calculate average
         */
        public Double getAverage(int fromIndex, int length) {
            Double sum = 0D;
            int count = 0;
            for (int i = fromIndex; i < fromIndex + length; i++) {
                Double d = (Double) this.get(i);
                if (d == null) {
                    break;
                }
                sum += d;
                count++;
            }
            if (count > 0) {
                return sum / count;
            } else {
                return 0D;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnRead = new javax.swing.JButton();
        Cbxequation = new javax.swing.JCheckBox();
        CbxWithWaitingTiming = new javax.swing.JCheckBox();
        cbxcumul = new javax.swing.JCheckBox();
        cbxvol = new javax.swing.JCheckBox();
        cbxDuration = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        cbxEndHour = new javax.swing.JComboBox();
        cbxEndMin = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        cbxStartMin = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        cbxStartHour = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        cbxInterval = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jLabel5 = new javax.swing.JLabel();
        cbxSections = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cbxSmoothing = new javax.swing.JCheckBox();
        cbxMinRate = new javax.swing.JCheckBox();
        txtSmoothingTimes = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cbxGreen = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textarea = new javax.swing.JTextArea();

        btnRead.setFont(new java.awt.Font("Verdana", 0, 12));
        btnRead.setText("Read");
        btnRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadActionPerformed(evt);
            }
        });

        Cbxequation.setText("equation error check");
        Cbxequation.setEnabled(false);

        CbxWithWaitingTiming.setSelected(true);
        CbxWithWaitingTiming.setText("With Waiting Time");
        CbxWithWaitingTiming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CbxWithWaitingTimingActionPerformed(evt);
            }
        });

        cbxcumul.setSelected(true);
        cbxcumul.setText("Flow data");

        cbxvol.setSelected(true);
        cbxvol.setText("Volume data");

        cbxDuration.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDurationActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel9.setText("hour");

        jLabel7.setText("or");

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel8.setText("for");

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel20.setText("End Time");

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setSelectedIndex(9);
        cbxEndHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEndHourActionPerformed(evt);
            }
        });

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel15.setText(":");

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel14.setText(":");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.setSelectedIndex(6);
        cbxStartHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStartHourActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel18.setText("Start Time");

        cbxInterval.setFont(new java.awt.Font("Verdana", 0, 12));

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel17.setText("Time Interval");

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel5.setText("Dates");

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 11));
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel1.setText("Route");

        cbxSmoothing.setText("Smoothing");
        cbxSmoothing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSmoothingActionPerformed(evt);
            }
        });

        cbxMinRate.setText("With Minimum Rate");
        cbxMinRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxMinRateActionPerformed(evt);
            }
        });

        txtSmoothingTimes.setText("3");
        txtSmoothingTimes.setEnabled(false);
        txtSmoothingTimes.setPreferredSize(new java.awt.Dimension(50, 21));
        txtSmoothingTimes.setRequestFocusEnabled(false);

        jLabel10.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel10.setText("Times");

        cbxGreen.setText("With Green Detector");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5)
                    .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel20))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbxEndHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxStartMin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel9))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel17)
                            .addGap(18, 18, 18)
                            .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(29, Short.MAX_VALUE))
            .addComponent(btnRead, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxvol)
                    .addComponent(cbxcumul))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CbxWithWaitingTiming)
                    .addComponent(cbxMinRate))
                .addContainerGap(65, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(cbxSmoothing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSmoothingTimes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(89, 89, 89))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(Cbxequation)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(cbxGreen)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxvol)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxcumul)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxSmoothing)
                            .addComponent(txtSmoothingTimes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(CbxWithWaitingTiming)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxMinRate)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxGreen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addComponent(Cbxequation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRead, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane1.setViewportView(textarea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10, 10, 10))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadActionPerformed
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ButtonCheck();
                    readData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 10);
        
    }//GEN-LAST:event_btnReadActionPerformed

    private void cbxStartHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxStartHourActionPerformed
        int slt1 = this.cbxStartHour.getSelectedIndex();
        int slt2 = this.cbxEndHour.getSelectedIndex();
        if (slt1 > slt2) {
            this.cbxEndHour.setSelectedIndex(slt1);
        }
}//GEN-LAST:event_cbxStartHourActionPerformed

    private void cbxEndHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxEndHourActionPerformed
        int slt1 = this.cbxStartHour.getSelectedIndex();
        int slt2 = this.cbxEndHour.getSelectedIndex();
        if (slt1 > slt2) {
            this.cbxStartHour.setSelectedIndex(slt2);
        }
}//GEN-LAST:event_cbxEndHourActionPerformed

    private void cbxDurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDurationActionPerformed
        selectDuration();
}//GEN-LAST:event_cbxDurationActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
}//GEN-LAST:event_cbxSectionsActionPerformed

private void CbxWithWaitingTimingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CbxWithWaitingTimingActionPerformed
// TODO add your handling code here:
    /*if(this.CbxWithWaitingTiming.isSelected())
        this.Cbxequation.setSelected(true);
    else
        this.Cbxequation.setSelected(false);*/
}//GEN-LAST:event_CbxWithWaitingTimingActionPerformed

private void cbxSmoothingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSmoothingActionPerformed
// TODO add your handling code here:
    System.out.println("CheckSmoothing = " + this.cbxSmoothing.isSelected() + "=" + IsSmoothing);
    IsSmoothing = this.cbxSmoothing.isSelected();
    if(IsSmoothing)
        this.txtSmoothingTimes.setEnabled(true);
    else
        this.txtSmoothingTimes.setEnabled(false);
}//GEN-LAST:event_cbxSmoothingActionPerformed

private void cbxMinRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxMinRateActionPerformed
// TODO add your handling code here:
    System.out.println("CheckMinimum Rate = " + this.cbxMinRate.isSelected() + "=" + IsMinRate);
    IsMinRate = this.cbxMinRate.isSelected();
}//GEN-LAST:event_cbxMinRateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CbxWithWaitingTiming;
    private javax.swing.JCheckBox Cbxequation;
    private javax.swing.JButton btnRead;
    private javax.swing.JComboBox cbxDuration;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JCheckBox cbxGreen;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JCheckBox cbxMinRate;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JCheckBox cbxSmoothing;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JCheckBox cbxcumul;
    private javax.swing.JCheckBox cbxvol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JTextArea textarea;
    private javax.swing.JTextField txtSmoothingTimes;
    // End of variables declaration//GEN-END:variables
}
