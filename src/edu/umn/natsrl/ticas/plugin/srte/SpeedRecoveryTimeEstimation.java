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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.ticas.plugin.ITicasPlugin;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */

@PluginImplementation
public class SpeedRecoveryTimeEstimation implements ITicasPlugin {

    @Override
    public void init(PluginFrame frame) {
        // set dialog properties
        frame.setSize(800, 570);       
        frame.setResizable(false);
        frame.setLocationRelativeTo(frame.getParent());
        
        // add GUI to dialog
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(new SRTEMainPanel(frame));
    }

}
