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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.util.StringOutputStream;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.views.ChartPanel;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEChartView extends javax.swing.JFrame {

    private Vector<SRTEResultSection> result;
    private SRTEResultSection currentSection;
    private SRTEResult currentStationResult;
    
//    StringOutputStream debugWriter;
    
    enum DataType{
        Speed_Origin("speed"),
        Speed_Smooth("speed"),
        Speed_Quan("speed"),
        Density_Origin("Density"),
        Density_Smooth("Density"),
        Density_Quan("Density");
        
        String name;
        DataType(String name){
            this.name = name;
        }
    }
    /**
     * Creates new form SRTEChartView
     */
    public SRTEChartView() {
        initComponents();
    }
    public SRTEChartView(Vector<SRTEResultSection> res){
        this();
//        debugWriter = new StringOutputStream(this.tbxDebug);
        this.result = res;
        updateComboBox();
        updateDataType();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanelMenu = new javax.swing.JPanel();
        cbxEventList = new javax.swing.JComboBox();
        cbxDataType = new javax.swing.JComboBox();
        cbxStation = new javax.swing.JComboBox();
        bntDuplicate = new javax.swing.JButton();
        lInformation = new javax.swing.JLabel();
        lTimeInformation = new javax.swing.JLabel();
        cbxShowDefault = new javax.swing.JCheckBox();
        cbxShowAPoint = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxDebug = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        cbxPoint = new javax.swing.JCheckBox();
        cbxAccPoint = new javax.swing.JCheckBox();
        tbxACCPointThreshHold = new javax.swing.JTextField();
        PanelChart = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setFocusable(false);
        setPreferredSize(new java.awt.Dimension(1100, 850));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        PanelMenu.setPreferredSize(new java.awt.Dimension(970, 142));

        cbxEventList.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxEventList.setPreferredSize(new java.awt.Dimension(800, 20));
        cbxEventList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEventListActionPerformed(evt);
            }
        });

        cbxDataType.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxDataType.setPreferredSize(new java.awt.Dimension(200, 20));
        cbxDataType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDataTypeActionPerformed(evt);
            }
        });

        cbxStation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStation.setPreferredSize(new java.awt.Dimension(200, 20));
        cbxStation.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                cbxStationMouseWheelMoved(evt);
            }
        });
        cbxStation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStationActionPerformed(evt);
            }
        });

        bntDuplicate.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        bntDuplicate.setText("Duplicate");
        bntDuplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bntDuplicateActionPerformed(evt);
            }
        });

        lInformation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lInformation.setText("Point Information");

        lTimeInformation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        lTimeInformation.setText("Time Information");

        cbxShowDefault.setSelected(true);
        cbxShowDefault.setText("Show Default");

        cbxShowAPoint.setText("Show Acc Point");

        tbxDebug.setColumns(20);
        tbxDebug.setRows(5);
        jScrollPane1.setViewportView(tbxDebug);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        cbxPoint.setSelected(true);
        cbxPoint.setText("Point List");
        cbxPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxPointActionPerformed(evt);
            }
        });

        cbxAccPoint.setText("Show Acc Point");
        cbxAccPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxAccPointActionPerformed(evt);
            }
        });

        tbxACCPointThreshHold.setText("1");
        tbxACCPointThreshHold.setName("1");
        tbxACCPointThreshHold.setPreferredSize(new java.awt.Dimension(50, 20));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxPoint)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cbxAccPoint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxACCPointThreshHold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 117, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxPoint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxAccPoint)
                    .addComponent(tbxACCPointThreshHold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PanelMenuLayout = new javax.swing.GroupLayout(PanelMenu);
        PanelMenu.setLayout(PanelMenuLayout);
        PanelMenuLayout.setHorizontalGroup(
            PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelMenuLayout.createSequentialGroup()
                .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(PanelMenuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PanelMenuLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelMenuLayout.createSequentialGroup()
                                .addComponent(cbxStation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(cbxDataType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(PanelMenuLayout.createSequentialGroup()
                                .addComponent(cbxEventList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 169, Short.MAX_VALUE)
                                .addComponent(bntDuplicate))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PanelMenuLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PanelMenuLayout.createSequentialGroup()
                                .addComponent(cbxShowDefault)
                                .addGap(0, 0, 0)
                                .addComponent(cbxShowAPoint))
                            .addComponent(lInformation)
                            .addComponent(lTimeInformation))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PanelMenuLayout.setVerticalGroup(
            PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelMenuLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxEventList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bntDuplicate))
                .addGap(6, 6, 6)
                .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxStation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxDataType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lInformation)
                .addGap(6, 6, 6)
                .addComponent(lTimeInformation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxShowDefault)
                    .addComponent(cbxShowAPoint))
                .addContainerGap())
        );

        PanelChart.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        javax.swing.GroupLayout PanelChartLayout = new javax.swing.GroupLayout(PanelChart);
        PanelChart.setLayout(PanelChartLayout);
        PanelChartLayout.setHorizontalGroup(
            PanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        PanelChartLayout.setVerticalGroup(
            PanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 448, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PanelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 1080, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EventEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EventEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EventEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EventEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
//                new EventEditor().setVisible(true);
            }
        });
    }
    
    private void cbxEventListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxEventListActionPerformed
        // TODO add your handling code here:
        updateStation((SRTEResultSection)this.cbxEventList.getSelectedItem());
    }//GEN-LAST:event_cbxEventListActionPerformed

    private void cbxStationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxStationActionPerformed
        // TODO add your handling code here:
        updateCurrentStation((SRTEResult)this.cbxStation.getSelectedItem());
    }//GEN-LAST:event_cbxStationActionPerformed

    private void cbxDataTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDataTypeActionPerformed
        // TODO add your handling code here:
        updateGraph((DataType)this.cbxDataType.getSelectedItem());
    }//GEN-LAST:event_cbxDataTypeActionPerformed

    private void bntDuplicateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bntDuplicateActionPerformed
        // TODO add your handling code here:
        SRTEChartView view = new SRTEChartView(this.result);
//        view.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        view.setVisible(true);
    }//GEN-LAST:event_bntDuplicateActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
//        JOptionPane.showMessageDialog(null, "Resize");
        updateGraph((DataType)this.cbxDataType.getSelectedItem());
    }//GEN-LAST:event_formComponentResized

    private void cbxPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxPointActionPerformed
        // TODO add your handling code here:
        UpdatePointList();
    }//GEN-LAST:event_cbxPointActionPerformed

    private void cbxStationMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_cbxStationMouseWheelMoved
        // TODO add your handling code here:
        if(evt.getWheelRotation() > 0){ //up
            this.cbxStation.setSelectedIndex(this.cbxStation.getSelectedIndex()+1);
        }else{ //down
            this.cbxStation.setSelectedIndex(this.cbxStation.getSelectedIndex()-1);
        }
    }//GEN-LAST:event_cbxStationMouseWheelMoved

    private void cbxAccPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxAccPointActionPerformed
        // TODO add your handling code here:
        UpdateAccPointList();
    }//GEN-LAST:event_cbxAccPointActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelChart;
    private javax.swing.JPanel PanelMenu;
    private javax.swing.JButton bntDuplicate;
    private javax.swing.JCheckBox cbxAccPoint;
    private javax.swing.JComboBox cbxDataType;
    private javax.swing.JComboBox cbxEventList;
    private javax.swing.JCheckBox cbxPoint;
    private javax.swing.JCheckBox cbxShowAPoint;
    private javax.swing.JCheckBox cbxShowDefault;
    private javax.swing.JComboBox cbxStation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lInformation;
    private javax.swing.JLabel lTimeInformation;
    private javax.swing.JTextField tbxACCPointThreshHold;
    private javax.swing.JTextArea tbxDebug;
    // End of variables declaration//GEN-END:variables

    private void updateComboBox() {
        this.cbxEventList.removeAllItems();
        for(SRTEResultSection re : this.result){
            this.cbxEventList.addItem(re);
        }
        updateStation((SRTEResultSection)this.cbxEventList.getSelectedItem());
    }

    private void updateStation(SRTEResultSection selectedItem) {
        currentSection = selectedItem;
        this.cbxStation.removeAllItems();
        for(SRTEResult re : currentSection.getResultData()){
            this.cbxStation.addItem(re);
        }
        updateCurrentStation((SRTEResult)this.cbxStation.getSelectedItem());
    }

    /**
     * All info Update
     * @param srteResult 
     */
    private void updateCurrentStation(SRTEResult srteResult) {
        currentStationResult = srteResult;
        updateGraph((DataType)this.cbxDataType.getSelectedItem());
        UpdatePointList();
        UpdateAccPointList();
    }
    
    private void updateGraph(DataType dataType) {
        if(currentStationResult == null)
            return;
        double[] data = null;
        if(dataType == DataType.Speed_Origin){
            data = currentStationResult.u_Avg_origin;
        }else if(dataType == DataType.Speed_Smooth){
            data = currentStationResult.u_Avg_smoothed;
        }else if(dataType == DataType.Speed_Quan){
            data = currentStationResult.u_Avg_quant;
        }else if(dataType == DataType.Density_Origin){
            data = currentStationResult.k_origin;
        }else if(dataType == DataType.Density_Smooth){
            data = currentStationResult.k_smoothed;
        }else if(dataType == DataType.Density_Quan){
            data = currentStationResult.k_quant;
        }
        
        if(data == null)
            return;
        
        HashMap<Integer,Boolean> point = new HashMap<Integer,Boolean>();
        point.put(currentStationResult.getcurrentPoint().srst,true);
        point.put(currentStationResult.getcurrentPoint().lst,true);
        point.put(currentStationResult.getcurrentPoint().rst,true);
        for(int srt :currentStationResult.getcurrentPoint().srt)
            point.put(srt,true);
        
        HashMap<Integer,Boolean> timestep = new HashMap<Integer,Boolean>();
        HashMap<Integer,Boolean> bare = new HashMap<Integer,Boolean>();
        timestep.put(currentStationResult.getStartTimeStep(),true);
        timestep.put(currentStationResult.getEndTimeStep(),true);
        bare.put(currentStationResult.getBareLaneTimeStep(),true);
        
        PanelChart.removeAll();
        SRTEChartLine cl = new SRTEChartLine();
        cl.setSpeedData(point, timestep,bare, data, dataType);
        ChartPanel cpn = new ChartPanel(cl.getChart());
        cpn.setSize(PanelChart.getSize());
        PanelChart.add(cpn);
        PanelChart.getParent().validate();
        
        updateInformation();
    }
    
    private void updateDataType() {
        this.cbxDataType.removeAllItems();
        for(DataType t : DataType.values()){
            this.cbxDataType.addItem(t);
        }
        updateGraph((DataType)this.cbxDataType.getSelectedItem());
    }
    private void updateInformation() {
        updatePointInformation(currentStationResult.getcurrentPoint());
        updateTimeInformation(currentStationResult);
    }
    private void updatePointInformation(ResultPoint currentPoint) {
        String str = null;
        str = "Point Information   -SRST:"+currentPoint.srst+"    -LST:"+currentPoint.lst+"    -RST:"+currentPoint.rst;
        int cnt = 1;
        for(int srt : currentPoint.srt){
            str += "    -SRT("+cnt+"):"+srt;
            cnt++;
        }
        this.lInformation.setText(str);
    }
    private void updateTimeInformation(SRTEResult cr) {
        String str = "Time Information   -StartTime:"+cr.getStartTimetoString()+"("+cr.getStartTimeStep()+")"
                +"    -EndTime:"+cr.getEndTimetoString()+"("+cr.getEndTimeStep()+")"
                +"    -BareLane:"+cr.getBareLaneTimetoString()+"("+cr.getBareLaneTimeStep()+")";
        this.lTimeInformation.setText(str);
    }
    
    private void UpdatePointList() {
        if(this.cbxPoint.isSelected()){
            if(currentStationResult == null)
                return;
            ClearDebugList();
            ClearAnotherCBX(this.cbxPoint);
            int cnt = 1;
            for(ResultPoint rp: currentStationResult.getPoint()){
                AddDebugList("["+cnt+"]"+rp.toString());
                cnt++;
            }
        }
        else
            this.ClearDebugList();
    }
    
    private void UpdateAccPointList() {
        if(this.cbxAccPoint.isSelected()){
            if(currentStationResult == null)
                return;
            String thr = this.tbxACCPointThreshHold.getText();
            double thd = 0;
            try{
                thd = Double.parseDouble(thr);
            }catch(Exception e){
                return;
            }
            
            ClearAnotherCBX(this.cbxAccPoint);
            ClearDebugList();
            for(SRTEResult.ResultRCRAccPoint rp: currentStationResult.getRCRAccPointList()){
                if(rp.data > thd)
                    AddDebugList("["+rp.point+"]"+rp.data);
            }
        }
        else
            this.ClearDebugList();
    }
    
    private void ClearDebugList() {
        this.tbxDebug.setText("");
    }
    
    private void AddDebugList(String toString) {
        String data = this.tbxDebug.getText();
        if(!data.equals(""))
            data += "\n";
        this.tbxDebug.setText(data+toString);
    }
    
    private void ClearAnotherCBX(JCheckBox cbxAccPoint) {
        if(!this.cbxAccPoint.equals(cbxAccPoint))
            this.cbxAccPoint.setSelected(false);
        if(!this.cbxPoint.equals(cbxAccPoint))
            this.cbxPoint.setSelected(false);
    }
}
