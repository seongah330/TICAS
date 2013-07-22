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
package edu.umn.natsrl.ticas.plugin.srte2;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.section.SectionManager;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class TimeEvent implements Serializable{
    private TMO tmo = TMO.getInstance();
    private long sDate=-1;
    private long eDate=-1;
    private long bDate=-1;
    private int TIMECOUNT = 3;
    
    private String TIMEDESC = "/";
    private String DATADESC = ",";
    
    private int CODE = -1;
    
    private boolean isdata = false;
    private String sectionName;
    
    TimeEvent(){;}
    TimeEvent(String sectionName, Calendar sdate, Calendar edate, Calendar bdate){
        this();
        this.sectionName = sectionName;
        this.sDate = sdate.getTimeInMillis();
        this.eDate = edate.getTimeInMillis();
        this.bDate = bdate.getTimeInMillis();
    }
    
    public void setCODE(int code){
        this.CODE = code;
    }
    public void setSection(Section section){
        setSection(section.getName());
    }
    public void setSection(String name){
        this.sectionName = name;        
    }
    public Section getSection(){
        return getSection(null);
    }
    private Section getSection(Section sec) {
        if(this.sectionName == null)
            return null;
        
        Section realsection = null;
        if(sec == null){
            Vector<Section> sections = loadSection();
            for(Section s : sections){
                if(s.getName().equals(sectionName)){
                    realsection = s;
                    break;
                }
            }
        }else
            realsection = sec;
        
        if(realsection == null)
            return null;
        
        return realsection;
    }
    /**
     * Load section data into combo box
     */
    private Vector<Section> loadSection() {
        SectionManager sm = tmo.getSectionManager();
        if(sm == null) return null;
        Vector<Section> sections = new Vector<Section>();
        sections.clear();
        sm.loadSections();
        sections.addAll(sm.getSections());
        return sections;
    }
//    public void setDate(Calendar ca){
//        this.date = ca.getTimeInMillis();
//    }
//    public void setDate(long ca){
//        this.date = ca;
//    }
    public void setStartTime(long sdate){
        this.sDate = sdate;
    }
    public void setEndTime(long edate){
        this.eDate = edate;
    }
    public void setBareLaneRegainTime(long bdate){
        this.bDate = bdate;
    }
    public void setStartTime(Calendar sdate){
        setStartTime(sdate.getTimeInMillis());
    }
    public void setEndTime(Calendar edate){
        setEndTime(edate.getTimeInMillis());
    }
    public void setBareLaneRegainTime(Calendar bdate){
        setBareLaneRegainTime(bdate.getTimeInMillis());
    }
    public void setAllTime(Calendar sdate,Calendar edate, Calendar bdate){
        setStartTime(sdate);
        setEndTime(edate);
        setBareLaneRegainTime(bdate);
    }
    public Calendar getStartTime(){
        if(!this.isStartTime())
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(sDate);
        return cal;
    }
    public Calendar getEndTime(){
        if(!this.isEndTime())
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(eDate);
        return cal;
    }
    public Calendar getBareLaneRegainTime(){
        if(!this.isBareLaneTime())
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(bDate);
        return cal;
    }
    public String getDatetoString(Calendar cal){
        SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return dateformatter.format(cal.getTime());
    }
    
    public boolean isStartTime(){
        if(sDate != -1)
            return true;
        else
            return false;
    }
    public boolean isEndTime(){
        if(eDate != -1)
            return true;
        else
            return false;
    }
    public boolean isBareLaneTime(){
        if(bDate != -1)
            return true;
        else
            return false;
    }
    public boolean isSection(){
        if(sectionName != null)
            return true;
        else return false;
    }
    
    public String getAllData(){
        return CODE + DATADESC + sectionName + DATADESC + getAllTime();
    }
    public void setAllData(String data){
        if(data == null)
            return;
        
//        System.out.println(data);
        String[] dataset = data.split(DATADESC);
        
        if(dataset.length <= 0)
            return;
        
        setCODE(Integer.parseInt(dataset[0]));
        setSection(dataset[1]);
        setAllTime(dataset[2]);
    }
    
    public String getAllTime(){
        return sDate+TIMEDESC+
                eDate+TIMEDESC+
                bDate+TIMEDESC;
    }
    
    public void setAllTime(String data){
        if(data == null)
            return;
        
        String[] dataset = data.split(TIMEDESC);
        
        if(dataset.length != TIMECOUNT)
            return;
        
        setStartTime(Long.parseLong(dataset[0]));
        setEndTime(Long.parseLong(dataset[1]));
        setBareLaneRegainTime(Long.parseLong(dataset[2]));
    }
    
    public int getCODE(){
        return this.CODE;
    }
    public boolean isdata(){
        if(this.isStartTime() && isEndTime() && isSection())
            return true;
        else return false;
    }
    
    public boolean isCODE(){
        if(this.CODE == -1)
            return false;
        else
            return true;
    }
    
    @Override
    public String toString()
    {
        return this.CODE + " - " +getSectionName()+ " : "+ getDatetoString(this.getStartTime()) + " -> " + getDatetoString(this.getEndTime()) + " BareLane R Point : " + getDatetoString(this.getBareLaneRegainTime());
    }

    public String getSectionName() {
        return this.sectionName;
    }
}
