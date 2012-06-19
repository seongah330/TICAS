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
package edu.umn.natsrl.ticas.plugin.srte;

import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class PatternType {
    private double[] q;
    private double[] k;
    private double[] u;
    private int sPoint = -1;
    private int RCRPoint = -1;
    private int TPRPoint = -1;
    private int KfPoint = -1;
    public pType type;
    
    //Additional ResultData
    public TrafficResult qTrafficData = new TrafficResult();
    public TrafficResult kTrafficData = new TrafficResult();
    public TrafficResult uTrafficData = new TrafficResult();
    
    private int T40mPoint = -1;
    private int T50mPoint = -1;
    private int T60mPoint = -1;
    
    //temporary
    /** Delta Q range */
    double dQ = 0;
    /** Delta K range */
    double dK = 0;
    /** Delta U range */
    double dU = 0;
    double TPRu = 0;
    int TPRhour = 0;
    double SDCk = 0;
    
    PatternType(){
        Initialize();
    }
    
    PatternType(pType type){
        Initialize();
        this.type = type;
    }

    PatternType(double[] filteredData, double[] k_smoothed, int StartPoint) {
        Initialize();
        q = filteredData;
        k = k_smoothed;
        u = CalculateSmoothedSpeed(q,k);
        this.sPoint = StartPoint;
    }
    
    private void Initialize(){
        dQ = SRTEConfig.RCR_Q;
        dK = SRTEConfig.RCR_K;
        dU = SRTEConfig.RCR_U;
        TPRu = SRTEConfig.TPR_U;
        SDCk = SRTEConfig.SDC_K;
        double thour = SRTEConfig.TPR_hour;
        TPRhour = thour <= 0 ? 0 : (int)(thour*4);
        
    }
    
    public void Process() {
        
        findType(q,k,u,sPoint);
//        System.out.println("After FinType : RCR-" + RCRPoint + " TPR-"+TPRPoint);
        RCRPoint = findRCRusingType(q,k,u,sPoint);
        
        if(RCRPoint >= 0)
            findEachPointAfterRCR(RCRPoint);
            
        
//        if(RCRPoint < 0)
//            TPRPoint = findTPRusingType(q,k,u,sPoint);
//        else
//            TPRPoint = findTPRusingType(q,k,u,RCRPoint);
        
//        System.out.println("After end : RCR-" + RCRPoint + " TPR-"+TPRPoint);
        
    }

    public static double[] CalculateSmoothedSpeed(double[] q, double[] k) {
        double[] u = new double[q.length];
        
        for(int i = 0; i < q.length; i++)
            u[i] = (k[i] == 0) ? 0 : q[i] / k[i];
        
        return u;
    }

    /**
     * 
     * @param q
     * @param k
     * @param u
     * @param sPoint
     * @return 
     */
    private int findUpperPattern(double[] q, double[] k, double[] u, int sPoint) {
        for(int i=sPoint;i<q.length;i++){
            if(i == sPoint)
                continue;
            
            double deltaQ = (q[i] - q[i-1]);
            double deltaK = Math.abs(k[i] - k[i-1]);
            double slope = deltaK == 0 ? 0 : deltaQ / deltaK;
            double deltaU = u[i] - u[i-1];
//            double deltaQ = q[i] - q[i-1];
//            double deltaK = k[i] - k[i-1];
//            double deltaU = u[i] - u[i-1];
            
            if(slope > dQ
                    && deltaU > dU){
//                RCRPoint = i;
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * @deprecated
     * @param q
     * @param k
     * @param u
     * @param sPoint
     * @return 
     */
    private int findUpperPatternOld(double[] q, double[] k, double[] u, int sPoint) {
        for(int i=sPoint;i<q.length;i++){
            if(i == sPoint)
                continue;
            
            double deltaQ = q[i] - q[i-1];
            double deltaK = Math.abs(k[i] - k[i-1]);
            double deltaU = u[i] - u[i-1];
//            double deltaQ = q[i] - q[i-1];
//            double deltaK = k[i] - k[i-1];
//            double deltaU = u[i] - u[i-1];
            
            if(deltaQ > dQ
                    && deltaK < dK
                    && deltaU > dU){
//                RCRPoint = i;
                return i;
            }
        }
        
        return -1;
    }

    private int findFreeflow(double[] q, double[] k, double[] u, int sPoint) {
        double kfMax = 0;
        int kfp = -1;
        int tcount = 0;
        ArrayList<double[]> tlist = new ArrayList<double[]>();
        ArrayList<ArrayList> freeflowList = new ArrayList<ArrayList>();
        for(int i=sPoint;i<q.length;i++){
            if(i == sPoint)
                continue;
            
            double deltaQ = q[i] - q[i-1];
            double deltaK = k[i] - k[i-1];
            double deltaU = Math.abs(u[i] - u[i-1]);
//            System.out.println("u :" +TPRu);
            if(deltaU < TPRu
                    && (deltaQ*deltaK) > 0){
                double[] d = {i,k[i]};
                
//                System.out.println("pt : " + i + "u :" +deltaU + " k : "+k[i]+" - pU :" + TPRu);
                
                if(tcount == 0 && tlist.size() == 0){
                    tlist.add(d);
                    tcount ++;
                    continue;
                }
                
                if(i - (int)tlist.get(tlist.size()-1)[0] == 1){
                    tlist.add(d);
                    tcount ++;
                }else{
                    freeflowList.add((ArrayList)tlist.clone());
                    tlist.clear();
                    tlist.add(d);
                    tcount = 0;
                }
               
//                
            }
        }
//        System.out.println("fflist : "+freeflowList.size());
        
        int tmpcount = 0;
        for(int i=0; i<freeflowList.size();i++){
            if(freeflowList.get(i).size() >= TPRhour){
//                System.out.println("Expected Freeflow = "+(tmpcount+1));
                ArrayList<double[]> ta = freeflowList.get(i);
                
                double[] freeflowSpoint = ta.get(0);
                if(kfMax < freeflowSpoint[1]){
                    kfMax = freeflowSpoint[1];
                    kfp = (int)freeflowSpoint[0];
                }
                
//                for(int z=0;z<ta.size();z++){
//                    System.out.println("t"+ta.get(z)[0]+" k"+ta.get(z)[1]);
//                }
                
                tmpcount ++;
            }
        }
        System.out.println("kfp :" + kfp);
        return kfp;
    }

    private int findRCRusingType(double[] q, double[] k, double[] u, int sPoint) {
        if(type == pType.ATYPE || type == pType.BTYPE)
            return RCRPoint;
        
        int Range = 2;
        for(int i=sPoint; i<q.length;i++){
            if(i<sPoint+Range)
                continue;
            
//            double ustate = 1;
//            for(int z=0;z<Range;z++)
//                ustate = (u[i-z] - u[i-(z+1)]) * ustate;
            
            double ustate1 = (u[i] - u[i-1]);
            double ustate2 = (u[i-1] - u[i-2]);
            
            if(ustate1 > 0 && ustate2 > 0
                    && k[i-Range] > k[i]
                    && (q[i-1] > q[i] && q[i-1] > q[i-2])){
                return RCRPoint = i-1;
            }
        }
        
        return -1;
    }
    
    private int findTPRusingType(double[] q, double[] k, double[] u, int sPoint) {
        if(type == pType.ATYPE || type == pType.CTYPE)
            return findFreeflow(q,k,u,sPoint);
        
        double kstateMax = 0;
        int kstatePoint = -1;
        int Range = 3;
//        System.out.println("findTPR : sPoint-" + sPoint);
        for(int i=sPoint; i<q.length;i++){
            if(i < sPoint + Range)
                continue;
            
            double kstate = Math.abs(k[i-3] - k[i]);
            
            if(kstateMax < kstate){
                kstateMax = kstate;
                kstatePoint = i;
            }
//            if(kstate > SDCk)
//                return i-1;
        }
        
        return kstatePoint;
    }
    
    private void findEachPointAfterRCR(int spoint) {
        for(int i=spoint; i<q.length;i++){
            qTrafficData.Compare(q[i], i);
            kTrafficData.Compare(k[i], i);
            uTrafficData.Compare(u[i], i);
        }
    }

    private void findType(double[] q, double[] k, double[] u, int sPoint) {
        //Find first RCR, TPR Point
        /**Find Upper Pattern and Freeflow Pattern
         * Afer LST ~ 7 hour after event end
         */
        RCRPoint = findUpperPattern(q,k,u,sPoint);
        /**
         * if there is upper pattern.. CTYPE
         */
        if(RCRPoint == -1){
            type = pType.CTYPE;
            KfPoint = findFreeflow(q,k,u,sPoint);
            return;
        }
        
        KfPoint = findFreeflow(q,k,u,RCRPoint);
//        KfPoint = TPRPoint;
        
        //caculate Kr, Kf for determining Type
        double kr = getRecoveryDensity(k,RCRPoint);
        double kf = KfPoint == -1 ? 0 : getFreeflowDensity(k,KfPoint);
        
        if(kr > kf)
            type = pType.ATYPE;
        else
            type = pType.BTYPE;
    }
    
    private double getRecoveryDensity(double[] k, int RCRPoint) {
        return k[RCRPoint];
    }
    public double getRecoveryDensity(){
        return getRecoveryDensity(k,RCRPoint);
    }
    private double getFreeflowDensity(double[] k, int TPRPoint) {
        return k[TPRPoint];
    }
    public double getFreeflowDensity(){
        return getFreeflowDensity(k,TPRPoint);
    }
    public double getKfDensity(){
        return k[KfPoint];
    }
    
    public int getRecoveryPoint(){
        return RCRPoint;
    } 
    public int getFreeflowPoint(){
        return TPRPoint;
    }
    
    public int getKfPoint(){
        return KfPoint;
    }
    public int getTypeNumber(){
        if(this.type == pType.ATYPE)
            return 1;
        else if(this.type == pType.BTYPE)
            return 2;
        else if(this.type == pType.CTYPE)
            return 3;
        else
            return -1;
    }
    
    enum pType{
        ATYPE,
        BTYPE,
        CTYPE;
        
        pType(){}
        
        public boolean isAType(){return this == ATYPE;}
        public boolean isBType(){return this == BTYPE;}
        public boolean isCType(){return this == CTYPE;}
    }
    public class TrafficResult{
        private int maxPoint = -1;
        private int minPoint = -1;
        private double maxData = -1;
        private double minData = Integer.MAX_VALUE;
        
        TrafficResult(){;}
        
        /**
         * 
         * @param mdata set Compare Data
         * @param i set Point
         */
        public void Compare(double mdata,int i){
            if(maxData < mdata){
                maxData = mdata;
                maxPoint = i;
            }
            
            if(minData > mdata){
                minData = mdata;
                minPoint = i;
            }
        }
        public int getMaxPoint(){
            return maxPoint;
        }
        public double getMaxData(){
            return maxData;
        }
        public int getMinPoint(){
            return minPoint;
        }
        public double getMinData(){
            return minData;
        }
        
    }
}
