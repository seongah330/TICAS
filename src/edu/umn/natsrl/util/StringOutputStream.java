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

package edu.umn.natsrl.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
/**
 * String Output Stream class for output redirection
 */
public class StringOutputStream extends OutputStream {

    JTextArea tbxLog;
    StringBuilder sb = new StringBuilder();

    public StringOutputStream(JTextArea logText) {
        this.tbxLog = logText;
    }

    @Override
    public void write(int b) throws IOException {
        update(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        update(new String(b, off, len));
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private synchronized void update(final String text) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if(tbxLog != null) {
                    tbxLog.append(text);
//                    tbxLog.setCaretPosition(tbxLog.getDocument().getLength());                       
                }
                else sb.append(text);
            }
        });
    }
    
    public String getString() {
        if(tbxLog != null) return tbxLog.getText();
        return sb.toString();
    }

}
