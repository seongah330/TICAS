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
package edu.umn.natsrl.ticas.Simulation;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.ticas.SimulationResultSaveDialog;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLResultManager;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLResults;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
class VSLSimulationResultSaveDialog extends SimulationResultSaveDialog{
    VSLResults vslresults;
    public VSLSimulationResultSaveDialog(PluginFrame simFrame, Section simSection, Period simPeriod, VSLResults vslResults) {
        super(simFrame,simSection,simPeriod);
        vslresults = vslResults;
    }
    
    @Override
    protected void saveResult(){
        super.saveResult();
        
        VSLResultManager vm = new VSLResultManager(fname, desc, vslresults);
        try {
            vm.save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
