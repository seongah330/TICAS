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

package edu.umn.natsrl.infra.types;

import edu.umn.natsrl.infra.infraobjects.Access;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.Interchange;
import edu.umn.natsrl.infra.infraobjects.Intersection;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.infraobjects.Station;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public enum InfraType {
    CORRIDOR(Corridor.class, "corridor"),
    STATION(Station.class, "Station"),
    ENTRANCE(Entrance.class, "Entrance"),
    EXIT(Exit.class, "Exit"),
    METER(RampMeter.class, "meter"),
    DETECTOR(Detector.class, "detector"),
    RNODE(RNode.class, "RNode"),
    ACCESS(Access.class, "Access"),
    INTERSECTION(Intersection.class, "Intersection"),
    DMS(DMS.class, "DMS"),
    INTERCHANGE(Interchange.class, "Interchange");

    private Class typeClass;
    private String typeTag;

    InfraType(Class c, String tag) {
        this.typeClass = c;
        this.typeTag = tag;
    }

    public static InfraType get(String tag)
    {
        for(InfraType it : InfraType.values())
        {
            if(tag.equals(it.typeTag)) return it;
        }
        return null;
    }

    public Class getTypeClass()
    {
        return this.typeClass;
    }

    public boolean isStation()
    {
        return (this == STATION);
    }

    public boolean isMeter() {
        return (this == METER);
    }
    
    public boolean isDetector() {
        return (this == DETECTOR);
    }    
    
    public boolean isDMS() {
        return (this == DMS);
    }

    public boolean isEntrance() {
        return (this == ENTRANCE);
    }    

    public boolean isExit() {
        return (this == EXIT);
    }        
    
    public boolean isRnode() {
        return (this == RNODE || this == STATION || this == ENTRANCE || this == EXIT || this == INTERSECTION);
    }
}
