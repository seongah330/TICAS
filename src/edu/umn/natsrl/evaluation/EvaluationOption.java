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

/*
 * EvaulationOption.java
 *
 * Created on Jul 24, 2011, 1:51:27 PM
 */
package edu.umn.natsrl.evaluation;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.section.SectionInfo;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public class EvaluationOption implements Serializable {

    /** selected section */
    private transient Section section;
    private SectionInfo sectionInfo;

    /** selected period, it may be multiple period because user can select multiple dates */
    private Vector<Period> period = new Vector<Period>();
    
    /** selected interval */
    private Interval interval;
    
    /** selected output direction */
    private OutputDirection outputDirection = OutputDirection.TO_BOTTOM;
    
    /** congestion threshold speed */
    private int congestionThresholdSpeed = 45;
    
    /** lane capacity */
    private int laneCapacity = 2200;
    
    /** critical density */
    private int criticalDensity = 40;
    
    /** checked options */
    private HashMap<String, OptionType> options = new HashMap<String, OptionType>();
    
    private transient HashMap<ContourType, ContourPanel> contourPanels = new HashMap<ContourType, ContourPanel>();    
    private HashMap<ContourType, ContourSetting> contourSettings = new HashMap<ContourType, ContourSetting>();    
    
    private boolean isLoaded;
    
    private int start_hour = 7;
    private int end_hour = 8;
    private int start_min = 0;
    private int end_min = 0;
    
    private transient IDetectorChecker detectorChecker;
    private boolean simulationMode = false;

    public EvaluationOption() {
        
    }
    
    /**
     * Add option to hashmap
     * @param optionType 
     */
    public void addOption(OptionType optionType) {
        this.options.put(optionType.name(), optionType);
    }

    /**
     * Check if it has option
     * @param optionType
     * @return true if it has given option, else false
     */
    public boolean hasOption(OptionType optionType) {
        return this.options.containsKey(optionType.name());
    }    
    
    /////////////////////////
    // Getter and Setter
    /////////////////////////
    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
        for(Period p : this.period) p.setInterval(interval.second);
    }

    public Period[] getPeriods() {
        return period.toArray(new Period[period.size()]);
    }
    
    public void setPeriods(Period[] periods)
    {
        setPeriods(Arrays.asList(periods));
    }
    
    public void setPeriods(List<Period> periods)
    {
        this.period.clear();
        this.period.addAll(periods);
    }    

    public void addPeriod(Period period) {
        this.period.add(period);
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        if(section == null) return;
        this.section = section;
        this.sectionInfo = section.getSectionInfo();
    }

    public OptionType[] getOptions() {
        return options.values().toArray(new OptionType[options.size()]);
    }

    public OptionType[] getEvaluationOptions() {
        Vector<OptionType> opts = new Vector<OptionType>();

        for (OptionType opt : options.values()) {
            if (opt.getCode() >= 0) {
                opts.add(opt);
            }
        }
        return opts.toArray(new OptionType[opts.size()]);
    }

    public OutputDirection getOutputDirection() {
        return outputDirection;
    }

    public void setOutputDirection(OutputDirection outputDirection) {
        this.outputDirection = outputDirection;
    }

    public void removeOption(OptionType optionType) {
        this.options.remove(optionType.name());
    }

    public int getCongestionThresholdSpeed() {
        return congestionThresholdSpeed;
    }

    public void setCongestionThresholdSpeed(int congestionThresholdSpeed) {
        this.congestionThresholdSpeed = congestionThresholdSpeed;
    }

    public int getCriticalDensity() {
        return criticalDensity;
    }

    public void setCriticalDensity(int criticalDensity) {
        this.criticalDensity = criticalDensity;
    }

    public int getLaneCapacity() {
        return laneCapacity;
    }

    public void setLaneCapacity(int laneCapacity) {
        this.laneCapacity = laneCapacity;
    }

    public void addContourPanel(ContourType cType, ContourPanel panel)
    {
        this.contourPanels.put(cType, panel);
        this.contourSettings.put(cType, panel.getContourSetting());
    }
    
    public ContourPanel getContourPanel(ContourType cType)
    {
        return this.contourPanels.get(cType);
    }
    
    public ContourSetting getContourSetting(ContourType cType)
    {
        return this.contourSettings.get(cType);
    }    
    
    boolean isLoaded() {
        return this.isLoaded;
    }


    public void setStartEndTime(int start_hour, int start_min, int end_hour, int end_min) {
        this.start_hour = start_hour;
        this.end_hour = end_hour;
        this.start_min = start_min;
        this.end_min = end_min;
    }

    public int getEndHour() {
        return end_hour;
    }

    public int getEndMin() {
        return end_min;
    }

    public int getStartHour() {
        return start_hour;
    }

    public int getStartMin() {
        return start_min;
    }

    public void setDetectChecker(IDetectorChecker detectorChecker) {
        this.detectorChecker = detectorChecker;
    }

    public IDetectorChecker getDetectorChecker() {
        return detectorChecker;
    }

    public boolean isSimulationMode() {
        return simulationMode;
    }

    public void setSimulationMode(boolean simulationMode) {
        this.simulationMode = simulationMode;
    }

    public SectionInfo getSectionInfo() {
        return sectionInfo;
    }
    
}
