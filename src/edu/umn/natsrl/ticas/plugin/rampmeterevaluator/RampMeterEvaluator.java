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
package edu.umn.natsrl.ticas.plugin.rampmeterevaluator;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.ticas.DateChecker;
import edu.umn.natsrl.ticas.Simulation.SimulationUtil;
import edu.umn.natsrl.ticas.SimulationResult;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class RampMeterEvaluator extends javax.swing.JPanel {
    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    
    private PrintStream backupOut;
    private PrintStream backupErr;
    
    private boolean isStart = false;
    PluginFrame sframe;
    /**
     * Creates new form RampMeterEvaluator
     */
    public RampMeterEvaluator(PluginFrame simFrame) {
        initComponents();
        init();
        sframe = simFrame;
        simFrame.setSize(900, 580);
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
        cbxInterval = new javax.swing.JComboBox();
        cbxshour = new javax.swing.JComboBox();
        cbxsmin = new javax.swing.JComboBox();
        cbxehour = new javax.swing.JComboBox();
        cbxemin = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jPanel3 = new javax.swing.JPanel();
        cbxeMode = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        cbxsimulationresult = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        cbxUseSimulationData = new javax.swing.JCheckBox();
        btnStart = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        cbxSection = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(800, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N
        jPanel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        cbxInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxInterval.setPreferredSize(new java.awt.Dimension(100, 25));

        cbxshour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxshour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxshour.setPreferredSize(new java.awt.Dimension(75, 25));

        cbxsmin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxsmin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxsmin.setPreferredSize(new java.awt.Dimension(75, 25));

        cbxehour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxehour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxehour.setPreferredSize(new java.awt.Dimension(75, 25));

        cbxemin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxemin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxemin.setPreferredSize(new java.awt.Dimension(75, 25));

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel1.setText("Time Interval");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel2.setText("Start");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel3.setText("End");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbxshour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxsmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbxehour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxemin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxshour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxsmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxehour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxemin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Calendar", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Extraction Type", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        cbxeMode.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxeMode.setPreferredSize(new java.awt.Dimension(200, 22));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxeMode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxeMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Extract Data from Simulation Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 10), java.awt.Color.black)); // NOI18N

        cbxsimulationresult.setPreferredSize(new java.awt.Dimension(400, 20));

        jLabel28.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel28.setText("Simulation Output Files");

        cbxUseSimulationData.setText("Extract Data from Simulation Results");
        cbxUseSimulationData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxUseSimulationDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(cbxUseSimulationData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel28))
                    .addComponent(cbxsimulationresult, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbxUseSimulationData)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel28)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxsimulationresult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnStart.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnStart.setLabel("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Section for Real Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        cbxSection.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxSection.setPreferredSize(new java.awt.Dimension(200, 22));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(cbxSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // TODO add your handling code here:
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                Process();
            }
        },10);
    }//GEN-LAST:event_btnStartActionPerformed

    private void cbxUseSimulationDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxUseSimulationDataActionPerformed
        // TODO add your handling code here:
        setVisibleOption(this.cbxUseSimulationData.isSelected());
        if(this.cbxUseSimulationData.isSelected()){
            new Timer().schedule(new TimerTask(){
                @Override
                public void run() {
                    loadSimulationResults();
                }
            },5);
        }
    }//GEN-LAST:event_cbxUseSimulationDataActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JComboBox cbxSection;
    private javax.swing.JCheckBox cbxUseSimulationData;
    private javax.swing.JComboBox cbxeMode;
    private javax.swing.JComboBox cbxehour;
    private javax.swing.JComboBox cbxemin;
    private javax.swing.JComboBox cbxshour;
    private javax.swing.JComboBox cbxsimulationresult;
    private javax.swing.JComboBox cbxsmin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    // End of variables declaration//GEN-END:variables

    private void init() {
        
        DateChecker dc = DateChecker.getInstance();
        this.natsrlCalendar.setDateChecker(dc);
        
        // interval
        for (Interval i : Interval.values()) {
            this.cbxInterval.addItem(i);
        }
        //Mode
        for(RampMeterEvaluatorMode m : RampMeterEvaluatorMode.values()){
            this.cbxeMode.addItem(m);
        }
        
        loadSection();
        setVisibleOption(false);
    }
    
    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        if (sm == null) {
            return;
        }
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());
        this.cbxSection.removeAllItems();
        this.cbxSection.addItem("Select the route");
        for (Section s : this.sections) {
            this.cbxSection.addItem(s);
        }
    }
    
    private void Process(){
        ButtonCheck();
        redirectOutput();
        
        Section section = null;
        ArrayList<Period> period_forAnal = null;
        ArrayList<Period> period_forRamp = null;
        SimulationResult sr = null;
        if(this.cbxUseSimulationData.isSelected()){
            sr = (SimulationResult)this.cbxsimulationresult.getSelectedItem();
            if(sr.IsListData()){
                System.out.println("Error : Selected Simulation Data is not Metering Simulation Data..");
                ButtonCheck();
                return;
            }
            section = sr.getSection();
            
            int start_hour=-1, start_min = -1;
            try {
                start_hour = Integer.parseInt(this.cbxshour.getSelectedItem().toString());//Integer.parseInt(sh);
                start_min = Integer.parseInt(this.cbxsmin.getSelectedItem().toString());//Integer.parseInt(sm);
            } catch(Exception ex) {}
//            Period p = sr.getPeriod();
            period_forAnal = new ArrayList();
            period_forRamp = new ArrayList();
            period_forAnal.add(sr.getPeriod(start_hour,start_min));
            period_forRamp.add(sr.getPeriod(start_hour,start_min));
        }else{
            Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
            if(selectedDates.length < 1){
                JOptionPane.showMessageDialog(sframe, "Select \'date and time\'");
                ButtonCheck();
                return;
            }
            try{
                section = (Section)this.cbxSection.getSelectedItem();
            }catch(Exception e){
                System.out.println("Error : Select Section..");
                ButtonCheck();
                return;
            }
            period_forAnal = getPeriod();
            period_forRamp = getPeriod();
        }
        
        int interval = ((Interval) this.cbxInterval.getSelectedItem()).second;
        RampMeterEvaluatorMode mode = (RampMeterEvaluatorMode)this.cbxeMode.getSelectedItem();
        RampMeterCalculator ramp = new RampMeterCalculator(section,period_forRamp,mode,interval,sr);
        RampMeterCalculator analysis = new RampMeterCalculator(section,period_forAnal,mode,3600,sr);
        RampMeterWriter writer = new RampMeterWriter(mode);
//        ramp.Process();
        
        printState(section,mode,interval,getPeriod());
        try{
            ArrayList<RampMeterResult> results = ramp.Process();
            
            if(mode.isDayModeEachDays())
                writer.WriteResult(results);
            else{
                ArrayList<RampMeterResult> anals = analysis.Process();
                writer.WriteResult(results,anals);
            }
            restoreOutput();
            ButtonCheck();
        }catch(Exception e){
            e.printStackTrace();
            restoreOutput();
            ButtonCheck();
        }
    }
    
    /**
     * load simulation results from local disk
     * set data to table
     */
    private void loadSimulationResults() {
        this.cbxsimulationresult.setEnabled(false);
        if(this.cbxsimulationresult != null)
            this.cbxsimulationresult.removeAllItems();
        cbxsimulationresult.addItem("Loading Datas.....");
        
        ArrayList<SimulationResult> res = SimulationUtil.loadSimulationResults();
        
        if(this.cbxsimulationresult != null)
            this.cbxsimulationresult.removeAllItems();
        
        
        for(SimulationResult s : res)
        {
            if(s != null) {
                cbxsimulationresult.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
        this.cbxsimulationresult.setEnabled(true);
    }

    private ArrayList<Period> getPeriod() {
        ArrayList<Period> periods = new ArrayList<Period>();
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        Calendar c1, c2;
        Period period;
        int start_hour = Integer.parseInt(this.cbxshour.getSelectedItem().toString());
        int start_min = Integer.parseInt(this.cbxsmin.getSelectedItem().toString());
        int end_hour = Integer.parseInt(this.cbxehour.getSelectedItem().toString());
        int end_min = Integer.parseInt(this.cbxemin.getSelectedItem().toString());

        for (Calendar date : selectedDates) {
            c1 = (Calendar) date.clone();
            c2 = (Calendar) date.clone();

            c1.set(Calendar.HOUR, start_hour);
            c1.set(Calendar.MINUTE, start_min);

            c2.set(Calendar.HOUR, end_hour);
            c2.set(Calendar.MINUTE, end_min);

            period = new Period(c1.getTime(), c2.getTime(), 30);
            periods.add(period);
        }
        return periods;
    }

    private void redirectOutput() {
        backupOut = System.out;
        backupErr = System.err;
        jTextArea1.setText("");
        // redirect System.out and System.err to log textbox
        StringOutputStream sos = new StringOutputStream(this.jTextArea1);
        System.setOut(new PrintStream(sos));
        System.setErr(new PrintStream(sos));
    }
    
    /**
     * Resotre output
     */
    public void restoreOutput() {
        System.setOut(backupOut);
        System.setErr(backupErr);
    }

    private void printState(Section section, RampMeterEvaluatorMode mode, int interval, ArrayList<Period> period) {
        System.out.println("Section : " +section.getName());
        System.out.println("Routes  : " +section.getRoutes());
        System.out.println("mode    : " +mode.toString());
        System.out.println("Interval: " +interval+"sec");
        System.out.println("Processing....");
    }

    private void ButtonCheck() {
        if(!isStart){
            isStart = true;
            this.btnStart.setEnabled(false);
        }
        else{
            isStart = false;
            this.btnStart.setEnabled(true);
        }
    }   

    private void setVisibleOption(boolean selected) {
        natsrlCalendar.setEnabled(!selected);
        this.cbxSection.setEnabled(!selected);
        this.cbxsimulationresult.setEnabled(selected);
        this.cbxehour.setEnabled(!selected);
        this.cbxemin.setEnabled(!selected);
    }
    
    /**
     * String Output Stream class for output redirection
     */
    public class StringOutputStream extends OutputStream {
        JTextArea logText;

        public StringOutputStream(JTextArea logText) {
            this.logText = logText;
        }
        
        @Override
        public void write(int b) throws IOException {
            updateLog(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            updateLog(new String(b, off, len));
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        private synchronized void updateLog(final String text) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    logText.append(text);
                }
            });
        }
    }
}
