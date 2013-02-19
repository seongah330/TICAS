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

package edu.umn.natsrl.infra.infraobjects;

import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.util.DistanceUtil;
import java.util.Properties;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Chongmyung Park
 */
public class Corridor extends InfraObject implements Comparable {
    
    private Direction dir;
    private Vector<RNode> rnodes = new Vector<RNode>();
    private Vector<Station> stations = new Vector<Station>();
    private Vector<Entrance> entrances = new Vector<Entrance>();
    private Vector<Exit> exits = new Vector<Exit>();
    private Vector<String> rnode_list = new Vector<String>();
    private Vector<String> station_list = new Vector<String>();
    private Vector<DMSImpl> dms = new Vector<DMSImpl>();
    private String name = null;

    public Corridor(Element element) {
        super(element);

        NodeList list = element.getElementsByTagName("r_node");
        for(int i=0; i<list.getLength(); i++)
        {
            Element ele = (Element)list.item(i);
            String rid = ele.getAttribute("name");
            rnode_list.add(rid);
            String sid = ele.getAttribute("station_id");
            if(sid != null && !sid.isEmpty()) {
                //System.out.println(element.getAttribute("route") + "("+element.getAttribute("dir")+") > Station " + ele.getAttribute("station_id"));
                this.station_list.add(rid);
            }
        }

        this.setCorridor();
    }

    public Corridor(Properties property) {
        this.property = (Properties)property.clone();
        this.setCorridor();
    }

    private void setCorridor()
    {
        this.dir = Direction.get(this.getProperty("dir"));
        this.id = this.getProperty("route") + " ("+this.dir.toString()+")";
        this.name = this.getProperty("route");
        this.infraType = InfraType.CORRIDOR;
    }

    public void addRNode(RNode n)
    {
        if(n == null) return;
        this.rnodes.add(n);
        if(n.isAvailableStation())this.stations.add((Station)n);
        else if(n.isEntrance()) this.entrances.add((Entrance)n);
        else if(n.isExit()) this.exits.add((Exit)n);
    }
    
    public void addRNodeForEditor(RNode n){
        if(n == null) return;
        int idx = getRnodeIndexbyMile(n,this.rnodes);
        if(idx != 0 && idx == rnodes.size())
            this.rnodes.add(n);
        else
            this.rnodes.add(idx, n);
        System.out.println("RNode List");
        for(RNode r : rnodes){
            System.out.println("-"+r.getId()+"-"+r.getStationId()+"-");
        }
        if(n.isAvailableStation()){
//            getRnodeIndexbyMile(n,stations);
            System.out.println("Add RNode : "+n.getId()+"-"+n.getStationId());
            this.stations.add((Station)n);}
        else if(n.isEntrance()) this.entrances.add((Entrance)n);
        else if(n.isExit()) this.exits.add((Exit)n);
    }

    public void removeRNode(RNode n) {
        this.rnodes.remove(n);
        if(n.isAvailableStation()) this.stations.remove((Station)n);
        if(n.isEntrance()) this.entrances.remove((Entrance)n);
        if(n.isExit()) this.exits.remove((Exit)n);
    }
    
    public void addDMSImpl(DMSImpl dimpl) {
        if(dimpl == null)
            return;
        dms.add(dimpl);
    }

    public Vector<String> getRNodeList() {
        return this.rnode_list;
    }

    public Vector<String> getStationNodeList() {
        return this.station_list;
    }

    public Vector<Entrance> getEntrances() {
        return entrances;
    }

    public Vector<Exit> getExits() {
        return exits;
    }

    public Vector<RNode> getRnodes() {
        return rnodes;
    }

    public Vector<Station> getStations() {
        return stations;
    }
    public Vector<DMSImpl> getDMS(){
        return dms;
    }

    public Station getStation(String station_id)
    {
        for(Station s : this.stations)
            if(station_id.equals(s.getStationId())) return s;
        return null;
    }

    public Station getStation(int station_id)
    {
        for(Station s : this.stations)
            if(station_id == s.getStationIdAsInteger()) return s;
        return null;
    }

    public Station getDownStation()
    {
        return this.stations.lastElement();
    }

    public int compareTo(Object o) {
        return this.id.compareTo(((Corridor)o).id);
    }

    public Direction getDirection() {
        return this.dir;
    }

    public String getRoute() {
        return this.getProperty("route");
    }

    public boolean isCD() {
        return this.id.contains(" CD");
    }
    
    public String getName(){
        return this.name;
    }

    public enum Direction {
        NB, SB, EB, WB, ALL;
        public static Direction get(String d)
        {
            for(Direction dr : Direction.values())
                if(dr.toString().equals(d)) return dr;
            return ALL;
        }
    }
    
    private int getRnodeIndexbyMile(RNode r, Vector<RNode> _rnodes){
        if(_rnodes.isEmpty() || isFirstRNode(r,_rnodes))
            return 0;
        
        RNode preNode = _rnodes.get(0);
        System.out.println("Distance Check!");
        int distanceToFirst = DistanceUtil.getDistanceInFeet(preNode, r);
        int idx = 0;
        for(int i=1;i<_rnodes.size();i++){
            int distance = DistanceUtil.getDistanceInFeet(preNode, _rnodes.get(i));
            System.out.println(_rnodes.get(i)+" : " +distance+",=="+r.getId()+" : "+distanceToFirst);
            if(distance >= distanceToFirst){
                idx = i;
                break;
            }
            else if(i == _rnodes.size()-1)
                idx = i+1;
        }
        
        return idx;
    }
    
    private boolean isFirstRNode(RNode r, Vector<RNode> _rnodes) {
        if(_rnodes.isEmpty())
            return true;
        int mindistance = DistanceUtil.getDistanceInFeet(r, _rnodes.get(0));
        for(int i=1;i<_rnodes.size();i++){
            int dis = DistanceUtil.getDistanceInFeet(r, _rnodes.get(i));
            if(dis < mindistance)
                return false;
        }
        
        return true;
    }
}
