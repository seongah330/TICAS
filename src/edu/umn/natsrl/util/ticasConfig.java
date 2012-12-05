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
package edu.umn.natsrl.util;

import java.io.File;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class ticasConfig {
    protected static PropertiesWrapper prop = new PropertiesWrapper();
    protected static String Folder = "config";
    
    protected static boolean saveConfig(String filename){
        File MainDir = new File(Folder);
        if (!MainDir.mkdir() && !MainDir.exists()) {
            return false;
        }     
        prop.save(Folder+"\\"+filename);
        return true;
    }
    
    protected static PropertiesWrapper loadConfig(String filename){
        PropertiesWrapper p = PropertiesWrapper.load(Folder+"\\"+filename);
        return p;
    }
}
