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

import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.InfraProperty;
import edu.umn.natsrl.infra.types.InfraType;
import org.w3c.dom.Element;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class RampMeter extends InfraObject {

    public RampMeter(Element element) {
        super(element);
        this.id = getProperty(InfraProperty.id);
        this.infraType = InfraType.METER;        
    }

    public RampMeter() {
        this.infraType = InfraType.METER;
    }
    
    public String getLabel()
    {
        return getProperty(InfraProperty.label);
    }
    
    public int getStorage()
    {
        return getPropertyInt(InfraProperty.storage);
    }
    
    public int getMaxWait()
    {
        return getPropertyInt(InfraProperty.max_wait);
    }  
    
    public String getGreen()
    {
        return getProperty(InfraProperty.green);
    }   
    
    public String getPassage()
    {
        return getProperty(InfraProperty.passage);
    }     
    
    public String[] getQueue()
    {
        return getPropertyArray(InfraProperty.queue);
    } 
    
    public String getByPass()
    {
        return getProperty(InfraProperty.bypass);
    }     
    
    public String getMerge()
    {
        return getProperty(InfraProperty.merge);
    }
    
    public void setPassage(String name) {
        this.setProperty(InfraProperty.passage, name);
    }
    
    public void setMerge(String name) {
        this.setProperty(InfraProperty.merge, name);
    }    
    
    public void setByPass(String name) {
        this.setProperty(InfraProperty.bypass, name);
    } 
    
    public void setGreen(String name) {
        this.setProperty(InfraProperty.green, name);
    }    

    public void setQueue(String names) {
        this.setProperty(InfraProperty.queue, names);
    }    
}
