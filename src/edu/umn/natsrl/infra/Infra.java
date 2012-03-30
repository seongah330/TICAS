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

import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.types.InfraType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class Infra implements Serializable {

    private HashMap<String, InfraList> infraObjects = new HashMap<String, InfraList>();
    private String cachePath = InfraConstants.CACHE_DIR;
    private String cacheFile = "infra.dat";
    private InfraConfig config;
    private boolean isLoaded = false;
    
    /**
     * set up infra
     */
    protected void load() {
        load(null);
    }
    
    protected void load(String configXmlPath) {
        if (config == null) {
            config = new InfraConfig();
        }

        long st = new Date().getTime();
                
        // cache operation
        if (!this.loadCache()) {            
            config.loadConfiguration(configXmlPath);
            this.organize_corridor();
            this.set_corridor_to_rnode();
            this.set_rnodes();                        
            this.saveCache();
        }// else if(organizer != null) organizer.setup(this);
        
        this.isLoaded = true;

        long et = new Date().getTime();
        System.out.println("Configuration Loading Time : " + (et - st)/1000 + " second(s)");        
    }


//    /**
//     * delete cache and re-configurate infra
//     */
//    public void reloadInfra() {
//        (new File(cacheFile)).delete();
//        this.isLoaded = false;
//        this.infraObjects.clear();
//        this.load();
//    }


    /**
     * Load cached all infra : corridor, station, detector, meter, entrance, exit
     */
    private boolean loadCache() {

        if (!(new File(cachePath)).exists()) {
            return false;
        }
        
        System.out.print("Loading cached infra structure ...");
        
        try {
            FileInputStream fileIn = new FileInputStream(cacheFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            this.infraObjects = (HashMap<String, InfraList>) in.readObject();
            in.close();
            fileIn.close();

//            this.doAfterLoaded();

            this.isLoaded = true;
            
            System.out.println(" (OK)");
            return true;            
        } catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println(" (Ooops!!)");
            System.out.println("Let's set up infrastructure");
            return false;
        }

//        return true;
    }

    /**
     * Caching all infra : corridor, station, detector, meter, entrance, exit
     */
    private void saveCache() {
        FileOutputStream fileOut = null;
        try {
            File cacheDir = new File(this.cachePath);
            if (!(cacheDir).mkdir() && !cacheDir.exists()) {
                JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + cachePath);
                return;
            }
            System.out.print("Caching infra structure ...");
            fileOut = new FileOutputStream(this.cacheFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.infraObjects);
            out.close();
            fileOut.close();
            System.out.println(" (OK)");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getStackTrace().toString());
        } finally {
            try {
                
                fileOut.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Add r_nodes and stations into corridor object
     */
    private void organize_corridor() {
        System.out.print("Organizing corridors ....................");

        Vector<Corridor> cs = this.getCorridors();

        // allocate r_nodes to corresponding corridor
        for (Corridor co : cs)
        {
            Vector<String> nodeList = co.getRNodeList();
            for (int i = 0; i < nodeList.size(); i++) {
                // retrieve rnode
                String nodeId = nodeList.get(i);
                RNode r_node = this.find(nodeId);
                
                if (r_node == null) {
                    System.out.println("R_NODE : " + nodeId + " is null");
                    continue;
                }

                // add r_node to corridor
                co.addRNode(r_node);
                r_node.setCorridor(co);

            }
        }
        System.out.println(" (OK)");

    }
    
    private void set_corridor_to_rnode()
    {
        System.out.print("Organizing RNodes ....................");
        for (Corridor co : this.getCorridors())
        {
            for(RNode r_node : co.getRnodes())
            {
                r_node.setCorridor(co);
            }
        }
        System.out.println(" (OK)");
    }

    private void set_rnodes()
    {
        for (Corridor co : this.getCorridors())
        {
            for(RNode r_node : co.getRnodes())
            {                
                // set downstream node
                String[] downstream = r_node.getDownstreamName();

                if (downstream != null) {
                    for(int i=0; i<downstream.length; i++) {
                        String ds = downstream[i];
                        RNode drn = this.find(ds);
                        if(drn == null) continue;

                        
                        Corridor downstreamCorridor = drn.getCorridor();
                        if(co.equals(downstreamCorridor)) {
                            r_node.setNextRNodeInSameCorridor(drn);
                        } else {
                            r_node.setDownstreamNodeToOtherCorridor(drn);
                        }
                        
//                        if(downstream.length == 1) {
//                            r_node.setNextRNodeInSameCorridor(drn);
//                        } else {
//                            // first downstream is to other corridor
//                            if(i == 0) {
//                                r_node.setDownstreamNodeToOtherCorridor(drn);
//                            } else {
//                                // second downstream is link to next node in current corridor
//                                r_node.setNextRNodeInSameCorridor(drn);
//                            }
//                        }
                    }
                    
                    if(downstream.length == 1) r_node.setNextRNodeInSameCorridor((RNode)this.find(downstream[0]));
                
                } else {
                        //System.out.println("   - RNode(" + r_node.id + ") : downstream = null");
                }
            }
        }       
    }    
    
    /**
     * Add infra object(corridor, station ..) into list
     * @param o
     */
    public void addInfraObject(InfraObject o) {
        if (o == null) {
            return;
        }
        InfraList node = this.infraObjects.get(o.infraType.getTypeClass().getName());
        
        if (node == null) {
            this.registType(o, o.infraType.getTypeClass());
        } else {
            node.addObject(o);
        }
        
        // TRICK for rnode
        // rnode is parent class of station, entrnace, exit ....
        // but rnode is registered as child class
        // therefore, rnode should be added rnode list
//        if(o.infraType.isRnode()) {
//            InfraList rnodeList = this.infraObjects.get(RNode.class.getName());
//            if(rnodeList == null) {
//                this.registType(o, RNode.class);
//            } else {
//                rnodeList.addObject(o);
//            }
//        }
        
    }
    /**
     * Remove infra object
     * @param o InfraObject
     */
    public void removeInfraObject(InfraObject o) {
        if (o == null) {
            return;
        }
        InfraList node = this.infraObjects.get(o.infraType.getTypeClass().getName());
        if (node == null) {
            return;
        }
        node.removeObject(o);
    }

    /**
     * Remove all loaded data from all detectors
     */
    public void clearTrafficData()
    {
        Collection<Detector> detectors = getInfraObjects(InfraType.DETECTOR);
        for(Detector d : detectors) {
            d.clear();
        }
    }
    
    /**
     * Get InfraObject collections according to INFRA_TYPE
     * @param type INFRA_TYPE { Corridor, Station, Entrance, Exit, RampMeter, Detector }
     * @return Collection<INFRA_TYPE>
     */
    public <T> Collection<T> getInfraObjects(InfraType type) {
        try {
            HashMap<String, T> map = getSet(type);
            if (map == null) {
                return null;
            }
            return map.values();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get InfraObject with id and type
     * @param id InfraObject id
     * @param type INFRA_TYPE { Corridor, Station, Entrance, Exit, RampMeter, Detector }
     * @return (INFRA_TYPE)InfraObject
     */
    public <T> T find(String id, InfraType type) {
        try {
            HashMap<String, T> map = getSet(type);
            return (T) map.get(id);
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * 
     * @param <T> InfraType Class
     * @param id object id (corridor's, station's or detector's ...)
     * @return 
     */
    public <T> T find(String id) {
        try {
            for (InfraType ifr : InfraType.values()) {
                HashMap<String, T> map = getSet(ifr);
                if (map == null) {
                    continue;
                }
                T d = map.get(id);
                if (d != null) {
                    return d;
                }
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get InfraObject map according to Class
     * @param type InfraType (e.g. InfraType.CORRIDOR, InfraType.STATION ...)
     * @see InfraType
     * @return
     */
    private <T> HashMap<String, T> getSet(InfraType type) {
        try {
            HashMap<String, T> map = new HashMap<String, T>();
            Class typeClass = type.getTypeClass();
            if (typeClass == null) {
                return null;
            }
            InfraList o = infraObjects.get(typeClass.getName());
            if (o == null) {
                return null;
            }
            o.getChildren(map);
            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void registType(InfraObject o, Class typeClass) {
        InfraList node = new InfraList(typeClass.getSimpleName());
        node.addObject(o);
        infraObjects.put(typeClass.getName(), node);
    }
       
    
    /**
     * Return corridor
     * @param corridor_id corridor id (e.g. I-35 (NB))
     * @return 
     */
    public Corridor getCorridor(String corridor_id) {
        return this.find(corridor_id, InfraType.CORRIDOR);
    }

    /**
     * Return all corridors
     * @return all corridors
     */
    public Vector<Corridor> getCorridors() {
        HashMap<String, Corridor> cs = this.getSet(InfraType.CORRIDOR);
        Vector<Corridor> corridors = new Vector<Corridor>();
        corridors.addAll(cs.values());
        Collections.sort(corridors);
        return corridors;
    }
    
    /**
     * Return station
     * @param station_id station id (e.g. S42)
     * @return station object
     */
    public Station getStation(String station_id) {
        Collection<Station> stations = this.getInfraObjects(InfraType.STATION);
        Iterator<Station> itr = stations.iterator();
        while (itr.hasNext()) {
            Station station = itr.next();
            if (station_id.equals(station.getStationId())) {
                return station;
            }
        }
        return null;
    }
    
    /**
     * Return detector
     * @param detector_id detector id (e.g. D123)
     * @return detector object
     */
    public Detector getDetector(String detector_id) {
        return this.find(detector_id, InfraType.DETECTOR);
    }

    /**
     * Return ramp meter
     * @param meter_id meter id (e.g. M62E23)
     * @return 
     */
    public RampMeter getMeter(String meter_id) {
        return this.find(meter_id, InfraType.METER);
    }    
    
    /**
     * Return DMS
     * @param dms_id dms id (e.g. L35WN20_1 or V35ES05)
     * @return 
     */
    public DMS getDMS(String dms_id) {
        return this.find(dms_id, InfraType.DMS);
    }
    
    /**
     * Return rnode
     * @param rnode_id  rnode id (e.g. Nrnd_91585)
     * @return 
     */
    public RNode getRNode(String rnode_id) {
//        return this.find(rnode_id);
        InfraObject rn = this.find(rnode_id);
        if(rn != null && rn.getInfraType().isRnode()) return (RNode)rn;
        return null;
    }
    
    /**
     * Return true if is loaded, else false
     * @return true if infra is loaded, or false
     */
    public boolean isLoaded() {
        return isLoaded;
    }

}
