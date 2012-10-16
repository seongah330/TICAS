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

package edu.umn.natsrl.vissimctrl;

import edu.umn.natsrl.util.FileHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Chongmyung Park
 */
public class VISSIMHelper {
    
    /**
     * Loads desired speed decision list from VISSIM case file
     * @param contents
     * @return 
     */    
    public static ArrayList<String> loadDesiredSpeedDecisionsFromCasefile(String caseFile) {

        String contents;
        try {
            contents = FileHelper.readTextFile(caseFile);
        } catch (IOException ex) {
            return null;
        }
        
        if (contents == null || contents.isEmpty()) {
            return null;
        }

        ArrayList<String> dsds = new ArrayList<String>();

        // get detector id from text
        String regx = "DESIRED_SPEED_DECISION ([0-9]+) NAME \"(.*?)\" LABEL";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(contents);
        while (matcher.find()) {
            String dname = matcher.group(2).trim();
            if (!dname.isEmpty()) {
                dsds.add(dname);
            }
        }

        return dsds;
    }

    /**
     * Loads signal group list from VISSIM case file
     * @param contents
     * @return 
     */
    public static ArrayList<String> loadSignalGroupsFromCasefile(String caseFile) {
        
        String contents;
        try {
            contents = FileHelper.readTextFile(caseFile);
        } catch (IOException ex) {
            return null;
        }
        
        if (contents == null || contents.isEmpty()) {
            return null;
        }

        ArrayList<String> sgs = new ArrayList<String>();

        // get detector id from text
        String regx = "SIGNAL_GROUP ([0-9]+)  NAME \"(.*?)\"";
        Pattern p = Pattern.compile(regx);
        Matcher matcher = p.matcher(contents);
        while (matcher.find()) {
            String dname = matcher.group(2).trim();
            if (!dname.isEmpty()) {
                sgs.add(dname);
            }
        }

        return sgs;
    }    
    
    
    
}
