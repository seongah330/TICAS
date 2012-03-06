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

import edu.umn.natsrl.infra.section.SectionInfo;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import java.security.*;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.simobjects.SimObject;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.types.TransitionType;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class Section implements Serializable {

    public static final String K_SECTION_NAME = "section.name"; 
    public static final String K_SECTION_DESC = "section.desc";
    public static final String K_SECTION_RNODES = "section.rnodes" ;
    public static final String K_SECTION_DETECTORS = "section.detectors";
    
    private PropertiesWrapper prop;
    private transient List<RNode> section = new ArrayList<RNode>();    
    private transient TMO tmo;
    private transient Infra infra;
    private final String section_dir = InfraConstants.SECTION_DIR;
    private List<String> rnode_ids = new ArrayList<String>();
    private String name;
    private String description;
    
    /**
     * Construct
     * @param name section name
     * @param desc section description
     */
    public Section(String name, String desc) {
        this.name = name;
        this.description = desc;
        this.tmo = TMO.getInstance();
        this.infra = tmo.getInfra();
        prop =  new PropertiesWrapper();        
    }
    
    /**
     * Save section information after serialization
     * @throws Exception 
     */
    public void save() throws Exception
    {
        File sectionCacheDir = new File(section_dir);
        if (!sectionCacheDir.mkdir() && !sectionCacheDir.exists()) {
            JOptionPane.showMessageDialog(null, "Fail to create cache folder\n" + sectionCacheDir);
            return;
        }        
        
        makeIdList();
        
        prop.put(K_SECTION_NAME, getName());
        prop.put(K_SECTION_DESC, getDescription());
        prop.put(K_SECTION_DETECTORS, getDetectorIds());
        prop.put(K_SECTION_RNODES, getRNodeIds());        
        
        String eName = getCacheFileName(this.name);
        String filename = this.section_dir + File.separator + eName;
        prop.save(filename, "Secion : " + this.name);              
    }
    
    /**
     * Load serialized section information and construct section
     * @param filepath serialized file path
     * @param finder RNode finder
     * @return section created from section information file
     */
    public static Section load(String filepath)
    {
        try {           
            PropertiesWrapper prop = PropertiesWrapper.load(filepath);
            Section s = new Section(prop.get(Section.K_SECTION_NAME), prop.get(Section.K_SECTION_DESC));
            s.tmo = TMO.getInstance();
            s.infra = s.tmo.getInfra();
            s.rnode_ids = prop.getStringList(Section.K_SECTION_RNODES);
            s.constructSection();
            s.organizeStations();
            return s;
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }          
    }
    
    /**
     * Sets up-down stream station and distance information to each station
     */
    public void organizeStations()
    {
        List<RNode> list = getSectionRoute();
        if(list.size() < 1) return;        
        
        // first node must be station
        Station prevStation = (Station)section.get(0);
        
        // distance between exit and entrance
        int distanceExitAndEntrance = 0;               
        
        for(int i=1; i<list.size(); i++)
        {
            RNode rn = list.get(i);

            if(rn.isAvailableStation()) {                
                // casting rnode to station
                Station s = (Station)rn;
                prevStation.setDownstreamStation(this.name, s);
                
                // distance between this station and previous(upstream) station
                int distanceInFeet = TMO.getDistanceInFeet(prevStation, rn);

                // substract distance between exit and entrance from distance
                prevStation.setDistanceToDownstreamStation(this.name, distanceInFeet - distanceExitAndEntrance);
                s.setDistanceToUpstreamStation(this.name, distanceInFeet - distanceExitAndEntrance);
                
                // reset distance between exit and entrance
                distanceExitAndEntrance = 0;
                
                // uddate downstream station
                prevStation = s;
                
            // rn is Exit to other corridor    
            } else if(rn.isExit()){
                
                // if n(i) is not station, n(i) must be exit
                // therefore n(i+1) is entrance
                RNode entrance = list.get(i+1);
                
                // distance between exit and entrance
                distanceExitAndEntrance = TMO.getDistanceInFeet(rn, entrance);                
                
                // already read n(i+1), so increase i
                i++;
            }
        }
    }
    
    /**
     * Return distance to downstream station in feet
     * @param station
     * @return 
     */
    public double getDistanceToDownStation(Station station)
    {
        return station.getDistanceToDownstreamStation(this.name);
    }
    
    /**
     * Make r_node id list for serializing
     * from RNode to String
     */
    private void makeIdList()
    {
        this.rnode_ids.clear();
        for(RNode rn : this.section)
        {
            this.rnode_ids.add(rn.getId());
        }
    }
    

    /**
     * Get routes for displaying
     * @param finder corridor finder
     * @return routes as string
     */
    public String getRoutes() {
        StringBuilder sb = new StringBuilder();
        List<RNode> list = getSectionRoute();
        for(int i=0; i<list.size(); i++)
        {
            RNode rnode = list.get(i);
            if(rnode.isStation()) sb.append(rnode.getStationId());
            else if(rnode.isExit()) {
                sb.append(" [" + rnode.getCorridor().getId()+" ");
            }
            else if(rnode.isEntrance()) {
                Corridor c = infra.getCorridor(rnode.getLabel());
                sb.append(rnode.getCorridor().getId()+"] ");
            }
            if(i < list.size()-1) sb.append(" -> ");
        }
        
        return sb.toString();
    }
    
   private List<RNode> getSectionRoute() {
        List<RNode> list = new ArrayList<RNode>();
        boolean throughExit = false;

        for (int i = 0; i < section.size(); i++) {

            RNode rnode = (RNode) section.get(i);

            // don't add rnode in CD corridor
            if (rnode.getCorridor().isCD()) {
                continue;
            }

            // add available station
            if (rnode.isAvailableStation()) {
                list.add(rnode);
            } else if (rnode.isExit()) {
                // don't add exit that don't be connected to other corridor
                if (rnode.getDownStreamNodeToOtherCorridor() == null) {
                    continue;
                }
                if (i < section.size() - 1) {
                    RNode nextNode = (RNode) section.get(i + 1);
                    if (rnode.getCorridor().equals(nextNode.getCorridor())) {
                        continue;
                    }
                }
                list.add(rnode);
                throughExit = true;
            } else if (rnode.isEntrance()) {
                if (throughExit) {
                    list.add(rnode);
                    throughExit = false;
                }
            }
        }
        return list;
    }      

    /**
     * 
     * @return station list
     */
    public Station[] getStations() {
        return getStations(null);
    }
    
    public Station[] getStations(IDetectorChecker detectorChecker) {
        List<RNode> rnodes = null;
        
        if(detectorChecker != null) rnodes = this.getFilteredSection(detectorChecker);
        else rnodes = this.section;
        
        List<Station> ss = new ArrayList<Station>();
        for(int i=0; i<rnodes.size(); i++)
        {
            RNode rnode = rnodes.get(i);
            if(rnode.isAvailableStation()) ss.add((Station)rnode);
        }
        return ss.toArray(new Station[ss.size()]);
    }    
    
    /**
     * 
     * @return station list as ArrayList
     */
    public List<String> getStationIds() {
        List<String> ids = new ArrayList<String>();
        for(RNode n : section) {
            if(n.isAvailableStation()) ids.add(n.getStationId());            
        }
        return ids;
    }     
    
    public List<String> getRNodeIds() {
        List<String> ids = new ArrayList<String>();
        for(RNode n : section) {
            ids.add(n.getId());
        }
        return ids;
    }  
    
    private List<String> getDetectorIds() {
        List<String> ids = new ArrayList<String>();
        for(RNode rn : this.getRNodes()) {
            for(Detector d : rn.getDetectors()) {
                ids.add(d.getId());
            }
        }                
        return ids;
    }    
    
    /**
     * 
     * @param name section name
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
            //return Base64.encode(name);
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }

    /**
     * Add r_node to section
     * @param node r_node
     */
    public void addRNode(RNode node)
    {
        this.section.add(node);
    }

    /**
     * Add r_nodes to section
     * @param nodes r_node list
     */    
    public void addRNode(RNode[] nodes)
    {
        this.section.addAll(Arrays.asList(nodes));
    }

    /**
     * Add r_nodes to section
     * @param nodes r_node list as ArrayList
     */     
    public void addRNode(List<RNode> nodes)
    {
        this.section.addAll(nodes);
    }

    /**
     * Get section description
     * @return section description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get section name
     * @return section name
     */
    public String getName() {
        return name;
    }

    /**
     * Get r_node list as ArrayList
     * @return r_node list
     */
    public List<RNode> getRNodes() {
        return section;
    }    
    
    public List<RNode> getRNodes(IDetectorChecker detectorChecker) {
        return getFilteredSection(detectorChecker);
    }      
    
    @Override
    public String toString()
    {
        return this.name;
    }

    /**
     * Loads traffic data for all stations in section
     */
    public void loadData(Period period) throws OutOfMemoryError {
        loadData(period, false);
    }    
    
    /**
     * Loads traffic data for all stations in section
     */
    public void loadData(Period period, boolean simmode) throws OutOfMemoryError {
        loadData(period,simmode,null);
    }
    
    public void loadData(Period period, boolean simmode, SimObjects sobj) throws OutOfMemoryError{
        for(RNode s : this.getRNodesWithExitEntrance()) {
            if(sobj == null)
                s.loadData(period, simmode);
            else
                s.loadData(period, simmode,sobj);
        }
    }
    
    /**
     * Returns traffic read data size
     * @return 
     */
    public int getDataSize()
    {
        return this.getStations()[0].getDataSize();
    }

    public RNode[] getRNodesWithExitEntrance()
    {      
        return getRNodesWithExitEntrance(null);
    }
    
    public RNode[] getRNodesWithExitEntrance(IDetectorChecker detectorChecker)
    {      
        List<RNode> fsection = null;
        
        if(detectorChecker != null) fsection = this.getFilteredSection(detectorChecker);
        else fsection = section;
        
        List<RNode> list = new ArrayList<RNode>();
        
        for(RNode rn : fsection) {                
            
            TransitionType ttype = rn.getTransitionType();
            
            if(ttype.isCD()) {
                
                // see Nrnd_1209(CD Exit) -> Nrnd_1210 (Common Entrance) -> Nrnd_86415 (Loop Entrance)
                // Nrnd_86415 is entrance from I-494 to I-35WN
                if(rn.infraType == InfraType.EXIT) {
                    RNode tNode = rn.getDownStreamNodeToOtherCorridor();
                    if(tNode == null) continue;
                    RNode cdEntranceNode = tNode.getDownstreamNodeToSameCorridor();
                    if(cdEntranceNode != null && cdEntranceNode.infraType == InfraType.ENTRANCE) {
                        //System.out.println("ADD cd node : " + cdEntranceNode.id);
                        list.add(cdEntranceNode);
                    }                    
                }
                continue;
            }
            
            list.add(rn);
        }
        
        return list.toArray(new RNode[list.size()]);
    }    
    
    /**
     * @deprecated 
     * @param urn
     * @param drn
     * @return 
     */
    private List<RNode> getBetweenRNodes(RNode urn, RNode drn)
    {
        List<RNode> rnodes = new ArrayList<RNode>();
        RNode rn = urn;
        
        RNode downRNode = null;
        while(rn != drn) {
            
            downRNode = rn.getDownstreamNodeToSameCorridor();
            
            if(downRNode == null) {
                break;
            }
            if(downRNode != drn) {
                TransitionType ttype = downRNode.getTransitionType();
                if(downRNode.isAvailableStation() || ttype.isLeg() || ttype.isLoop()) {
                    rnodes.add(downRNode);
                }
                // see Nrnd_1209(CD Exit) -> Nrnd_1210 (Common Entrance) -> Nrnd_86415 (Loop Entrance)
                // Nrnd_86415 is entrance from I-494 to I-35WN
                if(ttype.isCD() && downRNode.infraType == InfraType.EXIT) {
                    RNode tNode = downRNode.getDownStreamNodeToOtherCorridor();
                    if(tNode == null) continue;
                    RNode cdEntranceNode = tNode.getDownstreamNodeToSameCorridor();
                    if(cdEntranceNode != null && cdEntranceNode.infraType == InfraType.ENTRANCE) {
                        rnodes.add(cdEntranceNode);
                    }                    
                }
            }
            
            rn = downRNode;

        }

        return rnodes;
    }
    
    public int getFeetInSection(Station upstation, Station downstation)
    {
        Station[] stations = this.getStations();
        int distance = 0;
        boolean isStarted = false;
        for(int i=0; i<stations.length-1; i++)
        {
            if(stations[i].equals(upstation)) isStarted = true;
            if(isStarted) {
                distance += stations[i].getDistanceToDownstreamStation(this.name);
                if(stations[i+1].equals(downstation)) break;
            }
        }
        return distance;
    }
   
    public SectionInfo getSectionInfo() {
        makeIdList();
        return new SectionInfo(this.name, this.description, this.rnode_ids);
    }
    
    private List<RNode> getFilteredSection(IDetectorChecker detectorChecker)
    {
        List<RNode> filteredSection = new ArrayList<RNode>();
        for(RNode rn : this.section) {
            if(rn.getDetectors(detectorChecker).length > 0) filteredSection.add(rn);            
        }
        return filteredSection;
    }

    public void constructSection() {
        this.section = new ArrayList<RNode>();
        this.infra = tmo.getInfra();
        for(String rnid : this.rnode_ids) {
            RNode n = infra.getRNode(rnid);
            if(n != null) {
                this.section.add(n);
            }
        }        
    }

    public void setRnodeIds(List<String> rnode_ids) {
        this.rnode_ids = rnode_ids;
        this.constructSection();
        this.organizeStations();        
    }

    
    @Override
    public Section clone() {
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            output = new ObjectOutputStream(byteOutput);
            output.writeObject(this);
            output.flush();
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(byteOutput.toByteArray());
            input = new ObjectInputStream(bin);

            Section s = (Section) input.readObject();
            s.tmo = TMO.getInstance();
            s.constructSection();
            s.organizeStations();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ex) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }
        return null;
    }
    
    
}
