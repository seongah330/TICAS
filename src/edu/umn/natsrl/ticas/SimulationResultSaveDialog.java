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

package edu.umn.natsrl.ticas;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.simobjects.RandomSeed;
import edu.umn.natsrl.infra.simobjects.SimulationSeveralResult;
import java.awt.Frame;
import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class SimulationResultSaveDialog extends javax.swing.JDialog {

    private Section section;
    private Period period;
    public boolean isSaved = false;
    
    private RandomSeed SimRandom = RandomSeed.getInstance();
             
    public SimulationResultSaveDialog(Frame parent, Section section, Period period) {
        super(parent, true);
        initComponents();        
        this.section = section;
        this.period = period;               
    }
    
    private void saveResult()
    {
        String name = this.tbxSectionName.getText();
        String desc = this.tbxSectionDesc.getText();
        if(!checkName(name)) return;

        try {
            System.out.println("SAVE : period=" + period);
            System.out.println("SAVE : section="+section.getRoutes());
            section.loadData(period, true);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        SimulationResult sr = null;
        if(this.SimRandom.hasSimulationKey())
            sr = new SimulationResult(name, desc, section, period,false,SimulationSeveralResult.getKey(this.SimRandom.getName(), this.SimRandom.getSimulationKey()),this.SimRandom.getLength());
        else
            sr = new SimulationResult(name, desc, section, period);
        
        try {
            sr.save();
            this.isSaved = true;
            this.dispose();
        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(rootPane, "Fail to save result");
            this.dispose();
        }
    }


    
    private boolean checkName(String name)
    {
        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Fail : Result name is required");
            return false;
        }
        if(name.length() > 128) {
            JOptionPane.showMessageDialog(rootPane, "Fail : Result name is long. (128 characters limit)");
            return false;
        }
        String eName = SimulationResult.getCacheFileName(name);
        if(eName == null) {
            JOptionPane.showMessageDialog(rootPane, "Fail : MD5 algorithm does not exist!!");
            return false;            
        }
        String filename = SimulationResult.SAVE_PROP_DIR + File.separator + eName;
        File targetFile = new File(filename);
        if(targetFile.exists())
        {
            int res = JOptionPane.showConfirmDialog(rootPane, "Result name already exists. Overwrite the section?");
            if(res > 0) return false;
        }
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tbxSectionName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxSectionDesc = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Save Section");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel2.setText("Name");

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel3.setText("Description");

        tbxSectionDesc.setColumns(20);
        tbxSectionDesc.setRows(5);
        jScrollPane1.setViewportView(tbxSectionDesc);

        jButton1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(tbxSectionName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbxSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveResult();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea tbxSectionDesc;
    private javax.swing.JTextField tbxSectionName;
    // End of variables declaration//GEN-END:variables

}
