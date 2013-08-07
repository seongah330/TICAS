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
package edu.umn.natsrl.weatherRWIS.datas;

import edu.umn.natsrl.infra.Period;
import edu.umn.natsrl.weatherRWIS.ErrorType;
import edu.umn.natsrl.weatherRWIS.RWISError;
import edu.umn.natsrl.weatherRWIS.WeatherDB;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class WeatherData implements Serializable{
        protected String TableName;
        protected int Siteid;
        private boolean isloaded = false;
        private ArrayList<HashMap> DataRows = new ArrayList<HashMap>();
        RWISError rwiserror = new RWISError(ErrorType.NONE);
        /**
         * @param tname TableName
         */
        public WeatherData(String tname,int SiteId){
                TableName = tname;
                Siteid = SiteId;
        }
        
        
        /**
         * load Data
         * @param p Time Period
         * @param isnew get New Data
         * @return
         * @throws SQLException 
         */
        protected ArrayList<HashMap> loadData(Period p, boolean isnew) throws SQLException{
                if(!isloaded || isnew){
                        rwiserror = loadDBData(p);
                }
                
                return DataRows;
        }
        private RWISError loadDBData(Period p) throws SQLException{
                clearData();
                WeatherDB wdb = new WeatherDB();
                System.out.println(TableName + " - "+getTimetoSQL(p.startDate)+"   "+getTimetoSQL(p.endDate));
                ResultSet res = wdb.Select("select * from "+TableName+" where siteid = "+Siteid
                        +" and dttm >= '"+getTimetoSQL(p.startDate)
                        +"' and dttm <= '"+getTimetoSQL(p.endDate)+"'");
                //debug
                System.err.println("select * from "+TableName+" where siteid = "+Siteid
                        +" and dttm >= '"+getTimetoSQL(p.startDate)
                        +"' and dttm <= '"+getTimetoSQL(p.endDate)+"'");
                
                if(p.startDate.getTime() - p.endDate.getTime() > 0)
                        return new RWISError(ErrorType.PERIOD_REWIND);
                if(!res.next()){
                        System.err.println("load error");
                        RWISError er = new RWISError(ErrorType.DATABASE_LOAD,TableName);
                        er.addError(ErrorType.DATABASE_NOT_DATE);
                        return er;
                }
                
                
                ResultSetMetaData rsmd = res.getMetaData();
                int colCnt = rsmd.getColumnCount();
                
                Calendar sc = Calendar.getInstance();
                Calendar ec = Calendar.getInstance();
                sc.setTime(p.startDate);
                ec.setTime(p.endDate);
                int c_cur = 0;
                
                /** test time */
                long start = System.currentTimeMillis();
                while(true){
                        /**
                         * add Time with interval
                         */
                        sc.add(Calendar.SECOND, p.interval);
                        long deff = ec.getTimeInMillis() - sc.getTimeInMillis();
                        
                        c_cur = getMatchCursor(res, sc, c_cur);
                        
                        res.absolute(c_cur);
                        HashMap<String, Object> map = new HashMap();
                        map.put("period", sc.getTime());
                        for(int i=1;i<=colCnt;i++){
                                map.put(rsmd.getColumnName(i), res.getObject(rsmd.getColumnName(i)));
                        }
                        DataRows.add(map);
                         
                        /** break if sc == ec */
                        if(deff <= 0)
                                break;
                }
                long end = System.currentTimeMillis();
                System.out.println(TableName + " - Data Load and Adjust Time : "+(end - start)/1000.0);
                
                isloaded = true;
                return new RWISError(ErrorType.NONE);
        }
        
        private String getTimetoSQL(Date p){
                Calendar c = Calendar.getInstance();
                c.setTime(p);
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH)+1;
                int date = c.get(Calendar.DATE);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int min = c.get(Calendar.MINUTE);
                int sec = c.get(Calendar.SECOND);
                return String.format("%4d%02d%02d %02d:%02d:%02d", 
                year,month,date,hour,min,sec);
        }

        private int getMatchCursor(ResultSet res, Calendar sc, int scursor) throws SQLException {
                long prevalue = Long.MAX_VALUE;
                
                int Startbefore = 5;
                int startC = (scursor - Startbefore) < 0 ? 0 : scursor -Startbefore;
                res.absolute(startC);
                while(res.next()){
                        Date cd = (Date)res.getObject("Dttm");
                        long def = Math.abs(sc.getTimeInMillis() - cd.getTime());
                        if(def >= prevalue){
                                res.previous();
                                return res.getRow();
                        }else{
                                prevalue = def;
                        }
                }
                
                /** if last */
                int lastcnt = 0;
                if(res.last())
                        lastcnt = res.getRow();
                return lastcnt;
        }
        
        /**
         * to Double Array
         * @param data
         * @return 
         */
        protected Double[] ShorttoDoubleArray(ArrayList<Short> data)
        {        
                if(data == null || data.isEmpty()) return null;        
                Double[] ret = new Double[data.size()];
                for(int i=0; i<data.size(); i++) {
                        ret[i] = data.get(i).doubleValue();
                }
                return ret;
        }
        
        protected Double[] InttoDoubleArray(ArrayList<Integer> data)
        {        
                if(data == null || data.isEmpty()) return null;        
                Double[] ret = new Double[data.size()];
                for(int i=0; i<data.size(); i++) {
                        ret[i] = data.get(i).doubleValue();
                }
                return ret;
        }

        protected void clearData() {
                DataRows.clear();
                isloaded = false;
        }
        
        public boolean isLoaded(){
                return isloaded;
        }
        
        public RWISError getError(){
                return rwiserror;
        }
        
        public int getRowSize(){
                return DataRows.size();
        }
}
