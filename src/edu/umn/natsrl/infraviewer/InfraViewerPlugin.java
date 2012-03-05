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

package edu.umn.natsrl.infraviewer;

import edu.umn.natsrl.ticas.plugin.ITicasPlugin;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Chongmyung Park
 */
@PluginImplementation
public class InfraViewerPlugin implements ITicasPlugin {

    @Override
    public void init(PluginFrame frame) {
     
        frame.setSize(990, 740);
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());        
        
        final InfraViewerPanel ivp = new InfraViewerPanel(frame);
        // initialize panel
        ivp.init();
        
        // add distance calculator to 'tools' menu
        JMenuItem distanceMenu = new JMenuItem("RNode Distance Calculator");
        distanceMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ivp.openDistanceDialog();
            }
        });
        frame.addToolsMenu(distanceMenu);        
        
        // set title
        frame.setTitle("Infra Viewer - NATSRL");
        
        // set icon
        frame.setIconImage(new ImageIcon(getClass().getResource("/edu/umn/natsrl/infraviewer/resources/mapmarker.png")).getImage());        
        
        // add panel to frame
        container.add(ivp);
    }
}