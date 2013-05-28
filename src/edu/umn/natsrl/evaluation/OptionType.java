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
package edu.umn.natsrl.evaluation;

/**
 *
 * @author Chongmyung Park
 */
public enum OptionType {
    
    STATION_SPEED(0, StationSpeed.class),
    STATION_TOTAL_FLOW(0, StationTotalFlow.class),
    STATION_AVG_LANE_FLOW(0, StationAverageLaneFlow.class),
    STATION_DENSITY(0, StationDensity.class),
    STATION_OCCUPANCY(0, StationOccupancy.class),
    STATION_VOLUME(0, StationVolume.class),
    STATION_ACCEL(0, StationAccel.class),
    
    DETECTOR_OPT_WITH_LANECONFIG(100),
    DETECTOR_OPT_WITHOUT_LANECONFIG(200, DetectorWithoutLaneConfig.class),    
    
    DETECTOR_SPEED(1),
    DETECTOR_FLOW(2),
    DETECTOR_DENSITY(3),
    DETECTOR_OCCUPANCY(4),
    
    DETECTOR_WITH_LANECONFIG_SPEED(101, DetectorWithLaneConfigSpeed.class),
    DETECTOR_WITH_LANECONFIG_FLOW(102, DetectorWithLaneConfigFlow.class),
    DETECTOR_WITH_LANECONFIG_DENSITY(103, DetectorWithLaneConfigDensity.class),
    DETECTOR_WITH_LANECONFIG_OCCUPANCY(104, DetectorWithLaneConfigOccupancy.class),
    DETECTOR_WITHOUT_LANECONFIG_SPEED(0),
    DETECTOR_WITHOUT_LANECONFIG_FLOW(0),
    DETECTOR_WITHOUT_LANECONFIG_DENSITY(0),    
    DETECTOR_WITHOUT_LANECONFIG_OCCUPANCY(0),    
    
    EVAL_VMT(0, VMT.class),
    EVAL_LVMT(0, LVMT.class),
    EVAL_VHT(0, VHT.class),
    EVAL_DVH(0, DVH.class),
    EVAL_MRF(0, MRF.class),   // mainlane and ramp flow rates
    EVAL_TT(0, TT.class),
    EVAL_TT_REALTIME(0, TT_RealTime.class),
    EVAL_CM(0, CM.class),
    EVAL_CMH(0, CMH.class),
    EVAL_SV(0, SV.class),
    
    RWIS_WEATHER(0,RWISWeather.class),
        
    OUT_EXCEL(-1),
    OUT_CSV(-1),
    OUT_CONTOUR(-1),
    
    WITHOUT_HOV(-1),
    ONLY_HOV(-1),
    WITHOUT_WAVETRONICS(-1),
    WITHOUT_AUXLANE(-1),
    WITHOUT_VIRTUAL_STATIONS(-1),
    
    OPEN_CONTOUR(-1),
    FIXING_MISSING_DATA_ZERO(-1),
    FIXING_MISSING_DATA(-1),
    USE_INPUT_FLOW_FOR_MRF(-1)
    ;
    
    private int code = 0;
    private Class evaluationClass;
    private EvaluationCheckBox checkbox;

    private OptionType(int code) {
        this.code = code;
    }

    private OptionType(int code, Class targetClass) {
        this(code);
        this.evaluationClass = targetClass;
    }
    

    public boolean isDetectorOption()
    {
        return (this == DETECTOR_SPEED || this == DETECTOR_FLOW || this == DETECTOR_DENSITY || this == DETECTOR_OCCUPANCY);
    }
    
    /**
     * Returns OptionType for detector data
     * @param opt
     * @param detectorDataOption
     * @return 
     */
    public OptionType getCombination(OptionType opt, OptionType detectorDataOption)
    {
        if(!detectorDataOption.isDetectorOption()) return null;
        int combiCode = OptionType.getOptionByCode(opt.code).code + OptionType.getOptionByCode(detectorDataOption.code).code;
        return OptionType.getOptionByCode(combiCode);        
    }
    
    /**
     * Returns OptionType by code value
     * @param code
     * @return 
     */
    private static OptionType getOptionByCode(int code)
    {
        for(OptionType ot : OptionType.values()) {
            if(ot.code == code) return ot;
        }
        
        return null;
    }

    /**
     * Returns evaluation class for this option
     * @return 
     */
    public Class getTargetClass() {
        return evaluationClass;
    }

    public int getCode() {
        return code;
    }

    /**
     * Sets EvaluationCheckBox for this
     * @param chkbox 
     */
    public void setCheckBox(EvaluationCheckBox chkbox) {
        this.checkbox = chkbox;
    }

    /**
     * Returns EvaluationCheckBox for this 
     * @return 
     */
    public EvaluationCheckBox getCheckBox() {
        return this.checkbox;
    }
    
}
