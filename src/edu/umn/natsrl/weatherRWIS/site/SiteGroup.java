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
package edu.umn.natsrl.weatherRWIS.site;

import java.util.HashMap;


/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SiteGroup extends SiteInfraObject{
        SiteGroupType gtype;
        String disc;
        
        HashMap<Integer, Site> sites = new HashMap<Integer, Site>();
        
        public SiteGroup(int _id, String _name, SiteGroupType _gtype, String _disc){
                super(_id,_name);
                gtype = _gtype;
                disc = _disc;
                infraType = SiteInfraType.SITEGROUP;
        }

        public void addSite(Site s) {
                sites.put(s.id, s);
        }

        public Iterable<Site> getSites() {
                return sites.values();
        }
}
