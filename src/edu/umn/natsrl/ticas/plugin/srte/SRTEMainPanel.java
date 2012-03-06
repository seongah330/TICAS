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
package edu.umn.natsrl.ticas.plugin.srte;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com) (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEMainPanel extends javax.swing.JPanel {
    
    private TMO tmo = TMO.getInstance();    
    private Vector<Section> sections = new Vector<Section>();
    private SRTEConfig config = SRTEConfig.getInstance();
    private PluginFrame simFrame;

    /**
     * Constructor
     */
    public SRTEMainPanel(PluginFrame parent) {
        this();
        this.simFrame = parent;
    }
    
    /**
     * Constructor
     */
    public SRTEMainPanel() {
        initComponents();
        
        this.setSize(800, 520);        
        config.load();

        if(config.isLoaded()) {
            this.tbxFilterSize.setText(config.getString("SMOOTHING_FILTERSIZE"));
            this.tbxSteadyTime.setText(config.getString("STEADY_TIME"));
            this.tbxQThreshold.setText(config.getString("QUANTIZATION_THRESHOLD"));

            Calendar c = Calendar.getInstance();
            c.set(config.getInt("START_YEAR"), config.getInt("START_MONTH")-1, config.getInt("START_DAY"));
            this.calStartDate.setSelectedDate(c);
            c.set(config.getInt("END_YEAR"), config.getInt("END_MONTH")-1, config.getInt("END_DAY"));
            this.calEndDate.setSelectedDate(c);

            this.cbxStartHour.setSelectedIndex(config.getInt("START_HOUR"));
            this.cbxEndHour.setSelectedIndex(config.getInt("END_HOUR"));
            this.cbxStartMin.setSelectedIndex(config.getInt("START_MIN"));
            this.cbxEndMin.setSelectedIndex(config.getInt("END_MIN"));
        }

        loadSection();
    }


    
    /**
     * On clicked start button
     * Start to collect data and display chart
     */
    private void startEstimation() throws OutOfMemoryError, Exception {
        
        this.updateOption();
        
        SRTEAlgorithm srte = new SRTEAlgorithm();

        
        // set section
        Section section = (Section)this.cbxSection.getSelectedItem();
        
        // set period for staring time
        Calendar cs = this.calStartDate.getSelectedDate();
        int hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        cs.set(Calendar.HOUR_OF_DAY, hour);
        cs.set(Calendar.MINUTE, Integer.parseInt(this.cbxStartMin.getSelectedItem().toString()));
        cs.set(Calendar.SECOND, 0);
       
        // set period for ending time
        Calendar ce = this.calEndDate.getSelectedDate();
        hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        ce.set(Calendar.HOUR_OF_DAY, hour);
        ce.set(Calendar.MINUTE, Integer.parseInt(this.cbxEndMin.getSelectedItem().toString()));
        ce.set(Calendar.SECOND, 0);


        // set period with staing and enting time and 15min interval
        Period period = new Period(cs.getTime(), ce.getTime(), Interval.I15MIN.second);
        // extend gathering period
        srte.setSection(section, period);
        srte.setConfig(config);

        // run algorithm
        srte.start();
        
        JOptionPane.showMessageDialog(this, "Done");

    }

    /**
     * Print out traffic data
     */
    private void extractData() throws Exception
    {
        SRTEAlgorithm srte = new SRTEAlgorithm();

        // set section
        Section section = (Section)this.cbxSection.getSelectedItem();

        // set period
        Calendar cs = this.calStartDate.getSelectedDate();
        int hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        cs.set(Calendar.HOUR_OF_DAY, hour);
        cs.set(Calendar.MINUTE, Integer.parseInt(this.cbxStartMin.getSelectedItem().toString()));
        cs.set(Calendar.SECOND, 0);

        Calendar ce = this.calEndDate.getSelectedDate();
        hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        ce.set(Calendar.HOUR_OF_DAY, hour);
        ce.set(Calendar.MINUTE, Integer.parseInt(this.cbxEndMin.getSelectedItem().toString()));
        ce.set(Calendar.SECOND, 0);

        Period period = new Period(cs.getTime(), ce.getTime(), 300);
        srte.setSection(section, period);
        srte.setConfig(config);
        srte.extractData();
    }

    /**
     * Update configuration
     */
    private void updateOption()
    {
        System.out.print("Update Option ........");
        config.set("SMOOTHING_FILTERSIZE", this.tbxFilterSize.getText());
        config.set("STEADY_TIME", this.tbxSteadyTime.getText());
        config.set("QUANTIZATION_THRESHOLD", this.tbxQThreshold.getText());
        Calendar c = this.calStartDate.getSelectedDate();
        config.set("START_YEAR", c.get(Calendar.YEAR));
        config.set("START_MONTH", c.get(Calendar.MONTH)+1);
        config.set("START_DAY", c.get(Calendar.DATE));
        config.set("START_HOUR", this.cbxStartHour.getSelectedIndex());
        config.set("START_MIN", this.cbxStartMin.getSelectedIndex());
        c = this.calEndDate.getSelectedDate();
        config.set("END_YEAR", c.get(Calendar.YEAR));
        config.set("END_MONTH", c.get(Calendar.MONTH)+1);
        config.set("END_DAY", c.get(Calendar.DATE));
        config.set("END_HOUR", this.cbxEndHour.getSelectedIndex());
        config.set("END_MIN", this.cbxEndMin.getSelectedIndex());
        System.out.println(" (OK)");
    }

    /**
     * Load section data into combo box
     */
    private void loadSection() {
        this.sections.clear();
        this.tmo.getSectionManager().loadSections();
        this.sections.addAll(tmo.getSectionManager().getSections());

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
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tbxFilterSize = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        tbxQThreshold = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        tbxSteadyTime = new javax.swing.JTextField();
        btnSaveOption = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        cbxStartHour = new javax.swing.JComboBox();
        cbxStartMin = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cbxEndHour = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        cbxEndMin = new javax.swing.JComboBox();
        calStartDate = new datechooser.beans.DateChooserCombo();
        calEndDate = new datechooser.beans.DateChooserCombo();
        btnStart = new javax.swing.JButton();
        btnExtractData = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Section", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel1.setText("Select Section");

        cbxSection.setFont(new java.awt.Font("Verdana 10", 0, 12));
        cbxSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel2.setText("Description");

        tbxDesc.setColumns(20);
        tbxDesc.setEditable(false);
        tbxDesc.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxDesc.setLineWrap(true);
        tbxDesc.setRows(2);
        jScrollPane1.setViewportView(tbxDesc);

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel3.setText("Routes");

        tbxRoutes.setColumns(20);
        tbxRoutes.setEditable(false);
        tbxRoutes.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxRoutes.setLineWrap(true);
        tbxRoutes.setRows(2);
        jScrollPane2.setViewportView(tbxRoutes);

        btnOpenSectionEditor.setFont(new java.awt.Font("Verdana", 0, 12));
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
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cbxSection, 0, 217, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenSectionEditor)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOpenSectionEditor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Smoothing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel5.setText("Filter Size ");

        tbxFilterSize.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxFilterSize.setText("1");

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel13.setText("points");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbxFilterSize, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(252, 252, 252))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(tbxFilterSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quantization", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel8.setText("Step Threshold : ");

        tbxQThreshold.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxQThreshold.setText("2");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12));
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
                .addContainerGap(216, Short.MAX_VALUE))
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

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ETC", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 12))); // NOI18N

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel17.setText("Steady Time Threshold for LSP and SRP : ");

        tbxSteadyTime.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxSteadyTime.setText("8");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbxSteadyTime, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(tbxSteadyTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        btnSaveOption.setFont(new java.awt.Font("Verdana", 0, 12));
        btnSaveOption.setText("Save Option");
        btnSaveOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveOptionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(296, 296, 296)
                        .addComponent(btnSaveOption))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(153, 153, 153)
                .addComponent(btnSaveOption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Snow Event", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel6.setText("Start");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.setMinimumSize(new java.awt.Dimension(40, 20));

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxStartMin.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel19.setFont(new java.awt.Font("Verdana 12", 0, 12));
        jLabel19.setText(":");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel7.setText("End");

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setMinimumSize(new java.awt.Dimension(40, 20));

        jLabel20.setFont(new java.awt.Font("Verdana 12", 0, 12));
        jLabel20.setText(":");

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        cbxEndMin.setMinimumSize(new java.awt.Dimension(40, 20));

        calStartDate.setCurrentView(new datechooser.view.appearance.AppearancesList("Swing",
            new datechooser.view.appearance.ViewAppearance("custom",
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    true,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(0, 0, 255),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(128, 128, 128),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(255, 0, 0),
                    false,
                    false,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                (datechooser.view.BackRenderer)null,
                false,
                true)));

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(calStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addComponent(calEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
            .addGap(10, 10, 10)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(cbxStartHour, 0, 57, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLabel19))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addComponent(cbxEndHour, 0, 57, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(6, 6, 6)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(cbxEndMin, javax.swing.GroupLayout.Alignment.TRAILING, 0, 61, Short.MAX_VALUE)
                .addComponent(cbxStartMin, javax.swing.GroupLayout.Alignment.TRAILING, 0, 61, Short.MAX_VALUE))
            .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(cbxStartHour, 0, 25, Short.MAX_VALUE)
                    .addGap(64, 64, 64))
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(calStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbxStartMin, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                .addComponent(jLabel19))))
                    .addGap(13, 13, 13)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(calEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxEndMin, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                            .addComponent(jLabel20)
                            .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
                    .addGap(25, 25, 25))))
    );

    btnStart.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
    btnStart.setText("Start");
    btnStart.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnStartActionPerformed(evt);
        }
    });

    btnExtractData.setFont(new java.awt.Font("Verdana", 0, 12));
    btnExtractData.setText("Extract Data");
    btnExtractData.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnExtractDataActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                    .addComponent(btnExtractData, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)))
            .addGap(18, 18, 18)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnExtractData, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                        .addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)))
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExtractData;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnSaveOption;
    private javax.swing.JButton btnStart;
    private datechooser.beans.DateChooserCombo calEndDate;
    private datechooser.beans.DateChooserCombo calStartDate;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JComboBox cbxSection;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea tbxDesc;
    private javax.swing.JTextField tbxFilterSize;
    private javax.swing.JTextField tbxQThreshold;
    private javax.swing.JTextArea tbxRoutes;
    private javax.swing.JTextField tbxSteadyTime;
    // End of variables declaration//GEN-END:variables

}
