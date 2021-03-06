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
package edu.umn.natsrl.infraviewer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    StringOutputStream sos = new StringOutputStream();

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

    }

    public void releaseOutput()
    {
        System.setOut(outBack);
    }
    
    class StringOutputStream extends OutputStream {

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
        releaseOutput();
        super.dispose();
    }

    public static void writeTextFile(String contents, String fullPathFilename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFilename));
        writer.write(contents);
        writer.flush();
        writer.close();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(java.awt.Color.white);
        setResizable(false);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lbMsg.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        lbMsg.setText("loading...");

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 24));
        jLabel1.setText("Infra Viewer");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel2.setText("Northland Advanced Transportation System Research Laboratory");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(lbMsg))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(3, 3, 3)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbMsg)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lbMsg;
    // End of variables declaration//GEN-END:variables
}
