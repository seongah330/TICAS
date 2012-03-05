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

package edu.umn.natsrl.sfim;

import edu.umn.natsrl.infra.TMO;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Chongmyung Park
 */
public class SFIMMain {

    static TMO tmo = TMO.getInstance();
    static SplashDialog sd = new SplashDialog(null, true);
    
    public static void main(String[] args) {
        
        final SFIMFrame sfim = SFIMFrame.getInstance();
        
        // timer for splash window
        new Timer().schedule(new TimerTask() {
            
            @Override
            public void run() {
                // setup TMO object after 10ms
                tmo.setup();         
                
                // clear all chaches older than 7 days
                tmo.clearAllCache(7);
                
                // run sfim frame
                sfim.setAlwaysOnTop(true);                
                sfim.setVisible(true);                
                
                sd.releaseOutput();
                
                // close splash window dialog
                sd.dispose();
                
                sfim.setAlwaysOnTop(false);
                sfim.init(new SFIMManager());
            }
        }, 10);
        
        // show splash window dialog
        sd.setVisible(true);    
      
    } 

}
