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
package edu.umn.natsrl.ticas.plugin.simulation.IRIS;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.ticas.plugin.metering.Metering;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLSim;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLVersion;
import edu.umn.natsrl.vissimcom.VISSIMVersion;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class irisSim extends VSLSim{
    Metering metering;
    
    public irisSim(String caseFile, int seed, Section section, VISSIMVersion v, VSLVersion _vv, int Intv){
        super(caseFile,seed,section,v,_vv,Intv);
        metering = new Metering(section,meters,detectors);
    }
    
    @Override
    public void ExecuteAfterRun() {
        super.ExecuteAfterRun();
        metering.run(samples, vc.getCurrentTime());
    }
    
    @Override
    public void DebugMassage() {
        super.DebugMassage();
        DisplayStationState();
        metering.printEntrance();
    }
}
