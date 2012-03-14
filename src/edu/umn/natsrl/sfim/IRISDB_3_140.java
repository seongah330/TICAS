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

package edu.umn.natsrl.sfim;

import edu.umn.natsrl.sfim.comm.CommLink;
import edu.umn.natsrl.sfim.comm.CommProtocol;
import edu.umn.natsrl.sfim.comm.Controller;
import edu.umn.natsrl.sfim.comm.InfoCommLink;
import edu.umn.natsrl.sfim.comm.InfoController;
import edu.umn.natsrl.sfim.comm.InfoTimingPlan;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 * @author Chongmyung Park
 */
public class IRISDB_3_140 {

    Connection connection = null;
    Statement statement = null;

    /**
     * constructor
     */
    public IRISDB_3_140() {
        try {
            dbconnect();
//            String[][] plans = FileHelper.readCSV("timingplan.csv", 0);
//            for(int i=0; i<plans.length; i++) {
//                statement.addBatch("UPDATE iris.timing_plan SET start_min = "+plans[i][1]+", stop_min = "+plans[i][2]+" WHERE name = '" + plans[i][0] + "'");
//            }
            statement.executeBatch();
//        } catch (IOException ex) {
//            ex.printStackTrace();
        } catch (SQLException ex) {            
            SFIMExceptionHandler.handle(ex);
        }
    }

    /*
     * make connection with IRIS server
     * @throws SQLException
     */
    private void dbconnect() throws SQLException {

        try {
//            JOptionPane.showConfirmDialog(null, "Trying to connect db : " + SFIMConfig.IRIS_SERVER_ADDR, "Information", JOptionPane.DEFAULT_OPTION);
            // the postgresql driver string
            Class.forName("org.postgresql.Driver");

            // the postgresql url
            String url = "jdbc:postgresql://" + SFIMConfig.IRIS_SERVER_ADDR + "/" + SFIMConfig.IRIS_DB_NAME;

            // get the postgresql database connection
            connection = DriverManager.getConnection(url, SFIMConfig.IRIS_DB_USER, SFIMConfig.IRIS_DB_PASSWD);
            // now do whatever you want to do with the connection
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//            JOptionPane.showConfirmDialog(null, "Connected : " + SFIMConfig.IRIS_SERVER_ADDR, "Information", JOptionPane.DEFAULT_OPTION);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null, "Fail to connect IRIS database : " + SFIMConfig.IRIS_SERVER_ADDR, "Information", JOptionPane.DEFAULT_OPTION);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(null, "Fail to connect IRIS database : " + SFIMConfig.IRIS_SERVER_ADDR, "Information", JOptionPane.DEFAULT_OPTION);
        }
    }

    /**
     * Update comm_link information for active commlink
     *   - active_commlink = comm_link for detectors and meters in simulation
     * @param activeCommLinkInfo 
     */
    public void updateCommLinks(Vector<InfoCommLink> activeCommLinkInfo, SFIMManager manager, ArrayList<String> detectors, ArrayList<String> dsds, ArrayList<String> sgs) {
        try {
            InetAddress thisIp = InetAddress.getLocalHost();
            String ip = thisIp.getHostAddress();
            statement.execute("update iris.comm_link set uri = ''");
            statement.execute("update iris.controller set active = false");

            for (InfoCommLink ci : activeCommLinkInfo) {
//                statement.execute("update iris.comm_link set uri = '" + ip + ":" + ci.serverPort + "', timeout=1500 where name = '" + ci.name + "'");
                statement.execute("update iris.comm_link set uri = '" + ip + ":" + ci.serverPort + "' where name = '" + ci.name + "'");
                CommLink cl = manager.getCommLink(ci.name);
                Vector<InfoController> ctrls = loadControllers(ci.name);
                for (Controller c : cl.getControllers()) {
                    boolean isInCase = false;
//                    System.out.println("ctrl : "+c.getName());
                    for(String io : c.getIO()){
                        if(detectors.contains(io)){
                            isInCase = true;
                        }else if (dsds.contains(io)) {
                            isInCase = true;
                        } else if (sgs.contains(io) || sgs.contains(io + "_L")) {
                            isInCase = true;
                        }
                    }
                    if(isInCase){
//                        System.out.println("ctrl : "+c.getName() + "ok!");
//                            statement.execute("update iris.controller set active = true where comm_link = '" + cl.getLinkName() + "'");
                        statement.execute("update iris.controller set active = true where name = '" + c.getName() + "'");
                    }
                }
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void configIRISDB() {
        try {
            System.out.print("Configuring IRIS DB for simulation ... ");
            // detector auto fail
            // it's used in DetectorImpl of iris
            // if it's true, DetectorImpl tests malfunction of detector
            //    with volume and scan data (max value check)
            statement.execute("UPDATE iris.system_attribute SET value = 'false' WHERE name = 'detector_auto_fail_enable'");

            // update all force_fail value to false
            // it means detector is ok!!
            statement.execute("UPDATE iris.detector SET force_fail = false");

            // just to remove warning messages in standard error of IRIS
            ResultSet res = statement.executeQuery("SELECT * FROM iris.system_attribute WHERE name = 'dmsxml_op_timeout_secs'");
            res.last();
            if (res.getRow() == 0) {
                statement.execute("INSERT INTO iris.system_attribute VALUES('dmsxml_op_timeout_secs', '65')");
            }
            System.out.println(" (OK)");
        } catch (SQLException ex) {
            System.out.println(" (Fail)");
            ex.printStackTrace();
        }
    }

    /**
     * load comm_link information
     * @return
     */
    public Vector<InfoCommLink> loadCommLinks() {
        try {
            Vector<InfoCommLink> comms = new Vector<InfoCommLink>();

            // read protcol info
            HashMap<Integer, String> protocolNames = new HashMap<Integer, String>();

            // set ntcip_a protocol to dms_xml protocol
            //statement.execute("UPDATE iris.comm_link SET protocol = "+CommProtocol.DMS_LITE.getId()+" WHERE protocol = "+CommProtocol.NTCIP_A.getId());            
            //statement.execute("UPDATE iris.comm_link SET protocol = "+CommProtocol.MNDOT_4.getId()+" WHERE protocol = "+CommProtocol.MNDOT_5.getId());
            //statement.execute("UPDATE iris.comm_link SET protocol = "+CommProtocol.MNDOT_5.getId()+" WHERE protocol = "+CommProtocol.MNDOT_4.getId());

            ResultSet result = statement.executeQuery("SELECT * FROM iris.comm_protocol");
            while (result.next()) {
                protocolNames.put(result.getInt("id"), result.getString("description"));
            }

            result = statement.executeQuery("SELECT * FROM iris.comm_link WHERE protocol = " + CommProtocol.DMS_LITE.getId() + " or protocol = " + CommProtocol.MNDOT_4.getId() + " or protocol = " + CommProtocol.MNDOT_5.getId() + " ORDER BY name");
            while (result.next()) {
                String name = result.getString("name");
                String desc = result.getString("description");
                int protocol = result.getInt("protocol");
                String protocolName = protocolNames.get(protocol);
                int timeout = result.getInt("timeout");
                comms.add(new InfoCommLink(name, desc, protocol, protocolName, timeout));
            }

            return comms;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * load controllers in comm_link
     * @param commlinkName name of comm_link
     * @return
     */
    public Vector<InfoController> loadControllers(String commlinkName) {
        String prevCtrl = "";
        InfoController ci = null;
        Vector<InfoController> cilist = new Vector<InfoController>();
        try {
            // read protcol info
            HashMap<Integer, String> protocolNames = new HashMap<Integer, String>();
            ResultSet result = statement.executeQuery("SELECT * FROM iris.comm_protocol");
            while (result.next()) {
                protocolNames.put(result.getInt("id"), result.getString("description"));
            }

            result = statement.executeQuery("SELECT CL.name as commlink_name, CL.description as commlink_desc, CL.timeout as commlink_timeout, CL.protocol as commlink_protocol, CT.name as ctrl_name, CT.drop_id as ctrl_drop, CT.cabinet as ctrl_cabinet, CT.active as ctrl_active, IO.name as io_name, IO.pin as io_pin, DT.field_length as detector_fieldlength, GEO.easting, GEO.northing, LT.description as LaneType from iris.comm_link as CL LEFT JOIN iris.controller as CT ON CT.comm_link = CL.name LEFT JOIN iris._device_io as IO ON IO.controller = CT.name  LEFT JOIN iris._detector as DT ON IO.name = DT.name LEFT JOIN iris._ramp_meter as RM ON IO.name = RM.name LEFT JOIN iris.geo_loc as GEO ON GEO.name = DT.r_node OR GEO.name = RM.geo_loc LEFT JOIN iris.lane_type AS LT ON LT.id = DT.lane_type where CL.name = '" + commlinkName + "'");
            while (result.next()) {
                String ctrlName = result.getString("ctrl_name");
                if (ctrlName == null) {
                    continue;
                }
                if (ctrlName.equals(prevCtrl)) {
                    ci.IOs.add(result.getString("io_name"));
                    ci.PINs.add(result.getInt("io_pin"));
                    ci.LaneTypes.add(result.getString("lanetype"));
                    ci.Eastings.add(result.getInt("easting"));
                    ci.Northings.add(result.getInt("northing"));
                } else {
                    if (ci != null) {
                        cilist.add(ci);
                    }
                    ci = new InfoController();
                    ci.commlink = new InfoCommLink(result.getString("commlink_name"), result.getString("commlink_desc"), result.getInt("commlink_protocol"), protocolNames.get(result.getInt("commlink_protocol")), result.getInt("commlink_timeout"));
                    ci.name = ctrlName;
                    ci.drop = result.getInt("ctrl_drop");
                    ci.cabinet = result.getString("ctrl_cabinet");
                    ci.active = result.getString("ctrl_active");
                    ci.fieldlength = result.getFloat("detector_fieldlength");
                    ci.IOs.add(result.getString("io_name"));
                    ci.PINs.add(result.getInt("io_pin"));
                    ci.LaneTypes.add(result.getString("lanetype"));
                    ci.Eastings.add(result.getInt("easting"));
                    ci.Northings.add(result.getInt("northing"));
                }
                prevCtrl = ctrlName;
            }
            if (ci != null) {
                cilist.add(ci);
            }

            /* debug
            for(InfoController cii : cilist)
            {
            for(int i=0; i<cii.PINs.size(); i++)
            {
            System.out.printf("[%s] %s : %d\n", cii.name, cii.IOs.get(i), cii.PINs.get(i));
            }
            }
             */
            return cilist;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * get list of timing plans
     * @return
     */
    public Vector<InfoTimingPlan> getTimingPlans() {
        Vector<InfoTimingPlan> plans = new Vector<InfoTimingPlan>();
        try {
            ResultSet result = statement.executeQuery("SELECT * FROM iris.meter_algorithm");
            while (result.next()) {
                plans.add(new InfoTimingPlan(result.getInt("id"), result.getString("description")));
            }
            return plans;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * update timing plans in IRIS database
     * to activate meters to be used in simulation
     * @param meterNames
     * @param planId
     */
    public void updateTimingPlans(String[] meterNames, int planId, boolean useMetering, Calendar simTime, int simDuration) {
        int simStartHour = simTime.get(Calendar.HOUR_OF_DAY);
        int simStartMin = simTime.get(Calendar.MINUTE);
        int start_min = simStartHour * 60 + simStartMin;
        int stop_min = start_min + simDuration * 60;
        int simStopHour = stop_min / 60;
        int simStopMin = stop_min - (simStopHour*60);
        String starttime = (simStartHour < 10 ? "0"+simStartHour : simStartHour)+""+(simStartMin < 10 ? "0"+simStartMin : simStartMin);
        String endtime = (simStopHour < 10 ? "0"+simStopHour : simStopHour)+""+(simStopMin < 10 ? "0"+simStopMin : simStopMin);
        try {
            //3.140 update
            statement.execute("UPDATE iris.action_plan SET active = false");
            if (useMetering) {
                //update action_plan
                String ACTION_PLAN_NAME = "METER_"+starttime+"_"+endtime;
                System.out.println(ACTION_PLAN_NAME+"    "+starttime + "     " + endtime);
                ResultSet actionresult = statement.executeQuery("SELECT * from iris.action_plan WHERE name ='"+ACTION_PLAN_NAME+"'");
                
                int actioncnt = 0;
                while(actionresult.next()){
                    actioncnt ++;
                }
                System.out.println(actionresult.getRow() + " " + actioncnt);
                if(actioncnt == 0){
                    statement.execute("insert into iris.action_plan (name,description,active,sync_actions,default_phase,phase,sticky) values('"+ACTION_PLAN_NAME+"','Meters starting at "+starttime+"',false,false,'undeployed','undeployed',false)");
                }
                
                for (String name : meterNames) {
                    if (name == null) {
                        continue;
                    }

                    //3.140 updating
                    //update meter_action
//                    System.out.println("delete meter : "+name);
                    statement.execute("delete from iris.meter_action WHERE ramp_meter = '" + name + "'");
                    statement.execute("insert into iris.meter_action (name,action_plan,ramp_meter,phase) values('"+name+"_"+starttime+"_"+endtime+"','"+ACTION_PLAN_NAME+"','"+name+"','deployed')");
                    // Use timing plan that has max target value
                    ResultSet result = statement.executeQuery("SELECT * from iris.meter_action WHERE ramp_meter = '" + name + "'");
                    String id = null;                    
                    String ramp_name = null;
                    int target = -1;
                    int maxTarget = -1;
                    while(result.next()) {
//                        String planName = result.getString("name");
//                        target = result.getInt("target");
//                        if(target > maxTarget) {
//                            maxTarget = target;
//                            id = planName;
//                        }
                        id = result.getString("action_plan");
                        ramp_name = result.getString("ramp_meter");
                        //set action_plan by time!!!!
                        statement.addBatch("UPDATE iris.action_plan SET active = TRUE, phase='deployed' WHERE name = '" + id + "'");
                    }           
                    
                    if(id != null) {
                        //statement.addBatch("UPDATE iris.timing_plan SET start_min = "+start_min+", stop_min = "+stop_min+", plan_type = " + planId + ", active = TRUE WHERE name = '" + id + "'");
//                        System.out.println("meteral :" + planId);
                        statement.addBatch("UPDATE iris._ramp_meter SET algorithm = " + planId + " WHERE name = '" + ramp_name + "'");
                    } else {
                        System.out.println("  !! WARNNING : there's no timing plan for " + name);
                    }
                    
                }
                statement.executeBatch();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @deprecated 
     * @param detectorNames 
     */
    public void updateDetectorsState(String[] detectorNames) {
        try {
            statement.execute("UPDATE iris.detector SET force_fail = false");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @deprecated
     */
    private boolean createCommLink(String name, String description, String url, int protocol, int timeout) throws SQLException {
        try {
            statement.execute("delete from iris.comm_link where name = '" + name + "'");
            return statement.execute("insert into iris.comm_link (name, description, url, protocol, timeout) values ('" + name + "', '" + description + "', '" + url + "', " + protocol + ", " + timeout + ")");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * @deprecated
     */
    private boolean createController(String name, int drop, String commlinkName, String cabinet) throws SQLException {
        try {
            statement.execute("update iris._device_io set controller = null where controller = '" + name + "'");
            statement.execute("delete from iris.controller where name = '" + name + "' and comm_link = '" + commlinkName + "'");
            statement.execute("delete from iris.cabinet where name = '" + cabinet + "'");
            statement.execute("delete from iris.geo_loc where name = '" + cabinet + "'");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        try {
            statement.execute("insert into iris.geo_loc (name) values ('" + cabinet + "')");
            statement.execute("insert into iris.cabinet (name, geo_loc) values ('" + cabinet + "', '" + cabinet + "')");
            return statement.execute("insert into iris.controller (name, drop_id, comm_link, cabinet, active, notes) values ('" + name + "', '" + drop + "', '" + commlinkName + "', '" + cabinet + "', TRUE, '')");
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * @deprecated
     */
    private boolean updateDevice(String name, String controllerName, int pin) throws SQLException {
        return statement.execute("update iris._device_io set controller= '" + controllerName + "', pin= '" + pin + "' where name = '" + name + "'");
    }
    
    /**
     * Returns easting and northing of given meter
     * @param name meter name (e.g. M35WN27)
     * @return int[]{northing, easting}, easting and northing array 
     * @throws SQLException 
     */
    public int[] loadMeterLocation(String name) {
        try {
            String query = "SELECT G.easting as easting, G.northing as northing FROM iris.geo_loc AS G LEFT JOIN iris._ramp_meter AS R ON R.geo_loc = G.name WHERE R.name='"+name+"'";        
            ResultSet result = statement.executeQuery(query);
            result.next();
//            System.out.println(name + " : easting="+result.getInt("easting")+ ", northing="+result.getInt("northing"));
            return new int[]{result.getInt("easting"), result.getInt("northing")};
        } catch (SQLException ex) {
            return null;
        }
    }    
}
