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

/**
 *
 * @author Chongmyung Park
 */

public enum CommProtocol {
    NTCIP_B(0), MNDOT_4(1), MNDOT_5(2), DMS_LITE(9), NTCIP_A(11);

    int id;

    CommProtocol(int id) { this.id = id; }

    public int getId() { return this.id; }

    public static CommProtocol getCommProtocol(int id) {
        for(CommProtocol p : CommProtocol.values()) {
            if(p.id == id) return p;
        }
        return null;
    }
    
    public boolean isMnDot()
    {
        return ( this == MNDOT_4 || this == MNDOT_5);
    }
    
    public boolean isDMSLite()
    {
        return ( this == DMS_LITE );
    }
}
