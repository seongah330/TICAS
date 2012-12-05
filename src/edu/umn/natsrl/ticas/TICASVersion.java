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
/*
 * TICASVersion.java
 *
 * Created on Dec 30, 2011, 1:38:27 PM
 */
package edu.umn.natsrl.ticas;

import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class TICASVersion {
    public static final String version = " 3.275b";    
    public static final boolean ISINIT = true;
    public static String currentVersion;
    
    private static String versionFoler = "config";
    private static String versionFile = "version.config" ;
    private final static String VERSION_LOC = "VERSION";
    
    static TMO tmo = TMO.getInstance();
    
    private static PropertiesWrapper prop = new PropertiesWrapper();
    
    public static void saveConfig(){
        prop.put(VERSION_LOC, version);
        File MainDir = new File(versionFoler);
        if (!MainDir.mkdir() && !MainDir.exists()) {
            return;
        }     
        
        prop.save(versionFoler+"\\"+versionFile);
    }
    public static PropertiesWrapper loadConfig(){
        PropertiesWrapper p = PropertiesWrapper.load(versionFoler+"\\"+versionFile);
        if(p != null){
            prop = p;
            currentVersion = prop.get(VERSION_LOC);
            return prop;
        }else{
            return p;
        }
    }
    public static String toStr(){
        String v = "Update Info";
        if(TICASVersion.currentVersion != null){
            v+= "\nCurrent Version : " + TICASVersion.currentVersion;
        }
        v+="\n-> Update Version : "+TICASVersion.version;
        v+= "\n"+versionInfo();
        return v;
    }

    private static String versionInfo() {
        return "";
    }
    
    public static void CheckVersion() {
        if(TICASVersion.loadConfig() == null ||
                !TICASVersion.version.equals(TICASVersion.currentVersion)){
            if(ISINIT){
                initDataFiles();
            }
            TICASVersion.saveConfig();
            
            JOptionPane.showMessageDialog(null,TICASVersion.toStr());
        }
    }

    public static void initDataFiles() {
        System.out.println("Reset Geo Data.....");
        new File("infra.dat").delete();
        System.out.println("Clear All Cache.....");
        tmo.clearAllCache();
    }
}
