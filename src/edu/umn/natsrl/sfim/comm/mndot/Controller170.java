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

package edu.umn.natsrl.sfim.comm.mndot;

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimMeter.MeterType;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.Controller;
import edu.umn.natsrl.sfim.comm.InfoController;
import edu.umn.natsrl.sfim.comm.Responser;
import edu.umn.natsrl.vissimcom.MeterLight;
import edu.umn.natsrl.vissimcom.IStepListener;
import edu.umn.natsrl.vissimcom.wrapper.ISignalGroup;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Chongmyung Park
 */
public class Controller170 extends Controller implements IStepListener {
    
    private HashMap<String, ISignalGroup> signalGroupTable;
    private SimMeter[] meters = new SimMeter[SFIMConfig.RAMPMETER_PIN_RANGE];
    private SimDetector[] detectors = new SimDetector[SFIMConfig.DETECTOR_PIN_RANGE];
    private int[] detectorMap = new int[SFIMConfig.DETECTOR_PIN_RANGE];
    private boolean hasDetectorMap = false;
    private int dataReadIdx = 0;
    
    /**
     * constructor
     * @param drop  drop number of controller in comm_link
     */
    public Controller170(int drop) {
        super(drop);
    }

    /**
     * create device(detectors and meters) according to information of comm_link in IRIS
     * @param ci controller information
     */
    private void createDevice(InfoController ci) {
        Infra infra = tmo.getInfra();
        for(int i=0; i<ci.PINs.size(); i++)
        {
            String device_name = ci.IOs.get(i);
            int pin = ci.PINs.get(i);
            
//            if(pin > SFIMConfig.RAMPMETER_PIN_RANGE_END && pin <= SFIMConfig.DETECTOR_PIN_RANGE_END) device_name = "D"+device_name;
            if(pin > SFIMConfig.RAMPMETER_PIN_RANGE_END && pin <= SFIMConfig.DETECTOR_PIN_RANGE_END) device_name = ""+device_name;
            
            InfraObject o = infra.find(device_name);
            
            if(o == null) {
                System.out.println("    - Can not find : commlink="+ci.commlink.name + ", ctrl=" + ci.name + ", device=" + device_name +", pin=" + pin);
                continue;
            }
            
            manager.addInfraObject(o);            
            
            if(o.getInfraType().isDetector()) {
                SimDetector det = simObjects.getDetector(o.getId());
                det.setStartTime(manager.getStartTime());
                this.detectors[pin-SFIMConfig.DETECTOR_PIN_RANGE_START] = det;
                
            } else if(o.getInfraType().isMeter()) {
                RampMeter meter = (RampMeter)o;                
                SimMeter smeter = simObjects.getMeter(meter.getId());
                       
                if(this.signalGroupTable == null) System.out.println("Meter Table is null");
                if(device_name == null)  System.out.println("Device name is null");

                // single
                if(this.signalGroupTable.containsKey(device_name))
                {
                    smeter.setMeterType(MeterType.SINGLE);
                // not single ( 2 device per 1 rampmeter)
                } else if(this.signalGroupTable.containsKey(device_name + "_L") && this.signalGroupTable.containsKey(device_name + "_R")) {
                    smeter.setMeterType(MeterType.DUAL);                   
                // if rampmeter doesn't exist in VISSIM, disable rampmeter
                } else {
                    smeter.setEnabled(false);
                }
                this.meters[pin-SFIMConfig.RAMPMETER_PIN_RANGE_START] = smeter; 
            }
        }
    }

    @Override
    /**
     * routine executed every simulation step by VISSIMController
     */
    public void runStepListener() {
        if(!SFIMConfig.USE_METERING) return;
        for(SimMeter meter : meters)
        {
            updateRampmeter(meter);
        }
    }

    /**
     * update lamp of ramp meter every simulation step
     * @param meter
     */
    private void updateRampmeter(SimMeter meter) {
        if (meter == null || !meter.isEnabled()) return;        
        meter.setCurrentSimTime(this.vissimController.getCurrentTime());
        meter.updateLamp(this.vissimController);
    }

//    @Override
    /**
     * routine executed every 30s by VISSIMController
     */
//    public void addTraffic(int[] detectorIds, int[] volumes, int[] flows, float[] speeds, float[] densities) {
//        if(!this.hasDetectorMap) makeDetectorMap(detectorIds);        
//        for(int i=0; i<this.detectors.length; i++)
//        {
//            if(this.detectors[i] == null) continue;
//            int idx = this.detectorMap[i];
//            if(idx < 0) continue;
//            this.detectors[i].addVolume(volumes[idx]);
//            this.detectors[i].addFlow(flows[idx]);
//            this.detectors[i].addSpeed(speeds[idx]);
//            this.detectors[i].addDensity(densities[idx]);
//            this.detectors[i].afterScanOccupancy();
//        }
//    }
    
    /*
     * we need map data between index of this.detectors array and detector IDs from VISSIMController
     * because detector data from VISSIM include all detector data of VISSIM
     */
    private void makeDetectorMap(int[] detectorIds) {
        Arrays.fill(detectorMap, -1);
        for(int i=0; i<this.detectors.length; i++)
        {
            if(this.detectors[i] == null) continue;
            String id = this.detectors[i].getId();            
            for(int j=0; j<detectorIds.length; j++) {
//                if(id.equals("D"+detectorIds[j])) {
                if(id.equals(""+detectorIds[j])) {
                    detectorMap[i] = j;
                    break;
                } 
            }
        }
        this.hasDetectorMap = true;
    }

    @Override
    /**
     * set this controller to responser
     * and execute doResponse method of responser
     */
    public void doResponse(Responser rsp) {
        rsp.setController(this);
        rsp.doResponse();
    }
    
    @Override
    /**
     * @param o {controller information, vissim controller, metering plan id}) from SFIMManager
     */
    public void set(Object[] o) {
        InfoController ci = this.getInfo(o);
        this.vissimController = this.getVC(o);
        if(SFIMConfig.USE_METERING) this.meterInitialize();
        this.signalGroupTable = this.vissimController.getMeterTable();        
        this.createDevice(ci);                
        
        // update metering every 1step (0.1sec)
        this.vissimController.addStepListener(1, this);
    }
    
    /**
     * set lamp of meter to green
     * @param vc VISSIMController
     */
    private void meterInitialize()
    {
        
        this.vissimController.initializeMetering(SFIMConfig.SC_NUM_FOR_METERING);
        
        for(SimMeter meter : this.meters)
        {
            if(meter == null) continue;
            try {
                // single meter
                if(meter.getMeterType() == MeterType.SINGLE) this.vissimController.setMeterStatus(meter.getId(), MeterLight.GREEN);
                else {
                  // pair meter
                  this.vissimController.setMeterStatus(meter.getId()+"_L", MeterLight.GREEN);
                  this.vissimController.setMeterStatus(meter.getId()+"_R", MeterLight.GREEN);
                }                
            } catch (Exception ex) {
                SFIMExceptionHandler.handle(ex);
            }
        }
    }    
    
    /**
     * @return simulation detectors
     */
    public SimDetector[] getDetectors() {
        return this.detectors;
    }

    /**
     * @return simulation meters
     */
    public SimMeter[] getMeters() {
        return this.meters;
    }

}
