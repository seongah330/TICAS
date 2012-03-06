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

import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.Exit;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.map.InfraInfoDialog;
import edu.umn.natsrl.map.InfraPoint;
import edu.umn.natsrl.map.TMCProvider;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class SectionCreatorPanel extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    private Infra infra;
    private MapHelper mapHelper;
    private int initZoom = 4;
    // initial center position : University of Minnesota
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;
    private InfraPoint selectedPoint;
    private Point clickedPoint;
    private InfraPoint lastSectionPoint;
    private List<InfraObject> sectionPointList = new ArrayList<InfraObject>();
    private ArrayList<InfraObject> addedList = new ArrayList<InfraObject>();
    private int selectedCorridorIndex = 0;
    private ISectionChanged changeListener;
    
    /** Creates new form SectionCreatorPanel */
    public SectionCreatorPanel() {
        initComponents();

        if (!tmo.isLoaded()) {
            tmo.setup();
        }
        this.infra = tmo.getInfra();
        initMap();
    }

    private void initMap() {

        mapHelper = new MapHelper(jmKit);
        jmKit.setTileFactory(TMCProvider.getTileFactory());
        GeoPosition initCenter = new GeoPosition(initLatitude, initLongitude);

        jmKit.setAddressLocation(initCenter);
        jmKit.setZoom(initZoom);
        this.jmKit.getMiniMap().setVisible(false);

        this.mapHelper.removeMouseListener();
        jmKit.getMainMap().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                InfraPoint ip = mapHelper.getClickedMarker(e.getPoint());
                if (ip == null) {
                    return;
                }
                if (ip.isShowLabel()) {
                    ip.setLabelVisible(false);
                } else {
                    ip.setLabelVisible(true);
                }
                mapHelper.repaint();
            }

            public void mouseReleased(MouseEvent e) {
                InfraPoint ip = mapHelper.getClickedMarker(e.getPoint());

                if (ip != null && e.isPopupTrigger()) {

                    InfraObject o = ip.getInfraObject();
                    RNode rnode = (RNode) o;
                    menuItemSectionStart.setEnabled(true);
                    menuItemSectionThroughHere.setEnabled(true);
                    menuItemSectionEnd.setEnabled(true);

                    if (o.getInfraType().isStation()) {
                        menuItemSectionThroughHere.setEnabled(false);
                        if (sectionPointList.isEmpty()) {
                            menuItemSectionEnd.setEnabled(false);
                        } else {
                            menuItemSectionStart.setEnabled(false);
                        }
                    } else if (o.getInfraType().isExit()) {
                        if (sectionPointList.isEmpty()) {
                            menuItemSectionThroughHere.setEnabled(false);
                        }
                        RNode nextNode = rnode.getDownStreamNodeToOtherCorridor();
                        if (nextNode == null || nextNode.getCorridor().equals(rnode.getCorridor())) {
                            menuItemSectionThroughHere.setEnabled(false);
                        }
                        menuItemSectionStart.setEnabled(false);
                        menuItemSectionEnd.setEnabled(false);
                    } else {
                        menuItemSectionStart.setEnabled(false);
                        menuItemSectionThroughHere.setEnabled(false);
                        menuItemSectionEnd.setEnabled(false);
                    }

                    selectedPoint = ip;
                    clickedPoint = e.getLocationOnScreen();
                    popupMenu.show(jmKit.getMainMap(), e.getX(), e.getY());
                }
            }
        });

        loadCorridors();

        this.cbxCorridors.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {
                setSectionPoints();
            }
        });

    }
    
    private void setSectionPoints() {        
        mapHelper.clear();
        if (cbxCorridors.getSelectedIndex() != 0) {
            Corridor c = (Corridor) cbxCorridors.getSelectedItem();
            if(this.selectedCorridorIndex == 0) {
                RNode rn = c.getRnodes().get(0);
                mapHelper.setCenter(rn);
            }
            mapHelper.showCorridor(c);
        }
        // initialize on-makining-section
        mapHelper.setLinePoints(null);
        sectionPointList.clear();
        lastSectionPoint = null;
        updateRoutes();
        this.selectedCorridorIndex = cbxCorridors.getSelectedIndex();
    }    

    private void updateRoutes() {
        this.tbxRoutes.setText("");
        if (sectionPointList.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        ArrayList<InfraObject> list = getSectionRoute();
        for (int i = 0; i < list.size(); i++) {
            RNode rnode = (RNode) list.get(i);
            if (rnode.isStation()) {
                sb.append(rnode.getStationId());
            } else if (rnode.isExit()) {
                sb.append(" [" + rnode.getCorridor().getId() + " ");
            } else if (rnode.isEntrance()) {
                sb.append(rnode.getCorridor().getId() + "] ");
            }
            if (i < list.size() - 1) {
                sb.append(" -> ");
            }
        }
        this.tbxRoutes.setText(sb.toString());
    }

    private ArrayList<InfraObject> getSectionRoute() {
        ArrayList<InfraObject> list = new ArrayList<InfraObject>();
        boolean throughExit = false;

        for (int i = 0; i < sectionPointList.size(); i++) {

            RNode rnode = (RNode) sectionPointList.get(i);

            // don't add rnode in CD corridor
            if (rnode.getCorridor().isCD()) {
                continue;
            }

            // add available station
            if (rnode.isAvailableStation()) {
                list.add(rnode);
            } else if (rnode.isExit()) {
                // don't add exit that not connected to other corridor
                if (rnode.getDownStreamNodeToOtherCorridor() == null) {
                    continue;
                }
                if (i < sectionPointList.size() - 1) {
                    RNode nextNode = (RNode) sectionPointList.get(i + 1);
                    if (rnode.getCorridor().equals(nextNode.getCorridor())) {
                        continue;
                    }
                }
                list.add(rnode);
                throughExit = true;
            } else if (rnode.isEntrance()) {
                if (throughExit) {
                    list.add(rnode);
                    throughExit = false;
                }
            } else {
                System.out.println("Not available station : " + rnode.getStationId());
            }
        }
        return list;
    }

    private void loadCorridors() {
        this.cbxCorridors.addItem("Select Corridor");
        for (Corridor c : infra.getCorridors()) {
            this.cbxCorridors.addItem(c);
        }
    }

    private InfraPoint findPointFromMap(RNode rnode) {
        //System.out.println("findPointFromMap 0 - " + rnode.getStationId());
        InfraPoint[] points = mapHelper.getPoints();
        if (points == null) {
            return null;
        }
        //System.out.println("findPointFromMap 1");
        for (InfraPoint p : points) {
            if (p.getInfraObject().equals(rnode)) {
                return p;
            }
        }
        //System.out.println("findPointFromMap 2");
        // add rnodes of corridor (just rnode's downstream side)
        Corridor nextCorridor = rnode.getCorridor();
        addedList.addAll(getDownstreamRNodes(nextCorridor, rnode));
        mapHelper.addShowInfraObjects(addedList.toArray(new InfraObject[addedList.size()]));

        // find again
        return findPointFromMap(rnode);
    }

    private ArrayList<InfraObject> getDownstreamRNodes(Corridor corridor, RNode rnode) {
        ArrayList<InfraObject> list = new ArrayList<InfraObject>();
        boolean findThisNode = false;
        for (RNode rn : corridor.getRnodes()) {
            if (rn.equals(rnode)) {
                findThisNode = true;
            }
            if (findThisNode) {
                if (rn.isStation() && !rn.isAvailableStation()) {
                    continue;
                }
                list.add(rn);
            }
        }
        return list;
    }

    /**
     * Return infra objects list between two rnodes, but except two rnodes
     * (the two rnodes must be in same corridor)
     * @param urn Upstream RNode
     * @param drn Downstream RNode
     * @return InfraPoint list
     */
    private ArrayList<InfraObject> getInfraPointBetweenTwoRNode(RNode urn, RNode drn) {
        // if urn's corridor and drn's corridor are different, return null
        if (!urn.getCorridor().equals(drn.getCorridor())) {
            return null;
        }

        ArrayList<InfraObject> pointList = new ArrayList<InfraObject>();
        RNode rn = urn.getDownstreamNodeToSameCorridor();
        //System.out.println("between1");
        // add rnodes between last clicked marker and exit marker        
        while (rn != null) {
            if (rn.equals(drn)) {
                break;
            }
            pointList.add(rn);
            rn = rn.getDownstreamNodeToSameCorridor();
        }
        //System.out.println("between2");
        // if rn == null
        // it means that drn was not found in the corridor
        // drn might be upstream node of urn
        if (rn == null) {
            return null;
        } else {
            return pointList;
        }
    }
    
    public void setChangeListener(ISectionChanged changeListener) {
        this.changeListener = changeListener;
    }    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        menuItemSectionStart = new javax.swing.JMenuItem();
        menuItemSectionThroughHere = new javax.swing.JMenuItem();
        menuItemSectionEnd = new javax.swing.JMenuItem();
        menuItemProperties = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cbxCorridors = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxRoutes = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        btnSaveSection = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jmKit = new org.jdesktop.swingx.JXMapKit();

        popupMenu.setFont(new java.awt.Font("Verdana", 0, 11));

        menuItemSectionStart.setFont(new java.awt.Font("Verdana", 0, 11));
        menuItemSectionStart.setText("Section start from here");
        menuItemSectionStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSectionStartActionPerformed(evt);
            }
        });
        popupMenu.add(menuItemSectionStart);

        menuItemSectionThroughHere.setFont(new java.awt.Font("Verdana", 0, 11));
        menuItemSectionThroughHere.setText("Through this exit");
        menuItemSectionThroughHere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSectionThroughHereActionPerformed(evt);
            }
        });
        popupMenu.add(menuItemSectionThroughHere);

        menuItemSectionEnd.setFont(new java.awt.Font("Verdana", 0, 11));
        menuItemSectionEnd.setText("Section end to here");
        menuItemSectionEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSectionEndActionPerformed(evt);
            }
        });
        popupMenu.add(menuItemSectionEnd);

        menuItemProperties.setFont(new java.awt.Font("Verdana", 0, 11));
        menuItemProperties.setText("Properties");
        menuItemProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemPropertiesActionPerformed(evt);
            }
        });
        popupMenu.add(menuItemProperties);

        jSplitPane1.setDividerLocation(220);

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel2.setText("Select starting corridor :");

        tbxRoutes.setColumns(20);
        tbxRoutes.setEditable(false);
        tbxRoutes.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxRoutes.setLineWrap(true);
        tbxRoutes.setRows(5);
        jScrollPane1.setViewportView(tbxRoutes);

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel3.setText("Routes :");

        btnSaveSection.setText("Save Section");
        btnSaveSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSectionActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxCorridors, 0, 199, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSaveSection)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxCorridors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSaveSection, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jmKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jSplitPane1.setRightComponent(jmKit);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemPropertiesActionPerformed
        if (selectedPoint != null) {
            InfraInfoDialog rid = new InfraInfoDialog(selectedPoint.getInfraObject(), null, true);
            rid.setLocation(clickedPoint);
            rid.setVisible(true);
        } else {
            System.out.println("Selected Point is null");
        }
    }//GEN-LAST:event_menuItemPropertiesActionPerformed

    private void menuItemSectionStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSectionStartActionPerformed
        sectionPointList.clear();
        lastSectionPoint = this.selectedPoint;
        sectionPointList.add(this.selectedPoint.getInfraObject());
        updateRoutes();
        RNode snode = (RNode) selectedPoint.getInfraObject();
        ArrayList<InfraObject> downNodes = this.getDownstreamRNodes(snode.getCorridor(), snode);
        mapHelper.showInfraObjects(downNodes.toArray(new InfraObject[downNodes.size()]));
        mapHelper.setLinePoints(new InfraObject[]{this.selectedPoint.getInfraObject()});
        //System.out.println("Set start point : " + lastSectionPoint.getName());
    }//GEN-LAST:event_menuItemSectionStartActionPerformed

    private void menuItemSectionThroughHereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSectionThroughHereActionPerformed
        addedList.clear();
        InfraObject obj = this.selectedPoint.getInfraObject();
        Exit ext = (Exit) obj;
        RNode rn = (RNode) lastSectionPoint.getInfraObject();

        ArrayList<InfraObject> betweenPoints = this.getInfraPointBetweenTwoRNode(rn, ext);

        // it means that last point in the section points and clicked exit node are invalid
        // exit node might be upstream node of the last point
        if (betweenPoints == null) {
            System.out.println("Between points are null");
            return;
        }

        InfraPoint entrancePoint = findPointFromMap(ext.getDownStreamNodeToOtherCorridor());
        RNode entrance = (RNode) entrancePoint.getInfraObject();

        // CD corridor
        //  - Find next corridor
        Corridor toConnectCorridor = null;
        RNode toConnectEntrance = null;
        if (entrance.getCorridor().isCD()) {
            boolean foundNode = false;
            for (RNode cdnode : entrance.getCorridor().getRnodes()) {
                if (cdnode.equals(entrance)) {
                    foundNode = true;
                    continue;
                }
                if (foundNode && cdnode.isExit() && cdnode.getTransitionType().isLoop()) {
                    String connectedCorridor = cdnode.getLabel();
                    for (Corridor c : tmo.getInfra().getCorridors()) {
                        if (c.getId().contains(connectedCorridor) && !c.isCD()) {
                            for (RNode cnnode : c.getEntrances()) {
                                if (cnnode.getLabel().equals(entrance.getLabel())) {
                                    double distance = TMO.getDistanceInFeet(cnnode, entrance);
                                    if (distance < 1000) {
                                        toConnectCorridor = c;
                                        toConnectEntrance = cnnode;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (toConnectCorridor != null) {
            addedList.clear();
            System.out.println("Corridor to be connected : " + toConnectCorridor.getId());
            System.out.println("Entrance to be connected : " + toConnectEntrance.getId());
            entrancePoint = this.findPointFromMap(toConnectEntrance);
        }

        // it means that the exit node is not connected to other entrance (something wrong)        
        if (entrancePoint == null) {
            System.out.println("No entrance node next exit node");
            return;
        }

        sectionPointList.addAll(betweenPoints);
        sectionPointList.add(this.selectedPoint.getInfraObject());
        sectionPointList.add(entrancePoint.getInfraObject());

        this.lastSectionPoint = entrancePoint;

        updateRoutes();

        InfraObject[] objs = sectionPointList.toArray(new InfraObject[sectionPointList.size()]);
        mapHelper.showInfraObjects(objs);
        mapHelper.addShowInfraObjects(addedList.toArray(new InfraObject[addedList.size()]));
        mapHelper.setLinePoints(objs);
    }//GEN-LAST:event_menuItemSectionThroughHereActionPerformed

    private void menuItemSectionEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSectionEndActionPerformed
        //System.out.println(0);
        InfraObject obj = this.selectedPoint.getInfraObject();
        RNode rn = (RNode) obj;
        RNode entrance = (RNode) lastSectionPoint.getInfraObject();
        ArrayList<InfraObject> betweenPoints = this.getInfraPointBetweenTwoRNode(entrance, rn);
        if (betweenPoints == null) {
            System.out.println("Between points are null");
            return;
        }
        //System.out.println(1);
        sectionPointList.addAll(betweenPoints);
        sectionPointList.add(obj);
        updateRoutes();
        //System.out.println(2);
        InfraObject[] objs = sectionPointList.toArray(new InfraObject[sectionPointList.size()]);
        mapHelper.showInfraObjects(objs);
        mapHelper.setLinePoints(objs);
        //System.out.println(3);        

    }//GEN-LAST:event_menuItemSectionEndActionPerformed

    private void btnSaveSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSectionActionPerformed
        Vector<RNode> section = new Vector<RNode>();
        for (InfraObject o : this.sectionPointList) {
            section.add((RNode) o);
        }
        SectionSaveDialog ssd = new SectionSaveDialog(null, section);
        ssd.setChangeListener(changeListener);
        ssd.setLocationRelativeTo(this);
        ssd.setVisible(true);
        setSectionPoints();
    }//GEN-LAST:event_btnSaveSectionActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setSectionPoints();
    }//GEN-LAST:event_btnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSaveSection;
    private javax.swing.JComboBox cbxCorridors;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private org.jdesktop.swingx.JXMapKit jmKit;
    private javax.swing.JMenuItem menuItemProperties;
    private javax.swing.JMenuItem menuItemSectionEnd;
    private javax.swing.JMenuItem menuItemSectionStart;
    private javax.swing.JMenuItem menuItemSectionThroughHere;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTextArea tbxRoutes;
    // End of variables declaration//GEN-END:variables

}
