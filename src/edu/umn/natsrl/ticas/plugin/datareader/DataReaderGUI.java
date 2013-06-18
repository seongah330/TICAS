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
 * SimulationExampleGUI.java
 *
 * Created on Apr 21, 2011, 3:45:47 PM
 */

package edu.umn.natsrl.ticas.plugin.datareader;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Detector;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.interfaces.IDetectorChecker;
import edu.umn.natsrl.evaluation.Interval;
import edu.umn.natsrl.ticas.plugin.PluginFrame;
import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *
 * @author Chongmyung Park
 */
public class DataReaderGUI extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    
    /** Creates new form SimulationExampleGUI */
    public DataReaderGUI(PluginFrame simFrame) {
        initComponents();
        init();
        simFrame.setSize(640, 480);
    }
    private void init() {
        DateChecker dc = DateChecker.getInstance();
              
        // date checker
        this.natsrlCalendar.setDateChecker(dc);

        // interval
        for (Interval i : Interval.values()) {
            this.cbxInterval.addItem(i);
        }
        
        // duration
        this.cbxDuration.addItem("Select");
        for (int i = 1; i <= 32; i++) {
            this.cbxDuration.addItem(i);
        }
    }

    private void readData(Station station, Vector<Period> periods, IDetectorChecker dc) throws Exception {
        boolean likeOneDay = this.chkLikeOneDay.isSelected();
        boolean e1hour = this.chkSepEach1Hour.isSelected();
        boolean readDensity = this.chkDensity.isSelected();
        boolean readSpeed = this.chkSpeed.isSelected();
        boolean readFlow = this.chkFlow.isSelected();
        
        WritableWorkbook workbook = Workbook.createWorkbook(new File(station.getStationId()+".xls"));
        WritableSheet sheet = workbook.createSheet(station.getStationId(), 0);
        int colIdx = 0;
        
        if(!likeOneDay) addData(sheet, colIdx++, new String[] {"", ""}, periods.get(0).getTimeline());
        
        Vector<Double> density = new Vector<Double>();
        Vector<Double> flow = new Vector<Double>();
        Vector<Double> speed = new Vector<Double>();
        
        int ci=1;
        for(Period p : periods) {
            station.loadData(p, null);
            String day = String.format("%02d", p.start_date);
            if(!likeOneDay) {
                if(readDensity) {
                    addData(sheet, colIdx++, new String[]{day, "Density"}, station.getDensity(dc));
                    if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Density"}, station.getDensity(dc), p.interval);
                }
                if(readFlow) {
                    addData(sheet, colIdx++, new String[]{day, "Flow"}, station.getFlow(dc));
                    if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Flow"}, station.getFlow(dc), p.interval);
                }
                if(readSpeed) {
                    addData(sheet, colIdx++, new String[]{day, "Speed"}, station.getSpeed(dc));
                    if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Speed"}, station.getSpeed(dc), p.interval);
                }            
            } else {
                if(readDensity) addToVector(density, station.getDensity(dc));
                if(readFlow) addToVector(flow, station.getFlow(dc));
                if(readSpeed) addToVector(speed, station.getSpeed(dc));
            }
            ci++;
        }
        
        if(likeOneDay) {
            int interval = periods.get(0).interval;
            if(readDensity) {
                addData(sheet, colIdx++, new String[]{"Density"}, density);
                if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Density"}, station.getDensity(dc), interval);
            }
            if(readFlow) {
                addData(sheet, colIdx++, new String[]{"Flow"}, flow);
                if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Flow"}, station.getFlow(dc), interval);
            }
            if(readSpeed) {
                addData(sheet, colIdx++, new String[]{"Speed"}, speed);
                if(e1hour) colIdx = addEver1HourData(sheet, colIdx, new String[]{"Speed"}, station.getSpeed(dc), interval);
            }                        
            
            TreeMap<Long, Vector<Double>> pairQK = new TreeMap<Long, Vector<Double>>();
            TreeMap<Long, Vector<Double>> pairUK = new TreeMap<Long, Vector<Double>>();
            if(readDensity && readFlow) {
                for(int i=0; i<density.size(); i++) {
                    double k = density.get(i);
                    double q = flow.get(i);
                    double u = speed.get(i);
                    long ld = Math.round(k);
                    Vector<Double> qkBank = pairQK.get(ld);
                    Vector<Double> ukBank = pairUK.get(ld);
                    if(qkBank == null) {
                        qkBank = new Vector<Double>();
                        pairQK.put(ld, qkBank);
                    }
                    if(ukBank == null) {
                        ukBank = new Vector<Double>();                        
                        pairUK.put(ld, ukBank);                        
                    }
                    qkBank.add(q);
                    ukBank.add(u);
                }
                
                Iterator<Long> itr = pairQK.keySet().iterator();
                Vector<Double> avQK = new Vector<Double>();
                Vector<Double> minQK = new Vector<Double>();
                Vector<Double> maxQK = new Vector<Double>();
                Vector<Double> avUK = new Vector<Double>();
                Vector<Double> ldensity = new Vector<Double>();
                while(itr.hasNext()) {
                    long key = itr.next();
                    double qa = average(pairQK.get(key));
                    double ka = average(pairUK.get(key));
                    double min = min(pairQK.get(key));
                    double max = max(pairQK.get(key));
                    ldensity.add((double)key);
                    maxQK.add(max);
                    minQK.add(min);
                    avQK.add(qa);
                    avUK.add(ka);
                }
                
//                Iterator<Long> itr2 = pairUK.keySet().iterator();
//                while(itr2.hasNext()) {
//                    long key = itr2.next();
//                    double ka = average(pairUK.get(key));
//                    lspeed.add((double)key);
//                    avUK.add(ka);
//                }                
                
                addData(sheet, colIdx++, new String[]{"Density"}, ldensity);
                addData(sheet, colIdx++, new String[]{"Q Average"}, avQK);
                addData(sheet, colIdx++, new String[]{"Q Min"}, minQK);
                addData(sheet, colIdx++, new String[]{"Q Max"}, maxQK);
                addData(sheet, colIdx++, new String[]{"Density"}, ldensity);
                addData(sheet, colIdx++, new String[]{"U Average"}, avUK);                
            }            
        }
                
        workbook.write();
        workbook.close();
        
    }
    
    private double average(Vector<Double> data) {
        double sum = 0;
        for(double d : data) {
            sum += d;
        }
        return sum / data.size();
    }
    
    private double min(Vector<Double> data) {
        double min = 9999;
        for(double d : data) {
            if(min > d) min = d;
        }
        return min;
    }   
    
    private double max(Vector<Double> data) {
        double max = -999;
        for(double d : data) {
            if(max < d) max = d;
        }
        return max;
    }       

    
    private void addToVector(Vector<Double> vec, double[] arr) {
        for(double d : arr) {
            vec.add(d);
        }
    }
    
    private void readData() throws Exception {

        final boolean withoutHOV = this.chkWithoutHOV.isSelected();
        final boolean withoutWavetronics = this.chkWithoutWavetronics.isSelected();
        
        // detector checker
        IDetectorChecker dc = new IDetectorChecker() {
            @Override
            public boolean check(Detector d) {
                if(withoutHOV && d.isHov()) return false;
                if(withoutWavetronics && d.isWavetronics()) return false;
                if(d.isAbandoned()) return false;
                return true;
            }
        };
        
        // period
        Calendar[] selectedDates = this.natsrlCalendar.getSelectedDates();
        Calendar c1, c2;
        
        Vector<Period> periods = new Vector<Period>();
        int interval = ((Interval) this.cbxInterval.getSelectedItem()).second;
        int start_hour = Integer.parseInt(this.cbxStartHour.getSelectedItem().toString());
        int start_min = Integer.parseInt(this.cbxStartMin.getSelectedItem().toString());
        int end_hour = Integer.parseInt(this.cbxEndHour.getSelectedItem().toString());
        int end_min = Integer.parseInt(this.cbxEndMin.getSelectedItem().toString());

        
        for (Calendar date : selectedDates) {
            c1 = (Calendar) date.clone();
            c2 = (Calendar) date.clone();

            c1.set(Calendar.HOUR, start_hour);
            c1.set(Calendar.MINUTE, start_min);
            c1.set(Calendar.SECOND, 0);

            c2.set(Calendar.HOUR, end_hour);
            c2.set(Calendar.MINUTE, end_min);
            c2.set(Calendar.SECOND, 0);

            
            if (this.cbxDuration.getSelectedIndex() > 0) {
                c2.set(Calendar.HOUR, start_hour);
                c2.set(Calendar.MINUTE, start_min);
                c2.add(Calendar.HOUR, (Integer) this.cbxDuration.getSelectedItem());
            } else {
                c2.set(Calendar.HOUR, end_hour);
                c2.set(Calendar.MINUTE, end_min);
            }            
            
            periods.add(new Period(c1.getTime(), c2.getTime(), interval));
        }
        
        Station station = tmo.getInfra().getStation(this.tbxStation.getText());
        readData(station, periods, dc);
        
        
    }
    
    private int addEver1HourData(WritableSheet sheet, int column, String[] labels, double[] data, int interval) {
        int limit = 3600/interval;
        int sep = data.length / limit;
        if(data.length % limit != 0) {
            System.out.println("Not matched : " + data.length + ", " + limit + ", " + sep + ", " + (data.length%limit));
            return column;
        }
                
        for(int i=0; i<sep; i++) {
            int to = limit * (i+1);
            double[] sdata = new double[limit];
            for(int j=i*limit, k=0; j<to; j++, k++) {
                sdata[k] = data[j];
            }            
            addData(sheet, column++, new String[]{labels[0]+i}, sdata);
        }
        
        return column;
    }    
    
    private void addData(WritableSheet sheet, int column, String[] labels, double[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Number(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void addData(WritableSheet sheet, int column, String[] labels, Vector<Double> data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            
            for(int r=0; r<data.size(); r++)
            {
                sheet.addCell(new Number(column, row++, data.get(r)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    private void addData(WritableSheet sheet, int column, String[] labels, String[] data)
    {
        try {
            int row = 0;
            for(int r=0; r<labels.length; r++) {
                sheet.addCell(new Label(column, row++, labels[r]));
            }
            for(int r=0; r<data.length; r++)
            {
                sheet.addCell(new Label(column, row++, data[r]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnRead = new javax.swing.JButton();
        tbxStation = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        chkDensity = new javax.swing.JCheckBox();
        chkFlow = new javax.swing.JCheckBox();
        chkSpeed = new javax.swing.JCheckBox();
        natsrlCalendar = new edu.umn.natsrl.gadget.calendar.NATSRLCalendar();
        jLabel5 = new javax.swing.JLabel();
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
        jLabel6 = new javax.swing.JLabel();
        chkWithoutHOV = new javax.swing.JCheckBox();
        chkWithoutWavetronics = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        chkLikeOneDay = new javax.swing.JCheckBox();
        chkSepEach1Hour = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cbxDuration = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel1.setText("Station");

        btnRead.setFont(new java.awt.Font("Verdana", 0, 12));
        btnRead.setText("Read");
        btnRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadActionPerformed(evt);
            }
        });

        tbxStation.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tbxStation.setText("S35");
        tbxStation.setMinimumSize(new java.awt.Dimension(6, 25));

        jLabel3.setText("e.g. S43");

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel4.setText("Data Type");

        chkDensity.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkDensity.setSelected(true);
        chkDensity.setText("Density");

        chkFlow.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkFlow.setSelected(true);
        chkFlow.setText("Flow");

        chkSpeed.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        chkSpeed.setSelected(true);
        chkSpeed.setText("Speed");

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        jLabel5.setText("Dates");

        jLabel17.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel17.setText("Time Interval");

        cbxInterval.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel18.setText("Start Time");

        cbxStartHour.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxStartHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxStartHourActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel14.setText(":");

        cbxStartMin.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxStartMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel20.setText("End Time");

        cbxEndHour.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        cbxEndHour.setSelectedIndex(9);
        cbxEndHour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxEndHourActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel15.setText(":");

        cbxEndMin.setFont(new java.awt.Font("Verdana", 0, 12));
        cbxEndMin.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        jLabel6.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel6.setText("Detecor Filter");

        chkWithoutHOV.setFont(new java.awt.Font("Verdana", 0, 12));
        chkWithoutHOV.setSelected(true);
        chkWithoutHOV.setText("w/o HOV");

        chkWithoutWavetronics.setFont(new java.awt.Font("Verdana", 0, 12));
        chkWithoutWavetronics.setSelected(true);
        chkWithoutWavetronics.setText("w/o wavetronics");

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel2.setText("Option");

        chkLikeOneDay.setFont(new java.awt.Font("Verdana", 0, 12));
        chkLikeOneDay.setSelected(true);
        chkLikeOneDay.setText("Like one day");

        chkSepEach1Hour.setFont(new java.awt.Font("Verdana", 0, 12));
        chkSepEach1Hour.setSelected(true);
        chkSepEach1Hour.setText("Sep. each 1hour");

        jLabel7.setText("or");

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel8.setText("for");

        cbxDuration.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        cbxDuration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDurationActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 12));
        jLabel9.setText("hour");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel20))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbxEndHour, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbxStartMin, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel9))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel17)
                            .addGap(18, 18, 18)
                            .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkLikeOneDay)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tbxStation, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3))
                    .addComponent(jLabel4)
                    .addComponent(jLabel1)
                    .addComponent(jLabel6)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkWithoutHOV)
                        .addGap(18, 18, 18)
                        .addComponent(chkWithoutWavetronics))
                    .addComponent(btnRead, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                    .addComponent(chkSepEach1Hour)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chkDensity)
                        .addGap(18, 18, 18)
                        .addComponent(chkSpeed)
                        .addGap(18, 18, 18)
                        .addComponent(chkFlow)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(natsrlCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(cbxInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(cbxStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)
                            .addComponent(cbxStartMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbxStation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkDensity)
                            .addComponent(chkSpeed)
                            .addComponent(chkFlow))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chkWithoutHOV)
                            .addComponent(chkWithoutWavetronics))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkLikeOneDay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(chkSepEach1Hour)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(cbxEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(cbxEndMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRead, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(cbxDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadActionPerformed
        try {
            readData();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnReadActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRead;
    private javax.swing.JComboBox cbxDuration;
    private javax.swing.JComboBox cbxEndHour;
    private javax.swing.JComboBox cbxEndMin;
    private javax.swing.JComboBox cbxInterval;
    private javax.swing.JComboBox cbxStartHour;
    private javax.swing.JComboBox cbxStartMin;
    private javax.swing.JCheckBox chkDensity;
    private javax.swing.JCheckBox chkFlow;
    private javax.swing.JCheckBox chkLikeOneDay;
    private javax.swing.JCheckBox chkSepEach1Hour;
    private javax.swing.JCheckBox chkSpeed;
    private javax.swing.JCheckBox chkWithoutHOV;
    private javax.swing.JCheckBox chkWithoutWavetronics;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private edu.umn.natsrl.gadget.calendar.NATSRLCalendar natsrlCalendar;
    private javax.swing.JTextField tbxStation;
    // End of variables declaration//GEN-END:variables


}
