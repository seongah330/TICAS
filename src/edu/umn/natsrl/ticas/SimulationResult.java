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

package edu.umn.natsrl.ticas;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class SimulationResult implements Comparable {
    final String K_NAME = "name";
    final String K_DESC = "desc"; 
    final String K_CREATED = "created";
    final String K_PERIOD_START = "period.start";
    final String K_PERIOD_END = "period.end";
    final String K_PERIOD_INTERVAL = "period.interval";
    final String K_SECTION_NAME = "section.name"; 
    final String K_SECTION_DESC = "section.desc";
    final String K_SECTION_RNODES = "section.rnodes" ;
    final String K_SECTION_DETECTORS = "section.detectors";
    public final static String SAVE_PROP_DIR = "simulation results";    
    public final static String SAVE_DATA_DIR = "simulation results"+File.separator+"data";
    public final static String SAVE_PROP_DIR_SUB = "simulation results"+ File.separator + "subdata";    
    public final static String SAVE_DATA_DIR_SUB = "simulation results" + File.separator + "subdata" +File.separator+"data";
    private String name;
    private String desc;
    private Date created;
    
    transient Section section;
    private Period period;
    
    private PropertiesWrapper prop = new PropertiesWrapper();
    private PropertiesWrapper data = new PropertiesWrapper();
    
    //Several Save
    private boolean isSubData = false;
    private String DataKey = null;
    private String DataLength = null;
    final String K_DATAKEY = "dkey";
    final String K_DATALENGTH = "dlength";
    public final static int DESC_SNAME = 0;
    public final static int DESC_RSEED = 1;
    public final static int DESC_POINT = 2;
    public final static int DESC_KEY = 3;
    
    public SimulationResult(String name, String desc, Section section, Period period) {
        SimulationResultInit(name,desc,section,period);
    }
    
    public SimulationResult(String name, String desc, Section section, Period period, boolean subData, String _key) {
        this.isSubData = subData;
        this.DataKey = _key;
        SimulationResultInit(name,desc,section,period);
    }
    public SimulationResult(String name, String desc, Section section, Period period, boolean subData, String _key, int _length) {
        this.isSubData = subData;
        this.DataKey = _key;
        this.DataLength = String.valueOf(_length);
        SimulationResultInit(name,desc,section,period);
    }
    
    private void SimulationResultInit(String name, String desc, Section section, Period period){
        this.name = name;
        this.desc = desc;
        this.section = section;
        this.period = period;
        this.created = new Date();  
        
        prop.put(K_NAME, name);
        prop.put(K_DESC, desc);
        
        if(this.DataKey != null){
            prop.put(K_DATAKEY, this.DataKey);
        }
        if(this.DataLength != null)
            prop.put(K_DATALENGTH, this.DataLength);
        
        prop.put(K_CREATED, created);
        prop.put(K_SECTION_NAME, section.getName());
        prop.put(K_SECTION_DESC, section.getDescription());
        prop.put(K_SECTION_DETECTORS, getDetectorIds(section));
        prop.put(K_SECTION_RNODES, section.getRNodeIds());
        prop.put(K_PERIOD_START, period.startDate);
        prop.put(K_PERIOD_END, period.endDate);
        prop.put(K_PERIOD_INTERVAL, period.interval);
        
        if(!IsListData()){
            readTrafficDataFromDetectors();
        }else
            System.out.println("list data");
        
    }
    
    private SimulationResult(PropertiesWrapper p) {
        this.prop = p.clone();
        this.name = prop.get(K_NAME);
        this.desc = prop.get(K_DESC);
        
        try{
            this.DataKey = prop.get(K_DATAKEY);
        }catch(Exception e){
            System.out.println("Not Found Data Key");
        }
        
        try{
            this.DataLength = prop.get(K_DATALENGTH);
        }catch(Exception e){
            System.out.println("Not Found Data Length");
        }
        
        Section s = new Section(prop.get(K_SECTION_NAME), prop.get(K_SECTION_DESC));
        s.setRnodeIds(prop.getStringList(K_SECTION_RNODES));
        this.section = s;
        this.period = new Period(prop.getDate(K_PERIOD_START), prop.getDate(K_PERIOD_END), prop.getInteger(K_PERIOD_INTERVAL));
        this.created = prop.getDate(K_CREATED);
    }       
 
    public boolean IsListData(){
        if(this.DataKey != null && !this.isSubData)
            return true;
        else
            return false;
    }
    
    public int getSimulationLegnth(){
        if(this.DataLength == null){
            return 0;
        }else{
            return Integer.parseInt(this.DataLength);
        }
    }
    public String getDataKey(){
        return this.DataKey;
    }
    public void setSubData(boolean _f){
        this.isSubData = _f;
    }
    public void save() throws Exception {
        save(true);
    }
    
    public void save(boolean readTraffic) throws Exception {
        File MainDir = new File(this.SAVE_PROP_DIR);
        if (!MainDir.mkdir() && !MainDir.exists()) {
            JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + MainDir);
            return;
        }        
        
        File savePropDir = new File(this.getPropDIR());
        if (!savePropDir.mkdir() && !savePropDir.exists()) {
            JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + savePropDir);
            return;
        }        
        File saveDataDir = new File(this.getDataDIR());
        if (!saveDataDir.mkdir() && !saveDataDir.exists()) {
            JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + saveDataDir);
            return;
        } 
        
        
        String eName = getCacheFileName(this.name);
        String propFileName = this.getPropDIR() + File.separator + eName;
        prop.save(propFileName, "Simulation Result Title : " + this.name);

        if(!this.IsListData()){
            if(readTraffic) readTrafficDataFromDetectors();

            String dataFileName = this.getDataDIR() + File.separator + eName;
            data.save(dataFileName, "Simulation Result Title : " + this.name);
        }
    }

    // add traffic data into property
    private void readTrafficDataFromDetectors() {
        for(RNode rn : section.getRNodesWithExitEntrance()) {
            for(Detector d : rn.getDetectors()) {
                addTrafficData(d);
            }
        }        
    }    
    
    private void addTrafficData(Detector d) {
        double[] k=d.getDensity(), q=d.getFlow(), o=d.getOccupancy(), c=d.getScan(), u=d.getSpeed(), v=d.getVolume();
        data.put(getKey(d.getId(), TrafficType.DENSITY), getCsv(k));
        data.put(getKey(d.getId(), TrafficType.FLOW), getCsv(q));
        data.put(getKey(d.getId(), TrafficType.OCCUPANCY), getCsv(o));
        data.put(getKey(d.getId(), TrafficType.SCAN), getCsv(c));
        data.put(getKey(d.getId(), TrafficType.SPEED), getCsv(u));
        data.put(getKey(d.getId(), TrafficType.VOLUME), getCsv(v));        
    }
    
    public void setTrafficDataToDetectors(){
        SimObjects simObjects = SimObjects.getInstance();
        setTrafficDataToDetectors(simObjects);
    }
    
    public void setTrafficDataToDetectors(SimObjects sim) {
        if(!this.IsListData()){
            
            String dataFile = this.getDataDIR() + File.separator + SimulationResult.getCacheFileName(name);
            data = PropertiesWrapper.load(dataFile);        
            SimObjects simObjects = sim;
            List<String> detectorIds = prop.getStringList(K_SECTION_DETECTORS);
            for(String detector_id : detectorIds) {
                SimDetector sd = simObjects.getDetector(detector_id);
                sd.setTrafficData(TrafficType.DENSITY, data.getDoubleArray(getKey(detector_id, TrafficType.DENSITY)));
                sd.setTrafficData(TrafficType.FLOW, data.getDoubleArray(getKey(detector_id, TrafficType.FLOW)));
                sd.setTrafficData(TrafficType.OCCUPANCY, data.getDoubleArray(getKey(detector_id, TrafficType.OCCUPANCY)));
                sd.setTrafficData(TrafficType.SCAN, data.getDoubleArray(getKey(detector_id, TrafficType.SCAN)));
                sd.setTrafficData(TrafficType.SPEED, data.getDoubleArray(getKey(detector_id, TrafficType.SPEED)));
                sd.setTrafficData(TrafficType.VOLUME, data.getDoubleArray(getKey(detector_id, TrafficType.VOLUME)));
            }
        }else{
            System.out.println("this is several Model");
        }
    }   
    
    public void AddTrafficDataToDetectors(){
        SimObjects simObjects = SimObjects.getInstance();
        AddTrafficDataToDetectors(simObjects);
    }
    
    public void AddTrafficDataToDetectors(SimObjects sim) {
        if(!this.IsListData()){
            String dataFile = this.getDataDIR() + File.separator + SimulationResult.getCacheFileName(name);
            data = PropertiesWrapper.load(dataFile);        
            SimObjects simObjects = sim;
            List<String> detectorIds = prop.getStringList(K_SECTION_DETECTORS);
            for(String detector_id : detectorIds) {
                SimDetector sd = simObjects.getDetector(detector_id);
                sd.addTrafficData(TrafficType.DENSITY, data.getDoubleArray(getKey(detector_id, TrafficType.DENSITY)));
                sd.addTrafficData(TrafficType.FLOW, data.getDoubleArray(getKey(detector_id, TrafficType.FLOW)));
                sd.addTrafficData(TrafficType.OCCUPANCY, data.getDoubleArray(getKey(detector_id, TrafficType.OCCUPANCY)));
                sd.addTrafficData(TrafficType.SCAN, data.getDoubleArray(getKey(detector_id, TrafficType.SCAN)));
                sd.addTrafficData(TrafficType.SPEED, data.getDoubleArray(getKey(detector_id, TrafficType.SPEED)));
                sd.addTrafficData(TrafficType.VOLUME, data.getDoubleArray(getKey(detector_id, TrafficType.VOLUME)));
            }
        }else{
            System.out.println("this is several Model");
        }
    }

   
    /**
     * Load serialized simulation result
     * @return simulation result
     */
    public static SimulationResult load(File simResultFile)
    {
        return load(simResultFile.getAbsolutePath());
    }   
    
    public static SimulationResult load(String simResultPath)
    {
        try {            
            PropertiesWrapper prop = PropertiesWrapper.load(simResultPath);
            return new SimulationResult(prop);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }          
    }       
    

    /**
     * 
     * @param name simulation result name
     * @return hashed string with MD5
     */
    public static String getCacheFileName(String name)
    {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            m.update(name.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            return bigInt.toString(16);        
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }      

    public void update(String name, String desc) {
        try {            
            boolean samename = this.name.equals(name); 
            String originalName = this.name;
            File origin = new File(getPropPath(this.name));
            File target = new File(getPropPath(name));
            
            if(!name.equals(this.name) && target.exists()) {
                JOptionPane.showMessageDialog(null, "Use another name. (Already exists)");
                return;
            }
            if(name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name is required");
                return;            
            }

            prop.put(K_NAME, name);
            prop.put(K_DESC, desc);
            if(this.DataKey != null)
                prop.put(K_DATAKEY, this.DataKey);
            if(this.DataLength != null)
                prop.put(K_DATALENGTH, this.DataLength);
            
            this.name = name;
            this.desc = desc;

            this.save(false);            
            
            if(!samename) {
                File originDataFile = new File(this.getDataPath(originalName));
                FileHelper.copy(originDataFile, new File(getDataPath(name)));                
                originDataFile.delete();
                origin.delete();
            }
            
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fail");            
        }
    }

    private String getPropPath(String fileName) {
        return getPropDIR() + File.separator + getCacheFileName(fileName);
    }
    
    private String getDataPath(String fileName) {
        return getDataDIR() + File.separator + getCacheFileName(fileName);       
    }    
    private String getPropDIR(){
        if(this.isSubData)
            return SAVE_PROP_DIR_SUB;
        else
            return SAVE_PROP_DIR;
        
    }
    private String getDataDIR(){
        if(this.isSubData)
            return SAVE_DATA_DIR_SUB;
        else
            return SAVE_DATA_DIR;
        
    }
    private String getDetectorIds(Section section) {
        Vector<String> ids = new Vector<String>();
        for(RNode rn : section.getRNodesWithExitEntrance()) {
            for(Detector d : rn.getDetectors()) {
                ids.add(d.getId());
            }
        }                
        return getCsv(ids);
    }
    
    private String getKey(String did, TrafficType tType) {
        return did+"_"+tType.getTrafficTypeId();
    }
    
    private Vector<String> getVector(String str) {        
        String[] arr = str.split(",");
        if(arr.length == 0) return new Vector<String>();
        return new Vector<String>(Arrays.asList(arr));
    }
    
    private String getCsv(Vector<String> objs) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<objs.size(); i++) {
            if(i!=0) { sb.append(","); }
            sb.append(objs.get(i));
        }
        return sb.toString();        
    }    
    
    private String getCsv(double[] objs) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<objs.length; i++) {
            if(i!=0) { sb.append(","); }
            sb.append(objs[i]);
        }
        return sb.toString();        
    }

    @Override
    public int compareTo(Object o) {        
        SimulationResult r = (SimulationResult)o;        
        return this.name.compareTo(r.name);
        
    }
    
    public Date getCreated() {
        return created;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    public Period getPeriod() {
        return period;
    }

    public Section getSection() {
        return section;
    }
    public String getDESC(int key){
        if(this.desc == null)
            return null;
        String[] d = this.desc.split(",");
        
        if(key >= d.length)
            return null;
        
        return d[key];
    }
    @Override
    public String toString() {
        return this.name;
    }


}
