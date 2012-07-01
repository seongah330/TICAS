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

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.plugin.srte.SRTEProcess.SMOOTHING;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEAlgorithm extends Thread{
    
    private boolean isSave = true;
    private Section section;
    private Period period;
    private SRTEConfig config;
    private TimeEventLists eventlist;
    
    private int TimeInterval = Interval.I15MIN.second;
    private Vector<SRTEResultSection> rData = new Vector<SRTEResultSection>();
    public static interface AlogorithmEndListener{
        public void onEndMessage(boolean msg);
    }
    
    private AlogorithmEndListener endListener;
    public SRTEAlgorithm(){;}
    public SRTEAlgorithm(boolean isSave){
        this();
        this.isSave = isSave;
    }
    @Override
    public void run()
    {
        String projectName = null;

        if(eventlist == null){
            if(endListener != null){
                endListener.onEndMessage(true);
            }
            return;
        }
        
        for(TimeEvent te : eventlist.getTimeEvents()){
            System.out.println("\n-------------------------------------------");
            System.out.println("set Event : " + te.toString());
            System.out.println("-------------------------------------------");
            Calendar cs,ce;
            cs = te.getStartTime();
            ce = te.getEndTime();
            Period p = new Period(cs.getTime(),ce.getTime(),TimeInterval);
            Section s = te.getSection();
            this.setSection(s, p);
            SRTEResultSection result = process(s,p,config,te);
            rData.add(result);
        }
        projectName = eventlist.getName();
        
        // print out result
        if(this.isSave)
            presentResult(rData,projectName);

        System.out.println("SRTE Algorithm for Station has been done!!");
        
        if(endListener != null){
            endListener.onEndMessage(true);
        }
    }
    
    private SRTEResultSection process(Section sec, Period p, SRTEConfig config, TimeEvent te) {
        Station[] stations = sec.getStations();                
        //read data
        System.out.print("Loading Data....."+te.getSectionName()+".............");
        long st = new Date().getTime();
        sec.loadData(p);
        long et = new Date().getTime();
        System.out.println(" (OK : " + (et-st) + "ms)");
        
        SRTEResult[] result = new SRTEResult[stations.length];
        for(int i=0;i<stations.length;i++){
            System.out.println("\nCalculate Station..."+stations[i].getLabel()+"("+stations[i].getStationId()+")");
            SRTEProcess proc = new SRTEProcess(sec, p,stations[i],config,te);
            result[i] = proc.stateDecision();
            
            System.out.println("End Station..."+stations[i].getLabel()+"("+stations[i].getStationId()+")");
        }
        SRTEResultSection rSection = new SRTEResultSection(result,te,p);
        return rSection;
    }

    public void extractData() throws Exception
    {
        section.loadData(this.period,false);
        saveData();
    }

    /**
     * adjust period
     *  - Starting period is 2 hours earlier than the snow starting time
     *  - Ending period is 6 hours later than the snow ending time  
     * @param section
     * @param period
     */
    private void setSection(Section section, Period period) {
        this.section = section;
        this.period = period;
        this.period.addStartHour(-1* SRTEConfig.DATA_READ_EXTENSION_BEFORE_SNOW);
        this.period.addEndHour(SRTEConfig.DATA_READ_EXTENSION_AFTER_SNOW);
        this.period.syncInterval();
    }
    
    public void setSRTEMODE(TimeEventLists el){
        eventlist = el;
        
        this.TimeInterval = SRTEConfig.TimeInterval;
        System.out.println("set Time Interval : " +TimeInterval+"("+(TimeInterval/60)+"min)");
        System.out.println("set Smoothing Option : "+SMOOTHING.getSmooth(SRTEConfig.isSmoothing).toString());
    }
    public Vector<SRTEResultSection> getResults(){
        return this.rData;
    }
    
    public void setEndListener(AlogorithmEndListener listener){
        this.endListener = listener;
    }
    
    private void presentEachDataResult(Vector<SRTEResultSection> rData, String name) {
        String fname = name;
        String sectionName=null;
        if(fname == null)
            fname = "";
        else
            fname = fname+"-";
        try{
            for(SRTEResultSection result : rData){
                sectionName = result.getSectionName();
                String workbookFile = getFileName("SRTEResult ("+fname+sectionName+"-"+result.getPeriodtoString()+")", "xls");
                System.out.println(workbookFile);
                WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));
                WritableSheet sheet = workbook.createSheet("Summary", 0);
                getSummaryResult(result,sheet,0);
                getDataResult(result.getResultData(),workbook,sheet);
                workbook.write();
                workbook.close();
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void presentResult(Vector<SRTEResultSection> rData,String name){
        int sheet_count = 0;  
        int idx = 0;
        try{
            String workbookFile = getFileName("SRTEResult ("+name+")", "xls");
            WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));
            WritableSheet sheet = workbook.createSheet("Summary", sheet_count++);
            for(SRTEResultSection result : rData){
                idx = getSummaryResult(result,sheet,idx);
            }
            workbook.write();
            workbook.close();
            
            presentEachDataResult(rData,name);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private int getSummaryResult(SRTEResultSection res, WritableSheet sheet,int idx){
        SRTEResult[] result = null;
        try {
            int colIdx = 0;
            int[] speedKeylist = {45,50,55,60,65};
            // summary sheet //////////////////////////////////////////////////////
            sheet.addCell(new Label(colIdx++, idx, res.getSectionName()));
            colIdx+=2;
            sheet.addCell(new Label(colIdx++, idx, "Start Time"));
            sheet.addCell(new Label(colIdx++, idx, res.getStartTimetoString()));
            colIdx+=3;
            sheet.addCell(new Label(colIdx++, idx, "End Time"));
            sheet.addCell(new Label(colIdx++, idx, res.getEndTimetoString()));
            colIdx+=3;
            sheet.addCell(new Label(colIdx++, idx, "BareLane Time"));
            sheet.addCell(new Label(colIdx++, idx, res.getBareLaneTimetoString()));
            
            result = res.getResultData();
            /**
            * Time result
            */
            idx++;
            colIdx = 1;
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "SRST"));
//            sheet.addCell(new Label(colIdx++, 0, "U(SRST)"));                
            sheet.addCell(new Label(colIdx++, idx, "LST"));
//            sheet.addCell(new Label(colIdx++, 0, "U(LST)"));                
            sheet.addCell(new Label(colIdx++, idx, "RST"));
//            sheet.addCell(new Label(colIdx++, 0, "U(RST)"));                
            sheet.addCell(new Label(colIdx++, idx, "RCR"));
            sheet.addCell(new Label(colIdx++, idx, "SRT1"));
            sheet.addCell(new Label(colIdx++, idx, "SRT2"));
            sheet.addCell(new Label(colIdx++, idx, "Qmax"));
            sheet.addCell(new Label(colIdx++, idx, "Kmax"));
            sheet.addCell(new Label(colIdx++, idx, "Umax"));
            sheet.addCell(new Label(colIdx++, idx, "Umin"));
            for(int key : speedKeylist){
                sheet.addCell(new Number(colIdx++, idx, key));
            }
//            sheet.addCell(new Label(colIdx++, 0, "SST"));
            
            /**
            * Point result
            */
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "TYPE"));
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "SRST"));
            sheet.addCell(new Label(colIdx++, idx, "LST"));
            sheet.addCell(new Label(colIdx++, idx, "RST"));
            sheet.addCell(new Label(colIdx++, idx, "RCR"));
            sheet.addCell(new Label(colIdx++, idx, "SRT1"));
            sheet.addCell(new Label(colIdx++, idx, "SRT2"));
            sheet.addCell(new Label(colIdx++, idx, "Qmax"));
            sheet.addCell(new Label(colIdx++, idx, "Kmax"));
            sheet.addCell(new Label(colIdx++, idx, "Umax"));
            sheet.addCell(new Label(colIdx++, idx, "Ummin"));
            for(int key : speedKeylist){
                sheet.addCell(new Number(colIdx++, idx, key));
            }
            /**
             * each point data
             */
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "SRST(T)"));
            sheet.addCell(new Label(colIdx++, idx, "SRST"));
            sheet.addCell(new Label(colIdx++, idx, "Q(SRST)"));
            sheet.addCell(new Label(colIdx++, idx, "K(SRST)"));
            sheet.addCell(new Label(colIdx++, idx, "U(SRST)"));
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "LST(T)"));
            sheet.addCell(new Label(colIdx++, idx, "LST"));
            sheet.addCell(new Label(colIdx++, idx, "Q(LST)"));
            sheet.addCell(new Label(colIdx++, idx, "K(LST)"));
            sheet.addCell(new Label(colIdx++, idx, "U(LST)"));
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "RST(T)"));
            sheet.addCell(new Label(colIdx++, idx, "RST"));
            sheet.addCell(new Label(colIdx++, idx, "Q(RST)"));
            sheet.addCell(new Label(colIdx++, idx, "K(RST)"));
            sheet.addCell(new Label(colIdx++, idx, "U(RST)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "RCR(T)"));
            sheet.addCell(new Label(colIdx++, idx, "RCR"));
            sheet.addCell(new Label(colIdx++, idx, "Q(RCR)"));
            sheet.addCell(new Label(colIdx++, idx, "K(RCR)"));
            sheet.addCell(new Label(colIdx++, idx, "U(RCR)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "SST(T)"));
            sheet.addCell(new Label(colIdx++, idx, "SST"));
            sheet.addCell(new Label(colIdx++, idx, "Q(SST)"));
            sheet.addCell(new Label(colIdx++, idx, "K(SST)"));
            sheet.addCell(new Label(colIdx++, idx, "U(SST)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "SST2(T)"));
            sheet.addCell(new Label(colIdx++, idx, "SST2"));
            sheet.addCell(new Label(colIdx++, idx, "Q(SST2)"));
            sheet.addCell(new Label(colIdx++, idx, "K(SST2)"));
            sheet.addCell(new Label(colIdx++, idx, "U(SST2)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "QMAX(T)"));
            sheet.addCell(new Label(colIdx++, idx, "QMAX"));
            sheet.addCell(new Label(colIdx++, idx, "Q(QMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "K(QMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "U(QMAX)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "KMAX(T)"));
            sheet.addCell(new Label(colIdx++, idx, "KMAX"));
            sheet.addCell(new Label(colIdx++, idx, "Q(KMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "K(KMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "U(KMAX)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "UMAX(T)"));
            sheet.addCell(new Label(colIdx++, idx, "UMAX"));
            sheet.addCell(new Label(colIdx++, idx, "Q(UMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "K(UMAX)"));
            sheet.addCell(new Label(colIdx++, idx, "U(UMAX)"));
            
            colIdx += 1;
            sheet.addCell(new Label(colIdx++, idx, "UMIN(T)"));
            sheet.addCell(new Label(colIdx++, idx, "UMIN"));
            sheet.addCell(new Label(colIdx++, idx, "Q(UMIN)"));
            sheet.addCell(new Label(colIdx++, idx, "K(UMIN)"));
            sheet.addCell(new Label(colIdx++, idx, "U(UMIN)"));
            
            idx++;
            for(int i=0;i<result.length;i++){
                colIdx = 0;
                int rows = i;
                
                sheet.addCell(new Label(colIdx++, idx+rows, result[i].station.getLabel()+"("+result[i].station.getStationId()+")"));
                colIdx += 1;
                
                /**
                 * Time result
                 */
                Period p = result[i].period;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srst))); //srst
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,getPoint(result[i].getcurrentPoint().lst)))); //lst
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().rst))); //rst
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.getRecoveryPoint()))); //rxr
                double sst = 0;
                if(result[i].getcurrentPoint().srt.size() < 2){
                    sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srt.get(0)))); //srt1
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));//srt2
                }
                else{
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));//srt2
                    sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srt.get(1))));//srt2
                }
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.qTrafficData.getMaxPoint()))); //qMaxData
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.kTrafficData.getMaxPoint()))); //kMaxData
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.uTrafficData.getMaxPoint()))); //uMaxData
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.uTrafficData.getMinPoint()))); //uMaxData
                for(int key : speedKeylist){
                    SRTEResult.SpeedMap smap = result[i].getSpeedList().get(key);
                    if(smap != null)
                        sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,smap.getKeyTimeMin(),true))); //uMaxData
                    else
                        sheet.addCell(new Label(colIdx++, idx+rows, "")); //uMaxData
                }

                /**
                 * Type
                 */
                colIdx += 1;
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.getTypeNumber()));
                
                /**
                 * Point result
                 */
                colIdx += 1;
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srst));
                sheet.addCell(new Number(colIdx++, idx+rows, getPoint(result[i].getcurrentPoint().lst)));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().rst));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.getRecoveryPoint()));
                if(result[i].getcurrentPoint().srt.size() < 2){
//                    sheet.addCell(new Number(colIdx++, idx+rows, 0));
                    sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srt.get(0)));
                    sheet.addCell(new Number(colIdx++, idx+rows, 0));
                }
                else{
                    sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srt.get(1)));
                    sheet.addCell(new Number(colIdx++, idx+rows, 0));
                }
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.qTrafficData.getMaxPoint())); //qMaxData
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.kTrafficData.getMaxPoint())); //kMaxData
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.uTrafficData.getMaxPoint())); //uMaxData
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.uTrafficData.getMinPoint())); //uminData
                for(int key : speedKeylist){
                    SRTEResult.SpeedMap smap = result[i].getSpeedList().get(key);
                    if(smap != null)
                        sheet.addCell(new Number(colIdx++, idx+rows, smap.getKeyTimeStep())); //uMaxData
                    else
                        sheet.addCell(new Label(colIdx++, idx+rows, "")); //uMaxData
                }
                
                /**
                 * each point data
                 */
                //srst
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srst)));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srst));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].getcurrentPoint().srst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].getcurrentPoint().srst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].getcurrentPoint().srst)));
                //lst
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,getPoint(result[i].getcurrentPoint().lst))));
                sheet.addCell(new Number(colIdx++, idx+rows, getPoint(result[i].getcurrentPoint().lst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].getcurrentPoint().lst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].getcurrentPoint().lst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].getcurrentPoint().lst)));
                //rst
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().rst)));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().rst));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].getcurrentPoint().rst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].getcurrentPoint().rst)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].getcurrentPoint().rst)));
                //RCR
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.getRecoveryPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.getRecoveryPoint()));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].pType.getRecoveryPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].pType.getRecoveryPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].pType.getRecoveryPoint())));
                
                //SRT1
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srt.get(0))));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srt.get(0)));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].getcurrentPoint().srt.get(0))));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].getcurrentPoint().srt.get(0))));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].getcurrentPoint().srt.get(0))));
//                colIdx += 1;
//                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
//                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
//                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
//                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
//                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                
                if(result[i].getcurrentPoint().srt.size() > 1){
                    colIdx += 1;
                    sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].getcurrentPoint().srt.get(1))));
                    sheet.addCell(new Number(colIdx++, idx+rows, result[i].getcurrentPoint().srt.get(1)));
                    sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].getcurrentPoint().srt.get(1))));
                    sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].getcurrentPoint().srt.get(1))));
                    sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].getcurrentPoint().srt.get(1))));
                }else{
                    colIdx += 1;
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                    sheet.addCell(new Label(colIdx++, idx+rows, "0"));
                }
                
                //Qmax
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.qTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.qTrafficData.getMaxPoint()));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].pType.qTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].pType.qTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].pType.qTrafficData.getMaxPoint())));
                
                //Kmax
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.kTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.kTrafficData.getMaxPoint()));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].pType.kTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].pType.kTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].pType.kTrafficData.getMaxPoint())));
                
                //Umax
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.uTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.uTrafficData.getMaxPoint()));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].pType.uTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].pType.uTrafficData.getMaxPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].pType.uTrafficData.getMaxPoint())));
                
                //Umin
                colIdx += 1;
                sheet.addCell(new Label(colIdx++, idx+rows, getTime(p,result[i].pType.uTrafficData.getMinPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, result[i].pType.uTrafficData.getMinPoint()));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].q_smoothed,result[i].pType.uTrafficData.getMinPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].k_smoothed,result[i].pType.uTrafficData.getMinPoint())));
                sheet.addCell(new Number(colIdx++, idx+rows, getValue(result[i].u_Avg_smoothed,result[i].pType.uTrafficData.getMinPoint())));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return idx + result.length + 2;
    }
    private void getDataResult(SRTEResult[] result, WritableWorkbook workbook, WritableSheet sheet) {
        try {
            int sheet_count = 1;                   
            int idx = 0;
            int colIdx = 0;
            
            // summary sheet //////////////////////////////////////////////////////
            for(int i=0;i<result.length;i++){
                // data sheet //////////////////////////////////////////////////////
                colIdx = 0;
                sheet = workbook.createSheet(result[i].station.getLabel()+"("+result[i].station.getStationId()+")", sheet_count++);
                
                addData(sheet, colIdx++, "Times", result[i].period.getTimelineJustTime());
                
                colIdx++;
                addData(sheet, colIdx++, "Q", result[i].q_origin);
                addData(sheet, colIdx++, "SQ", result[i].q_smoothed);
                addData(sheet, colIdx++, "QQ", result[i].q_quant);
                
                colIdx++;
                addData(sheet, colIdx++, "K", result[i].k_origin);
                addData(sheet, colIdx++, "SK", result[i].k_smoothed);
                addData(sheet, colIdx++, "QK", result[i].k_quant);
//                
                colIdx++;
                addData(sheet, colIdx++, "U", result[i].data_origin);
                addData(sheet, colIdx++, "SU", result[i].data_smoothed);
                addData(sheet, colIdx++, "QU", result[i].data_quant);
//                
                colIdx++;
                addData(sheet, colIdx++, "U(Avg)", result[i].u_Avg_origin);
                addData(sheet, colIdx++, "SU(Avg)", result[i].u_Avg_smoothed);
                addData(sheet, colIdx++, "QU(Avg)", result[i].u_Avg_quant);
//
//                colIdx++;colIdx++;

//                Station[] stations = this.section.getStations();
//                addData(sheet, colIdx++, "Avg", result[i].data_origin);
//                for(int z=0;z<stations.length; z++)
//                {
//                    addData(sheet, colIdx++, "U-"+stations[i].toString(), stations[i].getSpeed());
//                    addData(sheet, colIdx++, "U(A)-"+stations[i].toString(), stations[i].getAverageLaneFlow(),stations[i].getDensity());
//                    addData(sheet, colIdx++, "K-"+stations[i].toString(), stations[i].getDensity());
//                    addData(sheet, colIdx++, "Q-"+stations[i].toString(), stations[i].getAverageLaneFlow());
//                    addData(sheet, colIdx++, "O-"+stations[i].toString(), stations[i].getOccupancy());
//                }
            }
                        
            

            // open result excel file
            //Desktop.getDesktop().open(new File(workbookFile));



        } catch (Exception ex) {
            Logger.getLogger(SRTEAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
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
//            sheet.addCell(new Label(colIdx++, idx+1, result.sectionName));
//            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.srst)));
//            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.srst]));
//            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.lst)));
//            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.lst]));
//            sheet.addCell(new Label(colIdx++, idx+1, getTime(result.rst)));
//            sheet.addCell(new Number(colIdx++, idx+1, result.data_smoothed[result.rst]));
//            if(result.srt.size() > 0) {
//                for(int i=0; i<result.srt.size(); i++) {
//                    sheet.addCell(new Label(colIdx++, idx+1+i, getTime(result.srt.get(i))));
//                    sheet.addCell(new Number(colIdx--, idx+1+i, result.data_smoothed[result.srt.get(i)]));
//                }
//                colIdx++;
//            } else {
//                sheet.addCell(new Label(colIdx++, idx+1, ""));
//                sheet.addCell(new Label(colIdx++, idx+1, ""));                    
//            }

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
                addData(sheet, colIdx++, "U(A)-"+stations[i].toString(), stations[i].getAverageLaneFlow(),stations[i].getDensity());
                addData(sheet, colIdx++, "K-"+stations[i].toString(), stations[i].getDensity());
                addData(sheet, colIdx++, "Q-"+stations[i].toString(), stations[i].getAverageLaneFlow());
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

    /*
     * addData for Average U
     */
    private void addData(WritableSheet sheet, int column, String label, double[] data, double[] data2)
    {
        try {
            sheet.addCell(new Label(column, 0, label));
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, r+1, data2[r] == 0 ? 0 : data[r]/data2[r]));
            }
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
    
    private String getTime(Period p, int count, boolean isMin){
        int tgap = 0;
        if(isMin){
            tgap = 1;
            count = count -1;
        }
        else
            tgap = TimeInterval/60;
        
        Calendar c = Calendar.getInstance();
        
        c.set(p.start_year, p.start_month-1, p.start_date, p.start_hour, p.start_min);
        for(int i=0; i<=count; i++) c.add(Calendar.MINUTE, tgap);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        
        return String.format("%02d:%02d", hour, min);
    }
    private String getTime(Period p,int count)
    {
        return getTime(p,count,false);
    }
    
    private String getFileName(String name, String ext) {
        String[] filter_word = {"\\\"","\\/","\\\\","\\:","\\*","\\?","\\<","\\>","\\|"};
        for(int i=0;i<filter_word.length;i++){
            name = name.replaceAll(filter_word[i], "-");
        }
        
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

    private int getPoint(int p) {
        return p < 0 ? 0 : p;
    }

    private double getValue(double[] value, int p) {
        if(p < 0)
            return -1;
        else{
            return value[p];
        }
    }

    
}
