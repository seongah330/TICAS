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
package edu.umn.natsrl.util;

import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.RNode;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class DistanceUtil {
    static int FEETPERMILE = 5280;
    static public double getFeetToMile(int feet){
        double f = (double)feet;
        return (f / FEETPERMILE);
    }
    static public int getMileToFeet(double m){
        return (int)(m * FEETPERMILE);
    }
    
    public static int getDistanceInFeet(RNode r1, DMSImpl dms){
        return getDistanceInFeet(r1.getEasting(),dms.getEasting(),r1.getNorthing(),dms.getNorthing());
    }
    public static int getDistanceInFeet(int _e1, int _e2, int _n1, int _n2){
        double e1 = _e1;
        double e2 = _e2;
        double n1 = _n1;
        double n2 = _n2;
        return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
    }
    
    public static int getDistanceInFeet(RNode o1, RNode o2)
    {
        double e1 = o1.getEasting();
        double e2 = o2.getEasting();
        double n1 = o1.getNorthing();
        double n2 = o2.getNorthing();
        return (int) (Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609 * 5280);
    }
    
    public static float getDistanceInMile(RNode o1, RNode o2)
    {
        double e1 = o1.getEasting();
        double e2 = o2.getEasting();
        double n1 = o1.getNorthing();
        double n2 = o2.getNorthing();
        return (float)(Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609);
    }
}
