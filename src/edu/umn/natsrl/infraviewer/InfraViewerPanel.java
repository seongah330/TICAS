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

package edu.umn.natsrl.infraviewer;

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.InfraObject;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.section.SectionHelper;
import edu.umn.natsrl.infra.section.SectionHelper.EntranceState;
import edu.umn.natsrl.infra.section.SectionHelper.State;
import edu.umn.natsrl.infra.section.SectionManager;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.infraobjects.Entrance;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.infraobjects.RampMeter;
import edu.umn.natsrl.infra.infraobjects.Station;
import edu.umn.natsrl.infra.types.InfraType;
import edu.umn.natsrl.map.InfraPoint;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import edu.umn.natsrl.util.FileHelper;
import edu.umn.natsrl.util.PropertiesWrapper;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class InfraViewerPanel extends javax.swing.JPanel {

    Image markerImg;
    Set<InfraPoint> waypoints = new HashSet<InfraPoint>();
    private TMO tmo = TMO.getInstance();
    private Vector<Section> sections = new Vector<Section>();
    private MapHelper mapHelper;
    private int tabNumber = 3;
    private int initZoom = 6;
    private double initLatitude = 44.974878;
    private double initLongitude = -93.233414;
    private GeoPosition[] positions = new GeoPosition[tabNumber];
    private int[] zooms = new int[tabNumber];
    private int currentTabIdx = 0;
    private final JFrame frame;
    
    
    private void initMap() {
        
        this.jmKit.setTileFactory(TMCProvider.getTileFactory());
        
        GeoPosition initCenter = new GeoPosition(initLatitude, initLongitude);
        for (int i = 0; i < tabNumber; i++) {
            positions[i] = initCenter;
            zooms[i] = initZoom;
        }
        jmKit.setAddressLocation(initCenter);
        jmKit.setZoom(initZoom);
        this.jmKit.getMiniMap().setVisible(false);
        this.jmKit.getMainMap().addPropertyChangeListener("centerPosition", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                positions[currentTabIdx] = jmKit.getMainMap().getCenterPosition();
            }
        });
        this.jmKit.getMainMap().addPropertyChangeListener("zoom", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                zooms[currentTabIdx] = jmKit.getMainMap().getZoom();
            }
        });
    }

    public void init() {
        initMap();
        this.listInfraObjects.setModel(new DefaultListModel());
        this.listSectionRNodes.setModel(new DefaultListModel());
        this.listCorridors.setModel(new DefaultListModel());
        mapHelper = new MapHelper(jmKit);
        mapHelper.setFrame(frame);
        showSection();
        loadSection();
        loadCorridors();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setLocation((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2);
    }    
    
    /** Creates new form InfraViewerPanel */
    public InfraViewerPanel(JFrame parent) {
        initComponents();
        this.frame = parent;
    }

    
    private void showSection() {
        Section section = (Section) this.cbxSections.getSelectedItem();
        if (section == null) {
            return;
        }
        this.mapHelper.showSection(section);
        for(RNode rn : section.getRNodesWithExitEntrance()) {
//            System.out.println("--> " + rn.getId() + "("+rn.getStationId()+")");
        }
        SectionHelper sectionHelper = new SectionHelper(section);
        DefaultListModel lm = (DefaultListModel) this.listSectionRNodes.getModel();
        lm.removeAllElements();
        for (State s : sectionHelper.getStates()) {
            RNode rnode = s.getRNode();
            if (rnode.isEntrance()) {
                Entrance e = (Entrance) rnode;
                EntranceState es = (EntranceState) s;
                RampMeter meter = es.getMeter();
                if (meter == null) {
                    continue;
                }
            }
            lm.addElement(rnode);
        }
        RNode first = (RNode)lm.get(0);
        mapHelper.setCenter(first);
        mapHelper.zoom(3);
    }

    private void findInfraObject() {
        String infraId = this.tbxFind.getText();
        if (infraId.isEmpty()) {
            return;
        }

        InfraObject obj = null;
        InfraType infraType = null;
        Infra infra = tmo.getInfra();
        if (infraId.startsWith("S")) {
            obj = infra.getStation(infraId);
            infraType = InfraType.STATION;
        } else if (infraId.startsWith("D")) {
            obj = infra.getDetector(infraId);
            infraType = InfraType.DETECTOR;
        } else if (infraId.startsWith("M")) {
            obj = infra.getMeter(infraId);
            infraType = InfraType.METER;
        } else {
            obj = infra.getRNode(infraId);
            infraType = InfraType.RNODE;
        }

        if (obj == null) {
            JOptionPane.showMessageDialog(this, "Cannot find " + infraId + " from infra (" + infraType + ")");
            return;
        }

        DefaultListModel lm = (DefaultListModel) this.listInfraObjects.getModel();
        lm.add(0, obj);

        showInfraObjects();
        mapHelper.setCenter(obj);
        this.tbxFind.setText("");
    }

    private void showInfraObjects() {
        DefaultListModel lm = (DefaultListModel) this.listInfraObjects.getModel();
        int size = lm.getSize();
        if (size == 0) {
            mapHelper.clear();
            return;
        }
        InfraObject[] objs = new InfraObject[size];

        for (int i = 0; i < size; i++) {
            objs[i] = (InfraObject) lm.get(i);
        }
        mapHelper.showInfraObjects(objs);
    }

    private void showInfraObjectFromFile() {
        DefaultListModel lm = (DefaultListModel) this.listInfraObjects.getModel();

        String filename = FileHelper.chooseFileToOpen(".", "Select Stored List File", FileHelper.FileFilterForProperties);
        if (filename == null) {
            return;
        }
        PropertiesWrapper ip = PropertiesWrapper.load(filename);
        if (ip == null) {
            JOptionPane.showMessageDialog(this, "Fail to load list");
            return;
        }

        lm.clear();
        String[] ids = ip.getStringArray("objects");
        Infra infra = tmo.getInfra();
        for (String id : ids) {
            if (id.startsWith("S")) {
                lm.addElement(infra.getStation(id));
            } else if (id.startsWith("D")) {
                lm.addElement(infra.getDetector(id));
            } else if (id.startsWith("M")) {
                lm.addElement(infra.getMeter(id));
            } else {
                lm.addElement(infra.getRNode(id));
            }
        }

        showInfraObjects();
    }

    private void showCorridors() {
        DefaultListModel lm = (DefaultListModel) this.listCorridors.getModel();
        Corridor[] corridors = new Corridor[lm.size()];
        for (int i = 0; i < lm.size(); i++) {
            corridors[i] = (Corridor)lm.get(i);
        }
        mapHelper.showCorridors(corridors);
    }

    /**
     * @deprecated 
     */
    private void showAllStations() {
//       if(showAllStation) {
//            mapHelper.showInfraObjects(null);
//            this.btnShowAllStations.setText("Show all stations"); 
//            showAllStation = false;
//            return;
//        }
//        ArrayList<InfraObject> objs = new ArrayList<InfraObject>();
//        Collection<Station> stations = tmo.infra.getInfraObjects(InfraType.STATION);
//        for(Station s : stations) {
//            if(s.isAvailableStation()) objs.add(s);
//        }
////        Collection<InfraObject> entrances = tmo.infra.getInfraObjects(InfraType.ENTRANCE);
////        Collection<InfraObject> exit = tmo.infra.getInfraObjects(InfraType.EXIT);
////        objs.addAll(entrances);
////        objs.addAll(exit);
//        mapHelper.showInfraObjects(objs.toArray(new InfraObject[objs.size()]));
//        showAllStation = true;
//        this.btnShowAllStations.setText("Remove all stations");        
    }    

    
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

    private void loadCorridors() {
        for (Corridor c : this.tmo.getInfra().getCorridors()) {
            this.cbxCorridors.addItem(c);
        }
    }
    
    /**
     * Open section editor
     */
    public void openSectionEditor() {
        tmo.openSectionEditor();
        //tmo.openSectionEditorDialog(this, true);
        this.loadSection();
    }    
    
    public void openDistanceDialog() {
        RNodeDistanceDialog rdd = new RNodeDistanceDialog(frame, false);
        rdd.setLocationRelativeTo(this);
        rdd.setVisible(true);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        mainTab = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        cbxSections = new javax.swing.JComboBox();
        btnOpenSectionEditor = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listSectionRNodes = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        tbxFind = new javax.swing.JTextField();
        btnFind = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        listInfraObjects = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnLoadList = new javax.swing.JButton();
        btnSaveList = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnClearCorridorList = new javax.swing.JButton();
        cbxCorridors = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        listCorridors = new javax.swing.JList();
        btnAddCorridor = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jmKit = new org.jdesktop.swingx.JXMapKit();

        mainTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabStateChanged(evt);
            }
        });

        cbxSections.setFont(new java.awt.Font("Verdana", 0, 10));
        cbxSections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxSectionsActionPerformed(evt);
            }
        });

        btnOpenSectionEditor.setFont(new java.awt.Font("Verdana", 0, 11));
        btnOpenSectionEditor.setText("Section Editor");
        btnOpenSectionEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenSectionEditorActionPerformed(evt);
            }
        });

        listSectionRNodes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listSectionRNodes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listSectionRNodesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listSectionRNodes);

        jLabel20.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel20.setText("* Center : double-click item");

        jLabel21.setFont(new java.awt.Font("Verdana", 1, 11));
        jLabel21.setText("Action");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnOpenSectionEditor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(cbxSections, 0, 241, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbxSections, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnOpenSectionEditor, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTab.addTab("Section", jPanel5);

        tbxFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbxFindKeyPressed(evt);
            }
        });

        btnFind.setFont(new java.awt.Font("Verdana", 0, 11));
        btnFind.setText("Find");
        btnFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindActionPerformed(evt);
            }
        });

        listInfraObjects.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listInfraObjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listInfraObjectsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(listInfraObjects);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel5.setText("Search Example");

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel6.setText("Station : S43");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel7.setText("Detector : D281");

        jLabel8.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel8.setText("Meter : M35WN14");

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel9.setText("RNode : Nrnd_86405");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel3.setText("* Center : double-click item");

        jLabel10.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel10.setText("* Remove : right-click item");

        jLabel11.setFont(new java.awt.Font("Verdana", 1, 11));
        jLabel11.setText("Action");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel3)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addGap(4, 4, 4)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnLoadList.setFont(new java.awt.Font("Verdana", 0, 11));
        btnLoadList.setText("Load List");
        btnLoadList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadListActionPerformed(evt);
            }
        });

        btnSaveList.setFont(new java.awt.Font("Verdana", 0, 11));
        btnSaveList.setText("Save List");
        btnSaveList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveListActionPerformed(evt);
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
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tbxFind, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFind, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnLoadList)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                        .addComponent(btnSaveList)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbxFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFind, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(67, 67, 67))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLoadList)
                            .addComponent(btnSaveList))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTab.addTab("Search", jPanel1);

        btnClearCorridorList.setText("Clear List");
        btnClearCorridorList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearCorridorListActionPerformed(evt);
            }
        });

        listCorridors.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listCorridors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listCorridorsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(listCorridors);

        btnAddCorridor.setText("Add");
        btnAddCorridor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCorridorActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel18.setText("* Remove : right-click item");

        jLabel19.setFont(new java.awt.Font("Verdana", 1, 11));
        jLabel19.setText("Action");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jLabel18))
                .addContainerGap(79, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(btnClearCorridorList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cbxCorridors, 0, 178, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddCorridor))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxCorridors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddCorridor))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnClearCorridorList, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainTab.addTab("Corridors", jPanel3);

        jSplitPane1.setLeftComponent(mainTab);

        jmKit.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        jSplitPane1.setRightComponent(jmKit);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 899, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 879, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 549, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbxSectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxSectionsActionPerformed
        if (mapHelper == null) {
            return;
        }
        showSection();
}//GEN-LAST:event_cbxSectionsActionPerformed

    private void btnOpenSectionEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSectionEditorActionPerformed
        this.openSectionEditor();
}//GEN-LAST:event_btnOpenSectionEditorActionPerformed

    private void listSectionRNodesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listSectionRNodesMouseClicked
        DefaultListModel lm = (DefaultListModel) this.listSectionRNodes.getModel();
        if (evt.getClickCount() > 1) {
            InfraObject o = (InfraObject) this.listSectionRNodes.getSelectedValue();
            mapHelper.setCenter(o);
            mapHelper.zoom(2);
        }
}//GEN-LAST:event_listSectionRNodesMouseClicked

    private void tbxFindKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbxFindKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            findInfraObject();
        }
}//GEN-LAST:event_tbxFindKeyPressed

    private void btnFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindActionPerformed
        findInfraObject();
}//GEN-LAST:event_btnFindActionPerformed

    private void listInfraObjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listInfraObjectsMouseClicked
        DefaultListModel lm = (DefaultListModel) this.listInfraObjects.getModel();
        if (evt.getButton() == MouseEvent.BUTTON3) {
            lm.removeElement(this.listInfraObjects.getSelectedValue());
            this.showInfraObjects();
        } else if (evt.getClickCount() > 1) {
            InfraObject o = (InfraObject) this.listInfraObjects.getSelectedValue();
            mapHelper.setCenter(o);
            mapHelper.zoom(2);
        }
}//GEN-LAST:event_listInfraObjectsMouseClicked

    private void btnLoadListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadListActionPerformed
        showInfraObjectFromFile();
}//GEN-LAST:event_btnLoadListActionPerformed

    private void btnSaveListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveListActionPerformed
        PropertiesWrapper ip = new PropertiesWrapper();
        DefaultListModel lm = (DefaultListModel) this.listInfraObjects.getModel();
        int size = lm.size();
        if (size <= 0) {
            return;
        }
        String[] ids = new String[size];
        String filename = FileHelper.chooseFileToSave(".", "Select File to Save", FileHelper.FileFilterForProperties);
        if (filename == null) {
            return;
        }
        filename = FileHelper.checkExtension(filename, FileHelper.FileFilterForProperties);
        for (int i = 0; i < size; i++) {
            InfraObject obj = (InfraObject) lm.get(i);
            InfraType type = obj.getInfraType();
            if (type.isStation()) {
                ids[i] = ((Station) obj).getStationId();
            } else {
                ids[i] = obj.getId();
            }
        }
        ip.put("objects", ids);
        ip.save(filename);
}//GEN-LAST:event_btnSaveListActionPerformed

    private void btnClearCorridorListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCorridorListActionPerformed
        DefaultListModel lm = (DefaultListModel) this.listCorridors.getModel();
        lm.clear();
        this.showCorridors();
}//GEN-LAST:event_btnClearCorridorListActionPerformed

    private void listCorridorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listCorridorsMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            DefaultListModel lm = (DefaultListModel) this.listCorridors.getModel();
            Corridor c = (Corridor) this.listCorridors.getSelectedValue();
            lm.removeElement(c);
        }
        showCorridors();
}//GEN-LAST:event_listCorridorsMouseClicked

    private void btnAddCorridorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCorridorActionPerformed
        Corridor c = (Corridor) this.cbxCorridors.getSelectedItem();
        DefaultListModel lm = (DefaultListModel) this.listCorridors.getModel();
        if (c != null && !lm.contains(c)) {
            lm.addElement(c);
        }
        showCorridors();
        RNode first = c.getRnodes().get(0);
        mapHelper.setCenter(first);
}//GEN-LAST:event_btnAddCorridorActionPerformed

    private void mainTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabStateChanged
        if (mapHelper == null) {
            return;
        }
        currentTabIdx = this.mainTab.getSelectedIndex();
        if (currentTabIdx == 0) {
            this.showSection();
        } else if (currentTabIdx == 1) {
            this.showInfraObjects();
        } else if (currentTabIdx == 2) {
            this.showCorridors();
        }
        
        this.jmKit.getMainMap().setCenterPosition(positions[currentTabIdx]);
        this.jmKit.getMainMap().setZoom(zooms[currentTabIdx]);
}//GEN-LAST:event_mainTabStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCorridor;
    private javax.swing.JButton btnClearCorridorList;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnLoadList;
    private javax.swing.JButton btnOpenSectionEditor;
    private javax.swing.JButton btnSaveList;
    private javax.swing.JComboBox cbxCorridors;
    private javax.swing.JComboBox cbxSections;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private org.jdesktop.swingx.JXMapKit jmKit;
    private javax.swing.JList listCorridors;
    private javax.swing.JList listInfraObjects;
    private javax.swing.JList listSectionRNodes;
    private javax.swing.JTabbedPane mainTab;
    private javax.swing.JTextField tbxFind;
    // End of variables declaration//GEN-END:variables
}
