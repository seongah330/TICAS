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

package edu.umn.natsrl.sfim.comm.dmsxml;

import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DMS XML Helper Class
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class DXHelper {
       
    /**
     * Get string value by tag from xml
     */
    public static String getString(String xml, DXElement tag)
    {
        String v = extract(xml, tag);
        if(v.isEmpty()) return "";
        else return v;
    }

    /**
     * Get long value by tag from xml
     */
    public static int getInt(String xml, DXElement tag) {
        return (int)getLong(xml, tag);
    }    
    
    /**
     * Get long value by tag from xml
     */
    public static long getLong(String xml, DXElement tag) {
        String v = extract(xml, tag);
        if(v.isEmpty()) return 0;
        else return Long.parseLong(v);        
    }
    
    /**
     * Get double value by tag from xml
     */
    public static double getDouble(String xml, DXElement tag) {
        String v = extract(xml, tag);
        if(v.isEmpty()) return 0;
        else return Double.parseDouble(v);        
    }    

    /**
     * Get float value by tag from xml
     */
    public static float getFloat(String xml, DXElement tag) {
        String v = extract(xml, tag);
        if(v.isEmpty()) return 0;
        else return Float.parseFloat(v);        
    }       
    
    /**
     * Get boolean value by tag from xml
     */    
    public static boolean getBoolean(String xml, DXElement tag) {
        String v = extract(xml, tag);
        if(v.isEmpty()) return false;
        else return Boolean.parseBoolean(v);
    }    
    
    /**
     * Get requested message type from xml
     * Message Structure : <DmsXml><TYPE><.....
     */
    public static String getMsgType(String xml)
    {
        String regx = "(\\<"+DXElement.DMSXML+"\\>\\<)(.*?)(\\>\\<)";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(xml);
        if(matcher.find()) {
            return matcher.group(2);
        } else return null;
    }
    
    /**
     * 
     * @param xml requested xml string from IRIS
     * @param tag tag including VSA information
     * @return 
     */
    public static int getVSA(String xml, DXElement tag)
    {
        // extracted string might be like this : [cf255,208,0][tr1,15,80,20][fo6]60[g10,29,43]
        String vsaString = extract(xml, tag).replaceAll("\\[.*?\\]", "");
        if(vsaString.isEmpty()) return 0;
        
        try {
            // return integer parsed from string that all [..] are replaced to blank            
            return Integer.parseInt(vsaString.replaceAll("\\[.*?\\]", ""));
        } catch(Exception ex) {
            SFIMExceptionHandler.handle(ex);
            return 0;
        }
    }
    
    /**
     * Extract value by tag from xml
     */
    private static String extract(String xml, DXElement tag)
    {
        if(xml == null || xml.isEmpty()) return "";
        String regx = "(\\<"+tag+"\\>)(.*?)(\\</"+tag+"\\>)";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(xml);
        if(matcher.find()) {
            return matcher.group(2);
        } else return "";        
    }
 
}
