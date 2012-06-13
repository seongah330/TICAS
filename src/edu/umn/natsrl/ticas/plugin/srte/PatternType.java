package edu.umn.natsrl.ticas.plugin.srte;

/**
 *
 * @author soobin Jeon
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
    
    //temporary
    /** Delta Q range */
    double dQ = 0;
    /** Delta K range */
    double dK = 0;
    /** Delta U range */
    double dU = 0;
    double TPRu = 0;
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
        
    }
    
    public void Process() {
        
        findType(q,k,u,sPoint);
//        System.out.println("After FinType : RCR-" + RCRPoint + " TPR-"+TPRPoint);
        RCRPoint = findRCRusingType(q,k,u,sPoint);
        
        if(RCRPoint < 0)
            TPRPoint = findTPRusingType(q,k,u,sPoint);
        else
            TPRPoint = findTPRusingType(q,k,u,RCRPoint);
        
//        System.out.println("After end : RCR-" + RCRPoint + " TPR-"+TPRPoint);
        
    }

    public static double[] CalculateSmoothedSpeed(double[] q, double[] k) {
        double[] u = new double[q.length];
        
        for(int i = 0; i < q.length; i++)
            u[i] = q[i] == 0 ? 0 : q[i] / k[i];
        
        return u;
    }

    private int findUpperPattern(double[] q, double[] k, double[] u, int sPoint) {
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
        for(int i=sPoint;i<q.length;i++){
            if(i == sPoint)
                continue;
            
            double deltaQ = q[i] - q[i-1];
            double deltaK = k[i] - k[i-1];
            double deltaU = Math.abs(u[i] - u[i-1]);
            
            if(deltaU < TPRu
                    && (deltaQ*deltaK) > 0){
                if(kfMax < k[i]){
                    kfMax = k[i];
                    kfp = i;
                }
            }
        }
        
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
            
            double ustate = (u[i] - u[i-1]) * (u[i-1] - u[i-2]);
            if(ustate > 0
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
            TPRPoint = findFreeflow(q,k,u,sPoint);
            return;
        }
        
        TPRPoint = findFreeflow(q,k,u,RCRPoint);
        KfPoint = RCRPoint;
        
        //caculate Kr, Kf for determining Type
        double kr = getRecoveryDensity(k,RCRPoint);
        double kf = getFreeflowDensity(k,TPRPoint);
        
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
}
