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
package edu.umn.natsrl.ticas.plugin.srte2;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.gadget.calendar.DayToggleListener;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.ticas.DateChecker;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import edu.umn.natsrl.ticas.plugin.PluginInfo;
import edu.umn.natsrl.ticas.plugin.PluginType;
import edu.umn.natsrl.ticas.plugin.srte2.SMOOTHING;
import edu.umn.natsrl.util.ModalFrameUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEMainPanel extends javax.swing.JPanel {
    
    private TMO tmo = TMO.getInstance();    
    private Vector<Section> sections = new Vector<Section>();
    private SRTEConfig config = SRTEConfig.getInstance();
    private PluginFrame simFrame;

    private PrintStream backupOut;
    private PrintStream backupErr;
    private Vector<TimeEventLists> timeeventlists = new Vector<TimeEventLists>();
    private TimeEventLists selectedEventList;
    
    SRTEAlgorithm srte;
    /**
     * Constructor
     */
    public SRTEMainPanel(PluginFrame parent) {
        this();
        this.simFrame = parent;
        this.simFrame.setSize(1310, 700);
    }
    
    /**
     * Constructor
     */
    public SRTEMainPanel() {
        initComponents();
        
        this.setSize(800, 580);        
        config.load();
        
        loadSection();
        graphButtonAction();
        
        //set calendar
        init();
        
        if(config.isLoaded()) {
            this.tbxFilterSize.setText(config.getString("SMOOTHING_FILTERSIZE"));
            this.tbxQThreshold.setText(config.getString("QUANTIZATION_THRESHOLD"));

//            Calendar c = Calendar.getInstance();
//            c.set(config.getInt("START_YEAR"), config.getInt("START_MONTH")-1, config.getInt("START_DAY"));
//            this.calStartDate.setSelectedDate(c);
//            c.set(config.getInt("END_YEAR"), config.getInt("END_MONTH")-1, config.getInt("END_DAY"));
//            this.calEndDate.setSelectedDate(c);
//
//            this.cbxStartHour.setSelectedIndex(config.getInt("START_HOUR"));
//            this.cbxEndHour.setSelectedIndex(config.getInt("END_HOUR"));
//            this.cbxStartMin.setSelectedIndex(config.getInt("START_MIN"));
//            this.cbxEndMin.setSelectedIndex(config.getInt("END_MIN"));
//            
            this.tbxRCRNofM.setText(config.getString("RCR_K"));
            this.tbxKEYSpeedLimit.setText(config.getString("KEYSPEED"));
            this.tbxSRTFSpeedLimit.setText(config.getString("SRTFSPEED"));
            this.tbxRCRboundary.setText(config.getString("RCRBoundary"));
            this.tbxRSTDelta.setText(config.getString("RSTDELTA"));
            
//            this.tbxTPR_U.setText(config.getString("TPR_U"));
//            this.tbxTPR_hour.setText(config.getString("TPR_HOUR"));
//            this.tbxSDC_k.setText(config.getString("SDC_K"));
            
            this.cbxTimeInverval.setSelectedItem(Interval.get(config.getInt(SRTEConfig.TIMEINTERVAL)));
            this.cbxsmoothing.setSelectedIndex(config.getInt(SRTEConfig.SMOOTHINGOPTION));
        }
    }


    
    /**
     * On clicked start button
     * Start to collect data and display chart
     */
    private void startEstimation() throws OutOfMemoryError, Exception {
        startEstimation(null);
    }
    /**
     * 
     * @param el
     * @throws OutOfMemoryError
     * @throws Exception 
     */
    private void startEstimation(TimeEventLists el) throws OutOfMemoryError, Exception{
        boolean listmode = true;
        if(el == null)
            listmode = false;
        
        clearMemory();
        srte = null;
        graphButtonAction();
        
        
        if(this.cbxisDebug.isSelected())
            redirectOutput();
        System.out.println("\n--------------------------------------------------------");
        this.btnStart.setEnabled(false);
        this.btnListStart.setEnabled(false);
        this.updateOption();
        config.save();
        
        srte = new SRTEAlgorithm(this.cbxisSave.isSelected());
        SRTEAlgorithm.AlogorithmEndListener cb = new SRTEAlgorithm.AlogorithmEndListener(){
            @Override
            public void onEndMessage(boolean msg) {
                setEndMessage(msg);
            }
        };
        srte.setEndListener(cb);

        if(!listmode){
            Section section = (Section)this.cbxSection.getSelectedItem();
        
            TimeEventLists tlist = setPeriod(section);
            if(tlist == null)
                return;

            // extend gathering period
            srte.setSRTEMODE(tlist);
        }else{
            srte.setSRTEMODE(el);
        }
         // set section
        srte.setConfig(config);

        // run algorithm
        srte.start();
        
        
    }
    
    private void setEndMessage(boolean msg){
        if(msg){
            this.restoreOutput();
            this.btnStart.setEnabled(true);
            this.btnListStart.setEnabled(true);
            System.out.println("End");
            this.graphButtonAction();
        }
    }

    /**
     * Print out traffic data
     */
    private void extractData() throws Exception
    {
//        SRTEAlgorithm srte = new SRTEAlgorithm();
//
//        // set section
//        Section section = (Section)this.cbxSection.getSelectedItem();
//
//        Period period = setPeriod();
//        if(period == null)
//            return;
//        
//        srte.setSRTEMODE(section, period);
//        srte.setConfig(config);
//        srte.extractData();
    }
    
    private TimeEventLists setPeriod(Section s){
        
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        
        if(selectedDates.length > 2){
            JOptionPane.showMessageDialog(null, "Select less than 2 days..");
            return null;
        }
        
        if(selectedDates.length <= 0){
            JOptionPane.showMessageDialog(null, "Select days..");
            return null;
        }
        
        Calendar cs,ce;
        if(selectedDates.length == 1){
            cs = (Calendar)selectedDates[0].clone();
            ce = (Calendar)selectedDates[0].clone();
        }else{
            cs = (Calendar)selectedDates[0].clone();
            ce = (Calendar)selectedDates[1].clone();
        }
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
//        ce.set(Calendar.SECOND, 0);

        Period period = new Period(cs.getTime(), ce.getTime(), Interval.I15MIN.second);

        TimeEvent te = new TimeEvent();
        te.setAllTime(cs, ce, ce);
        te.setSection(s);
        TimeEventLists tlist = new TimeEventLists();
        tlist.AddTimeEvent(te);
        // set period with staing and enting time and 15min interval
        tlist.setName(period.getPeriodString());
        
        return tlist;
    }

    /**
     * Update configuration
     */
    private void updateOption()
    {
        System.out.print("Update Option ........");
        config.set("SMOOTHING_FILTERSIZE", this.tbxFilterSize.getText());
//        config.set("STEADY_TIME", this.tbxSteadyTime.getText());
        config.set("QUANTIZATION_THRESHOLD", this.tbxQThreshold.getText());
//        Calendar c = this.calStartDate.getSelectedDate();
//        config.set("START_YEAR", c.get(Calendar.YEAR));
//        config.set("START_MONTH", c.get(Calendar.MONTH)+1);
//        config.set("START_DAY", c.get(Calendar.DATE));
//        config.set("START_HOUR", this.cbxStartHour.getSelectedIndex());
//        config.set("START_MIN", this.cbxStartMin.getSelectedIndex());
//        c = this.calEndDate.getSelectedDate();
//        config.set("END_YEAR", c.get(Calendar.YEAR));
//        config.set("END_MONTH", c.get(Calendar.MONTH)+1);
//        config.set("END_DAY", c.get(Calendar.DATE));
//        config.set("END_HOUR", this.cbxEndHour.getSelectedIndex());
//        config.set("END_MIN", this.cbxEndMin.getSelectedIndex());
        
        //new Algorithm
        config.set("RCR_K",this.tbxRCRNofM.getText());
        config.set("KEYSPEED",this.tbxKEYSpeedLimit.getText());
        config.set("SRTFSPEED",this.tbxSRTFSpeedLimit.getText());
        config.set("RCRBoundary",this.tbxRCRboundary.getText());
        config.set("RSTDELTA",this.tbxRSTDelta.getText());
        //option
        config.set(SRTEConfig.TIMEINTERVAL,((Interval)this.cbxTimeInverval.getSelectedItem()).second);
        config.set(SRTEConfig.SMOOTHINGOPTION,((SMOOTHING)this.cbxsmoothing.getSelectedItem()).getIndex());
//        config.set("")
        
        
        //set Parameter
        SRTEConfig.RCRNofM = Double.parseDouble(this.tbxRCRNofM.getText());
        SRTEConfig.RCRTopBandwith = Integer.parseInt(this.tbxRCRtopBandwith.getText());
        SRTEConfig.SRTFSpeedLimit = Double.parseDouble(this.tbxSRTFSpeedLimit.getText());
        SRTEConfig.KEYSpeedLimit = Double.parseDouble(this.tbxKEYSpeedLimit.getText());
        SRTEConfig.RCRBoundary = Double.parseDouble(this.tbxRCRboundary.getText());
//        SRTEConfig.TPR_U = Double.parseDouble(this.tbxTPR_U.getText());
//        SRTEConfig.TPR_hour = Double.parseDouble(this.tbxTPR_hour.getText());
//        SRTEConfig.SDC_K = Double.parseDouble(this.tbxSDC_k.getText());
        SRTEConfig.TimeInterval = (((Interval)this.cbxTimeInverval.getSelectedItem()).second);
        SRTEConfig.isSmoothing = ((SMOOTHING)this.cbxsmoothing.getSelectedItem()).getIndex();
        SRTEConfig.RSTDelta = Double.parseDouble(this.tbxRSTDelta.getText());
        
        SRTEConfig.isGroup = this.cbxgroup.isSelected();
        System.out.println(" (OK)");
    }

    /**
     * Load section data into combo box
     */
    private void loadSection() {
        SectionManager sm = tmo.getSectionManager();
        if(sm == null) return;
        this.sections.clear();
        sm.loadSections();
        this.sections.addAll(sm.getSections());
        this.cbxSection.removeAllItems();
        
        for (Section s : this.sections) {
            this.cbxSection.addItem(s);
        }
        setInfo();
    }

    /**
     * Update GUI according to section change
     */
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
     * Redirect output into log box
     */
    public void redirectOutput() {
        backupOut = System.out;
        backupErr = System.err;
        //reset Area
        jTextArea1.setText("");
        // redirect System.out and System.err to log textbox
        StringOutputStream sos = new StringOutputStream(this.jTextArea1);
        System.setOut(new PrintStream(sos));
        System.setErr(new PrintStream(sos));
    }

    /**
     * Resotre output
     */
    public void restoreOutput() {
        if(backupOut == null)
            return;
        System.setOut(backupOut);
        System.setErr(backupErr);
    }
    
    public void doDayToggleListener(boolean toggle, int day){
        this.natsrlCalendar.addMouseListener(null);
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        SimpleDateFormat dateformatter = new SimpleDateFormat("d MMM, y");
        if(selectedDates.length > 2){
            JOptionPane.showMessageDialog(null, "Select less than 2 days..");
            return;
        }
        if(selectedDates.length <= 2 && selectedDates.length > 0){
            if(selectedDates.length == 1){
                this.jLabel_ST.setText(dateformatter.format(selectedDates[0].getTime()));
                this.jLabel_ET.setText(dateformatter.format(selectedDates[0].getTime()));
            }else
            {
                this.jLabel_ST.setText(dateformatter.format(selectedDates[0].getTime()));
                this.jLabel_ET.setText(dateformatter.format(selectedDates[1].getTime()));
            }
        }
    }

    private void init() {
        loadEventLists();
        //set Calendar Date
        DateChecker dc = DateChecker.getInstance();
        this.natsrlCalendar.setDateChecker(dc);
        
        DayToggleListener cb = new DayToggleListener(){
            @Override
            public void onButtonToggle(boolean toggle, int day) {
                doDayToggleListener(toggle,day);
            }
            
        };
        this.natsrlCalendar.setDayToggleListener(cb);
        
        //set calendar label
        this.jLabel_ST.setText("");
        this.jLabel_ET.setText("");
        
        // interval
        for (Interval i : Interval.values()) {
            this.cbxTimeInverval.addItem(i);
        }
        // interval
        for (SMOOTHING i : SMOOTHING.values()) {
            this.cbxsmoothing.addItem(i);
        }
        
        this.tbxFilterSize.setEnabled(false);
        
        this.cbxisDebug.setSelected(true);
        this.cbxisSave.setSelected(false);
    }
    
    /**
     * Load eventList
     */
    private void loadEventLists(){
        if(timeeventlists != null)
            timeeventlists.clear();
        try{
            timeeventlists.addAll(TimeEventLists.loadAllEvents());
            this.cbxeventlists.removeAllItems();

            for(TimeEventLists s : timeeventlists){
                this.cbxeventlists.addItem(s);
            }
            setEventListinfo();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Load eventlist info
     */
    private void setEventListinfo() {
        selectedEventList = (TimeEventLists)this.cbxeventlists.getSelectedItem();
        DefaultListModel dm = new DefaultListModel();
        
        if(selectedEventList == null)
            return;
          
        for(TimeEvent te : selectedEventList.getTimeEvents()){
            dm.addElement(te);
        }
//        _list.setModel(dm);
        lbxeventlist.setModel(dm);
    }

    private void graphButtonAction() {
        if(this.srte == null)
            btnShowGraph.setEnabled(false);
        else
            btnShowGraph.setEnabled(true);
    }

    private void clearMemory() {
        if(srte != null)
            srte.getResults().clear();
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
                    logText.append(text);
                }
            });
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

        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tbxFilterSize = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        cbxsmoothing = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        tbxQThreshold = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        btnSaveOption = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        cbxTimeInverval = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        tbxRCRtopBandwith = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        tbxSRTFSpeedLimit = new javax.swing.JTextField();
        tbxRCRboundary = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        tbxRSTDelta = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        tbxKEYSpeedLimit = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        tbxRCRNofM = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        btnEventEditor = new javax.swing.JButton();
        cbxeventlists = new javax.swing.JComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        lbxeventlist = new javax.swing.JList();
        btnListStart = new javax.swing.JButton();
        btnShowGraph = new javax.swing.JButton();
        cbxisSave = new javax.swing.JCheckBox();
        cbxisDebug = new javax.swing.JCheckBox();
        cbxgroup = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbxSection = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbxDesc = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbxRoutes = new javax.swing.JTextArea();
        btnOpenSectionEditor = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        cbxStartHour = new javax.swing.JComboBox();
        cbxStartMin = new javax.swing.JComboBox();
        cbxEndHour = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        cbxEndMin = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        cbxBareMin = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        cbxBareHour = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel_ST = new javax.swing.JLabel();
        jLabel_ET = new javax.swing.JLabel();
        btnExtractData = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();

        setMaximumSize(new java.awt.Dimension(1135, 596));
        setPreferredSize(new java.awt.Dimension(1135, 596));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Smoothing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel5.setText("Filter Size ");

        tbxFilterSize.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxFilterSize.setText("1");

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel13.setText("points");

        cbxsmoothing.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxsmoothing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxsmoothingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxsmoothing, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbxFilterSize, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(tbxFilterSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13))
                    .addComponent(cbxsmoothing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantization", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel8.setText("Step Threshold : ");

        tbxQThreshold.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxQThreshold.setText("2");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel9.setText("mph");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbxQThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(tbxQThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        btnSaveOption.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnSaveOption.setText("Save Option");
        btnSaveOption.setEnabled(false);
        btnSaveOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveOptionActionPerformed(evt);
            }
        });

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time Interval", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        cbxTimeInverval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel12.setText("min");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxTimeInverval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxTimeInverval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("RCR Parameter"));

        jLabel19.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel19.setText("Number of Candidate :");

        tbxRCRtopBandwith.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRCRtopBandwith.setText("2");
        tbxRCRtopBandwith.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxRCRtopBandwithActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel25.setText("Minimum SRTF Speed Limit :");

        tbxSRTFSpeedLimit.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxSRTFSpeedLimit.setText("2");
        tbxSRTFSpeedLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxSRTFSpeedLimitActionPerformed(evt);
            }
        });

        tbxRCRboundary.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRCRboundary.setText("2");
        tbxRCRboundary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxRCRboundaryActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel26.setText("RCR boundary :");

        jLabel27.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel27.setText("RST Delta :");

        tbxRSTDelta.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRSTDelta.setText("2");
        tbxRSTDelta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxRSTDeltaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxRCRtopBandwith, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxSRTFSpeedLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxRCRboundary, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxRSTDelta, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(tbxRCRtopBandwith, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(tbxRCRboundary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(tbxRSTDelta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(tbxSRTFSpeedLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("KeyPoint Parameter"));

        tbxKEYSpeedLimit.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxKEYSpeedLimit.setText("2");
        tbxKEYSpeedLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxKEYSpeedLimitActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel24.setText("Maximum Key Point Speed Limit :");

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel18.setText("Cont'n Interval :");

        tbxRCRNofM.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxRCRNofM.setText("2");
        tbxRCRNofM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbxRCRNofMActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxKEYSpeedLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbxRCRNofM, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(tbxKEYSpeedLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(tbxRCRNofM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel18.getAccessibleContext().setAccessibleName("RCR Number of Minus");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSaveOption)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSaveOption)
                .addContainerGap())
        );

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        btnEventEditor.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnEventEditor.setText("EventEditor");
        btnEventEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEventEditorActionPerformed(evt);
            }
        });

        cbxeventlists.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxeventlists.setPreferredSize(new java.awt.Dimension(150, 22));
        cbxeventlists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxeventlistsActionPerformed(evt);
            }
        });

        lbxeventlist.setMaximumSize(new java.awt.Dimension(150, 0));
        lbxeventlist.setMinimumSize(new java.awt.Dimension(150, 0));
        lbxeventlist.setPreferredSize(new java.awt.Dimension(150, 0));
        jScrollPane4.setViewportView(lbxeventlist);

        btnListStart.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        btnListStart.setLabel("Start");
        btnListStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnListStartActionPerformed(evt);
            }
        });

        btnShowGraph.setText("Show Graph");
        btnShowGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowGraphActionPerformed(evt);
            }
        });

        cbxisSave.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxisSave.setText("Save Excel File");

        cbxisDebug.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxisDebug.setText("Debug");

        cbxgroup.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxgroup.setText("Result for Station Group");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cbxeventlists, 0, 174, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEventEditor))
                    .addComponent(jScrollPane4)
                    .addComponent(btnListStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnShowGraph, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxgroup)
                            .addComponent(cbxisDebug)
                            .addComponent(cbxisSave))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxeventlists, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEventEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnListStart, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnShowGraph, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbxgroup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxisSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxisDebug)
                .addContainerGap(157, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Event Lists", jPanel3);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Section", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel1.setText("Select Section");

        cbxSection.setFont(new java.awt.Font("Verdana 10", 0, 12)); // NOI18N
        cbxSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel2.setText("Description");

        tbxDesc.setColumns(20);
        tbxDesc.setEditable(false);
        tbxDesc.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxDesc.setLineWrap(true);
        tbxDesc.setRows(2);
        jScrollPane1.setViewportView(tbxDesc);

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel3.setText("Routes");

        tbxRoutes.setColumns(20);
        tbxRoutes.setEditable(false);
        tbxRoutes.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxRoutes.setLineWrap(true);
        tbxRoutes.setRows(2);
        jScrollPane2.setViewportView(tbxRoutes);

        btnOpenSectionEditor.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnOpenSectionEditor.setText("Edit");
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
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxSection, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenSectionEditor)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOpenSectionEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Snow Event", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.setMinimumSize(new java.awt.Dimension(40, 20));
        cbxStartHour.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cbxStartHourMouseClicked(evt);
            }
        });

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxStartMin.setMinimumSize(new java.awt.Dimension(40, 20));

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel20.setFont(new java.awt.Font("Verdana 12", 0, 12)); // NOI18N
        jLabel20.setText(":");

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxEndMin.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel28.setFont(new java.awt.Font("Verdana 12", 0, 12)); // NOI18N
        jLabel28.setText(":");

        cbxBareMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxBareMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxBareMin.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel29.setFont(new java.awt.Font("Verdana 12", 0, 12)); // NOI18N
        jLabel29.setText(":");

        cbxBareHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxBareHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxBareHour.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel4.setText("Start Time");

        jLabel6.setText("End Time");

        jLabel7.setText("BareLane Regain Time");

        jLabel_ST.setText("30 Jun, 2012");

        jLabel_ET.setText("30 Jun, 2012");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel_ST)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addComponent(jLabel4))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel_ET)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(cbxEndHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxEndMin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbxStartMin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(cbxBareHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(cbxBareMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel4)
                    .addComponent(jLabel_ST))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel6)
                    .addComponent(jLabel_ET))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxBareMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxBareHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(jLabel7))
                .addContainerGap())
        );

        btnExtractData.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnExtractData.setText("Extract Data");
        btnExtractData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtractDataActionPerformed(evt);
            }
        });

        btnStart.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(natsrlCalendar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnExtractData, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExtractData, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jTabbedPane1.addTab("Event", jPanel6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane3)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        this.tmo.openSectionEditor(this.simFrame, true);
        this.loadSection();
}//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void btnSaveOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveOptionActionPerformed
        this.updateOption();
        config.save();
}//GEN-LAST:event_btnSaveOptionActionPerformed

    /**
     * On start button click event
     * @param evt
     */
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        try {
            try {
                // start!!!
                startEstimation();
            } catch (Exception ex) {
                System.out.println("Exception : SRTEMainPanel.startEstimatin()");
                ex.printStackTrace();
            }
        } catch(OutOfMemoryError ex) {
            System.out.println("Exception : OutOfMemoryError");
            ex.printStackTrace();
        }
}//GEN-LAST:event_btnStartActionPerformed

    private void cbxSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionActionPerformed
        setInfo();
    }//GEN-LAST:event_cbxSectionActionPerformed

    /**
     * On extract data button click event
     */
    private void btnExtractDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtractDataActionPerformed
        try {
            // extract station data!!
            extractData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnExtractDataActionPerformed

    private void natsrlCalendarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_natsrlCalendarMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_natsrlCalendarMouseClicked

    private void cbxStartHourMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cbxStartHourMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxStartHourMouseClicked

    private void btnEventEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEventEditorActionPerformed
        // TODO add your handling code here:
//        EventEditor ed = new EventEditor(simFrame);
//        ed.setVisible(true);
        
        EventEditor se = new EventEditor(timeeventlists);
        se.setLocationRelativeTo(simFrame);            
        if(true && simFrame != null) ModalFrameUtil.showAsModal(se, simFrame);
        else se.setVisible(true);
        loadEventLists();
        
//        try {
////            SectionEditor se = new SectionEditor();
////        se.setLocationRelativeTo(parent);            
////        if(true && parent != null) ModalFrameUtil.showAsModal(se, parent);
////        else se.setVisible(true);
//                //Simulation Data Reset
//            PluginInfo pi = new PluginInfo("EventEditor",PluginType.TOOL, EventEditorPlugin.class);
//            simFrame.setPluginInfo(pi);
//            simFrame.setLocation(this.getLocation());
//            simFrame.setVisible(true);
//            this.setVisible(false);  
//        } catch (Exception ex) {
//            this.setVisible(true);
//            ex.printStackTrace();
//        }
    }//GEN-LAST:event_btnEventEditorActionPerformed

    private void cbxeventlistsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxeventlistsActionPerformed
        // TODO add your handling code here:
        setEventListinfo();
    }//GEN-LAST:event_cbxeventlistsActionPerformed

    private void btnListStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListStartActionPerformed
        // TODO add your handling code here:
        try {
            try {
                // start!!!
                startEstimation(selectedEventList);
            } catch (Exception ex) {
                System.out.println("Exception : SRTEMainPanel.startEstimatin()");
                ex.printStackTrace();
            }
        } catch(OutOfMemoryError ex) {
            System.out.println("Exception : OutOfMemoryError");
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnListStartActionPerformed

    private void cbxsmoothingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxsmoothingActionPerformed
        // TODO add your handling code here:
        if(((SMOOTHING)this.cbxsmoothing.getSelectedItem()).isMore())
            this.tbxFilterSize.setEnabled(true);
        else
            this.tbxFilterSize.setEnabled(false);
    }//GEN-LAST:event_cbxsmoothingActionPerformed

    private void btnShowGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowGraphActionPerformed
        // TODO add your handling code here:
        if(srte.getResults() == null)
            return;
        
        SRTEChartView tf = new SRTEChartView(srte.getResults());
        tf.setLocationRelativeTo(simFrame);            
        if(true && simFrame != null) ModalFrameUtil.showAsModal(tf, simFrame);
        else tf.setVisible(true);
    }//GEN-LAST:event_btnShowGraphActionPerformed

    private void tbxRCRNofMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxRCRNofMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxRCRNofMActionPerformed

    private void tbxRCRtopBandwithActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxRCRtopBandwithActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxRCRtopBandwithActionPerformed

    private void tbxKEYSpeedLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxKEYSpeedLimitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxKEYSpeedLimitActionPerformed

    private void tbxSRTFSpeedLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxSRTFSpeedLimitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxSRTFSpeedLimitActionPerformed

    private void tbxRCRboundaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxRCRboundaryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxRCRboundaryActionPerformed

    private void tbxRSTDeltaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbxRSTDeltaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbxRSTDeltaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEventEditor;
    private javax.swing.JButton btnExtractData;
    private javax.swing.JButton btnListStart;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnSaveOption;
    private javax.swing.JButton btnShowGraph;
    private javax.swing.JButton btnStart;
    private javax.swing.JComboBox cbxBareHour;
    private javax.swing.JComboBox cbxBareMin;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JComboBox cbxSection;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JComboBox cbxTimeInverval;
    private javax.swing.JComboBox cbxeventlists;
    private javax.swing.JCheckBox cbxgroup;
    private javax.swing.JCheckBox cbxisDebug;
    private javax.swing.JCheckBox cbxisSave;
    private javax.swing.JComboBox cbxsmoothing;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_ET;
    private javax.swing.JLabel jLabel_ST;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
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
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JList lbxeventlist;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JTextArea tbxDesc;
    private javax.swing.JTextField tbxFilterSize;
    private javax.swing.JTextField tbxKEYSpeedLimit;
    private javax.swing.JTextField tbxQThreshold;
    private javax.swing.JTextField tbxRCRNofM;
    private javax.swing.JTextField tbxRCRboundary;
    private javax.swing.JTextField tbxRCRtopBandwith;
    private javax.swing.JTextField tbxRSTDelta;
    private javax.swing.JTextArea tbxRoutes;
    private javax.swing.JTextField tbxSRTFSpeedLimit;
    // End of variables declaration//GEN-END:variables

}
