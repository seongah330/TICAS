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
package edu.umn.natsrl.ticas.plugin.srtedataextractor;

import edu.umn.natsrl.evaluation.Evaluation;
import edu.umn.natsrl.evaluation.EvaluationOption;
import edu.umn.natsrl.evaluation.EvaluationResult;
import edu.umn.natsrl.evaluation.OptionType;
import edu.umn.natsrl.evaluation.StationAverageLaneFlow;
import edu.umn.natsrl.evaluation.StationDensity;
import edu.umn.natsrl.evaluation.StationSpeed;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.ticas.TICASOption;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.Label;
import jxl.write.Number;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEextractor extends Thread{
        EvaluationOption eopt;
        Section section;
        SRTEOption sopt;
        
        public static interface AlogorithmEndListener{
                public void onEndMessage(boolean msg);
        }
        private AlogorithmEndListener endListener;
        public SRTEextractor(TICASOption opt, SRTEOption soption){
                eopt = opt.getEvaluationOption();
                section = eopt.getSection();
                sopt = soption;
        }
        
        @Override
        public void run(){
                System.out.println("load Speed Data..");
                Vector<EvaluationResult> speed = Evaluation.getResult(StationSpeed.class, eopt);
                System.out.println("load Density Data..");
                Vector<EvaluationResult> density = Evaluation.getResult(StationDensity.class, eopt);
                System.out.println("load Flow Data..");
                Vector<EvaluationResult> flow = Evaluation.getResult(StationAverageLaneFlow.class, eopt);
                int totalperiod = speed.size();
                int startdatecnt = totalperiod > 1 ? 1 : 0;
                try {
                        createSectionBased(speed,density,flow,totalperiod,startdatecnt);
                        createStationWideRange(speed,density,flow,totalperiod,startdatecnt);
                        
        //                        EvaluationResult res = EvaluationResult.copy(result);
        //                        for(int c = res.COL_DATA_START(); c < res.getColumnSize(); c++){
        //                                System.out.println(res.get(c, res.ROW_TITLE()));
        //                                for(res.ROW_DATA_START();
        //                        }
        //                }
                        if(endListener != null){
                                endListener.onEndMessage(true);
                        }
                } catch (Exception ex) {
                        if(endListener != null)
                                endListener.onEndMessage(true);
                        Logger.getLogger(SRTEextractor.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        private void createStationWideRange(Vector<EvaluationResult> speed, Vector<EvaluationResult> density, Vector<EvaluationResult> flow, int totalperiod, int startdatecnt) throws IOException, WriteException {
                String fname = "Range";
                //create sheet
                String workbookFile = getFileName(fname+section.getName(), "xls");
                System.out.println("Make Excel File...."+workbookFile+"....");
                WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));
                
                if(speed == null || speed.get(0) == null)
                        return;
                
                EvaluationResult tempres = EvaluationResult.copy(speed.get(0));
                this.removeVirtualStationFromResult(tempres);
                int col_end = tempres.getColumnSize();
                int stationcnt = 0;
                for(int station_i = tempres.COL_DATA_START();station_i<col_end;station_i++){
                        String sname = tempres.get(station_i,0).toString();
                        WritableSheet sheet = workbook.createSheet(sname, stationcnt++);
                        createsheetforRange(speed,density,flow,startdatecnt,totalperiod,station_i,sheet);
                }
                
                workbook.write();
                workbook.close();
                System.out.println("Complete");
        }
        
        private void createsheetforRange(Vector<EvaluationResult> speed, Vector<EvaluationResult> density, Vector<EvaluationResult> flow, 
                int startdatecnt, int totalperiod, int station_i, WritableSheet sheet) throws WriteException {
                
                int startrow = 1;
                int periodcnt = 0;
                for(int i = startdatecnt; i < totalperiod; i++){
                        ArrayList<ArrayList> data = new ArrayList<ArrayList>();
                        EvaluationResult ures = EvaluationResult.copy(speed.get(i));
                        EvaluationResult kres = EvaluationResult.copy(density.get(i));
                        EvaluationResult qres = EvaluationResult.copy(flow.get(i));
                        removeVirtualStationFromResult(ures);
                        removeVirtualStationFromResult(kres);
                        removeVirtualStationFromResult(qres);
//                        System.out.println("b -"+ures.getColumnSize()+", "+ures.getRowSize(0));
                        sopt.deleteTime(ures);
                        sopt.deleteTime(kres);
                        sopt.deleteTime(qres);
//                        System.out.println("a -"+ures.getColumnSize()+", "+ures.getRowSize(0));
                        ArrayList time = ures.getColumn(ures.COL_TIMELINE());
                        ArrayList us = ures.getColumn(station_i);
                        ArrayList ks = kres.getColumn(station_i);
                        ArrayList qs = qres.getColumn(station_i);
                        
                        adjustrange(time);
                        adjustrange(us);
                        adjustrange(ks);
                        adjustrange(qs);
                        
                        /**first row */
                        sheet.addCell(new Label(1, 0, "U"));
                        sheet.addCell(new Label(2, 0, "K"));
                        sheet.addCell(new Label(3, 0, "Q"));
                        sheet.addCell(new Label(4, startrow, eopt.getPeriods()[periodcnt++].getPeriodString()));
//                        time.add(0, eopt.getPeriods()[periodcnt++].getPeriodString());
//                        if(i == startdatecnt){
//                                us.add(0, "U");
//                                ks.add(0, "K");
//                                qs.add(0, "Q");
//                        }else{
//                                us.add(0, "");
//                                ks.add(0, "");
//                                qs.add(0, "");
//                        }
                        
                        data.add(time);
                        data.add(ks);
                        data.add(qs);
                        data.add(us);
                        EvaluationResult er = new EvaluationResult(data);
                        startrow = createsheet(sheet, er, startrow,false);
                        
                }
        }

        private void createSectionBased(Vector<EvaluationResult> speed, Vector<EvaluationResult> density, Vector<EvaluationResult> flow, int totalperiod, int startdatecnt) throws IOException, WriteException {
                String fname = "Normal";
                //create sheet
                String workbookFile = getFileName(fname+section.getName(), "xls");
                System.out.print("Make Excel File...."+workbookFile+"...");
                WritableWorkbook workbook = Workbook.createWorkbook(new File(workbookFile));
                int periodcnt = 0;
                
                for(int i = startdatecnt; i < totalperiod; i++){
                        int startrow = 0;
                        Period p = eopt.getPeriods()[periodcnt];
                        WritableSheet sheet = workbook.createSheet(p.getPeriodString(), periodcnt);
                        
                        //speed
                        EvaluationResult res = EvaluationResult.copy(speed.get(i));
                        sopt.deleteTime(res);
                        addAverage("U",res);
                        startrow = createsheet(sheet, res, startrow,false);
                        
                        //density
                        res = EvaluationResult.copy(density.get(i));
                        sopt.deleteTime(res);
                        addAverage("K",res);
                        startrow = createsheet(sheet, res, startrow+1,false);
                        
                        //flow
                        res = EvaluationResult.copy(flow.get(i));
                        sopt.deleteTime(res);
                        addAverage("Q",res);
                        startrow = createsheet(sheet, res, startrow+1,false);
                        periodcnt++;
                }
                
                workbook.write();
                workbook.close();
                System.out.println("Complete");
        }
        
        private int createsheet(WritableSheet sheet, EvaluationResult res, int startrow, boolean istrans) throws WriteException {
                ArrayList<ArrayList> data;
                if(!istrans)
                        data = res.getData();
                else
                        data = res.getTransposedData();
                int cols = data.size();
                int rows = data.get(0).size();

                for (int c = 0; c < cols; c++) {                
                        for (int r = 0, rr=startrow; r < rows; r++) {

                                Object o = data.get(c).get(r);
                                try {
                                        if (isDouble(o)) {
                                            sheet.addCell(new Number(c, rr, (Double) o));
                                        } else if (isInteger(o)) {
                                            sheet.addCell(new Number(c, rr, (Integer) o));
                                        } else {
                                            sheet.addCell(new Label(c, rr, (String) o));
                                        }
                                        rr++;
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                }
                        }
                }
                
                
                return startrow + rows;
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
        
        /**
     * Is it integer ?
     * @param data string
     * @return true if it is interger format, else false
     */
    protected boolean isInteger(Object data) {
        return (data instanceof Integer);
        //return !NO_STATION.equals(data) && data.matches("^-?[0-9]+$") || data.matches("^[0]+(.)[0]+$");
    }

    /**
     * Is it double ?
     * @param data string
     * @return true if it is double format, else false
     */
    protected boolean isDouble(Object data) {
        return (data instanceof Double);
    }

        private void addAverage(String name, EvaluationResult res) {
                ArrayList<Object> avg = new ArrayList();
                avg.add(name+" Average");
                avg.add("avg");
                avg.add("");
                for(int rr=res.ROW_DATA_START();rr<res.getRowSize(0);rr++){
                        double sum = 0;
                        int cnt = 0;
                        for(int cc=res.COL_DATA_START();cc<res.getColumnSize();cc++){
                                sum += Double.parseDouble(res.get(cc, rr).toString());
                                cnt++;
                        }
                        double cavg = cnt != 0 ? (sum / cnt) : 0;
                        avg.add(cavg);
                }
                res.addColumn(avg);
        }
        
        private EvaluationResult removeVirtualStationFromResult(EvaluationResult res){
                String NO_STATION = "-";
                ArrayList<ArrayList> ret = new ArrayList<ArrayList>();
                for (int c = 0; c < res.getColumnSize(); c++) {
                        if (!NO_STATION.equals(res.getColumn(c).get(0))) {
                                ret.add(res.getColumn(c));
                        }
                }
                res.setData(ret);
                res.setUseVirtualStation(false);
                return res;
        }

        private void adjustrange(ArrayList time) {
                for(int i=0;i<3;i++)
                        time.remove(0);
        }
        public void setEndListener(AlogorithmEndListener listener){
                this.endListener = listener;
        }
        
}
