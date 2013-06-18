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
package edu.umn.natsrl.ticas.plugin.simulation.IRIS;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.DMS;
import edu.umn.natsrl.infra.infraobjects.DMSImpl;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import edu.umn.natsrl.ticas.Simulation.Emulation;
import edu.umn.natsrl.ticas.Simulation.Simulation;
import edu.umn.natsrl.ticas.Simulation.SimulationConfig;
import edu.umn.natsrl.ticas.Simulation.SimulationUtil;
import edu.umn.natsrl.ticas.Simulation.StringOutputStream;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.metering.MeteringConfig;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLChartXY;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLConfig;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLEmulation;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLResultExtractor;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLResultStation;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLResults;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLSTAType;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.VSLSim;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.AccCheckThreshold;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.BottleneckSpeed;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.DensityAggregation;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.MaxSpeed;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.SpeedAggregation;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.SpeedforLowK;
import edu.umn.natsrl.ticas.plugin.simulation.VSL.algorithm.VSLVersion;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.vissimcom.ComError;
import edu.umn.natsrl.vissimcom.VISSIMVersion;
import info.monitorenter.gui.chart.views.ChartPanel;
import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SimulationGUI extends javax.swing.JPanel implements Simulation.ISimEndSignal, Emulation.ISimEndSignal{
    private PluginFrame simFrame;
    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    
    Simulation simulation;
    VSLEmulation emulation;
    private Date startTime;
    
    private PrintStream backupOut;
    private PrintStream backupErr;
    
    /**
     * Map
     */
    private int initZoom = 10;
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;    
    private MapHelper simMapHelper;
    
    /**
     * Result Chart
     */
    VSLChartXY resultchart;
    VSLResults vslresult_chart;
    ChartPanel result_cpn;
    boolean isResultLoaded = false;
    
    VSLSTAType STAType;
    /**
     * Creates new form VSLSimulationGUI
     */
    public SimulationGUI(PluginFrame parent) {
        initComponents();
        this.simFrame = parent;
        
        init();
        
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                if(simulation != null) {
                    simulation.simulationStop();
                }
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
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
                jPanel1 = new javax.swing.JPanel();
                jPanel3 = new javax.swing.JPanel();
                jLabel1 = new javax.swing.JLabel();
                cbxSections = new javax.swing.JComboBox();
                btnOpenSectionEditor = new javax.swing.JButton();
                jLabel2 = new javax.swing.JLabel();
                tbxCaseFile = new javax.swing.JTextField();
                btnSelectCasefile = new javax.swing.JButton();
                jLabel3 = new javax.swing.JLabel();
                jLabel6 = new javax.swing.JLabel();
                tbxRandom = new javax.swing.JTextField();
                cbxvissimVersion = new javax.swing.JComboBox();
                jLabel15 = new javax.swing.JLabel();
                cbxSimulationMode = new javax.swing.JComboBox();
                jLabel18 = new javax.swing.JLabel();
                cbxSimulationCate = new javax.swing.JComboBox();
                jLabel48 = new javax.swing.JLabel();
                cbxSimulationInterval = new javax.swing.JComboBox();
                btnStartSimulation = new javax.swing.JButton();
                pnDate = new javax.swing.JPanel();
                natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
                jLabel7 = new javax.swing.JLabel();
                cbxStartHour = new javax.swing.JComboBox();
                jLabel28 = new javax.swing.JLabel();
                cbxStartMin = new javax.swing.JComboBox();
                cbxEndMin = new javax.swing.JComboBox();
                jLabel20 = new javax.swing.JLabel();
                cbxEndHour = new javax.swing.JComboBox();
                jLabel11 = new javax.swing.JLabel();
                jPanel12 = new javax.swing.JPanel();
                jPanel4 = new javax.swing.JPanel();
                tbxZesDecceleration = new javax.swing.JTextField();
                jLabel5 = new javax.swing.JLabel();
                tbxMinStationDistance = new javax.swing.JTextField();
                jLabel10 = new javax.swing.JLabel();
                cbxVSLVersion = new javax.swing.JComboBox();
                jLabel14 = new javax.swing.JLabel();
                tbxVSLMOVEDec = new javax.swing.JTextField();
                jLabel16 = new javax.swing.JLabel();
                jLabel17 = new javax.swing.JLabel();
                tbxVSLMOVESpeed = new javax.swing.JTextField();
                jLabel22 = new javax.swing.JLabel();
                jLabel21 = new javax.swing.JLabel();
                tbxVSLZoneLength = new javax.swing.JTextField();
                jPanel5 = new javax.swing.JPanel();
                jLabel24 = new javax.swing.JLabel();
                cbxVSSSpeed = new javax.swing.JComboBox();
                tbxBSSpeedThreshold = new javax.swing.JTextField();
                tbxDeceleration = new javax.swing.JTextField();
                cbxVSSDec = new javax.swing.JComboBox();
                jLabel19 = new javax.swing.JLabel();
                cbxAccident = new javax.swing.JCheckBox();
                tbxAccident = new javax.swing.JTextField();
                tbxTurnOffAcceleration = new javax.swing.JTextField();
                jLabel9 = new javax.swing.JLabel();
                jLabel49 = new javax.swing.JLabel();
                jLabel50 = new javax.swing.JLabel();
                tbxVSLSTADISTANCE = new javax.swing.JTextField();
                tbxVSLSTASPEED = new javax.swing.JTextField();
                cbxAS = new javax.swing.JCheckBox();
                cbxUpstream = new javax.swing.JCheckBox();
                jPanel13 = new javax.swing.JPanel();
                jPanel14 = new javax.swing.JPanel();
                jLabel8 = new javax.swing.JLabel();
                jLabel13 = new javax.swing.JLabel();
                jLabel23 = new javax.swing.JLabel();
                tbxMaxWaittingTime = new javax.swing.JTextField();
                tbxKjam = new javax.swing.JTextField();
                tbxKc = new javax.swing.JTextField();
                jLabel25 = new javax.swing.JLabel();
                jLabel26 = new javax.swing.JLabel();
                jLabel27 = new javax.swing.JLabel();
                jLabel29 = new javax.swing.JLabel();
                jLabel30 = new javax.swing.JLabel();
                tbxKd_Rate = new javax.swing.JTextField();
                jLabel31 = new javax.swing.JLabel();
                tbxMaxRedTime = new javax.swing.JTextField();
                jLabel32 = new javax.swing.JLabel();
                jLabel33 = new javax.swing.JLabel();
                tbxMaxWaittingTimeF2F = new javax.swing.JTextField();
                jLabel34 = new javax.swing.JLabel();
                jLabel35 = new javax.swing.JLabel();
                jLabel36 = new javax.swing.JLabel();
                tbxKb = new javax.swing.JTextField();
                jLabel37 = new javax.swing.JLabel();
                jLabel38 = new javax.swing.JLabel();
                tbxAb = new javax.swing.JTextField();
                jLabel39 = new javax.swing.JLabel();
                jLabel40 = new javax.swing.JLabel();
                tbxKstop = new javax.swing.JTextField();
                jLabel41 = new javax.swing.JLabel();
                jLabel42 = new javax.swing.JLabel();
                tbxStopDuration = new javax.swing.JTextField();
                jLabel43 = new javax.swing.JLabel();
                jLabel44 = new javax.swing.JLabel();
                tbxStopTrend = new javax.swing.JTextField();
                jLabel45 = new javax.swing.JLabel();
                jLabel46 = new javax.swing.JLabel();
                tbxStopUpstreamCount = new javax.swing.JTextField();
                jLabel47 = new javax.swing.JLabel();
                jPanel2 = new javax.swing.JPanel();
                chkShowVehicles = new javax.swing.JCheckBox();
                jPanel6 = new javax.swing.JPanel();
                jScrollPane2 = new javax.swing.JScrollPane();
                errorstate = new javax.swing.JTextArea();
                jPanel7 = new javax.swing.JPanel();
                jScrollPane3 = new javax.swing.JScrollPane();
                simstate = new javax.swing.JTextArea();
                jPanel9 = new javax.swing.JPanel();
                jPanel10 = new javax.swing.JPanel();
                PanelChart = new javax.swing.JPanel();
                jPanel8 = new javax.swing.JPanel();
                jPanel11 = new javax.swing.JPanel();
                PanelChart_result = new javax.swing.JPanel();
                cbxSavedResult = new javax.swing.JComboBox();
                btnReadResult = new javax.swing.JButton();
                btnLoadExcel = new javax.swing.JButton();
                sld_resultChart = new javax.swing.JSlider();
                tbxTimer = new javax.swing.JTextField();
                btn_reChart_left = new javax.swing.JButton();
                btn_reChart_right = new javax.swing.JButton();
                jScrollPane1 = new javax.swing.JScrollPane();
                Result_table = new javax.swing.JTable();
                cbx_result_station = new javax.swing.JComboBox();
                jLabel12 = new javax.swing.JLabel();
                btn_deleteresult = new javax.swing.JButton();

                jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Simulation/Emulation Option", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

                jLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel1.setText("Select Freeway Section");

                cbxSections.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxSections.setPreferredSize(new java.awt.Dimension(200, 22));

                btnOpenSectionEditor.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                btnOpenSectionEditor.setText("Edit Route");
                btnOpenSectionEditor.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnOpenSectionEditorActionPerformed(evt);
                        }
                });

                jLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel2.setText("Select Simulation Case File (Needs to Match with Real Section)");

                tbxCaseFile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxCaseFile.setPreferredSize(new java.awt.Dimension(250, 22));

                btnSelectCasefile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                btnSelectCasefile.setText("Browser");
                btnSelectCasefile.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnSelectCasefileActionPerformed(evt);
                        }
                });

                jLabel3.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
                jLabel3.setText("Random Number");

                jLabel6.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
                jLabel6.setText("VISSIM Version");

                tbxRandom.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxRandom.setPreferredSize(new java.awt.Dimension(100, 22));

                cbxvissimVersion.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

                jLabel15.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel15.setText("Simulation/Emulation Mode");

                cbxSimulationMode.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxSimulationMode.setPreferredSize(new java.awt.Dimension(150, 22));
                cbxSimulationMode.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxSimulationModeActionPerformed(evt);
                        }
                });

                jLabel18.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel18.setText("Simulation/Emulation Option");

                cbxSimulationCate.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxSimulationCate.setPreferredSize(new java.awt.Dimension(150, 22));
                cbxSimulationCate.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxSimulationCateActionPerformed(evt);
                        }
                });

                jLabel48.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
                jLabel48.setText("Simulation Interval");

                cbxSimulationInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

                javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
                jPanel3.setLayout(jPanel3Layout);
                jPanel3Layout.setHorizontalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(btnSelectCasefile)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel3)
                                        .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbxvissimVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnOpenSectionEditor))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel15)
                                                        .addComponent(cbxSimulationMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(cbxSimulationCate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel18)))
                                        .addComponent(jLabel48)
                                        .addComponent(cbxSimulationInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(190, Short.MAX_VALUE))
                );
                jPanel3Layout.setVerticalGroup(
                        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel15)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbxSimulationMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel18)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbxSimulationCate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnOpenSectionEditor)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnSelectCasefile))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(13, 13, 13)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxvissimVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel48)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxSimulationInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(18, Short.MAX_VALUE))
                );

                btnStartSimulation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                btnStartSimulation.setText("Start Simulation/Emulation");
                btnStartSimulation.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnStartSimulationActionPerformed(evt);
                        }
                });

                pnDate.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

                jLabel7.setText("Start Time");

                cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
                cbxStartHour.setMinimumSize(new java.awt.Dimension(40, 20));
                cbxStartHour.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                cbxStartHourMouseClicked(evt);
                        }
                });

                jLabel28.setFont(new java.awt.Font("Verdana 12", 0, 12)); // NOI18N
                jLabel28.setText(":");

                cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
                cbxStartMin.setMinimumSize(new java.awt.Dimension(40, 20));

                cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
                cbxEndMin.setMinimumSize(new java.awt.Dimension(40, 20));

                jLabel20.setFont(new java.awt.Font("Verdana 12", 0, 12)); // NOI18N
                jLabel20.setText(":");

                cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
                cbxEndHour.setMinimumSize(new java.awt.Dimension(40, 20));

                jLabel11.setText("End Time");

                javax.swing.GroupLayout pnDateLayout = new javax.swing.GroupLayout(pnDate);
                pnDate.setLayout(pnDateLayout);
                pnDateLayout.setHorizontalGroup(
                        pnDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnDateLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(pnDateLayout.createSequentialGroup()
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(5, 5, 5)
                                                .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(pnDateLayout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(5, 5, 5)
                                                .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                pnDateLayout.setVerticalGroup(
                        pnDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(pnDateLayout.createSequentialGroup()
                                .addGroup(pnDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel28)
                                        .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel20)
                                        .addComponent(jLabel11)))
                );

                javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(btnStartSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(pnDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(327, Short.MAX_VALUE))
                );
                jPanel1Layout.setVerticalGroup(
                        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnStartSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(77, Short.MAX_VALUE))
                );

                tabPanel.addTab("Simulation/Emulation", jPanel1);

                jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "VSL Strategy Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

                tbxZesDecceleration.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxZesDecceleration.setPreferredSize(new java.awt.Dimension(60, 22));

                jLabel5.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel5.setText("Fixed Decel For VSL/Zone(Original Version)(mile/h^2)");

                tbxMinStationDistance.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxMinStationDistance.setPreferredSize(new java.awt.Dimension(40, 22));

                jLabel10.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel10.setText("Min Distance Bet 2 Stations(mile)");

                cbxVSLVersion.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxVSLVersion.setPreferredSize(new java.awt.Dimension(150, 22));
                cbxVSLVersion.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxVSLVersionActionPerformed(evt);
                        }
                });

                jLabel14.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel14.setText("VSS Tracking");

                tbxVSLMOVEDec.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxVSLMOVEDec.setPreferredSize(new java.awt.Dimension(60, 22));

                jLabel16.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel16.setText("Deceleration(mile/h^2)");

                jLabel17.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel17.setText("Speed Threshold(mile/h)");

                tbxVSLMOVESpeed.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxVSLMOVESpeed.setPreferredSize(new java.awt.Dimension(60, 22));

                jLabel22.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel22.setText("VSL Strategy Selection");

                jLabel21.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel21.setText("VSL Zone Length(mile)");

                tbxVSLZoneLength.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxVSLZoneLength.setPreferredSize(new java.awt.Dimension(40, 22));

                jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bottleneck Station(VSS) Initial Identification", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

                jLabel24.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel24.setText("VSS Speed Condition (mile/h)");

                cbxVSSSpeed.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxVSSSpeed.setPreferredSize(new java.awt.Dimension(150, 22));
                cbxVSSSpeed.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxVSSSpeedActionPerformed(evt);
                        }
                });

                tbxBSSpeedThreshold.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxBSSpeedThreshold.setPreferredSize(new java.awt.Dimension(60, 22));

                tbxDeceleration.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxDeceleration.setPreferredSize(new java.awt.Dimension(60, 22));
                tbxDeceleration.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                tbxDecelerationActionPerformed(evt);
                        }
                });

                cbxVSSDec.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxVSSDec.setPreferredSize(new java.awt.Dimension(150, 22));
                cbxVSSDec.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxVSSDecActionPerformed(evt);
                        }
                });

                jLabel19.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel19.setText("VSS Deceleration Condition (mile/h^2)");

                cbxAccident.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxAccident.setText("Accident Threshold Option (mile/h)");

                tbxAccident.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxAccident.setPreferredSize(new java.awt.Dimension(60, 22));
                tbxAccident.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                tbxAccidentActionPerformed(evt);
                        }
                });

                tbxTurnOffAcceleration.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxTurnOffAcceleration.setPreferredSize(new java.awt.Dimension(60, 22));

                jLabel9.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel9.setText("VSS Turnoff Decel (mile/h^2)");

                javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
                jPanel5.setLayout(jPanel5Layout);
                jPanel5Layout.setHorizontalGroup(
                        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel19)
                                        .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(cbxVSSSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(cbxVSSDec, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxDeceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(tbxBSSpeedThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel24)
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                                                        .addGap(21, 21, 21)
                                                        .addComponent(jLabel9)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(tbxTurnOffAcceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                                                        .addComponent(cbxAccident)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(tbxAccident, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(63, Short.MAX_VALUE))
                );
                jPanel5Layout.setVerticalGroup(
                        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbxVSSSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tbxBSSpeedThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel19)
                                .addGap(9, 9, 9)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tbxDeceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbxVSSDec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbxAccident)
                                        .addComponent(tbxAccident, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel9)
                                        .addComponent(tbxTurnOffAcceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                );

                jLabel49.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel49.setText("To Slow Traffic Ahead Sign");

                jLabel50.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel50.setText("STA Distance from boundary VSL Sign(mile)");

                tbxVSLSTADISTANCE.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxVSLSTADISTANCE.setPreferredSize(new java.awt.Dimension(60, 22));

                tbxVSLSTASPEED.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxVSLSTASPEED.setPreferredSize(new java.awt.Dimension(60, 22));

                cbxAS.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxAS.setText("Min VSL at 1st Active Sign (mile/h)");
                cbxAS.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxASActionPerformed(evt);
                        }
                });

                cbxUpstream.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxUpstream.setText("Upstream Speed( >= 1.5mile from VSS) > Speed Limit");
                cbxUpstream.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbxUpstreamActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
                jPanel4.setLayout(jPanel4Layout);
                jPanel4Layout.setHorizontalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(cbxVSLVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addComponent(jLabel5)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tbxZesDecceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addComponent(jLabel21)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tbxVSLZoneLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel10)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tbxMinStationDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(100, 100, 100))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addComponent(cbxAS)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tbxVSLSTASPEED, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(cbxUpstream)
                                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                                .addGap(21, 21, 21)
                                                                .addComponent(jLabel50)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(tbxVSLSTADISTANCE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGroup(jPanel4Layout.createSequentialGroup()
                                                                        .addComponent(jLabel22)
                                                                        .addGap(277, 277, 277))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                                                        .addGap(10, 10, 10)
                                                                        .addComponent(jLabel16)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(tbxVSLMOVEDec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(jLabel17)
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(tbxVSLMOVESpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addComponent(jLabel14)
                                                        .addComponent(jLabel49))
                                                .addGap(0, 0, Short.MAX_VALUE))))
                );
                jPanel4Layout.setVerticalGroup(
                        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxVSLVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel21)
                                        .addComponent(tbxVSLZoneLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel10)
                                        .addComponent(tbxMinStationDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5)
                                        .addComponent(tbxZesDecceleration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel16)
                                        .addComponent(jLabel17)
                                        .addComponent(tbxVSLMOVESpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tbxVSLMOVEDec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel49)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cbxAS)
                                        .addComponent(tbxVSLSTASPEED, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel50)
                                        .addComponent(tbxVSLSTADISTANCE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxUpstream)
                                .addContainerGap(130, Short.MAX_VALUE))
                );

                javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
                jPanel12.setLayout(jPanel12Layout);
                jPanel12Layout.setHorizontalGroup(
                        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel12Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(442, Short.MAX_VALUE))
                );
                jPanel12Layout.setVerticalGroup(
                        jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel12Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );

                tabPanel.addTab("VSL Option", jPanel12);

                jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Metering Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

                jLabel8.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel8.setText("Kjam");

                jLabel13.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel13.setText("Kcrit");

                jLabel23.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel23.setText("Max Wait Time1");

                tbxMaxWaittingTime.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxMaxWaittingTime.setText("4");

                tbxKjam.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxKjam.setText("180");

                tbxKc.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxKc.setText("40");

                jLabel25.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel25.setText("min");

                jLabel26.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel26.setText("veh/hr");

                jLabel27.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel27.setText("veh/hr");

                jLabel29.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel29.setText("x Kcrit");

                jLabel30.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel30.setText("Kd");

                tbxKd_Rate.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxKd_Rate.setText("0.8");

                jLabel31.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel31.setText("Max Red Time");

                tbxMaxRedTime.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxMaxRedTime.setText("30");

                jLabel32.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel32.setText("seconds");

                jLabel33.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel33.setText("Max Wait Time2");

                tbxMaxWaittingTimeF2F.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxMaxWaittingTimeF2F.setText("2");

                jLabel34.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel34.setText("min");

                jLabel35.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
                jLabel35.setText("(freeway to freeway)");

                jLabel36.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel36.setText("Kb");

                tbxKb.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxKb.setText("25");

                jLabel37.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel37.setText("veh/hr");

                jLabel38.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel38.setText("Ab");

                tbxAb.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxAb.setText("1000");

                jLabel39.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel39.setText("mile/hr^2");

                jLabel40.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel40.setText("Kstop");

                tbxKstop.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxKstop.setText("20");

                jLabel41.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel41.setText("veh/hr");

                jLabel42.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel42.setText("Stop Duration");

                tbxStopDuration.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxStopDuration.setText("10");

                jLabel43.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel43.setText("time steps");

                jLabel44.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel44.setText("Stop BS Trend");

                tbxStopTrend.setEditable(false);
                tbxStopTrend.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxStopTrend.setText("0");

                jLabel45.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel45.setText("time steps");

                jLabel46.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel46.setText("Stop Upstream #");

                tbxStopUpstreamCount.setEditable(false);
                tbxStopUpstreamCount.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                tbxStopUpstreamCount.setText("0");

                jLabel47.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
                jLabel47.setText("stations");

                javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
                jPanel14.setLayout(jPanel14Layout);
                jPanel14Layout.setHorizontalGroup(
                        jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel14Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel14Layout.createSequentialGroup()
                                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel23)
                                                        .addComponent(jLabel8)
                                                        .addComponent(jLabel13)
                                                        .addComponent(jLabel30)
                                                        .addComponent(jLabel33)
                                                        .addComponent(jLabel36)
                                                        .addComponent(jLabel38)
                                                        .addComponent(jLabel40)
                                                        .addComponent(jLabel42)
                                                        .addComponent(jLabel44)
                                                        .addComponent(jLabel46))
                                                .addGap(27, 27, 27)
                                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(tbxMaxRedTime, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxKc, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxKd_Rate, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxMaxWaittingTime, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxMaxWaittingTimeF2F, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxKjam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(tbxKb, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxAb, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxKstop, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxStopDuration, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxStopTrend, javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tbxStopUpstreamCount, javax.swing.GroupLayout.Alignment.LEADING)))
                                        .addGroup(jPanel14Layout.createSequentialGroup()
                                                .addComponent(jLabel31)
                                                .addGap(103, 103, 103)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel32)
                                        .addComponent(jLabel26)
                                        .addComponent(jLabel27)
                                        .addComponent(jLabel29)
                                        .addComponent(jLabel25)
                                        .addGroup(jPanel14Layout.createSequentialGroup()
                                                .addComponent(jLabel34)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLabel35))
                                        .addComponent(jLabel37)
                                        .addComponent(jLabel39)
                                        .addComponent(jLabel41)
                                        .addComponent(jLabel43)
                                        .addComponent(jLabel45)
                                        .addComponent(jLabel47)))
                );
                jPanel14Layout.setVerticalGroup(
                        jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel14Layout.createSequentialGroup()
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(tbxKjam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel26))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel13)
                                        .addComponent(tbxKc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel27))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel30)
                                        .addComponent(tbxKd_Rate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel29))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel36)
                                        .addComponent(tbxKb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel37))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel40)
                                        .addComponent(tbxKstop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel41))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel42)
                                        .addComponent(tbxStopDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel43))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel44)
                                        .addComponent(tbxStopTrend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel45))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel46)
                                        .addComponent(tbxStopUpstreamCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel47))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel38)
                                        .addComponent(tbxAb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel39))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel23)
                                        .addComponent(tbxMaxWaittingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel25))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel33)
                                        .addComponent(tbxMaxWaittingTimeF2F, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel34)
                                        .addComponent(jLabel35))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel31)
                                        .addComponent(tbxMaxRedTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel32))
                                .addContainerGap(20, Short.MAX_VALUE))
                );

                javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
                jPanel13.setLayout(jPanel13Layout);
                jPanel13Layout.setHorizontalGroup(
                        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel13Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(553, Short.MAX_VALUE))
                );
                jPanel13Layout.setVerticalGroup(
                        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel13Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(269, 269, 269))
                );

                tabPanel.addTab("MeteringOption", jPanel13);

                chkShowVehicles.setText("Show Vehicles and road");
                chkShowVehicles.setEnabled(false);
                chkShowVehicles.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                chkShowVehiclesActionPerformed(evt);
                        }
                });

                jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ErrorState", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

                errorstate.setColumns(20);
                errorstate.setRows(5);
                jScrollPane2.setViewportView(errorstate);

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
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                                .addContainerGap())
                );
                jPanel7Layout.setVerticalGroup(
                        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                                .addContainerGap())
                );

                javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
                jPanel2.setLayout(jPanel2Layout);
                jPanel2Layout.setHorizontalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(chkShowVehicles)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                );
                jPanel2Layout.setVerticalGroup(
                        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(chkShowVehicles)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                );

                tabPanel.addTab("Simulation Console", jPanel2);

                jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph"));

                javax.swing.GroupLayout PanelChartLayout = new javax.swing.GroupLayout(PanelChart);
                PanelChart.setLayout(PanelChartLayout);
                PanelChartLayout.setHorizontalGroup(
                        PanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 889, Short.MAX_VALUE)
                );
                PanelChartLayout.setVerticalGroup(
                        PanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 371, Short.MAX_VALUE)
                );

                javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
                jPanel10.setLayout(jPanel10Layout);
                jPanel10Layout.setHorizontalGroup(
                        jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel10Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(PanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );
                jPanel10Layout.setVerticalGroup(
                        jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel10Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(PanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );

                javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
                jPanel9.setLayout(jPanel9Layout);
                jPanel9Layout.setHorizontalGroup(
                        jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );
                jPanel9Layout.setVerticalGroup(
                        jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(233, Short.MAX_VALUE))
                );

                tabPanel.addTab("Simulation State", jPanel9);

                jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph"));

                PanelChart_result.addComponentListener(new java.awt.event.ComponentAdapter() {
                        public void componentResized(java.awt.event.ComponentEvent evt) {
                                PanelChart_resultComponentResized(evt);
                        }
                });
                PanelChart_result.addHierarchyListener(new java.awt.event.HierarchyListener() {
                        public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                                PanelChart_resultHierarchyChanged(evt);
                        }
                });

                javax.swing.GroupLayout PanelChart_resultLayout = new javax.swing.GroupLayout(PanelChart_result);
                PanelChart_result.setLayout(PanelChart_resultLayout);
                PanelChart_resultLayout.setHorizontalGroup(
                        PanelChart_resultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                );
                PanelChart_resultLayout.setVerticalGroup(
                        PanelChart_resultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 371, Short.MAX_VALUE)
                );

                javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
                jPanel11.setLayout(jPanel11Layout);
                jPanel11Layout.setHorizontalGroup(
                        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(PanelChart_result, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );
                jPanel11Layout.setVerticalGroup(
                        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel11Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(PanelChart_result, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );

                cbxSavedResult.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbxSavedResult.setPreferredSize(new java.awt.Dimension(300, 20));

                btnReadResult.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                btnReadResult.setText("Load");
                btnReadResult.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnReadResultActionPerformed(evt);
                        }
                });

                btnLoadExcel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                btnLoadExcel.setText("Extract");
                btnLoadExcel.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnLoadExcelActionPerformed(evt);
                        }
                });

                sld_resultChart.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                        public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                                sld_resultChartMouseWheelMoved(evt);
                        }
                });
                sld_resultChart.addChangeListener(new javax.swing.event.ChangeListener() {
                        public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                sld_resultChartStateChanged(evt);
                        }
                });

                tbxTimer.setEditable(false);
                tbxTimer.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

                btn_reChart_left.setText("< PREV");
                btn_reChart_left.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btn_reChart_leftActionPerformed(evt);
                        }
                });

                btn_reChart_right.setText("NEXT >");
                btn_reChart_right.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btn_reChart_rightActionPerformed(evt);
                        }
                });

                Result_table.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                Result_table.setModel(new javax.swing.table.DefaultTableModel(
                        new Object [][] {
                                {null, null, null, null},
                                {null, null, null, null},
                                {null, null, null, null},
                                {null, null, null, null}
                        },
                        new String [] {
                                "", "Title 2", "Title 3", "Title 4"
                        }
                ) {
                        boolean[] canEdit = new boolean [] {
                                false, false, false, true
                        };

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit [columnIndex];
                        }
                });
                jScrollPane1.setViewportView(Result_table);

                cbx_result_station.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                cbx_result_station.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                cbx_result_stationItemStateChanged(evt);
                        }
                });
                cbx_result_station.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                cbx_result_stationActionPerformed(evt);
                        }
                });

                jLabel12.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
                jLabel12.setText("Station Info");

                btn_deleteresult.setText("Delete");
                btn_deleteresult.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btn_deleteresultActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
                jPanel8.setLayout(jPanel8Layout);
                jPanel8Layout.setHorizontalGroup(
                        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(sld_resultChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                                .addComponent(tbxTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(btn_reChart_left)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(btn_reChart_right)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(cbx_result_station, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)))
                                                        .addComponent(cbxSavedResult, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                                .addComponent(btnReadResult, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(btn_deleteresult, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                                .addComponent(btnLoadExcel, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
                );
                jPanel8Layout.setVerticalGroup(
                        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sld_resultChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(tbxTimer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(btn_reChart_left)
                                                                .addComponent(btn_reChart_right)
                                                                .addComponent(jLabel12)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbx_result_station, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cbxSavedResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(btn_deleteresult, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnReadResult, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnLoadExcel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(63, 63, 63))
                                        .addGroup(jPanel8Layout.createSequentialGroup()
                                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                                .addContainerGap())))
                );

                tabPanel.addTab("VSL Result", jPanel8);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabPanel)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                                .addContainerGap())
                );
        }// </editor-fold>//GEN-END:initComponents

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        // TODO add your handling code here:
        this.openSectionEditor();
    }//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void btnSelectCasefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectCasefileActionPerformed
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
    }//GEN-LAST:event_btnSelectCasefileActionPerformed

    private void btnStartSimulationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartSimulationActionPerformed
        // TODO add your handling code here:
//        StartSimulation
        Process();
        
    }//GEN-LAST:event_btnStartSimulationActionPerformed

    private void chkShowVehiclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowVehiclesActionPerformed
        // TODO add your handling code here:
        setVissimVisible(this.chkShowVehicles.isSelected());
    }//GEN-LAST:event_chkShowVehiclesActionPerformed

    private void btnReadResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadResultActionPerformed
        // TODO add your handling code here:
        LoadResultChart();
    }//GEN-LAST:event_btnReadResultActionPerformed

    private void btnLoadExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadExcelActionPerformed
        // TODO add your handling code here:
        extractExcel();
    }//GEN-LAST:event_btnLoadExcelActionPerformed

    private void cbxStartHourMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cbxStartHourMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxStartHourMouseClicked

    private void cbxSimulationModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSimulationModeActionPerformed
        // TODO add your handling code here:
        SimulationModeVisible((SimulationMode)this.cbxSimulationMode.getSelectedItem());
    }//GEN-LAST:event_cbxSimulationModeActionPerformed

    private void sld_resultChartStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sld_resultChartStateChanged
        // TODO add your handling code here:
        if(!isResultLoaded){
            return;
        }
        updateSlide(sld_resultChart.getValue());
    }//GEN-LAST:event_sld_resultChartStateChanged

    private void sld_resultChartMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sld_resultChartMouseWheelMoved
        // TODO add your handling code here:
        if(!isResultLoaded){
            return;
        }
        
        if(evt.getWheelRotation() > 0){ //up
            updateSlideWheel(-1);
        }else{ //down
            updateSlideWheel(1);
        }
    }//GEN-LAST:event_sld_resultChartMouseWheelMoved

    private void btn_reChart_leftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_reChart_leftActionPerformed
        // TODO add your handling code here:
        if(isResultLoaded){
            updateSlideWheel(-1);
        }
    }//GEN-LAST:event_btn_reChart_leftActionPerformed

    private void btn_reChart_rightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_reChart_rightActionPerformed
        // TODO add your handling code here:
        if(isResultLoaded){
            updateSlideWheel(1);
        }
    }//GEN-LAST:event_btn_reChart_rightActionPerformed

    private void PanelChart_resultHierarchyChanged(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_PanelChart_resultHierarchyChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_PanelChart_resultHierarchyChanged

    private void PanelChart_resultComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_PanelChart_resultComponentResized
        // TODO add your handling code here:
        if(isResultLoaded){
            updateSlide(sld_resultChart.getValue());
        }
    }//GEN-LAST:event_PanelChart_resultComponentResized

    private void cbx_result_stationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbx_result_stationItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_cbx_result_stationItemStateChanged

    private void cbx_result_stationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbx_result_stationActionPerformed
        // TODO add your handling code here:
        if(isResultLoaded){
           loadResultDatas();
        }
    }//GEN-LAST:event_cbx_result_stationActionPerformed

    private void btn_deleteresultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deleteresultActionPerformed
        // TODO add your handling code here:
        deleteSelectedResult((VSLResults)this.cbxSavedResult.getSelectedItem());
    }//GEN-LAST:event_btn_deleteresultActionPerformed

    private void cbxSimulationCateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSimulationCateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxSimulationCateActionPerformed

        private void cbxVSLVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVSLVersionActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_cbxVSLVersionActionPerformed

        private void cbxVSSSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVSSSpeedActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_cbxVSSSpeedActionPerformed

        private void tbxDecelerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxDecelerationActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_tbxDecelerationActionPerformed

        private void cbxVSSDecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxVSSDecActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_cbxVSSDecActionPerformed

        private void tbxAccidentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxAccidentActionPerformed
                // TODO add your handling code here:
        }//GEN-LAST:event_tbxAccidentActionPerformed

        private void cbxASActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxASActionPerformed
                // TODO add your handling code here:
                setSTAType(VSLSTAType.OPTION1);
        }//GEN-LAST:event_cbxASActionPerformed

        private void cbxUpstreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxUpstreamActionPerformed
                // TODO add your handling code here:
                setSTAType(VSLSTAType.OPTION2);
        }//GEN-LAST:event_cbxUpstreamActionPerformed

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel PanelChart;
        private javax.swing.JPanel PanelChart_result;
        private javax.swing.JTable Result_table;
        private javax.swing.JButton btnLoadExcel;
        private javax.swing.JButton btnOpenSectionEditor;
        private javax.swing.JButton btnReadResult;
        private javax.swing.JButton btnSelectCasefile;
        private javax.swing.JButton btnStartSimulation;
        private javax.swing.JButton btn_deleteresult;
        private javax.swing.JButton btn_reChart_left;
        private javax.swing.JButton btn_reChart_right;
        private javax.swing.JCheckBox cbxAS;
        private javax.swing.JCheckBox cbxAccident;
        private javax.swing.JComboBox cbxEndHour;
        private javax.swing.JComboBox cbxEndMin;
        private javax.swing.JComboBox cbxSavedResult;
        private javax.swing.JComboBox cbxSections;
        private javax.swing.JComboBox cbxSimulationCate;
        private javax.swing.JComboBox cbxSimulationInterval;
        private javax.swing.JComboBox cbxSimulationMode;
        private javax.swing.JComboBox cbxStartHour;
        private javax.swing.JComboBox cbxStartMin;
        private javax.swing.JCheckBox cbxUpstream;
        private javax.swing.JComboBox cbxVSLVersion;
        private javax.swing.JComboBox cbxVSSDec;
        private javax.swing.JComboBox cbxVSSSpeed;
        private javax.swing.JComboBox cbx_result_station;
        private javax.swing.JComboBox cbxvissimVersion;
        private javax.swing.JCheckBox chkShowVehicles;
        private javax.swing.JTextArea errorstate;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel12;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel14;
        private javax.swing.JLabel jLabel15;
        private javax.swing.JLabel jLabel16;
        private javax.swing.JLabel jLabel17;
        private javax.swing.JLabel jLabel18;
        private javax.swing.JLabel jLabel19;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel20;
        private javax.swing.JLabel jLabel21;
        private javax.swing.JLabel jLabel22;
        private javax.swing.JLabel jLabel23;
        private javax.swing.JLabel jLabel24;
        private javax.swing.JLabel jLabel25;
        private javax.swing.JLabel jLabel26;
        private javax.swing.JLabel jLabel27;
        private javax.swing.JLabel jLabel28;
        private javax.swing.JLabel jLabel29;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel30;
        private javax.swing.JLabel jLabel31;
        private javax.swing.JLabel jLabel32;
        private javax.swing.JLabel jLabel33;
        private javax.swing.JLabel jLabel34;
        private javax.swing.JLabel jLabel35;
        private javax.swing.JLabel jLabel36;
        private javax.swing.JLabel jLabel37;
        private javax.swing.JLabel jLabel38;
        private javax.swing.JLabel jLabel39;
        private javax.swing.JLabel jLabel40;
        private javax.swing.JLabel jLabel41;
        private javax.swing.JLabel jLabel42;
        private javax.swing.JLabel jLabel43;
        private javax.swing.JLabel jLabel44;
        private javax.swing.JLabel jLabel45;
        private javax.swing.JLabel jLabel46;
        private javax.swing.JLabel jLabel47;
        private javax.swing.JLabel jLabel48;
        private javax.swing.JLabel jLabel49;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel50;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel10;
        private javax.swing.JPanel jPanel11;
        private javax.swing.JPanel jPanel12;
        private javax.swing.JPanel jPanel13;
        private javax.swing.JPanel jPanel14;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPanel jPanel4;
        private javax.swing.JPanel jPanel5;
        private javax.swing.JPanel jPanel6;
        private javax.swing.JPanel jPanel7;
        private javax.swing.JPanel jPanel8;
        private javax.swing.JPanel jPanel9;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JScrollPane jScrollPane3;
        private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
        private javax.swing.JPanel pnDate;
        private javax.swing.JTextArea simstate;
        private javax.swing.JSlider sld_resultChart;
        private javax.swing.JTabbedPane tabPanel;
        private javax.swing.JTextField tbxAb;
        private javax.swing.JTextField tbxAccident;
        private javax.swing.JTextField tbxBSSpeedThreshold;
        private javax.swing.JTextField tbxCaseFile;
        private javax.swing.JTextField tbxDeceleration;
        private javax.swing.JTextField tbxKb;
        private javax.swing.JTextField tbxKc;
        private javax.swing.JTextField tbxKd_Rate;
        private javax.swing.JTextField tbxKjam;
        private javax.swing.JTextField tbxKstop;
        private javax.swing.JTextField tbxMaxRedTime;
        private javax.swing.JTextField tbxMaxWaittingTime;
        private javax.swing.JTextField tbxMaxWaittingTimeF2F;
        private javax.swing.JTextField tbxMinStationDistance;
        private javax.swing.JTextField tbxRandom;
        private javax.swing.JTextField tbxStopDuration;
        private javax.swing.JTextField tbxStopTrend;
        private javax.swing.JTextField tbxStopUpstreamCount;
        private javax.swing.JTextField tbxTimer;
        private javax.swing.JTextField tbxTurnOffAcceleration;
        private javax.swing.JTextField tbxVSLMOVEDec;
        private javax.swing.JTextField tbxVSLMOVESpeed;
        private javax.swing.JTextField tbxVSLSTADISTANCE;
        private javax.swing.JTextField tbxVSLSTASPEED;
        private javax.swing.JTextField tbxVSLZoneLength;
        private javax.swing.JTextField tbxZesDecceleration;
        // End of variables declaration//GEN-END:variables

    @Override
    public void signalEnd(int code) {
        SimulationMode smode = (SimulationMode)this.cbxSimulationMode.getSelectedItem();
        SimulationOption soption = (SimulationOption)this.cbxSimulationCate.getSelectedItem();
        int samples = 0;
        VSLResults vslresult = null;
        if(smode.isSimulationMode()){
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

            samples = simulation.getSamples();
            if(samples < 5) {
                JOptionPane.showMessageDialog(simFrame, "Too short simulation");
                simFrame.afterSimulation(null, null);
                this.simFrame.setVisible(false);            
            }
            
            if(!soption.isMETERING()){
                VSLSim vs = (VSLSim)simulation;
                vslresult = vs.getVSLResults();
            }
        }else if(smode.isEmulationMode()){
            samples = emulation.getSample();
            vslresult = emulation.getVSLResults();
        }
        
        int duration = samples * simulation.getRunningInterval();

        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.set(Calendar.SECOND, 0);
        Date sTime = c.getTime();
        c.add(Calendar.SECOND, duration);
        Date eTime = c.getTime();
        simFrame.afterSimulation((Section)this.cbxSections.getSelectedItem(), new Period(sTime, eTime, simulation.getRunningInterval()));
        
        if(!soption.isMETERING()){
            SimulationUtil.SaveVSLSimulation((Section)this.cbxSections.getSelectedItem(),new Period(sTime, eTime, simulation.getRunningInterval()),simFrame,vslresult);
        }else
            SimulationUtil.SaveSimulation((Section)this.cbxSections.getSelectedItem(),new Period(sTime, eTime, simulation.getRunningInterval()),simFrame);
        
        System.out.println("Restore output redirection ... ");
        loadVSLResults();
        this.restoreOutput();
        isStartSimulation(false);
        this.cbxAccident.setSelected(true);
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
    
    private void setVissimVisible(boolean b) {
        if(simulation != null) {
            simulation.setVissimVisible(b);
        }
    }
    
    private void isStartSimulation(boolean flag){
        if(flag){
            tabPanel.setSelectedIndex(3);
        }else{
            tabPanel.setSelectedIndex(0);
        }
        this.btnStartSimulation.setEnabled(!flag);
        this.btnSelectCasefile.setEnabled(!flag);
        this.cbxSections.setEnabled(!flag);
        this.cbxvissimVersion.setEnabled(!flag);
    }

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

    private void openSectionEditor() {
        tmo.openSectionEditor(this.simFrame, true);
        this.loadSection();
    }

    private void init() {
        SimulationConfig.loadConfig();
        this.tbxCaseFile.setText(SimulationConfig.CASE_FILE);
        this.tbxRandom.setText(String.valueOf(SimulationConfig.RANDOM_SEED));
        /**
         * Load Section
         */
        try{
            this.loadSection();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        /*
         * VissimVersion
         */
        for(VISSIMVersion v : VISSIMVersion.values()){
            this.cbxvissimVersion.addItem(v);
        }
        
        loadSimulationInterval();
        loadVSLMode();
        loadSimulationMode();
        loadSimulationOption();
        loadVSLParameter();
        loadMeteringParameter();
        setGUIEnable();
        initResult();
        initSTAType();
    }
    
    private void saveSimulationConfig() {
        SimulationConfig.CASE_FILE = this.tbxCaseFile.getText();
        SimulationConfig.RANDOM_SEED = Integer.parseInt(this.tbxRandom.getText());
        SimulationConfig.saveConfig();
    }
    
    private void saveMeteringParameter() {
        MeteringConfig.Kjam = Integer.parseInt(this.tbxKjam.getText());
        MeteringConfig.Kc = Integer.parseInt(this.tbxKc.getText());
        MeteringConfig.Kd_Rate = Double.parseDouble(this.tbxKd_Rate.getText());
        MeteringConfig.Kb = Double.parseDouble(this.tbxKb.getText());            
        MeteringConfig.Kstop = Double.parseDouble(this.tbxKstop.getText());            
        MeteringConfig.stopDuration = Integer.parseInt(this.tbxStopDuration.getText());
        MeteringConfig.stopBSTrend = Integer.parseInt(this.tbxStopTrend.getText());
        MeteringConfig.stopUpstreamCount = Integer.parseInt(this.tbxStopUpstreamCount.getText());
        MeteringConfig.Ab = Integer.parseInt(this.tbxAb.getText());                                   
        MeteringConfig.Kd = MeteringConfig.Kc * MeteringConfig.Kd_Rate;
        MeteringConfig.setMaxWaitTimeF2F(Integer.parseInt(this.tbxMaxWaittingTimeF2F.getText()));
        MeteringConfig.setMaxWaitTime(Integer.parseInt(this.tbxMaxWaittingTime.getText()));
        MeteringConfig.MAX_RED_TIME = Integer.parseInt(this.tbxMaxRedTime.getText());
        MeteringConfig.saveConfig();
    }
    
    private void saveVSLParameter(){
        VSLConfig.VSL_VSS_DECISION_ACCEL = Integer.parseInt(this.tbxDeceleration.getText());
        VSLConfig.VSL_CONTROL_THRESHOLD = Integer.parseInt(this.tbxZesDecceleration.getText());
        VSLConfig.VSL_BS_THRESHOLD = Integer.parseInt(this.tbxBSSpeedThreshold.getText());
        VSLConfig.VSL_TURNOFF_ACCEL = Integer.parseInt(this.tbxTurnOffAcceleration.getText());
        VSLConfig.VSL_MIN_STATION_MILE = Double.parseDouble(this.tbxMinStationDistance.getText());
        VSLConfig.VSL_MOVING_ACCEL = Integer.parseInt(this.tbxVSLMOVEDec.getText());
        VSLConfig.VSL_RANGE_THRESHOLD = Double.parseDouble(this.tbxVSLMOVESpeed.getText());
        VSLConfig.AccidentSpeed = Double.parseDouble(this.tbxAccident.getText());
        VSLConfig.isAccidentSpeed = this.cbxAccident.isSelected();
        VSLConfig.VSL_ZONE_LENGTH_MILE = Double.parseDouble(this.tbxVSLZoneLength.getText());
        VSLConfig.coverDistance = Double.parseDouble(this.tbxVSLSTADISTANCE.getText());
        VSLConfig.coverageSpeed = Integer.parseInt(this.tbxVSLSTASPEED.getText());
        saveSpeedValueList();
        VSLConfig.save();
    }
    
    private void saveSpeedValueList() {
        AccCheckThreshold sa = (AccCheckThreshold)this.cbxVSSDec.getSelectedItem();
        VSLConfig.accCheckThreshold = sa;
        
        BottleneckSpeed bs = (BottleneckSpeed)this.cbxVSSSpeed.getSelectedItem();
        VSLConfig.bottleneckSpeedType = bs;
//        SpeedAggregation sa = (SpeedAggregation)this.cbxUAggregation.getSelectedItem();
//        VSLConfig.SPEED_SPEED_AGGREGATION = sa;
//        
//        DensityAggregation da = (DensityAggregation)this.cbxKAggregation.getSelectedItem();
//        da.setValue(Double.parseDouble(this.tbxMovingKAvgCount.getText()));
//        VSLConfig.SPEED_DENSITY_AGGREGATION = da;
//        
//        SpeedforLowK SLK = (SpeedforLowK)this.cbxUforLowK.getSelectedItem();
//        SLK.setValue(Double.parseDouble(this.tbxFixedUforLowK.getText()));
//        VSLConfig.SPEED_SPEED_FOR_LOW_K = SLK;
//        
//        MaxSpeed MS = (MaxSpeed)this.cbxMaxSpeed.getSelectedItem();
//        MS.setValue(Double.parseDouble(this.tbxMaxSpeedAlpha.getText()));
//        VSLConfig.SPEED_MAX_SPEED = MS;
    }

    private void loadVSLParameter() {
        VSLConfig.load();
        this.tbxDeceleration.setText(String.valueOf(VSLConfig.VSL_VSS_DECISION_ACCEL));
        this.tbxZesDecceleration.setText(String.valueOf(VSLConfig.VSL_CONTROL_THRESHOLD));
        this.tbxBSSpeedThreshold.setText(String.valueOf(VSLConfig.VSL_BS_THRESHOLD));
        this.tbxTurnOffAcceleration.setText(String.valueOf(VSLConfig.VSL_TURNOFF_ACCEL));
        this.tbxMinStationDistance.setText(String.valueOf(VSLConfig.VSL_MIN_STATION_MILE));
        this.tbxVSLMOVEDec.setText(String.valueOf(VSLConfig.VSL_MOVING_ACCEL));
        this.tbxVSLMOVESpeed.setText(String.valueOf(VSLConfig.VSL_RANGE_THRESHOLD));
        this.tbxAccident.setText(String.valueOf(VSLConfig.AccidentSpeed));
        this.cbxAccident.setSelected(VSLConfig.isAccidentSpeed);
        this.tbxVSLZoneLength.setText(String.valueOf(VSLConfig.VSL_ZONE_LENGTH_MILE));
        this.tbxVSLSTADISTANCE.setText(String.valueOf(VSLConfig.coverDistance));
        this.tbxVSLSTASPEED.setText(String.valueOf(VSLConfig.coverageSpeed));
        this.setSTAType(VSLConfig.vslSTAtype);
        loadSpeedValueList();
    }
    
    private void loadMeteringParameter(){
        MeteringConfig.loadConfig();
        DecimalFormat df = new DecimalFormat();
        df.setDecimalSeparatorAlwaysShown(false);
        this.tbxKjam.setText(df.format(MeteringConfig.Kjam));
        this.tbxKc.setText(df.format(MeteringConfig.Kc));
        this.tbxKd_Rate.setText(df.format(MeteringConfig.Kd_Rate));
        this.tbxKb.setText(df.format(MeteringConfig.Kb));
        this.tbxKstop.setText(df.format(MeteringConfig.Kstop));
        this.tbxStopDuration.setText(String.format("%d", MeteringConfig.stopDuration));
        this.tbxStopTrend.setText(String.format("%d", MeteringConfig.stopBSTrend));
        this.tbxStopUpstreamCount.setText(String.format("%d", MeteringConfig.stopUpstreamCount));
        this.tbxAb.setText(String.format("%d", MeteringConfig.Ab));
        this.tbxMaxRedTime.setText(df.format(MeteringConfig.MAX_RED_TIME));
        this.tbxMaxWaittingTime.setText(df.format(MeteringConfig.MAX_WAIT_TIME_MINUTE));
        this.tbxMaxWaittingTimeF2F.setText(df.format(MeteringConfig.MAX_WAIT_TIME_MINUTE_F2F));
    }

    private void loadSpeedValueList() {
        for(AccCheckThreshold act : AccCheckThreshold.values()){
            this.cbxVSSDec.addItem(act);
        }
        this.cbxVSSDec.setSelectedIndex(VSLConfig.accCheckThreshold.getSID());
        
        for(BottleneckSpeed act : BottleneckSpeed.values()){
            this.cbxVSSSpeed.addItem(act);
        }
        this.cbxVSSSpeed.setSelectedIndex(VSLConfig.bottleneckSpeedType.getSID());
        cbxVSSSpeed.setSelectedIndex(1);
//        /**
//         * Speed Aggregation
//         */
//        for(SpeedAggregation sa : SpeedAggregation.values()){
//            this.cbxUAggregation.addItem(sa);
//        }
//        this.cbxUAggregation.setSelectedIndex(VSLConfig.SPEED_SPEED_AGGREGATION.getSRC());
//        
//        /**
//         * DensityAggregation
//         */
//        for(DensityAggregation sa : DensityAggregation.values()){
//            this.cbxKAggregation.addItem(sa);
//        }
//        this.cbxKAggregation.setSelectedIndex(VSLConfig.SPEED_DENSITY_AGGREGATION.getSRC());
//        this.tbxMovingKAvgCount.setText(String.valueOf(VSLConfig.SPEED_DENSITY_AGGREGATION.getValue()));
//        setAlphaVisible(this.tbxMovingKAvgCount,VSLConfig.SPEED_DENSITY_AGGREGATION.hasValue());
//        
//        /**
//         * SpeedforLowK
//         */
//        for(SpeedforLowK sa : SpeedforLowK.values()){
//            this.cbxUforLowK.addItem(sa);
//        }
//        this.cbxUforLowK.setSelectedIndex(VSLConfig.SPEED_SPEED_FOR_LOW_K.getSRC());
//        this.tbxFixedUforLowK.setText(String.valueOf(VSLConfig.SPEED_SPEED_FOR_LOW_K.getValue()));
//        setAlphaVisible(this.tbxFixedUforLowK,VSLConfig.SPEED_SPEED_FOR_LOW_K.hasValue());
//        /**
//         * MaxSpeed
//         */
//        for(MaxSpeed sa : MaxSpeed.values()){
//            this.cbxMaxSpeed.addItem(sa);
//        }
//        this.cbxMaxSpeed.setSelectedIndex(VSLConfig.SPEED_MAX_SPEED.getSRC());
//        this.tbxMaxSpeedAlpha.setText(String.valueOf(VSLConfig.SPEED_MAX_SPEED.getValue()));
//        setAlphaVisible(this.tbxMaxSpeedAlpha,VSLConfig.SPEED_MAX_SPEED.hasValue());
    }

    private void setAlphaVisible(JTextField tbx, boolean hasValue) {
        tbx.setEnabled(hasValue);
    }

    private void Process(){
        saveSimulationConfig();
        saveVSLParameter();
        saveMeteringParameter();
        this.redirectOutput(simstate,errorstate);
        SimulationMode smode = (SimulationMode)cbxSimulationMode.getSelectedItem();
        VSLVersion vv = (VSLVersion)cbxVSLVersion.getSelectedItem();
        if(smode.isSimulationMode()){
            StartSimulation(vv);
        }else if(smode.isEmulationMode()){
            StartEmulation(vv);
        }
    }
    private void StartSimulation(VSLVersion vv) {
        try{
            SimulationOption sopt = (SimulationOption)this.cbxSimulationCate.getSelectedItem();
            startTime = new Date();
            Section s = (Section)this.cbxSections.getSelectedItem();
            VISSIMVersion version = (VISSIMVersion)this.cbxvissimVersion.getSelectedItem();
            Interval simIntv = (Interval)this.cbxSimulationInterval.getSelectedItem();
            simulation = sopt.getSimulationOption(SimulationConfig.CASE_FILE,SimulationConfig.RANDOM_SEED,s,version,vv,simIntv);
//            simulation = new VSLSim(SimulationConfig.CASE_FILE,SimulationConfig.RANDOM_SEED,s,version,vv);
            simulation.setSignalListener(this);
            simulation.setChartPanel(this.PanelChart);
            simulation.start();
            isStartSimulation(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void StartEmulation(VSLVersion vv){
        System.out.println("step 0");
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        
        if(selectedDates.length > 1){
            JOptionPane.showMessageDialog(null, "Please Select 1 day..");
            return;
        }
        
        if(selectedDates.length <= 0){
            JOptionPane.showMessageDialog(null, "please Select day..");
            return;
        }
        
        Section section = (Section) this.cbxSections.getSelectedItem();
        if (section == null) {
            return;
        }
        
        Calendar cs,ce;
        cs = (Calendar)selectedDates[0].clone();
        ce = (Calendar)selectedDates[0].clone();
        
        // set period for staring time
//        Calendar cs = this.calStartDate.getSelectedDate();
        int hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        cs.set(Calendar.HOUR_OF_DAY, hour);
        cs.set(Calendar.MINUTE, Integer.parseInt(this.cbxStartMin.getSelectedItem().toString()));
//        cs.set(Calendar.SECOND, 0);
       
        // set period for ending time
//        Calendar ce = this.calEndDate.getSelectedDate();
        hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        ce.set(Calendar.HOUR_OF_DAY, hour);
        ce.set(Calendar.MINUTE, Integer.parseInt(this.cbxEndMin.getSelectedItem().toString()));
        //barelane
        
        Period p = new Period(cs.getTime(),ce.getTime(),VSLConfig.Interval);
        
        startTime = new Date();
        emulation = new VSLEmulation(section,p,vv);
        emulation.setSignalListener(this);
        emulation.start();
        isStartSimulation(true);
    }

    private void setGUIEnable() {
        SpeedAggregationMenu(false);
    }

    private void SpeedAggregationMenu(boolean par) {
//        pnSpeedAggreagation.setEnabled(par);
//        label_SpeedAggregation.setEnabled(par);
//        label_MaxSpeed.setEnabled(par);
//        label_SpeedforLow.setEnabled(par);
//        label_DensityA.setEnabled(par);
//        cbxUAggregation.setEnabled(par);
//        cbxKAggregation.setEnabled(par);
//        cbxUforLowK.setEnabled(par);
//        cbxMaxSpeed.setEnabled(par);
//        tbxMovingKAvgCount.setEnabled(par);
//        tbxFixedUforLowK.setEnabled(par);
//        tbxMaxSpeedAlpha.setEnabled(par);
    }
    
    private void SimulationModeVisible(SimulationMode simulationMode) {
        if(simulationMode.isSimulationMode()){
            setModeVisible(false);
            cbxSimulationCate.setEnabled(true);
        }else if(simulationMode.isEmulationMode()){
            setModeVisible(true);
            if(cbxSimulationCate.getItemCount() > 1)
                this.cbxSimulationCate.setSelectedIndex(1);
            cbxSimulationCate.setEnabled(false);
        }
    }
    
    private void setModeVisible(boolean b) {
        this.pnDate.setVisible(b);
        this.cbxvissimVersion.setEnabled(!b);
        this.tbxRandom.setEnabled(!b);
        this.tbxCaseFile.setEnabled(!b);
        this.btnSelectCasefile.setEnabled(!b);
        this.cbxSimulationInterval.setEnabled(!b);
    }
    
    private void loadVSLMode() {
        if(this.cbxVSLVersion != null)
            this.cbxVSLVersion.removeAllItems();
        
        for(VSLVersion s : VSLVersion.values())
        {
            if(s != null) {
                cbxVSLVersion.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
    }
    private void loadSimulationOption(){
        if(this.cbxSimulationCate != null)
            this.cbxSimulationCate.removeAllItems();
        
        for(SimulationOption s : SimulationOption.values())
        {
            if(s != null) {
                cbxSimulationCate.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
        
        if(cbxSimulationCate.getItemCount() > 1)
            this.cbxSimulationCate.setSelectedIndex(0);
    }
    private void loadSimulationMode() {
        if(this.cbxSimulationMode != null)
            this.cbxSimulationMode.removeAllItems();
        
        for(SimulationMode s : SimulationMode.values())
        {
            if(s != null) {
                cbxSimulationMode.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
        
        if(cbxSimulationMode.getItemCount() > 1)
            this.cbxSimulationMode.setSelectedIndex(0);
    }
    
    private void LoadResultChart() {
        vslresult_chart = (VSLResults)cbxSavedResult.getSelectedItem();
        
        initSlide();
        initResultChart();
        updateResultChart(0);
        isResultLoaded = true;
        loadStationBox();
    }

    private void initSlide() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sld_resultChart.setMaximum(vslresult_chart.getDataLength()-1);
                sld_resultChart.setValue(0);
            }
        }, 0);
    }
    
    private void updateSlide(final int value) {
        updateResultChart(value);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String time = vslresult_chart.getPeriod() == null ? "" : getHowManyTimes(vslresult_chart.getPeriod().startDate,(value+1),
                        vslresult_chart.getPeriod().interval);
                tbxTimer.setText(time+"("+String.valueOf(value+1)+")");
                loadResultDatas();
            }
        }, 0);
    }
    
    public String getHowManyTimes(Date StartDate, int step, int interval)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(StartDate);

        for(int i=0;i<step;i++){
            c.add(Calendar.SECOND, interval);
        }
        
        return c.get(Calendar.HOUR_OF_DAY)+":"+(c.get(Calendar.MINUTE) < 10 ? "0"+c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE))
                +":"+(c.get(Calendar.SECOND) < 10 ? "0"+c.get(Calendar.SECOND) : c.get(Calendar.SECOND));
    }
    
    private void updateSlideWheel(int i) {
        int idx = sld_resultChart.getValue() + i;
        if(sld_resultChart.getMaximum() < idx || 0 > idx){
            return;
        }
        sld_resultChart.setValue(idx);
    }
    
    private void initResultChart() {
        PanelChart_result.removeAll();
        resultchart = new VSLChartXY(vslresult_chart.getMilePointListLayout(),null);
        result_cpn = new ChartPanel(resultchart.getChart());
        result_cpn.setSize(PanelChart_result.getSize());
        PanelChart_result.add(result_cpn);
    }

    private void setChartEnable(boolean b) {
        btnReadResult.setEnabled(b);
        btnLoadExcel.setEnabled(b);
        /**
         * Scroll
         */
        btn_reChart_left.setEnabled(b);
        btn_reChart_right.setEnabled(b);
        sld_resultChart.setEnabled(b);
        tbxTimer.setEnabled(b);
    }

    private void updateResultChart(final int value) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                resultchart.AddVSLResultStationSpeedGraph(vslresult_chart.getMapStations(), "StationSpeed",value);
                resultchart.AddMapDMSSpeedGraph(vslresult_chart.getMapDMSs(), "DMSSpeedLimit",value);
                resultchart.AddMapDMSActualSpeedGraph(vslresult_chart.getMapDMSs(), "DMSAtualSpeedLimit",value);
                resultchart.AddMapDMSSTAGraph(vslresult_chart.getMapDMSs(), "SLOW TRAFFIC AHEAD", value);
                result_cpn.setSize(PanelChart_result.getSize());
                PanelChart_result.getParent().validate();
            }
        }, 0);
    }

    /**
     * about VSL Result Tab
     */
    private void initResult() {
        loadVSLResults();
    }
    
    private void loadVSLResults(){
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                loadVSLSimulationResults();
            }
        },5);
    }
    
    private void loadVSLSimulationResults() {
        setChartEnable(false);
        this.cbxSavedResult.setEnabled(false);
        if(this.cbxSavedResult != null)
            this.cbxSavedResult.removeAllItems();
        cbxSavedResult.addItem("Loading Datas.....");
        
        ArrayList<VSLResults> res = SimulationUtil.loadVSLSimulationResults();
        
        if(this.cbxSavedResult != null)
            this.cbxSavedResult.removeAllItems();
        
        if(res == null){
            return;
        }
        for(VSLResults s : res)
        {
            if(s != null) {
                cbxSavedResult.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
        this.cbxSavedResult.setEnabled(true);
        setChartEnable(true);
    }

    private void extractExcel() {
        final VSLResults res = (VSLResults)cbxSavedResult.getSelectedItem();
        
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                VSLResultExtractor vex = new VSLResultExtractor(res);
                vex.run();
            }
        },5);
    }

    /**
     * Load Station ComboBox
     */
    private void loadStationBox() {
        if(!this.isResultLoaded){
            return;
        }
        
        if(cbx_result_station != null){
            this.cbx_result_station.removeAllItems();
        }
        
        for(VSLResultStation vs : vslresult_chart.getStations().values()){
            cbx_result_station.addItem(vs);
        }
    }
    
    private void deleteSelectedResult(VSLResults vslResults) {
        if(vslResults != null){
            SimulationUtil.DeleteVSLSimulationResult(vslResults);
            loadVSLResults();
        }
    }

    /**
     * load Data into Table
     * @param vslResultStation 
     */
    private void loadResultDatas() {
        if(!this.isResultLoaded){
            return;
        }
        
        VSLResultStation vslResultStation = (VSLResultStation)cbx_result_station.getSelectedItem();
        
        if(vslResultStation == null){
            return;
        }
        
        ArrayList<VSLResultStation> list = vslresult_chart.getNearStationbyID(vslResultStation.getID());
        
        if(list == null){
            return;
        }
        /**
         * Set Title
         */
        String[] columns = new String[list.size()+1];
        columns[0] = "";
        for(int i=0; i<list.size();i++){
            VSLResultStation vs = list.get(i);
            columns[i+1] = vs.getID();
        }
        
        DefaultTableModel tmodel = new DefaultTableModel();
        tmodel.setColumnIdentifiers(columns);
        Result_table.setModel(tmodel);
        
        DefaultTableModel rows = (DefaultTableModel)Result_table.getModel();
        String[] rowTitle = new String[]{"Speed","Acc","ControlAcc","VSS","PVSS","Count","STA"};
        int idx = this.sld_resultChart.getValue();
        
        for(int i=0; i<rowTitle.length;i++){
            Vector<Object> data = new Vector<Object>();
            data.add(rowTitle[i]);
            for(VSLResultStation vs : list){
                switch(i){
                    case 0 :
                        data.add(vs.getRollingSpeeds()[idx]);
                        break;
                    case 1 :
                        data.add(vs.getAcceleration()[idx]);
                        break;
                    case 2 :
                        data.add(vs.getControlThreshold()[idx]);
                        break;
                    case 3 :
                        data.add(vs.getCurrentVSS()[idx]);
                        break;
                    case 4 : 
                        data.add(vs.getPreviousVSS()[idx]);
                        break;
                    case 5 :
                        data.add(vs.getBottleneckCounts()[idx]);
                        break;    
                }
            }
            rows.addRow(data);
        }
//        
//        DefaultTableModel rows = (DefaultTableModel)tbRate.getModel();
//        for(int i=0;i<totalrows;i++){
//            int timeinterval = (i)*interval;
//            Vector<Double> data = new Vector<Double>();
//            
//            //Add interval
//            data.add((double)timeinterval);
//            
//            //Add Data
//            if(fmg != null){
//                for(FixedMeter fmeter : fmg.getFixedMeters()){
//                    if(i < fmeter.rate.size())
//                        data.add(fmeter.rate.get(i));
//                }
//            }
//            rows.addRow(data);
//        }
    }

        private void loadSimulationInterval() {
                for(Interval i : Interval.values()){
                        this.cbxSimulationInterval.addItem(i);
                }
                
                this.cbxSimulationInterval.setSelectedItem(Interval.get(SimulationConfig.RunningInterval));
        }
        
        private void initSTAType() {
                setSTAType(VSLSTAType.OPTION2);
            }
        
        private void setSTAType(VSLSTAType vslstaType) {
                this.STAType = vslstaType;
                if(STAType.isOption1()){
                    this.cbxAS.setSelected(true);
                    this.cbxUpstream.setSelected(false);
                    this.tbxVSLSTADISTANCE.setEnabled(true);
                    this.tbxVSLSTASPEED.setEnabled(true);
                }else{
                    this.cbxAS.setSelected(false);
                    this.cbxUpstream.setSelected(true);
                    this.tbxVSLSTADISTANCE.setEnabled(false);
                    this.tbxVSLSTASPEED.setEnabled(false);
                }
        }

    
}
