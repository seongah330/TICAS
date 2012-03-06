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

package edu.umn.natsrl.map;

import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class GoogleMapsTileProvider {

    private static final String VERSION = "2.75";
    private static final int minZoom = 1;
    private static final int maxZoom = 16;
    private static final int mapZoom = 17;
    private static final int tileSize = 256;
    private static final boolean xr2l = true;
    private static final boolean yt2b = true;
    private static final String baseURL = "http://mt1.google.com/mt?n=404&v=w" + VERSION;
    private static final String x = "x";
    private static final String y = "y";
    private static final String z = "zoom";
    private static final TileFactoryInfo GOOGLE_MAPS_TILE_INFO = new TileFactoryInfo(
            minZoom, maxZoom, mapZoom, tileSize, xr2l,
            yt2b, baseURL, x, y, z);

    public static TileFactory getDefaultTileFactory() {
        return (new DefaultTileFactory(
                GOOGLE_MAPS_TILE_INFO));
    }
}
