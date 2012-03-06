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
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.simobjects.SimObjects;
import edu.umn.natsrl.evaluation.ContourPlotter;
import edu.umn.natsrl.evaluation.ContourType;
import edu.umn.natsrl.evaluation.EvaluationOption;
import edu.umn.natsrl.evaluation.OutputDirection;
import edu.umn.natsrl.sfim.SfimTicasPlugin;
import edu.umn.natsrl.ticas.plugin.ITicasAfterSimulation;
import edu.umn.natsrl.ticas.plugin.PluginInfo;
import edu.umn.natsrl.ticas.plugin.PluginInfoLoader;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.PluginType;
import edu.umn.natsrl.ticas.plugin.metering.MeteringSimulation;
import edu.umn.natsrl.infraviewer.InfraViewerPlugin;
import edu.umn.natsrl.ticas.plugin.datareader.DataReader;
import edu.umn.natsrl.ticas.plugin.srte.SpeedRecoveryTimeEstimation;
import edu.umn.natsrl.ticas.plugin.srte.TICASPluginSRTE;
import edu.umn.natsrl.ticas.plugin.vissimcalibration.VissimEvaluator;
import edu.umn.natsrl.ticas.plugin.traveltimeIndexer.TTIndexterPlugin;
import edu.umn.natsrl.util.ModalFrameUtil;
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

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class TICAS2FrameOldVersion extends javax.swing.JFrame implements ITicasAfterSimulation {

    private final String OPTION_FILE = "ticas.cfg";
    private TMO tmo;
    private Vector<Section> sections = new Vector<Section>();
    private Vector<PluginInfo> pluginInfos = new Vector<PluginInfo>();
    private final int width = 980;
    private final int height = 660;
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
    private boolean addDataReaderPlugin = false;
    private boolean addSRTE = true;
    
    public TICAS2FrameOldVersion(TMO tmo) {

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
        this.setTitle("Traffic Information & Condition Analysis System");
        this.setSize(new Dimension(width, height));
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getResource("/edu/umn/natsrl/ticas/resources/ticas_icon.png")).getImage());        
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }

    /**
     * Initialize interface
     * this method must called after initializing TMO object
     */
    public void init() {

        DateChecker dc = DateChecker.getInstance();

        loadSection();
        loadPlugins();
        
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
        
        this.tbxTrafficDataUrl.setText(InfraConstants.TRAFFIC_DATA_URL);
        this.tbxTrafficXmlUrl.setText(InfraConstants.TRAFFIC_CONFIG_URL);
        
         this.tblSimResults.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                int row = tblSimResults.rowAtPoint(e.getPoint());
                int col = tblSimResults.columnAtPoint(e.getPoint());
                DefaultTableModel tm = (DefaultTableModel)tblSimResults.getModel();
                SimulationResult sr = (SimulationResult)tm.getValueAt(row, 1);                
                if(sr == null) return;
                showSimResultInfo(sr);                
             }
         });
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
        if(selectedSectionIndex < this.cbxSections.getItemCount()-1) this.cbxSections.setSelectedIndex(selectedSectionIndex);
        
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
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());

        this.cbxSections.removeAllItems();

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
        if(addSFIMPlugin) {
            PluginInfo sfimPlugin = new PluginInfo("SFIM", PluginType.SIMULATION, SfimTicasPlugin.class);
            addSimulationPlugins(sfimPlugin);            
        }
        if(addSimEvaluatorPlugin) {
            PluginInfo simEvaluator = new PluginInfo("Simulation Evaluator", PluginType.SIMULATION, VissimEvaluator.class);
            addSimulationPlugins(simEvaluator);            
        }
        
        if(addMeteringPlugin) {
            PluginInfo meteringPlugin = new PluginInfo("Metering Simulation", PluginType.SIMULATION, MeteringSimulation.class);
            addSimulationPlugins(meteringPlugin);            
        }        

        if(addInfraViewerPlugin) {
            PluginInfo infraViewer = new PluginInfo("Infra Viewer", PluginType.TOOL, InfraViewerPlugin.class);        
            addTools(infraViewer);            
        }
        
        if(addVSLEvaulatorPlugin) {
            PluginInfo vslEvaluator = new PluginInfo("VSL Evaluator", PluginType.TOOL, TTIndexterPlugin.class);
            addTools(vslEvaluator);
        }
        
        if(addDataReaderPlugin) {
            PluginInfo dataReader = new PluginInfo("Data Reader", PluginType.TOOL, DataReader.class);       
            addTools(dataReader);
        }
        
        if(addSRTE) {
            PluginInfo dataReader = new PluginInfo("Speed Recovery Time Estimation", PluginType.TOOL, SpeedRecoveryTimeEstimation.class);       
            addTools(dataReader);                        
        }
        
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
        final TICAS2FrameOldVersion frame = this;

        item.setFont(new Font("Verdana", 0, 12));
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                String menuText = item.getText();
                for (PluginInfo pi : pluginInfos) {
                    if (menuText.equals(pi.getName())) {
                        try {
                            PluginFrame sd = new PluginFrame(frame);
                            sd.setLocationRelativeTo(rootPane);
                            if(pi.getType().isSimulationPlugin()) {
                                sd.setLocation(getLocation());
                            }
                            sd.setPluginInfo(pi);
                            ModalFrameUtil.showAsModal(sd, frame);                            
                            //sd.setVisible(true);
                            //frame.setVisible(false);
                            
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

            if(this.simSection == null || this.simPeriod == null) {
                this.btnSaveSimResult.setEnabled(false);
            } else {
                this.btnSaveSimResult.setEnabled(true);                
            }
            
            if(sf.getPluginInfo().getType().isSimulationPlugin()) {
                this.setLocation(sf.getLocation());
            }
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
        simSection.loadData(simPeriod, true);
        
        try {
            System.out.println("= Simulation =======");
            System.out.println("Period : data=" + simPeriod.getTimeline().length + ", interval="+simPeriod.interval);
            System.out.println("Data Length = " + simSection.getRNodes().get(0).getDetectors()[0].getSpeed().length);
        } catch(Exception ex) {
            ex.printStackTrace();
        }        
        
        TICASOption selectedOption = getOption();
        EvaluationOption opt = selectedOption.getEvaluationOption();
        
        opt.setSection(simSection);
        opt.setPeriods(new Period[]{simPeriod});
        opt.setSimulationMode(true);
        
        if (selectedOption == null) {
            JOptionPane.showMessageDialog(null, "Check options");
            return;
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
        t.schedule(new EvaluateTask(selectedOption, options, rd), 10);
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
        DefaultTableModel tm = (DefaultTableModel)tblSimResults.getModel();        
        tm.getDataVector().removeAllElements();
        this.tblSimResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);       
        this.tblSimResults.getColumnModel().getColumn(0).setPreferredWidth(90);
        this.tblSimResults.getColumnModel().getColumn(1).setPreferredWidth(205);
        
        File file = new File(SimulationResult.SAVE_PROP_DIR);  
        File[] files = file.listFiles();  
        if(files == null) return;
        Calendar c = Calendar.getInstance();
        tm.getDataVector().removeAllElements();
        tm.fireTableDataChanged();
        
        ArrayList<SimulationResult> res = new ArrayList<SimulationResult>();
        for (int i = 0; i < files.length; i++)  
        {  
            if(files[i].isDirectory()) continue;
           SimulationResult s = SimulationResult.load(files[i]);
           if(s == null) continue;
           res.add(s);
        }
        
        Collections.sort(res);
        
        for(SimulationResult s : res)
        {
            if(s != null) {
               c.setTime(s.getCreated());
               String created = String.format("%02d-%02d-%02d", c.get(Calendar.YEAR),  c.get(Calendar.MONTH)+1,  c.get(Calendar.DATE));
               tm.addRow(new Object[]{created, s});
            } else {
                System.out.println("Loaded is null");
            }           
        }           
    }

    /**
     * Show simulation result info 
     * @param sr 
     */
    private void showSimResultInfo(SimulationResult sr) {
        this.tbxSimResultName.setText(sr.getName());
        this.tbxSimResultDesc.setText(sr.getDesc());
        this.tbxSimResultRoutes.setText(sr.getSection().getRoutes());
        selectedSimulationResult = sr;
    }    
    
    /**
     * Evaluate with selected simulation result
     */
    private void evaluateWithSimulationResult() {
        
        if(this.selectedSimulationResult == null) {
            JOptionPane.showMessageDialog(rootPane, "Select simulation result");            
            return;
        }
    
        SimObjects.getInstance().reset();        
        
        // set simulation results to detectors
        selectedSimulationResult.setTrafficDataToDetectors();
        
        this.simSection = selectedSimulationResult.getSection();
        this.simPeriod = selectedSimulationResult.getPeriod();
        String sh = this.tbxSimStartHour.getText();
        String sm = this.tbxSimStartMin.getText();
        
        int start_hour=-1, start_min = -1;
        try {
            start_hour = Integer.parseInt(sh);
            start_min = Integer.parseInt(sm);
        } catch(Exception ex) {}

        if( ( start_hour >= 0 && start_hour < 24) && ( start_min >= 0 && start_min < 60)) {
            Date sDate = simPeriod.startDate;
            Date eDate = simPeriod.endDate;
            int diff = (int)(eDate.getTime() - sDate.getTime());
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, start_hour);
            c.set(Calendar.MINUTE, start_min);
            c.set(Calendar.SECOND, 0);
            sDate = c.getTime();
            c.add(Calendar.MILLISECOND, diff-30000);
            eDate = c.getTime();
            this.simPeriod = new Period(sDate, eDate, 30);
        }
        
        this.evaluateSimulation();        
    }    
    
    /**
     * Delete simulation result
     */
    private void deleteSimulationResults() {
        if(JOptionPane.showConfirmDialog(rootPane, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
            if(new File(SimulationResult.SAVE_PROP_DIR + File.separator + SimulationResult.getCacheFileName(selectedSimulationResult.getName())).delete()) {
                new File(SimulationResult.SAVE_DATA_DIR + File.separator + SimulationResult.getCacheFileName(selectedSimulationResult.getName())).delete();
                this.selectedSimulationResult = null;
                this.tbxSimResultDesc.setText("");
                this.tbxSimResultName.setText("");
                this.tbxSimResultRoutes.setText("");
                this.loadSimulationResults();
            } else {                
                JOptionPane.showMessageDialog(rootPane, "Fail : " + SimulationResult.SAVE_PROP_DIR + File.separator + SimulationResult.getCacheFileName(selectedSimulationResult.getName()));
            }
        }
    }    
    
    private void updateSimulationResults() {
        if(JOptionPane.showConfirmDialog(rootPane, "Update?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;        
        this.selectedSimulationResult.update(this.tbxSimResultName.getText(), this.tbxSimResultDesc.getText());        
        this.selectedSimulationResult = null;
        this.tbxSimResultDesc.setText("");
        this.tbxSimResultName.setText("");
        this.tbxSimResultRoutes.setText("");        
        this.loadSimulationResults();
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

        TICASOption selectedOption = getOption();
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
        rd.setVisible(true);        
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

        public EvaluateTask(TICASOption selectedOption, OptionType[] options, RunningDialog rd) {
            this.selectedOption = selectedOption;
            this.option = selectedOption.getEvaluationOption();
            this.options = options;
            this.rd = rd;
        }

        @Override
        public void run() {
            
            Evaluation.clearCache();
            
            boolean hasError = false;
            try {
                System.out.println("Evaluation starts");
                long st = new Date().getTime();
                
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
                ex.printStackTrace();
            }
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
    private TICASOption getOption() {
    
        TICASOption ticasOption = new TICASOption();
        EvaluationOption opt = ticasOption.getEvaluationOption();

        // all checkbox options
        for (EvaluationCheckBox c : EvaluationCheckBox.getCheckBoxes("TICAS")) {
            if (c.isSelected()) {
                opt.addOption(c.getOption());
            }
        }
        
        // section
        Section section = (Section) this.cbxSections.getSelectedItem();
        if (section == null) {
            JOptionPane.showMessageDialog(null, "Make section at first");
            return null;
        }
        opt.setSection(section);

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
        InfraConstants.TRAFFIC_CONFIG_URL = this.tbxTrafficXmlUrl.getText();
        InfraConstants.TRAFFIC_DATA_URL = this.tbxTrafficDataUrl.getText();        
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
            JOptionPane.showMessageDialog(this, "Create section and select the section");
            return false;
        }

        if (!simulationMode && opts.getPeriods().length == 0) {
            JOptionPane.showMessageDialog(this, "Select date");
            return false;
        }

        // check second tab : evaluation metrics
        if (opts.getEvaluationOptions().length == 0) {
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
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        cbxInterval = new javax.swing.JComboBox();
        jLabel18 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        cbxStartHour = new javax.swing.JComboBox();
        cbxEndHour = new javax.swing.JComboBox();
        cbxStartMin = new javax.swing.JComboBox();
        cbxEndMin = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbxDuration = new javax.swing.JComboBox();
        cbxSections = new javax.swing.JComboBox();
        btnSectionInfo = new javax.swing.JButton();
        btnOpenSectionEditor = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
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
        jPanel2 = new javax.swing.JPanel();
        jCheckBox1 = new EvaluationCheckBox("TICAS", OptionType.STATION_SPEED);
        jCheckBox2 = new EvaluationCheckBox("TICAS", OptionType.STATION_TOTAL_FLOW);
        jCheckBox3 = new EvaluationCheckBox("TICAS", OptionType.STATION_AVG_LANE_FLOW);
        jCheckBox4 = new EvaluationCheckBox("TICAS", OptionType.STATION_DENSITY);
        jCheckBox5 = new EvaluationCheckBox("TICAS", OptionType.STATION_OCCUPANCY);
        jCheckBox11 = new EvaluationCheckBox("TICAS", OptionType.STATION_ACCEL);
        jPanel3 = new javax.swing.JPanel();
        chkWithLaneConfig = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OPT_WITH_LANECONFIG);
        chkWithoutLaneConfig = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OPT_WITHOUT_LANECONFIG);
        chkDetectorSpeed = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_SPEED);
        chkDetectorFlow = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_FLOW);
        chkDetectorDensity = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_DENSITY);
        chkDetectorOccupancy = new EvaluationCheckBox("TICAS", OptionType.DETECTOR_OCCUPANCY);
        jPanel11 = new javax.swing.JPanel();
        chkExcel = new EvaluationCheckBox("TICAS", OptionType.OUT_EXCEL);
        chkCSV = new EvaluationCheckBox("TICAS", OptionType.OUT_CSV);
        chkContour = new EvaluationCheckBox("TICAS", OptionType.OUT_CONTOUR);
        jLabel16 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        cbxOutputDirection = new javax.swing.JComboBox();
        tbxOutputFolder = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        btnEvaluate = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox6 = new EvaluationCheckBox("TICAS", OptionType.OPEN_CONTOUR);
        chkWithoutVirtualStations = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_VIRTUAL_STATIONS);
        jPanel8 = new javax.swing.JPanel();
        jCheckBox8 = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_HOV);
        jCheckBox9 = new EvaluationCheckBox("TICAS", OptionType.ONLY_HOV);
        jCheckBox10 = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_WAVETRONICS);
        jCheckBox12 = new EvaluationCheckBox("TICAS", OptionType.WITHOUT_AUXLANE);
        jPanel7 = new javax.swing.JPanel();
        tbSimulation = new javax.swing.JTabbedPane();
        jPanel12 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        btnRunSimulationPlugin = new javax.swing.JButton();
        cbxPlugins = new javax.swing.JComboBox();
        jLabel24 = new javax.swing.JLabel();
        btnSaveSimResult = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSimResults = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbxSimResultRoutes = new javax.swing.JTextArea();
        jLabel27 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbxSimResultDesc = new javax.swing.JTextArea();
        jLabel26 = new javax.swing.JLabel();
        tbxSimResultName = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        lbReloadSimulationResult = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        lbDeleteSimResult = new javax.swing.JLabel();
        lbUpdateSimResult = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        tbxSimStartHour = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        tbxSimStartMin = new javax.swing.JTextField();
        btnEvaulateWithSimResult = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        panContourSettingSpeed = new javax.swing.JPanel();
        panContourSettingTotalFlow = new javax.swing.JPanel();
        panContourSettingDensity = new javax.swing.JPanel();
        panContourSettingAverageFlow = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        tbxCongestionThresholdSpeed = new javax.swing.JTextField();
        tbxLaneCapacity = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tbxCriticalDensity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        chkInterpolate = new EvaluationCheckBox("TICAS", OptionType.FIXING_MISSING_DATA);
        chkUseInputFlowForMRF = new EvaluationCheckBox("TICAS", OptionType.USE_INPUT_FLOW_FOR_MRF);
        jPanel6 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        tbxTrafficXmlUrl = new javax.swing.JTextField();
        tbxTrafficDataUrl = new javax.swing.JTextField();
        btnSaveConfig = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuItemRouteEditor = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        menuItemRouteEditorManual = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        mainTab.setFont(new java.awt.Font("Verdana", 0, 12));

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Route and Times", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 11));
        jLabel17.setText("Time Interval");

        cbxInterval.setFont(new java.awt.Font("Verdana", 0, 12));

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 11));
        jLabel18.setText("Start Time");

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 11));
        jLabel20.setText("End Time");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.setSelectedIndex(7);
        cbxStartHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStartHourActionPerformed(evt);
            }
        });

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setSelectedIndex(8);
        cbxEndHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEndHourActionPerformed(evt);
            }
        });

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel14.setText(":");

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel15.setText(":");

        jLabel3.setText("or");

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel5.setText("for");

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel6.setText("hour");

        cbxDuration.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDurationActionPerformed(evt);
            }
        });

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 10));
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

        jLabel10.setFont(new java.awt.Font("Verdana", 1, 10));
        jLabel10.setText("Dates");

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 10));
        jLabel11.setText("Times");

        jLabel12.setFont(new java.awt.Font("Verdana", 1, 10));
        jLabel12.setText("Route");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(natsrlCalendar, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel20))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbxEndHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel10Layout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cbxStartMin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addComponent(jLabel11)
                            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel10Layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel6))
                                .addGroup(jPanel10Layout.createSequentialGroup()
                                    .addComponent(jLabel17)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSectionInfo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnOpenSectionEditor))
                            .addComponent(jLabel12)
                            .addComponent(jLabel10))
                        .addGap(34, 34, 34))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSectionInfo)
                    .addComponent(btnOpenSectionEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addGap(4, 4, 4)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(70, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Evaluation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        chkVMT.setFont(new java.awt.Font("Verdana", 0, 12));
        chkVMT.setText("Vehicle Miles Traveled (VMT)");

        chkLVMT.setFont(new java.awt.Font("Verdana", 0, 12));
        chkLVMT.setText("Lost VMT for congestion (LVMT)");
        chkLVMT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLVMTActionPerformed(evt);
            }
        });

        chkVHT.setFont(new java.awt.Font("Verdana", 0, 12));
        chkVHT.setText("Vehicle Hour Traveled (VHT)");

        chkDVH.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDVH.setText("Delayed Vehicle Hours (DVH)");

        chkMRF.setFont(new java.awt.Font("Verdana", 0, 12));
        chkMRF.setText("Mainlane and Ramp Flow Rates");

        chkTT.setFont(new java.awt.Font("Verdana", 0, 12));
        chkTT.setText("Travel Time (TT)");

        chkCM.setFont(new java.awt.Font("Verdana", 0, 12));
        chkCM.setText("Congested Miles (CM)");
        chkCM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCMActionPerformed(evt);
            }
        });

        chkCMH.setFont(new java.awt.Font("Verdana", 0, 12));
        chkCMH.setText("Congested Miles * Hours (CMH)");
        chkCMH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCMHActionPerformed(evt);
            }
        });

        chkSV.setFont(new java.awt.Font("Verdana", 0, 12));
        chkSV.setText("Speed Variations (SV)");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkTT)
                    .addComponent(chkCM)
                    .addComponent(chkSV)
                    .addComponent(chkCMH)
                    .addComponent(chkVMT)
                    .addComponent(chkLVMT)
                    .addComponent(chkVHT)
                    .addComponent(chkDVH)
                    .addComponent(chkMRF))
                .addContainerGap(9, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkVMT)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkLVMT)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVHT)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDVH)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMRF)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkTT)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkCM)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkCMH)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSV)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Station Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jCheckBox1.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox1.setText("Speed");

        jCheckBox2.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox2.setText("Total Flow");

        jCheckBox3.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox3.setText("Avg. Lane Flow");

        jCheckBox4.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox4.setText("Density");

        jCheckBox5.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox5.setText("Occupancy");

        jCheckBox11.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox11.setText("Acceleration");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBox4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox2))
                        .addGap(18, 18, 18)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox11)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox2)
                            .addComponent(jCheckBox5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox11))))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Individual Detector Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        chkWithLaneConfig.setFont(new java.awt.Font("Verdana", 0, 12));
        chkWithLaneConfig.setText("With Lane Config");
        chkWithLaneConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWithLaneConfigActionPerformed(evt);
            }
        });

        chkWithoutLaneConfig.setFont(new java.awt.Font("Verdana", 0, 12));
        chkWithoutLaneConfig.setText("Without Lane Config");
        chkWithoutLaneConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWithoutLaneConfigActionPerformed(evt);
            }
        });

        chkDetectorSpeed.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDetectorSpeed.setText("Speed");
        chkDetectorSpeed.setEnabled(false);

        chkDetectorFlow.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDetectorFlow.setText("Flow");
        chkDetectorFlow.setEnabled(false);

        chkDetectorDensity.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDetectorDensity.setText("Density");
        chkDetectorDensity.setEnabled(false);

        chkDetectorOccupancy.setFont(new java.awt.Font("Verdana", 0, 12));
        chkDetectorOccupancy.setText("Occupancy");
        chkDetectorOccupancy.setEnabled(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkWithLaneConfig)
                    .addComponent(chkWithoutLaneConfig))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkDetectorSpeed)
                    .addComponent(chkDetectorFlow)
                    .addComponent(chkDetectorDensity)
                    .addComponent(chkDetectorOccupancy)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(chkDetectorSpeed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDetectorFlow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDetectorDensity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkDetectorOccupancy))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(chkWithLaneConfig)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkWithoutLaneConfig))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Execution", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N
        jPanel11.setFont(new java.awt.Font("Verdana", 0, 10));

        chkExcel.setFont(new java.awt.Font("Verdana", 0, 12));
        chkExcel.setText("Excel");

        chkCSV.setFont(new java.awt.Font("Verdana", 0, 12));
        chkCSV.setText("CSV");

        chkContour.setFont(new java.awt.Font("Verdana", 0, 12));
        chkContour.setText("Contour");
        chkContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkContourActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel16.setText("Time Range");

        jLabel21.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel21.setText("in");

        cbxOutputDirection.setFont(new java.awt.Font("Verdana", 0, 12));

        tbxOutputFolder.setFont(new java.awt.Font("Verdana", 0, 10));

        jButton5.setFont(new java.awt.Font("Verdana", 0, 12));
        jButton5.setText("Browse");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        btnEvaluate.setFont(new java.awt.Font("Verdana", 1, 14));
        btnEvaluate.setForeground(new java.awt.Color(255, 0, 0));
        btnEvaluate.setText("RUN");
        btnEvaluate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEvaluateActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel1.setText("Output Folder");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel2.setText("Output Format");

        jCheckBox6.setFont(new java.awt.Font("Verdana", 0, 10));
        jCheckBox6.setSelected(true);
        jCheckBox6.setText("Open contour after finishing evaluation");

        chkWithoutVirtualStations.setFont(new java.awt.Font("Verdana", 0, 11));
        chkWithoutVirtualStations.setText("Without virtual stations");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox6)
                            .addComponent(jLabel2)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(chkExcel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkCSV)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkContour))
                            .addComponent(chkWithoutVirtualStations)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(jLabel16)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel21)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbxOutputDirection, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel11Layout.createSequentialGroup()
                                        .addComponent(tbxOutputFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(11, 11, 11)))
                        .addGap(12, 12, 12))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(btnEvaluate, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(21, Short.MAX_VALUE))))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkExcel)
                    .addComponent(chkCSV)
                    .addComponent(chkContour))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkWithoutVirtualStations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cbxOutputDirection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbxOutputFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addGap(18, 18, 18)
                .addComponent(btnEvaluate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detector Filter", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jCheckBox8.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox8.setText("w/o HOV");

        jCheckBox9.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox9.setText("only HOV");

        jCheckBox10.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox10.setText("w/o wavetronics");

        jCheckBox12.setFont(new java.awt.Font("Verdana", 0, 12));
        jCheckBox12.setText("w/o Aux");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox8)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox9)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox12)
                .addGap(18, 18, 18)
                .addComponent(jCheckBox10)
                .addContainerGap(84, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox8)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox12)
                    .addComponent(jCheckBox10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel11, 0, 268, Short.MAX_VALUE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(33, 33, 33))
        );

        mainTab.addTab("Data/Performance", jPanel9);

        tbSimulation.setFocusable(false);
        tbSimulation.setFont(new java.awt.Font("Verdana", 0, 12));
        tbSimulation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbSimulationStateChanged(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel19.setText("After saving simulation result, you can evaluate in 'Simulation Result History' tab.");

        btnRunSimulationPlugin.setFont(new java.awt.Font("Verdana", 0, 12));
        btnRunSimulationPlugin.setText("Run Simulation");
        btnRunSimulationPlugin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunSimulationPluginActionPerformed(evt);
            }
        });

        cbxPlugins.setFont(new java.awt.Font("Verdana", 0, 12));

        jLabel24.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel24.setText("Simulation Plugins");

        btnSaveSimResult.setFont(new java.awt.Font("Verdana", 0, 12));
        btnSaveSimResult.setText("Save Simulation Result");
        btnSaveSimResult.setEnabled(false);
        btnSaveSimResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSimResultActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(cbxPlugins, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRunSimulationPlugin))
                    .addComponent(jLabel19)
                    .addComponent(btnSaveSimResult, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(353, Short.MAX_VALUE))
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
                .addGap(18, 18, 18)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveSimResult, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
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
                .addContainerGap(283, Short.MAX_VALUE))
        );

        tbSimulation.addTab("New Simulation", jPanel12);

        tblSimResults.setFont(new java.awt.Font("Verdana", 0, 12));
        tblSimResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Created", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblSimResults);
        tblSimResults.getColumnModel().getColumn(0).setPreferredWidth(100);

        tbxSimResultRoutes.setColumns(20);
        tbxSimResultRoutes.setEditable(false);
        tbxSimResultRoutes.setFont(new java.awt.Font("Verdana", 0, 12));
        tbxSimResultRoutes.setLineWrap(true);
        tbxSimResultRoutes.setRows(5);
        jScrollPane3.setViewportView(tbxSimResultRoutes);

        jLabel27.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel27.setText("Routes");

        tbxSimResultDesc.setColumns(20);
        tbxSimResultDesc.setFont(new java.awt.Font("Verdana", 0, 12));
        tbxSimResultDesc.setLineWrap(true);
        tbxSimResultDesc.setRows(5);
        jScrollPane2.setViewportView(tbxSimResultDesc);

        jLabel26.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel26.setText("Description");

        tbxSimResultName.setFont(new java.awt.Font("Verdana", 0, 12));

        jLabel25.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel25.setText("Name");

        lbReloadSimulationResult.setFont(new java.awt.Font("Verdana", 0, 12));
        lbReloadSimulationResult.setText("Reload");
        lbReloadSimulationResult.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbReloadSimulationResultMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbxSimResultName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 271, Short.MAX_VALUE)
                        .addComponent(lbReloadSimulationResult))
                    .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(lbReloadSimulationResult))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbxSimResultName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel29.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel29.setText("Edit");

        lbDeleteSimResult.setFont(new java.awt.Font("Verdana", 0, 11));
        lbDeleteSimResult.setText("Delete selected result");
        lbDeleteSimResult.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbDeleteSimResult.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbDeleteSimResultMouseClicked(evt);
            }
        });

        lbUpdateSimResult.setFont(new java.awt.Font("Verdana", 0, 11));
        lbUpdateSimResult.setText("Update selected result");
        lbUpdateSimResult.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbUpdateSimResult.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbUpdateSimResultMouseClicked(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel31.setText("Evaluate");

        jLabel28.setFont(new java.awt.Font("Verdana", 0, 11));
        jLabel28.setText("Sim Start Hour :");

        tbxSimStartHour.setFont(new java.awt.Font("Verdana", 0, 11));
        tbxSimStartHour.setText("6");

        jLabel32.setText(":");

        tbxSimStartMin.setFont(new java.awt.Font("Verdana", 0, 11));
        tbxSimStartMin.setText("0");

        btnEvaulateWithSimResult.setFont(new java.awt.Font("Verdana", 0, 12));
        btnEvaulateWithSimResult.setText("Evaluate");
        btnEvaulateWithSimResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEvaulateWithSimResultActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel29, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbDeleteSimResult, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbUpdateSimResult, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(11, 11, 11))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEvaulateWithSimResult, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(tbxSimStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tbxSimStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE))
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(11, 11, 11)))
                        .addContainerGap())))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbDeleteSimResult)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbUpdateSimResult)
                .addGap(46, 46, 46)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbxSimStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32)
                    .addComponent(tbxSimStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEvaulateWithSimResult, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(221, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(38, 38, 38))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                .addContainerGap())
        );

        tbSimulation.addTab("Simulation Result History", jPanel13);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, 897, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainTab.addTab("W/Simulation", jPanel7);

        jTabbedPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Contour Setting", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N
        jTabbedPane2.setFont(new java.awt.Font("Verdana", 0, 12));

        panContourSettingSpeed.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Speed", panContourSettingSpeed);

        panContourSettingTotalFlow.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Total Flow", panContourSettingTotalFlow);

        panContourSettingDensity.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Density", panContourSettingDensity);

        panContourSettingAverageFlow.setLayout(new java.awt.BorderLayout());
        jTabbedPane2.addTab("Occupancy", panContourSettingAverageFlow);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Evaluation Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel4.setText("Congestion Threshold Speed");

        tbxCongestionThresholdSpeed.setText("45");

        tbxLaneCapacity.setText("2200");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel7.setText("Default Lane Capacity");

        tbxCriticalDensity.setText("40");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel9.setText("Critical Density");

        chkInterpolate.setFont(new java.awt.Font("Verdana", 0, 12));
        chkInterpolate.setSelected(true);
        chkInterpolate.setText("Interpolate missing station data");

        chkUseInputFlowForMRF.setFont(new java.awt.Font("Verdana", 0, 12));
        chkUseInputFlowForMRF.setText("Use input flow of ramp for MRF");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkUseInputFlowForMRF)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tbxCriticalDensity)
                            .addComponent(tbxLaneCapacity)
                            .addComponent(tbxCongestionThresholdSpeed, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))
                    .addComponent(chkInterpolate))
                .addContainerGap(210, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tbxCongestionThresholdSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tbxLaneCapacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tbxCriticalDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(chkInterpolate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkUseInputFlowForMRF)
                .addContainerGap(85, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "URL Setting", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel22.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel22.setText("Traffic Configuration File");

        jLabel23.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel23.setText("Traffic Data Folder");

        tbxTrafficXmlUrl.setFont(new java.awt.Font("Verdana", 0, 10));

        tbxTrafficDataUrl.setFont(new java.awt.Font("Verdana", 0, 10));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(jLabel23))
                .addGap(41, 41, 41)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbxTrafficDataUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addComponent(tbxTrafficXmlUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tbxTrafficXmlUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(tbxTrafficDataUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnSaveConfig.setFont(new java.awt.Font("Verdana", 0, 12));
        btnSaveConfig.setText("Save Configurations");
        btnSaveConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSaveConfig, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSaveConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                .addContainerGap())
        );

        mainTab.addTab("Contour/Parameters", jPanel5);

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Nothern Advanced Transportation Research Lab. @ University of Minnesota Duluth");

        jMenuBar1.setFont(new java.awt.Font("Verdana", 0, 12));

        jMenu1.setText("File");
        jMenu1.setFont(new java.awt.Font("Verdana", 0, 12));

        jMenuItem6.setFont(new java.awt.Font("Verdana", 0, 12));
        jMenuItem6.setText("Geo Update");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        jMenuItem2.setFont(new java.awt.Font("Verdana", 0, 12));
        jMenuItem2.setText("Save Current Setting");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem5.setFont(new java.awt.Font("Verdana", 0, 12));
        jMenuItem5.setText("Clear Cache");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuItem3.setFont(new java.awt.Font("Verdana", 0, 12));
        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        menuTools.setText("Tool");
        menuTools.setFont(new java.awt.Font("Verdana", 0, 12));

        menuItemRouteEditor.setFont(new java.awt.Font("Verdana", 0, 12));
        menuItemRouteEditor.setText("Route Editor");
        menuItemRouteEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRouteEditorActionPerformed(evt);
            }
        });
        menuTools.add(menuItemRouteEditor);

        jMenuBar1.add(menuTools);

        jMenu3.setText("Help");
        jMenu3.setFont(new java.awt.Font("Verdana", 0, 12));

        menuItemRouteEditorManual.setFont(new java.awt.Font("Verdana", 0, 12));
        menuItemRouteEditorManual.setText("Route Editor Manual");
        menuItemRouteEditorManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRouteEditorManualActionPerformed(evt);
            }
        });
        jMenu3.add(menuItemRouteEditorManual);

        jMenuItem1.setFont(new java.awt.Font("Verdana", 0, 12));
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 936, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        TICASOption.save(getOption(), OPTION_FILE);
        JOptionPane.showMessageDialog(this, "Configuration has been saved", "Info", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        this.openSectionEditor();
}//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void btnSectionInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSectionInfoActionPerformed
        this.openSectionInfoDialog();
}//GEN-LAST:event_btnSectionInfoActionPerformed

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
}//GEN-LAST:event_cbxSectionsActionPerformed

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

    private void btnRunSimulationPluginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunSimulationPluginActionPerformed
        runSimulationPlugin();
}//GEN-LAST:event_btnRunSimulationPluginActionPerformed

    private void btnEvaluateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEvaluateActionPerformed
        try {
            evalute();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnEvaluateActionPerformed

    private void menuItemRouteEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRouteEditorActionPerformed
        this.openSectionEditor();
    }//GEN-LAST:event_menuItemRouteEditorActionPerformed

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

    private void btnSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveConfigActionPerformed
        TICASOption.save(getOption(), OPTION_FILE);
        JOptionPane.showMessageDialog(this, "Configuration has been saved", "Info", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_btnSaveConfigActionPerformed

    private void btnSaveSimResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSimResultActionPerformed
        this.saveSimulationResult();
    }//GEN-LAST:event_btnSaveSimResultActionPerformed

    private void tbSimulationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbSimulationStateChanged
        if(this.tbSimulation.getSelectedIndex() == 1) {
            loadSimulationResults();
        }
    }//GEN-LAST:event_tbSimulationStateChanged

    private void lbReloadSimulationResultMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbReloadSimulationResultMouseClicked
        this.loadSimulationResults();
    }//GEN-LAST:event_lbReloadSimulationResultMouseClicked

    private void btnEvaulateWithSimResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEvaulateWithSimResultActionPerformed
        this.evaluateWithSimulationResult();
    }//GEN-LAST:event_btnEvaulateWithSimResultActionPerformed

    private void lbDeleteSimResultMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDeleteSimResultMouseClicked
        this.deleteSimulationResults();
    }//GEN-LAST:event_lbDeleteSimResultMouseClicked

    private void lbUpdateSimResultMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbUpdateSimResultMouseClicked
        this.updateSimulationResults();
    }//GEN-LAST:event_lbUpdateSimResultMouseClicked

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEvaluate;
    private javax.swing.JButton btnEvaulateWithSimResult;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnRunSimulationPlugin;
    private javax.swing.JButton btnSaveConfig;
    private javax.swing.JButton btnSaveSimResult;
    private javax.swing.JButton btnSectionInfo;
    private javax.swing.JComboBox cbxDuration;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JComboBox cbxOutputDirection;
    private javax.swing.JComboBox cbxPlugins;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JCheckBox chkCM;
    private javax.swing.JCheckBox chkCMH;
    private javax.swing.JCheckBox chkCSV;
    private javax.swing.JCheckBox chkContour;
    private javax.swing.JCheckBox chkDVH;
    private javax.swing.JCheckBox chkDetectorDensity;
    private javax.swing.JCheckBox chkDetectorFlow;
    private javax.swing.JCheckBox chkDetectorOccupancy;
    private javax.swing.JCheckBox chkDetectorSpeed;
    private javax.swing.JCheckBox chkExcel;
    private javax.swing.JCheckBox chkInterpolate;
    private javax.swing.JCheckBox chkLVMT;
    private javax.swing.JCheckBox chkMRF;
    private javax.swing.JCheckBox chkSV;
    private javax.swing.JCheckBox chkTT;
    private javax.swing.JCheckBox chkUseInputFlowForMRF;
    private javax.swing.JCheckBox chkVHT;
    private javax.swing.JCheckBox chkVMT;
    private javax.swing.JCheckBox chkWithLaneConfig;
    private javax.swing.JCheckBox chkWithoutLaneConfig;
    private javax.swing.JCheckBox chkWithoutVirtualStations;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
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
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
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
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JLabel lbDeleteSimResult;
    private javax.swing.JLabel lbReloadSimulationResult;
    private javax.swing.JLabel lbUpdateSimResult;
    private javax.swing.JTabbedPane mainTab;
    private javax.swing.JMenuItem menuItemRouteEditor;
    private javax.swing.JMenuItem menuItemRouteEditorManual;
    private javax.swing.JMenu menuTools;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JPanel panContourSettingAverageFlow;
    private javax.swing.JPanel panContourSettingDensity;
    private javax.swing.JPanel panContourSettingSpeed;
    private javax.swing.JPanel panContourSettingTotalFlow;
    private javax.swing.JTabbedPane tbSimulation;
    private javax.swing.JTable tblSimResults;
    private javax.swing.JTextField tbxCongestionThresholdSpeed;
    private javax.swing.JTextField tbxCriticalDensity;
    private javax.swing.JTextField tbxLaneCapacity;
    private javax.swing.JTextField tbxOutputFolder;
    private javax.swing.JTextArea tbxSimResultDesc;
    private javax.swing.JTextField tbxSimResultName;
    private javax.swing.JTextArea tbxSimResultRoutes;
    private javax.swing.JTextField tbxSimStartHour;
    private javax.swing.JTextField tbxSimStartMin;
    private javax.swing.JTextField tbxTrafficDataUrl;
    private javax.swing.JTextField tbxTrafficXmlUrl;
    // End of variables declaration//GEN-END:variables

}
