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

import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.ticas.Simulation.EntranceState;
import edu.umn.natsrl.ticas.Simulation.SectionHelper;
import edu.umn.natsrl.ticas.Simulation.SimRampMeter;
import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import edu.umn.natsrl.ticas.Simulation.StationState;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicMeter extends SimRampMeter{
    private BasicMeterConfig mconfig;
    private SectionHelper sectionHelper;
    private EntranceState es;
    private StationState upstream;
    private StationState downstream;
    private BasicMeterGroupErrorType error;
    private BasicMeterMethod metermethod;
    
    private final int AverageStep = 1;
    
    /**
     * new BasicMeter
     * @param _es
     * @param mcfg 
     */
    public BasicMeter(EntranceState _es, BasicMeterConfig mcfg){
        super(_es.getSimMeter());
        es = _es;
        mconfig = mcfg;
        
        //if Station was Associated, then SUCCESS
        error = BasicMeterGroupErrorType.NOT_LOADED;
    }
    
    public BasicMeter(EntranceState _es, BasicMeterConfig mcfg, BasicMeterMethod method){
        this(_es,mcfg);
        metermethod = method;
    }
    
    public BasicMeterConfig getConfig(){
        return mconfig;
    }
    
    public double[] getConfigDatas(){
        return mconfig.getDatas();
    }
    
    public void setConfigDatas(double[] datas){
        mconfig.setDatas(datas);
    }
    
    public String getID(){
        return es.getID();
    }
    
    public String getUpStreamStationID(){
        if(error.isSUCCESS())
            return upstream.getID();
        else
            return null;
    }
    
    public String getDownStreamStationID(){
        if(error.isSUCCESS())
            return downstream.getID();
        else
            return null;
    }
    
    public BasicMeterGroupErrorType getError(){
        return error;
    }
    
    public BasicMeterMethod getMethod(){
        return metermethod;
    }
    
    public EntranceState getEntranceState(){
        return this.es;
    }

    public BasicMeterGroupErrorType associateStationStream(String upSID, String downSID, SectionHelper sectionHelper) {
        upstream = sectionHelper.getStationState(upSID);
        downstream = sectionHelper.getStationState(downSID);
        if(upstream == null){
            String msg = "There is no Upstream\n"+getID()+" : sID"+upSID;
            error = BasicMeterGroupErrorType.NOSTATION;
            error.setErrorMsg(msg);
            return error;
        }
        
        if(downstream == null){
            String msg = "There is no Downstream\n"+getID()+" : sID"+downSID;
            error = BasicMeterGroupErrorType.NOSTATION;
            error.setErrorMsg(msg);
            return error;
        }
        error = BasicMeterGroupErrorType.SUCCESS;
        return error;
    }

    void updateState() {
        if(!error.isSUCCESS())
            return;
        
        double rnext = SimulationConfig.MAX_RATE;
        if(metermethod.isFLOWRATEBASED()){
            rnext = CalculateFlowRateBased();
        }else if(metermethod.isDENSITYBASED()){
            rnext = CalculateDensityBased();
        }else if(metermethod.isQUEUEDENSITYBASED1()){
            rnext = CalculateRateByQDB(this.CalculateFlowRateBased());
        }else if(metermethod.isQUEUEDENSITYBASED2()){
            rnext = CalculateRateByQDB(this.CalculateDensityBased());
        }
        
        super.setRate(rnext);
    }

    private double CalculateFlowRateBased() {
        double Kd = downstream.getAverageDensity(0, AverageStep);
        double Kcr = mconfig.Kcr;
        double Cd = mconfig.Dcap;
        double Qu = upstream.getAverageLaneFlow();
        double Alpha = mconfig.Alpha;
        
        double Ri = 0;
        if(Kd < Kcr){
            Ri = Cd - Qu;
        }else{
            Ri = Alpha * Qu;
        }
        
        return SimulationConfig.getCurrentRate(Ri);
    }

    private double CalculateDensityBased() {
        double Ri = super.getLastRate();
        double beta = mconfig.Beta;
        double Kcr = mconfig.Kcr;
        double Kd = downstream.getAverageDensity(0, AverageStep);
        
        Ri = Ri + beta * ( Kcr - Kd );
        
        return SimulationConfig.getCurrentRate(Ri);
    }

    private double CalculateQueueDensityBased() {
        double Ri = super.getLastRate();
        double gamma = mconfig.Gamma;
        double Kq = es.getRampDensity();
        double Kqd = mconfig.Kqd;
        
        Ri = Ri + gamma * (Kq-Kqd);
        
        return SimulationConfig.getCurrentRate(Ri);
    }

    private double CalculateRateByQDB(double rrate) {
        double Kq = es.getRampDensity();
        double Kqd = mconfig.Kqd;
        double Ri = 0;
        if(Kq <= Kqd){
            Ri = rrate;
        }else{
            Ri = this.CalculateQueueDensityBased();
        }
        
        return SimulationConfig.getCurrentRate(Ri);
    }
    
    public String getMethodState(){
        if(metermethod.isFLOWRATEBASED()){
            return this.printFlowRateBased();
        }else if(metermethod.isDENSITYBASED()){
            return this.printDensityBased();
        }else if(metermethod.isQUEUEDENSITYBASED1()){
            return this.printRatebyDB(this.printFlowRateBased());
        }else if(metermethod.isQUEUEDENSITYBASED2()){
            return this.printRatebyDB(this.printDensityBased());
        }else{
            return "";
        }
    }
    
    private String printFlowRateBased(){
        double Kd = downstream.getAverageDensity(0, AverageStep);
        double Kcr = mconfig.Kcr;
        double Cd = mconfig.Dcap;
        double Qu = upstream.getAverageLaneFlow();
        double Alpha = mconfig.Alpha;
        
        String data = "";
        
        data += "Kd("+String.format("%.1f", Kd)+") ";
        if(Kd < Kcr){
            data += "< Kcr("+Kcr+"), Ri = Cd("+Cd+") - Qu,t("+Qu+"),  △Ri = "+String.format("%.1f", super.getLastRate());
        }else{
            data += "> Kcr("+Kcr+"), Ri = alpha("+Alpha+") - Qu,t("+Qu+"),  △Ri = "+String.format("%.1f", super.getLastRate());
        }
        
        return data;
    }
    
    private String printDensityBased(){
        double Ri = super.lastbeforeRate;
        double beta = mconfig.Beta;
        double Kcr = mconfig.Kcr;
        double Kd = downstream.getAverageDensity(0, AverageStep);
        
        String data = "";
//        String.format("%02d", m)
        data += "Ri,t+1("+String.format("%.1f", super.lastRate)+") = Ri,t("+String.format("%.1f", Ri)+") + beta("+beta+") * ( Kcr("+Kcr+") - Kd,t("+String.format("%.1f", Kd)+") )";
        
        return data;
    }
    
    private String printQueueDensityBased(){
        double Ri = super.lastbeforeRate;
        double gamma = mconfig.Gamma;
        double Kq = es.getRampDensity();
        double Kqd = mconfig.Kqd;
        
        String data = "";
        data += "Ri,t+1 = Ri,t("+Ri+") + gamma("+gamma+") * ( Kq,t("+String.format("%.1f", Kq)+") - Kq,d("+Kqd+") )";
        
        return data;
    }
    
    private String printRatebyDB(String value){
        double Kq = es.getRampDensity();
        double Kqd = mconfig.Kqd;
        double Ri = 0;
        String data = "";
        if(Kq <= Kqd){
            data += "Kq,t("+String.format("%.1f", Kq)+") <= Kqd("+Kqd+"), "+value;
        }else{
            data += "Kq,t("+String.format("%.1f", Kq)+") > Kqd("+Kqd+"), "+this.printQueueDensityBased();
        }
        return data;
    }
}
    
