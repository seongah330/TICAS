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
 * VSLEvaluatorGUI.java
 *
 * Created on Jul 24, 2011, 2:54:52 PM
 */
package edu.umn.natsrl.ticas.plugin.traveltimeIndexer;

import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class TTIndexterGUI extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    private List<Section> sections = new ArrayList<Section>();
    private PluginFrame simFrame;

    public TTIndexterGUI(PluginFrame frame) {
        initComponents();
        this.simFrame = frame;
        this.simFrame.setSize(740, 740);
        this.simFrame.setResizable(false);
        loadSection();

        // interval
        for (Interval i : Interval.values()) {
            this.cbxDataInterval.addItem(i);
            this.cbxEvalInterval.addItem(i);
            this.cbxTTInterval.addItem(i);
        }
        Calendar c = Calendar.getInstance();
        c.set(2010, 9 - 1, 8);
        this.calStartDate.setSelectedDate(c);
        this.calEndDate.setSelectedDate(c);
        this.cbxStartHour.setSelectedIndex(7);
        this.cbxEndHour.setSelectedIndex(8);
        this.cbxDataInterval.setSelectedIndex(1);
        this.cbxEvalInterval.setSelectedIndex(5);
        this.cbxTTInterval.setSelectedIndex(1);
        JMenuItem merger = new JMenuItem("Merge Results");
        merger.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ResultMerger rm = new ResultMerger(simFrame, true);
                rm.setLocationRelativeTo(simFrame);
                rm.setVisible(true);
            }
        });
        this.simFrame.addToolsMenu(merger);
        //setHolidays();
        this.natsrlCalendar.setDateChecker(DateChecker.getInstance());
    }

    private void evaluate() {
        List<Period> periods = getPeriods();
        Section section = (Section) this.cbxSections.getSelectedItem();
        Interval dataInterval = (Interval) this.cbxDataInterval.getSelectedItem();
        Interval evalInterval = (Interval) this.cbxEvalInterval.getSelectedItem();
        Interval ttInterval = (Interval) this.cbxTTInterval.getSelectedItem();
        String targetStation = this.tbxVolumeAnalTargetStation.getText();

        double freeflowTT = -1;
        try {
            freeflowTT = Double.parseDouble(this.tbxFreeFlowTravelTime.getText());
        } catch (Exception ex) {
        }

        String[] targetStations = targetStation.split(" ");
        TravelTimeIndexer ev = new TravelTimeIndexer(section, periods, targetStations, dataInterval, evalInterval, ttInterval, freeflowTT);
        ev.run();
        System.out.println("End of Evaulation");
        if (simFrame != null) {
            this.simFrame.setAlwaysOnTop(true);
        }
        JOptionPane.showMessageDialog(this.simFrame, "Done");
        if (simFrame != null) {
            this.simFrame.setAlwaysOnTop(false);
        }
    }

    private List<Period> getPeriods() {
        Boolean[] okDays = new Boolean[]{chkSunday.isSelected(),
            chkMonday.isSelected(), chkTuesday.isSelected(),
            chkWednesday.isSelected(), chkThursday.isSelected(),
            chkFriday.isSelected(), chkSaturday.isSelected()};

        Calendar[] skipDates = this.natsrlCalendar.getSelectedDates();
        List<Period> days = new ArrayList<Period>();
        Calendar cursor = (Calendar) this.calStartDate.getSelectedDate().clone();
        cursor.set(Calendar.HOUR_OF_DAY, 0);
        cursor.set(Calendar.MINUTE, 0);
        cursor.set(Calendar.SECOND, 0);
        cursor.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) this.calEndDate.getSelectedDate().clone();
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        int startHour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        int endHour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());

        while (true) {



            int DAY_OF_WEEK = cursor.get(Calendar.DAY_OF_WEEK);


            boolean skip = false;
            // check skip date
            for (Calendar c : skipDates) {
                if (cursor.equals(c)) {
                    skip = true;
                    break;
                }
            }

            // check date
            if (!skip) {
                for (int i = 0; i < 7; i++) {
                    if (!okDays[DAY_OF_WEEK - 1]) {
                        skip = true;
                        break;
                    }
                }
            }

            // if should skip,
            if (skip) {
                cursor.add(Calendar.DATE, 1);
                continue;
            }

            // check last day
            if (cursor.compareTo(end) > 0) {
                break;
            }

            Calendar cs = (Calendar) cursor.clone();
            cs.set(Calendar.HOUR_OF_DAY, startHour);
            Calendar ce = (Calendar) cursor.clone();
            ce.set(Calendar.HOUR_OF_DAY, endHour);

            Period period = new Period(cs.getTime(), ce.getTime(), 30);
            days.add(period);
            cursor.add(Calendar.DATE, 1);
        }

        return days;
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
     * Open section editor
     */
    public void openSectionEditor() {
        tmo.openSectionEditor(simFrame, true);
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
        SectionInfoDialog si = new SectionInfoDialog(section, this.tmo, this.simFrame, true);
        si.setLocationRelativeTo(this);
        si.setVisible(true);
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
        cbxSections = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        calStartDate = new datechooser.beans.DateChooserCombo();
        calEndDate = new datechooser.beans.DateChooserCombo();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxStartHour = new javax.swing.JComboBox();
        cbxEndHour = new javax.swing.JComboBox();
        btnEvaulate = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        cbxDataInterval = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        cbxEvalInterval = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        cbxTTInterval = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        tbxVolumeAnalTargetStation = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        chkWednesday = new javax.swing.JCheckBox();
        chkSunday = new javax.swing.JCheckBox();
        chkMonday = new javax.swing.JCheckBox();
        chkTuesday = new javax.swing.JCheckBox();
        chkFriday = new javax.swing.JCheckBox();
        chkThursday = new javax.swing.JCheckBox();
        chkSaturday = new javax.swing.JCheckBox();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        tbxFreeFlowTravelTime = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Section", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 10));
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxSections, 0, 207, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Period", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

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

    jLabel1.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel1.setText("Start Date");

    jLabel2.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel2.setText("End Date");

    jLabel3.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel3.setText("Start Hour");

    jLabel4.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel4.setText("End Hour");

    cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12));
    cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
    cbxStartHour.setMinimumSize(new java.awt.Dimension(40, 20));

    cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12));
    cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
    cbxEndHour.setMinimumSize(new java.awt.Dimension(40, 20));

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel1)
                .addComponent(jLabel4)
                .addComponent(jLabel3)
                .addComponent(jLabel2))
            .addGap(31, 31, 31)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(cbxEndHour, 0, 117, Short.MAX_VALUE)
                .addComponent(cbxStartHour, 0, 117, Short.MAX_VALUE)
                .addComponent(calEndDate, 0, 0, Short.MAX_VALUE)
                .addComponent(calStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
            .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(calStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(calEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2)))
            .addGap(18, 18, 18)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cbxStartHour, 0, 25, Short.MAX_VALUE)
                .addComponent(jLabel3))
            .addGap(18, 18, 18)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel4))
            .addGap(82, 82, 82))
    );

    btnEvaulate.setFont(new java.awt.Font("Verdana", 1, 12));
    btnEvaulate.setText("Evaluate");
    btnEvaulate.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnEvaulateActionPerformed(evt);
        }
    });

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Intervals", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

    jLabel5.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel5.setText("Speed/Accel/Volume");

    cbxDataInterval.setFont(new java.awt.Font("Verdana", 0, 12));

    jLabel6.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel6.setText("VMT/DVH Interval");

    cbxEvalInterval.setFont(new java.awt.Font("Verdana", 0, 12));

    jLabel9.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel9.setText("TT Interval");

    cbxTTInterval.setFont(new java.awt.Font("Verdana", 0, 12));

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jLabel5)
                    .addGap(18, 18, 18)
                    .addComponent(cbxDataInterval, 0, 0, Short.MAX_VALUE))
                .addComponent(jLabel6)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jLabel9)
                    .addGap(74, 74, 74)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cbxTTInterval, 0, 0, Short.MAX_VALUE)
                        .addComponent(cbxEvalInterval, 0, 0, Short.MAX_VALUE))))
            .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel5)
                .addComponent(cbxDataInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cbxEvalInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel6))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel9)
                .addComponent(cbxTTInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Volume Anaisys", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

    jLabel7.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel7.setText("Target Station (optional)");

    jLabel8.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel8.setText("(e.g. S43 S44 S45)");

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tbxVolumeAnalTargetStation, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(jLabel7)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                    .addComponent(jLabel8)))
            .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel7)
                .addComponent(jLabel8))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(tbxVolumeAnalTargetStation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(54, Short.MAX_VALUE))
    );

    jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date Option", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

    chkWednesday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkWednesday.setSelected(true);
    chkWednesday.setText("Wed");

    chkSunday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkSunday.setText("Sun");

    chkMonday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkMonday.setSelected(true);
    chkMonday.setText("Mon");

    chkTuesday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkTuesday.setSelected(true);
    chkTuesday.setText("Tue");

    chkFriday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkFriday.setText("Fri");

    chkThursday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkThursday.setSelected(true);
    chkThursday.setText("Thu");

    chkSaturday.setFont(new java.awt.Font("Verdana", 0, 11));
    chkSaturday.setText("Sat");

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(chkSunday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkMonday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkTuesday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkWednesday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkThursday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkFriday)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(chkSaturday)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(chkSunday)
                .addComponent(chkMonday)
                .addComponent(chkTuesday)
                .addComponent(chkWednesday)
                .addComponent(chkThursday)
                .addComponent(chkFriday)
                .addComponent(chkSaturday))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jLabel11.setFont(new java.awt.Font("Verdana", 1, 11));
    jLabel11.setText("Except dayes");

    jLabel12.setFont(new java.awt.Font("Verdana", 1, 11));
    jLabel12.setText("Available dates");

    javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
    jPanel6.setLayout(jPanel6Layout);
    jPanel6Layout.setHorizontalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel11)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel12))
            .addContainerGap(40, Short.MAX_VALUE))
    );
    jPanel6Layout.setVerticalGroup(
        jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel6Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel11)
            .addGap(18, 18, 18)
            .addComponent(natsrlCalendar, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel12)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ETC", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

    jLabel10.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel10.setText("Free-flow Travel time (optional)");

    jLabel13.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel13.setText("min");

    javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
    jPanel7.setLayout(jPanel7Layout);
    jPanel7Layout.setHorizontalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel10)
            .addGap(18, 18, 18)
            .addComponent(tbxFreeFlowTravelTime, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel13)
            .addContainerGap(306, Short.MAX_VALUE))
    );
    jPanel7Layout.setVerticalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel10)
                .addComponent(tbxFreeFlowTravelTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel13))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(btnEvaulate, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(11, 11, 11)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(btnEvaulate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    }// </editor-fold>//GEN-END:initComponents

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
}//GEN-LAST:event_cbxSectionsActionPerformed

    private void btnEvaulateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEvaulateActionPerformed
        this.evaluate();
    }//GEN-LAST:event_btnEvaulateActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEvaulate;
    private datechooser.beans.DateChooserCombo calEndDate;
    private datechooser.beans.DateChooserCombo calStartDate;
    private javax.swing.JComboBox cbxDataInterval;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEvalInterval;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxTTInterval;
    private javax.swing.JCheckBox chkFriday;
    private javax.swing.JCheckBox chkMonday;
    private javax.swing.JCheckBox chkSaturday;
    private javax.swing.JCheckBox chkSunday;
    private javax.swing.JCheckBox chkThursday;
    private javax.swing.JCheckBox chkTuesday;
    private javax.swing.JCheckBox chkWednesday;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JTextField tbxFreeFlowTravelTime;
    private javax.swing.JTextField tbxVolumeAnalTargetStation;
    // End of variables declaration//GEN-END:variables

    private void setHolidays() {
//        List<Dates> dates = new ArrayList<Dates>();
//        dates.add(new Dates(2007, 1, 1));   // new year
//        dates.add(new Dates(2007, 1, 15));  // martin luther king
//        dates.add(new Dates(2007, 2, 19));  // president
//        dates.add(new Dates(2007, 5, 28));  // memorial
//        dates.add(new Dates(2007, 7, 4));  // independant
//        dates.add(new Dates(2007, 9, 3));  // labor
//        dates.add(new Dates(2007, 10, 8));  // columbus
//        dates.add(new Dates(2007, 11, 12));  // veterans
//        dates.add(new Dates(2007, 11, 22));  // thanks giving
//        dates.add(new Dates(2007, 11, 23));  // thanks giving
//        dates.add(new Dates(2007, 12, 25));  // christmas             
//        
//        dates.add(new Dates(2008, 1, 1));   // new year
//        dates.add(new Dates(2008, 1, 21));  // martin luther king
//        dates.add(new Dates(2008, 2, 18));  // president
//        dates.add(new Dates(2008, 5, 26));  // memorial
//        dates.add(new Dates(2008, 7, 4));  // independant
//        dates.add(new Dates(2008, 9, 1));  // labor
//        dates.add(new Dates(2008, 10, 13));  // columbus
//        dates.add(new Dates(2008, 11, 11));  // veterans
//        dates.add(new Dates(2008, 11, 27));  // thanks giving
//        dates.add(new Dates(2008, 11, 28));  // thanks giving
//        dates.add(new Dates(2008, 12, 25));  // christmas        
//        
//        dates.add(new Dates(2009, 1, 1));   // new year
//        dates.add(new Dates(2009, 1, 19));  // martin luther king
//        dates.add(new Dates(2009, 2, 16));  // president
//        dates.add(new Dates(2009, 5, 25));  // memorial
//        dates.add(new Dates(2009, 7, 4));  // independant
//        dates.add(new Dates(2009, 9, 7));  // labor
//        dates.add(new Dates(2009, 10, 12));  // columbus
//        dates.add(new Dates(2009, 11, 11));  // veterans
//        dates.add(new Dates(2009, 11, 26));  // thanks giving
//        dates.add(new Dates(2009, 11, 27));  // thanks giving
//        dates.add(new Dates(2009, 12, 25));  // christmas
//
//        dates.add(new Dates(2010, 1, 1));   // new year
//        dates.add(new Dates(2010, 1, 18));  // martin luther king
//        dates.add(new Dates(2010, 2, 15));  // president
//        dates.add(new Dates(2010, 5, 31));  // memorial
//        dates.add(new Dates(2010, 7, 4));  // independant
//        dates.add(new Dates(2010, 9, 6));  // labor
//        dates.add(new Dates(2010, 10, 11));  // columbus
//        dates.add(new Dates(2010, 11, 11));  // veterans
//        dates.add(new Dates(2010, 11, 25));  // thanks giving
//        dates.add(new Dates(2010, 11, 26));  // thanks giving
//        dates.add(new Dates(2010, 12, 25));  // christmas       
//        
//        dates.add(new Dates(2011, 1, 1));   // new year
//        dates.add(new Dates(2011, 1, 17));  // martin luther king
//        dates.add(new Dates(2011, 2, 21));  // president
//        dates.add(new Dates(2011, 5, 30));  // memorial
//        dates.add(new Dates(2011, 7, 4));  // independant
//        dates.add(new Dates(2011, 9, 5));  // labor
//        dates.add(new Dates(2011, 10, 10));  // columbus
//        dates.add(new Dates(2011, 11, 11));  // veterans
//        dates.add(new Dates(2011, 11, 24));  // thanks giving
//        dates.add(new Dates(2011, 11, 25));  // thanks giving
//        dates.add(new Dates(2011, 12, 25));  // christmas            

//        Calendar c = Calendar.getInstance();
//        for (Dates d : dates) {
//            c.set(d.year, d.month, d.day);
//            c.set(Calendar.HOUR_OF_DAY, 0);
//            c.set(Calendar.MINUTE, 0);
//            c.set(Calendar.SECOND, 0);
//            c.set(Calendar.MILLISECOND, 0);
//            this.natsrlCalendar.preselectDate(c);
//        }        
        
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = 2005; year <= thisYear; year++) {
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.NewYearsDayObserved(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.MartinLutherKing(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.PresidentsDay(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.MemorialDay(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.IndependenceDayObserved(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.LaborDay(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.ColumbusDay(year)));
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.VeteransDayObserved(year)));
            
            Calendar thx = getHoliday(Holidays.Thanksgiving(year));
            this.natsrlCalendar.preselectDate(thx);
            thx.add(Calendar.DATE, 1);
            this.natsrlCalendar.preselectDate(thx);
            
            this.natsrlCalendar.preselectDate(getHoliday(Holidays.ChristmasDayObserved(year)));
        }
        
        this.natsrlCalendar.setMonth(Calendar.getInstance());
    }

    protected Calendar getHoliday(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);       
        return c;
    }
    
    class Dates {

        int year;
        int month;
        int day;

        public Dates(int year, int month, int day) {
            this.year = year;
            this.month = month - 1;
            this.day = day;
        }
        
        public Dates(Date date) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);            
        }
    }
}
