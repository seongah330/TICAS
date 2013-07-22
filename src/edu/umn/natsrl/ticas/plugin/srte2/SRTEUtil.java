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
package edu.umn.natsrl.ticas.plugin.srte2;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEUtil {
    public static double calculateStep(double d, int step) {
        int origin = (int)d;
        if(step == 0)
            return 0;
        int division = origin / step;
        return step * division;
    }
    
    public static double[] CalculateSmoothedSpeed(double[] q, double[] k) {
        double[] u = new double[q.length];
        
        for(int i = 0; i < q.length; i++)
            u[i] = (k[i] == 0) ? 0 : q[i] / k[i];
        
        return u;
    }
}
