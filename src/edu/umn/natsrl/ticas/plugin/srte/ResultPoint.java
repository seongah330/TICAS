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
package edu.umn.natsrl.ticas.plugin.srte;

import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class ResultPoint {
    public int srst=-1; // speed decline point for 5 min data
    public int lst=0; // low speed point for 5 min data
    public int rst=0; // speed recovery point for 5 min data
    public ArrayList<Integer> srt = new ArrayList<Integer>(); // stable speed point for 5 min data
    
    ResultPoint(){;}
    
    public boolean isEmpty(){
//        System.out.println(toString());
        if(srst <=0 ||
                lst <=0 ||
                rst <=0)
            return true;
        
        if(srst > lst || lst > rst || srst > rst)
            return true;
        
        return false;
    }
    
    public void setSRST(int _srst){
        srst = _srst;
    }
    public void setLST(int _lst){
        lst = _lst;
    }
    public void setRST(int _rst){
        int gap = lst - _rst;
        if(gap > 0 && gap < 2)
            rst = lst;
        else
            rst = _rst;
    }
    
    @Override
    public ResultPoint clone(){
        ResultPoint temp = new ResultPoint();
        temp.srst = srst;
        temp.lst = lst;
        temp.rst = rst;
        
        return temp;
    }
    public String toString(){
        return "srst : "+srst + " lst : "+lst + " rst : "+rst;
    }

    ResultPoint getMovingPoint(ResultPoint current) {
        ResultPoint temp = new ResultPoint();
        temp.srst = current.srst;
        temp.lst = lst;
        temp.rst = rst;
        
        return temp;
    }

    int getSRST() {
        return this.srst;
    }
    int getLST(){
        return this.lst;
    }
    int getRST(){
        return this.rst;
    }
}
