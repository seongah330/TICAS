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

import edu.umn.natsrl.weatherRWIS.site.SiteInfra;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RWIS {
        private static RWIS realInstance = new RWIS();
        transient private SiteInfra infra;
        private boolean isLoaded = false;
        
        private RWIS(){}
        
        public static RWIS getInstance(){
                return realInstance;
        }
        
        public void setup(){
                if(!checkDB()){
                        JOptionPane.showMessageDialog(null, "Fail : Not Connected to RWIS");
                        return;
                }
                infra = new SiteInfra();
                infra.load();
                isLoaded = true;
        }
        
        public SiteInfra getInfra(){
                return infra;
        }

        private boolean checkDB() {
                WeatherDB db = new WeatherDB();
                return db.isConnected();
        }

        public boolean isLoaded() {
                return isLoaded;
        }
}
