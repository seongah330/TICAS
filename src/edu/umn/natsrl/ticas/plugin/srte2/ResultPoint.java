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
package edu.umn.natsrl.ticas.plugin.srte2;

import java.util.ArrayList;

/**
 *
 * @author Soobin Jeon <j.soobin@gmail.com>
 */
public class ResultPoint {
    public int srst=-1; // speed decline point for 5 min data
    public int rst=0; // speed recovery point for 5 min data
    public int TPR=0;
    public int NPR=0;
    public int csrt=Integer.MAX_VALUE; // SRT
    public int RCR=-1;
    public ArrayList<Integer> srt = new ArrayList<Integer>(); // stable speed point for 5 min data
    public ArrayList<Integer> srtc = new ArrayList<Integer>();
    public ArrayList<Integer> srtf = new ArrayList<Integer>();
    
    public int SRST_TT = 0;
    public int RST_TT = 0;
    public int NPR_TT = 0;
    ResultPoint(){;}
    
    public void setSRST(int _srst){
        srst = _srst;
    }
    
    @Override
    public ResultPoint clone(){
        ResultPoint temp = new ResultPoint();
        temp.srst = srst;
        temp.rst = rst;
        
        return temp;
    }
    public String toString(){
        return "srst : "+srst + " rst : "+rst;
    }

    int getSRST() {
        return this.srst;
    }
    int getRST(){
        return this.rst;
    }
}
