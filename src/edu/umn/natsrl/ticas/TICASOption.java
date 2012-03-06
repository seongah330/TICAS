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
package edu.umn.natsrl.ticas;

import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionInfo;
import edu.umn.natsrl.evaluation.EvaluationOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for storing option of TICAS GUI
 * It stores section, period, interval, output path and all check boxes in TICAS GUI
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class TICASOption implements Serializable {

    /** selected output path */
    private String outputPath;
    
    private boolean isLoaded;
    private int seletedSectionIndex = 0;
    private int seletedIntervalIndex = 0;
    private String trafficDataUrl = InfraConstants.TRAFFIC_DATA_URL;
    private String trafficConfigUrl = InfraConstants.TRAFFIC_CONFIG_URL;
    private int duration = -1;
    private long timestamp = 0;
    
    private EvaluationOption evaluationOption = new EvaluationOption();
    
    public TICASOption() {
        this.timestamp = new Date().getTime();
    }        

    /**
     * Saves option to file
     * @param opt
     * @param filename 
     */
    public static void save(TICASOption opt, String filename) {
        FileOutputStream fileOut = null;
        boolean isLoadedBackup = opt.isLoaded;
        try {            
            opt.isLoaded = false;
            fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(opt);
            out.close();
            fileOut.close();
        } catch (Exception ex) {
            opt.isLoaded = isLoadedBackup;
        } finally {
            try {                
                fileOut.close();
            } catch (IOException ex) {
                Logger.getLogger(TICASOption.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Load option from file
     * @param filename
     * @return 
     */
    public static TICASOption load(String filename) {
        File optFile = new File(filename);
        if(!optFile.exists()) 
        {
            System.err.println("Option file does not be found");
            return new TICASOption();
        }
        
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(optFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            TICASOption ticasOption = (TICASOption) in.readObject();
            EvaluationOption opt = ticasOption.getEvaluationOption();
            ticasOption.isLoaded = true;
            SectionInfo si = opt.getSectionInfo();
            opt.setSection(new Section(si.name, si.description));
            in.close();
            fileIn.close();
            return ticasOption;
        } catch (Exception ex) {
            ex.printStackTrace();
            optFile.delete();
        } finally {
            try {
                fileIn.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return new TICASOption();        
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return this.duration;
    }
    
    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    boolean isLoaded() {
        return this.isLoaded;
    }

    public void setSelectedSectionIndex(int selectedIndex) {
        this.seletedSectionIndex = selectedIndex;
    }

    public int getSeletedSectionIndex() {
        return seletedSectionIndex;
    }

    public void setSelectedIntervalIndex(int selectedIndex) {
        this.seletedIntervalIndex = selectedIndex;
    }
    
    public int getSelectedIntervalIndex() {
        return this.seletedIntervalIndex;
    }

    public String getTrafficDataUrl() {
        return this.trafficDataUrl;
    }

    public String getTrafficConfigUrl() {
        return this.trafficConfigUrl;
    }

    public void setTrafficConfigUrl(String trafficConfigUrl) {
        this.trafficConfigUrl = trafficConfigUrl;
    }

    public void setTrafficDataUrl(String trafficDataUrl) {
        this.trafficDataUrl = trafficDataUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
   
    public EvaluationOption getEvaluationOption()
    {        
        return this.evaluationOption;
    }
}
