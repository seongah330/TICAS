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

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.simobjects.SimStation;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.util.DistanceUtil;
import java.util.Vector;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
/**
* State class that represents station
*/
public class StationState extends State {
   SimObjects simobjects;
   SimStation simstation;
   Section section;
   //EntranceState associatedEntrance;
//   int stationIdx = 0;
   double aggregatedDensity = 0;
   double aggregatedSpeed = 0;

   protected int MOVING_U_AVG_WINDOW = 2;
   protected int MOVING_K_AVG_WINDOW = 2;

   protected int MAX_SPEED_ALPHA = 10;
   protected int lastSpeedAggCount = 0;
   protected double NEARBY_DISTANCE = 2000;  // 2000 feet

   protected Vector<Double> RollingSpeed = new Vector<Double>();

   public IDetectorChecker dc = SectionHelper.dc;
   private StationState upstreamStationState;
   private StationState downstreamStationState;
   private int distanceToUpstreamStation = -1;
   private int distanceToDownstreamStation = -1;

   public StationState(Station s, Section _sec,SimObjects simObjects) {            
       super(s.getStationId(), s);
       section = _sec;
       this.simstation = simObjects.getStation(s.getStationId());
       type = StateType.STATION;
       simobjects = simObjects;
   }

   //Update State
   public void updateState() {
       updateRollingSamples();
       updateRollingSpeed(getSpeed());
   }

   public SimStation getSimStation(){
       return simstation;
   }

   public Station getStation(){
       return simstation.getStation();
   }
   
   public Section getSection(){
       return this.section;
   }
   public SimObjects getSimObject(){
       return this.simobjects;
   }
   
   /**
    * Return aggregated density
    * @return 
    */
   public double getAggregatedDensity() {
       return getAggregatedDensity(0);
   }        
   public double getAggregatedDensity(int prevStep){
       return getAverageDensity(prevStep,MOVING_K_AVG_WINDOW);
   }
   /**
    * Returns aggregated density before given prevStep time step
    * @return 
    */
   public double getAverageDensity(int prevStep,int howManySteps) {
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           double k = simstation.getData(dc, TrafficType.DENSITY, prevStep+i);
           //debug
//                double k = station.getData(dc, TrafficType.DENSITY, prevStep+i);

           if(k > 0) {
               sum += k;
               validCount++;
           }
       }
       if(validCount == 0 || sum < 0) return 0;

       return sum/validCount;
   }   

   /**
    * get Aggregate Speed
    * @return 
    */
   public double getAggregatedSpeed() {
       return getAggregatedSpeed(0);
   }        

   public double getAggregatedSpeed(int prevStep) {
       return getAverageSpeed(prevStep, MOVING_U_AVG_WINDOW);
   }

   public double getAverageSpeed(int prevStep, int howManySteps)
   {
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           double u = simstation.getData(dc, TrafficType.SPEED, prevStep+i);
           if(u > 0) {
               sum += u;
               validCount++;
           }
       }
       if(validCount == 0 || sum < 0) return 0;
       return sum/validCount;                        
   }

   /**
    * get AggregateRollingSpeed
    * @return 
    */
   public double getAggregateRollingSpeed(){
       if(isSpeedValid()){
           int n_samples = this.calculateRollingSamples();
           if(n_samples > 0){
               return getAverageRollingSpeed(0,n_samples);
           }else{
               return this.getStation().getSpeedLimit();
           }
       }else{
           return -1;
       }
   }
   public double getAverageRollingSpeed(int prevStep, int howManySteps){
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           int idx = RollingSpeed.size()-(prevStep+i)-1;
           double q = this.RollingSpeed.get(idx);
           if(q > 0) {
               sum += q;
               validCount++;
           }
       }
       if(validCount == 0 || sum < 0) return 0;
       return sum/validCount;   
   }

   public double getAverageFlow(int prevStep, int howManySteps){
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           double q = simstation.getData(dc, TrafficType.AVERAGEFLOW, prevStep+i);
           if(q > 0) {
               sum += q;
               validCount++;
           }
       }
       if(validCount == 0 || sum < 0) return 0;
       return sum/validCount;   
   }


   /**
    * Return aggregated speed
    * @param lastSampleIndex
    * @return 
    */
   public double getAggregatedSpeed2(int lastSampleIndex) {
       double density = getAggregatedDensity();
       double usum, u30s;
       usum = u30s = simstation.getData(dc, TrafficType.SPEED);
       int divide = 1;
       int period = 1;

       if (density < 10) {
           this.lastSpeedAggCount = lastSampleIndex;
           return getSpeedForLowK();

       } else if (density < 15) {
           period = 6;
       } else if (density < 25) {
           period = 4;
       } else if (density < 40) {
           period = 3;
       } else if (density < 55) {
           period = 4;
       } else {
           period = 6;
       }

       // trend check
       if (density >= 15) {
           double cU = u30s;
           double pU = this.simstation.getData(dc, TrafficType.SPEED, 1);
           double ppU = this.simstation.getData(dc, TrafficType.SPEED, 2);

           // if it has trend (incrase or decrease trend)
           if ((cU >= pU && pU >= ppU) || (cU <= pU && pU <= ppU)) {
               period = 2;
           }
       }

       divide = 1;
       int last = lastSampleIndex;
       for (int i = 1; i < period; i++) {
           if (lastSampleIndex - i < 0 || lastSampleIndex - i < this.lastSpeedAggCount) {
               break;
           }
           usum += this.simstation.getData(dc, TrafficType.SPEED, i);
           last = lastSampleIndex - i;
           divide++;
       }

       this.lastSpeedAggCount = last;

       return checkMaxSpeed(usum / divide, simstation.getStation().getSpeedLimit());
   }

   private double checkMaxSpeed(double u, double speedLimit) {
       int alpha = MAX_SPEED_ALPHA;

       // max speed = speed limit
       if (u > speedLimit) {
           return speedLimit + alpha;
       } else {
           return u;
       }
   }

   private int getSpeedForLowK() {
       int speedLimit = this.rnode.getSpeedLimit();
       if (this.downstream == null) {
           return speedLimit;
       }
       RNode downNode = this.downstream.rnode;
       if (downNode != null && downNode.getSpeedLimit() < speedLimit) {
           return (downNode.getSpeedLimit() + speedLimit) / 2;
       }

       return speedLimit;
   }

   public double getVolume(){
       return this.simstation.getData(dc,TrafficType.VOLUME);
   }
   public double getTotalVolume(int prevStep, int howManySteps){
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           double v = simstation.getData(dc, TrafficType.VOLUME, prevStep+i);
           if(v > 0) {
               sum += v;
               validCount++;
           }
       }
       return sum;
   }
   public double getAverageLaneFlow(){
       return this.simstation.getData(dc, TrafficType.AVERAGEFLOW);
   }
   public double getFlow(){
       return this.simstation.getData(dc,TrafficType.FLOW);
   }
   public double getTotalFlow(int prevStep, int howManySteps){
       double sum = 0;
       int validCount = 0;
       for(int i=0; i<howManySteps; i++) {
           double q = simstation.getData(dc, TrafficType.FLOW, prevStep+i);
           if(q > 0) {
               sum += q;
               validCount++;
           }
       }
       return sum;
   }
   /**
    * @return 
    */
   public double getSpeed() {
       return this.simstation.getData(dc, TrafficType.SPEED);
   }

   /**
    * @return 
    */
   public double getDensity() {
       return this.simstation.getData(dc, TrafficType.DENSITY);
//            return this.station.getDataForDebug(dc, TrafficType.DENSITY);
   }

   /** Samples used in previous time step */
   protected int rolling_samples = 0;

   /** Update the rolling samples for previous time step */
   protected void updateRollingSamples() {
           rolling_samples = calculateRollingSamples();
   }

   /** Calculate the number of samples for rolling average */
   protected int calculateRollingSamples() {
           return Math.min(calculateMaxSamples(), rolling_samples + 1);
   }

   /** Calculate the maximum number of samples for rolling average */
   protected int calculateMaxSamples(){
       if(isSpeedTrending()){
           return 2;
       }else{
           return DensityRank.samples(this.getDensity());
       }
   }
   protected boolean isSpeedTrending(){
       return isSpeedValid() &&
               (isSpeedTrendingDownward() || isSpeedTrendingUpward());
   }

   /** Is recent rolling speed data valid? */
   protected boolean isSpeedValid() {
           if(RollingSpeed.size() < 3){
               return false;
           }

           return RollingSpeed.get(0) > 0 && RollingSpeed.get(1) > 0 && RollingSpeed.get(2) > 0;
   }

   /** Is the speed trending downward? */
   protected boolean isSpeedTrendingDownward() {
           return RollingSpeed.get(0) < RollingSpeed.get(1) &&
                  RollingSpeed.get(1) < RollingSpeed.get(2);
   }

   /** Is the speed trending upward? */
   protected boolean isSpeedTrendingUpward() {
           return RollingSpeed.get(0) > RollingSpeed.get(1) &&
                  RollingSpeed.get(1) > RollingSpeed.get(2);
   }

   private void updateRollingSpeed(double speed) {
       this.RollingSpeed.add(Math.min(speed, this.getStation().getSpeedLimit() + 10));
       if(RollingSpeed.size() > DensityRank.getMaxSamples()){
           RollingSpeed.remove(0);
       }
   }

   public void setUpstreamStationState(StationState compareStation) {
       upstreamStationState = compareStation;
       if(upstreamStationState != null){
        setdistanceToUpstreamStationState(compareStation);
       }
   }
   public void setDownStreamStationState(StationState compareStation){
       downstreamStationState = compareStation;
       if(downstreamStationState != null){
        setdistanceToDownstreamStationState(compareStation);
       }
   }
   public StationState getUpstreamStationState(){
       return upstreamStationState;
   }
   public StationState getDownStreamStationState(){
       return downstreamStationState;
   }
   public void setdistanceToUpstreamStationState(StationState compareStation){
       distanceToUpstreamStation = TMO.getDistanceInFeet(this.getStation(), compareStation.getStation());
   }
   public void setdistanceToDownstreamStationState(StationState compareStation){
       distanceToDownstreamStation = TMO.getDistanceInFeet(this.getStation(), compareStation.getStation());
   }
   public int getdistanceToUpstreamStationState(){
       if(upstreamStationState == null)
           return -1;
       return distanceToUpstreamStation;
   }
   public int getdistanceToDownstreamStationState(){
       if(downstreamStationState == null)
           return -1;
       return distanceToDownstreamStation;
   }


   @Override
   public boolean equals(Object o){
       StationState com = (StationState)o;
       if(this == null || com == null || com.getID() == null || this.getID() == null){
           return false;
       }
       return com.getID().equals(this.getID());
   }

   public Double calculateAcceleration() {
       return calculateAcceleration(this.getUpstreamStationState(),this.getdistanceToUpstreamStationState());
   }
   public Double calculateAcceleration(StationState upstream){
       int distance = TMO.getDistanceInFeet(this.getStation(), upstream.getStation());
       return calculateAcceleration(upstream,distance);
   }
   public Double calculateAcceleration(StationState upstream, int distance){
       if(upstream == null){
           return null;
       }
       double u = this.getAggregateRollingSpeed();
       double usu = upstream.getAggregateRollingSpeed();
       return calculateAcceleration(u,usu,distance);
   }

   private Double calculateAcceleration(double u, double usu, int distance) {
       assert distance > 0;
       double d = DistanceUtil.getFeetToMile(distance);
       if(u > 0 && usu > 0){
           return (u * u - usu * usu) / (2 * d);
       }else
           return null;
   }


}
