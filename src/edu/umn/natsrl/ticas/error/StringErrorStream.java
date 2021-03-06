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
package edu.umn.natsrl.ticas.error;

import edu.umn.natsrl.ticas.RunningDialog;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JOptionPane;

/**
 *
 * @author soobin Jeon
 */
public class StringErrorStream extends OutputStream {
    StringBuilder message = new StringBuilder();
    RunningDialog rd = null;
    public StringErrorStream(RunningDialog _rd){
        this.rd = _rd;
    }
    @Override
    public void write(int b) throws IOException {
        message.append((char) b);
    }
    public String getMessage(String mes,String desc){
        String Error = mes + "\n";
        if(message.toString().length() != 0){
            Error = Error + "Error Msg\n  - " + message.toString().split("\n")[0];
        }
        Error = Error + "\nDescription\n  - " + desc;
        if(rd != null){
            JOptionPane.showMessageDialog(rd, Error);
            rd.close(0.5);
        }
        return Error;
    }
}
