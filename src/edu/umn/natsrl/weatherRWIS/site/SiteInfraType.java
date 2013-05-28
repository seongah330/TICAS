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

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public enum SiteInfraType {
        SITE(Site.class, "Site"),
        SITEGROUP(SiteGroup.class, "SiteGroup");
        
        private Class typeClass;
        private String typeTag;
        
        SiteInfraType(Class c, String tag){
                this.typeClass = c;
                this.typeTag = tag;
        }
        
        public static SiteInfraType get(String tag){
                for(SiteInfraType it : SiteInfraType.values())
                        if(tag.equals(it.typeTag)) return it;
                return null;
        }
        
        public Class getTypeClass(){
                return typeClass;
        }
        
        public String getTag(){
                return typeTag;
        }
}
