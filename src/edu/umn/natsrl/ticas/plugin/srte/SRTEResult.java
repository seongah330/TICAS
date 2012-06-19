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
import edu.umn.natsrl.infra.infraobjects.Station;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEResult {
    public String sectionName;
    public Calendar cs,ce,cb;
    public Period period;
    public int srst; // speed decline point for 5 min data
    public int lst; // low speed point for 5 min data
    public int rst; // speed recovery point for 5 min data
    /**
     * @deprecated
     * */
    public ArrayList<Integer> srt = new ArrayList<Integer>(); // stable speed point for 5 min data
    
    /**
     * new Algorithm
     */
    public Station station;
    public PatternType pType;

    public double[] data_origin;   // original 5 min speed data
    public double[] data_smoothed;   // smoothed 5 min speed data
    public double[] data_quant;   // 5 min speed data after quantization
    
    public double[] u_Avg_origin;
    public double[] u_Avg_smoothed;
    public double[] u_Avg_quant;

    public double[] k_smoothed;
    public double[] k_origin;
    public double[] k_quant;
    
    public double[] q_origin;
    public double[] q_smoothed;
    public double[] q_quant;
    
    public int[] phases;
    public List<String> msgs = new ArrayList<String>();
    
    public SRTEResult() { }

    public void setResult(int sdp, int lsp, int srp) {
        this.srst = sdp;
        this.lst = lsp;
        this.rst = srp;
    }
    public void setTime(Calendar CS,Calendar CE,Calendar CB){
        cs = CS;
        ce = CE;
        cb = CB;
    }
    public Calendar getStartTime(){
        return cs;
    }
    
    public Calendar getEndTime(){
        return ce;
    }
    
    public Calendar getBareLaneTime(){
        return cb;
    }
    public String getStartTimetoString(){
        if(cs == null)
            return null;
        else
            return getDatetoString(cs);
    }
    public String getEndTimetoString(){
        if(ce == null)
            return null;
        else
            return getDatetoString(ce);
    }
    public String getBareLaneTimetoString(){
        if(cb == null)
            return null;
        else
            return getDatetoString(cb);
    }

    /**
     * @deprecated
     * */
    public void addSRT(int srt)
    {
        this.srt.add(srt);
    }

    void addLog(String msg) {
        addLog(msg,true);
    }
    void addLog(String msg,boolean entrance) {
        if(entrance)
            System.out.println(msg);
        else
            System.out.print(msg);
        this.msgs.add(msg);
    }

    private String getDatetoString(Calendar c) {
        if(c == null)
            return null;
        
        SimpleDateFormat dateformatter = new SimpleDateFormat("d MMM, yyyy 'at' HH:mm:ss");
        return dateformatter.format(c.getTime());
    }
    public void setPeriod(Period p){
        period = p;
    }
    public String getPeriodtoString(){
        if(period == null)
            return null;
        return period.getPeriodString();
    }

}
