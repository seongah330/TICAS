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

package edu.umn.natsrl.sfim.comm;

import edu.umn.natsrl.sfim.SFIMManager;
import edu.umn.natsrl.sfim.comm.mndot.MnDotHelper;
import java.io.OutputStream;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public abstract class Responser {
    
    protected final int DIGIT_MASK = 0x0F;
    protected SFIMManager manager = SFIMManager.getInstance();
    protected MnDotHelper helper = new MnDotHelper();
    protected byte[] dataBuffer;
    protected OutputStream os;
    protected Controller ctrl;
    protected ResponserType type;
    
    public Responser() {
    }
        
    public Responser(byte[] buffer, OutputStream os) {
        set(buffer, os);
    }
    
    public void set(byte[] buffer, OutputStream os) {
        this.dataBuffer = buffer;
        this.os = os;        
    }
    
    abstract public void doResponse();

    public void setController(Controller ctrl) {
        this.ctrl = ctrl;
    }

    public ResponserType getResponseType() {
        return type;
    }   

}
