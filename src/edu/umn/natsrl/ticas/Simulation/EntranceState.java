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
package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.TrafficType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class EntranceState extends State {
   Entrance entrance;
   SimMeter meter;

   ArrayList<Double> cumulativeDemand = new ArrayList<Double>();
   ArrayList<Double> cumulativeMergingVolume = new ArrayList<Double>();
   ArrayList<Double> rateHistory = new ArrayList<Double>();        
   HashMap<Integer, Double> segmentDensityHistory = new HashMap<Integer, Double>(); // <dataCount, K>
   SimObjects simObjects;
   double lastDemand = 0;
   double lastVolumeOfRamp = 0;

   public EntranceState(Entrance e, SimObjects _simObject) {
       super(e.getId(), e);
       this.entrance = e;
       type = StateType.ENTRANCE;
       simObjects = _simObject;
   }

   /**
    * Return output in this time interval
    */        
   public double getPassageVolume() {
       return this.lastVolumeOfRamp;
   }

   /**
    * Return output before 'prevStep' time step
    */        
   public double getMergingVolume(int prevStep) {
       int nIdx = cumulativeMergingVolume.size() - prevStep - 1;
       int pIdx = cumulativeMergingVolume.size() - prevStep - 2;
       //System.out.println(cumulativeFlow.size() + ", " + prevStep + ", " + pIdx + ", " + nIdx);
       return cumulativeMergingVolume.get(nIdx) - cumulativeMergingVolume.get(pIdx);
   }        

   /**
    * Return demand in this time interval
    */
   public double getQueueVolume() {
       return this.lastDemand;
   }

   public double getCumulativeDemand(){
       return this.cumulativeDemand.get(cumulativeDemand.size()-1);
   }
   public double getCumulativePassage(){
       return this.cumulativeMergingVolume.get(cumulativeMergingVolume.size()-1);
   }

   public double getRampDensity()
   {
       if(this.cumulativeDemand.isEmpty()) return 0;

       int currentIdx = this.cumulativeDemand.size()-1;            
       double It = this.cumulativeDemand.get(currentIdx);
       double Ot = this.cumulativeMergingVolume.get(currentIdx);

       // ramp length in mile
       double L = this.meter.getMeter().getStorage() / 5280D;
       System.out.println("Storage :" + L);
       // if dual type, length should be double
       if(this.meter.getMeterType() == SimMeter.MeterType.DUAL) {
           L *= 2;
       }                    

       // ramp density : ( cumulative input - cumulative output ) / ramp length
       //                    vehicles in ramp at current time
       double k = (It - Ot) / L;               
//            if(this.meter.getId().equals("M62E35")){
//             System.out.println(this.meter.getId()+" : k="+k+", ( LT("+It+") - Ot("+Ot+") ) / L("+L+")");   
//            }
//            if(k >= 100) {
//                System.out.println(this.meter.getId() + " -> Kramp = " + k + "(i="+It+", o="+Ot+", i-o="+(It-Ot)+", L="+L+")" );                
//            }
       return k;
   }

   /**
    * Calculate demand and output
    */
   public void updateState() {
       if(this.meter == null) return;


       double p_volume = calculateRampVolume();
       double demand = calculateRampDemand();

       double prevCd = 0;
       double prevCq = 0;

       if(this.cumulativeDemand.size()>0) prevCd = this.cumulativeDemand.get(this.cumulativeDemand.size()-1);
       if(this.cumulativeMergingVolume.size()>0) prevCq = this.cumulativeMergingVolume.get(this.cumulativeMergingVolume.size()-1);

       this.cumulativeDemand.add(prevCd + demand);
       this.cumulativeMergingVolume.add(prevCq + p_volume);

       this.lastDemand = demand;                
       this.lastVolumeOfRamp = p_volume;
   }

   /**
    * Return ramp demand
    * @return 
    */
   private double calculateRampDemand() {

       if(this.meter == null) return 0;

       SimDetector[] qDets = this.meter.getQueue();           

       double demand = 0;
       double p_flow = calculateRampVolume();

       // queue detector is ok
       if(qDets != null) {
           for(int i=0; i<qDets.length; i++) {
               double d = (int)simObjects.getDetector(qDets[i].getId()).getData(TrafficType.VOLUME);
               if(d > 0) demand += d;
           }

           return demand;
       }

       return p_flow;                                    
   }

   /**
    * Return ramp flow now
    * @return ramp flow
    */
   private double calculateRampVolume() {
       return calculateRampVolume(0);
   }

   /**
    * Return ramp flow before given prevStep intervals
    * @param prevStep
    * @return ramp flow
    */
   private double calculateRampVolume(int prevStep) {
       if(this.meter == null) return 0;
       SimDetector pDet = this.meter.getPassage();
       SimDetector mDet = this.meter.getMerge();
       SimDetector bpDet = this.meter.getByPass();            

       double p_volume = 0;           

       // passage detector is ok
       if(pDet != null) {
           p_volume = simObjects.getDetector(pDet.getId()).getData(TrafficType.VOLUME, prevStep);
       } else {
           // merge detector is ok
           if(mDet != null) {
               p_volume = simObjects.getDetector(mDet.getId()).getData(TrafficType.VOLUME, prevStep);                      
               // bypass detector is ok
               if(bpDet != null) {
                   p_volume -= simObjects.getDetector(bpDet.getId()).getData(TrafficType.VOLUME, prevStep);
                   if(p_volume < 0) p_volume = 0;
               }                                      
           }   
       }    

       return p_volume;
   }

   public void saveSegmentDensityHistory(int dataCount, double Kt) {
       this.segmentDensityHistory.put(dataCount, Kt);
   }

   public Double getSegmentDensity(int dataCount)
   {
       return segmentDensityHistory.get(dataCount);            
   }

   public String getID(){
       if(hasMeter())
           return meter.getId();
       else
           return null;
   }

   public String getLabel(){
       if(hasMeter())
           return meter.getMeter().getLabel();
       else return null;
   }

   public boolean hasMeter(){
       if(meter != null)
           return true;
       else
           return false;
   }

   public double getCurrentRate(){
       if(meter != null)
           return 0;
       else
           return meter.currentRate;
   }

   public SimMeter getSimMeter(){
       return this.meter;
   }
}
