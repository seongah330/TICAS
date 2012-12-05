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

import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.DMS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author Chongmyung Park
 */
public class InfraConfig implements Serializable {
    
    private TMO tmo = TMO.getInstance();
    private Infra infra;
    
    public InfraConfig() {
        infra = tmo.getInfra();
        (new File(InfraConstants.CACHE_DIR)).mkdir();
        (new File(InfraConstants.CACHE_DETDATA_DIR)).mkdir();
        (new File(InfraConstants.CACHE_DETFAIL_DIR)).mkdir();
        (new File(InfraConstants.CACHE_TRAFDATA_DIR)).mkdir();
        (new File(InfraConstants.SECTION_DIR)).mkdir();        
    }

    public void loadConfiguration(String xmlFilePath)
    {
        if(xmlFilePath == null) {
            loadConfiguration();
            return;
        }
        
        try {
            if(infra.isLoaded()) return;
            System.out.print("Reading local config file ...........");            
            loadConfiguration(new FileInputStream(xmlFilePath), false);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fail to read local config file. It will be terminated!!");            
            System.exit(0);
        }        
    }
    
    public void loadConfiguration() {
        try {
            if(infra.isLoaded()) return;
            URL url;
            InputStream in;
            System.out.print("Downloading remote config file ...........");
            url = new URL(InfraConstants.TRAFFIC_CONFIG_URL);
            URLConnection conn = url.openConnection();
            in = conn.getInputStream();
            loadConfiguration(in, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fail to read remote config file. It will be terminated!!");            
            System.exit(0);
        }
    }
    
    private void loadConfiguration(InputStream is, boolean isZip)
    {
        try {
            if(infra.isLoaded()) return;
            
            DocumentBuilderFactory docBuilderFactory;
            DocumentBuilder docBuilder;
            Document doc;
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docBuilderFactory.newDocumentBuilder();
            
            if(isZip) doc = docBuilder.parse(new GZIPInputStream(is));
            else doc = docBuilder.parse(is);
            
            System.out.println(" (OK)");
            
            loadDMSFromConfigXML(doc.getElementsByTagName("dms"));
            loadDetectorsFromConfigXML(doc.getElementsByTagName("detector"));
            loadMetersFromConfigXML(doc.getElementsByTagName("meter"));
            loadRNodesFromConfigXML(doc.getElementsByTagName("r_node"));
            loadCorridorsFromConfigXML(doc.getElementsByTagName("corridor"));
        } catch (FileNotFoundException ex) {
            String url = JOptionPane.showInputDialog(null, "\"tms_config.xml.gz\" file location : ", InfraConstants.TRAFFIC_CONFIG_URL);
            if(url.isEmpty()) System.exit(0);
            else {
                InfraConstants.TRAFFIC_CONFIG_URL = url;
                loadConfiguration();
            }            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fail to read config file. It will be terminated!!");            
            System.exit(0);
        }          
    }
    
    private void loadDMSFromConfigXML(NodeList dmslist){
        System.out.print("Loading DMSs ...................... ");
        for(int i=0;i<dmslist.getLength();i++){
            DMS dms = new DMS((Element)dmslist.item(i));
            infra.addInfraObject(dms);
        }
        System.out.println(" (OK)");
    }

    private void loadDetectorsFromConfigXML(NodeList detectorList) {
        System.out.print("Loading detectors ...................... ");
        for(int i=0; i<detectorList.getLength(); i++)
        {
            Detector detector = new Detector((Element) detectorList.item(i));
            infra.addInfraObject(detector);
        }    
        System.out.println(" (OK)");
    }

    private void loadMetersFromConfigXML(NodeList meterList) {
        System.out.print("Loading meters ...................... ");
        for(int i=0; i<meterList.getLength(); i++)
        {
            RampMeter meter = new RampMeter((Element) meterList.item(i));
            infra.addInfraObject(meter);
        }
        System.out.println(" (OK)");
    }

    private void loadRNodesFromConfigXML(NodeList rnodeList) {
        System.out.print("Loading rnodes ...................... ");
        System.out.println();
        for(int i=0; i<rnodeList.getLength(); i++)
        {
//            System.out.print("rnode["+i+"] : "); //debug
            RNode rnode = RNode.create((Element) rnodeList.item(i));
            if(rnode == null) continue;
            //System.out.println("   adding : " + rnode.infraType.toString() + ", " + rnode.id);
            infra.addInfraObject(rnode);
        }
        System.out.println(" (OK)");
    }

    private void loadCorridorsFromConfigXML(NodeList corridorList) {
        System.out.print("Loading cooridors ...................... ");
        for(int i=0; i<corridorList.getLength(); i++)
        {
            Corridor corridor = new Corridor((Element) corridorList.item(i));
            infra.addInfraObject(corridor);
        }
        System.out.println(" (OK)");
    }
    
}
