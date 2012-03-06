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

package edu.umn.natsrl.infra.section;

import edu.umn.natsrl.infra.Infra;
import edu.umn.natsrl.infra.infraobjects.Corridor;
import edu.umn.natsrl.infra.InfraConstants;
import edu.umn.natsrl.infra.infraobjects.RNode;
import edu.umn.natsrl.infra.Section;
import edu.umn.natsrl.infra.TMO;
import edu.umn.natsrl.map.MapHelper;
import edu.umn.natsrl.map.TMCProvider;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class SectionEditorPanel extends javax.swing.JPanel {

    private TMO tmo = TMO.getInstance();
    private Vector<Corridor> corridors = new Vector<Corridor>();
    private Section selectedSection;    
    private JComponent rootPane = null;
    private final Infra infra;
    private MapHelper mapHelper;
    private ISectionChanged changeListener;
    
    /** Creates new form SectionEditorPanel */
    public SectionEditorPanel() {
        
        initComponents();
        jxMap.setTileFactory(TMCProvider.getTileFactory());
        mapHelper = new MapHelper(this.jxMap);
        jxMap.getMiniMap().setVisible(false);
        this.tabMainTab.setSelectedIndex(1);
        SectionCreatorPanel scp = new SectionCreatorPanel();
        scp.setChangeListener(new ISectionChanged() {
            @Override
            public void sectionChanged(SectionChangedStatus status) {
                sectionIsChanged(status);                
            }
        });
        this.panCreateSection.add(scp);        
        if(!tmo.isLoaded()) tmo.setup();
        infra = tmo.getInfra();
        corridors = infra.getCorridors();

         this.tbSectionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                int row = tbSectionList.rowAtPoint(e.getPoint());
                int col = tbSectionList.columnAtPoint(e.getPoint());
                DefaultTableModel tm = (DefaultTableModel)tbSectionList.getModel();                
                Section s = (Section)tm.getValueAt(row, 0);                
                setProperties(s);               
            }
         });
         
         loadSectionList();        
    }
    
    private void sectionIsChanged(SectionChangedStatus sectionChangedStatus) {
        this.loadSectionList();
        if(changeListener != null) changeListener.sectionChanged(sectionChangedStatus);        
    }    

    private void setProperties(Section s)
    {
        this.tbxSectionName.setText(s.getName());
        this.tbxSectionDescription.setText(s.getDescription());
        mapHelper.showSection(s);
        mapHelper.setCenter(s.getRNodes().get(0));
        this.selectedSection = s;
    }
    
    private void deleteSection() {                        
        if(this.selectedSection == null) return;
        int res = JOptionPane.showConfirmDialog(rootPane, "Delete this section?", "Confirm", JOptionPane.YES_NO_OPTION);
        if(res == JOptionPane.YES_OPTION) {
            DefaultTableModel tm = (DefaultTableModel)tbSectionList.getModel();                
            File cache = new File(InfraConstants.SECTION_DIR + File.separator + Section.getCacheFileName(selectedSection.getName()));
            cache.delete();
            this.selectedSection = null;
            this.tbxSectionName.setText("");
            this.tbxSectionDescription.setText("");
            sectionIsChanged(SectionChangedStatus.DELETED);
            mapHelper.clear();
        }
    }    
    
    private void loadSectionList()
    {
        DefaultTableModel tm = (DefaultTableModel)tbSectionList.getModel();
        tm.getDataVector().removeAllElements();
        tm.fireTableDataChanged();
        SectionManager sm = tmo.getSectionManager();
        Infra infra = tmo.getInfra();
        sm.loadSections();
        Vector<Section> sections = sm.getSections();
                
        for (Section s : sections)  
        {             
            for(String nid : s.getStationIds())
            {
                RNode node = infra.find(nid);
                if(node != null) s.addRNode(node);
            }                 
            if(s != null) {
               tm.addRow(new Object[]{s, s.getDescription(), "Delete"});
            }
       }           
    }
    
    public void setChangeListener(ISectionChanged changeListener) {
        this.changeListener = changeListener;
    }        

    private void printSection(JTextArea tbxTarget, Section s)
    {
        StringBuilder sb = new StringBuilder();
        tbxTarget.setText(s.getRoutes());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabMainTab = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbSectionList = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbxSectionDescription = new javax.swing.JTextArea();
        tbxSectionName = new javax.swing.JTextField();
        btnDeleteSelection = new javax.swing.JButton();
        jxMap = new org.jdesktop.swingx.JXMapKit();
        panCreateSection = new javax.swing.JPanel();

        tabMainTab.setFont(new java.awt.Font("Verdana", 0, 12));
        tabMainTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabMainTabStateChanged(evt);
            }
        });

        tbSectionList.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbSectionList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Name"
            }
        ));
        jScrollPane3.setViewportView(tbSectionList);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 10))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel3.setText("name :");

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jLabel4.setText("description :");

        tbxSectionDescription.setColumns(20);
        tbxSectionDescription.setEditable(false);
        tbxSectionDescription.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxSectionDescription.setLineWrap(true);
        tbxSectionDescription.setRows(3);
        tbxSectionDescription.setAutoscrolls(false);
        jScrollPane4.setViewportView(tbxSectionDescription);

        tbxSectionName.setEditable(false);
        tbxSectionName.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tbxSectionName.setMaximumSize(new java.awt.Dimension(50, 20));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tbxSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tbxSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnDeleteSelection.setText("Delete");
        btnDeleteSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectionActionPerformed(evt);
            }
        });

        jxMap.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDeleteSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jxMap, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jxMap, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDeleteSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabMainTab.addTab("Route Lists", jPanel1);

        panCreateSection.setLayout(new java.awt.BorderLayout());
        tabMainTab.addTab("Create Route", panCreateSection);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMainTab, javax.swing.GroupLayout.DEFAULT_SIZE, 949, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMainTab, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectionActionPerformed
        this.deleteSection();
    }//GEN-LAST:event_btnDeleteSelectionActionPerformed

    private void tabMainTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabMainTabStateChanged
        //this.loadSectionList();
    }//GEN-LAST:event_tabMainTabStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeleteSelection;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private org.jdesktop.swingx.JXMapKit jxMap;
    private javax.swing.JPanel panCreateSection;
    private javax.swing.JTabbedPane tabMainTab;
    private javax.swing.JTable tbSectionList;
    private javax.swing.JTextArea tbxSectionDescription;
    private javax.swing.JTextField tbxSectionName;
    // End of variables declaration//GEN-END:variables

    public void setPane(JPanel parent) {
        this.rootPane = parent;
    }

}
