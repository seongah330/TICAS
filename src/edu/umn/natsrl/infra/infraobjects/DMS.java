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

import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.infraobjects.Corridor.Direction;
import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.map.CoordinateConversion;
import org.w3c.dom.Element;

/**
 * This class is not used now.
 * because 'tms_config.xml' does not include dms information
 * @author Chongmyung Park
 */
public class DMS extends InfraObject {
    public static int MAX_RANGE_TO_STATION = 800; //feet
    SimDMS simDMS;
    
    private int easting = 0;
    private int northing = 0;
    private int gid = 1;
    private DMSImpl dmsImpl;

    public DMS(){
        this.infraType = InfraType.DMS;
    }
    public DMS(Element element) {
        super(element);
        this.infraType = InfraType.DMS;
        this.id = this.getProperty("name");
        setLocation();
    }
    
    public DMS(String id) {
        this.infraType = InfraType.DMS;
        this.id = id;
    }
    
    public void setSimDMS(SimDMS simDMS) {
        this.simDMS = simDMS;
        this.id = simDMS.name;
    }
    
    void setDMSImpl(DMSImpl dmsimpl) {
        if(dmsimpl == null)
            return;
        this.dmsImpl = dmsimpl;
    }
    
    public DMSImpl getDMSImpl(){
        return dmsImpl;
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
    
    public void setGID(int _gid) {
        gid = _gid;
    }
    
    /**
     * get Group ID
     * @return 
     */
    public int getGID(){
        return gid;
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
    
    public int getWidth_Pixels(){
        return this.getPropertyInt(InfraProperty.width_pixels);
    }
    public int getHeight_Pixels(){
        return this.getPropertyInt(InfraProperty.height_pixels);
    }
    public String getCorridorName(){
        return getDescbySplit()[0];
    }
    public Direction getDirection(){
        if(getDescbySplit().length < 2){
            return Direction.ALL;
        }else{
            return Direction.get(getDescbySplit()[1]);
        }
    }
    public String getDesc(){
        return this.getProperty(InfraProperty.description);
    }
    public String[] getDescbySplit(){
        return this.getProperty(InfraProperty.description).split(" ");
    }

    

}
