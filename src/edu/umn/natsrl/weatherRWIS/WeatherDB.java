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
package edu.umn.natsrl.weatherRWIS;

import edu.umn.natsrl.util.MSSQLDBConnector;
import edu.umn.natsrl.weatherRWIS.site.*;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class WeatherDB extends MSSQLDBConnector{
        
        public WeatherDB(){
                super("sa","natsrl1!","rwis","131.212.105.85","1433");
        }
        
        public void testserver() throws SQLException{
                ResultSet res = statement.executeQuery("select * from Site");
                
                while(res.next()){
                        System.out.println("Name : "+res.getString("Name"));
                }
        }
        
        public ArrayList<Site> getSites() throws SQLException{
                ArrayList<Site> sites = new ArrayList<Site>();
                ResultSet res = statement.executeQuery("select * from Site");
                while(res.next()){
                        int siteid = res.getInt("Siteid");
                        String name = res.getString("Name");
                        double lat = res.getDouble("lat");
                        double lon = res.getDouble("Lon");
                        SiteType stype = SiteType.getType(res.getString("SiteType"));
                        //int _id, String _name, double _lat, double _lon, SiteType _stype
                        sites.add(new Site(siteid,name,lat,lon,stype));
                }
                return sites;
        }
        
        public ArrayList<SiteGroup> getSiteGroups() throws SQLException{
                ArrayList<SiteGroup> sitegroups = new ArrayList<SiteGroup>();
                ResultSet res = statement.executeQuery("select * from SiteGroup");
                while(res.next()){
                        int gid = res.getInt("Groupid");
                        String name = res.getString("Name");
                        SiteGroupType stype = SiteGroupType.getType(res.getString("SiteGroupType"));
                        String desc = res.getString("Description");
                        //SiteGroup(int _id, String _name, SiteGroupType _gtype, String _disc){
                        sitegroups.add(new SiteGroup(gid,name,stype,desc));
                }
                return sitegroups;
        }

        public ArrayList<Integer> getSiteIdsByGroup(int id) throws SQLException {
                ArrayList<Integer> sitelist = new ArrayList<Integer>();
                ResultSet res = statement.executeQuery("select * from SiteGrouping where Groupid = "+id);
                while(res.next()){
                        int siteid = res.getInt("Siteid");
                        sitelist.add(siteid);
                }
                return sitelist;
        }
}
