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
package edu.umn.natsrl.infra.section;

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.section.SectionInfraCreatorDialog.InfraDialogChanged;
import edu.umn.natsrl.map.CoordinateConversion;
import edu.umn.natsrl.map.InfraPoint;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import edu.umn.natsrl.ticas.RunningDialog;
import edu.umn.natsrl.ticas.SplashDialog;
import edu.umn.natsrl.util.FileHelper;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SectionInfraCreatorPanel extends javax.swing.JPanel implements InfraDialogChanged{
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
    
    /**
     * Creates new form SectionInfraCreatorPanel
     */
    public SectionInfraCreatorPanel() {
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
                    setDialog(e.getPoint(),null);
                }else{
                    setDialog(e.getPoint(),ip);
                }
                
                mapHelper.repaint();
            }

//            public void mouseReleased(MouseEvent e) {
//                InfraPoint ip = mapHelper.getClickedMarker(e.getPoint());
//
//                if (ip != null && e.isPopupTrigger()) {
//
//                    InfraObject o = ip.getInfraObject();
//                    RNode rnode = (RNode) o;
//                    menuItemSectionStart.setEnabled(true);
//                    menuItemSectionThroughHere.setEnabled(true);
//                    menuItemSectionEnd.setEnabled(true);
//
//                    if (o.getInfraType().isStation()) {
//                        menuItemSectionThroughHere.setEnabled(false);
//                        if (sectionPointList.isEmpty()) {
//                            menuItemSectionEnd.setEnabled(false);
//                        } else {
//                            menuItemSectionStart.setEnabled(false);
//                        }
//                    } else if (o.getInfraType().isExit()) {
//                        if (sectionPointList.isEmpty()) {
//                            menuItemSectionThroughHere.setEnabled(false);
//                        }
//                        RNode nextNode = rnode.getDownStreamNodeToOtherCorridor();
//                        if (nextNode == null || nextNode.getCorridor().equals(rnode.getCorridor())) {
//                            menuItemSectionThroughHere.setEnabled(false);
//                        }
//                        menuItemSectionStart.setEnabled(false);
//                        menuItemSectionEnd.setEnabled(false);
//                    } else {
//                        menuItemSectionStart.setEnabled(false);
//                        menuItemSectionThroughHere.setEnabled(false);
//                        menuItemSectionEnd.setEnabled(false);
//                    }
//
//                    selectedPoint = ip;
//                    clickedPoint = e.getLocationOnScreen();
//                    popupMenu.show(jmKit.getMainMap(), e.getX(), e.getY());
//                }
//            }
        });

        loadCorridors();

        this.cbxCorridors.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {
                setSectionPoints();
            }
        });

    }
    private void setDialog(Point point, InfraPoint ip) {
        InfraObject io = ip == null ? null : ip.getInfraObject();
        if (point != null) {
                GeoPosition g = mapHelper.getGeoPosition(point);
                System.out.println("!!!");
                System.out.println("g : L:"+g.getLatitude()+", Log:"+g.getLongitude());
                
                CoordinateConversion converter = new CoordinateConversion();
                String en = converter.latLon2UTM(g.getLatitude(), g.getLongitude());
                String[] EN = en.split(" ");
                int easting = 0;
                int northing = 0;
                if(EN.length > 3){
                    easting = Integer.parseInt(EN[2]);
                    northing = Integer.parseInt(EN[3]);
                    System.out.println("E : L:"+easting+", N:"+northing);
                }
                Corridor cor = (Corridor)this.cbxCorridors.getSelectedItem();
                SectionInfraCreatorDialog rid = new SectionInfraCreatorDialog(io,cor,easting,northing, null, true);
                rid.setSignalListener(this);
                rid.setLocation(point);
                rid.setVisible(true);
        } else {
            System.out.println("Selected Point is null");
        }
    
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
//        updateRoutes();
        this.selectedCorridorIndex = cbxCorridors.getSelectedIndex();
    }
    
    private void loadCorridors() {
        this.cbxCorridors.addItem("Select Corridor");
        for (Corridor c : infra.getCorridors()) {
            this.cbxCorridors.addItem(c);
        }
    }
    
    public void setChangeListener(ISectionChanged changeListener) {
        this.changeListener = changeListener;
    } 
    
    private void DeleteAllSection() {
        System.out.println("Delete All Sections...");
        FileHelper.deleteDirectory(new File("section"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cbxCorridors = new javax.swing.JComboBox();
        btnSaveInfra = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jmKit = new org.jdesktop.swingx.JXMapKit();

        setPreferredSize(new java.awt.Dimension(931, 593));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Infromation"));

        btnSaveInfra.setText("SAVE INFRA");
        btnSaveInfra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveInfraActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxCorridors, 0, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSaveInfra)
                .addGap(623, 623, 623))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxCorridors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveInfra))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jmKit, javax.swing.GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jmKit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveInfraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveInfraActionPerformed
        // TODO add your handling code here:
//        JOptionPane.showMessageDialog(rootPane, "Empty : RNODE ");
        int reply = JOptionPane.showConfirmDialog(null, "Warning:: All Section will be deleted.\nDo you want to execute it?", "Save Infra Data",JOptionPane.YES_NO_OPTION);
        if(reply == JOptionPane.YES_OPTION){
            final RunningDialog rd = new RunningDialog(null, true);
            rd.setLocationRelativeTo(this);
            rd.showLog();
            Timer t = new Timer();
            t.schedule(new TimerTask(){
                @Override
                public void run() {
                    DeleteAllSection();
                    tmo.getInfra().saveCache();
                    rd.dispose();
                    changeListener.sectionChanged(SectionChangedStatus.CREATED);
                }
            },10);
            rd.setTimer(t);
            rd.setVisible(true);
        }
//        rd.dispose();
    }//GEN-LAST:event_btnSaveInfraActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSaveInfra;
    private javax.swing.JComboBox cbxCorridors;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private org.jdesktop.swingx.JXMapKit jmKit;
    // End of variables declaration//GEN-END:variables

    @Override
    public void infrachanged() {
        System.out.println("ReLoad Corridor");
        loadCorridors();
        setSectionPoints();
    }
}
