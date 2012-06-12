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
package edu.umn.natsrl.ticas.plugin.srte;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Subok Kim (derekkim29@gmail.com)
 */
public class SRTEResult {
    public String name;
    public int srst; // speed decline point for 5 min data
    public int lst; // low speed point for 5 min data
    public int rst; // speed recovery point for 5 min data
    public ArrayList<Integer> srt = new ArrayList<Integer>(); // stable speed point for 5 min data
    
    /**
     * new Algorithm
     */
    public PatternType pType;
    public int RCR;
    public int TPR;

    public double[] data_origin;   // original 5 min speed data
    public double[] data_smoothed;   // smoothed 5 min speed data
    public double[] data_quant;   // 5 min speed data after quantization

    public double[] k_smoothed;
    public double[] k_origin;
    public double[] k_quant;
    
    public int[] phases;
    public List<String> msgs = new ArrayList<String>();
    
    public SRTEResult() { }

    public void setResult(int sdp, int lsp, int srp) {
        this.srst = sdp;
        this.lst = lsp;
        this.rst = srp;
    }

    public void addSRT(int srt)
    {
        this.srt.add(srt);
    }

    void addLog(String msg) {
        System.out.println(msg);
        this.msgs.add(msg);
    }

}
