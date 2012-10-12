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

import edu.umn.natsrl.evaluation.EvaluationCheckBox;
import edu.umn.natsrl.evaluation.ContourPanel;
import edu.umn.natsrl.evaluation.Evaluation;
import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.evaluation.OptionType;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.infra.section.SectionChangedStatus;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.evaluation.ContourPlotter;
import edu.umn.natsrl.evaluation.ContourType;
import edu.umn.natsrl.evaluation.EvaluationForCalibration;
import edu.umn.natsrl.evaluation.EvaluationOption;
import edu.umn.natsrl.evaluation.OutputDirection;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.section.ISectionChanged;
import edu.umn.natsrl.infra.section.SectionEditorPanel;
import edu.umn.natsrl.infra.simobjects.RandomSeed;
import edu.umn.natsrl.infra.simobjects.SimDetector;
import edu.umn.natsrl.infra.simobjects.SimulationSeveralResult;
import edu.umn.natsrl.sfim.SfimTicasPlugin;
import edu.umn.natsrl.ticas.plugin.ITicasAfterSimulation;
import edu.umn.natsrl.ticas.plugin.PluginInfo;
import edu.umn.natsrl.ticas.plugin.PluginInfoLoader;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.PluginType;
import edu.umn.natsrl.ticas.plugin.metering.MeteringSimulation;
import edu.umn.natsrl.infraviewer.InfraViewerPlugin;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import edu.umn.natsrl.ticas.Simulation.SimulationUtil;
import edu.umn.natsrl.ticas.error.ErrorMessage;
import edu.umn.natsrl.ticas.error.StringErrorStream;
import edu.umn.natsrl.ticas.plugin.VideoChecker.VideoChecker;
import edu.umn.natsrl.ticas.plugin.datareader.DataReader;
import edu.umn.natsrl.ticas.plugin.detecterdatareader.DetecterDataReader;
import edu.umn.natsrl.ticas.plugin.fixedmetering.FixedMeteringSimulation;
import edu.umn.natsrl.ticas.plugin.rampmeterevaluator.RampMeterEvaluatorPlugin;
import edu.umn.natsrl.ticas.plugin.simulation.basicmetering.BasicMetering;
import edu.umn.natsrl.ticas.plugin.srte.TICASPluginSRTE;
import edu.umn.natsrl.ticas.plugin.srte.TestFrame;
import edu.umn.natsrl.ticas.plugin.vissimcalibration2.VissimCalibration2;
import edu.umn.natsrl.ticas.plugin.traveltimeIndexer.TTIndexterPlugin;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Chongmyung Park
 */
public class TICAS2FrameLab extends javax.swing.JFrame implements ITicasAfterSimulation {
    //temporary empty - soobin Jeon
    static SplashDialog sd = new SplashDialog(null, true);
    static TMO tmoInit = TMO.getInstance();
    
    public static void main(String[] args) {
       
        //InfraConstants.TRAFFIC_DATA_URL = "http://iris.byfun.com/trafdat";

        //InfraConstants.TRAFFIC_DATA_URL = "http://iris.byfun.com/trafdat";
        TICASOption opt = TICASOption.load("ticas.cfg");
        if (opt.isLoaded()) {
//            InfraConstants.TRAFFIC_CONFIG_URL = opt.getTrafficConfigUrl();
//            InfraConstants.TRAFFIC_DATA_URL = opt.getTrafficDataUrl();
        }
        
        // create ticas frame
        final TICAS2FrameLab ticas = new TICAS2FrameLab(tmoInit);

        // timer for splash window
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                TICASVersion.CheckVersion();
                // setup TMO object after 10ms
                tmoInit.setup();
                               
                // clear all chaches older than 365 days
                //tmo.clearAllCache(365);
                
                // run ticas frame
               /*
                * temporary empty - soobin Jeon
                */
                ticas.init();
                ticas.setAlwaysOnTop(true);
                ticas.setVisible(true);

                // close splash window dialog
                sd.dispose();

                ticas.setAlwaysOnTop(false);
            }
        }, 10);

        // show splash window dialog
        /*
        * temporary empty - soobin Jeon
        */
        sd.setAlwaysOnTop(false);
        sd.setVisible(true);
    }
    
    private final String OPTION_FILE = "ticas.cfg";
    private TMO tmo;
    private Vector<Section> sections = new Vector<Section>();
    private Vector<PluginInfo> pluginInfos = new Vector<PluginInfo>();
    private final int width = 990;
    private final int height = 760;
    private ContourPanel speedContourSetting = new ContourPanel(11, 55, true);
    private ContourPanel totalFlowContourSetting = new ContourPanel(11, 6000, false);
    private ContourPanel occupancyContourSetting = new ContourPanel(11, 100, false);
    private ContourPanel densityContourSetting = new ContourPanel(9, 150, false);   

    private Section simSection;
    private Period simPeriod;
    
    private SimulationResult selectedSimulationResult;
   
    private boolean addSFIMPlugin = true;
    private boolean addSimEvaluatorPlugin = true;
    private boolean addMeteringPlugin = true;
    private boolean addInfraViewerPlugin = true;
    private boolean addVSLEvaulatorPlugin = true;
    private boolean addDataReaderPlugin = true;
    private boolean addSRTE = true;    
    private boolean VideoChecker = true;
    
    private int initZoom = 10;
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;    
    private MapHelper mapHelper;
    
    public TICAS2FrameLab(TMO tmo) {

        initComponents();
        
        try {
            final String filename = "jawin.dll";
            final String resourcesPath = "resources/" + filename;
            InputStream is = this.getClass().getResourceAsStream(resourcesPath);
            File file = new File(filename);
            OutputStream out=new FileOutputStream(file);
            byte buf[]=new byte[1024];
            int len;
            while((len=is.read(buf))>0)
            out.write(buf,0,len);
            out.close();
            is.close();
        } catch (IOException ex) {
            
        }   
        
        this.tmo = tmo;       
        this.setTitle("Traffic Information & Condition Analysis System " + TICASVersion.version);
        this.setSize(new Dimension(width, height));
        //this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/edu/umn/natsrl/ticas/resources/ticas_icon.png")).getImage());        
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        this.mainTab.setSelectedIndex(1);
        //this.setExtendedState(this.MAXIMIZED_BOTH);        
    }

    /**
     * Initialize interface
     * this method must called after initializing TMO object
     */
    public void init() {

        DateChecker dc = DateChecker.getInstance();

        loadSection();
        loadPlugins();
        SimulationModeAction(false);
        
        // date checker
        this.natsrlCalendar.setDateChecker(dc);

        // interval
        for (Interval i : Interval.values()) {
            this.cbxInterval.addItem(i);
        }

        // output direction
        for (OutputDirection d : OutputDirection.values()) {
            this.cbxOutputDirection.addItem(d);
        }
        
        // duration
        this.cbxDuration.addItem("Select");
        for (int i = 1; i <= 32; i++) {
            this.cbxDuration.addItem(i);
        }

        // load option saved perviously
        TICASOption ticasOption = TICASOption.load("ticas.cfg");
        EvaluationOption opt = ticasOption.getEvaluationOption();
        
        // if ticas option is loaded
        if (ticasOption.isLoaded()) {
            this.speedContourSetting.setContourSetting(opt.getContourSetting(ContourType.SPEED));
            this.totalFlowContourSetting.setContourSetting(opt.getContourSetting(ContourType.TOTAL_FLOW));
            this.occupancyContourSetting.setContourSetting(opt.getContourSetting(ContourType.OCCUPANCY));
            this.densityContourSetting.setContourSetting(opt.getContourSetting(ContourType.DENSITY));
            applyOption(ticasOption);            
        } else {
            System.err.println("Option is not loaded");
            // option isn't loaded
            this.speedContourSetting.setUnit("mile/h");
            this.totalFlowContourSetting.setUnit("veh/h");
            this.occupancyContourSetting.setUnit("%");
            this.densityContourSetting.setUnit("veh/mile");
            
            // set excel checkbox selected by default
            this.chkExcel.setSelected(true);                       
        }
        // add contour setting panel to tab panel
        this.panContourSettingSpeed.add(speedContourSetting, BorderLayout.NORTH);
        this.panContourSettingTotalFlow.add(totalFlowContourSetting, BorderLayout.NORTH);
        this.panContourSettingAverageFlow.add(occupancyContourSetting, BorderLayout.NORTH);
        this.panContourSettingDensity.add(densityContourSetting, BorderLayout.NORTH);
        
        
        this.jmKit.setTileFactory(TMCProvider.getTileFactory());
        jmKit.setAddressLocation(new GeoPosition(this.initLatitude, this.initLongitude));
        jmKit.setZoom(this.initZoom);
        this.jmKit.getMiniMap().setVisible(false);
        mapHelper = new MapHelper(jmKit);
        mapHelper.setFrame(this);        
        
        this.jmKit.getMiniMap().setVisible(false);

        SectionEditorPanel sep = new SectionEditorPanel();
        sep.setChangeListener(new ISectionChanged() {
            @Override
            public void sectionChanged(SectionChangedStatus status) {
                loadSection();
            }
        });
        this.panRouteCreation.add(sep);         
    }

    private void showSection(Section section) {
        this.mapHelper.showSection(section);
        RNode first = section.getRNodes().get(0);
        mapHelper.setCenter(first);
        mapHelper.zoom(5);
    }
    
    
    /**
     * Set TICAS interface according to saved option
     * @param opt 
     */
    private void applyOption(TICASOption ticasOption) {

        EvaluationOption opt = ticasOption.getEvaluationOption();
        
        // times
        this.cbxStartHour.setSelectedIndex(opt.getStartHour());
        this.cbxStartMin.setSelectedIndex(opt.getStartMin());
        
        // duration and times
        int duration = ticasOption.getDuration();
        if (duration > 0) {
            this.cbxDuration.setSelectedItem(duration);
            this.cbxEndHour.setEnabled(false);
            this.cbxEndMin.setEnabled(false);
        } else {
            this.cbxEndHour.setSelectedIndex(opt.getEndHour());
            this.cbxEndMin.setSelectedIndex(opt.getEndMin());
        }        
        
        // set all checkboxes in saved option checked
        for (OptionType option : opt.getOptions()) {
            option.getCheckBox().setSelected(true);
        }

        // detector options
        if (opt.hasOption(OptionType.DETECTOR_OPT_WITHOUT_LANECONFIG) || opt.hasOption(OptionType.DETECTOR_OPT_WITH_LANECONFIG)) {
            this.chkDetectorSpeed.setEnabled(true);
            this.chkDetectorDensity.setEnabled(true);
            this.chkDetectorFlow.setEnabled(true);
            this.chkDetectorOccupancy.setEnabled(true);
        }        
        
        // output direction
        this.cbxOutputDirection.setSelectedItem(opt.getOutputDirection());

        // output folder
        this.tbxOutputFolder.setText(ticasOption.getOutputPath());

        // configuration values
        this.tbxCongestionThresholdSpeed.setText(String.format("%d", opt.getCongestionThresholdSpeed()));
        this.tbxCriticalDensity.setText(String.format("%d", opt.getCriticalDensity()));
        this.tbxLaneCapacity.setText(String.format("%d", opt.getLaneCapacity()));
        
        // selected section index
        int selectedSectionIndex = ticasOption.getSeletedSectionIndex();
//        if(selectedSectionIndex < this.cbxSections.getItemCount()-1) this.cbxSections.setSelectedIndex(selectedSectionIndex);
        
        // selected interval index
        int selectedIntervalIndex = ticasOption.getSelectedIntervalIndex();
        if(selectedIntervalIndex < this.cbxInterval.getItemCount()-1) this.cbxInterval.setSelectedIndex(selectedIntervalIndex);        
        
        InfraConstants.TRAFFIC_DATA_URL = ticasOption.getTrafficDataUrl();
        InfraConstants.TRAFFIC_CONFIG_URL = ticasOption.getTrafficConfigUrl();

    }

    /**
     * Change times status according to selected duration
     */
    private void selectDuration() {
        if (this.cbxDuration.getSelectedIndex() == 0) {
            this.cbxEndHour.setEnabled(true);
            this.cbxEndMin.setEnabled(true);
        } else {
            this.cbxEndHour.setEnabled(false);
            this.cbxEndMin.setEnabled(false);
        }
    }

    /**
     * Loads section information from TMO
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        if(sm == null) return;
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());
        this.cbxSections.removeAllItems();
        this.cbxSections.addItem("Select the route");
        for (Section s : this.sections) {
            this.cbxSections.addItem(s);
        }        
    }

    /**
     * Load plugins from plugin directory
     */
    private void loadPlugins() {
        PluginInfoLoader pis = new PluginInfoLoader();
        pis.load();

        for (PluginInfo pi : pis.getPlugins()) {
            if (pi.getType().isSimulationPlugin()) {
                addSimulationPlugins(pi);
            } else if (pi.getType().isToolPlugin()) {
                addTools(pi);
            }
        }

        addBuiltinPlugins();
    }

    /**
     * Loads builtin plugins from edu.umn.natsrl.ticas.plugin package
     */
    private void addBuiltinPlugins() {
        // load builtin simulation plugins                
        
        if(addSimEvaluatorPlugin) {
            PluginInfo simEvaluator = new PluginInfo("Basic Simulation", PluginType.SIMULATION, VissimCalibration2.class);
            addSimulationPlugins(simEvaluator);            
        }
        
        PluginInfo simFixedMetering = new PluginInfo("Fixed Metering Simulation", PluginType.TOOL, FixedMeteringSimulation.class);       
        addSimulationPlugins(simFixedMetering);
        
        PluginInfo simBasicMetering = new PluginInfo("Local Responsible Metering Simulation", PluginType.TOOL, BasicMetering.class);       
        addSimulationPlugins(simBasicMetering);
        
        if(addMeteringPlugin) {
            PluginInfo meteringPlugin = new PluginInfo("K_Adaptive Metering Simulation", PluginType.SIMULATION, MeteringSimulation.class);
            addSimulationPlugins(meteringPlugin);            
        }        
        
        if(addSFIMPlugin) {
            PluginInfo sfimPlugin = new PluginInfo("IRIS_in_Loop Simulation", PluginType.SIMULATION, SfimTicasPlugin.class);
            addSimulationPlugins(sfimPlugin);            
        }
        if(addInfraViewerPlugin) {
            PluginInfo infraViewer = new PluginInfo("Infra Viewer", PluginType.TOOL, InfraViewerPlugin.class);        
            addTools(infraViewer);            
        }
        
        if(addVSLEvaulatorPlugin) {
            PluginInfo ttIndexer = new PluginInfo("Operation measures", PluginType.TOOL, TTIndexterPlugin.class);
            addTools(ttIndexer);
        }
        
        if(addDataReaderPlugin) {
            PluginInfo dataReader = new PluginInfo("Station Data Reader", PluginType.TOOL, DataReader.class);       
            addTools(dataReader);
        }
        
        PluginInfo ramp = new PluginInfo("Ramp Meter Evaluator", PluginType.TOOL, RampMeterEvaluatorPlugin.class);       
        addTools(ramp);
        
        /*PluginInfo rampwtime = new PluginInfo("Ramp Cumulative Input/Output", PluginType.TOOL, RampWaitingTime.class);       
        addTools(rampwtime);*/
        
        PluginInfo DdataReader = new PluginInfo("Detector Data Reader", PluginType.TOOL, DetecterDataReader.class);       
        addTools(DdataReader);        
        
//        if(VideoChecker){
//            PluginInfo videochecker = new PluginInfo("Video Checker", PluginType.TOOL, VideoChecker.class);       
//            addTools(videochecker);        
//        }
        
        PluginInfo srte = new PluginInfo("SRTE", PluginType.TOOL, TICASPluginSRTE.class);       
        addSimulationPlugins(srte);
    }

    /**
     * Adds simulation plugins to simulation combobox
     * @param pluginInfo 
     */
    private void addSimulationPlugins(PluginInfo pluginInfo) {
        this.cbxPlugins.addItem(pluginInfo);
        this.pluginInfos.add(pluginInfo);
    }

    /**
     * Adds plugins to tools menu
     * @param pluginInfo 
     */
    private void addTools(PluginInfo pluginInfo) {
        this.pluginInfos.add(pluginInfo);
        JMenuItem item = this.menuTools.add(pluginInfo.getName());
        final TICAS2FrameLab frame = this;

        item.setFont(new Font("Verdana", 0, 12));
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                String menuText = item.getText();
                for (PluginInfo pi : pluginInfos) {
                    if (menuText.equals(pi.getName())) {
                        try {
                            PluginFrame sd = new PluginFrame(frame);
                            sd.setLocation(getLocation());
                            sd.setPluginInfo(pi);
                            sd.setVisible(true);
                            frame.setVisible(false);
                        } catch (Exception ex) {
                            frame.setVisible(true);
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Executes simulation plugin
     */
    private void runSimulationPlugin() {
        try {            
                //Simulation Data Reset
                SimObjects.getInstance().reset();
            if(this.cbxPlugins.getItemCount() == 0) {
                JOptionPane.showMessageDialog(rootPane, "Plugin must be selected");
                return;
            }
            PluginInfo pi = (PluginInfo) this.cbxPlugins.getSelectedItem();
            PluginFrame sf = new PluginFrame(this);
            sf.setPluginInfo(pi);
            sf.setLocation(this.getLocation());
            sf.setVisible(true);
            this.setVisible(false);  
        } catch (Exception ex) {
            this.setVisible(true);
            ex.printStackTrace();
        }
    }
    
    @Override
    public void afterSimulation(PluginFrame sf) {
            this.simSection = sf.getSection();
            this.simPeriod = sf.getPeriod();

//            RandomSeed crsd = RandomSeed.getInstance();
//            if(crsd.IsEndSeed())
//                this.btnSaveSimResult.setEnabled(true);
//            else{
                if(this.simSection == null || this.simPeriod == null) {
                    this.btnSaveSimResult.setEnabled(false);
                } else {
                    this.btnSaveSimResult.setEnabled(true);                
                }
//            }
            this.setLocation(sf.getLocation());
            this.setVisible(true);        
    }
    
    /**
     * Executes evaluation with simulation results
     */
    public void evaluateSimulation() {
            
        if(simSection == null || simPeriod == null) {
            JOptionPane.showMessageDialog(rootPane, "Section or Period is null");
            return;
        }
        
        simPeriod.interval = ((Interval)this.cbxInterval.getSelectedItem()).second;        
        
        
//        try {
//            System.out.println("= Simulation =======");
//            System.out.println("Period : data=" + simPeriod.getTimeline().length + ", interval="+simPeriod.interval);
//            System.out.println("Data Length = " + simSection.getRNodes().get(0).getDetectors()[0].getSpeed().length);
//        } catch(Exception ex) {
//            ex.printStackTrace();
//        }        
        
        TICASOption selectedOption = getOption(true);
        EvaluationOption opt = selectedOption.getEvaluationOption();
        
        if(!this.cbxSimulationForCalibration.isSelected()){
            //process Single 
            opt.setPeriods(new Period[]{simPeriod});
//            if(!this.selectedSimulationResult.IsListData())
//                simSection.loadData(simPeriod, true);
        }
        
        opt.setSection(simSection);
        
        
        opt.setSimulationMode(true);
        
        if (selectedOption == null) {
            JOptionPane.showMessageDialog(null, "Check options");
            return;
        }
        
        // save option
        TICASOption.save(selectedOption, OPTION_FILE);
        
        //Output Option Check
        if(this.cbxSimulationForCalibration.isSelected()){
            opt.removeOption(OptionType.OUT_CONTOUR);
            opt.removeOption(OptionType.OUT_EXCEL);
            opt.removeOption(OptionType.OUT_CSV);
            opt.removeOption((OptionType.STATION_ACCEL));
            opt.removeOption((OptionType.STATION_AVG_LANE_FLOW));
            opt.removeOption((OptionType.STATION_DENSITY));
            opt.removeOption((OptionType.STATION_OCCUPANCY));
            opt.removeOption((OptionType.STATION_SPEED));
            opt.removeOption((OptionType.STATION_TOTAL_FLOW));
            opt.removeOption((OptionType.STATION_VOLUME));
            if(this.chkExcel.isSelected()){
                opt.addOption(OptionType.OUT_EXCEL);
            }
            if(this.chkCSV.isSelected()){
                opt.addOption(OptionType.OUT_CSV);
            }
            if(this.chkContour.isSelected()){
                opt.addOption(OptionType.OUT_CONTOUR);
                opt.addOption(OptionType.STATION_SPEED);
                opt.addOption(OptionType.STATION_TOTAL_FLOW);
            }
        }
        
        OptionType[] options = opt.getOptions();
        
        // check options
        if (!checkOptions(selectedOption, true)) {
            return;
        } 
        
        // open RunningDialog
        RunningDialog rd = new RunningDialog(this, true);
        rd.setLocationRelativeTo(this);       
        Timer t = new Timer();
        if(this.cbxSimulationForCalibration.isSelected())
            t.schedule(new EvaluationForCalibration(selectedOption, options, rd,this.selectedSimulationResult), 10);
        else{
            t.schedule(new EvaluateTask(selectedOption, options, rd,this.selectedSimulationResult), 10);
        }
        rd.setTimer(t);
        rd.setVisible(true);            

    }
    
    /**
     * Save simulation result
     */
    private void saveSimulationResult() {
        if(simSection == null || simPeriod == null) {
            JOptionPane.showMessageDialog(rootPane, "Empty simulation result");
            return;
        }
        // Detectors must have data, before this routine
        SimulationResultSaveDialog srd = new SimulationResultSaveDialog(this, simSection, simPeriod);
        srd.setLocationRelativeTo(this);
        srd.setVisible(true);        
    }
    
    /**
     * load simulation results from local disk
     * set data to table
     */
    private void loadSimulationResults() {
        this.cbxsimulationresult.setEnabled(false);
        if(this.cbxsimulationresult != null)
            this.cbxsimulationresult.removeAllItems();
        cbxsimulationresult.addItem("Loading Datas.....");
        
        ArrayList<SimulationResult> res = SimulationUtil.loadSimulationResults();
        
        if(this.cbxsimulationresult != null)
            this.cbxsimulationresult.removeAllItems();
        
        
        for(SimulationResult s : res)
        {
            if(s != null) {
                cbxsimulationresult.addItem(s);
            } else {
                System.out.println("Loaded is null");
            }           
        }           
        this.cbxsimulationresult.setEnabled(true);
    }

    /**
     * Evaluate with selected simulation result
     */
    private void evaluateWithSimulationResult(SimulationResult sr) {
        selectedSimulationResult = sr;
        if(this.selectedSimulationResult == null) {
            JOptionPane.showMessageDialog(rootPane, "Select simulation result");            
            return;
        }
        
        this.simSection = selectedSimulationResult.getSection();
        this.simPeriod = selectedSimulationResult.getPeriod();
        
        SimObjects.getArrayListInstance().clear();
//        if(!selectedSimulationResult.IsListData()){
//            SimObjects.getInstance().reset();        
//            // set simulation results to detectors
//            selectedSimulationResult.setTrafficDataToDetectors();
//        }
        
        
        //String sh = this.tbxSimStartHour.getText();
        //String sm = this.tbxSimStartMin.getText();
        
        int start_hour=-1, start_min = -1;
        try {
            start_hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());//Integer.parseInt(sh);
            start_min = Integer.parseInt(this.cbxStartMin.getSelectedItem().toString());//Integer.parseInt(sm);
        } catch(Exception ex) {}
        
        
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        if(this.cbxSimulationForCalibration.isSelected()){
            if(selectedDates.length < 1){
                JOptionPane.showMessageDialog(rootPane, "Select \'date and time\' of Real Data");
                return;
            }else if(selectedDates.length > 1){
                JOptionPane.showMessageDialog(rootPane, "Select just one date");
                return;
            }
        }
        
        if( ( start_hour >= 0 && start_hour < 24) && ( start_min >= 0 && start_min < 60)) {
            Date sDate = simPeriod.startDate;
            Date eDate = simPeriod.endDate;
            int diff = (int)(eDate.getTime() - sDate.getTime());
            Calendar c;
            if(this.cbxSimulationForCalibration.isSelected())
                c = (Calendar)selectedDates[0].clone();////Calendar.getInstance();//
            else
                c = Calendar.getInstance();
            
            c.set(Calendar.HOUR_OF_DAY, start_hour);
            c.set(Calendar.MINUTE, start_min);
            c.set(Calendar.SECOND, 0);
            sDate = c.getTime();
            c.add(Calendar.MILLISECOND, diff);
            eDate = c.getTime();
            this.simPeriod = new Period(sDate, eDate, 30);
        }
        
        this.evaluateSimulation();        
    }    
    
    /**
     * Reloads section information
     */
    public void reLoadSection() {
        this.loadSection();
    }

    /**
     * File > Clear Cache
     */
    private void clearCache() {
        ClearCacheDialog ccd = new ClearCacheDialog(this, true);
        ccd.setLocationRelativeTo(this);
        ccd.setVisible(true);
    }    
    
    /**
     * File > Geo Update
     */
    private void geoUpdate()
    {
        new File("infra.dat").delete();
        JOptionPane.showMessageDialog(rootPane, "Run TICAS again!!");
        System.exit(0);
    }

        
    /**
     * Evaluates based on checked options
     */
      public void evalute() throws IOException {

        TICASOption selectedOption = getOption(false);
        EvaluationOption opt = selectedOption.getEvaluationOption();
        
        if (selectedOption == null) {
            return;
        }

        OptionType[] options = opt.getOptions();
        
        // check options
        if (!checkOptions(selectedOption, false)) {
            return;
        }
        
        // save option
        TICASOption.save(selectedOption, OPTION_FILE);
        
        if (selectedOption == null) {
            JOptionPane.showMessageDialog(null, "Check options");
            return;
        }
        
        // open RunningDialog
        RunningDialog rd = new RunningDialog(this, true);
        rd.setLocationRelativeTo(this);
        Timer t = new Timer();
        t.schedule(new EvaluateTask(selectedOption, options, rd), 10);
        rd.setTimer(t);
        rd.setVisible(true);        
    }

    private void changedSection() {
        Section section = getSelectedSection();
        if(section != null) {
            //this.tbxSectionDesc.setText(section.getDescription());
            this.showSection(section);
        } else {
            //this.tbxSectionDesc.setText("");
            if(mapHelper !=null) this.mapHelper.clear();
            jmKit.setAddressLocation(new GeoPosition(this.initLatitude, this.initLongitude));
            jmKit.setZoom(this.initZoom);            
        }
    }


    private Section getSelectedSection()
    {
        Object so = this.cbxSections.getSelectedItem();
        if(so instanceof Section) {
            return (Section)this.cbxSections.getSelectedItem();            
        } else return null;
    }

    private void SimulationModeAction(boolean selected) {
        if(selected){
            new Timer().schedule(new TimerTask(){
                @Override
                public void run() {
                    loadSimulationResults();
                }
            },5);
            this.cbxSimulationForCalibration.setEnabled(true);
            this.cbxsimulationresult.setEnabled(true);
            this.cbxSections.setEnabled(false);
            this.cbxEndHour.setEnabled(false);
            this.cbxEndMin.setEnabled(false);
            this.cbxDuration.setEnabled(false);
        }else{
            this.cbxSimulationForCalibration.setEnabled(false);
            this.cbxSimulationForCalibration.setSelected(false);
            this.cbxsimulationresult.setEnabled(false);
            this.cbxSections.setEnabled(true);
            this.cbxEndHour.setEnabled(true);
            this.cbxEndMin.setEnabled(true);
            this.cbxDuration.setEnabled(true);
            MainVisibleCheck(true);
        }
    }

    private void SimulationModeforCalibration(boolean selected) {
        boolean s = !selected;
        MainVisibleCheck(s);
    }

    private void MainVisibleCheck(boolean s) {
        this.cbxStationDataAcceleration.setEnabled(s);
        this.cbxStationDataAlaneFlow.setEnabled(s);
        this.cbxStationDataDensity.setEnabled(s);
        this.cbxStationDataOccupancy.setEnabled(s);
        this.cbxStationDataSpeed.setEnabled(s);
        this.cbxStationDataTotalFlow.setEnabled(s);
        this.chkWithLaneConfig.setEnabled(s);
        this.chkWithoutLaneConfig.setEnabled(s);
        this.chkVMT.setEnabled(s);
        this.chkTT.setEnabled(s);
        this.chkLVMT.setEnabled(s);
        this.chkSV.setEnabled(s);
        this.chkVHT.setEnabled(s);
        this.chkCM.setEnabled(s);
        this.chkDVH.setEnabled(s);
        this.chkCMH.setEnabled(s);
        this.chkMRF.setEnabled(s);
        this.chkWOAUX.setEnabled(s);
        this.chkWOHOV.setEnabled(s);
        this.chkWOWAVE.setEnabled(s);
        this.chkONLYHOV.setEnabled(s);
        this.tbxCongestionThresholdSpeed.setEnabled(s);
        this.tbxLaneCapacity.setEnabled(s);
        this.tbxCriticalDensity.setEnabled(s);
        boolean timecheck = false;
        if(this.cbxUseSimulationData.isSelected() && this.cbxSimulationForCalibration.isSelected()){
            timecheck = true;
        }
        if(!this.cbxUseSimulationData.isSelected())
            timecheck = true;
        this.cbxEndHour.setEnabled(timecheck);
        this.cbxEndMin.setEnabled(timecheck);
        this.cbxDuration.setEnabled(timecheck);
}

    /**
     * Task class to execute all selected evaluations
     */
    class EvaluateTask extends TimerTask {

        TICASOption selectedOption;
        EvaluationOption option;
        OptionType eot = null;
        OptionType[] options;
        RunningDialog rd;
        SimulationResult selectedSimulationResult;
        ArrayList<SimulationResult> simResultList;// = new ArrayList<SimulationResult>();
        boolean IsinitSuc = false;

        public EvaluateTask(TICASOption selectedOption, OptionType[] options, RunningDialog rd) {
            init(selectedOption, options,rd);
            this.IsinitSuc = true;
        }
        
        public EvaluateTask(TICASOption selectedOption, OptionType[] options, RunningDialog rd, SimulationResult _sr) {
            init(selectedOption, options,rd);
            selectedSimulationResult = _sr;
            try{
                ReadData();
                this.IsinitSuc = true;
            }
            catch(Exception ex){
                StringErrorStream sos = new StringErrorStream(rd);
                ex.printStackTrace(new PrintStream(sos));
                sos.getMessage("Calibration Fail","Data Load Failed, Check Data File");
            }
        }
        
        public void init(TICASOption selectedOption, OptionType[] options, RunningDialog rd){
            this.selectedOption = selectedOption;
            this.option = selectedOption.getEvaluationOption();
            this.options = options;
            this.rd = rd;
            rd.showLog();
        }

        @Override
        public void run() {
            if(!this.IsinitSuc)
                return;
            
            try {
                System.out.println("Evaluation starts");
                long st = new Date().getTime();
            
                if(this.selectedSimulationResult != null && this.selectedSimulationResult.IsListData()){
                    System.out.println("Load Simulation Result List Data");
                    //Avg Data
                    SimObjects.getInstance().reset();  
                    for(SimulationResult _sr : this.simResultList){
                        System.out.println("Load Simulation Result Seed : " + _sr.getName());
                        this.ReadAvgData(_sr);
                    }
                    RunData();
                    
                    //Each Data
                    for(SimulationResult _sr : this.simResultList){
                        System.out.println("Load Simulation Result Seed : " + _sr.getName());
                        this.ReadSingleData(_sr);
                        RunData();
                    }
                }else{
                    RunData();

                }
                
                long et = new Date().getTime();
                System.out.println("Evaluation has been done ("+(et-st)/1000+" seconds)");

                // close RunningDialog after 1.8 seconds
                rd.close(1.8);

                // open output folder
                Desktop desktop = Desktop.getDesktop();
                desktop.open(new File(selectedOption.getOutputPath()));
                // if error, open log file
                //if(hasError) desktop.open(new File("log.txt"));
                
            } catch (IOException ex) {
                StringErrorStream sos = new StringErrorStream(rd);
                ex.printStackTrace(new PrintStream(sos));
                sos.getMessage("Calibration Fail","");
            }
        }

        private void ReadData() {
            if(!this.selectedSimulationResult.IsListData()){
                ReadSingleData(selectedSimulationResult);
            }else{
                SimulationSeveralResult ssr = new SimulationSeveralResult(option.getSection(),option.getPeriods()[0]);
                this.simResultList = ssr.getSimulationResults(selectedSimulationResult);
            }
        }
        private void ReadSingleData(SimulationResult _sr){
            SimObjects.getInstance().reset();        
            _sr.setTrafficDataToDetectors();
        }
        
        private void ReadAvgData(SimulationResult _sr){
            _sr.AddTrafficDataToDetectors();
        }

        private void RunData() throws IOException {
            Evaluation.clearCache();
            boolean hasError = false;
            

            for (OptionType ot : options) {
                // detector option
                if (ot.isDetectorOption()) {
                    if (option.hasOption(OptionType.DETECTOR_OPT_WITH_LANECONFIG)) {
                        eot = ot.getCombination(OptionType.DETECTOR_OPT_WITH_LANECONFIG, ot);
                        if(!runEvaluate(eot, selectedOption)) hasError = true;
                    }
                // other evaulations
                } else {
                    if(!runEvaluate(ot, selectedOption)) hasError = true;
                }
            }
        }
        
        public boolean cancel(){
            System.out.println("Cancel!!");
            return super.cancel();
        }
    }

    /**
     * Create evaluation instances according to option
     * @param ot
     * @param selectedOption
     * @return 
     */
    private boolean runEvaluate(OptionType ot, TICASOption selectedOption) {
        EvaluationOption opts = selectedOption.getEvaluationOption();
        Evaluation ev = Evaluation.createEvaluate(ot, selectedOption.getEvaluationOption());
        if (ev != null) {
            System.out.println("    - Evaluating " + ev.getName() + " ... ");
            ev.setPrintDebug(true);
            boolean v = ev.doEvaluate();
            try {
                // save excel
                if (opts.hasOption(OptionType.OUT_EXCEL)) {                
                    ev.saveExcel(selectedOption.getOutputPath());
                }

                // save CSV
                if (opts.hasOption(OptionType.OUT_CSV)) {
                    ev.saveCsv(selectedOption.getOutputPath());
                }

                // save Contour
                if (opts.hasOption(OptionType.OUT_CONTOUR)) {
                    if (ot.equals(OptionType.STATION_SPEED)) {
                        saveContour(ev, selectedOption, opts, ContourType.SPEED);
                    } else if (ot.equals(OptionType.STATION_TOTAL_FLOW)) {
                        saveContour(ev, selectedOption, opts, ContourType.TOTAL_FLOW);
                    } else if (ot.equals(OptionType.STATION_OCCUPANCY)) {
                        saveContour(ev, selectedOption, opts, ContourType.OCCUPANCY);
                    } else if (ot.equals(OptionType.STATION_DENSITY)) {
                        saveContour(ev, selectedOption, opts, ContourType.DENSITY);
                    }

                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(null, "Fail to save result : " + ev.getName());
                ex.printStackTrace();                
            }
            
            
            
            return v;
        }
        return true;
    }

    /**
     * Save contour
     * @param ev
     * @param selectedOption
     * @param opts
     * @param cType 
     */
    private void saveContour(Evaluation ev, TICASOption selectedOption, EvaluationOption opts, ContourType cType) {
        ContourPlotter cp = new ContourPlotter(opts.getSection(), opts.getContourSetting(cType), ev, selectedOption.getOutputPath());
        cp.saveImage(opts.hasOption(OptionType.OPEN_CONTOUR));        
    }    
    
    /**
     * Returns options by selected by user
     * @return options selected by user (section, interval, ouput path, checked options..)
     */
    private TICASOption getOption(boolean simMode) {
    
        TICASOption ticasOption = new TICASOption();
        EvaluationOption opt = ticasOption.getEvaluationOption();

        // all checkbox options
        for (EvaluationCheckBox c : EvaluationCheckBox.getCheckBoxes("TICAS")) {
            if (c.isSelected()) {
                opt.addOption(c.getOption());
            }
        }
        
        // section
        if(!simMode) {
            Section section = this.getSelectedSection();

            if (section == null) {
                JOptionPane.showMessageDialog(null, "Select section at first");
                this.mainTab.setSelectedIndex(1);
                return null;
            }
            opt.setSection(section);
        }
        
        // interval
        opt.setInterval((Interval) this.cbxInterval.getSelectedItem());

        // output path
        ticasOption.setOutputPath(this.tbxOutputFolder.getText());

        // output direction
        opt.setOutputDirection((OutputDirection) this.cbxOutputDirection.getSelectedItem());

        // period
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        Calendar c1, c2;
        Period period;
        int interval = ((Interval) this.cbxInterval.getSelectedItem()).second;
        int start_hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        int start_min = Integer.parseInt(this.cbxStartMin.getSelectedItem().toString());
        int end_hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        int end_min = Integer.parseInt(this.cbxEndMin.getSelectedItem().toString());

        // set hour and min
        opt.setStartEndTime(start_hour, start_min, end_hour, end_min);
        
        for (Calendar date : selectedDates) {
            c1 = (Calendar) date.clone();
            c2 = (Calendar) date.clone();

            c1.set(Calendar.HOUR, start_hour);
            c1.set(Calendar.MINUTE, start_min);

            if (this.cbxDuration.getSelectedIndex() > 0) {
                ticasOption.setDuration((Integer) this.cbxDuration.getSelectedItem());
                c2.set(Calendar.HOUR, start_hour);
                c2.set(Calendar.MINUTE, start_min);
                c2.add(Calendar.HOUR, (Integer) this.cbxDuration.getSelectedItem());
            } else {
                c2.set(Calendar.HOUR, end_hour);
                c2.set(Calendar.MINUTE, end_min);
            }

            period = new Period(c1.getTime(), c2.getTime(), interval);
            opt.addPeriod(period);
        }

        // contour
        opt.addContourPanel(ContourType.SPEED, speedContourSetting);
        opt.addContourPanel(ContourType.DENSITY, this.densityContourSetting);
        opt.addContourPanel(ContourType.TOTAL_FLOW, this.totalFlowContourSetting);
        opt.addContourPanel(ContourType.OCCUPANCY, this.occupancyContourSetting);
        
        // selected section
        ticasOption.setSelectedSectionIndex(this.cbxSections.getSelectedIndex());
        
        // selected interval
        ticasOption.setSelectedIntervalIndex(this.cbxInterval.getSelectedIndex());
        
        try {
            opt.setCongestionThresholdSpeed(Integer.parseInt(this.tbxCongestionThresholdSpeed.getText()));
            opt.setCriticalDensity(Integer.parseInt(this.tbxCriticalDensity.getText()));
            opt.setLaneCapacity(Integer.parseInt(this.tbxLaneCapacity.getText()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fill in integer value into text box");
            return null;
        }
        
        // url settings
        ticasOption.setTrafficConfigUrl(InfraConstants.TRAFFIC_CONFIG_URL);
        ticasOption.setTrafficDataUrl(InfraConstants.TRAFFIC_DATA_URL);
        
        final boolean withoutHOV = opt.hasOption(OptionType.WITHOUT_HOV);
        final boolean withoutWavetronics = opt.hasOption(OptionType.WITHOUT_WAVETRONICS);
        final boolean onlyHOV = opt.hasOption(OptionType.ONLY_HOV);
        final boolean withoutAux = opt.hasOption(OptionType.WITHOUT_AUXLANE);

        // detector checker
        opt.setDetectChecker(new IDetectorChecker() {
            @Override
            public boolean check(Detector d) {
                if(onlyHOV && !d.isHov()) return false; 
                if(withoutHOV && d.isHov()) return false;
                if(withoutAux && d.isAuxiliary()) return false;                
                if(withoutWavetronics && d.isWavetronics()) return false;
                if(d.isAbandoned()) return false;
                return true;
            }
        });
        
        return ticasOption;
    }

    /**
     * Are options selected correctly?
     * @param options TICASOption
     * @return true if all options are selected correctly, else false
     */
    private boolean checkOptions(TICASOption options, boolean simulationMode) {

        EvaluationOption opts = options.getEvaluationOption();
        
        // check first tab : section and dates
        if (!simulationMode && opts.getSection() == null) {
            JOptionPane.showMessageDialog(this, "Create route and select the route");
            return false;
        }
        if (!simulationMode && opts.getPeriods().length == 0) {
            JOptionPane.showMessageDialog(this, "Select date");
            return false;
        }

        // check second tab : evaluation metrics
        if (opts.getEvaluationOptions().length == 0 && !this.cbxSimulationForCalibration.isSelected()) {
            JOptionPane.showMessageDialog(this, "Select evaluation metric at least one");
            return false;
        }

        if (opts.hasOption(OptionType.DETECTOR_OPT_WITHOUT_LANECONFIG)
                || opts.hasOption(OptionType.DETECTOR_OPT_WITH_LANECONFIG)) {
            if (!opts.hasOption(OptionType.DETECTOR_SPEED)
                    && !opts.hasOption(OptionType.DETECTOR_DENSITY)
                    && !opts.hasOption(OptionType.DETECTOR_FLOW)
                    && !opts.hasOption(OptionType.DETECTOR_OCCUPANCY)) {
                JOptionPane.showMessageDialog(this, "Select detector data option");
                return false;
            }
        }

        // check third tab : execution tab
        if (!opts.hasOption(OptionType.OUT_CSV) && !opts.hasOption(OptionType.OUT_EXCEL)) {

            if (opts.hasOption(OptionType.OUT_CONTOUR) && !opts.hasOption(OptionType.STATION_SPEED) && !opts.hasOption(OptionType.STATION_TOTAL_FLOW) && !opts.hasOption(OptionType.STATION_OCCUPANCY) && !opts.hasOption(OptionType.STATION_DENSITY)) {
                JOptionPane.showMessageDialog(this, "Contour output is for evalutations of station data section except average lane flow");
            }

            if (!opts.hasOption(OptionType.OUT_CONTOUR) || (opts.hasOption(OptionType.OUT_CONTOUR) && !opts.hasOption(OptionType.STATION_SPEED) && !opts.hasOption(OptionType.STATION_TOTAL_FLOW) && !opts.hasOption(OptionType.STATION_OCCUPANCY) && !opts.hasOption(OptionType.STATION_DENSITY))) {
                JOptionPane.showMessageDialog(this, "Select output format at least one");
                return false;
            }
        }

        if (opts.hasOption(OptionType.OUT_CONTOUR) && !opts.hasOption(OptionType.STATION_SPEED) && !opts.hasOption(OptionType.STATION_TOTAL_FLOW) && !opts.hasOption(OptionType.STATION_OCCUPANCY) && !opts.hasOption(OptionType.STATION_DENSITY)) {
            JOptionPane.showMessageDialog(this, "Contour output is for evalutations of station data section except average lane flow");
        }
        
        // output path
        File outputPath = new File(options.getOutputPath());
        if (!outputPath.exists()) {            
            JOptionPane.showMessageDialog(this, "Select available output path");
            return false;
        }

        // check date availability
        if(!simulationMode && !DateChecker.getInstance().isAvailable(opts.getPeriods()[0].endDate)) {
            JOptionPane.showMessageDialog(this, "End date is not available");
            return false;            
        }


        return true;
    }

    /**
     * Sets enable for detector options
     * @param enabled enable? or not?
     */
    private void setEnableDetector(boolean enabled) {
        this.chkDetectorSpeed.setEnabled(enabled);
        this.chkDetectorDensity.setEnabled(enabled);
        this.chkDetectorOccupancy.setEnabled(enabled);
        this.chkDetectorFlow.setEnabled(enabled);

        if (!enabled) {
            this.chkDetectorSpeed.setSelected(enabled);
            this.chkDetectorDensity.setSelected(enabled);
            this.chkDetectorOccupancy.setSelected(enabled);
            this.chkDetectorFlow.setSelected(enabled);
        }
    }

    /**
     * Open section editor
     */
    public void openSectionEditor() {
        tmo.openSectionEditor(this, true);
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
        SectionInfoDialog si = new SectionInfoDialog(section, this.tmo, this, true);
        si.setLocationRelativeTo(this);
        si.setVisible(true);
    }

    /**
     * Open output path select dialog
     */
    private void selectOutputPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select output folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.tbxOutputFolder.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void checkClose() {
        if(JOptionPane.showConfirmDialog(rootPane, "Do you want to quit TICAS?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
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

        mainTab = new javax.swing.JTabbedPane();
        panRouteCreation = new javax.swing.JPanel();
        tbDataPerformance = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        cbxSections = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        jmKit = new org.jdesktop.swingx.JXMapKit();
        jPanel4 = new javax.swing.JPanel();
        chkVMT = new EvaluationCheckBox("TICAS", OptionType.EVAL_VMT);
        chkLVMT = new EvaluationCheckBox("TICAS", OptionType.EVAL_LVMT);
        chkVHT = new EvaluationCheckBox("TICAS", OptionType.EVAL_VHT);
        chkDVH = new EvaluationCheckBox("TICAS", OptionType.EVAL_DVH);
        chkMRF = new EvaluationCheckBox("TICAS", OptionType.EVAL_MRF);
        chkTT = new EvaluationCheckBox("TICAS", OptionType.EVAL_TT);
        chkCM = new EvaluationCheckBox("TICAS", OptionType.EVAL_CM);
        chkCMH = new EvaluationCheckBox("TICAS", OptionType.EVAL_CMH);
        chkSV = new EvaluationCheckBox("TICAS", OptionType.EVAL_SV);
        jLabel4 = new javax.swing.JLabel();
        tbxCongestionThresholdSpeed = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tbxLaneCapacity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tbxCriticalDensity = new javax.swing.JTextField();
        cbxStationDataAcceleration = new EvaluationCheckBox("TICAS", OptionType.STATION_ACCEL);
        cbxStationDataOccupancy = new EvaluationCheckBox("TICAS", OptionType.STATION_OCCUPANCY);
        cbxStationDataAlaneFlow = new EvaluationCheckBox("TICAS", OptionType.STATION_AVG_LANE_FLOW);
        cbxStationDataSpeed = new EvaluationCheckBox("TICAS", OptionType.STATION_SPEED);
        cbxStationDataDensity = new EvaluationCheckBox("TICAS", OptionType.STATION_DENSITY);
        cbxStationDataTotalFlow = new EvaluationCheckBox("TICAS", OptionType.STATION_TOTAL_FLOW);
        chkWithLaneConfig = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OPT_WITH_LANECONFIG);
        chkWithoutLaneConfig = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OPT_WITHOUT_LANECONFIG);
        chkDetectorOccupancy = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OCCUPANCY);
        chkDetectorFlow = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_FLOW);
        chkDetectorDensity = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_DENSITY);
        chkDetectorSpeed = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_SPEED);
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        chkWOHOV = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_HOV);
        chkWOWAVE = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_WAVETRONICS);
        chkONLYHOV = new EvaluationCheckBox("TICAS", OptionType.ONLY_HOV);
        chkWOAUX = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_AUXLANE);
        jPanel11 = new javax.swing.JPanel();
        chkExcel = new EvaluationCheckBox("TICAS", OptionType.OUT_EXCEL);
        chkCSV = new EvaluationCheckBox("TICAS", OptionType.OUT_CSV);
        chkContour = new EvaluationCheckBox("TICAS", OptionType.OUT_CONTOUR);
        jLabel16 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        cbxOutputDirection = new javax.swing.JComboBox();
        tbxOutputFolder = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jLabel10 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        cbxInterval = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        cbxStartHour = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        cbxStartMin = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        cbxEndHour = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        cbxEndMin = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbxDuration = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jCheckBox6 = new EvaluationCheckBox("TICAS", OptionType.OPEN_CONTOUR);
        chkWithoutVirtualStations = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_VIRTUAL_STATIONS);
        CbxFixmissingstationdata = new EvaluationCheckBox("TICAS", OptionType.FIXING_MISSING_DATA);
        CbxmissingstationdataZero = new EvaluationCheckBox("TICAS", OptionType.FIXING_MISSING_DATA_ZERO);
        btnEvaluate = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        cbxsimulationresult = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        cbxUseSimulationData = new javax.swing.JCheckBox();
        cbxSimulationForCalibration = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        tbSimulation = new javax.swing.JTabbedPane();
        jPanel12 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        btnRunSimulationPlugin = new javax.swing.JButton();
        cbxPlugins = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        btnSaveSimResult = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        panContourSettingSpeed = new javax.swing.JPanel();
        panContourSettingTotalFlow = new javax.swing.JPanel();
        panContourSettingDensity = new javax.swing.JPanel();
        panContourSettingAverageFlow = new javax.swing.JPanel();
        btnSaveConfig = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        chkConfigUseMRFInput = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        menuItemRouteEditorManual = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(990, 740));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainTab.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        mainTab.setMinimumSize(size());
        mainTab.setPreferredSize(new java.awt.Dimension(935, 640));
        mainTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabStateChanged(evt);
            }
        });

        panRouteCreation.setLayout(new java.awt.BorderLayout());
        mainTab.addTab("Route Creation", panRouteCreation);

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel22.setText("Route");

        jmKit.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jmKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jmKit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jmKit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        chkVMT.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkVMT.setText("Vehicle Miles Traveled (VMT)");

        chkLVMT.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkLVMT.setText("Lost VMT for congestion (LVMT)");
        chkLVMT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLVMTActionPerformed(evt);
            }
        });

        chkVHT.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkVHT.setText("Vehicle Hour Traveled (VHT)");

        chkDVH.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkDVH.setText("Delayed Vehicle Hours (DVH)");

        chkMRF.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkMRF.setText("Mainlane and Ramp Flow Rates");

        chkTT.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkTT.setText("Travel Time (TT)");

        chkCM.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkCM.setText("Congested Miles (CM)");
        chkCM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCMActionPerformed(evt);
            }
        });

        chkCMH.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkCMH.setText("Congested Miles * Hours (CMH)");
        chkCMH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCMHActionPerformed(evt);
            }
        });

        chkSV.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkSV.setText("Speed Variations (SV)");

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel4.setText("Congestion Threshold Speed (mph)");

        tbxCongestionThresholdSpeed.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxCongestionThresholdSpeed.setText("45");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel7.setText("Default Lane Capacity (veh/hr)");

        tbxLaneCapacity.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxLaneCapacity.setText("2200");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel9.setText("Critical Density (veh/mile)");

        tbxCriticalDensity.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxCriticalDensity.setText("40");

        cbxStationDataAcceleration.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataAcceleration.setText("Acceleration");

        cbxStationDataOccupancy.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataOccupancy.setText("Occupancy");

        cbxStationDataAlaneFlow.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataAlaneFlow.setText("Avg. Lane Flow");

        cbxStationDataSpeed.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataSpeed.setText("Speed");

        cbxStationDataDensity.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataDensity.setText("Density");

        cbxStationDataTotalFlow.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStationDataTotalFlow.setText("Total Flow");

        chkWithLaneConfig.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWithLaneConfig.setText("With Lane Config");
        chkWithLaneConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWithLaneConfigActionPerformed(evt);
            }
        });

        chkWithoutLaneConfig.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWithoutLaneConfig.setText("Without Lane Config");
        chkWithoutLaneConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWithoutLaneConfigActionPerformed(evt);
            }
        });

        chkDetectorOccupancy.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkDetectorOccupancy.setText("Occupancy");
        chkDetectorOccupancy.setEnabled(false);

        chkDetectorFlow.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkDetectorFlow.setText("Flow");
        chkDetectorFlow.setEnabled(false);

        chkDetectorDensity.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkDetectorDensity.setText("Density");
        chkDetectorDensity.setEnabled(false);

        chkDetectorSpeed.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkDetectorSpeed.setText("Speed");
        chkDetectorSpeed.setEnabled(false);

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel11.setText("Station Data");

        jLabel12.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel12.setText("Detector Data");

        jLabel13.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel13.setText("Measurments");

        jLabel23.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel23.setText("Detector Filter");

        chkWOHOV.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWOHOV.setText("w/o HOV");

        chkWOWAVE.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWOWAVE.setText("w/o wavetronics");

        chkONLYHOV.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkONLYHOV.setText("only HOV");

        chkWOAUX.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWOAUX.setText("w/o Aux");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxStationDataTotalFlow)
                                    .addComponent(cbxStationDataSpeed)
                                    .addComponent(cbxStationDataDensity)
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxStationDataAlaneFlow)
                                    .addComponent(cbxStationDataOccupancy)
                                    .addComponent(cbxStationDataAcceleration)))
                            .addComponent(jLabel11))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(chkDetectorSpeed)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkDetectorDensity)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkDetectorFlow))
                            .addComponent(jLabel12)
                            .addComponent(chkWithLaneConfig)
                            .addComponent(chkWithoutLaneConfig)
                            .addComponent(chkDetectorOccupancy)))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                    .addComponent(chkWOHOV)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(chkWOWAVE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(chkONLYHOV)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(chkWOAUX)
                            .addGap(88, 88, 88))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(chkDVH)
                                .addComponent(chkLVMT)
                                .addComponent(chkVMT)
                                .addComponent(chkVHT))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(chkCMH)
                                .addComponent(chkCM)
                                .addComponent(chkSV)
                                .addComponent(chkTT)))
                        .addComponent(chkMRF)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel7)
                                .addComponent(jLabel9))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tbxLaneCapacity, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tbxCongestionThresholdSpeed, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tbxCriticalDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxStationDataSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxStationDataAlaneFlow)
                    .addComponent(chkWithLaneConfig))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxStationDataDensity)
                    .addComponent(cbxStationDataOccupancy)
                    .addComponent(chkWithoutLaneConfig))
                .addGap(3, 3, 3)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxStationDataTotalFlow)
                    .addComponent(cbxStationDataAcceleration)
                    .addComponent(chkDetectorSpeed)
                    .addComponent(chkDetectorDensity)
                    .addComponent(chkDetectorFlow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDetectorOccupancy)
                .addGap(7, 7, 7)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(chkVMT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkLVMT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkVHT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkDVH)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkMRF))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(chkTT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkCM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkCMH)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tbxCongestionThresholdSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbxLaneCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tbxCriticalDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWOHOV)
                    .addComponent(chkWOWAVE)
                    .addComponent(chkONLYHOV)
                    .addComponent(chkWOAUX))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        chkExcel.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkExcel.setText("Excel");

        chkCSV.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkCSV.setText("CSV");

        chkContour.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkContour.setText("Contour");
        chkContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkContourActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel16.setText("Time Range");

        jLabel21.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel21.setText("in");

        cbxOutputDirection.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        tbxOutputFolder.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxOutputFolder.setPreferredSize(new java.awt.Dimension(200, 20));

        jButton5.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jButton5.setText("Browse");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel1.setText("Output Folder");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel2.setText("Output Format");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(chkExcel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkCSV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkContour)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxOutputDirection, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                            .addComponent(tbxOutputFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkExcel)
                    .addComponent(chkCSV)
                    .addComponent(chkContour)
                    .addComponent(jLabel16)
                    .addComponent(cbxOutputDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbxOutputFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jLabel10.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel10.setText("Date and Time");

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel17.setText("Time Interval");

        cbxInterval.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel18.setText("Start Time");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStartHourActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel14.setText(":");

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel20.setText("End Time");

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setSelectedIndex(8);
        cbxEndHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEndHourActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel15.setText(":");

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel3.setText("or");

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel5.setText("for");

        cbxDuration.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        cbxDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDurationActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel6.setText("hour");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel6))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel20))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbxEndHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel15)
                                        .addGap(1, 1, 1)
                                        .addComponent(cbxEndMin, 0, 1, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(18, 18, 18)
                                .addComponent(cbxInterval, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jCheckBox6.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jCheckBox6.setSelected(true);
        jCheckBox6.setText("Open contour after extraction");

        chkWithoutVirtualStations.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        chkWithoutVirtualStations.setText("Without virtual stations");

        CbxFixmissingstationdata.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        CbxFixmissingstationdata.setSelected(true);
        CbxFixmissingstationdata.setText("Interpolate missing station data");
        CbxFixmissingstationdata.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CbxFixmissingstationdataActionPerformed(evt);
            }
        });

        CbxmissingstationdataZero.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        CbxmissingstationdataZero.setText("Interpolate '0' missing station data");
        CbxmissingstationdataZero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CbxmissingstationdataZeroActionPerformed(evt);
            }
        });

        btnEvaluate.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        btnEvaluate.setForeground(new java.awt.Color(255, 0, 0));
        btnEvaluate.setText("Extract");
        btnEvaluate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEvaluateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEvaluate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(chkWithoutVirtualStations)
                        .addComponent(jCheckBox6)
                        .addComponent(CbxFixmissingstationdata, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(CbxmissingstationdataZero, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkWithoutVirtualStations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CbxFixmissingstationdata)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CbxmissingstationdataZero)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEvaluate)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Extract Data from Simulation Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 10), java.awt.Color.black)); // NOI18N

        cbxsimulationresult.setPreferredSize(new java.awt.Dimension(400, 20));

        jLabel28.setFont(new java.awt.Font("Verdana", 1, 10)); // NOI18N
        jLabel28.setText("Simulation Output Files");

        cbxUseSimulationData.setText("Extract Data/MOE from Simulation Results");
        cbxUseSimulationData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxUseSimulationDataActionPerformed(evt);
            }
        });

        cbxSimulationForCalibration.setText("For Comparing Simulation Results with Real Data");
        cbxSimulationForCalibration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSimulationForCalibrationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(cbxUseSimulationData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxSimulationForCalibration)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel28))
                    .addComponent(cbxsimulationresult, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbxUseSimulationData)
                        .addComponent(cbxSimulationForCalibration))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel28)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxsimulationresult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tbDataPerformanceLayout = new javax.swing.GroupLayout(tbDataPerformance);
        tbDataPerformance.setLayout(tbDataPerformanceLayout);
        tbDataPerformanceLayout.setHorizontalGroup(
            tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tbDataPerformanceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(tbDataPerformanceLayout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(tbDataPerformanceLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(295, 295, 295))
        );
        tbDataPerformanceLayout.setVerticalGroup(
            tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tbDataPerformanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tbDataPerformanceLayout.createSequentialGroup()
                        .addGroup(tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(tbDataPerformanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        mainTab.addTab("Data Extraction", tbDataPerformance);

        tbSimulation.setFocusable(false);
        tbSimulation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbSimulation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbSimulationStateChanged(evt);
            }
        });

        btnRunSimulationPlugin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnRunSimulationPlugin.setText("Run Simulation");
        btnRunSimulationPlugin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunSimulationPluginActionPerformed(evt);
            }
        });

        cbxPlugins.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel24.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel24.setText("Simulation Options");

        btnSaveSimResult.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnSaveSimResult.setText("Save Simulation Result");
        btnSaveSimResult.setEnabled(false);
        btnSaveSimResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSimResultActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel27.setText("Extracting data with simulation results uses the metrics of 'Real Data Extraction' tab.");

        jLabel19.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel19.setText("After saving simulation result, you can extract data from simulation results on 'Simulation Results' tab.");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addGroup(jPanel14Layout.createSequentialGroup()
                                .addComponent(cbxPlugins, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRunSimulationPlugin))
                            .addComponent(jLabel19)
                            .addComponent(jLabel27)))
                    .addComponent(btnSaveSimResult, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(238, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxPlugins, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRunSimulationPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveSimResult, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(340, Short.MAX_VALUE))
        );

        tbSimulation.addTab("New Simulation", jPanel12);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbSimulation)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbSimulation)
                .addContainerGap())
        );

        mainTab.addTab("Simulation", jPanel7);

        jTabbedPane2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        panContourSettingSpeed.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Speed", panContourSettingSpeed);

        panContourSettingTotalFlow.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Total Flow", panContourSettingTotalFlow);

        panContourSettingDensity.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Density", panContourSettingDensity);

        panContourSettingAverageFlow.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Occupancy", panContourSettingAverageFlow);

        btnSaveConfig.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnSaveConfig.setText("Save Configurations");
        btnSaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveConfigActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Extraction Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        chkConfigUseMRFInput.setForeground(new java.awt.Color(255, 255, 255));
        chkConfigUseMRFInput.setText("Use Input Flow of Ramp on using Mainlane and Ramp Flow Rates");
        chkConfigUseMRFInput.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkConfigUseMRFInput)
                .addContainerGap(152, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkConfigUseMRFInput)
                .addContainerGap(487, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnSaveConfig))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnSaveConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTab.addTab("Contour Configuration", jPanel5);

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Northland Advanced Transportation Systems Research Lab. @ University of Minnesota Duluth");

        jMenuBar1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jMenu1.setText("File");
        jMenu1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jMenuItem6.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuItem6.setText("Geo Update");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        jMenuItem2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuItem2.setText("Save Current Setting");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem5.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuItem5.setText("Clear Cache");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuItem3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        menuTools.setText("Tool");
        menuTools.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuBar1.add(menuTools);

        jMenu3.setText("Help");
        jMenu3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        menuItemRouteEditorManual.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        menuItemRouteEditorManual.setText("Route Editor Manual");
        menuItemRouteEditorManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRouteEditorManualActionPerformed(evt);
            }
        });
        jMenu3.add(menuItemRouteEditorManual);

        jMenuItem1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jMenuItem1.setText("About TICAS");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel8))
                    .addComponent(mainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 940, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 655, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        checkClose();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        //TICASOption opt = getOption();
//        if (!checkOptions(opt)) {
//            return;
//        }
        TICASOption.save(getOption(true), OPTION_FILE);
        JOptionPane.showMessageDialog(this, "Configuration has been saved", "Info", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void cbxStartHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxStartHourActionPerformed
        int slt1 = this.cbxStartHour.getSelectedIndex();
        int slt2 = this.cbxEndHour.getSelectedIndex();
        if (slt1 > slt2) {
            this.cbxEndHour.setSelectedIndex(slt1);
        }
}//GEN-LAST:event_cbxStartHourActionPerformed

    private void cbxEndHourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxEndHourActionPerformed
        int slt1 = this.cbxStartHour.getSelectedIndex();
        int slt2 = this.cbxEndHour.getSelectedIndex();
        if (slt1 > slt2) {
            this.cbxStartHour.setSelectedIndex(slt2);
        }
}//GEN-LAST:event_cbxEndHourActionPerformed

    private void cbxDurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDurationActionPerformed
        selectDuration();
}//GEN-LAST:event_cbxDurationActionPerformed

    private void chkLVMTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLVMTActionPerformed
        //        this.tbxCriticalDensity.setEnabled(this.chkLVMT.isSelected());
        //        this.tbxLaneCapacity.setEnabled(this.chkLVMT.isSelected());
}//GEN-LAST:event_chkLVMTActionPerformed

    private void chkCMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkCMActionPerformed
        //        this.tbxCongestionThresholdSpeed.setEnabled(this.chkCM.isSelected() || this.chkCMH.isSelected());
}//GEN-LAST:event_chkCMActionPerformed

    private void chkCMHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkCMHActionPerformed
        //        this.tbxCongestionThresholdSpeed.setEnabled(this.chkCM.isSelected() || this.chkCMH.isSelected());
}//GEN-LAST:event_chkCMHActionPerformed

    private void chkWithLaneConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWithLaneConfigActionPerformed
        setEnableDetector(this.chkWithLaneConfig.isSelected() || this.chkWithoutLaneConfig.isSelected());
}//GEN-LAST:event_chkWithLaneConfigActionPerformed

    private void chkWithoutLaneConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWithoutLaneConfigActionPerformed
        setEnableDetector(this.chkWithLaneConfig.isSelected() || this.chkWithoutLaneConfig.isSelected());
}//GEN-LAST:event_chkWithoutLaneConfigActionPerformed

    private void chkContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkContourActionPerformed
        //        if(this.chkContour.isSelected()) {
        //            JOptionPane.showMessageDialog(mainPanel, "If you want to change setting for contour, see 'contour setting' tab");
        //        }
}//GEN-LAST:event_chkContourActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        selectOutputPath();
}//GEN-LAST:event_jButton5ActionPerformed

    private void btnEvaluateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEvaluateActionPerformed
        try {
            if(this.cbxUseSimulationData.isSelected()){
                evaluateWithSimulationResult((SimulationResult)this.cbxsimulationresult.getSelectedItem());
            }else{
                evalute();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnEvaluateActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        TICAS2AboutBox tb = new TICAS2AboutBox(this, true);
        tb.setLocationRelativeTo(this);
        tb.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        clearCache();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        geoUpdate();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        checkClose();
    }//GEN-LAST:event_formWindowClosing

    private void menuItemRouteEditorManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRouteEditorManualActionPerformed

        try {
            final String filename = "Manual - Route Editor.pdf";
            final String resourcesPath = "resources/" + filename;
            InputStream is = this.getClass().getResourceAsStream(resourcesPath);
            File file = new File(filename);
            OutputStream out=new FileOutputStream(file);
            byte buf[]=new byte[1024];
            int len;
            while((len=is.read(buf))>0)
            out.write(buf,0,len);
            out.close();
            is.close();
            java.awt.Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            
        }                
    }//GEN-LAST:event_menuItemRouteEditorManualActionPerformed

    private void btnSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveConfigActionPerformed
        TICASOption.save(getOption(false), OPTION_FILE);
        JOptionPane.showMessageDialog(this, "Configuration has been saved", "Info", JOptionPane.PLAIN_MESSAGE);
}//GEN-LAST:event_btnSaveConfigActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
        changedSection();
    }//GEN-LAST:event_cbxSectionsActionPerformed

    private void mainTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabStateChanged

    }//GEN-LAST:event_mainTabStateChanged

private void CbxFixmissingstationdataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CbxFixmissingstationdataActionPerformed
// TODO add your handling code here:
    if(this.CbxFixmissingstationdata.isSelected()){
        if(this.CbxmissingstationdataZero.isSelected())
            this.CbxmissingstationdataZero.setSelected(false);
    }
}//GEN-LAST:event_CbxFixmissingstationdataActionPerformed

private void CbxmissingstationdataZeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CbxmissingstationdataZeroActionPerformed
// TODO add your handling code here:
    if(this.CbxmissingstationdataZero.isSelected()){
        if(this.CbxFixmissingstationdata.isSelected())
            this.CbxFixmissingstationdata.setSelected(false);
    }
}//GEN-LAST:event_CbxmissingstationdataZeroActionPerformed

    private void tbSimulationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbSimulationStateChanged
    }//GEN-LAST:event_tbSimulationStateChanged

    private void btnSaveSimResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSimResultActionPerformed
        this.saveSimulationResult();
    }//GEN-LAST:event_btnSaveSimResultActionPerformed

    private void btnRunSimulationPluginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunSimulationPluginActionPerformed
        runSimulationPlugin();
    }//GEN-LAST:event_btnRunSimulationPluginActionPerformed

    private void cbxSimulationForCalibrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSimulationForCalibrationActionPerformed
        // TODO add your handling code here:
        SimulationModeforCalibration(this.cbxSimulationForCalibration.isSelected());
    }//GEN-LAST:event_cbxSimulationForCalibrationActionPerformed

    private void cbxUseSimulationDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxUseSimulationDataActionPerformed
        // TODO add your handling code here:
        SimulationModeAction(this.cbxUseSimulationData.isSelected());
    }//GEN-LAST:event_cbxUseSimulationDataActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CbxFixmissingstationdata;
    private javax.swing.JCheckBox CbxmissingstationdataZero;
    private javax.swing.JButton btnEvaluate;
    private javax.swing.JButton btnRunSimulationPlugin;
    private javax.swing.JButton btnSaveConfig;
    private javax.swing.JButton btnSaveSimResult;
    private javax.swing.JComboBox cbxDuration;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JComboBox cbxOutputDirection;
    private javax.swing.JComboBox cbxPlugins;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JCheckBox cbxSimulationForCalibration;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JCheckBox cbxStationDataAcceleration;
    private javax.swing.JCheckBox cbxStationDataAlaneFlow;
    private javax.swing.JCheckBox cbxStationDataDensity;
    private javax.swing.JCheckBox cbxStationDataOccupancy;
    private javax.swing.JCheckBox cbxStationDataSpeed;
    private javax.swing.JCheckBox cbxStationDataTotalFlow;
    private javax.swing.JCheckBox cbxUseSimulationData;
    private javax.swing.JComboBox cbxsimulationresult;
    private javax.swing.JCheckBox chkCM;
    private javax.swing.JCheckBox chkCMH;
    private javax.swing.JCheckBox chkCSV;
    private javax.swing.JCheckBox chkConfigUseMRFInput;
    private javax.swing.JCheckBox chkContour;
    private javax.swing.JCheckBox chkDVH;
    private javax.swing.JCheckBox chkDetectorDensity;
    private javax.swing.JCheckBox chkDetectorFlow;
    private javax.swing.JCheckBox chkDetectorOccupancy;
    private javax.swing.JCheckBox chkDetectorSpeed;
    private javax.swing.JCheckBox chkExcel;
    private javax.swing.JCheckBox chkLVMT;
    private javax.swing.JCheckBox chkMRF;
    private javax.swing.JCheckBox chkONLYHOV;
    private javax.swing.JCheckBox chkSV;
    private javax.swing.JCheckBox chkTT;
    private javax.swing.JCheckBox chkVHT;
    private javax.swing.JCheckBox chkVMT;
    private javax.swing.JCheckBox chkWOAUX;
    private javax.swing.JCheckBox chkWOHOV;
    private javax.swing.JCheckBox chkWOWAVE;
    private javax.swing.JCheckBox chkWithLaneConfig;
    private javax.swing.JCheckBox chkWithoutLaneConfig;
    private javax.swing.JCheckBox chkWithoutVirtualStations;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox6;
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
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JTabbedPane jTabbedPane2;
    private org.jdesktop.swingx.JXMapKit jmKit;
    private javax.swing.JTabbedPane mainTab;
    private javax.swing.JMenuItem menuItemRouteEditorManual;
    private javax.swing.JMenu menuTools;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JPanel panContourSettingAverageFlow;
    private javax.swing.JPanel panContourSettingDensity;
    private javax.swing.JPanel panContourSettingSpeed;
    private javax.swing.JPanel panContourSettingTotalFlow;
    private javax.swing.JPanel panRouteCreation;
    private javax.swing.JPanel tbDataPerformance;
    private javax.swing.JTabbedPane tbSimulation;
    private javax.swing.JTextField tbxCongestionThresholdSpeed;
    private javax.swing.JTextField tbxCriticalDensity;
    private javax.swing.JTextField tbxLaneCapacity;
    private javax.swing.JTextField tbxOutputFolder;
    // End of variables declaration//GEN-END:variables

}
