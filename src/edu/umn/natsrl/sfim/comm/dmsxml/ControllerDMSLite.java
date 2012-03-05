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

package edu.umn.natsrl.sfim.comm.dmsxml;

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.sfim.SFIMExceptionHandler;
import edu.umn.natsrl.sfim.comm.Controller;
import edu.umn.natsrl.sfim.comm.InfoController;
import edu.umn.natsrl.sfim.comm.Responser;

/**
 *
 * @author Chongmyung Park
 */
public class ControllerDMSLite extends Controller  {

    SimDMS dms;
    
    public ControllerDMSLite(int drop) {
        super(drop);
    }

    /**
     * CAUTION!!
     * actually this method make just one DMS for controller,
     * because comm_link for DMS in IRIS has one DMS
     * @param ci 
     */
    private void createDevice(InfoController ci) {    
        Infra infra = tmo.getInfra();
        for(String n : ci.IOs) {
            if(n != null && !n.isEmpty()) {               
                dms = new SimDMS(n);       
                DMS realDMS = new DMS();
                realDMS.setSimDMS(dms);
                // add dms into infra, because infra doesn't have DMS objects                
                infra.addInfraObject(realDMS);
                
                // manager should know all infra information
                manager.addInfraObject(realDMS);
                //System.out.println("      [" + realDMS.getInfraType() + "] " + realDMS.getId());
                break;
            }
        }
        
        if(dms == null) {
            SFIMExceptionHandler.handle(new Exception("Can not create DMS for controller " + this.name));
        }
    }
    

    @Override
    /**
     * set this controller to responser
     * and execute doResponse method of responser
     */    
    public void doResponse(Responser rsp) {
        rsp.setController(this);
        rsp.doResponse();
    }

    @Override
    /**
     * @param o {controller information, vissim controller, metering plan id}) from SFIMManager
     */    
    public void set(Object[] o) {
        InfoController ci = this.getInfo(o);
        this.vissimController = this.getVC(o);        
        this.createDevice(ci);
        // run every 300 step (1step = 0.1s, when dimension = 1)
//        this.vissimController.addStepListener(300, this);
    }

    /**
     * @return simulation DMSs
     */
    public SimDMS getDMS() {
        return this.dms;
    }

//    @Override
//    public void runStepListener() {
//        if(!SFIMConfig.USE_VSA) return;
//        //System.out.println("   -> " + this.name + " (DMSXML) step run");
//        // if dms.vsa is not int VSA, it will return NULL
//        // see VSA
//        VSA vsa = VSA.getVSA(this.dms.vsa);
//        if(vsa == null) return;
//                
//        else this.vissimController.setVSA(this.dms.getId(), vsa);
//    }


}
