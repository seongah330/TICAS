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
package edu.umn.natsrl.ticas.plugin.rtchart;

import edu.umn.natsrl.ticas.plugin.rtchart.reader.StationNode;


/**
 *
 * @author Chongmyung Park
 */
public class QKGraph extends LiveXYGraph {

    public QKGraph() {
        super("K", "Q");
        this.yMax = 3000;
        this.yMin = 0;
    }

    @Override
    /**
     * Return K (Density)
     */
    protected float getXData(StationNode station) {
        return station.getFlow() / station.getSpeed();
    }

    @Override
    /**
     * Return Q (Flow)
     */
    protected float getYData(StationNode station) {
        return (float)station.getFlow();
    }

}
