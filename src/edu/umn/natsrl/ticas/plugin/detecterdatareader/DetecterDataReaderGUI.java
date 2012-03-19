/*
 * MeterReaderGUI.java
 *
 * Created on Jan 11, 2012, 10:22:43 AM
 */
package edu.umn.natsrl.ticas.plugin.detecterdatareader;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.traveltimeIndexer.DateChecker;
import edu.umn.natsrl.util.FileHelper;
import java.awt.Desktop;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.Number;

/**
 *
 * @author soobin.Jeon
 */
public class DetecterDataReaderGUI extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();

    /** Creates new form MeterReaderGUI */
    public DetecterDataReaderGUI(PluginFrame _frame) {
        initComponents();
        _frame.setSize(310,550);
        init();
    }

    private void init() {

        //date checker
        nATSRLCalendar1.setDateChecker(DateChecker.getInstance());

        //interval
        for (Interval i : Interval.values()) {
            this.cbxInterval.addItem(i);
        }

        //duration
        this.cbxduration.addItem("Select");
        for (int i = 1; i <= 32; i++) {
            this.cbxduration.addItem(i);
        }

    }

    /**
     * Change times status according to selected duration
     */
    private void selectDuration() {
        if (this.cbxduration.getSelectedIndex() == 0) {
            this.cbxEndTimeInter.setEnabled(true);
            this.cbxEndMinInter.setEnabled(true);
        } else {
            this.cbxEndTimeInter.setEnabled(false);
            this.cbxEndMinInter.setEnabled(false);
        }
    }
    private void readData(Detector detector, Vector<Period> peroids) throws Exception{
        boolean readDensity = this.chkDensity.isSelected();
        boolean readSpeed = this.chkSpeed.isSelected();
        boolean readFlow = this.chkFlow.isSelected();
        boolean readScan = this.cbxScan.isSelected();
        boolean readOccu = this.cbxOccupancy.isSelected();
        boolean readVol = this.cbxVol.isSelected();
        
        String filepath = FileHelper.getNumberedFileName("D"+detector.getDetectorId()+".xls");
        WritableWorkbook workbook = Workbook.createWorkbook(new File(filepath));
        WritableSheet sheet = workbook.createSheet(String.valueOf(detector.getDetectorId()), 0);
        int colldx = 0;
        System.out.println("detector Length("+detector.getLabel()+") : "+detector.getFieldLength());
        addData(sheet,colldx++,new String[]{"",""},peroids.get(0).getTimeline());
        
        for(Period p : peroids){
            System.out.println(detector.getLabel() + " ");
            detector.loadData(p, false);
            String day = String.format("%02d",p.start_date);
            
//            System.out.println("test : " + detector.isAuxiliary() + ", " + detector.isAbandoned() + ", " + detector.isMissing() + ", " + detector.getCategory());
            if(readDensity)
                addData(sheet, colldx++, new String[]{day,"Density"},detector.getDensity());
            if(readFlow)
                addData(sheet, colldx++, new String[]{day,"Flow"},detector.getFlow());
            if(readSpeed)
                addData(sheet, colldx++, new String[]{day,"Speed"},detector.getSpeed());
            if(readScan)
                addData(sheet, colldx++, new String[]{day,"Scan"},detector.getScan());
            if(readOccu)
                addData(sheet, colldx++, new String[]{day,"Occupancy"},detector.getOccupancy());
            if(readVol)
                addData(sheet, colldx++, new String[]{day,"Volume"},detector.getVolume());
            
        }
        workbook.write();
        workbook.close();
        
        Desktop.getDesktop().open(new File(filepath));
    }
    
    
    private void readData(){
        Calendar[] selectedDates = this.nATSRLCalendar1.getSelectedDates();
        Calendar c1, c2;
        
        Vector<Period> periods = new Vector<Period>();
        int interval = ((Interval) this.cbxInterval.getSelectedItem()).second;
        int start_hour = Integer.parseInt(this.cbxStartTimeInter.getSelectedItem().toString());
        int start_min = Integer.parseInt(this.cbxStartMinInter.getSelectedItem().toString());
        int end_hour = Integer.parseInt(this.cbxEndTimeInter.getSelectedItem().toString());
        int end_min = Integer.parseInt(this.cbxEndMinInter.getSelectedItem().toString());

        
        for (Calendar date : selectedDates) {
            c1 = (Calendar) date.clone();
            c2 = (Calendar) date.clone();

            c1.set(Calendar.HOUR, start_hour);
            c1.set(Calendar.MINUTE, start_min);
            c1.set(Calendar.SECOND, 0);

            c2.set(Calendar.HOUR, end_hour);
            c2.set(Calendar.MINUTE, end_min);
            c2.set(Calendar.SECOND, 0);

            
            if (this.cbxduration.getSelectedIndex() > 0) {
                c2.set(Calendar.HOUR, start_hour);
                c2.set(Calendar.MINUTE, start_min);
                c2.add(Calendar.HOUR, (Integer) this.cbxduration.getSelectedItem());
            } else {
                c2.set(Calendar.HOUR, end_hour);
                c2.set(Calendar.MINUTE, end_min);
            }            
            
            periods.add(new Period(c1.getTime(), c2.getTime(), interval));
        }
        
        if(!this.tbxDetector.getText().isEmpty()){
            String detectorid = this.tbxDetector.getText().toUpperCase();
            Detector detector = tmo.getInfra().getDetector(detectorid);
            try {
                this.readData(detector,periods);
                //JOptionPane.showMessageDialog(this, "Complete!");
            } catch (Exception ex) {
                Logger.getLogger(DetecterDataReaderGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            JOptionPane.showMessageDialog(this, "Insert Detector Name");
        }
        
            
            
                    
        
                    
    }
    private void addData(WritableSheet sheet, int column, String[] labels, double[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void addData(WritableSheet sheet, int column, String[] labels, Vector<Double> data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            
            for(int r=0; r<data.size(); r++)
            {
                sheet.addCell(new Number(column, row++, data.get(r)) {});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    private void addData(WritableSheet sheet, int column, String[] labels, String[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Label(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nATSRLCalendar1 = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tbxDetector = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        chkDensity = new javax.swing.JCheckBox();
        chkSpeed = new javax.swing.JCheckBox();
        chkFlow = new javax.swing.JCheckBox();
        btRead = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxInterval = new javax.swing.JComboBox();
        cbxEndMinInter = new javax.swing.JComboBox();
        cbxEndTimeInter = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        cbxStartTimeInter = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        cbxStartMinInter = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        cbxduration = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        cbxScan = new javax.swing.JCheckBox();
        cbxOccupancy = new javax.swing.JCheckBox();
        cbxVol = new javax.swing.JCheckBox();

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel1.setText("Dates");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel2.setText("Detecter");

        tbxDetector.setFont(new java.awt.Font("Verdana", 0, 12));
        tbxDetector.setPreferredSize(new java.awt.Dimension(60, 22));

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel3.setText("Data Type");

        chkDensity.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDensity.setSelected(true);
        chkDensity.setText("Density");

        chkSpeed.setFont(new java.awt.Font("Verdana", 0, 12));
        chkSpeed.setSelected(true);
        chkSpeed.setText("Speed");

        chkFlow.setFont(new java.awt.Font("Verdana", 0, 12));
        chkFlow.setSelected(true);
        chkFlow.setText("Flow");

        btRead.setFont(new java.awt.Font("Verdana", 0, 18));
        btRead.setText("Read");
        btRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btReadActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel5.setText("Start Time");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 29, -1, -1));

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel4.setText("Time Interval");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 1, -1, -1));

        cbxInterval.setPreferredSize(new java.awt.Dimension(156, 20));
        jPanel1.add(cbxInterval, new org.netbeans.lib.awtextra.AbsoluteConstraints(109, 0, -1, -1));

        cbxEndMinInter.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndMinInter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        jPanel1.add(cbxEndMinInter, new org.netbeans.lib.awtextra.AbsoluteConstraints(164, 54, -1, -1));

        cbxEndTimeInter.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndTimeInter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndTimeInter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEndTimeInterActionPerformed(evt);
            }
        });
        jPanel1.add(cbxEndTimeInter, new org.netbeans.lib.awtextra.AbsoluteConstraints(108, 54, -1, -1));

        jLabel6.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel6.setText(":");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 29, -1, -1));

        cbxStartTimeInter.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartTimeInter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartTimeInter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStartTimeInterActionPerformed(evt);
            }
        });
        jPanel1.add(cbxStartTimeInter, new org.netbeans.lib.awtextra.AbsoluteConstraints(108, 26, -1, -1));

        jLabel7.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel7.setText("End Time");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 57, -1, -1));

        cbxStartMinInter.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartMinInter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        jPanel1.add(cbxStartMinInter, new org.netbeans.lib.awtextra.AbsoluteConstraints(164, 26, -1, -1));

        jLabel9.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel9.setText("or for");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(61, 82, -1, -1));

        cbxduration.setPreferredSize(new java.awt.Dimension(150, 20));
        cbxduration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxdurationActionPerformed(evt);
            }
        });
        jPanel1.add(cbxduration, new org.netbeans.lib.awtextra.AbsoluteConstraints(108, 82, 100, -1));

        jLabel10.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel10.setText("hour");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, 85, -1, -1));

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel8.setText(":");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(155, 57, -1, -1));

        jLabel11.setFont(new java.awt.Font("Verdana", 0, 11));
        jLabel11.setText("e.g. D100");

        cbxScan.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxScan.setText("Scan");

        cbxOccupancy.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxOccupancy.setText("Occupancy");

        cbxVol.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxVol.setText("Volume");
        cbxVol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxVolActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(nATSRLCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(chkDensity)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkSpeed)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkFlow))
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tbxDetector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel11))
                            .addComponent(btRead, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbxScan)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxOccupancy)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxVol)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nATSRLCalendar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tbxDetector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkDensity)
                    .addComponent(chkSpeed)
                    .addComponent(chkFlow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxScan)
                    .addComponent(cbxOccupancy)
                    .addComponent(cbxVol))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(btRead, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void cbxdurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxdurationActionPerformed
// TODO add your handling code here:
    selectDuration();
}//GEN-LAST:event_cbxdurationActionPerformed

private void btReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btReadActionPerformed
// TODO add your handling code here:
    try {
        readData();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}//GEN-LAST:event_btReadActionPerformed

private void cbxStartTimeInterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxStartTimeInterActionPerformed
// TODO add your handling code here:
    int slt1 = this.cbxStartTimeInter.getSelectedIndex();
    int slt2 = this.cbxEndTimeInter.getSelectedIndex();
    if (slt1 > slt2) {
        this.cbxEndTimeInter.setSelectedIndex(slt1);
    }
}//GEN-LAST:event_cbxStartTimeInterActionPerformed

private void cbxEndTimeInterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxEndTimeInterActionPerformed
// TODO add your handling code here:
    int slt1 = this.cbxStartTimeInter.getSelectedIndex();
    int slt2 = this.cbxEndTimeInter.getSelectedIndex();
    if (slt1 > slt2) {
        this.cbxStartTimeInter.setSelectedIndex(slt2);
    }
}//GEN-LAST:event_cbxEndTimeInterActionPerformed

private void cbxVolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVolActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_cbxVolActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btRead;
    private javax.swing.JComboBox cbxEndMinInter;
    private javax.swing.JComboBox cbxEndTimeInter;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JCheckBox cbxOccupancy;
    private javax.swing.JCheckBox cbxScan;
    private javax.swing.JComboBox cbxStartMinInter;
    private javax.swing.JComboBox cbxStartTimeInter;
    private javax.swing.JCheckBox cbxVol;
    private javax.swing.JComboBox cbxduration;
    private javax.swing.JCheckBox chkDensity;
    private javax.swing.JCheckBox chkFlow;
    private javax.swing.JCheckBox chkSpeed;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar nATSRLCalendar1;
    private javax.swing.JTextField tbxDetector;
    // End of variables declaration//GEN-END:variables
}
