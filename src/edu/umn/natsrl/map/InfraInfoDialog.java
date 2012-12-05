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
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Chongmyung Park
 */
public class InfraInfoDialog extends javax.swing.JDialog {

    TMO tmo = TMO.getInstance();
    
    /** Creates new form RNodeInfoDialog */
    public InfraInfoDialog(InfraObject obj, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();        
        setEscape();        
        this.setLocationRelativeTo(parent);
        if (obj.getInfraType().isRnode()) {
            setRNode((RNode) obj);
        } else if (obj.getInfraType().isDetector()) {
            setDetector((Detector)obj);
        } else if(obj.getInfraType().isMeter()) {
            setMeter((RampMeter)obj);
        } else if(obj.getInfraType().isDMSImpl()){
            setDMSImpl((DMSImpl)obj);
        }
    }

    private void setRNode(RNode rnode) {
        DefaultTableModel tm = (DefaultTableModel) this.tblProperty.getModel();
        String id = rnode.getId();
        if (rnode.isStation()) {
            id = id + "(" + rnode.getStationId() + ")";
        }
        id = id + " - " + rnode.getLabel();

        this.lbId.setText(id);

        tm.addRow(new Object[]{"id", rnode.getId()});
        tm.addRow(new Object[]{"type", rnode.getInfraType()});
        tm.addRow(new Object[]{"transition", rnode.getTransitionType()});

        if (rnode.isStation()) {
            tm.addRow(new Object[]{"station id", rnode.getStationId()});
            tm.addRow(new Object[]{"speed limit", rnode.getSpeedLimit()});
            int dcnt = 1;
            for(Detector d : rnode.getDetectors()){
                String dextend = "";
                if(d.isAbandoned())
                    dextend += "X,";
                if(d.isAuxiliary())
                    dextend += "Aux,";
                if(d.isHov())
                    dextend += "Hov,";
                if(d.isWavetronics())
                    dextend += "Wave";
                if(!dextend.equals("")){
                    dextend = "("+dextend+")";
                }
                tm.addRow(new Object[]{"detector "+dcnt,d.getOriginId()+dextend});
                dcnt++;
            }
        }
        tm.addRow(new Object[]{"corridor", rnode.getCorridor()});
        tm.addRow(new Object[]{"label", rnode.getLabel()});
        tm.addRow(new Object[]{"lanes", rnode.getLanes()});
        tm.addRow(new Object[]{"easting", rnode.getEasting()});
        tm.addRow(new Object[]{"northing", rnode.getNorthing()});
        tm.addRow(new Object[]{"shift", rnode.getShift()});

        if (rnode.isStation()) {
            String[] detNames = rnode.getDetectorNames();
            if(detNames != null) {
                String dets = join(rnode.getDetectorNames(), ", ");
                tm.addRow(new Object[]{"detectors", dets});
            }
        } else {
            for (Detector d : rnode.getDetectors()) {
                tm.addRow(new Object[]{d.getLaneType().toString(), d.getId()});
            }
        }

        if (rnode.isEntrance()) {
            RampMeter meter = tmo.getMeter((Entrance) rnode);
            if(meter != null) {
                tm.addRow(new Object[]{"ramp meter", meter.getId()});
                tm.addRow(new Object[]{"ramp meter storage", meter.getStorage()});
            }
        }
        
        RNode dNode = rnode.getDownstreamNodeToSameCorridor();
        if(dNode != null) {
            String dNodeId = dNode.getId() + " - " + dNode.toString();
            if(dNode.isStation() && !dNode.isAvailableStation()) {
                dNodeId = dNode.getId() + "(Station)";
            }
            tm.addRow(new Object[]{"downstream (same corridor) ", dNodeId});
        }
        
        dNode = rnode.getDownStreamNodeToOtherCorridor();
        if(dNode != null) {
            String dNodeId = dNode.getId() + " - " + dNode.toString();
            if(dNode.isStation() && !dNode.isAvailableStation()) {
                dNodeId = dNode.getId() + "(Station)";
            }
            tm.addRow(new Object[]{"downstream (other corridor) ", dNodeId});
        }        
        
    }
    
    private void setDMSImpl(DMSImpl dmsImpl) {
        DefaultTableModel tm = (DefaultTableModel) this.tblProperty.getModel();
        this.lbId.setText("DMS - "+dmsImpl.getId());
        
        tm.addRow(new Object[]{"id", dmsImpl.getId()});
        tm.addRow(new Object[]{"type", dmsImpl.getInfraType().toString()});
        tm.addRow(new Object[]{"easting", dmsImpl.getEasting()});
        tm.addRow(new Object[]{"northing", dmsImpl.getNorthing()});
        tm.addRow(new Object[]{"DistanceFromFirstStation", dmsImpl.getDistanceFromFirstStation()});
        
        int cnt = 1;
        for(DMS d : dmsImpl.getDMSList()){
            tm.addRow(new Object[]{"DMS_"+cnt, d.getId()});
        }
    }

    
    private void setDetector(Detector d) {
        DefaultTableModel tm = (DefaultTableModel) this.tblProperty.getModel();
        this.lbId.setText(d.getId() + " - " + d.getLabel());
        
        tm.addRow(new Object[]{"id", d.getId()});
        tm.addRow(new Object[]{"type", d.getLaneType().toString()});
        tm.addRow(new Object[]{"label", d.getLabel()});
        tm.addRow(new Object[]{"lane", d.getLane()});        
        tm.addRow(new Object[]{"field length", d.getFieldLength()});
        
        RNode rnode = d.getRNode();
        if(rnode != null) {
            if(rnode.isStation()) tm.addRow(new Object[]{"r_node", rnode.getId() + "(" + rnode.getStationId()+")"});       
            else  tm.addRow(new Object[]{"r_node id", rnode.getId() + "(" + rnode.getInfraType()+")"});
            
            tm.addRow(new Object[]{"r_node easting", rnode.getEasting()});
            tm.addRow(new Object[]{"r_node northing", rnode.getNorthing()});
            
            if(!rnode.isStation()) 
            {
                for (Detector det : rnode.getDetectors()) {
                    tm.addRow(new Object[]{"r_node " + det.getLaneType().toString(), det.getId()});
                }
            }            
        }
    }    
    
    private void setMeter(RampMeter m) {
        DefaultTableModel tm = (DefaultTableModel) this.tblProperty.getModel();
        this.lbId.setText(m.getId() + " - " + m.getLabel());
        
        tm.addRow(new Object[]{"id", m.getId()});
        tm.addRow(new Object[]{"label", m.getLabel()});
        tm.addRow(new Object[]{"storage", m.getStorage()});
        
        Entrance rnode = tmo.getEntrance(m);
        if(rnode != null) {
            if(rnode.isStation()) tm.addRow(new Object[]{"r_node", rnode.getId() + "(" + rnode.getStationId()+")"});       
            else  tm.addRow(new Object[]{"r_node id", rnode.getId() + "(" + rnode.getInfraType()+")"});
            
            tm.addRow(new Object[]{"r_node easting", rnode.getEasting()});
            tm.addRow(new Object[]{"r_node northing", rnode.getNorthing()});
            
            if(!rnode.isStation()) 
            {
                for (Detector det : rnode.getDetectors()) {
                    tm.addRow(new Object[]{"r_node " + det.getLaneType().toString(), det.getId()});
                }
            }            
        }        
    }

    

    private void setEscape() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);        
    }
    
    private String join(String[] strings, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProperty = new javax.swing.JTable();
        lbId = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Infra Information");
        setResizable(false);
        setUndecorated(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        tblProperty.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ));
        jScrollPane1.setViewportView(tblProperty);

        lbId.setText("RNODE ID");

        btnClose.setFont(new java.awt.Font("Verdana", 0, 12));
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbId, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbId)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed

    }//GEN-LAST:event_formKeyPressed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbId;
    private javax.swing.JTable tblProperty;
    // End of variables declaration//GEN-END:variables

    

}
