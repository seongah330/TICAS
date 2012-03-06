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

/**
 * Data structure for saving comm_link information
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class InfoCommLink {
    public String name;
    public String description;
    public int protocol;
    public int timeout;
    public int serverPort;
    public String protcolName;

    public InfoCommLink(String name, String description, int protocol, String protocolName, int timeout) {
        this.name = name;
        this.description = description;
        this.protocol = protocol;
        this.protcolName = protocolName;
        this.timeout = timeout;
    }
    public String toString()
    {
        return this.name + ", " + this.description + ", " + this.protcolName;
    }
}
