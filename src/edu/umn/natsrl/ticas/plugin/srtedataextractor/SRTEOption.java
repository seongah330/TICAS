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
package edu.umn.natsrl.ticas.plugin.srtedataextractor;

import edu.umn.natsrl.evaluation.EvaluationResult;
import edu.umn.natsrl.infra.Period;
import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class SRTEOption {
        int dstarthour = 0;
        int dstartmin = 0;
        int dendhour = 0;
        int dendmin = 0;
        Period period;
        boolean isdeleteTime = false;
        
        public SRTEOption(Period p){
                period = p;
        }
        
        public void setDeleteTime(int sh, int sm, int dh, int dm){
                dstarthour = sh;
                dstartmin = sm;
                dendhour = dh;
                dendmin = dm;
                System.out.println(dstarthour+", "+dstartmin+", "+dendhour+", "+dendmin);
        }
        
        public EvaluationResult deleteTime(EvaluationResult res){
                if(!isdeleteTime)
                        return res;
                int totalperiod = period.getIntervalPeriod();
                int DStartscd = (dstarthour) * 3600 + dstartmin * 60;
                int DEndscd = (dendhour) * 3600 + dendmin * 60;
                int startidx = DStartscd / period.interval;
                int endidx = DEndscd / period.interval;
                startidx += res.ROW_DATA_START()-1;
                endidx += res.ROW_DATA_START()-1;
//                System.out.println(totalperiod + ", "+startidx+", "+endidx + " , "+res.getRowSize(0));
                ArrayList<ArrayList> ret = new ArrayList<ArrayList>();
                for(int col = 0;col < res.getColumnSize(); col++){
                        ArrayList coldata = new ArrayList();
                        for(int i = 0 ; i < res.getRowSize(col);i++){
                                if(i < startidx || i > endidx)
                                        coldata.add(res.get(col, i));
                        }
                        ret.add(coldata);
//                        for(int i = startidx ; i < endidx ; i++){
//                                res.getColumn(col).remove(i);
//                        }
                }
                res.setData(ret);
                return res;
        }

        public void isDeleteTime(boolean b) {
                isdeleteTime = b;
        }
        
        public boolean isDeleteTime(){
                return isdeleteTime;
        }
}
