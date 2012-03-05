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
package edu.umn.natsrl.infra.types;

/**
 *
 * @author soobin Jeon
 */
public enum AdjustType {
    Average,
    Cumulative,
    Flow,
    DensityWithStation,
    SpeedWithStation;
    
    public boolean IsAverage(){
        return (this == Average);
    }
    public boolean IsCumulative(){
        return (this == Cumulative);
    }
    public boolean IsFlow(){
        return (this == Flow);
    }
    public boolean IsDensityWithStation(){
        return (this == DensityWithStation);
    }
    public boolean IsSpeedWithStation(){
        return (this == SpeedWithStation);
    }
}
