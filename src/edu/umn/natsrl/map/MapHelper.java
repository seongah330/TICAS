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

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.section.SectionHelper.State;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.Corridor.Direction;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.map.InfraPoint.LABEL_LOC;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class MapHelper {

    Set<InfraPoint> waypoints;
    private TMO tmo = TMO.getInstance();
    private JXMapViewer map;
    private final JXMapKit jmKit;
    private Frame frame;
    private MouseAdapter mouseListner;
    private Infra infra;
    private InfraObject[] sectionList;
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;
    private int initZoom = 6;
    
    /**
     * Constructor
     * @param jmKit 
     */
    public MapHelper(JXMapKit jmKit) {
        this.jmKit = jmKit;
        this.map = jmKit.getMainMap();
        this.infra = tmo.getInfra();
        init();
    }

    /**
     * Set markers in the map (main renderer)
     * @param markers 
     */
    private void setMarkers(Set<InfraPoint> markers) {  
        WaypointPainter painter = new WaypointPainter();
        waypoints = markers;

        if(this.sectionList != null) {            
            for(InfraObject ip : sectionList) {
                for(InfraPoint wp : markers) {
                    if(!wp.getInfraObject().equals(ip)) continue;
                    wp.setMarkerType(InfraPoint.MarkerType.BLUE);
                    break;
                }                
            }      
        }        
        
        painter.setWaypoints(markers);
        painter.setRenderer(new WaypointRenderer() {

            @Override
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                InfraPoint ip = (InfraPoint) wp;
                int markerX = 0, markerY = 0;
                Image markerImg = ip.getMarkerImg();
                markerX = Math.round(-1 * ((float) markerImg.getWidth(map)) / 2);
                markerY = Math.round(-1 * ((float) markerImg.getHeight(map)));
                g.drawImage(markerImg, markerX, markerY, null);

                if (ip.isShowLabel()) {
                    String name = ip.getName();
                    if (name == null) {
                        name = ip.getInfraObject().getId();
                    }
                    Point p = ip.getLabelLocation(g.getFontMetrics().stringWidth(name), g.getFontMetrics().getHeight());
                    g.setPaint(ip.getLabelColor());                    
                    g.drawString(name, p.x+ip.offset_x, p.y+ip.offset_y);
                }               
                return true;
            }
        });
        jmKit.getMainMap().setOverlayPainter(painter);
    }

    /**
     * Return clicked marker
     * @param point
     * @return 
     */
    public InfraPoint getClickedMarker(Point point) {
        if (waypoints == null) {
            return null;
        }
        Iterator<InfraPoint> itr = waypoints.iterator();
        while (itr.hasNext()) {
            InfraPoint marker = itr.next();
            Rectangle markerRect = getMarkerRect(marker);
            if (markerRect.contains(point)) {
                return marker;
            }
        }
        return null;
    }
    
    
    /**
     * Set markers of infra object in given section
     * @param section 
     */
    public void showSection(Section section) {
        SectionHelper sectionHelper = new SectionHelper(section, false);
        Set<InfraPoint> markers = new HashSet<InfraPoint>();

        for (State s : sectionHelper.getStates()) {
            RNode rnode = s.getRNode();
            InfraPoint ip = new InfraPoint(rnode);
            ip.setLabelLoc(getLabelLoc(rnode.getCorridor().getDirection()));
            markers.add(ip);
            if (rnode.isEntrance()) {
                Entrance e = (Entrance) rnode;
                EntranceState es = (EntranceState) s;
                RampMeter meter = es.getMeter();
                if (meter == null) {
                    continue;
                }
                ip.setName(ip.getName() + " - [" + meter.getId() + "]");
            }
        }

        this.setMarkers(markers);
    }
    
    /**
     * Set markers of corridor
     * @param corridor 
     */
    public void showCorridor(Corridor corridor) {
        showCorridors(new Corridor[]{corridor});
    }
    
    /**
     * Set markers of corridors
     * @param corridors 
     */
    public void showCorridors(Corridor[] corridors) {
        ArrayList<InfraObject> objs = new ArrayList<InfraObject>();
        for(Corridor c : corridors) {
            objs.addAll(getRNodeFromCorridor(c));
        }
        this.showInfraObjects(objs.toArray(new InfraObject[objs.size()]));        
    }

    /**
     * Set markers of given infra objects
     * @param objs 
     */
    public void showInfraObjects(InfraObject[] objs) {
        
        Set<InfraPoint> markers = getMarkers(objs);
        checkDuplicateLocation(markers);
        this.setMarkers(markers);
    }
    
    /**
     * Add markers of given infra objects
     * @param objs 
     */
    public void addShowInfraObjects(InfraObject[] objs) {
        this.waypoints.addAll(getMarkers(objs));
        checkDuplicateLocation(waypoints);
        this.setMarkers(waypoints);        
    }
    
    /**
     * Add markers of given infra objects
     * @param objs 
     */
    public void addShowCorridor(Corridor c) {
        ArrayList<InfraObject> rnodes = getRNodeFromCorridor(c);
        this.waypoints.addAll(getMarkers(rnodes.toArray(new InfraObject[rnodes.size()])));
        checkDuplicateLocation(waypoints);
        this.setMarkers(waypoints);        
    }    

    /**
     * Return markers from infra object array
     * @param objs
     * @return 
     */
    private Set<InfraPoint> getMarkers(InfraObject[] objs) {
        Set<InfraPoint> markers = new HashSet<InfraPoint>();
        for (InfraObject o : objs) {
            InfraType type = o.getInfraType();
            if (type.isRnode()) {
                RNode rn = (RNode) o;
                InfraPoint ip = new InfraPoint(rn);
                ip.setLabelLoc(getLabelLoc(rn.getCorridor().getDirection()));
                markers.add(ip);
            } else if (type.isDetector()) {
                Detector d = (Detector) o;
                RNode rn = d.getRNode();
                InfraPoint ip = new InfraPoint(d.getId(), rn.getEasting(), rn.getNorthing());
                ip.setInfraObject(d);
                ip.setLabelLoc(getLabelLoc(rn.getCorridor().getDirection()));
                markers.add(ip);
            } else if (type.isMeter()) {
                RampMeter m = (RampMeter) o;
                Detector d = infra.getDetector(m.getPassage());
                if (d == null) {
                    d = infra.getDetector(m.getMerge());
                }
                if (d == null) {
                    JOptionPane.showMessageDialog(null, "Cannot identify " + m.getId() + "'s location");
                    continue;
                }
                RNode rn = d.getRNode();
                InfraPoint ip = new InfraPoint(m.getId(), rn.getEasting(), rn.getNorthing());
                ip.setInfraObject(m);
                ip.setLabelLoc(getLabelLoc(rn.getCorridor().getDirection()));
                markers.add(ip);
            }
        }
        return markers;
    }
    
    /**
     * Return rnode list from corridor
     * @param corridor
     * @return 
     */
    private ArrayList<InfraObject> getRNodeFromCorridor(Corridor corridor) {
        ArrayList<InfraObject> objs = new ArrayList<InfraObject>();
        for (RNode rnode : corridor.getRnodes()) {
            if (rnode.isStation()) {
                if (rnode.isAvailableStation()) {
                    objs.add(rnode);
                }
            } else {
                // CD node
//                if(rnode.isExit() && rnode.getTransitionType().isCD()) {
//                    RNode cdnode = rnode.getDownStreamNodeToOtherCorridor();
//                    while(cdnode.getDownstreamNodeToSameCorridor() != null)
//                    {
//                        cdnode = cdnode.getDownstreamNodeToSameCorridor();
//                        if(cdnode.isExit() && cdnode.getTransitionType().isCommon()) {
//                            objs.add(cdnode);
//                            break;
//                        }
//                    }
//                } else {
                    objs.add(rnode);
//                }
            }
        }     
        return objs;
    }    
    
    /**
     * Return label location of marker
     * @param dir
     * @return 
     */
    private LABEL_LOC getLabelLoc(Direction dir) {
        if (dir == Direction.EB) {
            return LABEL_LOC.BOTTOM;
        }
        if (dir == Direction.WB) {
            return LABEL_LOC.TOP;
        }
        if (dir == Direction.SB) {
            return LABEL_LOC.LEFT;
        }

        return LABEL_LOC.RIGHT;
    }
    
    /**
     * Remove and integrate duplicated location from the marker list
     * @param markers 
     */
    private void checkDuplicateLocation(Set<InfraPoint> markers) {
        InfraPoint[] points = markers.toArray(new InfraPoint[markers.size()]);
        ArrayList<Integer> toRemoveIndex = new ArrayList<Integer>();
        for (int i = 0; i < points.length - 1; i++) {
            for (int j = i + 1; j < points.length; j++) {
                if(points[i].getInfraObject().equals(points[j].getInfraObject())) {
                    toRemoveIndex.add(j);
                }
                GeoPosition pos1 = points[i].getPosition();
                GeoPosition pos2 = points[j].getPosition();
                if (pos1.equals(pos2)) {
                    points[i].setName(points[i].getName() + ", " + points[j].getName());
                    toRemoveIndex.add(j);
                }
            }
        }
        for (int idx : toRemoveIndex) {
            markers.remove(points[idx]);
        }
    }    
    
    /**
     * Remove and integrate duplicated location from the marker list
     * @param markers 
     */
    private void checkDuplicateLocation_new(Set<InfraPoint> markers) {
        InfraPoint[] points = markers.toArray(new InfraPoint[markers.size()]);
        ArrayList<Integer> toRemoveIndex = new ArrayList<Integer>();
        for (int i = 0; i < points.length - 1; i++) {
            RNode rn1 = points[i].getRNode();
            for (int j = i + 1; j < points.length; j++) {
                RNode rn2 = points[j].getRNode();
                int distance = TMO.getDistanceInFeet(rn1, rn2) ;
                
                // real duplicated
                if(points[i].getInfraObject().equals(points[j].getInfraObject())) {
                    toRemoveIndex.add(j);
                    continue;
                }
                else {
                    GeoPosition pos1 = points[i].getPosition();
                    GeoPosition pos2 = points[j].getPosition();
                    if (pos1.equals(pos2)) {
                        points[i].setName(points[i].getName() + ", " + points[j].getName());
                        toRemoveIndex.add(j);
                    }
                }
                
                // very close
//                if(distance < 15) {     
//                    System.out.println("close : distance=" + distance + " > " + rn1.getId() + "(" + rn1.getStationId() + ") -- " + rn2.getId() + "("+ rn2.getStationId()+")");
//                    int offset_easting = 0;
//                    int offset_northing = 0;                    
//                    switch(rn1.getCorridor().getDirection()) {
//                        case EB : offset_northing -= 10; break;
//                        case WB : offset_northing += 10; break;
//                        case SB : offset_easting -= 10; break;
//                        case NB : offset_easting += 10; break;
//                    }
//                    points[j].offset_x = offset_easting;
//                    points[j].offset_y = offset_northing;
//                }
                                
            }
        }
        for (int idx : toRemoveIndex) {
            markers.remove(points[idx]);
        }
    }

    /**
     * Initialize
     *   - add mouse click event
     */
    private void init() {
        mouseListner = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                InfraPoint ip = getClickedMarker(e.getPoint());
                if (ip == null) {
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {                    
                    InfraObject io = ip.getInfraObject();
                    if (io != null) {
                        InfraInfoDialog rid = new InfraInfoDialog(io, frame, true);
                        int w = rid.getWidth();
                        int h = rid.getHeight();
                        int sx = e.getXOnScreen();
                        int sy = e.getYOnScreen();
                        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                        if(dim.width < sx+w) sx -= w;
                        if(dim.height < sy+h) sy -= h;
                        rid.setLocation(sx, sy);
                        rid.setVisible(true);
                    }
                } else {
                    if (ip.isShowLabel()) {
                        ip.setLabelVisible(false);
                    } else {
                        ip.setLabelVisible(true);
                    }
                }
                map.repaint();
            }
        };
        jmKit.getMainMap().addMouseListener(mouseListner);
        GeoPosition initCenter = new GeoPosition(initLatitude, initLongitude);
        jmKit.setAddressLocation(initCenter);
        jmKit.setZoom(initZoom);        
    }
    
    /**
     * Set points to draw line
     * @param list 
     */
    public void setLinePoints(InfraObject[] list) {        
        this.sectionList = list;       
        this.repaint();
    }    
    
    /**
     * Return marker that indicate given infra object
     * @param obj
     * @return 
     */
    private InfraPoint findPointFromMap(InfraObject obj) {
        for(InfraPoint p : this.waypoints) {
            if(p.getInfraObject().equals(obj)) return p;
        }
        return null;
    }    
    
    /**
     * Return rectangle of given marker
     * @param marker
     * @return 
     */
    private Rectangle getMarkerRect(InfraPoint marker) {
        int w, h, x, y;
        Image markerImg = marker.getMarkerImg();
        w = markerImg.getWidth(map);
        h = markerImg.getHeight(map);
        x = Math.round(-1 * ((float) w) / 2);
        y = Math.round(-1 * ((float) h));
        GeoPosition gp = marker.getPosition();
        Point2D gp_pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
        Rectangle rect = map.getViewportBounds();
        Point converted_gp_pt = new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
        return new Rectangle(converted_gp_pt.x + x, converted_gp_pt.y + y, w, h);
    }

    /**
     * Set center position
     * @param infraObject 
     */
    public void setCenter(InfraObject infraObject)
    {
        CoordinateConversion converter = new CoordinateConversion();
        InfraType type = infraObject.getInfraType();
        RNode rnode = null;
        if (type.isRnode()) {
            rnode = (RNode) infraObject;
        } else if (type.isDetector()) {
            Detector d = (Detector) infraObject;
            rnode = d.getRNode();
        } else if (type.isMeter()) {
            RampMeter m = (RampMeter) infraObject;
            Detector d = infra.getDetector(m.getPassage());
            if (d == null) {
                d = infra.getDetector(m.getMerge());
            }
            if (d == null) {
                JOptionPane.showMessageDialog(null, "Cannot identify " + m.getId() + "'s location");
                return;
            }
            rnode = d.getRNode();
        }

        double[] lat = converter.utm2LatLon("15 T " + rnode.getEasting() + " " + rnode.getNorthing());

        map.setCenterPosition(new GeoPosition(lat[0], lat[1]));        
    }
    
    public void setCenter(InfraPoint infraPoint)
    {
        map.setCenterPosition(infraPoint.getPosition());
    }
    
    /**
     * Zoom as given zoom level and center the given point
     * @param marker
     * @param zoomLevel 
     */
    public void zoom(int zoomLevel) {
        map.setZoom(zoomLevel);
    }

    /**
     * Set main frame
     * @param frame 
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Remove mouse listener
     */
    public void removeMouseListener() {
        this.map.removeMouseListener(mouseListner);
    }
    
    /**
     * Return infra points that marked on the map
     * @return 
     */
    public InfraPoint[] getPoints() {
        if(this.waypoints == null) return null;
        return this.waypoints.toArray(new InfraPoint[this.waypoints.size()]);
    }

    public void repaint() {
        if(this.waypoints != null) {
            this.setMarkers(waypoints);
        }
    }

    public void clear() {
        this.setMarkers(new HashSet<InfraPoint>());
        this.sectionList = null;
    }
}
