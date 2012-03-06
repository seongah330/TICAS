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
package edu.umn.natsrl.ticas.plugin.rtchart.reader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class DataLoader {

    Date lastDate;

    /**
     * Returns StationNode that includes id, timestamp, volume, occupancy, flow and speed
     * @param doc Document
     * @param stationId  Station ID
     * @return StationNode or NULL
     */
    public StationNode getStationData(Document doc, String stationId) throws Exception
    {
        System.out.println("getStationData : station_id = " + stationId);
        NodeList stations = doc.getElementsByTagName("station");
        int length = stations.getLength();

        NodeList station_data = doc.getElementsByTagName("station_data");
        Node root = station_data.item(0);
        NamedNodeMap rootAttr = root.getAttributes();
        String time_stamp = rootAttr.item(1).getNodeValue();
        // Thu Sep 09 12:06:19 CDT 2010

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        Date timestamp = df.parse(time_stamp.replace("CST", ""));
        this.lastDate = timestamp;
        //System.out.printf("Data on %s\n", timestamp.toString());
        for(int i=0; i<length; i++){
            Element element = (Element)stations.item(i);
            if(element.getAttribute("id").equals(stationId)) {
                try {
                    float volume = Float.parseFloat(element.getElementsByTagName("volume").item(0).getTextContent());
                    float occupancy = Float.parseFloat(element.getElementsByTagName("occupancy").item(0).getTextContent());
                    int flow = Integer.parseInt(element.getElementsByTagName("flow").item(0).getTextContent());
                    int speed = Integer.parseInt(element.getElementsByTagName("speed").item(0).getTextContent());
                    return new StationNode(stationId, timestamp, volume, occupancy, flow, speed);
                } catch(NullPointerException ex) {
                    System.out.println("DataLoader > Null Exception");
                    //ex.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns Document object that includes station.xml
     * @return XML Document including 30s data of all stations
     */
    public Document xmlLoad () throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory docBuilderFactory;
        DocumentBuilder docBuilder;
        URL url;
        Document doc;        
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilder = docBuilderFactory.newDocumentBuilder();
        InputStream in;
        System.out.print("Downloading station traffic data file ...........");
        url = new URL("http://data.dot.state.mn.us/dds/station.xml");
        URLConnection conn = url.openConnection();
        in = conn.getInputStream();
        doc = docBuilder.parse(in);
        System.out.println(" (OK)");

        NodeList station_data = doc.getElementsByTagName("station_data");
        Node root = station_data.item(0);
        NamedNodeMap rootAttr = root.getAttributes();
        String time_stamp = rootAttr.item(1).getNodeValue();

        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        Date timestamp;
        try {
            timestamp = df.parse(time_stamp.replace("CST", ""));
            this.lastDate = timestamp;
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        return doc;
    }

    /**
     * Returns last timestamp
     * @return 
     */
    public Date getLastDate() {
        return lastDate;
    }
    
}
