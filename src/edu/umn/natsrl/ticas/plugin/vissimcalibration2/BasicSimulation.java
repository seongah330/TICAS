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
package edu.umn.natsrl.ticas.plugin.vissimcalibration2;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.ticas.Simulation.SimInterval;
import edu.umn.natsrl.ticas.Simulation.Simulation;
import edu.umn.natsrl.ticas.Simulation.SimulationGroup;
import edu.umn.natsrl.ticas.Simulation.SimulationImpl;
import edu.umn.natsrl.vissimcom.VISSIMVersion;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicSimulation extends Simulation implements SimulationImpl{
    
    public BasicSimulation(String caseFile, int seed, Section section, VISSIMVersion v, SimInterval SimIntv) {
        super(caseFile,seed,section,v,SimIntv);
    }

    @Override
    public void RunningInitialize() {
        super.RunningInitialize();
        super.setDebugEntranceInfo(false);
    }

    @Override
    public void ExecuteBeforeRun() {
        super.ExecuteBeforeRun();
    }

    @Override
    public void ExecuteAfterRun() {
        super.ExecuteAfterRun();
    }

    @Override
    public void DebugMassage() {
        super.DebugMassage();
    }
    
}
