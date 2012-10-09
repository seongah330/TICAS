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
package edu.umn.natsrl.ticas.plugin.simulation.basicmetering;

import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicMeterConfig {
    public double Dcap = 0;;
    public double Kcr = 0;
    public double Alpha = 0;
    public double Beta = 0;
    public double Gamma = 0;
    public double Kqd = 0;
    public int Count = 6;
    public BasicMeterConfig(){;}
    public BasicMeterConfig(double dcap, double kcr, double alpha, double beta, double gamma, double kqd){
        Dcap = dcap;
        Kcr = kcr;
        Alpha = alpha;
        Beta = beta;
        Gamma = gamma;
        Kqd = kqd;
    }
    
    /**
     * 0 : Dcap
     * 1 : Kcr
     * 2 : Alpha
     * 3 : Beta
     * 4 : Gamma
     * 5 : Kqd
     * @return 
     */
    public double[] getDatas(){
        double[] data = new double[Count];
        data[0] = Dcap;
        data[1] = Kcr;
        data[2] = Alpha;
        data[3] = Beta;
        data[4] = Gamma;
        data[5] = Kqd;
        return data;
    }
    /**
     * 0 : Dcap
     * 1 : Kcr
     * 2 : Alpha
     * 3 : Beta
     * 4 : Gamma
     * 5 : Kqd
     * @param datas 
     */
    public void setDatas(double[] datas){
        Dcap = datas[0];
        Kcr = datas[1];
        Alpha = datas[2];
        Beta = datas[3];
        Gamma = datas[4];
        Kqd = datas[5];
    }
    public void setDatas(ArrayList<Double> datas){
        double[] data = new double[Count];
        if(data.length != datas.size())
            return;
        int idx = 0;
        for(double d : datas){
            data[idx] = d;
            idx ++;
        }
        setDatas(data);
    }
}
