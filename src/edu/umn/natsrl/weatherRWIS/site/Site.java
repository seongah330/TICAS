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

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.map.CoordinateConversion;
import edu.umn.natsrl.weatherRWIS.datas.Atmospheric;
import edu.umn.natsrl.weatherRWIS.datas.Surface;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class Site extends SiteInfraObject{
        double lat;
        double lon;
        int easting;
        int northing;
        SiteType stype;
        
        private Atmospheric atmospheric;
        private Surface surface;
        SiteInfraType getI;
        
        public Site(int _id, String _name, double _lat, double _lon, SiteType _stype){
                super(_id,_name);
                setLatLon(_lat,_lon);
                stype = _stype;
                infraType = SiteInfraType.SITE;
                atmospheric = new Atmospheric(_id);
                surface = new Surface(_id);
                setLocation();
        }

        public boolean loadData(Period period) {
                atmospheric.loadData(period);
                surface.loadData(period);
                
                if(isLoaded())
                        return true;
                else
                        return false;
        }
        
        public Atmospheric getAtmospheric(){
                return atmospheric;
        }
        
        public Surface getSurface(){
                return surface;
        }
        
        public double getLat(){
                return lat;
        }
        
        public double getLon(){
                return lon;
        }
        
        public int getEasting(){
                return easting;
        }
        
        public int getNorthing(){
                return northing;
        }
        
        /**
         * set Location
         */
        private void setLocation() {
        CoordinateConversion converter = new CoordinateConversion();
        String en = converter.latLon2UTM(getLat(), getLon());
        String[] EN = en.split(" ");
        if(EN.length > 3){
            easting = Integer.parseInt(EN[2]);
            northing = Integer.parseInt(EN[3]);
        }
    }

        private void setLatLon(double _lat, double _lon) {
                int value = 10000000;
                lat = _lat / value;
                lon = _lon / value;
        }
        
        public boolean isLoaded(){
                return atmospheric.isLoaded() && surface.isLoaded();
        }
        
        public String printError(){
                return "Atmospheric Error - "+atmospheric.getError().getErrorMsg()+
                "Surface Error - "+surface.getError().getErrorMsg();
        }
}
