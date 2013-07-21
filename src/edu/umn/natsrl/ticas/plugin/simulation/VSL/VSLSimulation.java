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
package edu.umn.natsrl.ticas.plugin.simulation.VSL;

import edu.umn.natsrl.ticas.plugin.ITicasPlugin;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.BorderLayout;
import java.awt.Container;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class VSLSimulation implements ITicasPlugin{

    @Override
    public void init(PluginFrame frame) {
        frame.setSize(1024,768);
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
//        container.add(new VSLSimulationGUI(frame));
    }
    
}
