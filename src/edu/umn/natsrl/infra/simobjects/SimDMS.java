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

package edu.umn.natsrl.infra.simobjects;

import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.vissimcom.VISSIMController;
import edu.umn.natsrl.vissimcom.VSA;

/**
 *
 * @author Chongmyung Park
 */
public class SimDMS extends SimObject {
    
    private DMS dms;
    
    public long dms_id;
    public String name;
    
    public int vsa;
    public String ErrMsg = "";
    public String MsgText = "";    
    public boolean isValid = true;
    public String Bitmap = "";
    
    public int ActPriority = 4;
    public int RunPriority = 4;
    public boolean UseOnTime = false;
    public String OnTime = "";  // date (yyyy-MM-dd'T'HH:mm:ss'Z')
    public boolean UseOffTime = false;
    public String OffTime = "";  // date (yyyy-MM-dd'T'HH:mm:ss'Z')  
    public int DisplayTimeMS = 1000;    
    
    /** DMS physical parameters */
    final public String signAccess = "Back";
    final public String model = "Simulation Framework for IRIS and Microscopic simulator";
    final public String make = "UMD";
    final public String Owner = "UMD";
    final public String version = "1.0";
    final public String dmsType = "VMS Full-Matrix";
    final public int horizBorder = 49;
    final public int vertBorder = 56;
    final public int horizPitch = 20;
    final public int vertPitch = 20;
    final public int signHeight = 1372;
    final public int signWidth = 1676;
    final public int characterHeightPixels = 0;
    final public int characterWidthPixels = 0;
    final public int signHeightPixels = 64;
    final public int signWidthPixels = 80;
    final public boolean MsgTextAvailable = true;

    final public boolean UseBitmap = false;
    private boolean isUpdatedVSA = false;

    


    public SimDMS() {
        this.type = SimDeviceType.DMS;
    }

    public SimDMS(String n) {
        this();
        this.id = n;
        this.name = n;
    }

    public SimDMS(DMS dms) {
        this(dms.getId());
        this.dms = dms;
    }
        

    public void updateVSA(int vsa) {
        this.vsa = vsa;
        this.isUpdatedVSA = true;
    }
    
    public boolean isUpdated() {
        return this.isUpdatedVSA;
    }
    
    public void resetUpdated() {
        this.isUpdatedVSA = false;
    }
    
    public VSA getVSA() {
        if(!isUpdatedVSA){
            return VSA.getMaxVSA();
        }
        return VSA.getVSA(this.vsa);
    }

    public DMS getDms() {
        return dms;
    }
    
    public void setVSAtoVISSIM(VISSIMController vc) {
        if(vc == null){
            return;
        }
        vc.setVSA(this.getId(), getVSA());
    }

    @Override
    public void reset() {  }
}
