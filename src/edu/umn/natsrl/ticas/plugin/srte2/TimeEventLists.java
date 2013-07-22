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

import edu.umn.natsrl.util.PropertiesWrapper;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class TimeEventLists implements Serializable{
    private HashMap<Integer, TimeEvent> timeevents = new HashMap<Integer, TimeEvent>();
    private PropertiesWrapper prop = new PropertiesWrapper();
    
    private String NAME = "name";
    private String DATA = "data";
    private String LISTCOUNT = "count";
    
    private String name;
    
    public final static String SAVE_PROP_DIR = "srte";
    public final static String SAVE_PROP_NAME = SAVE_PROP_DIR+File.separator+"timeevent.data";
    
    TimeEventLists(){;}
    TimeEventLists(String name){this.name = name;}
    TimeEventLists(String listname, PropertiesWrapper _prop){
        this(_prop);
        name = listname;
    }
    TimeEventLists(PropertiesWrapper _prop){
        prop = _prop.clone();
        int Count = prop.getInteger(LISTCOUNT);
        setName(prop.get(this.NAME));
        
        for(int i=0;i<Count;i++){
            TimeEvent te = new TimeEvent();
            String data = prop.get(DATA+"."+i);
            
            if(data == null)
                continue;
            
            te.setAllData(data);
            AddTimeEvent(te);
        }
    }
    
    public boolean AddTimeEvent(TimeEvent te){
        TimeEvent newt = new TimeEvent();
        newt.setAllData(te.getAllData());
        
        if(!newt.isdata()){
            System.out.println("Empty Data");
            return false;
        }
        
        if(!IsTimeEvent(newt)){
            if(!newt.isCODE())
                newt.setCODE(setCODE());
            timeevents.put(newt.getCODE(),newt);
            return true;
        }else
            return false;
    }
    public boolean UpdateEvent(TimeEvent te){
        TimeEvent newt = new TimeEvent();
        newt.setAllData(te.getAllData());
        
        if(!newt.isdata()){
            System.out.println("Empty Data");
            return false;
        }
        
        if(IsTimeEvent(newt))
            timeevents.remove(newt.getCODE());
        
        timeevents.put(newt.getCODE(),newt);
        
        return true;
    }
    
    public void RemoveTimeEvent(TimeEvent te){
        if(IsTimeEvent(te))
            timeevents.remove(te.getCODE());
        save();
    }
    
    public void clearTimeEvent(){
        timeevents.clear();
        save();
    }
    
    public Collection<TimeEvent> getTimeEvents(){
        return timeevents.values();
    }
    
    public int getSize(){
        return timeevents.size();
    }
    
    public void setName(String Name){
        name = Name;
    }
    
    public void save(){
        if(name == null)
            return;
        
        File MainDir = new File(TimeEventLists.SAVE_PROP_DIR);
        
        if (!MainDir.mkdir() && !MainDir.exists()) {
            JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + MainDir);
            return;
        }
        
        prop.put(this.NAME, name);
        prop.put(this.LISTCOUNT,getTimeEvents().size());
        
        int count = 0;
        for(TimeEvent te : getTimeEvents()){
            prop.put(this.DATA+"."+count,te.getAllData());
            count ++;
        }
        
        prop.save(TimeEventLists.SAVE_PROP_NAME+"."+name,"Time Event List Data");
    }
    
    public boolean deleteList(){
        File file = new File(TimeEventLists.SAVE_PROP_NAME+"."+name);
        
        try{
            file.delete();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
    }
    
    public static Collection<TimeEventLists> loadAllEvents(){
        File file = new File(TimeEventLists.SAVE_PROP_DIR);  
        File[] files = file.listFiles();  
        if(files == null) return null;
        
        ArrayList<TimeEventLists> res = new ArrayList<TimeEventLists>();
        for (int i = 0; i < files.length; i++)  
        {  
            if(files[i].isDirectory()) continue;
            System.out.println("fname : "+ files[i].getAbsolutePath());
           TimeEventLists s = TimeEventLists.load(files[i]);
           if(s == null) continue;
           res.add(s);
        }
        System.out.println("res count : "+res.size());
        return res;
    }
    
    public static TimeEventLists load(File _f){
        return load(_f.getAbsolutePath());
    }
    
    public static TimeEventLists load(String _path){
        try{
            PropertiesWrapper prop = PropertiesWrapper.load(_path);
            return new TimeEventLists(prop);
        }catch(Exception ex){
            ex.printStackTrace();
            return new TimeEventLists();
        }
    }

    private boolean IsTimeEvent(TimeEvent newt) {
        if(!newt.isCODE())
            return false;
        
        if(timeevents.get(newt.getCODE()) == null)
            return false;
        else
            return true;
    }

    private int setCODE() {
        int count = 0;
        while(true){
            if(timeevents.get(count) == null)
                return count;
            
            count++;
        }
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }
}
