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
package edu.umn.natsrl.ticas.plugin.srte2;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.plugin.srte2.SRTEResult.ResultRCRAccPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEProcess {

//    private int SMOOTHING_FILTERSIZE = 1;
//    private int QUANTIZATION_THRESHOLD = 2;
    private int STEADY_TIMELSP = 8;
    
    /**
    * Constant Value
    * SRTC Speed Interval Max Value
    */
    final double MaximumSpeedInterval = 10;
    
    /**
    * set MinSpeed
    * Minimum SRTF Value
    */
    final double MinSpeed = 55;
        
    private SRTEResult result = new SRTEResult();
    private TimeEvent timeevent;
    private final Section section;
    private Period period;
    private SRTESection selectedStation;
    //normal SRTESection -> fix me : Sectionss
    private SRTESection normalStation;
//    private SMOOTHING sfilter;
    private SRTEConfig config;
    
    //Check data for proccesing
    private boolean hasData = true;

    public SRTEProcess(Section section, Period period,SRTESection station,SRTESection normalStation, SRTEConfig config,TimeEvent te) {
        // config setting
//        this.SMOOTHING_FILTERSIZE = config.getInt("SMOOTHING_FILTERSIZE");
//        this.QUANTIZATION_THRESHOLD = config.getInt("QUANTIZATION_THRESHOLD");
//        sfilter = SMOOTHING.getSmooth(config.getInt(SRTEConfig.SMOOTHINGOPTION));
        this.selectedStation = station;
        this.normalStation = normalStation;
        this.section = section;
        this.period = period;
        timeevent = te;
        this.config = config;
        
        //result base sett
        result.station = selectedStation;
        result.sectionName = this.section.getName();

        //set SpeedLimit
        result.SpeedLimit = station.getSpeedLimit();
        
        result.setTime(te.getStartTime(), te.getEndTime(), te.getBareLaneRegainTime());
        result.setPeriod(period);
        
        result.setSnowData(selectedStation);
        result.setNormalData(normalStation);
    }
    /**
     * Main algorithm process
     *  - smoothing => Quantization => find SRST / LST / RST / SRT
     * @return
     */
    public SRTEResult stateDecision() {
//        System.out.println(selectedStation.hasData());
//        if(!selectedStation.hasData())
//            return result;
        
        int SRSTspeed = findSRSTspeed();
        result.getcurrentPoint().srst = SRSTspeed;
        
        int RSTspeed = findRSTspeed();
        result.getcurrentPoint().rst = RSTspeed;
        
        int NPRspeed = findNPRspeed();
        result.getcurrentPoint().NPR = NPRspeed;
        
        int SRSTtt = findSRSTtt();
        result.getcurrentPoint().SRST_TT = SRSTtt;
        
        int RSTtt = findRSTtt ();
        result.getcurrentPoint().RST_TT = RSTtt;
        
        int NPRtt = findNPRtt ();
        result.getcurrentPoint().NPR_TT = NPRtt;
       
        findRCRuk();
        
        return result;
    }

    
    /**
     * get TimeStemp
     * @param periodsec Time second
     * @return 
     */
    private int getTimestemp(int periodsec) {
        int timestemp = period.interval == 0 ? 0 : periodsec / period.interval;
        timestemp = timestemp == 0 ? 1 : timestemp;
        return timestemp;
    }

    /**
     * Find SRST - using AverageSpeed
     */
    private int findSRSTspeed() {
       int sustainDuration = 30;
       
       double[] normalU = result.getNormalData().data_origin;
       double[] snowU = result.getSnowData().data_origin;
       double[] Udiff = new double[normalU.length];
       int count = 0;
       
       int i;
       
       for (i=0; i<normalU.length; i++){
           
           Udiff[i] = normalU[i]-snowU[i];
//           System.out.println("[" + i + "] " + "NormalU - SnowU = " + Udiff[i]);
           
           if (Udiff[i]>0){
           count++;    
           }else{
           count = 0;
           }
//           System.out.println("The count of (Normal-Snow) = " + count);
      
           if (count==(sustainDuration/5)){
           break;
           }
       }
       int SRSTpoint = i-(sustainDuration/5);
 //      System.out.println("SRST = " + SRSTpoint);     
       return SRSTpoint;
    }
    
   /**
    * Find RST - using AverageSpeed
   */ 
    private int findRSTspeed() {
    double[] snowU = result.getSnowData().u_Avg_smoothed;
    
    int i;
    int SRSTpoint = result.getcurrentPoint().srst;
    double smallSpeed;
    double minSpeed=200;
    int minUpoint=0;
    int RSTpoint;
    
    
    for (i=SRSTpoint; i<snowU.length; i++){
        if(snowU[i-1]-snowU[i]<0){
           smallSpeed = snowU[i-1];
           if (smallSpeed <= minSpeed) {
               minSpeed = smallSpeed;
               minUpoint = i-1;
           }
        }
    }
    RSTpoint = minUpoint;
 //   System.out.println("RST = " + RSTpoint);
    return RSTpoint;
    }

   /**
     * Find NPR - using AverageSpeed
     */
    private int findNPRspeed() {
        int sustainDuration = 0;
                
        double[] normalU = result.getNormalData().data_origin;
        double[] snowU = result.getSnowData().data_origin;
        
        double[] Udiff = new double[normalU.length];
        int i;
        int count=0;
        double NPRth = 0;
        int RSTpoint = result.getcurrentPoint().rst;
        
        for (i=RSTpoint; i<normalU.length; i++){
            Udiff[i] = normalU[i] - snowU[i];
            if (Udiff[i] <= NPRth){
            count++;    
            }else{
            count=0;
            }
//            System.out.println("["+i+"]"+ " Normal= " + normalU[i] + " Snow= " + snowU[i] + " Difference= " + Udiff[i]);
//            System.out.println("NPR count= " + count);
            
            if (count==1){
                break;
            }
        }
        
        int NPRpoint = i-(sustainDuration/5);
//        System.out.println("NPR = " + NPRpoint);
        return NPRpoint;
    }
    
    /**
     * Find SRST_TT - using Travel Time 
     */
    private int findSRSTtt(){
        int sustainDuration=30;
        int SRSTttPoint;
        
        double[] normalTT = result.getNormalData().tt_origin;
        double[] snowTT = result.getSnowData().tt_origin;
        double[] TTdiff = new double[normalTT.length];
        
        int i;
        int count=0;
        for (i=0; i<normalTT.length; i++){
            TTdiff[i] = normalTT[i]-snowTT[i];
            if (TTdiff[i]<0){
            count++;   
            }else{
            count=0;
            }
            if (count==(sustainDuration/5)){
            break;
            }
        }
        SRSTttPoint = i-(sustainDuration/5);
//        System.out.println("SRST_TT = "+SRSTttPoint);
        return SRSTttPoint;
    }

    /**
     * Find RST_TT - using Travel Time 
     */
    private int findRSTtt() {
     int RSTttPoint;
     double[] snowTT = result.getSnowData().tt_origin;
    
     int i;
     double bigTT = 0;
     int bigPoint=result.getcurrentPoint().SRST_TT;
     double maxTT = 0;
     int maxPoint=result.getcurrentPoint().SRST_TT;
     int SRSTttpoint=result.getcurrentPoint().SRST_TT;
     
     for(i=(SRSTttpoint+1); i<snowTT.length; i++){
         if (snowTT[i]-snowTT[i-1] > 0){
            bigTT = snowTT[i];
            bigPoint= i;
         }
         if (bigTT - maxTT > 0){
             maxTT = bigTT;
             maxPoint = bigPoint;
         }
     }
     RSTttPoint = maxPoint;
//     System.out.println("RST_TT = " + maxPoint);
     return RSTttPoint;   
    }
   
    /**
     * Find NPR_TT - using Travel Time
     */
   
    private int findNPRtt() {
    double[] normaltt = result.getNormalData().tt_origin;
    double[] snowtt = result.getSnowData().tt_origin;
    double[] TTdiff = new double[normaltt.length];
    
    int i;
    int RSTttPoint = result.getcurrentPoint().RST_TT;
    int NPRttPoint;
    double NPRth=0;
    int count=0;
    int sustainDuration = 5;
    
    for (i=RSTttPoint; i<normaltt.length; i++){
        TTdiff[i]=snowtt[i]-normaltt[i];
        
        if (TTdiff[i] <=NPRth){
            count++;
        }else{
            count=0;
        }
//        System.out.println("["+i+"]"+" Snow TT= "+ snowtt[i] + ",  Normal TT= "+ normaltt[i]+",  Difference= "+ TTdiff[i]+ ",  count= "+ count+".");
        if (count==(sustainDuration/5)){
            break;
        }
    }
    NPRttPoint=(i-(sustainDuration/5)+1);
//    System.out.println("NPR_TT = " + (i+1));
    return NPRttPoint;    
    }

    /**
     * Find RCR_UK - using UK plot 
     */
    
    private void findRCRuk() {
    double[] snowU = result.getSnowData().data_smoothed;
    double[] snowK = result.getSnowData().k_smoothed;   
    
    for(int i=0; i<snowU.length; i++){
     System.out.println("snowU ["+i+"]= "+snowU[i]+", snowK ["+i+"]= " + snowK[i]);   
    }
    
    int dotNumber = 5;
    double[] fewU1 = new double[dotNumber];
    double[] fewU2 = new double[dotNumber];
    double[] fewK1 = new double[dotNumber];
    double[] fewK2 = new double[dotNumber];
    
    for(int i=0; i<dotNumber; i++){          // this Forloop makes the two lists that each containing the pre-defined points (as dotNumber) of U and K data 
        fewU1[i] = snowU[i];
        fewK1[i] = snowK[i];
        System.out.println("fewU1 ["+i+"]= "+snowU[i] + ", fewK1 ["+i+"]= "+snowK[i]);
    }
    
    for(int i=0; i <dotNumber; i++){
        fewU2[i] = snowU[i+dotNumber];
        fewK2[i] = snowK[i+dotNumber];
        System.out.println("fewU2 ["+i+"]= " + snowU[i+dotNumber] + ", fewK2 [" + i + "]= "+ snowK[i+dotNumber]);
    }
    
    
    
    double sum=0;
    double count=0;
    double avgU1=0, avgU2=0, avgK1=0, avgK2=0;

    for (int i=0; i<dotNumber; i++){              // average of U1
        if(fewU1[i]==0 || fewU1[i]==-1){
        //Skip the data without counting    
        }else{
        sum = sum + fewU1[i];
        count++;
        }
        System.out.println("U1 ["+i+"]  sum= "+sum+", count= "+count);
        avgU1 = sum/count;
    }

        count = 0;
        sum = 0;
        for (int i=0; i<dotNumber; i++){              // average of U2
        if(fewU2[i]==0 || fewU2[i]==-1){
        //Skip the data without counting    
        }else{
        sum = sum + fewU2[i];
        count++;
        }
        System.out.println("U2 ["+i+"]  sum= "+sum+", count= "+count);
        
        avgU2 = sum/count;
    }

        count = 0;
        sum = 0;
        for (int i=0; i<dotNumber; i++){              // average of K1
        if(fewK1[i]==0 || fewK1[i]==-1){
        //Skip the data without counting    
        }else{
        sum = sum + fewK1[i];
        count++;
        }
        System.out.println("K1 ["+i+"]  sum= "+sum+", count= "+count);
        
        avgK1 = sum/count;

    }        
        
        count = 0;
        sum = 0;
        for (int i=0; i<dotNumber; i++){              // average of K2
        if(fewK2[i]==0 || fewK2[i]==-1){
        //Skip the data without counting    
        }else{
        sum = sum + fewK2[i];
        count++;
        }
        System.out.println("K2 ["+i+"]  sum= "+sum+", count= "+count);
        
        avgK2 = sum/count;
    }
        
        System.out.println("avgU1= "+avgU1+ ", avgK1= "+avgK1);
        System.out.println("avgU2= "+avgU2+ ", avgK2= "+avgK2);
        
        
        double Num1;                             // slope of the Line1 (beta1) using Simple Linear Regression Model
        double Denom1;
        double sumNum1=0;
        double sumDenom1=0;
        double slope1;
        
        for (int i=0; i<dotNumber; i++){           
            Num1 = fewU1[i]*(fewK1[i]-avgK1);
            sumNum1 = sumNum1 + Num1;
            
            Denom1 = Math.pow((fewK1[i]-avgK1),2);
            sumDenom1 = sumDenom1 + Denom1;
        }
       slope1 = sumNum1/sumDenom1;

        double Num2;                            // slope of the Line2  (beta2) using Simple Linear Regression Model
        double Denom2;
        double sumNum2=0;
        double sumDenom2=0;
        double slope2;
        
        for (int i=0; i<dotNumber; i++){            
            Num2 = fewU2[i]*(fewK2[i]-avgK2);
            sumNum2 = sumNum2 + Num2;
            
            Denom2 = Math.pow((fewK2[i]-avgK2),2);
            sumDenom2 = sumDenom2 + Denom2;
        }
       slope2 = sumNum2/sumDenom2;
   
   /**
    * Two points on each of the two linear equations are as following:
    * Line1: (0,0) (1,slope1)    i.e., x1=1, y1=slope1
    * Line2: (0,0) (1,slope2)    i.e., x2=1, y2=slope2
    */    

       
  /**
   * Calculate the angle in-between the two lines
   */     
 double x1=1,y1=slope1;
 double x2=1,y2=slope2;
 double angle;
 double inner;
 double sumLength = Math.sqrt(x1*x1 + y1*y1)*Math.sqrt(x2*x2 + y2*y2);
 
 inner = x1*x2 + y1*y2;
 angle = (double) Math.acos(inner/sumLength)*180/Math.PI;            //convert the radian to the decimal angle
    
 System.out.println("The slope1= " + slope1 + ", The slope2= " + slope2);
 System.out.println("The angle between the lines is= " + angle);
    }
     
       
       
    /**
     * Algorithm for using the slopes between the two points
   ---------------------------------------------------------------------------------------------------------------- 
    double[] snowUK = new double[snowU.length];                                  //snowUK =  slope between two points in UK plane
    int i;
    int RSTpoint = result.getcurrentPoint().rst;
    int NPRpoint = result.getcurrentPoint().NPR;
    for(i = 0; i < snowU.length; i++){
        if (i==0 || snowK[i]==0 || snowK[i]==-1){
        snowUK[i] = 0;    
        }else{
        snowUK[i] = (snowU[i]-snowU[i-1])/(snowK[i]-snowK[i-1]);
        }
        System.out.println("[" + i + "]"+ " U = "+snowU[i] + ", K = "+snowK[i] + ", delta-U/K = "+snowUK[i]);
    }
   -----------------------------------------------------------------------------------------------------------------
    */


    
}
