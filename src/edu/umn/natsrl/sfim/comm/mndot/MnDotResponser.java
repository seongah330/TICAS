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

package edu.umn.natsrl.sfim.comm.mndot;

import edu.umn.natsrl.sfim.SFIMConfig;
import edu.umn.natsrl.sfim.comm.Responser;
import java.io.OutputStream;
import java.util.HashMap;

/**
 *
 * @author Chongmyung Park
 */
public abstract class MnDotResponser  extends Responser {
    
    protected static HashMap<String, MnDotResponser> ResponserList = new HashMap<String, MnDotResponser>();
    
    protected boolean printPacket = SFIMConfig.PRINT_PACKET;
    protected int drop;
    protected int cat;
    protected int addr;
    
    public void set(int drop, int cat, int addr, byte[] buffer, OutputStream os)
    {
        this.drop = drop;
        this.cat = cat;
        this.addr = addr;
        super.set(buffer, os);
    }    
    
    @Override
    public void doResponse() {
        //System.out.println(this.name + " (drop="+drop+", cat="+cat+", addr="+addr+")");
        response();        
    }
    
    abstract protected void response();
}
