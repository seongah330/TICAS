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

package edu.umn.natsrl.infra.infraobjects;

import edu.umn.natsrl.evaluation.EvaluationOption;
import edu.umn.natsrl.infra.*;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.infra.types.AdjustType;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.types.TrafficType;
import edu.umn.natsrl.infra.types.TransitionType;
import edu.umn.natsrl.map.CoordinateConversion;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Chongmyung Park
 */
public class RNode extends InfraObject {
       
    protected int distanceToDownstreamNode = -1;
    protected int distanceToUpstreamNode = -1;
    protected TransitionType transitionType = TransitionType.None;
    protected boolean isWavetronicsStation = false;
    protected boolean isAvailable = true;
    protected boolean isMissing = false;   
    protected double confidence = -1;    
        
    transient protected Corridor corridor;    
    transient protected RNode downstreamNode;
    transient protected RNode upstreamNode;
    transient protected RNode nextRNodeInSameCorridor;
    
    /**
     * Location
     */
    protected int easting = 0;
    protected int northing = 0;

    protected HashMap<String, Detector> detectors = new HashMap<String, Detector>();   
    
    public RNode(){
        
    }
    
    public RNode(Element element)
    {
        super(element);
        this.id = this.getProperty("name");
        this.transitionType = TransitionType.get(getProperty(InfraProperty.transition));
        this.infraType = InfraType.get(getProperty(InfraProperty.n_type));
        initDetectors(element);
        setLocation();
    }

    public static RNode create(Element element) {
        String type = element.getAttribute("n_type");
        String sid = element.getAttribute("station_id");
        
        
        if(type.equals("Station")) {
            String active = element.getAttribute("active");
            Station s = new Station(element);
            if(sid.isEmpty() || (!active.isEmpty() && active.equals("f"))) {
                s.isAvailable = false;
            }
            return s;
        }
        else if(type.equals("Entrance")) return new Entrance(element);
        else if(type.equals("Exit")) return new Exit(element);
        else if(type.equals("Interchange")) return new Interchange(element);
        else if(type.equals("Access")) return new Access(element);
        else if(type.equals("Intersection")) return new Intersection(element);
        return null;
    }
    
    public static RNode create(String id,String stationId, String label, int lanes, int easting, int northing, InfraType itype){
        if(itype.isStation())return new Station(id, stationId, label, lanes, easting, northing, itype);
        else if(itype.isEntrance()) return new Entrance(id, label, lanes, easting, northing, itype);
        else if(itype.isExit()) return new Exit(id, label, lanes, easting, northing, itype);
        return null;
    }

    public void loadData(Period period, DataLoadOption dopt) throws OutOfMemoryError
    {
        loadData(period,dopt,null);
    }
    
    public void loadData(Period period, DataLoadOption dopt, SimObjects sobj) throws OutOfMemoryError
    {
        for(Detector d : this.detectors.values())
        {
            if(sobj == null)
                d.loadData(period, dopt);
            else
                d.loadData(period, dopt,sobj);
        }
    }
    
    ////////////////////////////////////
    // Setter
    ////////////////////////////////////    
        
    public void setDownstreamNodeToOtherCorridor(RNode n) {
        if(n == null) return;
        this.downstreamNode = n;
        setProperty(InfraProperty.DownStreamNodeId, n.getId());
        
        n.setUpstreamNodeToOtherCorridor(this);
        int distance = calculateDistance(this.getEasting(), n.getEasting(), this.getNorthing(), n.getNorthing());
        this.distanceToDownstreamNode = distance;
    }

    private void setUpstreamNodeToOtherCorridor(RNode n) {
        this.upstreamNode = n;
        setProperty(InfraProperty.UpStreamNodeId, n.getId());
        
        int distance = calculateDistance(this.getEasting(), n.getEasting(), this.getNorthing(), n.getNorthing());
        this.distanceToUpstreamNode = distance;
    }
    
    public RNode getDownStreamNodeToOtherCorridor() {
        TMO tmo = TMO.getInstance();
        if(downstreamNode == null) downstreamNode = tmo.getInfra().getRNode(getProperty(InfraProperty.DownStreamNodeId));
        return this.downstreamNode;
    }

    public RNode getUpStreamNodeToOtherCorridor() {
        TMO tmo = TMO.getInstance();
        if(upstreamNode == null) upstreamNode = tmo.getInfra().getRNode(getProperty(InfraProperty.UpStreamNodeId));
        return this.upstreamNode;
    }    
    
    
    public void setCorridor(Corridor co) {
        this.corridor = co;
        setProperty(InfraProperty.CorridorId, co.getId());
    }

    public void setNextRNodeInSameCorridor(RNode n) {
        this.nextRNodeInSameCorridor = n;
        setProperty(InfraProperty.NextRNodeIdInSameCorridor, n.getId());
    }
    
    private void initDetectors(Element ele)
    {
        if(detectors != null && !detectors.isEmpty()) return;                
        if(detectors == null) detectors = new HashMap<String, Detector>();        
        
        NodeList detectorList = ele.getElementsByTagName(InfraObjects.detector.toString());
        if(detectorList.getLength() == 0){
//            System.out.println("do not find detector"); //debug test
            return;
        }
        
//        System.out.println("find Detector : "+detectorList.getLength()); //debug test
        TMO tmo = TMO.getInstance();
        for(int i=0;i<detectorList.getLength();i++){
            Element e = (Element)detectorList.item(i);
            String d = e.getAttribute(InfraProperty.name.toString());
            Detector det = tmo.getInfra().getDetector(d);
            if(det != null){
                detectors.put(det.getId(),det);
                det.setRNode(this);
            }
        }
    }
    
    ////////////////////////////////////
    // Getter
    ////////////////////////////////////
    
    public int getDataSize()
    {
        if(detectors.isEmpty()) return 0;
        else return detectors.get(0).getDataSize();
    }
    
    /**
     * @deprecated 
     * @return 
     */
    public String[] getDownstreamName()
    {
        return getPropertyArray(InfraProperty.downstream);        
    }
    
    public String[] getForkName(){
        return getPropertyArray(InfraProperty.forks);
    }

    public double[] getSpeed() { return getData(null, TrafficType.SPEEDFORSTATION); }
    public double[] getDensity() { return getData(null, TrafficType.DENSITY); }
    public double[] getFlow() { return getData(null, TrafficType.FLOW); }
    public double[] getFlowForAverageLaneFlow() { return getData(null, TrafficType.FLOWFORAVERAGE); } //modify soobin Jeon 02/15/2012
    public double[] getAverageLaneFlow() { return getData(null, TrafficType.AVERAGEFLOW); } //modify soobin Jeon 02/15/2012
    public double[] getVolume() { return getData(null, TrafficType.VOLUME); }
    public double[] getScan() { return getData(null, TrafficType.SCAN); }    
    public double[] getOccupancy() { return getData(null, TrafficType.OCCUPANCY); }        
    
    public double[] getSpeed(IDetectorChecker checker) { return getData(checker, TrafficType.SPEEDFORSTATION); }
    public double[] getDensity(IDetectorChecker checker) { return getData(checker, TrafficType.DENSITY); }
    public double[] getFlow(IDetectorChecker checker) { return getData(checker, TrafficType.FLOW); }
    public double[] getFlowForAverageLaneFlow(IDetectorChecker checker) { return getData(checker, TrafficType.FLOWFORAVERAGE); }//modify soobin Jeon 02/15/2012
    public double[] getAverageLaneFlow(IDetectorChecker checker) { return getData(checker, TrafficType.AVERAGEFLOW); }//modify soobin Jeon 02/15/2012
    public double[] getVolume(IDetectorChecker checker) { return getData(checker, TrafficType.VOLUME); }
    public double[] getScan(IDetectorChecker checker) { return getData(checker, TrafficType.SCAN); }    
    public double[] getOccupancy(IDetectorChecker checker) { return getData(checker, TrafficType.OCCUPANCY); }    
    
    /**
     * Return total flow
     * Equation : average flow * lanes
     * CAUTION!! : lanes = number of detectors that is not missing-detector
     * @param detectorChecker
     * @return 
     */    
    public double[] getTotalFlow() { return getTotalFlow(null); }    
    
    /**
     * Return total flow
     * Equation : average flow * lanes
     * CAUTION!! : lanes = number of detectors that is not missing-detector
     * @param detectorChecker
     * @return 
     */
    public double[] getTotalFlow(IDetectorChecker detectorChecker) {
        double[] data = this.getFlow(detectorChecker);        
        int lanes = 0;
        /*
         * modify soobin Jeon 02/14/2012
         */
        /*for(Detector d : this.getDetectors(detectorChecker)) {
            if(!d.isMissing()) lanes++;
        }
        for(int i =0; i<data.length; i++) {
            data[i] *= lanes;
        }*/
        return data;
    }    
    
    /*
     * modify soobin Jeon 02/13/2012
     */
//    private double[] getData(IDetectorChecker checker, TrafficType type) {
//        double[][] data = new double[this.detectors.size()][];
//        int idx = 0;
//        for(Detector d : this.detectors.values())
//        {
//                data[idx++] = d.getData(type);            
//        }
//        
//        /*
//         * modify soobin Jeon 02/14/2012 //problem
//         */
//        return makeAdjustData(checker, data,type);
//    }
    
    /*
     * modify soobin Jeon 02/15/2012
     */
    private double[] getData(IDetectorChecker checker, TrafficType type) {
            return makeAdjustData(checker, MakeData(checker,type),type);
    }
    
    /*
     * modify soobin Jeon 02/15/2012
     */
    private double[][] MakeData(IDetectorChecker checker, TrafficType type){
        double[][] ddata = new double[this.detectors.size()][];

        if(type.isSppedForStation()){
            Vector<Double> data[] = new Vector[this.detectors.size()];
            int idx = 0;
            for(Detector d : this.detectors.values())
            {
                data[idx++] = d.getDataVector(type);
            }
            if(idx > 0){
                for(int i=0;i<data[0].size();i++){
                    double NotZero = 0;
                    double NotZeroCount = 0;
                    for(int DIdx=0;DIdx<data.length;DIdx++){
                        if(data[DIdx].get(i) > 0){
                            NotZero += data[DIdx].get(i);
                            NotZeroCount ++;
                        }
                    }
                    if(NotZeroCount != data.length){
                        NotZero = NotZero/NotZeroCount; //Average
                        for(int DIdx=0;DIdx<data.length;DIdx++){
                            if(data[DIdx].get(i) <= 0){
                                data[DIdx].set(i, NotZero);
                            }
                        }
                    }
                }
                
                idx = 0;
                for(Detector d : this.detectors.values()){
                        ddata[idx] = toDoubleArray(d.adjustInterval(data[idx], AdjustType.Average));
                        idx++;
                }
            }

            return ddata;

        }else{
            int idx = 0;
            for(Detector d : this.detectors.values())
            {
                    ddata[idx++] = d.getData(type);            
            }
            
            return ddata;
        }
    }
    
    /*
     * modify soobin Jeon 02/13/2012 //problem
     */
    private double[] makeAdjustData(IDetectorChecker checker, double[][] data, TrafficType atype)
    {
        if(data == null || data.length == 0) return null;
        double[] avg = new double[data[0].length];
        Detector[] ds = this.detectors.values().toArray(new Detector[this.detectors.size()]);
        
        double totalValidCount = 0;
        double totalDataCount = 0;
        
        for(int i=0; i<data[0].length; i++) {
            double sum = 0;
            double validCount = 0;            
            for(int detIdx=0;detIdx<data.length; detIdx++)
            {
                if(checker != null && !checker.check(ds[detIdx])) {
                    continue;
                }
                if(!atype.isFlow() && ds[detIdx].isAuxiliary())
                    continue;
                /*
                 * Modify to check missing detectors
                 * Check Missing detectors in Station
                 * modify soobin Jeon 02/13/2012
                 */
                if(ds[detIdx].isMissing()){
//                    System.out.println("missing : " + ds[detIdx].getLabel() + "("+ds[detIdx].getConfidence());
                    continue;
                }
                
                totalDataCount++;
                
                double v = data[detIdx][i];                        
                
                /*
                 * modify soobin Jeon 02/14/2012
                 */
                if(v > 0)
                {                    
                    sum += v;
                    validCount++;
                }else if(atype.isDensity())
                    validCount++;
            }    
            totalValidCount += validCount;
            
            /*
             * modify soobin Jeon 02/14/2012
             */
            if(validCount > 0) {
                    if(atype.isFlow() || atype.isVolume() || atype.isFlowForAverage()){
                        avg[i] = sum;
                    }
                    else{
                        avg[i] = sum/validCount;
//                        if(avg[i] < 1)
//                            System.out.println("id :(" + i + ")" + avg[i] + "sum :" + sum + " vlidCount : " + validCount); 
                    }
            }///validCount;//this.roundUp(sum / validCount, 2);
            else {avg[i] = InfraConstants.MISSING_DATA;}            
        }
//        System.out.println("========================================\n");
        
        this.confidence = totalValidCount / totalDataCount * 100;
        if(confidence < 50) isMissing = true;
        return avg;
    }
   
    public int[] getDetectorIds(IDetectorChecker checker) {
        if(detectors.isEmpty()) return null;
        int[] det_ids = new int[this.detectors.size()];
        Detector[] ds = this.getDetectors();
        for(int i=0; i<det_ids.length; i++)
        {
            if(checker != null && !checker.check(ds[i])) continue;
            det_ids[i] = ds[i].getDetectorId();
        }
        return det_ids;
    }

    public Detector getDetector(String detector_id)
    {
        return this.detectors.get(detector_id);
    }

    public Detector getDetector(Integer detector_id)
    {
        return this.getDetector(detector_id.toString());
    }

    public Detector[] getDetectors()
    {
        return getDetectors(null);
    }
    
    public Detector[] getDetectors(IDetectorChecker checker)
    {
        Vector<Detector> dlist = new Vector<Detector>();
        for(Detector d : this.detectors.values()) {
            if(checker != null && !checker.check(d)) continue;
            dlist.add(d);
        }
        return dlist.toArray(new Detector[dlist.size()]);
    }    

    public boolean hasDetector(String detector_id) {
        for(Detector d : this.detectors.values()) {
            if(d.getId().equals(detector_id)) return true;
        }        
        return false;
    }
    
    public boolean isAvailableStation()
    {
        return ( this.infraType == InfraType.STATION ) && this.isAvailable && !isAbandoned() && this.getStationId() != null;
    }
    
    public boolean hasHov(){
        for(Detector d : this.getDetectors()){
            if(d.isHov()) return true;
        }
        return false;
    }
    
    public boolean isAbandoned() {
        for(Detector d : this.getDetectors() ) {
            if(!d.isAbandoned()) return false;
        }
        return true;
    }

    public boolean isStation()
    {
        return ( this.infraType == InfraType.STATION );
    }

    public boolean isEntrance()
    {
        return ( this.infraType == InfraType.ENTRANCE );
    }

    public boolean isExit()
    {
        return ( this.infraType == InfraType.EXIT );
    }
    
    public double getConfidence() {
        return this.roundUp(this.confidence, 1);
    }

    public int getSpeedLimit() {
        int sLimit = getPropertyInt(InfraProperty.s_limit);
        return ( sLimit > 0 ? sLimit : 55);
    }
    


    public String getLabel() {
        return getProperty(InfraProperty.label);
    }


    public Corridor getCorridor() {
        TMO tmo = TMO.getInstance();
        if(corridor == null) corridor = tmo.getInfra().getCorridor(getProperty(InfraProperty.CorridorId));
        return corridor;
    }
    
    private void setLocation() {
        CoordinateConversion converter = new CoordinateConversion();
        String en = converter.latLon2UTM(getLat(), getLon());
        String[] EN = en.split(" ");
        if(EN.length > 3){
            easting = Integer.parseInt(EN[2]);
            northing = Integer.parseInt(EN[3]);
        }
    }

    /**
     * @return 
     */
    public int getEasting() {
        return easting;
//        return getPropertyInt(InfraProperty.easting);
    }

    /**
     * @return 
     */
    public int getNorthing() {
        return northing;
//        return getPropertyInt(InfraProperty.northing);
    }
    
    public double getLon(){
        return this.getPropertyDouble(InfraProperty.lon);
    }
    
    public double getLat(){
        return this.getPropertyDouble(InfraProperty.lat);
    }
    
    public String getStationId() {
        return getProperty(InfraProperty.station_id);
    }

    public String[] getDetectorNames() {
        return getPropertyArray(InfraProperty.dets);
    }

    public String getAttachSide() {
        return getProperty(InfraProperty.attach_side);
    }

    public int getDistanceToDownstreamNode() {
        return distanceToDownstreamNode;
    }

    public int getDistanceToUpstreamNode() {
        return distanceToUpstreamNode;
    }

    public boolean isWavetronicsStation() {
        return isWavetronicsStation;
    }

    public int getLanes() {
        return getPropertyInt(InfraProperty.lanes);
    }
    
    public int getLanes(IDetectorChecker checker)
    {
        int l = 0;
        for(Detector d : this.detectors.values())
            if(checker.check(d)) l++;
        return l;
    }

    public RNode getDownstreamNodeToSameCorridor() {
        if(nextRNodeInSameCorridor == null) {
            TMO tmo = TMO.getInstance();
            nextRNodeInSameCorridor = tmo.getInfra().getRNode(getProperty(InfraProperty.NextRNodeIdInSameCorridor));
        }
        return nextRNodeInSameCorridor;
    }


    public int getShift() {
        return Math.max(getPropertyInt(InfraProperty.shift), 0);
    }


    public String getTransition() {
        return getProperty(InfraProperty.transition);
    }

    public TransitionType getTransitionType() {
        return transitionType;
    }

    public boolean isMissing() {
        Iterator<Detector> itr = this.detectors.values().iterator();
        while(itr.hasNext()) {
            Detector d = itr.next();
            if(!d.isMissing()) {
                return false;
            }
        }
        return true;
    }
    
    public void setStationId(String text) {
        setProperty(InfraProperty.station_id,text);
        System.out.println("ok");
    }

    public void setLabel(String text) {
        setProperty(InfraProperty.label, text);
    }

    public void setLanes(int integer) {
        setProperty(InfraProperty.lanes, integer);
    }

    public void setEasting(int _easting) {
        this.easting = _easting;
    }

    public void setNorthing(int _northing) {
        this.northing = _northing;
    }

    public void addDetectors(List<Detector> adets) {
        if(detectors == null) detectors = new HashMap<String, Detector>();        
        
        TMO tmo = TMO.getInstance();
        for(Detector dd : adets){
            System.out.println("Searching : "+dd.getId());
            String d = dd.getId();
            Detector det = tmo.getInfra().getDetector(d);
            if(det != null){
                detectors.put(det.getId(),det);
                det.setRNode(this);
                System.out.println("updated detector : "+det.getId());
            }
        }
    }

    public void setInfraType(InfraType itype) {
        infraType = itype;
    }

    public void removeDetector(Detector d) {
        detectors.remove(d.getId());
    }
}
