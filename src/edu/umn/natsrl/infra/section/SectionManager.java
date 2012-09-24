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

package edu.umn.natsrl.infra.section;

import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.Station;
import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park
 */
public final class SectionManager {
    private Vector<Section> sections = new Vector<Section>();
    private String sectionDir = InfraConstants.SECTION_DIR;
    private TMO tmo = TMO.getInstance();
    
    public SectionManager() {
        //loadSections();
    }
    
    public Vector<Section> getSections()
    {
        return this.sections;
    }
    
    public void loadSections() {
        this.sections.clear();
        File file = new File(sectionDir);  
        File[] files = file.listFiles();  
        if(files == null) return;
                
        for (int i = 0; i < files.length; i++)  
        {             
           Section s = Section.load(files[i].getAbsolutePath());
           if(s == null) continue;
           loadRNodes(s);
           if(s != null) sections.add(s);
           else {
               System.out.println("\""+files[i].getName()+"\" can not be loaded to section");
               //files[i].delete();
           }
        }   
    }

    private void loadRNodes(Section s) {

        // retrieve r_node  
        for(String nid : s.getStationIds())
        {
            System.out.println("sid : "+nid+"\n");
            RNode node = tmo.getInfra().find(nid);
            if(node != null) s.addRNode(node);
        }     

        // make link among stations
        List<RNode> rnodes = s.getRNodes();
        if(rnodes.size() > 0) {
            RNode upStation = rnodes.get(0);
            for(RNode rnode : s.getRNodes())
            {
                if( rnode.isStation() && rnode != upStation) {
                    ((Station)upStation).setDownstreamStation(s.getName(), (Station)rnode);
                    upStation = rnode;
                }
            }
        }
    }
    
}
