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

package edu.umn.natsrl.ticas.plugin.traveltimeIndexer;

import edu.umn.natsrl.ticas.plugin.datareader.*;
import edu.umn.natsrl.gadget.calendar.IDateChecker;
import edu.umn.natsrl.infra.InfraConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * 
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public final class DateChecker implements IDateChecker {

    private static DateChecker realInstance = new DateChecker();
    
    Vector<Integer> loadedYear = new Vector<Integer>();
    Vector<String> availableDates = new Vector<String>();
    String path = InfraConstants.TRAFFIC_DATA_URL;
    static private boolean hasError = false;
    
    private DateChecker() {
        Calendar c = Calendar.getInstance();
        int thisYear = c.get(Calendar.YEAR);
        loadDates(thisYear);
        //loadDates(new int[]{thisYear, thisYear-1, thisYear-2});
    }

    public static DateChecker getInstance() {
        return realInstance;
    }

    public void loadDates(int year) {
        try {
            if(hasError) return;
            
            URL url = new URL(path + "/" + year);
            loadedYear.add(year);
            String str;
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((str = br.readLine()) != null) {
                availableDates.add(str);
            }
            br.close();
        } catch (UnknownHostException ex) {    
//            ex.printStackTrace();
            System.out.println(this.path);
            hasError = true;
            loadedYear.removeElementAt(loadedYear.size()-1);
//            JOptionPane.showMessageDialog(null, "Check your internet connection");            
        } catch (IOException ex) {
//            ex.printStackTrace();
            hasError = true;
            loadedYear.removeElementAt(loadedYear.size()-1);
//            JOptionPane.showMessageDialog(null, "Fail to connect server : " + this.path);            
//            loadedYear.removeElementAt(loadedYear.size()-1);
//            String url = JOptionPane.showInputDialog(null, "Traffic data location location : ", InfraConstants.TRAFFIC_DATA_URL);
//            if(url != null && !url.isEmpty()) {
//                InfraConstants.TRAFFIC_DATA_URL = url;
//                this.path = url;
//                hasError = false;
//                loadDates(year);
//            }
        } catch (Exception ex) {
            hasError = true;
            loadedYear.removeElementAt(loadedYear.size()-1);
//            JOptionPane.showMessageDialog(null, "Fail to connect server : " + this.path);
        }
    }
    
    public void loadDates(int[] years)
    {
        for(int y : years) loadDates(y);
    }

    @Override
    public boolean isAvailable(Calendar c) {
        String dateString = String.format("%4d%02d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        int year = c.get(Calendar.YEAR);
        if(!loadedYear.contains(year)) {
            loadDates(year);
        }
        return availableDates.contains(dateString);
    }
    
    @Override
    public boolean isAvailable(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return isAvailable(c);
    }    
}
