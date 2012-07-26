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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.ticas.plugin.srte.SRTEResult.ResultRCRAccPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEProcess {

    private int SMOOTHING_FILTERSIZE = 1;
    private int QUANTIZATION_THRESHOLD = 2;
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
    private SRTEStation selectedStation;
    private SMOOTHING sfilter;
    private SRTEConfig config;

    public SRTEProcess(Section section, Period period,SRTEStation station, SRTEConfig config,TimeEvent te) {
        // config setting
        this.SMOOTHING_FILTERSIZE = config.getInt("SMOOTHING_FILTERSIZE");
        this.QUANTIZATION_THRESHOLD = config.getInt("QUANTIZATION_THRESHOLD");
        sfilter = SMOOTHING.getSmooth(config.getInt(SRTEConfig.SMOOTHINGOPTION));
        this.selectedStation = station;
        this.section = section;
        this.period = period;
        timeevent = te;
        this.config = config;
        
        //result base sett
        result.station = selectedStation;
        result.sectionName = this.section.getName();

        //set SpeedLimit
        result.SpeedLimit = station.getSpeedLimit();
        
        //Speed Setting
        result.data_origin = selectedStation.getSpeed();
        result.data_smoothed = smoothing(result.data_origin);
        result.data_quant = quantization(result.data_smoothed);
        
        //Flow setting
        result.q_origin = selectedStation.getAverageLaneFlow();
        result.q_smoothed = smoothing(result.q_origin);
        result.q_quant = quantization(result.q_smoothed);
        
        // density setting
        result.k_origin = selectedStation.getDensity();
        result.k_smoothed = smoothing(result.k_origin);
        result.k_quant = quantization(result.k_smoothed,config.K_quan);
        
        //Average u Data
        result.u_Avg_origin = SRTEUtil.CalculateSmoothedSpeed(result.q_origin, result.k_origin);
        result.u_Avg_smoothed = SRTEUtil.CalculateSmoothedSpeed(result.q_smoothed, result.k_smoothed);
        result.u_Avg_quant = quantization(result.u_Avg_smoothed);
        
        result.setTime(te.getStartTime(), te.getEndTime(), te.getBareLaneRegainTime());
        result.setPeriod(period);

    }
    /**
     * Main algorithm process
     *  - smoothing => Quantization => find SRST / LST / RST / SRT
     * @return
     */
    public SRTEResult stateDecision() {
        double filteredData[];
        double qData[];
        double qK[];
        filteredData = result.data_smoothed;
        qData = result.data_quant;
        qK = result.k_quant;
        int EndLength = filteredData.length;
        int sPoint = result.getStartTimeStep();
        int endPoint = result.getEndTimeStep()+1 >= qData.length ? qData.length : result.getEndTimeStep() +1;
        
        //Find Point until End
        while(sPoint < endPoint){
            ResultPoint rp = new ResultPoint();
            
            /**
             * Find SRST Point
             */
            rp.setSRST(findSRST(qData, filteredData,sPoint,endPoint));
            sPoint = rp.srst;
            
            if(sPoint == 0)
                break;
            System.out.println("srst : "+rp.srst);
            
            /**
             * find LST Point
             */
            rp.setLST(findLST(qData,filteredData, sPoint,EndLength));
            System.out.println("..lst : "+rp.lst);
            if(rp.lst == -1 || rp.lst >= endPoint)
                break;
            
            
            /**
             * find RST Point
             */
            // loop index i : increase count theshold for defining RST
            // example: if i= 4 and speed countinously increases 4 times from LST, the fist point of the speed change will be the RST
//            for (int i = 4; i > 0; i--) {
//                rp.setRST(findRST(qData, rp.lst, filteredData, i,endPoint));
//
//                //if RST is found, break
//                if (rp.rst > rp.lst) {
//                    break;
//                }
//            }
            rp.setRST(findRST(qData,rp.lst,filteredData,EndLength));
            System.out.println("..rst : "+rp.rst);
            System.out.println();
            
            /**
             * Add Each Point
             */
            if(rp.isEmpty()){
                break;
            }
            result.addPoint(rp);
            sPoint = rp.rst+1;
        }
        
        /**
         * find SRT Point
         */
//        findSRT(qData, result);
        findSRT(qData,qK,result);
        System.out.println("SRT :" + result.getcurrentPoint().csrt);
        for (int i = 0; i < result.getcurrentPoint().srt.size(); i++) {
            System.err.println("SRT : " + result.getcurrentPoint().srt.get(i));
        }
        
        /**
         * print Points
         */
        System.out.println("*********************************************");
        System.out.println("point list -> " + result.getPoint().size());
        System.out.println("*********************************************");
        int cnt = 0;
        for(ResultPoint trp : result.getPoint()){
            System.out.println("p["+cnt+"]"+" srst :" + trp.srst + " lst : "+trp.lst+" rst : "+trp.rst);
            cnt++;
        }
        System.out.print("current->");
        System.out.println(" srst :" + result.getcurrentPoint().srst + " lst : "+result.getcurrentPoint().lst+" rst : "+result.getcurrentPoint().rst);
        System.out.println("*********************************************");
        
        /**
         * find Speed Step
         */
        findSpeedStep(result,filteredData);
        /**
         * find RCR Point
         */
        findRCRPoint(result);
        
        /**
         * Print Speed Steps
         */
        List resultlst = (List)result.getSpeedKeyList();
        Iterator<Integer> itr = resultlst.iterator();
        while(itr.hasNext()){
            int key = itr.next();
            System.out.print("key->"+key + " time : ");
            SRTEResult.SpeedMap sm = result.getSpeedList().get(key);
            System.out.println(sm.time + " speed : "+sm.speed+ " bf :" + sm.beforeSpeed + " interpol min : "+sm.getKeyTimeMin()+"("+sm.getKeyTimeStep()+")");
        } 

        return result;
    }

    /**
     * Moving average method
     * example, Data at 10:30 is averaged from 10:15 to 10:45 (45min averaged data)
     * @param data 15 min route-wide data
     * @return array of smoothed data
     */
    private double[] smoothing(double[] data) {
        int i = 0;
        int j = 0;
        double tot = 0;
        double[] filteredData = new double[data.length];

        if(sfilter == null)
            return data;
        
        if(sfilter.isNOSMOOTHING())
            return data;
        
        if(sfilter.isSecond()){
            for(i=0;i<data.length;i++){
                if(i==0){
                    filteredData[i] = data[i];
                    continue;
                }
                filteredData[i] = (data[i-1] + data[i])/2;
            }
        }else{
            if(sfilter.isDefault())
                SMOOTHING_FILTERSIZE = sfilter.getCount();
            
            for (i = 0; i < SMOOTHING_FILTERSIZE; i++) {
                filteredData[i] = data[SMOOTHING_FILTERSIZE - 1];
            }
            // 0 ~ 11
            for (i = SMOOTHING_FILTERSIZE; i < data.length - SMOOTHING_FILTERSIZE; i++) {
                for (j = i - SMOOTHING_FILTERSIZE; j <= i + SMOOTHING_FILTERSIZE; j++) {
                    tot += data[j];
                }
                filteredData[i] = tot / (2 * SMOOTHING_FILTERSIZE + 1);
                tot = 0;
            }

            for (i = data.length - SMOOTHING_FILTERSIZE; i <= data.length - 1; i++) {
                filteredData[i] = filteredData[data.length - SMOOTHING_FILTERSIZE - 1];
            }
        }

        return filteredData;
    }

    /**
     * Quantization : the process of approximating the continuous set of values in the speed data with a finite set of values
     *  - Find out approximate points of speed change
     *  - example, threshold 2 mi/h,
     *      -filtered data: 56,56,56,55,54,53,52,51,50,50,49,49,46,46
     *      -Output       : 56,56,56,56,54,54,52,52,50,50,50,50,46,46
     *
     * @param filteredData 45min route-wide average data
     * @return
     */
    private double[] quantization(double[] filteredData, Integer TH) {
        int stick = 0;
        double[] qData = new double[filteredData.length];
        for (int i = 0; i < 3; ++i) {
            stick += filteredData[i];
        }
        stick = stick / 3;

        for (int i = 0; i <= filteredData.length - 1; i++) {
            if ((Math.abs(stick - filteredData[i]) > TH)) {
                stick = (int) Math.round(filteredData[i]);
            }
            qData[i] = stick;
        }
        return qData;
    }
    private double[] quantization(double[] filteredData) {
        return quantization(filteredData,QUANTIZATION_THRESHOLD);
    }
    /**
     * Find SRST
     *  - the first point of speed continuously decreases
     * @param qData : quantized data
     * @param smootedData : smoothed data
     * @return
     */
    private int findSRST(double[] qData, double[] smootedData, int sPoint, int ePoint) {
        result.addLog("Finding SRST..........",false);
        int declineCount = 0;
        int srst = 0;
        int endPoint = ePoint < 0 ? qData.length-1 : ePoint;

        // decesion SRST if the point is met following condtions
        for (int i = sPoint; i <= endPoint; i++) {
            if (declineCount == 1) {
                //search back and find at the highest speed level in the smoothed data
                for (int j = srst; j > sPoint; j--) {
                    if (smootedData[j - 1] < smootedData[j]) {

                        return srst - 2;
                    }
                    if (smootedData[j - 1] > smootedData[j]) {
                        srst = j + 1;
                    }
                }

                return srst - 1;
            }
            // increases decline point if speed drops
            if (qData[i - 1] > qData[i]) {
                srst = i;
                declineCount++;

            }
            // reset 0 if speed increases
            if (qData[i - 1] < qData[i]) {
                declineCount = 0;

            }
        }

        return srst;

    }

    /**
     * @deprecated 
     *Find LST
     *  - speed reaches the low speed level
     *      -case 1: find the last decline point when speed starts to increases 2 times
     *      -case 2: find the last decline point when speed are stable for 2 hours since the last decline point
     * @param qData
     * @param srst
     * @return
     */
    private int findLST_Old(double[] qData, int srst, int ePoint) {
        int lst = 0;
        int increaseCount = 0;
        int increaseCountB = 0;
        int steadyState = 0;
        int endPoint = ePoint < 0 ? qData.length-1 : ePoint;
        System.out.print("eP : "+ePoint);
        result.addLog("Finding LST..........",false);
        for (int i = srst + 1; i <= endPoint - 1; i++) {
            if (increaseCount == 2) {
                //search retrospectively to find lsp
                for (int j = lst; j > srst; j--) {
                    if (increaseCountB == 2) {

                        return lst;
                    }
                    if (qData[j - 1] > qData[j]) {
                        increaseCountB++;
                        //set lsp if lsp is not set yet
                        if (lst == 0) {
                            lst = j;
                        }
                    } else if (qData[j - 1] < qData[j]) {
                        //reset
                        lst = 0;
                        increaseCountB = 0;
                    }
                }

                return lst;
            }

            //find lsp if speed is same for timesTillSteay value
            if (STEADY_TIMELSP == steadyState) {

                return lst;
            }
            if (qData[i - 1] < qData[i]) {
                steadyState = 0;
                increaseCount++;
            }
            if (qData[i - 1] == qData[i]) {
                steadyState++;

            } else if (qData[i - 1] > qData[i]) {
                lst = i;
                steadyState = 0;
                increaseCount = 0;
            }
        }

        return lst;
    }
    
    /**
     * find LST New Algorithm
     * @param qData
     * @param srst
     * @param ePoint
     * @return 
     */
    private int findLST(double[] qData,double[] sData, int sPoint, int ePoint) {
        if(qData == null)
            return sPoint;
        
        int lst = sPoint;
        for(int i=sPoint;i<ePoint;i++){
            if(i == sPoint)
                continue;
            
            if(qData[i-1] > qData[i]){
                lst = i;
            }else if(qData[i-1] < qData[i]){
                //Search Foword
                System.out.println("lst -> "+lst);
                lst = findLST(sData,null,lst,ePoint);
                return lst;
            }
        }
        return -1;
    }
    
    private int findRST(double[] qData, int sPoint, double[] filteredData, int ePoint) {
        int rst = -1;
        for(int i=sPoint;i<ePoint;i++){
            if(i==sPoint)
                continue;
            if(qData[i-1] < qData[i]){
                rst = i-1;
                //Search Back
                int tempRST = rst;
                for(int j=rst;j>=sPoint;j--){
                    if(filteredData[j] < filteredData[tempRST])
                        tempRST = j;
                    
                    if(filteredData[j-1] >= filteredData[j]){
                        rst = tempRST;
                        break;
                    }
                    
                }
                return rst;
            }
        }
        return rst;
    }
    /**
     * @deprecated 
     * Find RST when speed starts to continuously increase
     * @param qData
     * @param lst
     * @param filteredData
     * @param targetIncreaseCount = 4
     *  - increasing speed change point from LST
     * @return
     */
    private int findRST_old(double[] qData, int lst, double[] filteredData, int targetIncreaseCount, int ePoint) {
        lst = lst <= 0 ? 1 : lst;
        int rst = lst;
        int increaseCount = 0;
        int steadyState = 0;
        int endPoint = ePoint < 0 ? qData.length-1 : ePoint;
        //int decreaseCount = 0;
        result.addLog("Finding RST (" + targetIncreaseCount + ").............",false);
        for (int i = lst; i <= endPoint - 1; i++) {
            if (increaseCount == targetIncreaseCount) {
                for (int j = rst - 1; j > lst; j--) {
                    if (filteredData[j] < qData[j]) {
                        rst = j;
                        result.addLog(" -> case 1",false);
                        return rst;
                    }
                }
                result.addLog(" -> case 2",false);
                return rst;
            }

            // increase the count if the speed at time t+1 is greater than the speed at time t
            if (qData[i - 1] < qData[i]) {
                //set srp if srp is not set yet
                increaseCount++;
                steadyState = 0;
                if (rst == lst) {
                    rst = i - 1;
                }
            }
            // set if the speed at time t+1 is the same as the speed at time t
            if (qData[i - 1] == qData[i]) {
                steadyState++;
            } else if (qData[i - 1] > qData[i]) {
                // reset srp
                rst = lst;
                increaseCount = 0;
                steadyState = 0;
            }
        }
        result.addLog(" -> case 3",false);
        return rst;
    }

    /**
     * @deprecated 
     *Find SRT
     * Speed Recovery Time(s) are identified as the expected flow pattern recovered time (EFPRT)
     * and the speed stabilization time (SST) when speed satisfies condition A,
     * and either of B or C, or only D. The definitions of the conditions are as follows:
     *
     * - Condition A: When the quantized speed level is constant for an hour after
     *   the Recovery Starting Time, the time value of the point satisfying this
     *   condition will be candidate(s) for the Speed Recovery Time.
     * - Condition B: If the speed meets the Condition A, the density and flow data
     *   need to be checked to see if they are in the expected flow pattern recovered
     *   time as in Pattern 2. If Pattern 2 continuously occurs for more than an hour
     *   between the candidate’s time points defined by the Condition A, the time
     *   value of the first point which met this condition will be the EFPRT and
     *   the time value of the first point which is the longest duration of the
     *   Pattern 2 will be the SST. If only one candidate is found and meets this
     *   condition, the time value of the first point will be the SST.
     * - Condition C: If the speed meets the condition A, the density and flow data
     *   needs to be checked to see if they are in the bare-lane status.
     *   When Pattern 1 continuously occurs, the time value of the point which is
     *   the maximum slops difference of flow-density data points will be the EFPRT.
     *   If the candidate point(s) is after the time value of the EFPRT, then
     *   the time value of the first candidate point will be the SST.
     * - Condition D: If the point meets all above conditions, then the time
     *   value of the earliest one will be the Speed Recovery Time.
     *
     * @param filteredData
     * @param qData
     * @param result 

     */
    private void findSRT_Old(double[] qData, SRTEResult result) {

        result.addLog("Finding SRT.............");
        // create pattern search instance
        SRTEPatternSearch patternSearch = new SRTEPatternSearch();
      
        // tmp[0]: phase = traffic pattern k, q, u  
        // tmp[1]: tansition points = EFPRT and SST
        Object[] tmp = patternSearch.getPatternAndTransitionpoint(this.section, this.period);
        int[] phases = (int[]) tmp[0];
        result.phases = phases;
        ArrayList<Integer> transitionPoints = (ArrayList<Integer>) tmp[1];
        int trPoint = (transitionPoints.isEmpty() ? 99999999 : transitionPoints.get(1));

        //temp variable to find speed change point
        double stickSpeed = -999;

        // speed change point list
        // tick: speed change point
        List<Tick> ticks = new ArrayList<Tick>();

        // current speed change point
        Tick stickTick = null;

        //variable for the condition C
        int firstPointAfterTrPoint = -1;

        //variatble for the condition B
        int maxPhase2Point = -1;
        int maxPhase2Count = -1;


        for (int i = result.getcurrentPoint().rst; i < qData.length; i++) {
            double u = qData[i];
            if (stickSpeed != u) {
                if (stickTick != null) {
                    if (stickTick.phase_2_count > maxPhase2Count) {
                        maxPhase2Point = stickTick.pos;
                        maxPhase2Count = stickTick.phase_2_count;
                    }
                }
                //stickTick includes index and traffic papttern
                stickTick = new Tick();
                stickTick.pos = i;
                stickTick.tick_pattern = phases[i];
                ticks.add(stickTick);
//                System.out.println("["+i+"] : sticSpeed="+stickSpeed+" u="+u+"stickTick.pos="+stickTick.pos+" parttern="+stickTick.tick_pattern);
                if (firstPointAfterTrPoint < 0 && i >= trPoint) {
                    firstPointAfterTrPoint = i;
                }
                stickSpeed = u;
            }
            //increase stickTick.pashe_2)count  when traffic pattern 2 occurs
            if (phases[i] == 2) {
                stickTick.phase_2_count++;

                // set the fist point if patter 2 count is greater than 4
                if (stickTick.phase_2_count >= 4 && result.getcurrentPoint().srt.isEmpty()) {
                    result.addLog(" -> case 1 : " + stickTick.phase_2_count + " / " + stickTick.pos);
                    result.getcurrentPoint().srt.add(stickTick.pos);
                }
            }
        }
        // update the last stickTick
        if (stickTick.phase_2_count > maxPhase2Count) {
            maxPhase2Point = stickTick.pos;
            maxPhase2Count = stickTick.phase_2_count;
        }
        // if the first point of SRT ans max-phase-2-count point are different
        // add max-phase-2-count to SRST list
        if (maxPhase2Count >= 4) {
            if (!result.getcurrentPoint().srt.isEmpty()) {
                int p = result.getcurrentPoint().srt.get(0);
                if (p != maxPhase2Point) {
                    result.addLog(" -> case 2 : " + maxPhase2Count + " / " + maxPhase2Point);
                    result.getcurrentPoint().srt.add(maxPhase2Point);
                }
            }
        }
        // just return if SRT point is found
        if (!result.getcurrentPoint().srt.isEmpty()) {
            return;
        }
        // if not, add result satisfying condition C
        if (firstPointAfterTrPoint > 0) {
            result.addLog(" -> case 3");
            result.getcurrentPoint().srt.add(trPoint);
            result.getcurrentPoint().srt.add(firstPointAfterTrPoint);
            return;
        }
        // if not, add max-phase-2-count point to result
        if (maxPhase2Point > 0) {
            result.addLog(" -> case 4");
            result.getcurrentPoint().srt.add(maxPhase2Point);
            return;
        }

    }

    /**
     * Find Speed Step by Step
     * @param result
     * @param data 
     */
    private void findSpeedStep(SRTEResult result, double[] data) {
        int periodsec = 1800;
        int timestemp = period.interval == 0 ? 0 : periodsec / period.interval;
        timestemp = timestemp == 0 ? 1 : timestemp;
        int spoint = result.getcurrentPoint().rst+1;
        int endLength = data.length;
        double tempStep = 0;
        double sSpeedstep = 40;
        double step = 5;
//        boolean isStep = false;
        int timecnt = 0;
        SRTEResult.SpeedMap stempData = null;
        System.out.println("Start Speed Find - "+spoint);
        if(spoint-1 <= 0)
            return;
        
        for(int i=spoint;i<endLength;i++){
            if(data[i] >= sSpeedstep){
                double bt = i == spoint ? data[i] : data[i-1];
                tempStep = SRTEUtil.calculateStep(data[i],(int)step);
                stempData = new SRTEResult.SpeedMap(i,data[i],bt,tempStep,(double)period.interval);
                System.out.println("data["+i+"] : "+data[i] + "sstep < data, sSpeedstep :"+sSpeedstep + "tempStep : " + tempStep);
                timecnt = 0;
                for(int j=i+1;j<endLength;j++){
                    if(data[j] >= tempStep)
                        timecnt++;
                    else
                        break;
                    
                    if(timecnt == timestemp){
                        sSpeedstep = tempStep + step;
                        result.AddSpeedData((int)tempStep, stempData);
                        break;
                    }
                }
            }else{
                System.out.println("data["+i+"] : "+data[i]);
            }
//            if(isStep){
//                if(data[i] >= tempStep && data[i] < tempStep + step && i != (endLength-1)){
//                    timecnt ++;
//                }else{
//                    if(data[i] >= tempStep && data[i] < tempStep + step) //Last time Count
//                        timecnt ++;
//                    
//                    if(data[i] >= tempStep + step || 
//                            timecnt >= timestemp){
//                        sSpeedstep = tempStep+step;
//                        result.AddSpeedData((int)tempStep, stempData);
//                    }
//                    timecnt = 0;
//                    isStep = false;
//                }
//            }
//            
//            if(data[i] >= sSpeedstep && !isStep){
//                double bt = i == spoint ? data[i] : data[i-1];
//                stempData = new SRTEResult.SpeedMap(i,data[i],bt);
//                isStep = true;
//                tempStep = calculateStep(data[i],(int)step);
//                System.out.println("data["+i+"] : "+data[i] + "sstep < data, sSpeedstep :"+sSpeedstep + "tempStep : " + tempStep);
//            }else
//                System.out.println("data["+i+"] : "+data[i]);
        }
    }

    /**
     * Find RCR Point New Version
     * @param result 
     */
    private void findRCRPoint(SRTEResult result) {    
        double[] sdata = result.data_smoothed;
        double[] qdata = result.data_quant;
        double lSpeed = this.getLimitSpeed(qdata, result,true);
        
        int sPoint = result.getcurrentPoint().rst; //RST
        int ePoint = sPoint;
        if(result.getcurrentPoint().srt.size() > 0)
            ePoint = result.getcurrentPoint().csrt; //SRT
        
        if(sPoint <= 0 || ePoint <= 0)
            return;
        /**
         * find uLimit Point
         */
        System.out.println("speed limit : "+lSpeed);
        int uLimitPoint = sPoint;
        for(int i=sPoint;i<=ePoint;i++){
            if(sdata[i] > lSpeed && i > sPoint){
                uLimitPoint = i-1;
                break;
            }
        
            if(i == ePoint)
                uLimitPoint = i;
        }
        
        int LST = result.getcurrentPoint().lst;
        int SRTF = result.getcurrentPoint().srt.get(0);
        int SRTC = result.getcurrentPoint().srt.get(1);
        
        int RCR = -1;
        
        
        if(lSpeed > sdata[LST]){ // if There is many snow
            if(SRTC != -1 && SRTC == result.getcurrentPoint().csrt && SRTC < uLimitPoint)
                RCR = findUpperPattern(result.q_smoothed, result.k_smoothed, result.u_Avg_smoothed, sPoint, SRTC);
            else
                RCR = calculateRCRusingSRTF(sdata,result.data_quant,sPoint,uLimitPoint);
//            if(SRTF != -1){ //if SRTF
//                RCR = calculateRCRusingSRTF(sdata,sPoint,uLimitPoint);
//            }else if(SRTF == -1 && SRTC != -1){ //else !SRTF && SRTC
//                System.out.println("RCR Selected : A-2");
//                RCR = findUpperPattern(result.q_smoothed, result.k_smoothed, result.u_Avg_smoothed, sPoint, SRTC);
//            }else{} //!SRTF && !SRTC
        }else{ //else small snow
            if(SRTF != -1)
                RCR = calculateRCRusingSRTF(sdata, result.k_smoothed,sPoint,SRTF);
        }
        
        result.getcurrentPoint().RCR = RCR;
//        for(int i=sPoint; i<=ePoint;i++){
//            if(i < sPoint + 2)
//                continue;
//            
//            SRTEResult.ResultRCRAccPoint p = new SRTEResult.ResultRCRAccPoint();
//            p.data = Math.abs((sdata[i]-sdata[i-1]) - (sdata[i-1]-sdata[i-2]));
//            p.point = i-1;
//            result.AddRCRAccPoint(p);
//        }
    }
    
    /**
     * 
     * @param q
     * @param k
     * @param u
     * @param sPoint
     * @return 
     */
    private int findUpperPattern(double[] q, double[] k, double[] u, int sPoint, int ePoint) {
        double dQ = SRTEConfig.RCR_Q;
        double dU = SRTEConfig.RCR_U;
        for(int i=sPoint;i<=ePoint;i++){
            if(i == sPoint)
                continue;
            
            double deltaQ = (q[i] - q[i-1]);
            double deltaK = Math.abs(k[i] - k[i-1]);
            double slope = deltaK == 0 ? 0 : deltaQ / deltaK;
            double deltaU = u[i] - u[i-1];
            if(slope > dQ
                    && deltaU > dU){
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * New Algorithm
     * K-based RCR Searching Algorithm
     * @param sdata
     * @param sPoint
     * @param ePoint
     * @return 
     */
    private int calculateRCRusingSRTF(double[] sdata, double[] u_data, int sPoint, int ePoint) {
        int RCR = -1;

        /**
         * Find AccPoint ASC
         */
        ArrayList<SRTEResult.ResultRCRAccPoint> point = calculatePoint(u_data,sPoint,ePoint,false,false);
        
        System.out.println("RCR Key : sPoint :" + sPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        
        if(point.size() <= 0)
            return sPoint;
        
        /**
         * Research Point from ResetPoint
         * Find 2 continuously subtraction Point 
         */
        int resetPoint = sPoint; //reset Point
        
        int findPoint = 0;
        int ZeroTh = (int)config.RCRNofM;
        int MinusCount = 0; //MinusCount
        int beforeMCount = 0;
        for(int i = 0;i<point.size();i++){
            if(point.get(i).data < 0 && i != point.size()-1){
                MinusCount = 0;
                for(int z=i+1;z<point.size();z++){
                    if(u_data[point.get(i).point] == u_data[point.get(z).point])
                        MinusCount ++;
                    else
                        break;
                }
                
                if(MinusCount >= ZeroTh && beforeMCount < MinusCount){
                    resetPoint = point.get(i).point;
                    findPoint = i;
                    beforeMCount = MinusCount;
                }
            }
//            if(point.get(i).data < 0)
//                MinusCount ++;
//            else
//                MinusCount = 0;
//            
//            
//            if(MinusCount > (config.RCRNofM - 1)){
//                int tp = i;
//                if(i == 0)
//                    tp = i;
//                else
//                    tp = i-((int)config.RCRNofM-1);
//                
//                /**
//                * add ResetPoint
//                */
//                resetPoint = point.get(tp).point;
//                result.AddRCRAccPoint(point.get(tp));
//                break;
//            }    
        }
        if(findPoint > 0)
            result.AddRCRAccPoint(point.get(findPoint));
        else
            result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(-1,-1));
        
//        if(result.getRCRAccPointList().size() <= 0)
//            result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(sPoint,-1));
        
        
        point = calculatePoint(sdata,resetPoint,ePoint,true,true);
        
        
        System.out.println("RCR : sPoint :" + resetPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            result.AddRCRAccPoint(point.get(i));
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        /**
         * Find RCR Point to Top 2 Point
         */
        int TopBandwith = 2;
        for(int i=0;i<point.size();i++){
            if(i == TopBandwith)
                break;
            
            if(RCR < point.get(i).point && point.get(i).data > 0)
                RCR = point.get(i).point;
        }
        
        if(RCR < 0)
            RCR = resetPoint;
        
        return RCR;
    }
    
    /**
     * @deprecated 
     * Last Update 07_22_2012
     * New Algorithm
     * K-based RCR Searching Algorithm
     * @param sdata
     * @param sPoint
     * @param ePoint
     * @return 
     */
    private int calculateRCRusingSRTF_default(double[] sdata, double[] k_data, int sPoint, int ePoint) {
        int RCR = -1;
        /**
         * Searching K
         */
        sPoint = searchingsPoint(sdata,k_data,sPoint,ePoint);
        System.out.println("K Con Point : "+sPoint);
        
        result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(sPoint,k_data[sPoint]));
        
        ArrayList<SRTEResult.ResultRCRAccPoint> point = calculatePoint(sdata,sPoint,ePoint,true,true);
            
            
        System.out.println("RCR : sPoint :" + sPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            result.AddRCRAccPoint(point.get(i));
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        /**
         * Find RCR Point to Top 2 Point
         */
        int TopBandwith = config.RCRTopBandwith;
        for(int i=0;i<point.size();i++){
            if(i == TopBandwith)
                break;
            
            if(RCR < point.get(i).point && point.get(i).data > 0)
                RCR = point.get(i).point;
        }
        
        if(RCR < 0)
            RCR = sPoint;
        
        return RCR;
    }
    
    /**
     * @deprecated 
     * @param sdata
     * @param sPoint
     * @param ePoint
     * @return 
     */
    private int calculateRCRusingSRTF_justfindkeyPoint(double[] sdata, int sPoint, int ePoint) {
        int RCR = -1;

        /**
         * Find AccPoint ASC
         */
        ArrayList<SRTEResult.ResultRCRAccPoint> point = calculatePoint(sdata,sPoint,ePoint,false,true);
        
        System.out.println("RCR Key : sPoint :" + sPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        
        if(point.size() <= 0)
            return sPoint;
        
        /**
         * Research Point from ResetPoint
         */
        int resetPoint = sPoint;
        if(point.get(0).data < 0){
            resetPoint = point.get(0).point;
                /**
                * add ResetPoint
                */
            result.AddRCRAccPoint(point.get(0));
        }else{
            result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(sPoint,-1));
        }
        point = calculatePoint(sdata,resetPoint,ePoint,true,true);
            
            
        System.out.println("RCR : sPoint :" + resetPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            result.AddRCRAccPoint(point.get(i));
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        /**
         * Find RCR Point to Top 2 Point
         */
        int TopBandwith = 2;
        for(int i=0;i<point.size();i++){
            if(i == TopBandwith)
                break;
            
            if(RCR < point.get(i).point && point.get(i).data > 0)
                RCR = point.get(i).point;
        }
        
        if(RCR < 0)
            RCR = resetPoint;
        
        return RCR;
    }
    
    /**
     * @deprecated 
     * @param sdata
     * @param sPoint
     * @param ePoint
     * @return 
     */
    private int calculateRCRusingSRTF_old_findKeyPointandResearch(double[] sdata, int sPoint, int ePoint) {
        int RCR = -1;

        /**
         * Find AccPoint ASC
         */
        ArrayList<SRTEResult.ResultRCRAccPoint> point = calculatePoint(sdata,sPoint,ePoint,false,false);
        
        System.out.println("RCR Key : sPoint :" + sPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        
        if(point.size() <= 0)
            return sPoint;
        
        /**
         * Research Point from ResetPoint
         * Find 2 continuously subtraction Point 
         */
        int resetPoint = sPoint; //reset Point
        
        int MinusCount = 0; //MinusCount
        for(int i = 0;i<point.size();i++){
            if(point.get(i).data < 0)
                MinusCount ++;
            else
                MinusCount = 0;
            
            
            if(MinusCount > (config.RCRNofM - 1)){
                int tp = i;
                if(i == 0)
                    tp = i;
                else
                    tp = i-((int)config.RCRNofM-1);
                
                /**
                * add ResetPoint
                */
                resetPoint = point.get(tp).point;
                result.AddRCRAccPoint(point.get(tp));
                break;
            }
            
            
        }
        
        if(result.getRCRAccPointList().size() <= 0)
            result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(sPoint,-1));
//        if(point.get(0).data < 0){
//                resetPoint = point.get(0).point;
//                /**
//                * add ResetPoint
//                */
//                result.AddRCRAccPoint(point.get(0));
//            }else{
//                result.AddRCRAccPoint(new SRTEResult.ResultRCRAccPoint(sPoint,-1));
//            }
        
        point = calculatePoint(sdata,resetPoint,ePoint,true,true);
        
        
        System.out.println("RCR : sPoint :" + resetPoint + " - ePoint : "+ ePoint);
        for(int i=0;i<point.size();i++){
            result.AddRCRAccPoint(point.get(i));
            System.out.println("RCRPoint -> " + point.get(i).data + "("+point.get(i).point+")");
        }
        
        /**
         * Find RCR Point to Top 2 Point
         */
        int TopBandwith = 2;
        for(int i=0;i<point.size();i++){
            if(i == TopBandwith)
                break;
            
            if(RCR < point.get(i).point && point.get(i).data > 0)
                RCR = point.get(i).point;
        }
        
        if(RCR < 0)
            RCR = resetPoint;
        
        return RCR;
    }
    
    /**
     * 
     * @param sdata
     * @param sPoint
     * @param ePoint
     * @param b true : DESC, false : ASC
     * @param isSort true : Sort, false : unSort
     * @return 
     */
    private ArrayList<ResultRCRAccPoint> calculatePoint(double[] sdata, int sPoint, int ePoint, boolean b, boolean isSort) {
        ArrayList<SRTEResult.ResultRCRAccPoint> point = new ArrayList<SRTEResult.ResultRCRAccPoint>();
        
        /**
         * if sPoint <--not 2point--> ePoint
         */
//        int eTime = 1;
//        int g = ePoint - sPoint;
//        if(g < eTime){
//            if(ePoint + (eTime - g) > sdata.length)
//                ePoint = sdata.length;
//            else ePoint += (eTime - g);
//        }
        
        int eTime = 1;
        /**
         * Caculate Last Point
         */
        if(ePoint + eTime <= sdata.length)
            ePoint += eTime;
        
        for(int i=sPoint; i<=ePoint;i++){
            if(i < sPoint + 2)
                continue;
            
            SRTEResult.ResultRCRAccPoint p = new SRTEResult.ResultRCRAccPoint();
            p.data = (Math.abs(sdata[i]-sdata[i-1]) - Math.abs(sdata[i-1]-sdata[i-2]));
            p.point = i-1;
            if(b)
                p.setDesc();
            else
                p.setASC();
            
            point.add(p);
        }
        
        /**
         * sort Points
         */
        if(isSort)
            Collections.sort(point);
        
        return point;
    }

    /**
     * find SRT
     * @param qData
     * @param qK
     * @param result 
     */
    private void findSRT(double[] qData, double[] qK, SRTEResult result) {
        int sPoint = result.getcurrentPoint().rst;
        int ePoint = qData.length;
        int[] srt = new int[2];
        int csrt = -1;
        int reSRT = sPoint;
        for(int i=getTimestemp(3600);i>0;i--){
            srt[0] = findSRTF(sPoint,ePoint,i,qData,result);
            if(srt[0] != -1)
                break;
        }
        srt[1] = findSRTC(sPoint,ePoint,qData,qK,result.getcurrentPoint());
        
        for(int psrt : srt){
            System.out.println("list srt -> "+psrt);
            result.getcurrentPoint().srt.add(psrt);
            if(result.getcurrentPoint().csrt > psrt && psrt > 0){
                result.getcurrentPoint().csrt = psrt;
            }
        }
        
        if(result.getcurrentPoint().csrt == Integer.MAX_VALUE)
            result.getcurrentPoint().csrt = -1;
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
     * Find SRTF
     * @param sPoint
     * @param ePoint
     * @param timestemp
     * @param qData
     * @param result
     * @return 
     */
    private int findSRTF(int sPoint, int ePoint, int timestemp, double[] qData, SRTEResult result) {
        
        double lSpeed = getLimitSpeed(qData,result,false);
        
        int stempCnt = 0;
        int currentSrt = 0;
        int srt = sPoint;
        double CstSpeed = 0;
        
        double MaxSpeedHistory = 0;
        System.out.println("speed :" + lSpeed);
        //Find SRTF
        for(int i=sPoint;i<ePoint;i++){
            if(CstSpeed != qData[i] || i == (ePoint-1)){
                if(stempCnt >= timestemp){
//                    if(qData[currentSrt] > qData[srt]){
                    if(qData[currentSrt] >= lSpeed && qData[currentSrt] >= MaxSpeedHistory){
                        srt = currentSrt;
                        return srt;
                    }
                }
                CstSpeed = qData[i];
                currentSrt = i;
                stempCnt = 0;
            }
            else
                stempCnt ++;
            if(MaxSpeedHistory < qData[i])
                MaxSpeedHistory = qData[i];
        }
        if(srt != sPoint)
            return srt;
        else return -1;
    }

    /**
     * Find SRTC
     * @param sPoint
     * @param ePoint
     * @param qData
     * @param qK
     * @param currentPoint
     * @return 
     */
    private int findSRTC(int sPoint, int ePoint, double[] qData, double[] qK, ResultPoint currentPoint) {
        boolean isDetect = false;
        int currentSrt = 0;
        int srt = sPoint;
        //Find SRTC
        for(int i=sPoint;i<ePoint;i++){
            if(i == sPoint)
                continue;
            
            if(isDetect){
                double deltaU = (qData[i] - qData[i-1]);
                if(deltaU > 0){
                    int aftersrt = i-1;
                    
                    double deltaK = (qK[aftersrt] -qK[currentSrt]);
                    if(deltaK > 0){
                        currentPoint.srtc.add(currentSrt);
//                        if(qData[srt] < qData[currentSrt] || srt == sPoint){
//                            srt = currentSrt;
//                        }
                    }
                    
                    isDetect = false;
                }
            }else{
                double deltaU = (qData[i] - qData[i-1]);
                if(deltaU < 0){
                    currentSrt = i-1;
                    isDetect = true;
                }
            }
        }
        
        if(currentPoint.srtc.size() > 0){
            srt = AdjustSRTC(currentPoint.srtc,qData,selectedStation.getSpeedLimit());
            if(srt != -1)
                return srt;
            else
                return AdjustSRTC(currentPoint.srtc,qData,Double.MAX_VALUE);
        }else{
            return -1;
        }
            
        
    }
    
    private int AdjustSRTC(ArrayList<Integer> srtc,double[] data, double speedLimit){
        int csrtc = -1;
        for(int tsrt : srtc){
                if(csrtc < tsrt && data[tsrt] < speedLimit)
                    csrtc = tsrt;
        }
        return csrtc;
    }
    
    /**
     * get Limit Speed
     * @param qData
     * @param result1
     * @return 
     */
    private double getLimitSpeed(double[] qData, SRTEResult result,boolean useOrigin) {
        double LSpeed = MinSpeed;
        
        /**
         * using SRST
         */
//        int SRST = result.getcurrentPoint().srst;
//        if(SRST != -1){
//            double tempspeed = qData[SRST] - MaximumSpeedInterval;
//            LSpeed = tempspeed < LSpeed ? LSpeed : tempspeed;
//        }
        
        /**
        * set MinSpeed
        * Minimum SRTF Value
        */
        double dSpeed = 3;
        LSpeed = selectedStation.getSpeedLimit();
        if(!useOrigin && LSpeed >= 60)
            LSpeed = selectedStation.getSpeedLimit()-dSpeed;
        return LSpeed;
    }

    private int searchingsPoint(double[] sdata, double[] k_data, int sPoint, int ePoint) {
        int timestemp = (int)config.RCRNofM-1;
        int stempCnt = 0;
        int currentPoint = 0;
        double CstK = 0;
        int point = sPoint;
        ePoint = k_data.length;
        System.out.println("RCR timestemp -> "+timestemp);
        //Find K continously point
        for(int i=sPoint;i<ePoint;i++){
            if(CstK != k_data[i] || i == (ePoint-1)){
                if(stempCnt >= timestemp){
//                    if(qData[currentSrt] > qData[srt]){
                    point = currentPoint;
                    System.out.println("get!!! : "+stempCnt + " " + point + "-"+currentPoint);
                    
                    if(point > ePoint)
                        return sPoint;
                    else
                        return point;
                }
                CstK = k_data[i];
                currentPoint = i;
                stempCnt = 0;
            }
            else
                stempCnt ++;
            System.out.println("RCR K -> ["+i+"]"+k_data[i]+" "+stempCnt );
        }
        
        return point;
    }

    

    

    

    

    /**
     * class representing speed change point
     */
    class Tick {
        int pos = 0;    // index
        int tick_pattern = 0;   // pattern at tick
        int phase_2_count = 0;    // count of phase 2 after tick
    }
    
    enum SMOOTHING{
        NOSMOOTHING(0,"NO SMOOTHING",0),
        SECOND(1,"2 Window Filtering",2),
        DEFAULT(2,"3 Window Filtering",1),
        MORE(3,"More..",0);
        
        int Num = 2;
        String desc = "";
        int count = 0;
        
        SMOOTHING(int icn, String desc, int cnt){
            Num = icn;
            this.desc = desc;
            count = cnt;
        }
        
        public static SMOOTHING getSmooth(int idx){
            for(SMOOTHING i : SMOOTHING.values())
                if(i.getIndex() == idx) return i;
            return null;
        }
        
        public int getCount(){return count;}
        public int getIndex(){return Num;}
        public boolean isNOSMOOTHING(){return this==NOSMOOTHING;}
        public boolean isSecond(){return this==SECOND;}
        public boolean isDefault(){return this==DEFAULT;}
        public boolean isMore(){return this==MORE;}
        
        @Override
        public String toString(){
            return desc;
        }
    }
}
