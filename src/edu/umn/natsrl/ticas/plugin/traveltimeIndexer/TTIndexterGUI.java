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
import edu.umn.natsrl.infra.weather.WeatherTMC;
import edu.umn.natsrl.infra.weather.type.WeatherDevice;
import edu.umn.natsrl.infra.weather.type.WeatherType;
import edu.umn.natsrl.ticas.RunningDialog;
import edu.umn.natsrl.ticas.TICAS2FrameLab;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class TTIndexterGUI extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    private List<Section> sections = new ArrayList<Section>();
    private PluginFrame simFrame;
    private boolean isHoliday = false;
    private List<String> nonweather;

    public TTIndexterGUI(PluginFrame frame) {
        initComponents();
        this.simFrame = frame;
        this.simFrame.setSize(740, 830);
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
        this.label_loadC.setVisible(false);
        
        /**
         * set WeatherList
         */
        this.cbxWeatherDList.removeAllItems();
        for(WeatherDevice wd : WeatherDevice.values()){
            this.cbxWeatherDList.addItem(wd);
        }
        this.cbxWeatherAll.setSelected(true);
        
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                setHolidays();
            }
        },10);
        this.natsrlCalendar.setDateChecker(DateChecker.getInstance());
    }

    private void evaluate() {
        this.btnEvaulate.setEnabled(false);
        this.btnEvaulate.setText("Calculating...");
        
        List<WeatherTMC> weatherList = new ArrayList<WeatherTMC>();
        List<Period> periods = getPeriodwithWeather(weatherList,getPeriods());
        System.out.println("after P Length : "+periods.size());
        System.out.println("after Selected Length : "+weatherList.size());
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
        RunningDialog rd = new RunningDialog(simFrame, true);
        rd.setLocationRelativeTo(this);
        Timer t = new Timer();   
        t.schedule(new TravelTimeIndexer(section, periods, targetStations, dataInterval, evalInterval, ttInterval, freeflowTT,weatherList, rd), 10);
        rd.showLog();
        rd.setTimer(t);
        rd.setVisible(true);   
//        TravelTimeIndexer ev = new TravelTimeIndexer(section, periods, targetStations, dataInterval, evalInterval, ttInterval, freeflowTT,weatherList);
//        ev.run();
        //send non Weather Data message 
        checkNonWeather(nonweather);
        System.out.println("End of Evaulation");
        if (simFrame != null) {
            this.simFrame.setAlwaysOnTop(true);
        }
        JOptionPane.showMessageDialog(this.simFrame, "Done");
        if (simFrame != null) {
            this.simFrame.setAlwaysOnTop(false);
        }
        
        this.btnEvaulate.setEnabled(true);
        this.btnEvaulate.setText("Evaluate");
    }
    private List<Period> getPeriods(){
        return getPeriods(null);
    }
    private List<Period> getPeriods(List<Calendar> weatherSkip) {
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
            
            if(weatherSkip != null){
                for(Calendar c : weatherSkip){
                    if(cursor.equals(c)){
                        System.out.println("W skip : "+c.getTime().toString());
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
        label_loadC = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        tbxFreeFlowTravelTime = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        cbxWeatherDList = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        cbxWeatherAll = new javax.swing.JCheckBox();
        jLabel15 = new javax.swing.JLabel();
        cbxWeatherDry = new javax.swing.JCheckBox();
        cbxWeatherRain = new javax.swing.JCheckBox();
        cbxWeatherMixed = new javax.swing.JCheckBox();
        cbxWeatherSnow = new javax.swing.JCheckBox();
        cbxWeatherHail = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Section", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
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
                .addComponent(cbxSections, 0, 237, Short.MAX_VALUE)
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

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel1.setText("Start Date");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel2.setText("End Date");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel3.setText("Start Hour");

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel4.setText("End Hour");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.setMinimumSize(new java.awt.Dimension(40, 20));

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
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
                    .addComponent(cbxEndHour, 0, 147, Short.MAX_VALUE)
                    .addComponent(cbxStartHour, 0, 147, Short.MAX_VALUE)
                    .addComponent(calEndDate, 0, 0, Short.MAX_VALUE)
                    .addComponent(calStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
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
                    .addComponent(cbxStartHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(82, 82, 82))
        );

        btnEvaulate.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        btnEvaulate.setText("Evaluate");
        btnEvaulate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEvaulateActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Intervals", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel5.setText("Speed/Accel/Volume");

        cbxDataInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel6.setText("VMT/DVH Interval");

        cbxEvalInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel9.setText("TT Interval");

        cbxTTInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

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
                        .addComponent(cbxDataInterval, 0, 1, Short.MAX_VALUE))
                    .addComponent(jLabel6)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(74, 74, 74)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxTTInterval, 0, 1, Short.MAX_VALUE)
                            .addComponent(cbxEvalInterval, 0, 1, Short.MAX_VALUE))))
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
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Volume Anaisys", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel7.setText("Target Station (optional)");

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel8.setText("(e.g. S43 S44 S45)");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(tbxVolumeAnalTargetStation, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbxVolumeAnalTargetStation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Date Option", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        chkWednesday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkWednesday.setSelected(true);
        chkWednesday.setText("Wed");

        chkSunday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkSunday.setText("Sun");

        chkMonday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkMonday.setSelected(true);
        chkMonday.setText("Mon");

        chkTuesday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkTuesday.setSelected(true);
        chkTuesday.setText("Tue");

        chkFriday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkFriday.setText("Fri");

        chkThursday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        chkThursday.setSelected(true);
        chkThursday.setText("Thu");

        chkSaturday.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
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

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel11.setText("Except dayes");

        jLabel12.setFont(new java.awt.Font("Verdana", 1, 11)); // NOI18N
        jLabel12.setText("Available dates");

        label_loadC.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        label_loadC.setText("Loading Calendar..");

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
                    .addComponent(jLabel12)
                    .addComponent(label_loadC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label_loadC)
                .addGap(3, 3, 3)
                .addComponent(natsrlCalendar, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ETC", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        jLabel10.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel10.setText("Free-flow Travel time (optional)");

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
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
                .addContainerGap(308, Short.MAX_VALUE))
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

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Weather", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 12))); // NOI18N

        cbxWeatherDList.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherDList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel14.setText("Device List");

        cbxWeatherAll.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherAll.setText("All Weather");
        cbxWeatherAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherAllActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        jLabel15.setText("Available Weather");

        cbxWeatherDry.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherDry.setText("Dry");
        cbxWeatherDry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherDryActionPerformed(evt);
            }
        });

        cbxWeatherRain.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherRain.setText("Rain");
        cbxWeatherRain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherRainActionPerformed(evt);
            }
        });

        cbxWeatherMixed.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherMixed.setText("Rain&Snow");
        cbxWeatherMixed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherMixedActionPerformed(evt);
            }
        });

        cbxWeatherSnow.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherSnow.setText("Snow");
        cbxWeatherSnow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherSnowActionPerformed(evt);
            }
        });

        cbxWeatherHail.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxWeatherHail.setText("Hailstone");
        cbxWeatherHail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxWeatherHailActionPerformed(evt);
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
                        .addComponent(cbxWeatherDry)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxWeatherRain)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxWeatherMixed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxWeatherSnow)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxWeatherHail))
                    .addComponent(cbxWeatherAll)
                    .addComponent(jLabel14)
                    .addComponent(cbxWeatherDList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxWeatherDList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxWeatherAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxWeatherDry)
                    .addComponent(cbxWeatherRain)
                    .addComponent(cbxWeatherMixed)
                    .addComponent(cbxWeatherSnow)
                    .addComponent(cbxWeatherHail))
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
                    .addComponent(btnEvaulate, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        if(isHoliday)
            this.evaluate();
        else
            JOptionPane.showMessageDialog(this.simFrame, "Setting of Except dayes is not completed. \nPlease Wait..");
    }//GEN-LAST:event_btnEvaulateActionPerformed

    private void cbxWeatherAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherAllActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherAll);
    }//GEN-LAST:event_cbxWeatherAllActionPerformed

    private void cbxWeatherDryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherDryActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherDry);
    }//GEN-LAST:event_cbxWeatherDryActionPerformed

    private void cbxWeatherRainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherRainActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherRain);
    }//GEN-LAST:event_cbxWeatherRainActionPerformed

    private void cbxWeatherMixedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherMixedActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherMixed);
    }//GEN-LAST:event_cbxWeatherMixedActionPerformed

    private void cbxWeatherSnowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherSnowActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherSnow);
    }//GEN-LAST:event_cbxWeatherSnowActionPerformed

    private void cbxWeatherHailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxWeatherHailActionPerformed
        // TODO add your handling code here:
        removeAllCheckBox(this.cbxWeatherHail);
    }//GEN-LAST:event_cbxWeatherHailActionPerformed

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
    private javax.swing.JCheckBox cbxWeatherAll;
    private javax.swing.JComboBox cbxWeatherDList;
    private javax.swing.JCheckBox cbxWeatherDry;
    private javax.swing.JCheckBox cbxWeatherHail;
    private javax.swing.JCheckBox cbxWeatherMixed;
    private javax.swing.JCheckBox cbxWeatherRain;
    private javax.swing.JCheckBox cbxWeatherSnow;
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
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel label_loadC;
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
        this.natsrlCalendar.setVisible(false);
        this.label_loadC.setVisible(true);
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
        
        this.natsrlCalendar.setVisible(true);
        this.label_loadC.setVisible(false);
        this.isHoliday = true;
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

    private List<Period> getPeriodwithWeather(List<WeatherTMC> weatherList, List<Period> periods) {
        WeatherDevice selectedDevice = (WeatherDevice)this.cbxWeatherDList.getSelectedItem();
        System.out.println(selectedDevice.toString());
        /**
         * 0 : All
         * 1 : Dry
         * 2 : Rain
         * 3 : Mix
         * 4 : snow
         * 5 : hail
         */
        Boolean[] Weathercheck = new Boolean[]{this.cbxWeatherAll.isSelected(),this.cbxWeatherDry.isSelected(),
                                            this.cbxWeatherRain.isSelected(),this.cbxWeatherMixed.isSelected(),
                                            this.cbxWeatherSnow.isSelected(),this.cbxWeatherHail.isSelected()};
    
        List<String> nonWeather = new ArrayList<String>();
        
        List<Calendar> weatherSkip = new ArrayList<Calendar>();
        
        for(Period p : periods){
            Period rp = p.clone();
            rp.setInterval(60);
            System.out.println(p.getPeriodString());
            WeatherTMC we = new WeatherTMC(selectedDevice,rp);
            we.load();
            /**
             * Check Accurary
             */
            if(!we.isAccurary()){
                nonWeather.add(we.getPeriod().getPeriodStringHWithoutTime());
                continue;
            }
            /**
             * Check Skip date
             */
            if(!Weathercheck[0]){
                if(!Weathercheck[we.getWeatherType().getId()+1]){
                    Calendar c = Calendar.getInstance();
                    c.setTime(we.getPeriod().startDate);
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);
                    c.set(Calendar.MILLISECOND, 0);
                    weatherSkip.add(c);
                }
            }
            
            /**
             * set Selected Date
             */
            if(Weathercheck[0] || Weathercheck[we.getWeatherType().getId()+1])
                weatherList.add(we);
            System.out.println("Weather Type : "+we.getWeatherType().toString()+", Acc : "+we.getAccurary()+", Rainfall : "+we.getAvgRainFall());
        }
        System.out.println("Period Length : "+periods.size());
        System.out.println("Seletected length : "+weatherList.size());
        System.out.println("Skip length : "+weatherSkip.size());
        for(Calendar c : weatherSkip){
            System.out.println("Cal : "+c.getTime().toString());
        }
        this.nonweather = nonWeather;
        return getPeriods(weatherSkip);
    }

    private void removeAllCheckBox(JCheckBox selectedcbx) {
        JCheckBox[] WeatherCbx = new JCheckBox[]{this.cbxWeatherDry,this.cbxWeatherRain,this.cbxWeatherMixed,
                                                this.cbxWeatherSnow,this.cbxWeatherHail};
        if(!selectedcbx.isSelected())
            return;
        
        if(selectedcbx.equals(this.cbxWeatherAll)){
            for(JCheckBox cbx : WeatherCbx){
                cbx.setSelected(false);
            }
        }else{
            this.cbxWeatherAll.setSelected(false);
        }
    }

    private void checkNonWeather(List<String> nonWeather) {
        if(nonWeather.size() > 0){
            int wcnt = 0;
            String str = "There is no Weathere Data.\n It Does not apply to the following dates."+"\n";
            for(String nw : nonWeather){
                if(wcnt > 13){
                    str += "and more..";
                    break;
                }
                str += "- "+nw + "\n";
                wcnt++;
            }
            JOptionPane.showMessageDialog(null, str);
        }
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
