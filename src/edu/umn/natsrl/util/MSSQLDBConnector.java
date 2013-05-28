/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
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
package edu.umn.natsrl.util;

import java.sql.*;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class MSSQLDBConnector {
        protected String userID;
        protected String passwd;
        protected String dbName;
        protected String serverName;
        protected String serverPort;
        protected Connection connection;
        protected Statement statement;
        protected Statement statement_scroll;
        protected boolean isConnected = false;
        
        public MSSQLDBConnector(String uid, String _passwd, String _dbName, String _serverName, String _port){
                userID = uid;
                passwd = _passwd;
                dbName = _dbName;
                serverName = _serverName;
                serverPort = _port;
                dbconnect();
        }
        
        private void dbconnect(){
                try {
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        String connectionUrl = "jdbc:sqlserver://" + serverName + ":"+ serverPort + ";" +
                                        "databaseName=" + dbName + ";" +
                                        "user = " + userID + ";" +
                                        "password = " + passwd + ";";
                        connection = DriverManager.getConnection(connectionUrl);
                        statement = connection.createStatement();
                        statement_scroll = connection.createStatement(
                                ResultSet.TYPE_SCROLL_INSENSITIVE,
                                ResultSet.CONCUR_READ_ONLY
                                );
                        isConnected = true;
                } catch (Exception ex) {
                        ex.printStackTrace();
                        isConnected = false;
                }
        }
        
        public ResultSet Select(String sql) throws SQLException{
                return statement_scroll.executeQuery(sql);
        }
        /**
         * disconnect Database
         */
        public void disconnect(){
                try {
                        connection.close();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
        }
        
        public boolean isConnected(){
                return isConnected;
        }
}
