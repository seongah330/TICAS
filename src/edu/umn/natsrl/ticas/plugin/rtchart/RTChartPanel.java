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
 * RTChartPanel.java
 *
 * Created on Apr 21, 2011, 1:13:15 PM
 */

package edu.umn.natsrl.ticas.plugin.rtchart;

import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.TMO;
import java.io.File;
import java.util.Vector;
import edu.umn.natsrl.ticas.plugin.rtchart.reader.StationDataReader;
import edu.umn.natsrl.ticas.plugin.rtchart.reader.StationNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Chongmyung Park
 */
public class RTChartPanel extends javax.swing.JPanel {
    
    private Vector<String> streetList;
    private Vector<File> fileVector;
    private boolean loaded = false;
    StationDataReader stationDataReader;

    Timer timer;
    boolean isStarted;
    private int DATA_READ_INTERVAL = 30;  // second
    private int DATA_READ_RETRY_DELAY = 2;    // second

    private LiveTimelineGraph speedGraph;
    private LiveTimelineGraph flowGraph;
    private LiveTimelineGraph volumeGraph;
    private LiveTimelineGraph densityGraph;
    private LiveXYGraph qkGraph;
    private TMO tmo;
    private Vector<Section> sections = new Vector<Section>();
    
    /** Creates new form RTChartPanel */
    public RTChartPanel() {
        
        initComponents();
        
        this.tmo = TMO.getInstance();
        
        //this.setResizable(false);
        this.stationDataReader = new StationDataReader();
        
        this.speedGraph = new SpeedGraph();
        this.flowGraph = new FlowGraph();
        this.densityGraph = new DensityGraph();
        this.volumeGraph = new VolumeGraph();
        this.qkGraph = new QKGraph();
        
        loadSection();        
        
    }

    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());

        this.cbxSection.removeAllItems();

        for (Section s : this.sections) {
            this.cbxSection.addItem(s);
        }
        setInfo();
    }

    private void setInfo() {
        Section section = (Section) this.cbxSection.getSelectedItem();
        if (section == null) {
            this.tbxDesc.setText("");
            this.tbxRoutes.setText("");
            return;
        }

        this.tbxRoutes.setText(section.getRoutes());
        this.tbxDesc.setText(section.getDescription());
    }

    /**
     * On clicked start button
     * Start to collect data and display chart
     */
    private void startPlot() {
        List<String> stationIds = ((Section)this.cbxSection.getSelectedItem()).getStationIds();

        for(int i=0; i<stationIds.size(); i++)
        {            
            System.out.println(stationIds.get(i).replace("S", ""));
            this.stationDataReader.addStationId(stationIds.get(i).replace("S", ""));
        }
        
        this.panelSpeed.add(speedGraph.getGraphPanel());
        this.panelFlow.add(flowGraph.getGraphPanel());
        this.panelDensity.add(densityGraph.getGraphPanel());
        this.panelVolume.add(volumeGraph.getGraphPanel());
        this.panelQK.add(qkGraph.getGraphPanel());

        timer = new Timer();
        timer.schedule(new DataGetter(this), 0, DATA_READ_INTERVAL*1000);
    }

    protected void collectData()
    {
        System.out.println("Collecting Data ....");
        
        if(!this.stationDataReader.collectData()) {
            System.out.printf("Not collected...\n");
            timer.cancel();
            timer.purge();
            timer = new Timer();
            timer.schedule(new DataGetter(this), DATA_READ_RETRY_DELAY*1000, DATA_READ_INTERVAL*1000);
            return;
        }
        ArrayList<String> stationList = stationDataReader.getStationList();
        int stationCount = stationList.size();
        for(int i=0; i<stationCount; i++) {
            String stationId = stationList.get(i);
            StationNode sn = stationDataReader.getLatestStationNode(stationId);
            if(sn == null) {
                System.out.printf("S%s is null\n", stationId);
            }
            this.speedGraph.addData(stationDataReader.getLatestStationNode(stationList.get(i)));
            this.flowGraph.addData(stationDataReader.getLatestStationNode(stationList.get(i)));
            this.densityGraph.addData(stationDataReader.getLatestStationNode(stationList.get(i)));
            this.volumeGraph.addData(stationDataReader.getLatestStationNode(stationList.get(i)));
            this.qkGraph.addData(stationDataReader.getLatestStationNode(stationList.get(i)));
        }
        
    }

    private void setFormEnabled(boolean v)
    {
        this.cbxSection.setEnabled(v);
    }

    private void updatePanels() {
        this.panelSpeed.updateUI();
        this.panelFlow.updateUI();
        this.panelDensity.updateUI();
        this.panelVolume.updateUI();
        this.panelQK.updateUI();
    }

    private class DataGetter extends TimerTask
    {
        RTChartPanel chartMain;

        public DataGetter(RTChartPanel sr) {
            this.chartMain = sr;
        }

        @Override
        public void run() {
            chartMain.collectData();
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

        pnMenu = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        cbxSection = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxDesc = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbxRoutes = new javax.swing.JTextArea();
        tabPane = new javax.swing.JTabbedPane();
        panelSpeed = new javax.swing.JPanel();
        panelFlow = new javax.swing.JPanel();
        panelDensity = new javax.swing.JPanel();
        panelVolume = new javax.swing.JPanel();
        panelQK = new javax.swing.JPanel();

        pnMenu.setBorder(javax.swing.BorderFactory.createTitledBorder("Section"));

        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        tbxDesc.setColumns(20);
        tbxDesc.setEditable(false);
        tbxDesc.setLineWrap(true);
        tbxDesc.setRows(3);
        jScrollPane1.setViewportView(tbxDesc);

        tbxRoutes.setColumns(20);
        tbxRoutes.setEditable(false);
        tbxRoutes.setLineWrap(true);
        tbxRoutes.setRows(5);
        jScrollPane2.setViewportView(tbxRoutes);

        javax.swing.GroupLayout pnMenuLayout = new javax.swing.GroupLayout(pnMenu);
        pnMenu.setLayout(pnMenuLayout);
        pnMenuLayout.setHorizontalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(cbxSection, 0, 216, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnMenuLayout.setVerticalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(7, 7, 7)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPaneStateChanged(evt);
            }
        });

        panelSpeed.setLayout(new java.awt.BorderLayout());
        tabPane.addTab("Speed", panelSpeed);

        panelFlow.setLayout(new java.awt.BorderLayout());
        tabPane.addTab("Flow", panelFlow);

        panelDensity.setLayout(new java.awt.BorderLayout());
        tabPane.addTab("Density", panelDensity);

        panelVolume.setLayout(new java.awt.BorderLayout());
        tabPane.addTab("Volume", panelVolume);

        panelQK.setLayout(new java.awt.BorderLayout());
        tabPane.addTab("Q-K", panelQK);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 891, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 490, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                    .addComponent(pnMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if(this.cbxSection.isEnabled()) {
            setFormEnabled(false);
            this.btnStart.setText("Stop");
            startPlot();
        } else {
            timer.cancel();
            timer.purge();
            timer = null;
            this.speedGraph.reset();
            setFormEnabled(true);
            this.btnStart.setText("Start");
        }
}//GEN-LAST:event_btnStartActionPerformed

    private void tabPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabPaneStateChanged
        //updatePanels();
}//GEN-LAST:event_tabPaneStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JComboBox cbxSection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel panelDensity;
    private javax.swing.JPanel panelFlow;
    private javax.swing.JPanel panelQK;
    private javax.swing.JPanel panelSpeed;
    private javax.swing.JPanel panelVolume;
    private javax.swing.JPanel pnMenu;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextArea tbxDesc;
    private javax.swing.JTextArea tbxRoutes;
    // End of variables declaration//GEN-END:variables

}
