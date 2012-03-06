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


package edu.umn.natsrl.ticas.plugin.vissimcalibration;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.sfim.SectionInfoDialog;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import java.util.Vector;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class VissimEvaluatorGUI extends javax.swing.JPanel implements ISimEndSignal {

    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    private PropertiesWrapper prop;
    private final String K_CASEFILE = "simulation.casefile";
    private final String K_RANDOMSEED = "simulation.seed";
    private final String configFile = "simulationEvaulator.properties";
    private PluginFrame simFrame;
    private Simulation sim;
    private Date startTime;
    
    /** Creates new form SimulationExampleGUI */
    public VissimEvaluatorGUI(PluginFrame parent) {
        initComponents();
        this.simFrame = parent;
        this.loadSection();
        prop = PropertiesWrapper.load(configFile);
        if(prop != null) {
            this.tbxCaseFile.setText(prop.get(K_CASEFILE));
            this.tbxRandom.setText(prop.get(K_RANDOMSEED));
        } else {
            prop = new PropertiesWrapper();
        }
    }

    private void selectFile() {
        String caseFile = FileHelper.chooseFileToOpen(this.tbxCaseFile.getText(), "Select case file", FileHelper.FileFilterForVISSIM);
        if(caseFile != null && !"".equals(caseFile)) this.tbxCaseFile.setText(caseFile);
    }
    
    private void runSimulation() {
        try {
            this.btnOpenSectionEditor.setEnabled(false);
            this.btnBrowse.setEnabled(false);
            this.cbxSections.setEnabled(false);
            this.tbxCaseFile.setEnabled(false);
            this.btnRun.setEnabled(false);
            this.tbxRandom.setEnabled(false );
            startTime = new Date();
            this.prop.put(K_CASEFILE, this.tbxCaseFile.getText());
            this.prop.put(K_RANDOMSEED, this.tbxRandom.getText());
            this.prop.save(configFile);            
            sim = new Simulation(this.tbxCaseFile.getText(), Integer.parseInt(this.tbxRandom.getText()));
            sim.setSignalListener(this);
            sim.start();                 
        } catch(Exception ex) {
            this.btnRun.setEnabled(true);
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "ERROR : " + ex.getMessage());
        }
    }

    @Override
    public void signalEnd(int code) {
        if(code == 0) {
            this.chkShowVehicles.setEnabled(true);
            return;
        }
        int samples = sim.getSamples();
        if(samples < 10) {
            JOptionPane.showMessageDialog(simFrame, "Too short simulation");
            simFrame.afterSimulation(null, null);
            //this.simFrame.setVisible(false);            
        }

        int duration = samples * 30;        
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.set(Calendar.SECOND, 0);
        Date sTime = c.getTime();
        c.add(Calendar.SECOND, duration);
        Date eTime = c.getTime();
        simFrame.afterSimulation((Section)this.cbxSections.getSelectedItem(), new Period(sTime, eTime, 30));
        //this.simFrame.setVisible(false);
    }
    
    
    private void setVissimVisible(boolean selected) {
        if(sim != null) {
            sim.setVissimVisible(selected);
        }
    }
    
    
    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());

        this.cbxSections.removeAllItems();

        for (Section s : this.sections) {
            this.cbxSections.addItem(s);
        }
    }    
    
    /**
     * Open section editor
     */
    public void openSectionEditor() {
        tmo.openSectionEditor(this.simFrame, true);
        this.loadSection();
    }    
    
    /**
     * Open section information dialog
     */
    private void openSectionInfoDialog() {
        Section section = (Section) this.cbxSections.getSelectedItem();
        if (section == null) {
            return;
        }
        SectionInfoDialog si = new SectionInfoDialog(section, null, true);
        si.setLocationRelativeTo(this);
        si.setVisible(true);
    }       
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBrowse = new javax.swing.JButton();
        tbxCaseFile = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnRun = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        tbxRandom = new javax.swing.JTextField();
        cbxSections = new javax.swing.JComboBox();
        btnSectionInfo = new javax.swing.JButton();
        btnOpenSectionEditor = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        chkShowVehicles = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();

        btnBrowse.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        btnBrowse.setText("Browse");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        tbxCaseFile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxCaseFile.setPreferredSize(new java.awt.Dimension(6, 25));

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel1.setText("Case File");

        btnRun.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnRun.setText("Run Simulation");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel2.setText("Random Number");

        tbxRandom.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRandom.setPreferredSize(new java.awt.Dimension(59, 25));

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        btnSectionInfo.setText("Info");
        btnSectionInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSectionInfoActionPerformed(evt);
            }
        });

        btnOpenSectionEditor.setText("Edit Route");
        btnOpenSectionEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenSectionEditorActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel3.setText("Section");

        chkShowVehicles.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkShowVehicles.setSelected(true);
        chkShowVehicles.setText("show vehicles and road");
        chkShowVehicles.setEnabled(false);
        chkShowVehicles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowVehiclesActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel4.setText("Option");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkShowVehicles)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSectionInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenSectionEditor))
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBrowse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tbxCaseFile, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                    .addComponent(jLabel2)
                    .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(btnRun, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSectionInfo)
                    .addComponent(btnOpenSectionEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBrowse)
                    .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkShowVehicles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRun, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        selectFile();
    }//GEN-LAST:event_btnBrowseActionPerformed

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        runSimulation();
    }//GEN-LAST:event_btnRunActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
        
}//GEN-LAST:event_cbxSectionsActionPerformed

    private void btnSectionInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSectionInfoActionPerformed
        this.openSectionInfoDialog();
}//GEN-LAST:event_btnSectionInfoActionPerformed

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        this.openSectionEditor();
}//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void chkShowVehiclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowVehiclesActionPerformed
        setVissimVisible(this.chkShowVehicles.isSelected());
}//GEN-LAST:event_chkShowVehiclesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnSectionInfo;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JCheckBox chkShowVehicles;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField tbxCaseFile;
    private javax.swing.JTextField tbxRandom;
    // End of variables declaration//GEN-END:variables


}
