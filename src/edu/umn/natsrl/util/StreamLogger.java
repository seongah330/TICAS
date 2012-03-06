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

import edu.umn.natsrl.infra.InfraConstants;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class StreamLogger extends OutputStream {
    
    public static String SAVE_DIR = InfraConstants.CACHE_DIR + File.separator + "log";
    private FileWriter outputFile;

    public StreamLogger(String outputPath) throws IOException  {        
        outputPath = FileHelper.getNumberedFileName(outputPath);
        outputFile = new FileWriter(outputPath);        
    }    

    @Override
    public void write(int b) throws IOException {
        outputFile.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputFile.write(new String(b, off, len));
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputFile.write(new String(b));
    }
    
    public void close() throws IOException {
        this.outputFile.close();
    }
}
