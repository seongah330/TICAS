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

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com) (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEPatternSearch {

    HashMap<String, Integer> phaseMap = new HashMap<String, Integer>();

    /**
     * @deprecated
     */
    String[] patternToFind = new String[]{"12", "32222"};

    /**
     * Constructor
     */
    public SRTEPatternSearch() {
        // kqu
        
        // pattern 1 condtions
        //   -1 : denisty decreases
        //   -1 : flow decreases
        //    1 : speed increases
        phaseMap.put("-1-11", 1);

        // pattern 2 conditions
        //  1 : density increases
        //  1 : flow increses
        //  1 : speed increases
        phaseMap.put("111", 2);


        //  1 : density increases
        //  1 : flow increses
        //  0 : no pattern
        phaseMap.put("110", 2);

        //  1 : density increases
        //  1 : flow increses
        //  0 : speed decreases
        phaseMap.put("11-1", 2);



        // pattern 3 conditions
        //  -1: density decreases
        //   1: flow increses
        //   1: speed increases
        phaseMap.put("-111", 3);
    }

    /**
     *
     * Return pattern and transition points
     * 
     * @see SRTEProcess.findSRT()
     *
     * @param section
     * @param period
     * @return
     */
    public Object[] getPatternAndTransitionpoint(Section section, Period period) {

        // set interval of period to 15min interval
        period.setInterval(Interval.I15MIN.second);

        // load data
        section.loadData(period, false);

        // station list
        Station[] stations = section.getStations();

        // data count for given period
        int dataLength = stations[0].getDensity().length;
        System.out.println("data length = " + dataLength);

        // data buffer to save all station data
        double[][] speed_2d = new double[stations.length][];
        double[][] density_2d = new double[stations.length][];
        double[][] flow_2d = new double[stations.length][];

        // fill data buffer
        for (int i = 0; i < stations.length; i++) {
            Station station = stations[i];
            speed_2d[i] = station.getSpeed();
            density_2d[i] = station.getDensity();
            flow_2d[i] = station.getFlow();
        }

        // data buffer for route-wide average data
        double[] speed = new double[dataLength];
        double[] density = new double[dataLength];
        double[] flow = new double[dataLength];

        // calculate average
        for (int i = 0; i < dataLength; i++) {
            double u_sum = 0, k_sum = 0, q_sum = 0;
            for (int j = 0; j < stations.length; j++) {
                u_sum += speed_2d[j][i];
                k_sum += density_2d[j][i];
                q_sum += flow_2d[j][i];
            }
            speed[i] = u_sum / stations.length;
            density[i] = k_sum / stations.length;
            flow[i] = q_sum / stations.length;
        }

        // smoothing
        double[] smoothed_speed = smoothing(speed);
        double[] smoothed_density = smoothing(density);
        double[] smoothed_flow = smoothing(flow);

        // get pattern : increase(1), decrease(-1) or same(0)
        int[] speed_pattern = getPattern(smoothed_speed);
        int[] density_pattern = getPattern(smoothed_density);
        int[] flow_pattern = getPattern(smoothed_flow);

        // pattern phases according to pattern map (see member variable => phaseMap)
        int[] phases = decidePhase(speed_pattern, density_pattern, flow_pattern);

        // find transition points with smoothed data that phase pattern is '1111'
        ArrayList<Integer> transitionPoints = findTransitionPoints(phases, smoothed_density, smoothed_flow);

        // decide phases 1, 2, 3,0
        // array[0] : phases
        // array[1] : transition points list that phase pattern is '1111'
        return new Object[]{phases, transitionPoints};
    }


    /**
     *
     * Find transition point that has max slope in the section phase pattern is '1111'
     *
     * @param phases
     * @param density
     * @param flow
     * @return
     */
    private ArrayList<Integer> findTransitionPoints(int[] phases, double[] density, double[] flow) {

        // 'phases' (int array) to string
        String pattern_string = getString(phases);

        // buffer to save results
        ArrayList<Integer> points = new ArrayList<Integer>();

        // points that phase pattern is '1111'
        ArrayList<Integer> pattern_1111_section = transitionPoints(pattern_string, "1111");

        // temp variable for the next loop
        int stick = -1;
        int maxCoverage = -1;
        int transitionPoint = -1;

        // find max slope point in the pattern-1111-sections
        for (Integer idx : pattern_1111_section) {
            if (idx < stick) {
                continue;
            }

            // count of contineous '1' pattern
            int areaCoverage = 0;

            // max slope diff
            double maxSlopeDiff = -9999999;

            // index of max slope diff point
            int maxSlopeDiffIdx = -1;


            int i;            
            for (i = idx + 1; i < flow.length - 2; i++) {

                // if next point's pattern is not '1', break
                if (phases[i + 1] != 1) {
                    break;
                }

                // calculate slopes and diff
                double firstSlope = (flow[i - 1] - flow[i]) / (density[i - 1] - density[i]);
                double secondSlope = (flow[i] - flow[i + 1]) / (density[i] - density[i + 1]);
                double slopeDiff = secondSlope - firstSlope;

                // update max slope and diff
                if (slopeDiff > maxSlopeDiff) {
                    maxSlopeDiff = slopeDiff;
                    maxSlopeDiffIdx = i;
                }

                // move stick to 'i'
                stick = i;
            }

            // update coverage
            areaCoverage = i - idx;

            // if there's max slope and its coverage > max coverage,
            // update transition point
            if (maxSlopeDiffIdx > 0 && areaCoverage >= maxCoverage) {
                System.out.println("point 2 : " + idx + " => coverage = " + areaCoverage);
                transitionPoint = maxSlopeDiffIdx;
                maxCoverage = areaCoverage;
            }
        }

        // if there's transition point,
        // add transition point to result list
        // points[0] : coverage
        // points[1] : point index
        if (transitionPoint > 0) {
            points.add(maxCoverage);
            points.add(transitionPoint);
        }
        
        return points;
    }

    /**
     *
     * Find 'pattern' occurrence points in given 'phaseString'
     *
     * @param phaseString phase string i.e. 0000111222220000
     * @param pattern pattern string to find i.e. 12, 3222
     * @return
     */
    private ArrayList<Integer> transitionPoints(String phaseString, String pattern) {

        int idx = 0;
        int cur = 0;

        // transition point can be multiple point
        ArrayList<Integer> points = new ArrayList<Integer>();

        // find 'pattern' from the 'phaseString' with String.indexOf method
        while ((idx = phaseString.indexOf(pattern, cur)) >= 0) {
            points.add(idx);
            cur = idx + pattern.length();
        }
        return points;
    }


    /**
     * 
     * Return pattern phase list according to pattern
     * 
     * @param speed_pattern 
     * @param density_pattern
     * @param flow_pattern
     * @return
     */
    private int[] decidePhase(int[] speed_pattern, int[] density_pattern, int[] flow_pattern) {

        int len = speed_pattern.length;
        int[] phases = new int[len];

        for (int i = 0; i < len; i++) {
            // make pattern string with density, flow and speed at current iteration
            String pattern = String.format("%d%d%d", density_pattern[i], flow_pattern[i], speed_pattern[i]);

            // decide phase according to pattern string
            Integer phase = phaseMap.get(pattern);

            // if no phase, phase is set to 0
            if (phase == null) {
                phase = 0;  // no pattern
            }

            // insert the decided phase to phases list
            phases[i] = phase;
        }
        return phases;
    }


    /**
     * Return pattern array
     *   - increase : 1
     *   - same : 0
     *   - decrease : -1
     *
     * @param data
     * @return
     */
    private int[] getPattern(double[] data) {
        int len = data.length;
        int[] pdata = new int[len];
        pdata[0] = 0;
        for (int i = 1; i < data.length; i++) {
            double diff = data[i] - data[i - 1];
            if (diff > 0) {
                pdata[i] = 1;
            } else if (diff < 0) {
                pdata[i] = -1;
            } else {
                pdata[i] = 0;
            }
        }
        return pdata;
    }

    /**
     *
     * Return string converted from int array
     * 
     * @param array integer array
     * @return string
     */
    private String getString(int[] array) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            result.append(array[i]);
        }
        return result.toString();
    }

    /**
     * Return moving average data
     * @param data
     * @return
     */
    private double[] smoothing(double[] data) {

        int len = data.length;
        
        // result buffer
        double[] fdata = new double[len];

        // first data
        fdata[0] = (data[0] + data[1]) / 2;

        // last data
        fdata[len - 1] = (data[len - 1] + data[len - 2]) / 2;

        // rest data
        // moving average (window = 3)
        for (int i = 1; i < len - 1; i++) {
            fdata[i] = (data[i - 1] + data[i] + data[i + 1]) / 3;
        }
        
        return fdata;
    }







    

    ////////////////////////////////////////////////////
    // NOT USED METHODS
    // (previously used for standalone mode)
    ////////////////////////////////////////////////////


    /**
     * @deprecated 
     * @param log
     * @param label
     * @param data
     */
    private void addLog(Logger log, String label, double[] data) {
        log.print(label);
        for (double d : data) {
            log.print("," + d);
        }
        log.println();
    }

    /**
     * @deprecated 
     * @param log
     * @param label
     * @param data
     */
    private void addLog(Logger log, String label, int[] data) {
        log.print(label);
        for (int d : data) {
            log.print("," + d);
        }
        log.println();
    }

    /**
     * @deprecated 
     * @param log
     * @param label
     * @param data
     */
    private void addLog(Logger log, String label, Object[] data) {
        log.print(label);
        for (Object d : data) {
            log.print("," + d);
        }
        log.println();
    }

    /**
     * @deprecated 
     * @param speed
     * @return
     */
    private int lowestSpeedPoint(double[] speed) {

        int lowestIdx = 0;
        double lowestU = 999;
        for (int i = 0; i < speed.length; i++) {
            if (speed[i] < lowestU) {
                lowestIdx = i;
                lowestU = speed[i];
            }
        }
        return lowestIdx;
    }

    /**
     * @deprecated
     * @param section
     * @param period
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void start(Section section, Period period) throws OutOfMemoryError, Exception {
        Logger log = new Logger("pattern_" + period.getPeriodString() + "_" + section.getName(), "csv");
        period.setInterval(Interval.I15MIN.second);
        section.loadData(period, false);

        Station[] stations = section.getStations();
        int dataLength = stations[0].getDensity().length;
        double[][] speed_2d = new double[stations.length][];
        double[][] density_2d = new double[stations.length][];
        double[][] flow_2d = new double[stations.length][];

        for (int i = 0; i < stations.length; i++) {
            Station station = stations[i];
            speed_2d[i] = station.getSpeed();
            density_2d[i] = station.getDensity();
            flow_2d[i] = station.getFlow();
        }

        double[] speed = new double[dataLength];
        double[] density = new double[dataLength];
        double[] flow = new double[dataLength];

        for (int i = 0; i < dataLength; i++) {
            double u_sum = 0, k_sum = 0, q_sum = 0;
            for (int j = 0; j < stations.length; j++) {
                u_sum += speed_2d[j][i];
                k_sum += density_2d[j][i];
                q_sum += flow_2d[j][i];
            }
            speed[i] = u_sum / stations.length;
            density[i] = k_sum / stations.length;
            flow[i] = q_sum / stations.length;
        }

        this.search(section.getName(), speed, density, flow, period, log);
    }

    /**
     * @deprecated
     * @param section
     * @param period
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void start_by_occupancy(Section section, Period period) throws OutOfMemoryError, Exception {
        Logger log = new Logger("pattern_by_occupancy_" + period.getPeriodString() + "_" + section.getName(), "csv");
        period.setInterval(Interval.I15MIN.second);
        section.loadData(period, false);

        Station[] stations = section.getStations();
        int dataLength = stations[0].getDensity().length;
        double[][] speed_2d = new double[stations.length][];
        double[][] occupancy_2d = new double[stations.length][];
        double[][] flow_2d = new double[stations.length][];

        for (int i = 0; i < stations.length; i++) {
            Station station = stations[i];
            speed_2d[i] = station.getSpeed();
            occupancy_2d[i] = station.getOccupancy();
            flow_2d[i] = station.getFlow();
        }

        double[] speed = new double[dataLength];
        double[] occupancy = new double[dataLength];
        double[] flow = new double[dataLength];

        for (int i = 0; i < dataLength; i++) {
            double u_sum = 0, k_sum = 0, q_sum = 0;
            for (int j = 0; j < stations.length; j++) {
                u_sum += speed_2d[j][i];
                k_sum += occupancy_2d[j][i];
                q_sum += flow_2d[j][i];
            }
            speed[i] = u_sum / stations.length;
            occupancy[i] = k_sum / stations.length;
            flow[i] = q_sum / stations.length;
        }

        this.search_by_occupancy(section.getName(), speed, occupancy, flow, period, log);
    }

    /**
     * @deprecated
     * @param station
     * @param period
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void start(Station station, Period period) throws OutOfMemoryError, Exception {
        Logger log = new Logger("pattern_" + period.getPeriodString() + "_" + station.getStationId(), "csv");
        period.setInterval(Interval.I15MIN.second);
        station.loadData(period, false);
        double[] speed = station.getSpeed();
        double[] density = station.getDensity();
        double[] flow = station.getFlow();

        this.search(station.getStationId(), speed, density, flow, period, log);
    }

    /**
     * @deprecated
     * @param station
     * @param period
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void start_by_occupancy(Station station, Period period) throws OutOfMemoryError, Exception {
        Logger log = new Logger("pattern_by_occupancy_" + period.getPeriodString() + "_" + station.getStationId(), "csv");
        period.setInterval(Interval.I15MIN.second);
        station.loadData(period, false);
        double[] speed = station.getSpeed();
        double[] occupancy = station.getOccupancy();
        double[] flow = station.getFlow();

        this.search_by_occupancy(station.getStationId(), speed, occupancy, flow, period, log);
    }

    /**
     * @deprecated
     * @param name
     * @param speed
     * @param density
     * @param flow
     * @param period
     * @param log
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void search(String name, double[] speed, double[] density, double flow[], Period period, Logger log) throws OutOfMemoryError, Exception {

        int lowestPoint = lowestSpeedPoint(speed);

        double[] after_lowest_speed = Arrays.copyOfRange(speed, lowestPoint, speed.length);
        double[] after_lowest_density = Arrays.copyOfRange(density, lowestPoint, density.length);
        double[] after_lowest_flow = Arrays.copyOfRange(flow, lowestPoint, flow.length);

        if (after_lowest_speed.length < 2) {
            System.err.println("ERROR : " + name + " -> No available lowest point");
            return;
        }

        // smoothing
        double[] smoothed_speed = smoothing(after_lowest_speed);
        double[] smoothed_density = smoothing(after_lowest_density);
        double[] smoothed_flow = smoothing(after_lowest_flow);

        // get pattern : increase(1), decrease(-1) or same(0)
        int[] speed_pattern = getPattern(smoothed_speed);
        int[] density_pattern = getPattern(smoothed_density);
        int[] flow_pattern = getPattern(smoothed_flow);

        // decide phases (1~3, 0)
        int[] phases = decidePhase(speed_pattern, density_pattern, flow_pattern);
        int[] index = new int[after_lowest_speed.length];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        // find transition points : case 1
        ArrayList<Integer> transitionPoints1 = findTransitionPoints1(phases, smoothed_flow);

        // find transition points : case 2
        ArrayList<Integer> transitionPoints2 = findTransitionPoints(phases, smoothed_density, smoothed_flow);

        int point1_coverage = -1, point2_coverage = -1;
        if (!transitionPoints1.isEmpty()) {
            point1_coverage = transitionPoints1.get(0);
        }
        if (!transitionPoints2.isEmpty()) {
            point2_coverage = transitionPoints2.get(0);
        }
        ArrayList<Integer> transitionPoints = new ArrayList<Integer>();
        if (point2_coverage > 4) {
            transitionPoints.add(transitionPoints2.get(1));
        } else if (point1_coverage > point2_coverage) {
            transitionPoints.add(transitionPoints1.get(1));
        } else if (point2_coverage > point1_coverage) {
            transitionPoints.add(transitionPoints2.get(1));
        } else if (point1_coverage != -1) {
            transitionPoints.add(transitionPoints1.get(1));
            transitionPoints.add(transitionPoints2.get(1));
        }

        String[] original_timeline = period.getTimelineJustTime();
        String[] timeline = Arrays.copyOfRange(original_timeline, lowestPoint, original_timeline.length);

        addLog(log, "time", original_timeline);

        addLog(log, "density(15min)", density);
        addLog(log, "flow(15min)", flow);
        addLog(log, "speed(15min)", speed);

        addLog(log, "time", timeline);
        addLog(log, "density(after lowest)", after_lowest_density);
        addLog(log, "flow(after lowest)", after_lowest_flow);
        addLog(log, "speed(after lowest)", after_lowest_speed);

        addLog(log, "time", timeline);
        addLog(log, "density(smoothed)", smoothed_density);
        addLog(log, "flow(smoothed)", smoothed_flow);
        addLog(log, "speed(smoothed)", smoothed_speed);

        addLog(log, "time", timeline);
        addLog(log, "density_pattern", density_pattern);
        addLog(log, "flow_pattern", flow_pattern);
        addLog(log, "speed_pattern", speed_pattern);

        addLog(log, "time", timeline);
        addLog(log, "phases", phases);

        //addLog(log, "index", index);
        //addLog(log, "time", timeline);

        int transitionCount = transitionPoints.size();
        String[] transitionTimes = new String[transitionCount];
        double[] transitionSpeeds = new double[transitionCount];
        for (int i = 0; i < transitionCount; i++) {
            transitionTimes[i] = timeline[transitionPoints.get(i)];
            transitionSpeeds[i] = smoothed_speed[transitionPoints.get(i)];
        }

        //addLog(log, "transition point time", new String[]{timeline[transitionPoints[0]]});
        addLog(log, "transition time", transitionTimes);
        //addLog(log, "transition point", transitionPoints.toArray(new Integer[transitionPoints.size()]));
        addLog(log, "speed at TP", transitionSpeeds);


        log.writeLog();
    }

    /**
     * @deprecated
     * @param name
     * @param speed
     * @param occupancy
     * @param flow
     * @param period
     * @param log
     * @throws OutOfMemoryError
     * @throws Exception
     */
    public void search_by_occupancy(String name, double[] speed, double[] occupancy, double flow[], Period period, Logger log) throws OutOfMemoryError, Exception {

        int lowestPoint = lowestSpeedPoint(speed);

        double[] after_lowest_speed = Arrays.copyOfRange(speed, lowestPoint, speed.length);
        double[] after_lowest_occupancy = Arrays.copyOfRange(occupancy, lowestPoint, occupancy.length);
        double[] after_lowest_flow = Arrays.copyOfRange(flow, lowestPoint, flow.length);

        if (after_lowest_speed.length < 2) {
            System.err.println("ERROR : " + name + " -> No available lowest point");
            return;
        }

        // smoothing
        double[] smoothed_speed = smoothing(after_lowest_speed);
        double[] smoothed_occupancy = smoothing(after_lowest_occupancy);
        double[] smoothed_flow = smoothing(after_lowest_flow);

        // get pattern : increase(1), decrease(-1) or same(0)
        int[] speed_pattern = getPattern(smoothed_speed);
        int[] occupancy_pattern = getPattern(smoothed_occupancy);
        int[] flow_pattern = getPattern(smoothed_flow);

        // decide phases (1~3, 0)
        int[] phases = decidePhase(speed_pattern, occupancy_pattern, flow_pattern);
        int[] index = new int[after_lowest_speed.length];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }

        // find transition points : case 1
        ArrayList<Integer> transitionPoints1 = findTransitionPoints1(phases, smoothed_flow);

        // find transition points : case 2
        ArrayList<Integer> transitionPoints2 = findTransitionPoints(phases, smoothed_occupancy, smoothed_flow);

        int point1_coverage = -1, point2_coverage = -1;
        if (!transitionPoints1.isEmpty()) {
            point1_coverage = transitionPoints1.get(0);
        }
        if (!transitionPoints2.isEmpty()) {
            point2_coverage = transitionPoints2.get(0);
        }

        ArrayList<Integer> transitionPoints = new ArrayList<Integer>();
        if (point2_coverage > 4) {
            transitionPoints.add(transitionPoints2.get(1));
        } else if (point1_coverage > point2_coverage) {
            transitionPoints.add(transitionPoints1.get(1));
        } else if (point2_coverage > point1_coverage) {
            transitionPoints.add(transitionPoints2.get(1));
        } else if (point1_coverage != -1) {
            transitionPoints.add(transitionPoints1.get(1));
            transitionPoints.add(transitionPoints2.get(1));
        }

        String[] original_timeline = period.getTimelineJustTime();
        String[] timeline = Arrays.copyOfRange(original_timeline, lowestPoint, original_timeline.length);

        addLog(log, "time", original_timeline);

        addLog(log, "occupancy(15min)", occupancy);
        addLog(log, "flow(15min)", flow);
        addLog(log, "speed(15min)", speed);

        addLog(log, "time", timeline);
        addLog(log, "occupancy(after lowest)", after_lowest_occupancy);
        addLog(log, "flow(after lowest)", after_lowest_flow);
        addLog(log, "speed(after lowest)", after_lowest_speed);

        addLog(log, "time", timeline);
        addLog(log, "occupancy(smoothed)", smoothed_occupancy);
        addLog(log, "flow(smoothed)", smoothed_flow);
        addLog(log, "speed(smoothed)", smoothed_speed);

        addLog(log, "time", timeline);
        addLog(log, "occupancy", occupancy_pattern);
        addLog(log, "flow_pattern", flow_pattern);
        addLog(log, "speed_pattern", speed_pattern);

        addLog(log, "time", timeline);
        addLog(log, "phases", phases);

        //addLog(log, "index", index);
        //addLog(log, "time", timeline);

        int transitionCount = transitionPoints.size();
        String[] transitionTimes = new String[transitionCount];
        double[] transitionSpeeds = new double[transitionCount];
        for (int i = 0; i < transitionCount; i++) {
            transitionTimes[i] = timeline[transitionPoints.get(i)];
            transitionSpeeds[i] = smoothed_speed[transitionPoints.get(i)];
        }

        //addLog(log, "transition point time", new String[]{timeline[transitionPoints[0]]});
        addLog(log, "transition time", transitionTimes);
        //addLog(log, "transition point", transitionPoints.toArray(new Integer[transitionPoints.size()]));
        addLog(log, "speed at TP", transitionSpeeds);


        log.writeLog();
    }

    // pattern : 1->2, 3->2
    /**
     * @deprecated
     * @param phases
     * @param flow
     * @return
     */
    private ArrayList<Integer> findTransitionPoints1(int[] phases, double[] flow) {

        // multiple points that satisfy transition conditions
        ArrayList<Integer> candidates = new ArrayList<Integer>();

        // convert integer array to string
        String pattern_string = getString(phases);

        for (String transitionPattern : patternToFind) {
            ArrayList<Integer> p = transitionPoints(pattern_string, transitionPattern);
            if (!p.isEmpty()) {
                candidates.addAll(p);
            }
        }
        //return candidates;
        return decideTransitionPoints1(candidates, phases, flow);
    }

    /**
     *
     * ajsdkfljaskdl adlsjf aklsdjf aklsdfj aklsd jfakls djf
     * asdjfklas jdfkal dsafk ;lasd fk
     * ads fjklas dfjakls asdf
     *
     * @deprecated
     * @param candidates asfjklasd ajsdklfjk asl d
     * @param phases  asdf ajsdkfj
     * @param flow
     * @return
     */
    private ArrayList<Integer> decideTransitionPoints1(ArrayList<Integer> candidates, int[] phases, double[] flow) {

        ArrayList<Integer> transitionPoints = new ArrayList<Integer>();
        Collections.sort(candidates);

        int maxCoverage = -1;
        int maxCoverageIndex = -1;
        int maxPhases2Count = -1;

        int len = candidates.size();

        for (int ci = 0; ci < len; ci++) {

            int idx = candidates.get(ci);
            int toIdx = (ci == len - 1 ? flow.length - 1 : candidates.get(ci + 1));

            double baseFlow = flow[idx];
            int increaseCount = 0;
            int phases2Count = 0;
            // does it increase continuously after pattern ?
            for (int i = idx + 1; i <= toIdx; i++) {
                if (phases[i] == 2) {
                    phases2Count++;
                }
                if (baseFlow < flow[i]) {
                    increaseCount++;
                } else {
                    break;
                }
            }

            if (increaseCount > 3) {
                System.out.println("point 1 : " + idx + " => coverage = " + increaseCount);
                if (phases2Count > maxPhases2Count) {
                    maxPhases2Count = phases2Count;
                    maxCoverageIndex = idx;
                    maxCoverage = increaseCount;
                }
            }
        }
        transitionPoints.add(maxCoverage);
        transitionPoints.add(maxCoverageIndex);

        return transitionPoints;
    }

}
