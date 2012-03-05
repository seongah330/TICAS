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
import edu.umn.natsrl.infra.simobjects.SimDMS;
import edu.umn.natsrl.infra.types.InfraType;

/**
 * This class is not used now.
 * because 'tms_config.xml' does not include dms information
 * @author Chongmyung Park
 */
public class DMS extends InfraObject {
    
    SimDMS simDMS;

    public DMS() {
        this.infraType = InfraType.DMS;
    }
    
    public DMS(String id) {
        this();
        this.id = id;
    }
    
    public void setSimDMS(SimDMS simDMS) {
        this.simDMS = simDMS;
        this.id = simDMS.name;
    }

}
