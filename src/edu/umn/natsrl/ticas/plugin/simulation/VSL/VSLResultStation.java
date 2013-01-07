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
package edu.umn.natsrl.ticas.plugin.simulation.VSL;

import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLStationState;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
class VSLResultStation extends VSLResultInfra{    
    private ArrayList<Double> q = new ArrayList<Double>();
    private ArrayList<Double> aq = new ArrayList<Double>();
    private ArrayList<Double> k = new ArrayList<Double>();
    private ArrayList<Double> u = new ArrayList<Double>();
    private ArrayList<Double> ru = new ArrayList<Double>();
    private ArrayList<Double> v = new ArrayList<Double>();
    private ArrayList<Double> acc = new ArrayList<Double>();
    private ArrayList<Double> bottleneckCount = new ArrayList<Double>();
    private ArrayList<Boolean> cVSS = new ArrayList<Boolean>();
    private ArrayList<Boolean> pVSS = new ArrayList<Boolean>();
    
    
    VSLResultStation(VSLStationState s) {
        super(s.getID(),s.getMilePoint());
        initData();
    }

    VSLResultStation(Double dkey, String did) {
        super(did,dkey);
        initData();
    }

    public void addData(VSLStationState s) {
        double acc = -1;
        if(s.getAcceleration() != null){
            acc = s.getAcceleration();
        }
        addData(s.getFlow(),s.getAverageFlow(0, 1),s.getAverageDensity(0, 1),s.getAverageSpeed(0, 1),s.getAggregateRollingSpeed(),s.getTotalVolume(0, 1),acc
                ,s.n_bottleneck,s.bottleneck,s.p_bottleneck);
    }
    
    public void addData(double _q, double _aq, double _k, double _u, double _ru, double _v, double _acc, double bcnt, int cvss, int pvss){
        boolean _cvss = convertDoubletoBoolean(cvss);
        boolean _pvss = convertDoubletoBoolean(pvss);
        addData(_q,_aq,_k,_u,_ru,_v,_acc,bcnt,_cvss,_pvss);
    }
    
    public void addData(double _q, double _aq, double _k, double _u, double _ru, double _v, double _acc, double bcnt, boolean cvss, boolean pvss){
        q.add(_q);
        aq.add(_aq);
        k.add(_k);
        u.add(_u);
        ru.add(_ru);
        v.add(_v);
        acc.add(_acc);
        bottleneckCount.add(bcnt);
        cVSS.add(cvss);
        pVSS.add(pvss);
    }
    
    public void addAllDatas(double[] _q,double[] _qa,double[] _k,double[] _u,double[] _ru,double[] _acc,double[] _v,double[] _bcnt,double[] _cvss,double[] _pvss){
        addAllDatas(_q, q);
        addAllDatas(_qa, aq);
        addAllDatas(_k, k);
        addAllDatas(_u, u);
        addAllDatas(_ru, ru);
        addAllDatas(_acc, acc);
        addAllDatas(_v, v);
        addAllDatas(_bcnt, bottleneckCount);
        addAllDatas(convertDoubletoBooleanArray(_cvss), cVSS);
        addAllDatas(convertDoubletoBooleanArray(_pvss), pVSS);
    }
    
    

    private void initData() {
        q.clear();
        aq.clear();
        k.clear();
        u.clear();
        ru.clear();
        v.clear();
        acc.clear();
        bottleneckCount.clear();
        cVSS.clear();
        pVSS.clear();
    }

    public double[] getFlows() {
        return getArraytoDouble(q);
    }
    public double[] getAverageFlows(){
        return getArraytoDouble(aq);
    }
    public double[] getDensitys(){
        return getArraytoDouble(k);
    }
    public double[] getSpeeds(){
        return getArraytoDouble(u);
    }
    public double[] getRollingSpeeds(){
        return getArraytoDouble(ru);
    }
    public double[] getTotalVolumes(){
        return getArraytoDouble(v);
    }
    public double[] getAcceleration(){
        return getArraytoDouble(acc);
    }
    public double[] getBottleneckCounts(){
        return getArraytoDouble(bottleneckCount);
    }
    public boolean[] getCurrentVSS(){
        return getArraytoBoolean(cVSS);
    }
    public boolean[] getPreviousVSS(){
        return getArraytoBoolean(pVSS);
    }
    public double[] getCurrentVSStoDouble(){
        return convertBooleantoDoubleArray(getCurrentVSS());
    }
    public double[] getPreviousVSStoDouble(){
        return convertBooleantoDoubleArray(getPreviousVSS());
    }
    
    @Override
    public String toString(){
        return getID();
    }
}
