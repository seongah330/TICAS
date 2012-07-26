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
package edu.umn.natsrl.ticas.plugin.srte;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEStationGroup {
    private Document dom;
    private HashMap<String,Integer> Hstations = new HashMap<String,Integer>();
    private HashMap<Integer,String> Hgroup = new HashMap<Integer,String>();
    private SRTEStation[] stations;
    private SRTEGroup[] group;
    
    /**
     * get GroupID using stationID
     * @param stationID
     * @return 
     */
    public Integer getGroupID(String stationID){
        return Hstations.get(stationID);
    }
    /**
     * get GroupName using stationID
     * @param stationID
     * @return 
     */
    public String getGroupName(String stationID){
        return Hgroup.get(Hstations.get(stationID));
    }
    
    /**
     * get GroupName using groupID
     * @param groupid
     * @return 
     */
    public String getGroupName(int groupid){
        return Hgroup.get(groupid);
    }
    
    public SRTEStation[] getStationList(){
        return stations;
    }
    public SRTEGroup[] getGroupList(){
        return group;
    }
    public void loadfromXML(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            final String resourcesPath = "/edu/umn/natsrl/ticas/resources/SRTEStationGroup.xml";
            InputStream is = this.getClass().getResourceAsStream(resourcesPath);
            dom = db.parse(is);
            System.out.println("XML Load OK");
            
            XMLParse(dom);
        } catch (SAXException ex) {
            Logger.getLogger(SRTEStationGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SRTEStationGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SRTEStationGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }

    private void XMLParse(Document dom) {
        XMLParseGroup(dom,"group",Hgroup,group);
        XMLParseStation(dom,"Station",Hstations,stations);
    }

    /**
     * set Hash Data from XML Elements
     * @param dom documents
     * @param tagName Tag Name
     * @param hm HashMap
     */
    private void XMLParseGroup(Document dom, String tagName, HashMap<Integer, String> hm, SRTEGroup[] groups) {
        NodeList List = dom.getElementsByTagName(tagName);
        groups = new SRTEGroup[List.getLength()];
        for(int temp = 0;temp<List.getLength();temp++){
            Node nNode = List.item(temp);
            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)nNode;
                hm.put(getTagIntValue("id",element), getTagStringValue("name",element));
                groups[temp] = new SRTEGroup(getTagIntValue("id",element), getTagStringValue("name",element));
            }
        }
    }
    
    /**
     * set Hash Data from XML Elements
     * @param dom documents
     * @param tagName Tag Name
     * @param hm HashMap
     */
    private void XMLParseStation(Document dom, String tagName, HashMap<String, Integer> hm,SRTEStation[] stations) {
        NodeList List = dom.getElementsByTagName(tagName);
        stations = new SRTEStation[List.getLength()];
        for(int temp = 0;temp<List.getLength();temp++){
            Node nNode = List.item(temp);
            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element)nNode;
                hm.put(getTagStringValue("id",element), getTagIntValue("groupid",element));
                stations[temp] = new SRTEStation(getTagStringValue("id",element), getTagIntValue("groupid",element));
            }
        }
    }

    private String getTagStringValue(String name, Element element) {
        String nList = element.getAttribute(name);
        return nList;
    }
    private int getTagIntValue(String name, Element element){
        int v = -1;
        try{
            v = Integer.parseInt(element.getAttribute(name));
        }catch(Exception e){
            e.printStackTrace();
        }
        return v;
    }
}
