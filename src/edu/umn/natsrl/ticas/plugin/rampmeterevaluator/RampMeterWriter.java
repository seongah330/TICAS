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
package edu.umn.natsrl.ticas.plugin.rampmeterevaluator;

import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.util.FileHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.DateTime;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterWriter {
    RampMeterEvaluatorMode mode;
    TrafficType ttype;
    RampMeterWriter(RampMeterEvaluatorMode m){
        mode = m;
        if(mode.isFlow())
            ttype = TrafficType.FLOW;
        else if(mode.isVolume())
            ttype = TrafficType.VOLUME;
    }

        enum DataType{
        WaitTime,
        Storage,
        Green;
        public boolean isWaitTime(){
            return this == WaitTime;
        }
        public boolean isSotrage(){
            return this == Storage;
        }
        public boolean isGreen(){
            return this == Green;
        }
    }
    
    public void WriteResult(ArrayList<RampMeterResult> dayResult) throws Exception{
        if(mode.isDayModeEachDays()){
            System.out.println("eachDay");
            WriteEachDay(dayResult);
        }
    }
    
    public void WriteResult(ArrayList<RampMeterResult> results, ArrayList<RampMeterResult> anals) throws Exception {
        if(mode.isDayModeRampBased()){
            WriteRampBased_old(results);
        }else if(mode.isDayModeRampBasedAnalyisis()){
            WriteRampBased(results);
        }else{
            WriteAllDays(results,anals);
        }
    }

    /**
     * Write Each Day
     * @param dayResult
     * @throws Exception 
     */
    private void WriteEachDay(ArrayList<RampMeterResult> dayResult) throws Exception{
        System.out.println("total days : "+dayResult.size());
        for(RampMeterResult day : dayResult){
            String filename = FileHelper.getNumberedFileName(day.getSectionName()+"_"+day.getPeriodtoString()+".xls");
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
            writeSheet(workbook,"data",day,1);
            workbook.write();
            workbook.close();
            System.out.println("Write Sucess :" + filename);
        }
    }

    private void WriteAllDays(ArrayList<RampMeterResult> dayResult, ArrayList<RampMeterResult> analysis) throws Exception{
        System.out.println("total days : "+dayResult.size());
        if(dayResult.size() <= 0)
            return;
        /**
         * print Data Type
         */
        for(DataType type : DataType.values()){
            String filename = FileHelper.getNumberedFileName(dayResult.get(0).getSectionName()+"_"+type.toString()+".xls");
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
            int sheetnum = 1;
            for(RampMeterResult day : dayResult){
                writeSheet(workbook,day.getPeriodtoString(),day,sheetnum++,type);
            }
            writeAnalysis(workbook,analysis,dayResult,type);
            workbook.write();
            workbook.close();
            System.out.println("Write Sucess :" + filename);
        }
    }
    
    private void WriteRampBased(ArrayList<RampMeterResult> dayResult) throws Exception{
        System.out.println("total days : "+dayResult.size());
        if(dayResult.size() <= 0)
            return;
        String filename = FileHelper.getNumberedFileName(dayResult.get(0).getSectionName()+"_AllData"+".xls");
        WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
        
        int sheetnum = 1;
        /**
         * write sheet
         */
        int column = 0;
        RampMeterResult tempday = dayResult.get(0);
        WritableSheet[] sheets = new WritableSheet[dayResult.get(0).getRamps().size()];
        
        /**
         * init Sheet by ramp
         */
        int cnt = 0;
        final int INITALROW = 2;
        int row = INITALROW;
        for(RampMeterNode ramp : tempday.getRamps()){
            sheets[cnt] = workbook.createSheet(ramp.getLabel(), sheetnum);
            row = INITALROW;
            for(int i=0;i<3;i++){                
                addData(sheets[cnt],column,row,tempday.getPeriod().getTimeline()); //addTimeLine
                row += ramp.Tlength + 2;
            }
            
            sheetnum++;
            cnt++;
        }
        column += 2;
        for(RampMeterResult day : dayResult){
            column = writeSheet(sheets,day,column,INITALROW);
        }
        
//        writeAnalysis(workbook,dayResult,0);
        
        workbook.write();
        workbook.close();
        System.out.println("Write Sucess :" + filename);
    }
    
    /**
     * @deprecated 
     * @param dayResult
     * @throws Exception 
     */
    private void WriteRampBased_old(ArrayList<RampMeterResult> dayResult) throws Exception{
        System.out.println("total days : "+dayResult.size());
        if(dayResult.size() <= 0)
            return;
        String filename = FileHelper.getNumberedFileName(dayResult.get(0).getSectionName()+"_AllData"+".xls");
        WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
        
        int sheetnum = 1;
        /**
         * write sheet
         */
        int column = 0;
        RampMeterResult tempday = dayResult.get(0);
        WritableSheet[] sheets = new WritableSheet[dayResult.get(0).getRamps().size()];
        
        /**
         * init Sheet by ramp
         */
        int cnt = 0;
        for(RampMeterNode ramp : tempday.getRamps()){
            sheets[cnt] = workbook.createSheet(ramp.getLabel(), sheetnum);
            addData(sheets[cnt],column,new String[]{"",""},tempday.getPeriod().getTimeline()); //addTimeLine
            sheetnum++;
            cnt++;
        }
        column += 2;
        for(RampMeterResult day : dayResult){
            column = writeSheet_old(sheets,day,column,dayResult.size());
        }
        
//        writeAnalysis(workbook,dayResult,0);
        
        workbook.write();
        workbook.close();
        System.out.println("Write Sucess :" + filename);
    }
    
    private void writeAnalysis(WritableWorkbook workbook, ArrayList<RampMeterResult> analysis, ArrayList<RampMeterResult> dayResult, DataType type) {
        if(analysis.size() <= 0)
            return;
        
        int sheetnum = 1;
        /**
         * write sheet
         */
        int column = 0;
        int row = 0;
        RampMeterResult tempday = analysis.get(0);
        WritableSheet sheet = workbook.createSheet("Analysis", 0);
        
        for(int i=0;i<tempday.getRamps().size();i++){
            int RowLength = tempday.getRamps().get(i).Tlength;
            column = 0;
            RampMeterNode currentRamp = tempday.getRamps().get(i);
            
            String str= currentRamp.getLabel()+"("+currentRamp.getId()+")          State-"+currentRamp.isActive();
            addData(sheet,column,row++,new String[]{str});
            addData(sheet,column,row++,new String[]{"Metering"});
            addData(sheet,column,row++,new String[]{""});
            addData(sheet,column,row++,new String[]{"Start"});
            addData(sheet,column,row++,new String[]{"End"});
            row ++;
            /**
             * Write TimeLine
             */
            addData(sheet,column,row,tempday.getPeriod().getTimeline()); //addTimeLine
            column ++;
            /**
             * WRite Data
             */
            row-=5;
            int daycnt = 0;
            for(RampMeterResult day : analysis){
                currentRamp = dayResult.get(daycnt).getRamps().get(i);
                int dayrow = row;
                addData(sheet,column,dayrow++,new String[]{""+currentRamp.isRampMeteringActive()});
                addData(sheet,column,dayrow++,new String[]{day.getPeriodtoHString()});
                if(currentRamp.isRampMeteringActive()){
                    addData(sheet,column,dayrow++,new String[]{""+currentRamp.getRampStartTime()});
                    addData(sheet,column,dayrow++,new String[]{""+currentRamp.getRampEndTime()});
//                    addData(sheet,column,dayrow++,currentRamp.getRampStartDate());
//                    addData(sheet,column,dayrow++,currentRamp.getRampEndDate());
                }else{
                    addData(sheet,column,dayrow++,new String[]{"0:00"});
                    addData(sheet,column,dayrow++,new String[]{"0:00"});
                }
                addData(sheet,column,dayrow++,new String[]{day.getPeriodtoHString()});
                column = writeDatas(sheet,column,dayrow,day,i,type);
                
                daycnt ++;
            }
            row+= RowLength + 6;
        }
        
//        /**
//         * init Sheet by ramp
//         */
//        int cnt = 0;
//        final int INITALROW = 2;
//        int row = INITALROW;
//        for(RampMeterNode ramp : tempday.getRamps()){
//            sheets[cnt] = workbook.createSheet(ramp.getLabel(), sheetnum);
//            row = INITALROW;
//            for(int i=0;i<3;i++){                
//                addData(sheets[cnt],column,row,tempday.getPeriod().getTimeline()); //addTimeLine
//                row += ramp.Tlength + 2;
//            }
//            
//            sheetnum++;
//            cnt++;
//        }
//        column += 2;
//        for(RampMeterResult day : dayResult){
//            column = writeSheet(sheets,day,column,INITALROW);
//        }
    }
    
    /**
     * RampBased
     * @param sheets
     * @param data
     * @param column
     * @param  InitialRow start Row
     * @return 
     */
    private int writeSheet(WritableSheet[] sheets, RampMeterResult data, int column, int InitialRow) {
        int i = 0;
        int col = column;
        int row = InitialRow;
        for(RampMeterNode node : data.getRamps()){
            col = column;
            row = InitialRow;
            
            addData(sheets[i],0,row-1,new String[]{"green"});
            addData(sheets[i],col,new String[]{"",data.getPeriodtoHString()},node.getGreenData(ttype));
            
            row += node.Tlength+1;
            addData(sheets[i],0,row,new String[]{"WaitTime"});
            addData(sheets[i],col,row++,new String[]{data.getPeriodtoHString()});
            addData(sheets[i],col,row,node.getWaitTime());
            row += node.Tlength+1;
            addData(sheets[i],0,row,new String[]{"Storage"});
            addData(sheets[i],col,row++,new String[]{data.getPeriodtoHString()});
            addData(sheets[i],col,row,node.getQueueStorage(ttype));
            i++;
        }
        return column+1;
    }
    
    /**
     * RampBased
     * @param sheets
     * @param data
     * @param column
     * @param  datesize day size
     * @return 
     */
    private int writeSheet_old(WritableSheet[] sheets, RampMeterResult data, int column,int datesize) {
        int i = 0;
        int col = column;
        for(RampMeterNode node : data.getRamps()){
            col = column;
            addData(sheets[i],col,new String[]{"green",data.getPeriodtoHString()},node.getGreenData(ttype));
            col += datesize+1;
            addData(sheets[i],col,new String[]{"WaitTime",data.getPeriodtoHString()},node.getWaitTime());
            col += datesize+1;
            addData(sheets[i],col,new String[]{"Storage",data.getPeriodtoHString()},node.getQueueStorage(ttype));
            i++;
        }
        return column+1;
    }

    /**
     * Write Sheet
     * @param workbook workbook
     * @param name sheet name
     * @param day day data
     * @param num sheet num -- start -> 1
     */
    private void writeSheet(WritableWorkbook workbook, String name, RampMeterResult data, int num) {
        WritableSheet sheet = workbook.createSheet(name, num);
        int column = 0;
        /**
         * Write Time Interval
         */
        addData(sheet,column++,new String[]{"",""},data.getPeriod().getTimeline());
        column++;
        for(RampMeterNode node : data.getRamps()){
                column = writeDatas(sheet,node, column);
                column++;
        }
        
        
    }
    
    /**
     * Write Sheet
     * @param workbook workbook
     * @param name sheet name
     * @param day day data
     * @param num sheet num -- start -> 1
     */
    private void writeSheet(WritableWorkbook workbook, String name, RampMeterResult data, int num, DataType type) {
        WritableSheet sheet = workbook.createSheet(name, num);
        int column = 0;
        /**
         * Write Time Interval
         */
        addData(sheet,column++,new String[]{"",""},data.getPeriod().getTimeline());
        for(RampMeterNode node : data.getRamps()){
                column = writeDatas(sheet,node, column,type);
        }
        
        
    }
    
    /**
     * add Data colume
     * @param sheet
     * @param column
     * @param labels
     * @param data 
     */
    private void addData(WritableSheet sheet, int column, String[] labels, String[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                if(!labels[r].equals(""))
                    sheet.addCell(new Label(column, row, labels[r]));
                else
                    row++;
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
     * add Data colume
     * @param sheet
     * @param column
     * @param labels
     * @param data 
     */
    private void addData(WritableSheet sheet, int column, int row, String[] data)
    {
        try {
            if(data == null)
                return;
            
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Label(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void addData(WritableSheet sheet, int column, int row, Date data)
    {
        try {
            if(data == null)
                return;
                sheet.addCell(new DateTime(column, row++, data));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * Add Data Number
     * @param sheet
     * @param column
     * @param labels
     * @param data double Data
     */
    private void addData(WritableSheet sheet, int column, String[] labels, double[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                if(!labels[r].equals(""))
                    sheet.addCell(new Label(column, row++, labels[r]));
                else
                    row++;
            }
            
            if(data == null)
                return;
            
            for(double d : data)
            {
                sheet.addCell(new Number(column, row++, d));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * add Data colume
     * @param sheet
     * @param column
     * @param labels
     * @param data 
     */
    private void addData(WritableSheet sheet, int column, int row, double[] data)
    {
        try {
            if(data == null)
                return;
            
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Add Data Number
     * @param sheet
     * @param row
     * @param labels
     * @param data double Data
     */
    private void addDataRow(WritableSheet sheet, int row, String[] labels, double[] data)
    {
        try {
            int column = 0;
            for(int r=0; r<labels.length; r++) {
                if(!labels[r].equals(""))
                    sheet.addCell(new Label(column++, row, labels[r]));
            }
            
            if(data == null)
                return;
            
            for(double d : data)
            {
                sheet.addCell(new Number(column++, row, d));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Add Data Number
     * @param sheet
     * @param row
     * @param labels
     * @param data double Data
     */
    private void addDataRow(WritableSheet sheet, int row, String[] labels, String[] data)
    {
        try {
            int column = 0;
            for(int r=0; r<labels.length; r++) {
                if(!labels[r].equals(""))
                    sheet.addCell(new Label(column++, row, labels[r]));
            }
            
            if(data == null)
                return;
            
            for(String d : data)
            {
                sheet.addCell(new Label(column++, row, d));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int writeDatas(WritableSheet sheet, RampMeterNode node, int column) {
        
//            addData(sheet,column++,new String[]{node.getLabel()+"("+node.getId()+")","Queue"},node.getQueue(ttype));
//            addData(sheet,column++,new String[]{"","Passage"},node.getPassage(ttype));
//            addData(sheet,column++,new String[]{"","Queue"},node.getCumulativeQueue(ttype));
//            addData(sheet,column++,new String[]{"","Passage"},node.getCumulativePassage(ttype));
//            addData(sheet,column++,new String[]{"","Green"},node.getGreenData(ttype));
//            addData(sheet,column++,new String[]{"","WaitTime"},node.getWaitTime());
//            addData(sheet,column++,new String[]{"","Storage"},node.getQueueStorage(ttype));
//        addData(sheet,column++,new String[]{node.getLabel()+"("+node.getId()+")","Green"},node.getGreenData(ttype));
//        addData(sheet,column++,new String[]{""+node.isRampMeteringActive(),"WaitTime"},node.getWaitTime());
//        addData(sheet,column++,new String[]{node.getRampStartTime()+"-"+node.getRampEndTime(),"Storage"},node.getQueueStorage(ttype));
        addData(sheet,column++,new String[]{node.getLabel()+"","Green"},node.getGreenData(ttype));
        addData(sheet,column++,new String[]{"","WaitTime"},node.getWaitTime());
        addData(sheet,column++,new String[]{"","Storage"},node.getQueueStorage(ttype));
        
        return column;
    }
    
    private int writeDatas(WritableSheet sheet, RampMeterNode node, int column, DataType type) {
        double[] data = getDatabyType(node,type);
        
        addData(sheet,column++,new String[]{"",node.getLabel()+""},data);
        return column;
    }
    private int writeDatas(WritableSheet sheet, int column,int row, RampMeterResult day, int i, DataType type) {
        RampMeterNode node = day.getRamps().get(i);
        double[] data = getDatabyType(node,type);
        
        addData(sheet,column++,row,data);
        return column;
    }
    
    private double[] getDatabyType(RampMeterNode node, DataType type) {
        double[] data = null;
        if(type.isGreen())
            data = node.getGreenData(ttype);
        else if(type.isSotrage())
            data = node.getQueueStorage(ttype);
        else if(type.isWaitTime())
            data = node.getWaitTime();
        return data;
    }
    

    

    

    
}
