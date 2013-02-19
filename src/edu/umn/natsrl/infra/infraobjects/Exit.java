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

import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.infra.types.TransitionType;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park
 */
public class Exit extends RNode {
        
    public Exit(Element ele) {
        super(ele);
    }

    public Exit(String id, String label, int lanes, int easting, int northing, InfraType itype) {
        this.id = id;
        this.infraType = itype;
        setProperty(InfraProperty.label,label);
        setProperty(InfraProperty.lanes,lanes);
        setProperty(InfraProperty.shift,0);
        setProperty(InfraProperty.transition,"Leg");
        setProperty(InfraProperty.attach_side,"right");
        setProperty(InfraProperty.s_limit,55);
        this.transitionType = TransitionType.Leg;
        
        this.isAvailable = true;
        this.easting = easting;
        this.northing = northing;
    }

    @Override
    public String toString()
    {
//        return "Exit";// ("+this.id+")";
        return "Exit (" + this.getLabel() + ")";
        
    }
}
