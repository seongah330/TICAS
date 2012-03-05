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

package edu.umn.natsrl.infra;

import edu.umn.natsrl.infra.infraobjects.Station;

/**
 *
 * @author Chongmyung Park
 */
public class Main {
    public static void main(String[] args)
    {
        
//        Calendar c1 = Calendar.getInstance();
//        c1.set(2011, 1, 1, 7, 0, 0);
//        Calendar c2 = Calendar.getInstance();
//        c2.set(2011, 1, 1, 8, 0, 0);
//        Period period = new Period(c1.getTime(), c2.getTime(), 30);
        
        TMO tmo = TMO.getInstance();
        tmo.setup();
//        Station S910 = tmo.infra.getStation("S910");
//        Station S911 = tmo.infra.getStation("S911");
//        System.out.println(TMO.getDistanceInFeet(S910,S911) );
//        System.out.println(TMO.getDistanceInMile(S910,S911) );
    }
}
