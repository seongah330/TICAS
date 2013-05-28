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
package edu.umn.natsrl.weatherRWIS.site;

import edu.umn.natsrl.weatherRWIS.RWIS;
import edu.umn.natsrl.weatherRWIS.WeatherDB;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SiteInfra {
        private final String FileDir = "RWISInfra.dat";
        
        private HashMap<SiteInfraType, SiteInfraList> objects = new HashMap<SiteInfraType, SiteInfraList>();
        public SiteInfra(){}
        public void load() {
                System.out.println("Load RWIS DATA");
                if(loadCache())
                        return;

                setSite();
                setSiteGroup();
                System.out.println("Organization........RWIS");
                Organization();
                saveCache();
//                        loadCache();
        }
        
        public void saveCache() {
                FileOutputStream fileOut = null;
                try {
                    System.out.print("Caching RWIS Weather infra structure ...");
                    fileOut = new FileOutputStream(FileDir);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(objects);
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
        
        private boolean loadCache() {
                System.out.print("Loading cached RWIS Weather infra structure ...");

                try {
                    FileInputStream fileIn = new FileInputStream(FileDir);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    objects = (HashMap<SiteInfraType, SiteInfraList>) in.readObject();
                    in.close();
                    fileIn.close();

//                    System.out.println(" (OK)");
//                    debug();
                    return true;            
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    System.out.println(" (Ooops!!)");
                    System.out.println("Let's set up infrastructure");
                    return false;
                }
        }
        
        public ArrayList<Site> getSites(){
                ArrayList<Site> sites = new ArrayList<Site>();
                SiteInfraList node = objects.get(SiteInfraType.SITE);
                if(node == null)
                        return sites;
                
                for(SiteInfraObject sobj : node.getObject()){
                        sites.add((Site)sobj);
                }
                
                return sites;
        }
        
        public Site getSite(int id){
                for(Site s : getSites()){
                        if(s.getID() == id){
                                return s;
                        }
                }
                
                return null;
        }
        
        public ArrayList<SiteGroup> getSiteGroups(){
                ArrayList<SiteGroup> sgroup = new ArrayList<SiteGroup>();
                SiteInfraList node = objects.get(SiteInfraType.SITEGROUP);
                if(node == null)
                        return null;
                
                for(SiteInfraObject sobj : node.getObject()){
                        sgroup.add((SiteGroup)sobj);
                }
                
                return sgroup;
        }

        /**
         * get Sites from the RWIS SQL DATABASE
         */
        private void setSite() {
                System.out.println("Loading... RWIS Data from Database");
                WeatherDB wdb = new WeatherDB();
                try {
                        ArrayList<Site> sites = wdb.getSites();
                        for(Site s : sites){
                                AddSiteInfraObjects(s);
                        }
                } catch (SQLException ex) {
                        Logger.getLogger(SiteInfra.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        private void setSiteGroup() {
                WeatherDB wdb = new WeatherDB();
                try{
                        ArrayList<SiteGroup> sgroups = wdb.getSiteGroups();
                        for(SiteGroup sg : sgroups){
                                AddSiteInfraObjects(sg);
                        }
                }catch(SQLException ex){
                        ex.printStackTrace();
                }
        }
        
        /**
         * Organize the Site and SiteGroup
         */
        private void Organization() {
                SiteInfraList glist = objects.get(SiteInfraType.SITEGROUP);
                WeatherDB wdb = new WeatherDB();
                for(SiteGroup sg : getSiteGroups()){
                        try {
                                for(int sid : wdb.getSiteIdsByGroup(sg.getID())){
                                        Site site = getSite(sid);
                                        if(site != null)
                                                sg.addSite(site);
                                }
                        } catch (SQLException ex) {
                                Logger.getLogger(SiteInfra.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
        }
        
        /**
         * Add Objects
         * @param obj 
         */
        private void AddSiteInfraObjects(SiteInfraObject obj){
                SiteInfraType type = obj.getInfraType();
                SiteInfraList node = objects.get(type);
                
                if(node == null){
                        registSiteInfraObject(type, obj);
                }else{
                        node.addObject(obj);
                }
        }

        private void registSiteInfraObject(SiteInfraType type, SiteInfraObject obj) {
                SiteInfraList node = new SiteInfraList(type);
                node.addObject(obj);
                objects.put(type, node);
        }

        private void debug() {
                for(SiteInfraType ty : objects.keySet()){
                        System.out.println(ty.getTag()+ " - "+ty.getTypeClass().getName());
                        printobjects(ty);
                }
        }

        private void printobjects(SiteInfraType ty) {
                ArrayList<SiteInfraObject> obj;
                if(ty == SiteInfraType.SITE){
//                        for(Site s : getSites()){
//                                System.out.println(s.id+" - "+s.name);
//                        }
                }else{
                        for(SiteGroup s : getSiteGroups()){
                                System.out.println(s.id+" - "+s.name);
                                for(Site si : s.getSites()){
                                        System.out.println("   - "+si.getID() + " : "+ si.name);
                                }
                        }
                }
        }
}
