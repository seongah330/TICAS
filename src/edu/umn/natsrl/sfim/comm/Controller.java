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

package edu.umn.natsrl.sfim.comm;

import edu.umn.natsrl.sfim.comm.dmsxml.ControllerDMSLite;
import edu.umn.natsrl.sfim.comm.mndot.Controller170;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.sfim.SFIMManager;
import edu.umn.natsrl.vissimcom.VISSIMController;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class Controller {
    protected int drop;
    protected String name;
    protected TMO tmo = TMO.getInstance();
    protected VISSIMController vissimController;
    protected SFIMManager manager = SFIMManager.getInstance();
    protected SimObjects simObjects = SimObjects.getInstance();
    protected Vector<String> IOs;
    /**
     * constructor
     * @param drop  drop number of controller in comm_link
     */
    public Controller(int drop) {
        this.drop = drop;
    }    
    
    public static Controller createController(CommProtocol protocol, int drop)
    {
        if(protocol.isMnDot()) return new Controller170(drop);
        else if(protocol.isDMSLite()) return new ControllerDMSLite(drop);
        return null;
    }
    
    public int getDrop() {
        return drop;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setIO(Vector<String> IOs){
        this.IOs = IOs;
    }
    
    public Vector<String> getIO(){
        return this.IOs;
    }

    protected InfoController getInfo(Object o[]) {
        return (InfoController)o[0];
    }
    
    protected int getPlanId(Object o[]) {
        return(Integer)o[2];
    }
    
    protected VISSIMController getVC(Object o[]) {
        return (VISSIMController)o[1];
    }
    
    //public abstract void executeEverySimulationStep();
    //public abstract void addTrafficData(int detector_id, int volume, int flow, float speed, float density);
    public abstract void doResponse(Responser rsp);
    public abstract void set(Object[] object);

    public String getName() {
        return this.name;
    }
}
