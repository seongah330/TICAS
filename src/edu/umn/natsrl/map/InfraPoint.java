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

package edu.umn.natsrl.map;

import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.infraobjects.RNode;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class InfraPoint extends Waypoint {

    public enum LABEL_LOC { TOP, BOTTOM, RIGHT, LEFT; }
    public enum MarkerType { RED("red"), BLUE("blue"), PURPLE("purple"), GREEN("green"), GRAY("gray");
        String desc;
        private MarkerType(String desc) {
            this.desc = desc;
        }        
    }
    private String name;
    private Image markerImg;
    private boolean showLabel = true;
    private CoordinateConversion converter = new CoordinateConversion();
    private Color color = Color.black;
    private LABEL_LOC labelLoc = LABEL_LOC.RIGHT;
    private InfraObject infraObject;    
    public int offset_x = 0;
    public int offset_y = 0;
    
    public InfraPoint(String name, double latitude, double longitude) {
        super(latitude, longitude);
        this.name = name;
        setMarkerType(MarkerType.RED);
    }
    
    public void setMarkerType(MarkerType type) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        URL imageUrl = getClass().getResource("/edu/umn/natsrl/map/resources/marker-"+type.desc+".png");
        markerImg = toolkit.getImage(imageUrl);        
    }
    
    
    public InfraPoint(RNode rnode) {        
        double[] lat = converter.utm2LatLon("15 T " + rnode.getEasting() + " " + rnode.getNorthing());
        this.setPosition(new GeoPosition(lat[0], lat[1]));
        this.infraObject = rnode;
        if(rnode.isStation()) {
            this.name = rnode.getStationId();
        } else if(rnode.isEntrance()) {
            this.name = "Ent (" + rnode.getLabel() + ")";
        } else if(rnode.isExit()) {
            this.name = "Ext (" + rnode.getLabel() + ")";            
        } else {
            this.name = rnode.getInfraType() + "(" + rnode.getLabel() +")";
        }
        if(rnode.isStation()) setMarkerType(MarkerType.RED);
        else if(rnode.isEntrance()) setMarkerType(MarkerType.GREEN);
        else if(rnode.isExit()) setMarkerType(MarkerType.PURPLE);
        else setMarkerType(MarkerType.GRAY);
    }

    public InfraPoint(String name, int easting, int northing) {        
        double[] lat = converter.utm2LatLon("15 T " + easting + " " + northing);
        this.setPosition(new GeoPosition(lat[0], lat[1]));
        this.name = name;        
        setMarkerType(MarkerType.RED);
    }
    
    
    public Image getMarkerImg() {
        return this.markerImg;
    }
    
    public String getName() {
        return name;
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public Color getLabelColor() {
        return this.color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabelVisible(boolean b) {
        this.showLabel = b;
    }

    public void setLabelLoc(LABEL_LOC labelLoc) {
        this.labelLoc = labelLoc;
    }
    
    public LABEL_LOC getLabelLoc() {
            return this.labelLoc;
        }
    
    public Point getLabelLocation(int sw, int sh) {
        int x=0, y=0;
        int iw = this.markerImg.getWidth(null);
        int ih = this.markerImg.getHeight(null);
        if(this.labelLoc == LABEL_LOC.TOP) {
            x = -1 * sw/2;
            y = -1 * (ih + 5);
        }
        else if(this.labelLoc == LABEL_LOC.BOTTOM) {
            x = -1 * sw/2;
            y = 10;
        }        
        else if(this.labelLoc == LABEL_LOC.RIGHT) {
            x = 10;
            y = -5;
        }        
        else if(this.labelLoc == LABEL_LOC.LEFT) {
            x = -1 * (sw + 5 + iw/2);
            y = -5;
        }        
        return new Point(x, y);
    }

    public RNode getRNode() {
        if(this.infraObject.getInfraType().isRnode()) {
            return (RNode)this.infraObject;
        }
        return null;
    }
    
    public void setInfraObject(InfraObject o) {
        this.infraObject = o;
    }

    public InfraObject getInfraObject() {
        return infraObject;
    }
}
