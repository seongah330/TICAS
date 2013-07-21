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
package edu.umn.natsrl.ticas.plugin.simulation.basicmetering;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.simobjects.SimMeter;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import edu.umn.natsrl.ticas.Simulation.EntranceState;
import edu.umn.natsrl.ticas.Simulation.SimInterval;
import edu.umn.natsrl.ticas.Simulation.Simulation.ISimEndSignal;
import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import edu.umn.natsrl.ticas.Simulation.SimulationUtil;
import edu.umn.natsrl.ticas.Simulation.StationState;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.ExcelAdapter;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.ComError;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class BasicMeteringGUI extends javax.swing.JPanel implements ISimEndSignal{
    
    private PluginFrame simFrame;
    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    private Section currentSection;
    private BasicMeterSimulation simulation;
    
    /**
     * Map
     */
    private int initZoom = 10;
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;    
    private MapHelper simMapHelper;
    
    private Date startTime;
    
    private boolean isInit = false;
    
    private PrintStream backupOut;
    private PrintStream backupErr;
    
    /**
     * Creates new form BasicMeteringGUI
     */
    public BasicMeteringGUI(PluginFrame parent) {
        initComponents();
        this.simFrame = parent;
        this.loadSection();
        
        //VissimVersion
        for(VISSIMVersion v : VISSIMVersion.values()){
            this.cbxvissimVersion.addItem(v);
        }
        
        //Meter Method
        for(BasicMeterMethod bm : BasicMeterMethod.values()){
            this.cbxSOption.addItem(bm);
        }
        
        SimulationConfig.loadConfig();
        this.tbxCaseFile.setText(SimulationConfig.CASE_FILE);
        DecimalFormat df = new DecimalFormat();
        this.tbxRandom.setText(df.format(SimulationConfig.RANDOM_SEED));
        
        ExcelAdapter exap = new ExcelAdapter(this.mtable);
        
        /**
         * Load Map
         */
        this.simJmKit.setTileFactory(TMCProvider.getTileFactory());
        this.simJmKit.setAddressLocation(new GeoPosition(this.initLatitude, this.initLongitude));
        this.simJmKit.setZoom(this.initZoom);
        simMapHelper = new MapHelper(this.simJmKit);
        simMapHelper.setFrame(simFrame); 
        
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                if(simulation != null)
                    simulation.simulationStop();
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
        
        /**
         * load current section
         */
        currentSection = (Section)this.cbxSections.getSelectedItem();
        showMap();
        isLoaded(true);
        
        isInit = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPanel = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbxSections = new javax.swing.JComboBox();
        tbxCaseFile = new javax.swing.JTextField();
        tbxRandom = new javax.swing.JTextField();
        cbxvissimVersion = new javax.swing.JComboBox();
        btnOpenSectionEditor = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        simJmKit = new org.jdesktop.swingx.JXMapKit();
        btnLoadCaseFile = new javax.swing.JButton();
        cbxSOption = new javax.swing.JComboBox();
        btnStartSimulation = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mtable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        chkShowVehicles = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        meterstate = new javax.swing.JTextArea();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        simstate = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(1070, 750));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Simulation Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel3.setText("Select Real Freeway Section");

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel1.setText("Select Simulation Case File (Needs to Match with Real Section)");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel2.setText("Random Number");

        jLabel6.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel6.setText("VISSIM Version");

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxSections.setPreferredSize(new java.awt.Dimension(200, 22));
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        tbxCaseFile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxCaseFile.setPreferredSize(new java.awt.Dimension(250, 22));

        tbxRandom.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRandom.setPreferredSize(new java.awt.Dimension(100, 22));

        cbxvissimVersion.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        btnOpenSectionEditor.setText("Edit Route");
        btnOpenSectionEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenSectionEditorActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jButton3.setText("Browser");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxvissimVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnOpenSectionEditor))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOpenSectionEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxvissimVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        simJmKit.setMiniMapVisible(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(simJmKit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(simJmKit, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnLoadCaseFile.setText("Load CaseFile");
        btnLoadCaseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadCaseFileActionPerformed(evt);
            }
        });

        cbxSOption.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxSOption.setPreferredSize(new java.awt.Dimension(390, 22));

        btnStartSimulation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnStartSimulation.setText("Start Simulation");
        btnStartSimulation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartSimulationActionPerformed(evt);
            }
        });

        jLabel4.setText("R 2) Density Based - Ri,t+1 = Ri,t + beta * (Kcr - Kd,t)");

        jLabel5.setText("R 3) Queue Density Based - Ri,t+1 = Ri,t + gamma * (Kq,t - Kq,d)");

        jLabel7.setText("Cd = Capacity at D,   Kcr = Critical(or Desired) Density at D,    Kq,d = Desired Queue Density");

        jLabel8.setText("R 1) Flow Rate Based - if Kd,t < Kcr, then Ri,t = cd - Qu,t,   Else Ri,t = alpha * Qu,t");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel7))
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel8))
                .addContainerGap(158, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addContainerGap())
        );

        mtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(mtable);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbxSOption, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnStartSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLoadCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnLoadCaseFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxSOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnStartSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabPanel.addTab("Simulation setting", jPanel4);

        jPanel5.setPreferredSize(new java.awt.Dimension(1045, 740));

        chkShowVehicles.setText("Show Vehicles and road");
        chkShowVehicles.setEnabled(false);
        chkShowVehicles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowVehiclesActionPerformed(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Meter State", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        meterstate.setColumns(20);
        meterstate.setRows(5);
        jScrollPane2.setViewportView(meterstate);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Simulation State", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        simstate.setColumns(20);
        simstate.setRows(5);
        jScrollPane3.setViewportView(simstate);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(chkShowVehicles)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkShowVehicles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabPanel.addTab("Simulation Console", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        // TODO add your handling code here:
        this.openSectionEditor();
    }//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
        // TODO add your handling code here:
        if(!isInit)
            return;
        System.out.println(this.cbxSections.getItemCount());
        currentSection = (Section)this.cbxSections.getSelectedItem();
        showMap();
        isLoaded(true);
    }//GEN-LAST:event_cbxSectionsActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        String path = ".";
        String prevPath = this.tbxCaseFile.getText();
        if (!prevPath.isEmpty()) {
            path = new File(prevPath).getAbsolutePath();
        }
        String caseFile = FileHelper.chooseFileToOpen(prevPath, "Select VISSIM Case File", FileHelper.FileFilterForVISSIM);
        if (caseFile != null) {
            this.tbxCaseFile.setText(caseFile);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnLoadCaseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadCaseFileActionPerformed
        // TODO add your handling code here:
        this.loadDatas();
    }//GEN-LAST:event_btnLoadCaseFileActionPerformed

    private void btnStartSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartSimulationActionPerformed
        // TODO add your handling code here:
        StartSimulation();
    }//GEN-LAST:event_btnStartSimulationActionPerformed

    private void chkShowVehiclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowVehiclesActionPerformed
        // TODO add your handling code here:
        setVissimVisible(this.chkShowVehicles.isSelected());
    }//GEN-LAST:event_chkShowVehiclesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLoadCaseFile;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnStartSimulation;
    private javax.swing.JComboBox cbxSOption;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JComboBox cbxvissimVersion;
    private javax.swing.JCheckBox chkShowVehicles;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea meterstate;
    private javax.swing.JTable mtable;
    private org.jdesktop.swingx.JXMapKit simJmKit;
    private javax.swing.JTextArea simstate;
    private javax.swing.JTabbedPane tabPanel;
    private javax.swing.JTextField tbxCaseFile;
    private javax.swing.JTextField tbxRandom;
    // End of variables declaration//GEN-END:variables

    @Override
    public void signalEnd(int code) {
        if(code == -1) {
            this.chkShowVehicles.setEnabled(true);
            this.chkShowVehicles.setSelected(false);            
            setVissimVisible(false);
            return;
        }
        
        ComError ce = ComError.getErrorbyID(code);
        if(!ce.isCorrect()){
            JOptionPane.showMessageDialog(simFrame, ce.toString());
            this.isStartSimulation(false);
            return;
        }
        
        int samples = simulation.getSamples();
        if(samples < 5) {
            JOptionPane.showMessageDialog(simFrame, "Too short simulation");
            simFrame.afterSimulation(null, null);
            this.simFrame.setVisible(false);            
        }
        
        int duration = samples * 30;        

        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.set(Calendar.SECOND, 0);
        Date sTime = c.getTime();
        c.add(Calendar.SECOND, duration);
        Date eTime = c.getTime();
        simFrame.afterSimulation((Section)this.cbxSections.getSelectedItem(), new Period(sTime, eTime, 30));
        SimulationUtil.SaveSimulation((Section)this.cbxSections.getSelectedItem(),new Period(sTime, eTime, 30),simFrame);
        System.out.println("Restore output redirection ... ");
        this.restoreOutput();
    }
    
    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        this.sections.clear();
        sm.loadSections();
        if(sm.getSections() == null){
            return;
        }
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

    private void showMap() {
        if(currentSection == null || currentSection.getRNodes().isEmpty())
            return;
        this.simMapHelper.showSection(currentSection);
        this.simMapHelper.setCenter(currentSection.getRNodes().get(0));
    }
    
    public void loadDatas(){
        SimulationConfig.CASE_FILE = this.tbxCaseFile.getText();
        SimulationConfig.RANDOM_SEED = Integer.parseInt(this.tbxRandom.getText());
        SimulationConfig.saveConfig();
        
        Section s = (Section)this.cbxSections.getSelectedItem();
        VISSIMVersion v = (VISSIMVersion)this.cbxvissimVersion.getSelectedItem();
//        Interval simIntv = (Interval)this.cbxSimulationInterval.getSelectedItem();
        SimInterval sitv = new SimInterval(s,Interval.getDefaultSimulationInterval(),Interval.getDefaultSimulationInterval());
        simulation = new BasicMeterSimulation(SimulationConfig.CASE_FILE,SimulationConfig.RANDOM_SEED,s,v,sitv);
        if(!checkLoadState(simulation,s))
            return;
        BasicMeterGroup bg = BasicMeterGroup.load(SimulationConfig.CASE_FILE, simulation.sectionHelper);
        System.out.println(bg.errortype.toString());
        if(bg.errortype.isNOT_LOADED()){
            setTable(null);
        }else if(bg.errortype.isSUCCESS()){
            setTable(bg);
        }else{
            JOptionPane.showMessageDialog(simFrame, bg.errortype.toString());
            return;
        }
        this.readySimulation(true);
    }
    
    private void setTable(BasicMeterGroup bg){
        String[] columns = new String[]{"MeterID","UpStream Station","DownStream Station","Dcap(Veh/hr/lane)","Kcr(Veh/mile)","Alplha","Beta","Gamma","Kqd"};
        
        DefaultTableModel tmodel = new DefaultTableModel();
        tmodel.setColumnIdentifiers(columns);
        mtable.setModel(tmodel);
        
        ArrayList<EntranceState> es = simulation.getEntranceStates(true);
//        ArrayList<SimMeter> es = simulation.getSimMeter();
        
        int totalrows = es.size();
//        totalrows = totalrows < 1 ? 1 : totalrows;
        
        DefaultTableModel rows = (DefaultTableModel)mtable.getModel();
        for(int i=0;i<totalrows;i++){
            Vector<Object> data = new Vector<Object>();
            
            //Add interval
            data.add(es.get(i).getID());
            
            //Add Data
            if(bg != null){
                for(BasicMeter bmeter : bg.getBasicMeters()){
                    if(bmeter.getID().equals(es.get(i).getID())){
                        /**
                         * set Station
                         */
                        data.add(bmeter.getUpStreamStationID());
                        data.add(bmeter.getDownStreamStationID());
                        
                        /**
                         * set Data
                         */
                        for(double d : bmeter.getConfigDatas()){
                            data.add(d);
                        }
                        break;
                    }
                }
            }
            
            rows.addRow(data);
        }
        
        mtable.getModel().addTableModelListener(new TableModelListener(){
            @Override
            public void tableChanged(TableModelEvent e) {
                if(mtable.isEditing()){
                    int row = mtable.getEditingRow();
                    int col = mtable.getEditingColumn();
                    String data = mtable.getValueAt(row, col).toString();
                    if(data == "")
                        return;
                    
                    if(col > 0 && col < 3){
                        if(data.length() > 1 && data.charAt(0) == 's'){
                            String[] tempd = data.split("s");
                            data = "S"+tempd[1];
                            mtable.setValueAt(data, row, col);
                        }else if(data.length() > 1 && data.charAt(0) == 'S'){
                        }else{
                            JOptionPane.showMessageDialog(simFrame, "Wrong data\n Type : S or s(ex. S111, s111)");
                            mtable.setValueAt("", row, col);
                        }
                    }else{
                        try{
                            double d = Double.parseDouble(data);
                        }catch(Exception error){
                            JOptionPane.showMessageDialog(simFrame, "Type only double data");
                            mtable.setValueAt(0, row, col);
                        }
                        System.out.println(data);
                    }
                }
            }
            
        });
    }
    
    private BasicMeter LoadTableData(int idx, EntranceState e,BasicMeterMethod bmm) {
        int index = idx+1;
        
        BasicMeterConfig bc = new BasicMeterConfig();
        
        DefaultTableModel rows = (DefaultTableModel)this.mtable.getModel();
        
        /**
         * set Config Data
         */
        ArrayList<Double> data = new ArrayList();
        for(int i=3;i<rows.getColumnCount();i++){
            double d = 0;
            if(rows.getValueAt(idx,i) != null){
                d = Double.parseDouble(rows.getValueAt(idx, i).toString());
            }
            data.add(d);
        }
        bc.setDatas(data);
        
        String upSID = rows.getValueAt(idx, 1).toString();
        String downSID = rows.getValueAt(idx, 2).toString();
        
        BasicMeter bm = new BasicMeter(e,bc,bmm);
        
        BasicMeterGroupErrorType error = bm.associateStationStream(upSID,downSID,simulation.sectionHelper);
        if(error.isNOSTATION()){
            JOptionPane.showMessageDialog(simFrame, error.toString());
            return null;
        }
        
        return bm;
    }
    
    private BasicMeterGroup SaveCurrentData(){
        if(!checkTable())
            return null;
        
        ArrayList<EntranceState> es = simulation.getEntranceStates(true);
        ArrayList<BasicMeter> bmeters = new ArrayList<BasicMeter>();
        BasicMeterMethod bmm = (BasicMeterMethod)this.cbxSOption.getSelectedItem();
        int idx = 0;
        for(EntranceState e : es){
            BasicMeter bm = LoadTableData(idx,e,bmm);
            if(bm == null)
                return null;
            
            bmeters.add(bm);
            idx++;
            
            System.out.print(bm.getID()+" : ");
            for(double d : bm.getConfigDatas()){
                System.out.print(d+", ");
            }
            System.out.println();
        }
        
        BasicMeterGroup bg = new BasicMeterGroup(SimulationConfig.CASE_FILE,bmeters);
        bg.Save();
        
        return bg;
    }

    private boolean checkTable() {
        BasicMeterConfig bc = new BasicMeterConfig();
        
        DefaultTableModel rows = (DefaultTableModel)this.mtable.getModel();
        
        /**
         * set Config Data
         */
        for(int z=0;z<rows.getRowCount();z++){
            if(z == 0)
                continue;
            for(int i=1;i<3;i++){
                if(rows.getValueAt(z, i) == null
                     || rows.getValueAt(z, i).equals("")){
                    JOptionPane.showMessageDialog(simFrame, "Fill the All table datas");
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean checkLoadState(BasicMeterSimulation sim, Section s) {
        List<String> stationid = s.getStationIds();
        for(String id : stationid){
            StationState station = sim.getStationState(id);
            if(station != null){
                return true;
            }
        }
        JOptionPane.showMessageDialog(simFrame, "Section and Casefile was not matched.");
        return false;
    }
    private void isLoaded(boolean flag){
        this.btnLoadCaseFile.setEnabled(flag);
        this.btnStartSimulation.setEnabled(false);
    }
    private void readySimulation(boolean isready){
        this.btnStartSimulation.setEnabled(isready);
        this.btnLoadCaseFile.setEnabled(!isready);
    }
    
    private void isStartSimulation(boolean flag){
        if(flag){
            tabPanel.setSelectedIndex(1);
        }
        this.btnStartSimulation.setEnabled(!flag);
        this.btnLoadCaseFile.setEnabled(!flag);
        this.cbxSOption.setEnabled(!flag);
        this.cbxSections.setEnabled(!flag);
        this.cbxvissimVersion.setEnabled(!flag);
        this.btnLoadCaseFile.setEnabled(!flag);
    }

    private void StartSimulation() {
            BasicMeterGroup bmg = this.SaveCurrentData();
            if(bmg == null)
                return;
            this.redirectOutput(simstate, meterstate);
            try{
                startTime = new Date();
                Section section = (Section)this.cbxSections.getSelectedItem();
//                simulation = new BasicMeterSimulation(SimulationConfig.CASE_FILE,SimulationConfig.RANDOM_SEED,section,(VISSIMVersion)this.cbxvissimVersion.getSelectedItem());
                simulation.setVISSIMVersion((VISSIMVersion)this.cbxvissimVersion.getSelectedItem());
                simulation.setRandomSeed(SimulationConfig.RANDOM_SEED);
                simulation.setSignalListener(this);
                simulation.setBasicMeter(bmg.getBasicMeters());
                simulation.start();
                this.isStartSimulation(true);
            }catch(Exception e){
                e.printStackTrace();
            }
    }

    private void setVissimVisible(boolean b) {
        if(simulation != null)
            simulation.setVissimVisible(b);
    }
    
    /**
     * Redirect output into log box
     */
    public void redirectOutput(JTextArea j1, JTextArea j2) {
        backupOut = System.out;
        backupErr = System.err;
        // redirect System.out and System.err to log textbox
        StringOutputStream sos = new StringOutputStream(j1);
        StringOutputStream sos1 = new StringOutputStream(j2);
        System.setOut(new PrintStream(sos));
        System.setErr(new PrintStream(sos1));
    }
    
    public void restoreOutput() {
        System.setOut(backupOut);
        System.setErr(backupErr);
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
                    if(text.contains("clearlog"))
                        logText.setText("");
                    else
                        logText.append(text);
                }
            });
        }
    }
}
