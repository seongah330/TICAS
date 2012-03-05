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
/*
 * VideoCheckerGUI.java
 *
 * Created on Feb 14, 2012, 3:10:01 PM
 */
package edu.umn.natsrl.ticas.plugin.VideoChecker;

import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.FileHelper;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author soobin Jeon
 */
public class VideoCheckerGUI extends javax.swing.JPanel {
    boolean IsStart = false;
    Calendar Startdate;
    ArrayList<Double> inputdata;
    ArrayList<Double> outputdata;
    double Ind;
    double Oud;
    PrintStream outBack;
    PrintStream errBack;
    
    
    boolean LoopCheck = false;
    /** Creates new form VideoCheckerGUI */
    public VideoCheckerGUI(PluginFrame _frame) {
        initComponents();
        _frame.setSize(600,500);
        this.addKeyListener(new MyKeyListener(this));        
    }
    
    void init(){
        Startdate = Calendar.getInstance();
        inputdata = new ArrayList<Double>();
        outputdata = new ArrayList<Double>();
        Ind = 0;
        Oud = 0;
        LoopCheck = true;
        this.txtLog.setText("");
        StringOutputStream sos = new StringOutputStream();
        outBack = System.out;
        errBack = System.err;
        System.setOut(new PrintStream(sos));
        System.setErr(new PrintStream(sos));
        
    }
    
    void Sec30Input(double in, double out){
        inputdata.add(in);
        outputdata.add(out);
        Ind = 0;
        Oud = 0;
    }
    
    public void ButtonPress(int btype){
        EvaluationStart();
        if(btype == 1)
            Ind++;
        else
            Oud++;
        WriteStreamButtonData();
    }
    public void EvaluationStart(){
        if(!IsStart){
            init();
            IsStart = true;
            Timer t = new Timer();
            t.schedule(new EvaluationTask(this), 0);
        }
    }
    public class Sec30Task extends TimerTask{
        VideoCheckerGUI gui;
        Calendar date;
        public Sec30Task(VideoCheckerGUI vgui, Calendar _date){
            this.gui = vgui;
            this.date = _date;
        }
        
        @Override
        public void run(){
            double in = this.gui.Ind;
            double out = this.gui.Oud;
            this.gui.Sec30Input(in, out);
            this.gui.WriteStreamButtonData();
            System.out.println("Time : "+new Timestamp(date.getTimeInMillis()-1000));
            System.out.println("input = "+in+", output = "+out);
            System.out.println("---------------------------------------------------");
        }
    }
    public class EvaluationTask extends TimerTask{
        VideoCheckerGUI gui;
        Calendar date;
        public EvaluationTask(VideoCheckerGUI vgui){
            this.gui = vgui;
            date = Calendar.getInstance();
            
            date.set(2000, 01, 01, Integer.parseInt(gui.cbxTime.getSelectedItem().toString()), Integer.parseInt(gui.cbxMin.getSelectedItem().toString()), Integer.parseInt(gui.cbxSec.getSelectedItem().toString()));
            gui.Startdate.set(2000, 01, 01, Integer.parseInt(gui.cbxTime.getSelectedItem().toString()), Integer.parseInt(gui.cbxMin.getSelectedItem().toString()), Integer.parseInt(gui.cbxSec.getSelectedItem().toString()));
            gui.txtCurrentTime.setText(String.valueOf(new Timestamp(date.getTimeInMillis())));
        }
        
        @Override
        public void run(){
            try {
                double currentTime = 0;
                while(this.gui.LoopCheck){
                    gui.txtTime.setText(String.valueOf(new Timestamp(date.getTimeInMillis())));
                    if(currentTime == 30000){
                        currentTime = 0;
                        Timer t = new Timer();
                        t.schedule(new Sec30Task(gui,date), 0);
                    }

                    date.setTimeInMillis(date.getTimeInMillis()+1000);
                    currentTime += 1000;

                    Thread.sleep(1000);
                    
                }
                try {
                    this.gui.CreateDataFile();
                } catch (Exception ex) {
                    Logger.getLogger(VideoCheckerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.gui.dispose();
            } catch (InterruptedException ex) {
                Logger.getLogger(VideoCheckerGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void WriteStreamButtonData(){
        this.txtInput.setText(String.valueOf(this.Ind));
        this.txtOutput.setText(String.valueOf(this.Oud));
    }
    
    public void CreateDataFile() throws Exception{
        String FileName = FileHelper.getNumberedFileName("VideoCheck.xls");
        System.out.println("Making File.."+FileName);
        WritableWorkbook workbook = Workbook.createWorkbook(new File(FileName));
        WritableSheet sheet = workbook.createSheet("videoData", 1);
        
        sheet.addCell(new Label(0, 0, "Time"));
        sheet.addCell(new Label(1, 0, "input"));
        sheet.addCell(new Label(2, 0, "output"));
        //Write Time
        for(int i=0;i<inputdata.size();i++){
            this.Startdate.setTimeInMillis(Startdate.getTimeInMillis()+30000);
            String timeString = String.format("%02d:%02d:%02d", Startdate.get(Calendar.HOUR),Startdate.get(Calendar.MINUTE),Startdate.get(Calendar.SECOND));
            sheet.addCell(new Label(0, i+1, timeString));
        }
        
        for(int i=0;i<inputdata.size();i++){
            sheet.addCell(new Number(1, i+1, inputdata.get(i)));
            sheet.addCell(new Number(2, i+1, outputdata.get(i)));
        }
        
        System.out.println("Make Workbook..");
        workbook.write();
        workbook.close();
        
    }
    
    public void dispose()
    {
        System.setOut(outBack);
        System.setErr(errBack);
//        FileHelper.writeTextFile(tbxLog.getText(), "log.txt");
    }
    
    class StringOutputStream extends OutputStream 
    {
        StringBuilder message = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            message.append((char) b);
            txtLog.setText(message.toString());
            txtLog.setCaretPosition(txtLog.getDocument().getLength());            
        }
    }   
   
    class MyKeyListener implements KeyListener{
        VideoCheckerGUI pan;
        public MyKeyListener(VideoCheckerGUI gui){
            this.pan = gui;
        }
        @Override
        public void keyTyped(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void keyPressed(KeyEvent e) {
            char keycode = e.getKeyChar();
        if(keycode == '1'){pan.ButtonPress(1);}
        else if(keycode == '2'){pan.ButtonPress(2);}
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet.");
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

        txtTime = new javax.swing.JTextField();
        btInput = new javax.swing.JButton();
        btOutput = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        txtInput = new javax.swing.JTextField();
        txtOutput = new javax.swing.JTextField();
        btReset = new javax.swing.JButton();
        cbxTime = new javax.swing.JComboBox();
        cbxMin = new javax.swing.JComboBox();
        cbxSec = new javax.swing.JComboBox();
        txtCurrentTime = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(500, 500));

        txtTime.setPreferredSize(new java.awt.Dimension(200, 20));

        btInput.setText("input");
        btInput.setPreferredSize(new java.awt.Dimension(80, 23));
        btInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInputActionPerformed(evt);
            }
        });

        btOutput.setText("output");
        btOutput.setPreferredSize(new java.awt.Dimension(80, 23));
        btOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOutputActionPerformed(evt);
            }
        });

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        txtInput.setPreferredSize(new java.awt.Dimension(120, 20));

        txtOutput.setPreferredSize(new java.awt.Dimension(120, 20));

        btReset.setText("Stop&Evaluation");
        btReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btResetActionPerformed(evt);
            }
        });

        cbxTime.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));

        cbxMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        cbxSec.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        txtCurrentTime.setPreferredSize(new java.awt.Dimension(200, 20));

        jLabel1.setText("Start Time");

        jLabel2.setText("Current Time");

        jLabel3.setText("Set Timer");

        jLabel4.setText(":");

        jLabel5.setText(":");

        jLabel6.setText("Input Data info");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btReset, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .addComponent(txtCurrentTime, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(txtTime, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxSec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cbxTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(cbxMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(cbxSec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addComponent(jLabel1)
                        .addGap(5, 5, 5)
                        .addComponent(txtCurrentTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addGap(3, 3, 3)
                        .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addComponent(btReset, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void btInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInputActionPerformed
// TODO add your handling code here:
    ButtonPress(1);
    this.requestFocus();
}//GEN-LAST:event_btInputActionPerformed

private void btOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOutputActionPerformed
// TODO add your handling code here:
    ButtonPress(2);
    this.requestFocus();
}//GEN-LAST:event_btOutputActionPerformed

private void btResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btResetActionPerformed
// TODO add your handling code here:
    LoopCheck = false;
    IsStart = false;
}//GEN-LAST:event_btResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btInput;
    private javax.swing.JButton btOutput;
    private javax.swing.JButton btReset;
    private javax.swing.JComboBox cbxMin;
    private javax.swing.JComboBox cbxSec;
    private javax.swing.JComboBox cbxTime;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtCurrentTime;
    private javax.swing.JTextField txtInput;
    private javax.swing.JTextArea txtLog;
    private javax.swing.JTextField txtOutput;
    private javax.swing.JTextField txtTime;
    // End of variables declaration//GEN-END:variables
}
