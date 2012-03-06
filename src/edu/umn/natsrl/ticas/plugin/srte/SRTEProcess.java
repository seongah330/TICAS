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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com) (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEProcess {

    private int SMOOTHING_FILTERSIZE = 1;
    private int QUANTIZATION_THRESHOLD = 2;
    private int STEADY_TIMELSP = 8;
    
    private SRTEResult result = new SRTEResult();
    private double[] u15m;
    private final Section section;
    private Period period;

    /**
     * Main algorithm process
     *  - smoothing => Quantization => find SRST / LST / RST / SRT
     * @return
     */
    public SRTEResult stateDecision() {
        double filteredData[];
        double qData[];

        filteredData = smoothing(u15m);
        qData = quantization(filteredData);
        //filteredData = qData;

        result.srst = findSRST(qData, filteredData);
        result.addLog("SRST : " + result.srst);

        result.lst = findLST(qData, result.srst);
        result.addLog("LST : " + result.lst);

        // loop index i : increase count theshold for defining RST
        // example: if i= 4 and speed countinously increases 4 times from LST, the fist point of the speed change will be the RST
        for (int i = 4; i > 0; i--) {
            result.rst = findRST(qData, result.lst, filteredData, i);

            //if RST is found, break
            if (result.rst > 0) {
                break;
            }
        }
        result.addLog("RST : " + result.rst);

        findSRT(qData, result);

        for (int i = 0; i < result.srt.size(); i++) {
            System.err.println("SRT : " + result.srt.get(i));
        }

        result.name = this.section.getName();
        result.data_origin = u15m;
        result.data_smoothed = filteredData;
        result.data_quant = qData;

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
    private double[] quantization(double[] filteredData) {
        int stick = 0;
        double[] qData = new double[filteredData.length];
        for (int i = 0; i < 3; ++i) {
            stick += filteredData[i];
        }
        stick = stick / 3;

        for (int i = 0; i <= filteredData.length - 1; i++) {
            if ((Math.abs(stick - filteredData[i]) > QUANTIZATION_THRESHOLD)) {
                stick = (int) Math.round(filteredData[i]);
            }
            qData[i] = stick;
        }
        return qData;
    }

    /**
     * Find SRST
     *  - the first point of speed continuously decreases
     * @param qData : quantized data
     * @param smootedData : smoothed data
     * @return
     */
    private int findSRST(double[] qData, double[] smootedData) {
        result.addLog("Finding SRST..........");
        int declineCount = 0;
        int srst = 0;
        int endPoint = qData.length - 1;

        // decesion SRST if the point is met following condtions
        for (int i = 1; i <= endPoint; i++) {
            if (declineCount == 1) {
                //search back and find at the highest speed level in the smoothed data
                for (int j = srst; j > 0; j--) {
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
     *Find LST
     *  - speed reaches the low speed level
     *      -case 1: find the last decline point when speed starts to increases 2 times
     *      -case 2: find the last decline point when speed are stable for 2 hours since the last decline point
     * @param qData
     * @param srst
     * @return
     */
    private int findLST(double[] qData, int srst) {
        int lst = 0;
        int increaseCount = 0;
        int increaseCountB = 0;
        int steadyState = 0;
        int endPoint = qData.length-1;
        result.addLog("Finding LST..........");
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
     * Find RST when speed starts to continuously increase
     * @param qData
     * @param lst
     * @param filteredData
     * @param targetIncreaseCount = 4
     *  - increasing speed change point from LST
     * @return
     */
    private int findRST(double[] qData, int lst, double[] filteredData, int targetIncreaseCount) {
        int rst = 0;
        int increaseCount = 0;
        int steadyState = 0;
         int endPoint = qData.length - 1;
        //int decreaseCount = 0;
        result.addLog("Finding RST (" + targetIncreaseCount + ").............");
        for (int i = lst + 1; i <= endPoint - 1; i++) {
            if (increaseCount == targetIncreaseCount) {
                for (int j = rst - 1; j > lst; j--) {
                    if (filteredData[j] < qData[j]) {
                        rst = j;
                        result.addLog(" -> case 1");
                        return rst;
                    }
                }
                result.addLog(" -> case 2");
                return rst;
            }

            // increase the count if the speed at time t+1 is greater than the speed at time t
            if (qData[i - 1] < qData[i]) {
                //set srp if srp is not set yet
                increaseCount++;
                steadyState = 0;
                if (rst == 0) {
                    rst = i - 1;
                }
            }
            // set if the speed at time t+1 is the same as the speed at time t
            if (qData[i - 1] == qData[i]) {
                steadyState++;
            } else if (qData[i - 1] > qData[i]) {
                // reset srp
                rst = 0;
                increaseCount = 0;
                steadyState = 0;
            }
        }
        result.addLog(" -> case 3");
        return rst;
    }

    /**
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
    private void findSRT(double[] qData, SRTEResult result) {

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


        for (int i = result.rst; i < qData.length; i++) {
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
                if (firstPointAfterTrPoint < 0 && i >= trPoint) {
                    firstPointAfterTrPoint = i;
                }
                stickSpeed = u;
            }
            //increase stickTick.pashe_2)count  when traffic pattern 2 occurs
            if (phases[i] == 2) {
                stickTick.phase_2_count++;

                // set the fist point if patter 2 count is greater than 4
                if (stickTick.phase_2_count >= 4 && result.srt.isEmpty()) {
                    result.addLog(" -> case 1 : " + stickTick.phase_2_count + " / " + stickTick.pos);
                    result.srt.add(stickTick.pos);
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
            if (!result.srt.isEmpty()) {
                int p = result.srt.get(0);
                if (p != maxPhase2Point) {
                    result.addLog(" -> case 2 : " + maxPhase2Count + " / " + maxPhase2Point);
                    result.srt.add(maxPhase2Point);
                }
            }
        }
        // just return if SRT point is found
        if (!result.srt.isEmpty()) {
            return;
        }
        // if not, add result satisfying condition C
        if (firstPointAfterTrPoint > 0) {
            result.addLog(" -> case 3");
            result.srt.add(trPoint);
            result.srt.add(firstPointAfterTrPoint);
            return;
        }
        // if not, add max-phase-2-count point to result
        if (maxPhase2Point > 0) {
            result.addLog(" -> case 4");
            result.srt.add(maxPhase2Point);
            return;
        }

    }

    /**
     * class representing speed change point
     */
    class Tick {
        int pos = 0;    // index
        int tick_pattern = 0;   // pattern at tick
        int phase_2_count = 0;    // count of phase 2 after tick
    }

    /**
     * constructor
     * @param stationName
     * @param u5m
     * @param k15m
     */
    public SRTEProcess(Section section, Period period, double[] u15m, double[] k15m, SRTEConfig config) {
        this.section = section;
        this.period = period;
        this.u15m = u15m;
        
        // density setting
        result.k_origin = k15m;
        result.k_smoothed = smoothing(k15m);
        result.k_quant = quantization(result.k_smoothed);

        // config setting
        this.SMOOTHING_FILTERSIZE = config.getInt("SMOOTHING_FILTERSIZE");
        this.QUANTIZATION_THRESHOLD = config.getInt("QUANTIZATION_THRESHOLD");

    }
}
