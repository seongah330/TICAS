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
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author Chongmyung Park
 */
public class SectionEditorPanel_OLD extends javax.swing.JPanel {

    private final int COL_NAME = 0;
    private final int COL_ACTION = 1;

    private TMO tmo = TMO.getInstance();
    private Vector<Corridor> corridors = new Vector<Corridor>();
    private Vector<RNode> section = new Vector<RNode>();
    private int startRowIndex = 0;
    private RNode startNode = null, endNode = null;
    private Vector<RNode> exitNodes = new Vector<RNode>();
    private Vector<RNode> entranceNodes = new Vector<RNode>();
    private Section selectedSection;    
    private JComponent rootPane = null;
    private Frame rootFrame = null;
    private final Infra infra;
    
    
    
    /** Creates new form SectionEditorPanel */
    public SectionEditorPanel_OLD() {
        
        initComponents();
        this.tabMainTab.setSelectedIndex(1);
        this.tbStations.getColumnModel().getColumn(1).setPreferredWidth(60);

        if(!tmo.isLoaded()) tmo.setup();
        infra = tmo.getInfra();
        corridors = infra.getCorridors();
        this.setCorridors();
        this.setStations((Corridor)this.cbxCorridors.getSelectedItem());
        
        this.cbxCorridors.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {
                JComboBox cb = (JComboBox)evt.getSource();
                Corridor c = (Corridor)evt.getItem();
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    setStations(c);
                } 
            }
         });

         this.tbStations.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                int row = tbStations.rowAtPoint(e.getPoint());
                int col = tbStations.columnAtPoint(e.getPoint());
                if(endNode != null) return;
                DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
                if(col == COL_ACTION) {
                    RNode rnode = (RNode)tm.getValueAt(row, COL_NAME);

                    // set section start node
                    if(startNode == null && rnode.isStation())
                    {
                        setStartPoint(row);                       
                    }

                    // set section end node
                    else if(startNode != null && rnode.isStation() && row > startRowIndex)
                    {
                        setEndPoint(row);
                    }
                    
                    // move to other corridor
                    else if(rnode.isExit() && row > startRowIndex)
                    {
                        if(startNode == null) {
                            JOptionPane.showMessageDialog(rootPane, "Set section start point at first");
                            return;
                        }
                        goNextCorridor(row);
                    }
                }
             }
         });

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

    private void setProperties(Section s)
    {
        this.tbxSectionName.setText(s.getName());
        this.tbxSectionDescription.setText(s.getDescription());
        printSection(this.tbxSectionRNodes, s.getRNodes());        
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
            this.tbxSectionRNodes.setText("");
            loadSectionList();
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
    
    private void saveSection()
    {
        SectionSaveDialog ssd = new SectionSaveDialog(rootFrame, this.section);
        ssd.setLocationRelativeTo(this);                
        ssd.setVisible(true);
        if(ssd.isSaved) {
            this.reset();
            this.loadSectionList();
        }
    }


    private void setStartPoint(int row)
    {
        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
        startNode = (RNode)tm.getValueAt(row, COL_NAME);
        this.cbxCorridors.setEnabled(false);
        this.setLabelToTable();
    }

    private void setEndPoint(int row)
    {
        if(!checkDistance(row)) return;

        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
        tbStations.setEnabled(false);
        this.endNode = (RNode)tm.getValueAt(row, COL_NAME);
        this.btnSave.setEnabled(true);
        this.btnBack.setEnabled(false);
        this.addStationToSection(row);
        printSection(this.tbxRNodesListForCreate, this.section);
        this.setLabelToTable();
    }

    private void goNextCorridor(int row)
    {
        if(!checkDistance(row)) return;
        
        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
        RNode ext = (RNode)tm.getValueAt(row, COL_NAME);
        RNode ent = ext.getDownStreamNodeToOtherCorridor();

        this.entranceNodes.add(ent);
        this.exitNodes.add(ext);

        Corridor c = getCorridorById(ent.getCorridor().getId());
        this.addStationToSection(row);
        this.setStations(c);
        printSection(this.tbxRNodesListForCreate, this.section);
        this.btnBack.setEnabled(true);
    }

    private void addStationToSection(int row)
    {
        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
        RNode rnode = (RNode)tm.getValueAt(row, COL_NAME);
       
        int r = 0;
        int startPoint = 0;

        // find section start node
        for(int i=0; i<tm.getRowCount(); i++)
        {
            RNode rn = (RNode)tm.getValueAt(i, COL_NAME);
            if(rn == startNode) {
                startPoint = r;
            }
            r++;
        }
      
        // add stations to section
        for(int i=startPoint; i<=row; i++)
        {
            RNode rn = (RNode)tbStations.getModel().getValueAt(i, COL_NAME);
            if(rn.isStation()) {
                section.add(rn);
            }
        }

        // add exit and entrance to section
        if(rnode.isExit()) {
            section.add(rnode);
            section.add(rnode.getDownStreamNodeToOtherCorridor());
        }
    }

    private void printSection(JTextArea tbxTarget, List<RNode> rnodes)
    {
        StringBuilder sb = new StringBuilder();
        tbxTarget.setText("");

        for(int i=0; i<rnodes.size(); i++)
        {
            RNode rnode = rnodes.get(i);
            if(rnode.isStation()) sb.append(rnode.getStationId());
            else if(rnode.isExit()) {
                sb.append(" [" + rnode.getCorridor().getId()+" ");
            }
            else if(rnode.isEntrance()) {
                Corridor c = getCorridor(rnode.getLabel());
                sb.append(rnode.getCorridor().getId()+"] ");
            }
            if(i < rnodes.size()-1) sb.append(" -> ");
        }
        tbxTarget.setText(sb.toString());
    }

    private void setCorridors()
    {
        for(Corridor c : corridors)
        {
            this.cbxCorridors.addItem(c);
        }
    }

    private void setStations(Corridor corridor)
    {
        DefaultTableModel tm = (DefaultTableModel)this.tbStations.getModel();
        tm.getDataVector().removeAllElements();
        tm.fireTableDataChanged();
        
        this.lbDisplayedCorridor.setText(corridor.getId());
        boolean foundEntrance = false;

        // loop for all rnodes
        for(RNode rn : corridor.getRnodes())
        {
            // if this corridor is from other corridor,
            // hide stations before entrance
            if(entranceNodes.size()>0 && rn == entranceNodes.lastElement())
            {
                foundEntrance = true;
            }

            // if there is entrance that is from other corridor
            if(foundEntrance || entranceNodes.isEmpty()) {
                if(rn.isAvailableStation()) tm.addRow(new Object[]{rn});
                if(rn.isExit())
                {
                    if(rn.getDownStreamNodeToOtherCorridor() == null) continue;
                    Corridor c = rn.getDownStreamNodeToOtherCorridor().getCorridor();
                    if(c.getId().contains(rn.getLabel())) tm.addRow(new Object[]{rn});
                }
            }
        }

        this.setLabelToTable();
    }

    private void setLabelToTable()
    {
        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();

        // find startRowIndex;
        boolean foundStartNode = false;
        boolean foundEndNode = false;
        int endRowIndex = tm.getRowCount();
        
        this.startRowIndex = 0;

        // loop for all rnodes
        for(int i=0; i<tm.getRowCount(); i++)
        {
            RNode rn = (RNode)tm.getValueAt(i, COL_NAME);

            // found section start node
            if(rn != null && rn == startNode) {
                this.startRowIndex = i;
                foundStartNode = true;
            }
            // found section end node
            if(rn != null && rn == endNode) {
                endRowIndex = i;
                foundEndNode = true;
            }
        }
        
        int startPoint = 0;
        if(foundStartNode) {
            tm.setValueAt("Section Start", startRowIndex, COL_ACTION);
            startPoint = this.startRowIndex + 1;
        }
        if(foundEndNode) {
            tm.setValueAt("Section End", endRowIndex, COL_ACTION);
        }
        
        for(int i=0; i<startRowIndex; i++)
        {
            tm.setValueAt("-", i, COL_ACTION);
        }

        for(int i=startPoint; i<endRowIndex; i++)
        {
            RNode rnode = (RNode)tm.getValueAt(i, COL_NAME);
            if( startNode == null && endNode == null ) tm.setValueAt("SET start point", i, COL_ACTION);
            else if(!foundEndNode) tm.setValueAt("SET end point", i, COL_ACTION);
            else if(rnode.isStation()) tm.setValueAt("in section", i, COL_ACTION);

            if(rnode.isExit()) {
                Corridor c = rnode.getDownStreamNodeToOtherCorridor().getCorridor();
                tm.setValueAt("GO to "+c.getId(), i, COL_ACTION);
            }
        }
        for(int i=endRowIndex+1; i<tm.getRowCount(); i++)
        {
            tm.setValueAt("-", i, COL_ACTION);   
        }
        
        
        if(entranceNodes.isEmpty()) this.btnBack.setEnabled(false);
    }

    private Corridor getCorridor(String name)
    {
        for(Corridor c : corridors)
        {
            if(name.equals(c.getRoute())) return c;
        }
        return null;
    }

    private Corridor getCorridorById(String id)
    {
        for(Corridor c : corridors)
        {
            if(id.equals(c.getId())) return c;
        }
        return null;
    }

    private void reset()
    {
        this.cbxCorridors.setEnabled(true);
        this.section.clear();
        this.startNode = null;
        this.endNode = null;
        this.startRowIndex = 0;
        this.tbxRNodesListForCreate.setText("");
        this.entranceNodes.clear();
        this.exitNodes.clear();
        this.btnSave.setEnabled(false);
        this.btnBack.setEnabled(false);
        this.setStations((Corridor)this.cbxCorridors.getSelectedItem());
    }

    private void back() {
        Corridor c = this.exitNodes.lastElement().getCorridor();
        int size = this.exitNodes.size();
        
        this.section.remove(this.section.lastElement());
        this.section.remove(this.section.lastElement());
        
        for(int i=this.section.size()-1; i>=0; i--)
        {
            RNode node = this.section.get(i);
            if(node.isEntrance()) break;
            this.section.remove(node);
        }
        
        if(size > 0) {
            this.exitNodes.removeElementAt(size-1);
            this.entranceNodes.removeElementAt(size-1);
        }
        this.setStations(c);
        this.printSection(this.tbxRNodesListForCreate, this.section);
    }

    private boolean checkDistance(int clickedIndex)
    {
        DefaultTableModel tm = (DefaultTableModel)tbStations.getModel();
        RNode prevNode = (RNode)tm.getValueAt(0, COL_NAME);
        if(this.entranceNodes.isEmpty()) prevNode = this.startNode;
        
        for(int i=this.startRowIndex; i<=clickedIndex; i++)
        {
            RNode node = (RNode)tm.getValueAt(i, COL_NAME);
            float distance = getDistance(prevNode.getEasting(), node.getEasting(), prevNode.getNorthing(), node.getNorthing());
            if(distance > InfraConstants.SEPARATED_CORRIDOR_DISTANCE_THRESHOLD)
            {
                System.out.println("It may be broken section");
                StringBuilder sb = new StringBuilder();
                sb.append("Distance between ");
                if(prevNode.isStation()) sb.append(prevNode.getStationId());
                else sb.append("( Exit to "+prevNode.getLabel()+" )");
                sb.append(" and ");
                if(node.isStation()) sb.append(node.getStationId());
                else sb.append("( Exit to "+node.getLabel()+" )");
                sb.append(" is "+ Math.round(distance)+"mile");
                sb.append("\nIt may be separated corridor such as I-35 to I-35W and I-35E");
                sb.append("\nDo you want to set section as current selection?");
                int res = JOptionPane.showConfirmDialog(rootPane, sb.toString(), "Confirm", JOptionPane.YES_NO_OPTION);
                if(res == 0) return true;
                else return false;
                
            }
            prevNode = node;
        }
        return true;
    }

    private float getDistance(float e1, float e2, float n1, float n2)
    {
        return (float)Math.sqrt((e1 - e2) * (e1 - e2) + (n1 - n2) * (n1 - n2)) / 1609;
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
        btnReloadSectionList = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbxSectionDescription = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tbxSectionRNodes = new javax.swing.JTextArea();
        tbxSectionName = new javax.swing.JTextField();
        btnDeleteSelection = new javax.swing.JButton();
        panCreateSection = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cbxCorridors = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        lbDisplayedCorridor = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbStations = new javax.swing.JTable();
        btnReset = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbxRNodesListForCreate = new javax.swing.JTextArea();
        btnSave = new javax.swing.JButton();

        tabMainTab.setFont(new java.awt.Font("Verdana", 0, 12));

        tbSectionList.setFont(new java.awt.Font("Verdana", 0, 10));
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
        tbSectionList.setEnabled(false);
        jScrollPane3.setViewportView(tbSectionList);

        btnReloadSectionList.setFont(new java.awt.Font("Verdana", 0, 12));
        btnReloadSectionList.setText("Reload");
        btnReloadSectionList.setPreferredSize(new java.awt.Dimension(71, 25));
        btnReloadSectionList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadSectionListActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Properties", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 10))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel3.setText("name :");

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel4.setText("description :");

        tbxSectionDescription.setColumns(20);
        tbxSectionDescription.setEditable(false);
        tbxSectionDescription.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxSectionDescription.setLineWrap(true);
        tbxSectionDescription.setRows(5);
        tbxSectionDescription.setAutoscrolls(false);
        jScrollPane4.setViewportView(tbxSectionDescription);

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel5.setText("routes :");

        tbxSectionRNodes.setColumns(20);
        tbxSectionRNodes.setEditable(false);
        tbxSectionRNodes.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxSectionRNodes.setLineWrap(true);
        tbxSectionRNodes.setRows(5);
        tbxSectionRNodes.setAutoscrolls(false);
        jScrollPane5.setViewportView(tbxSectionRNodes);

        tbxSectionName.setEditable(false);
        tbxSectionName.setFont(new java.awt.Font("Verdana", 0, 10));
        tbxSectionName.setMaximumSize(new java.awt.Dimension(50, 20));

        btnDeleteSelection.setText("Delete");
        btnDeleteSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDeleteSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tbxSectionName, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tbxSectionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(btnReloadSectionList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(btnReloadSectionList, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabMainTab.addTab("Route Lists", jPanel1);

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 10));
        jLabel2.setText("Select starting corridor :");

        cbxCorridors.setFont(new java.awt.Font("Verdana", 0, 10));
        cbxCorridors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxCorridorsActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 12));
        jLabel1.setText("Corridor : ");

        lbDisplayedCorridor.setFont(new java.awt.Font("Verdana", 0, 10));
        lbDisplayedCorridor.setText("corridor name");

        btnBack.setFont(new java.awt.Font("Verdana", 0, 10));
        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        tbStations.setFont(new java.awt.Font("Verdana", 0, 10));
        tbStations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Name", "Action"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
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
        tbStations.setFocusable(false);
        tbStations.setRowSelectionAllowed(false);
        tbStations.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(tbStations);

        btnReset.setFont(new java.awt.Font("Verdana", 0, 12));
        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        tbxRNodesListForCreate.setColumns(20);
        tbxRNodesListForCreate.setEditable(false);
        tbxRNodesListForCreate.setLineWrap(true);
        tbxRNodesListForCreate.setRows(5);
        jScrollPane2.setViewportView(tbxRNodesListForCreate);

        btnSave.setFont(new java.awt.Font("Verdana", 0, 12));
        btnSave.setText("Save Section");
        btnSave.setEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panCreateSectionLayout = new javax.swing.GroupLayout(panCreateSection);
        panCreateSection.setLayout(panCreateSectionLayout);
        panCreateSectionLayout.setHorizontalGroup(
            panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panCreateSectionLayout.createSequentialGroup()
                .addGroup(panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panCreateSectionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE))
                    .addGroup(panCreateSectionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(10, 10, 10)
                        .addComponent(cbxCorridors, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                    .addGroup(panCreateSectionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbDisplayedCorridor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 351, Short.MAX_VALUE)
                        .addComponent(btnBack))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panCreateSectionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 330, Short.MAX_VALUE)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panCreateSectionLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panCreateSectionLayout.setVerticalGroup(
            panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panCreateSectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxCorridors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lbDisplayedCorridor)
                    .addComponent(btnBack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(panCreateSectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabMainTab.addTab("Create Route", panCreateSection);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMainTab, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabMainTab, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnReloadSectionListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadSectionListActionPerformed
        this.loadSectionList();
}//GEN-LAST:event_btnReloadSectionListActionPerformed

    private void cbxCorridorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCorridorsActionPerformed
        
}//GEN-LAST:event_cbxCorridorsActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        this.back();
}//GEN-LAST:event_btnBackActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        this.reset();
}//GEN-LAST:event_btnResetActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        this.saveSection();
}//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectionActionPerformed
        this.deleteSection();
    }//GEN-LAST:event_btnDeleteSelectionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnDeleteSelection;
    private javax.swing.JButton btnReloadSectionList;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cbxCorridors;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbDisplayedCorridor;
    private javax.swing.JPanel panCreateSection;
    private javax.swing.JTabbedPane tabMainTab;
    private javax.swing.JTable tbSectionList;
    private javax.swing.JTable tbStations;
    private javax.swing.JTextArea tbxRNodesListForCreate;
    private javax.swing.JTextArea tbxSectionDescription;
    private javax.swing.JTextField tbxSectionName;
    private javax.swing.JTextArea tbxSectionRNodes;
    // End of variables declaration//GEN-END:variables

    public void setFrame(Frame parent) {
        this.rootFrame = parent;
    }

    public void setPane(JPanel parent) {
        this.rootPane = parent;
    }

}
