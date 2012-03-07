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

/*
 * RunningDialog.java
 *
 * Created on Apr 21, 2011, 2:31:32 PM
 */
package edu.umn.natsrl.ticas;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Chongmyung Park
 */
public class SplashDialog extends javax.swing.JDialog {

    PrintStream outBack;
    StringOutputStreamOneLine sos = new StringOutputStreamOneLine();

    public SplashDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        initComponents();
        this.setAlwaysOnTop(true);
        
        outBack = System.out;
        System.setOut(new PrintStream(sos));

        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2 - 100);
        
        this.lbVersion.setText(TICASVersion.version);

    }

    public void restoreOutput()
    {
        System.setOut(outBack);
    }
    
    class StringOutputStreamOneLine extends OutputStream {

        StringBuilder message = new StringBuilder();
        String newline = System.getProperty("line.separator");
        
        @Override
        public void write(int b) throws IOException {
            message.append((char) b);
            String[] str = message.toString().split(newline);
            if(str.length>0) lbMsg.setText(str[str.length-1]);
        }
    }

    public void close(int second) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                dispose();
            }
        }, second * 1000);
    }

    @Override
    public void dispose() {        
        super.dispose();
        restoreOutput();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbMsg = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lbVersion = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(java.awt.Color.white);
        setResizable(false);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbMsg.setFont(new java.awt.Font("Verdana", 0, 11));
        lbMsg.setForeground(new java.awt.Color(51, 51, 255));
        lbMsg.setText("loading...");
        jPanel1.add(lbMsg, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 48));
        jLabel1.setText("T I C A S");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 240, 50));

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel2.setText("Traffic Information and Condition Analysis System");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel3.setForeground(new java.awt.Color(130, 157, 168));
        jLabel3.setText("TICAS has been licensed under the GNU General Public License version 2,");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(130, 157, 168));
        jLabel4.setText("which is develped by Northland Advanced Transportation System Research Laboratory and");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 155, -1, -1));

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel5.setForeground(new java.awt.Color(130, 157, 168));
        jLabel5.setText("Software & System Laboratory in Kangwon National University, Korea.");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel6.setForeground(new java.awt.Color(130, 157, 168));
        jLabel6.setText("This project is supported by Minnesota Department of Transportation, US.");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 185, -1, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lbVersion.setFont(new java.awt.Font("Verdana", 0, 10));
        lbVersion.setText("2.x");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(262, Short.MAX_VALUE)
                .addComponent(lbVersion)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lbVersion)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, 290, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbMsg;
    private javax.swing.JLabel lbVersion;
    // End of variables declaration//GEN-END:variables
}
