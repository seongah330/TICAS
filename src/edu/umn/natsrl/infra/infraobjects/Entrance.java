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

package edu.umn.natsrl.infra.infraobjects;

import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park
 */
public class Entrance extends RNode {
    
    ArrayList<Detector> queue = new ArrayList<Detector>();
    Detector passage, bypass, merge, green;
    RampMeter meter;
    
    public Entrance(Element ele) {
        super(ele);
        setEntranceDetectors();
    }

    private void setEntranceDetectors() {
        for(Detector d : this.getDetectors()) {
            if("Q".equals(d.getCategory())) queue.add(d);
            else if("B".equals(d.getCategory())) bypass = d;
            else if("P".equals(d.getCategory())) passage = d;
            else if("G".equals(d.getCategory())) green = d;
            else if("M".equals(d.getCategory())) merge = d;
        }
    }

    public Detector getBypass() {
        return bypass;
    }

    public Detector getGreen() {
        return green;
    }

    public Detector getMerge() {
        return merge;
    }

    public Detector getPassage() {
        return passage;
    }

    public ArrayList<Detector> getQueue() {
        return queue;
    }
    
    public void setMeter(RampMeter meter) {
        this.meter = meter;
    }
    
    public String toString() {
        return "Ent (" + this.getLabel() + ")";
    }
    
}
