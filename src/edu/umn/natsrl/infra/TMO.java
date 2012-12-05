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

package edu.umn.natsrl.infra;

import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.section.SectionEditor;
import edu.umn.natsrl.util.ModalFrameUtil;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFrame;

/**
 *
 * @author Chongmyung Park
 */
public class TMO implements Serializable {

    private static TMO realInstance = new TMO();
    transient private Infra infra;
    transient private SectionManager sectionManager;
    private boolean isLoaded = false;
    
    private TMO() {
    }

    public static TMO getInstance() {
        return realInstance;
    }

    public Infra getInfra() {
        return infra;
    }

    public SectionManager getSectionManager() {
        return sectionManager;
    }
    
    public void clearDetectorDataCache()
    {
        deleteDirectory(InfraConstants.CACHE_DETDATA_DIR);
    }

    public void clearDetectorFailCache()
    {
        deleteDirectory(InfraConstants.CACHE_DETFAIL_DIR);
    }    
    
    public void clearTrafficDataCache()
    {
        deleteDirectory(InfraConstants.CACHE_TRAFDATA_DIR);
    }        
    
    public void clearAllCache()
    {
        deleteDirectory(InfraConstants.CACHE_DETDATA_DIR);
        deleteDirectory(InfraConstants.CACHE_DETFAIL_DIR);
        deleteDirectory(InfraConstants.CACHE_TRAFDATA_DIR);
    }       

    public void clearDetectorDataCache(int days)
    {
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_DETDATA_DIR);
    }

    public void clearDetectorFailCache(int days)
    {
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_DETFAIL_DIR);
    }    
    
    public void clearTrafficDataCache(int days)
    {
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_TRAFDATA_DIR);
    }        
    
    public void clearAllCache(int days)
    {
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_DETDATA_DIR);
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_DETFAIL_DIR);
        deleteFilesOlderThanNdays(days, InfraConstants.CACHE_TRAFDATA_DIR);
    }           
    
    
    private void deleteDirectory(String filepath)  {
        File f = new File(filepath);
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteDirectory(c.getAbsolutePath());       
            }
        }
        f.delete();
    }

    private void deleteFilesOlderThanNdays(int days, String filepath) {
        File f = new File(filepath);
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteFilesOlderThanNdays(days, c.getAbsolutePath());       
            }
        }
        
        //long purgeTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days * -1);
        long purgeTime = cal.getTimeInMillis(); 

        if (f.lastModified() < purgeTime) {
            if (!f.delete()) {
                //System.err.println("Unable to delete file: " + f);
            } //else System.err.println("delete : " + f);
        }                    
    }
    
    
    public void setup()
    {
        if(this.isLoaded) return;
        infra = new Infra();
        infra.load();    
        sectionManager = new SectionManager();        
        this.isLoaded = true;  
        
//        makeCouple("M35WN27", "Nrnd_86415");
    }
    
    public void setup(String configXmlFilePath)
    {
        if(this.isLoaded) return;
        infra = new Infra();
        infra.load(configXmlFilePath);    
        sectionManager = new SectionManager();        
        this.isLoaded = true;  
        
//        makeCouple("M35WN27", "Nrnd_86415");
    }    

    /**
     * @deprecated 
     * @param meterId meter id (e.g. M35WN27)
     * @param entranceId rnode id of entrance (e.g. Nrnd_86415)
     */
    private void makeCouple(String meterId, String entranceId) {
        RampMeter meter = infra.getMeter(meterId);
        RNode rnode = infra.getRNode(entranceId);
        if(meter == null || rnode == null || rnode.getInfraType() != InfraType.ENTRANCE) return;
        Entrance rn = (Entrance)rnode;
        Detector d = rn.getPassage();
        if(d != null) meter.setPassage(d.getId());
        if(rn.getQueue().size() > 0) {
            String queues = "";
            for(int i=0 ; i<rn.getQueue().size(); i++) {
                if(i>0) queues = "," + rn.getQueue().get(i).getId();
                else queues = rn.getQueue().get(0).getId();
            }
            meter.setQueue(queues);
        }        
        d = rn.getMerge();
        if(d != null)meter.setMerge(d.getId());
        d = rn.getGreen();
        if(d != null) meter.setGreen(d.getId());
        d = rn.getBypass();
        if(d != null) meter.setByPass(d.getId());
    }
    
    public boolean isLoaded() {
        return this.isLoaded;
    }
    
    public void openSectionEditor()
    {
        openSectionEditor(null, false);
    }
    
    public void openSectionEditor(JFrame parent, boolean modal)
    {
        SectionEditor se = new SectionEditor();
        se.setLocationRelativeTo(parent);            
        if(modal && parent != null) ModalFrameUtil.showAsModal(se, parent);
        else se.setVisible(true);
    }
    
    public RampMeter getMeter(Entrance entrance) {
        Collection<RampMeter> meters = infra.getInfraObjects(InfraType.METER);
        Iterator<RampMeter> itr = meters.iterator();
        ArrayList<Detector> queue = entrance.getQueue();
        Detector passage = entrance.getPassage();
        Detector merge = entrance.getMerge();
        Detector bypass = entrance.getBypass();
        Detector green = entrance.getGreen();

        while (itr.hasNext()) {
            RampMeter meter = itr.next();
            if (passage != null && passage.getId().equals(meter.getPassage())) {
                return meter;
            } else if (merge != null && merge.getId().equals(meter.getMerge())) {
                return meter;
            } else if (bypass != null && bypass.getId().equals(meter.getByPass())) {
                return meter;
            } else if (green != null && green.getId().equals(meter.getGreen())) {
                return meter;
            } else if (queue != null) {
                String[] qs = meter.getQueue();
                if (qs == null) {
                    continue;
                }
                for (Detector d : queue) {
                    for (String did : qs) {
                        if (did.equals(d.getId())) {
                            return meter;
                        }
                    }
                }
            }
        }
        return null;
    }    
    

    public Entrance getEntrance(RampMeter m) {
        Collection<Entrance> entrances = infra.getInfraObjects(InfraType.ENTRANCE);
        Iterator<Entrance> itr = entrances.iterator();
        while(itr.hasNext()) {
            Entrance ent = itr.next();
            if(m.equals(getMeter(ent))) return ent;
        }
        return null;
    }    
    
    /**
     * @deprecated Use DistanceUtil class
     * @param _e1
     * @param _e2
     * @param _n1
     * @param _n2
     * @return 
     */
    public static int getDistanceInFeet(int _e1, int _e2, int _n1, int _n2){
        double e1 = _e1;
        double e2 = _e2;
        double n1 = _n1;
        double n2 = _n2;
        return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
    }
    
    /**
     * @deprecated Use DistanceUtil class
     * @param o1
     * @param o2
     * @return 
     */
    public static int getDistanceInFeet(RNode o1, RNode o2)
    {
        double e1 = o1.getEasting();
        double e2 = o2.getEasting();
        double n1 = o1.getNorthing();
        double n2 = o2.getNorthing();
        return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
    }
    
    /**
     * @deprecated Use DistanceUtil class
     * @param o1
     * @param o2
     * @return 
     */
    public static float getDistanceInMile(RNode o1, RNode o2)
    {
        double e1 = o1.getEasting();
        double e2 = o2.getEasting();
        double n1 = o1.getNorthing();
        double n2 = o2.getNorthing();
        return (float)(Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609);
    }
}
