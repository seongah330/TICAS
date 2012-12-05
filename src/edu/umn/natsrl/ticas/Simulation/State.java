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

import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.simobjects.SimDetector;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
/**
* State class to organize for metering
*/
public class State {

   StateType type;
   String id;
   int easting, northing;
   State upstream;
   State downstream;
   RNode rnode;
   double distanceToDownState = 0;
   public State(){

   }
   public State(String id, RNode rnode) {
       this();
       this.id = id;
       this.rnode = rnode;
       if(rnode != null) {
           this.easting = rnode.getEasting();
           this.northing = rnode.getNorthing();
       }
   }

   public boolean hasDetector(SimDetector sd) {
       if (sd == null) {
           return false;
       }
       return this.rnode.hasDetector(sd.getId());
   }

   public boolean hasDetector(SimDetector[] sds) {
       if (sds == null) {
           return false;
       }
       for (SimDetector sd : sds) {
           if (this.rnode.hasDetector(sd.getId())) {
               return true;
           }
       }
       return false;
   }

   public double getDistanceToDownState(){
       return distanceToDownState;
   }

   public State getDownStreamState(){
       return this.downstream;
   }

   public String getID(){
       return id;
   }

   public String getLabel(){
       return rnode.getLabel();
   }
}
