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

package edu.umn.natsrl.sfim;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.sfim.comm.CommProtocol;
import edu.umn.natsrl.sfim.comm.InfoCommLink;
import edu.umn.natsrl.sfim.comm.InfoController;
import edu.umn.natsrl.sfim.comm.InfoTimingPlan;
import edu.umn.natsrl.ticas.Simulation.SimulationUtil;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.StreamLogger;
import edu.umn.natsrl.util.StringOutputStream;
import edu.umn.natsrl.vissimcom.VISSIMHelper;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Chongmyung Park
 */
public class SFIMPanel extends javax.swing.JPanel {
    
    private SFIMPanel thisPanel;
//    private IRISDB_3_128 idb;
    private IRISDB_3_140 idb;
    private Vector<InfoCommLink> clist;
    private SFIMManager manager;
    private Vector<InfoTimingPlan> plans;
    private final int START_PORT = SFIMConfig.SERVER_PORT_START;
    private int port;
    private StreamLogger remoteErrLog;
    private PrintStream backupOut;
    private PrintStream backupErr;
    private RemoteDebug errorDebug;
    private RemoteDebug outDebug;
    private StringOutputStream sosOut;
    private StringOutputStream sosErr;
    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    private Date sTime;
    private PluginFrame simFrame;
    private String curDirectory = ".";
    
    private double mainSplitRate = 0.8;
    private double outputSplitRate = 0.5;
    private boolean stopped = false;
    
    /**
     * Initialize panel
     * @param manager 
     */
    public boolean init(SFIMManager manager) {        
        
        try {
            
            SFIMConfig.loadConfig();            
            
            SFIMConfigDialog scd = new SFIMConfigDialog(null, true);
            scd.setLocationRelativeTo(this);
            scd.setVisible(true);
            
            SFIMConfig.loadConfig();
                    
//            idb = new IRISDB_3_128();
            idb = new IRISDB_3_140();
            
            this.manager = manager;
            this.manager.setSfimPanel(this);

            IRISController.stopIRIS();

            System.out.print("Loading timing plans from IRIS server ...");
            plans = idb.getTimingPlans();
            for (InfoTimingPlan p : plans) {
                this.cbxMeteringAlgorithm.addItem(p.description);
            }

            // set default value
            this.cbxMeteringAlgorithm.setSelectedIndex(2);
            System.out.println(" (OK)");

            this.loadSection();

            this.tbxCaseFile.setText(SFIMConfig.caseFile);
            this.tbxRandom.setText(String.format("%d", SFIMConfig.randomSeed));
            this.tbxSimDuration.setText(String.format("%d", SFIMConfig.simulationDuration));
            long timestamp = SFIMConfig.simulationTime;
            if(timestamp > 0) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(timestamp);
                this.dcSimStartDate.setSelectedDate(c);
                this.tbxSimStartHour.setText(String.format("%d", c.get(Calendar.HOUR_OF_DAY)));
                this.tbxSimStartMin.setText(String.format("%d", c.get(Calendar.MINUTE)));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            SFIMConfigDialog scd = new SFIMConfigDialog(null, true);
            scd.setLocationRelativeTo(this);
            scd.setVisible(true);
            JOptionPane.showMessageDialog(this, "Restart SFIM!!");
            return false;
        }
        return true;
    }

    /**
     * Redirect output into log box
     */
    public void redirectOutput() {
        backupOut = System.out;
        backupErr = System.err;
        // redirect System.out and System.err to log textbox
        StringOutputStream sos = new StringOutputStream(this.tbxLog);
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

    /**
     * Start simulation
     * Creates comm_links and simulation is automatically started
     */
    private void start() {
        
        redirectOutput();
        this.stopped = false;
        int simStartHour = 0;
        int simStartMin = 0;
        int simDuration = 0;
        SFIMConfig.DEBUG_IRIS_STATION = this.chkPrintStation.isSelected();
        SFIMConfig.DEBUG_IRIS_METER = this.chkPrintMeter.isSelected();
        Calendar c = this.dcSimStartDate.getCurrent();        
//        Calendar c = Calendar.getInstance();
        try {
            simStartHour = Integer.parseInt(this.tbxSimStartHour.getText());
            simStartMin = Integer.parseInt(this.tbxSimStartMin.getText());
            simDuration = Integer.parseInt(this.tbxSimDuration.getText());
            c.set(Calendar.HOUR_OF_DAY, simStartHour);
            c.set(Calendar.MINUTE, simStartMin);
            c.set(Calendar.SECOND, 0);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Put integer value to simulation start time");
            return;
        }
                
        Section section = (Section)this.cbxSections.getSelectedItem();        
        manager.start(section, c, simDuration);        
        saveConfig();
        
        idb.configIRISDB();
        
        // start remote debug
        if(outDebug == null) {
            sosOut = new StringOutputStream(this.tbxRemoteLog);
            outDebug = new RemoteDebug(sosOut, SFIMConfig.REMOTE_OUT_PORT);
            System.out.println("Remote output debugger started ("+SFIMConfig.REMOTE_OUT_PORT+")");
            outDebug.start();
        }

        if(errorDebug == null) {
            sosErr = new StringOutputStream(this.tbxErrors);
//            try {
//                remoteErrLog = new StreamLogger(Logger.getLogFilePath("Iris-Error-Log"));
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            errorDebug = new RemoteDebug(remoteErrLog, SFIMConfig.REMOTE_ERR_PORT);
            errorDebug = new RemoteDebug(sosErr, SFIMConfig.REMOTE_ERR_PORT);
            System.out.println("Remote error debugger started ("+SFIMConfig.REMOTE_ERR_PORT+")");
            errorDebug.start();            
        }        
        
        this.tabMain.setSelectedIndex(1);
        port = START_PORT;
        sTime = new Date();
        final SFIMSectionHelper sectionHelper = new SFIMSectionHelper((Section)this.cbxSections.getSelectedItem(), this.tbxCaseFile.getText());

        new Timer().schedule(new TimerTask() {
                        
            @Override
            public void run() {
                String caseFile = tbxCaseFile.getText();
                int randomSeed = SFIMConfig.randomSeed;
                try {
                    randomSeed = Integer.parseInt(tbxRandom.getText());
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(thisPanel, "Random seed must be integer value");
                    return;
                }
                // load case file information : detector, desired speed decision and signal group
                ArrayList<String> detectors = VISSIMHelper.loadDetectorsFromCasefile(caseFile);
                ArrayList<String> dsds = VISSIMHelper.loadDesiredSpeedDecisionsFromCasefile(caseFile);
                ArrayList<String> sgs = VISSIMHelper.loadSignalGroupsFromCasefile(caseFile);
                
               if (detectors == null) {
                    JOptionPane.showMessageDialog(thisPanel, "Select correct case file");
                    return;
                }                
                
                SimObjects.getInstance().reset();

                manager.setCaseFile(caseFile);
                manager.setRandomSeed(randomSeed);
                manager.setSectionHelper(sectionHelper);
                manager.setStartTime(new Date());
                manager.setUseMetering(chkMetering.isSelected());
                manager.setUseVSA(chkVSA.isSelected());
                System.out.print("Loading comm_link information from IRIS server ...");
                clist = idb.loadCommLinks();
                System.out.println(" (OK)");

                // decide comm_link that includes device in case file as activeLinks
                Vector<InfoCommLink> activeLinks = new Vector<InfoCommLink>();

                ArrayList<String> TempList = new ArrayList<String>();
                ArrayList<String> CtrlList = new ArrayList<String>();
                System.out.print("Making active comm_links ...");
                
//                /*
//                 * soobin
//                 */
//                for (InfoCommLink ic : clist){
//                    System.out.println("Llist ->"+ic.name);
//                }
                
                for (InfoCommLink ic : clist) {
                    Vector<InfoController> ctrls = idb.loadControllers(ic.name);
                    // continue if no VSA and protocol type is DMS
                    if(!chkVSA.isSelected() && CommProtocol.getCommProtocol(ic.protocol).isDMSLite()) {
                        continue;
                    } 
                    boolean isInCase = false;

                    
                    for (InfoController ctr : ctrls) {
                        for (String io : ctr.IOs) {
                            if (detectors.contains(io)) {
//                                System.out.println("Selected Detector :" + io + ", ctrl = " + ctr.name);
                                isInCase = true;
                            } else if (dsds.contains(io)) {
                                isInCase = true;
                            } else if (sgs.contains(io) || sgs.contains(io + "_L")) {
                                isInCase = true;
                            }else{
                                /*
                                 * soobin
                                 */
                                if(!TempList.contains(io)){
                                    TempList.add(io);
                                    CtrlList.add(ctr.name);
                                }
                            }
                            
                            if (isInCase) {
                                break;
                            }
                        }
                        if (isInCase) {
                            break;
                        }
                    }

                    if (isInCase) {
                        ic.serverPort = getNextPort();
                        System.out.println("Active Link : " + ic.name + "("+ic.protcolName+"),desc="+ic.description);
                        activeLinks.add(ic);
                    }
                }
//                /*
//                 * soobin
//                 */
//                for(int q=0;q<TempList.size();q++){
//                    System.out.println("Non Selected Detector :" + TempList.get(q) + ", ctrl = " + CtrlList.get(q));
//                }
//                System.out.println(" (OK)");

                manager.setTimingPlan(plans.get(cbxMeteringAlgorithm.getSelectedIndex()).id);

                // create commlinks
                System.out.print("Initializing simulation ...");
                manager.simulationInitialize();
                manager.setVissimVisible(chkShowVehicles.isSelected());
                long runTime = manager.simulationInitialRun();
                System.out.println(" (OK - "+runTime+" seconds)");

                System.out.println("Starting commlinks ...");
                //3.140 updating..
                manager.makeCommLinks(activeLinks, idb);
                idb.updateCommLinks(activeLinks, manager,detectors,dsds,sgs);
                System.out.println("Commlinks are started");
                
                IRISController.startIRIS();
            }
        }, 10);

        this.btnStartStop.setText("Running simulation");
        this.btnCaseFile.setEnabled(false);
        this.tbxCaseFile.setEditable(false);
        this.cbxSections.setEnabled(false);
        this.btnOpenSectionEditor.setEnabled(false);
        this.btnSectionInfo.setEnabled(false);
        this.chkMetering.setEnabled(false);
        this.chkVSA.setEnabled(false);
        this.cbxMeteringAlgorithm.setEnabled(false);
        this.btnStartStop.setEnabled(false);
        this.tbxRandom.setEnabled(false);
        this.tbxSimDuration.setEditable(false);
        this.tbxSimStartHour.setEditable(false);
        this.tbxSimStartMin.setEditable(false);
        this.dcSimStartDate.setEnabled(false);
        this.chkPrintMeter.setEnabled(false);
        this.chkPrintStation.setEnabled(false);
        
//        simFrame.setLocation(0,0);
        simFrame.setExtendedState(simFrame.MAXIMIZED_BOTH);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                splitConsoleMainPane.setDividerLocation(mainSplitRate);          
                splitConsoleOutputPane.setDividerLocation(outputSplitRate);             
            }
        });        
    }

    /**
     * Stop simulation
     * Close all servers
     */
    private void stop() {       
        if(stopped) return;
        try {
            this.stopped = true;
            manager.stop();
            this.sosOut = null;
            this.sosErr = null;
            this.errorDebug.close();
            this.outDebug.close();        
            this.remoteErrLog.close();
            this.remoteErrLog = null;
        } catch (Exception ex) {
            
        }
        
        this.btnStartStop.setText("Finished");
        System.err.println("SFIMPanel is closed");        
    }
    


    /**
     * Get available comm_link port
     * @return 
     */
    private int getNextPort() {
        do {
            boolean available = isPortAvailable(port++);
            if (available) {
                return port - 1;
            }
        } while (true);
    }

    /**
     * Check port availability
     * @param port port to check
     * @return
     */
    private boolean isPortAvailable(int port) {
        try {
            ServerSocket srv = new ServerSocket(port);
            srv.setReuseAddress(true);
            srv.close();
            srv = null;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save log into text file
     * @param textArea 
     */
    private void saveLog(JTextArea textArea) {
        String filepath = FileHelper.chooseFileToSave(this.curDirectory, "Choose file to save log", FileHelper.FileFilterForText);        
        if(filepath == null) return;
        filepath = FileHelper.checkExtension(filepath, FileHelper.FileFilterForText);
        this.curDirectory = new File(filepath).getAbsolutePath();
        try {
            FileHelper.writeTextFile(textArea.getText(), filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Save simulation parameters
     */
    private void saveConfig() {       
        SFIMConfig.caseFile = this.tbxCaseFile.getText();
        try {
            SFIMConfig.randomSeed = Integer.parseInt(this.tbxRandom.getText());
            SFIMConfig.simulationDuration = Integer.parseInt(this.tbxSimDuration.getText());

            int simStartHour = 0;
            int simStartMin = 0;
            Calendar c = this.dcSimStartDate.getCurrent();        
            simStartHour = Integer.parseInt(this.tbxSimStartHour.getText());
            simStartMin = Integer.parseInt(this.tbxSimStartMin.getText());
            c.set(Calendar.HOUR_OF_DAY, simStartHour);
            c.set(Calendar.MINUTE, simStartMin);
            c.set(Calendar.SECOND, 0);   
            SFIMConfig.simulationTime = c.getTimeInMillis();
            
        } catch(Exception ex) {
        }
        SFIMConfig.saveConfig();
    }    


    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        this.sections.clear();
        SectionManager sm = tmo.getSectionManager();
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
        if(simFrame != null) tmo.openSectionEditor(simFrame, true);
        else tmo.openSectionEditor(SFIMFrame.getInstance(), true);
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
    
    /** Creates new form SFIMPanel */
    public SFIMPanel() {
        initComponents();
        thisPanel = this;
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {}

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                stop();
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
    }

    /**
     * Returns section
     */
    public Section getSection() {
        return (Section)this.cbxSections.getSelectedItem();
    }

    /**
     * Returns period
     * @return 
     */
    public Period getPeriod() {
        return manager.getPeriod();
    }

    public void signalSimulationEnd() {
//        int elapsedSeconds = (int)( (new Date().getTime() - sTime) / 1000 );
//        int h = elapsedSeconds / 3600;
//        int m = ( elapsedSeconds % 3600 ) / 60;
//        int s = ( elapsedSeconds % 3600 ) % 60;
//        System.out.println("!! Run time="+String.format("%02d", h)+":"+String.format("%02d", m)+":"+String.format("%02d", s));        
        int samples = manager.getSamples();
        
        if(samples < 5) {
            JOptionPane.showMessageDialog(simFrame, "Too short simulation");
            simFrame.afterSimulation(null, null);
            this.simFrame.setVisible(false);            
        }
        int duration = samples * 30;
        
        Calendar c = Calendar.getInstance();
        c.setTime(sTime);
        c.set(Calendar.SECOND, 0);
        Date startTime = c.getTime();
        c.add(Calendar.SECOND, duration);
        Date eTime = c.getTime();
        
        this.stop();
        if(simFrame != null){
            simFrame.afterSimulation(this.getSection(), this.getPeriod());
            SimulationUtil.SaveSimulation((Section)this.cbxSections.getSelectedItem(),new Period(startTime, eTime, 30),simFrame);
        }
    }

    void setSimulationFrame(PluginFrame frame) {
        this.simFrame = frame;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        tabMain = new javax.swing.JTabbedPane();
        panSimulation = new javax.swing.JPanel();
        btnStartStop = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnCaseFile = new javax.swing.JButton();
        tbxCaseFile = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tbxRandom = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cbxSections = new javax.swing.JComboBox();
        btnSectionInfo = new javax.swing.JButton();
        btnOpenSectionEditor = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        chkMetering = new javax.swing.JCheckBox();
        chkVSA = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        cbxMeteringAlgorithm = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        dcSimStartDate = new datechooser.beans.DateChooserCombo();
        tbxSimStartHour = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        tbxSimStartMin = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        tbxSimDuration = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        chkPrintStation = new javax.swing.JCheckBox();
        chkPrintMeter = new javax.swing.JCheckBox();
        panLog = new javax.swing.JPanel();
        chkShowVehicles = new javax.swing.JCheckBox();
        splitConsoleMainPane = new javax.swing.JSplitPane();
        splitConsoleOutputPane = new javax.swing.JSplitPane();
        panOutputSfim = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxLog = new javax.swing.JTextArea();
        btnSaveLog = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        panOutputIris = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbxRemoteLog = new javax.swing.JTextArea();
        btnSaveRemoteLog = new javax.swing.JButton();
        btnRemoteClear = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbxErrors = new javax.swing.JTextArea();

        jLabel5.setText("jLabel5");

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        tabMain.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        panSimulation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        btnStartStop.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnStartStop.setText("Start Simulation");
        btnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Simulation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel1.setText("VISSIM Case File");

        btnCaseFile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnCaseFile.setText("Browse");
        btnCaseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaseFileActionPerformed(evt);
            }
        });

        tbxCaseFile.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel8.setText("Random Number");

        tbxRandom.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRandom.setText("13");
        tbxRandom.setPreferredSize(new java.awt.Dimension(59, 25));

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel3.setText("Section");

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSectionInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenSectionEditor))
                    .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(tbxCaseFile, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tbxCaseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCaseFile))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSectionInfo)
                    .addComponent(btnOpenSectionEditor))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(tbxRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IRIS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel4.setText("Traffic Policy");

        chkMetering.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkMetering.setSelected(true);
        chkMetering.setText("Metering");

        chkVSA.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkVSA.setText("VSL");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel2.setText("Metering Algorithm");

        cbxMeteringAlgorithm.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel9.setText("Time");

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel13.setText("From");

        tbxSimStartHour.setText("6");

        jLabel10.setText(":");

        tbxSimStartMin.setText("0");

        jLabel11.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel11.setText("for");

        tbxSimDuration.setText("3");

        jLabel12.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel12.setText("hour");

        jLabel14.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel14.setText("Options");

        chkPrintStation.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkPrintStation.setSelected(true);
        chkPrintStation.setText("debug station info from IRIS");

        chkPrintMeter.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkPrintMeter.setSelected(true);
        chkPrintMeter.setText("debug ramp meter info from IRIS");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(chkMetering)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkVSA))
                    .addComponent(jLabel2)
                    .addComponent(cbxMeteringAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dcSimStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tbxSimStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxSimStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(243, 243, 243)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tbxSimDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel12))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(chkPrintStation))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(chkPrintMeter)))
                .addContainerGap(446, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(chkMetering)
                    .addComponent(chkVSA))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxMeteringAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dcSimStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbxSimStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10)
                        .addComponent(tbxSimStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tbxSimDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)
                        .addComponent(jLabel11)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(chkPrintStation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkPrintMeter))
                    .addComponent(jLabel14))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panSimulationLayout = new javax.swing.GroupLayout(panSimulation);
        panSimulation.setLayout(panSimulationLayout);
        panSimulationLayout.setHorizontalGroup(
            panSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnStartStop, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panSimulationLayout.setVerticalGroup(
            panSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnStartStop, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabMain.addTab("Simulation", panSimulation);

        chkShowVehicles.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkShowVehicles.setText("show vehicles");
        chkShowVehicles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowVehiclesActionPerformed(evt);
            }
        });

        splitConsoleMainPane.setDividerLocation(500);
        splitConsoleMainPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        splitConsoleOutputPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        splitConsoleOutputPane.setDividerLocation(300);

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel6.setText("SFIM");

        tbxLog.setColumns(20);
        tbxLog.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        tbxLog.setLineWrap(true);
        tbxLog.setRows(5);
        jScrollPane1.setViewportView(tbxLog);

        btnSaveLog.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnSaveLog.setText("Save");
        btnSaveLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveLogActionPerformed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panOutputSfimLayout = new javax.swing.GroupLayout(panOutputSfim);
        panOutputSfim.setLayout(panOutputSfimLayout);
        panOutputSfimLayout.setHorizontalGroup(
            panOutputSfimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panOutputSfimLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panOutputSfimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addGroup(panOutputSfimLayout.createSequentialGroup()
                        .addGroup(panOutputSfimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(btnClear))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 150, Short.MAX_VALUE)
                        .addComponent(btnSaveLog)))
                .addContainerGap())
        );
        panOutputSfimLayout.setVerticalGroup(
            panOutputSfimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panOutputSfimLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panOutputSfimLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveLog, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear))
                .addContainerGap())
        );

        splitConsoleOutputPane.setLeftComponent(panOutputSfim);

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel7.setText("IRIS");

        tbxRemoteLog.setColumns(20);
        tbxRemoteLog.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        tbxRemoteLog.setLineWrap(true);
        tbxRemoteLog.setRows(5);
        jScrollPane2.setViewportView(tbxRemoteLog);

        btnSaveRemoteLog.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnSaveRemoteLog.setText("Save");
        btnSaveRemoteLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveRemoteLogActionPerformed(evt);
            }
        });

        btnRemoteClear.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnRemoteClear.setText("Clear");
        btnRemoteClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoteClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panOutputIrisLayout = new javax.swing.GroupLayout(panOutputIris);
        panOutputIris.setLayout(panOutputIrisLayout);
        panOutputIrisLayout.setHorizontalGroup(
            panOutputIrisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panOutputIrisLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panOutputIrisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panOutputIrisLayout.createSequentialGroup()
                        .addComponent(btnSaveRemoteLog)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 372, Short.MAX_VALUE)
                        .addComponent(btnRemoteClear)))
                .addContainerGap())
        );
        panOutputIrisLayout.setVerticalGroup(
            panOutputIrisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panOutputIrisLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panOutputIrisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveRemoteLog, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoteClear))
                .addContainerGap())
        );

        splitConsoleOutputPane.setRightComponent(panOutputIris);

        splitConsoleMainPane.setTopComponent(splitConsoleOutputPane);

        tbxErrors.setColumns(20);
        tbxErrors.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        tbxErrors.setLineWrap(true);
        tbxErrors.setRows(5);
        jScrollPane3.setViewportView(tbxErrors);

        splitConsoleMainPane.setRightComponent(jScrollPane3);

        javax.swing.GroupLayout panLogLayout = new javax.swing.GroupLayout(panLog);
        panLog.setLayout(panLogLayout);
        panLogLayout.setHorizontalGroup(
            panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panLogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkShowVehicles)
                    .addComponent(splitConsoleMainPane))
                .addContainerGap())
        );
        panLogLayout.setVerticalGroup(
            panLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panLogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkShowVehicles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitConsoleMainPane, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabMain.addTab("Console", panLog);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMain)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMain)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCaseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCaseFileActionPerformed
        String cur = this.tbxCaseFile.getText();
        if(cur == null || cur.isEmpty()) cur = ".";
        String filename = FileHelper.chooseFileToOpen(cur, "Open VISSIM Case File", new FileFilter[]{FileHelper.FileFilterForVISSIM});
        if(filename != null) this.tbxCaseFile.setText(filename);
    }//GEN-LAST:event_btnCaseFileActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        tbxLog.setText("");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopActionPerformed
        start();
    }//GEN-LAST:event_btnStartStopActionPerformed

    private void btnSaveLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveLogActionPerformed
        saveLog(this.tbxLog);
    }//GEN-LAST:event_btnSaveLogActionPerformed

    private void btnSaveRemoteLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveRemoteLogActionPerformed
        saveLog(this.tbxRemoteLog);
    }//GEN-LAST:event_btnSaveRemoteLogActionPerformed

    private void btnRemoteClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoteClearActionPerformed
        tbxRemoteLog.setText("");
    }//GEN-LAST:event_btnRemoteClearActionPerformed

    private void chkShowVehiclesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowVehiclesActionPerformed
        manager.setVissimVisible(this.chkShowVehicles.isSelected());
    }//GEN-LAST:event_chkShowVehiclesActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
        
}//GEN-LAST:event_cbxSectionsActionPerformed

    private void btnSectionInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSectionInfoActionPerformed
        this.openSectionInfoDialog();
}//GEN-LAST:event_btnSectionInfoActionPerformed

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        this.openSectionEditor();
}//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        
    }//GEN-LAST:event_formComponentHidden

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        this.splitConsoleMainPane.setDividerLocation(this.mainSplitRate);          
        this.splitConsoleOutputPane.setDividerLocation(this.outputSplitRate);             
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCaseFile;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnRemoteClear;
    private javax.swing.JButton btnSaveLog;
    private javax.swing.JButton btnSaveRemoteLog;
    private javax.swing.JButton btnSectionInfo;
    private javax.swing.JButton btnStartStop;
    private javax.swing.JComboBox cbxMeteringAlgorithm;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JCheckBox chkMetering;
    private javax.swing.JCheckBox chkPrintMeter;
    private javax.swing.JCheckBox chkPrintStation;
    private javax.swing.JCheckBox chkShowVehicles;
    private javax.swing.JCheckBox chkVSA;
    private datechooser.beans.DateChooserCombo dcSimStartDate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panLog;
    private javax.swing.JPanel panOutputIris;
    private javax.swing.JPanel panOutputSfim;
    private javax.swing.JPanel panSimulation;
    private javax.swing.JSplitPane splitConsoleMainPane;
    private javax.swing.JSplitPane splitConsoleOutputPane;
    private javax.swing.JTabbedPane tabMain;
    private javax.swing.JTextField tbxCaseFile;
    private javax.swing.JTextArea tbxErrors;
    private javax.swing.JTextArea tbxLog;
    private javax.swing.JTextField tbxRandom;
    private javax.swing.JTextArea tbxRemoteLog;
    private javax.swing.JTextField tbxSimDuration;
    private javax.swing.JTextField tbxSimStartHour;
    private javax.swing.JTextField tbxSimStartMin;
    // End of variables declaration//GEN-END:variables

}
