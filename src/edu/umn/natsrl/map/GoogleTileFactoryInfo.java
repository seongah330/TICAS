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

import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

public class GoogleTileFactoryInfo extends TileFactoryInfo {

    boolean sateliteView;

    public GoogleTileFactoryInfo(int minimumZoomLevel, int maximumZoomLevel, int totalMapZoom,
            int tileSize, boolean xr2l, boolean yt2b,
            boolean sateliteView) {
        super(minimumZoomLevel, maximumZoomLevel, totalMapZoom, tileSize, xr2l, yt2b, null, null, null, null);
        this.sateliteView = sateliteView;
    }

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        if (!sateliteView) {
            return getMapUrl(x, y, zoom);
        } else {
            return getSatURL(x, y, zoom);
        }
    }

    protected String getMapUrl(int x, int y, int zoom) {
        String url = "http://mt.google.com/mt?w=2.43"
                + "&x=" + x
                + "&y=" + y
                + "&zoom=" + zoom;
        return url;
    }

    /**
     * Not functional.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public String getSatURL(int x, int y, int zoom) {
        int ya = 1 << (17 - zoom);
        if ((y < 0) || (ya - 1 < y)) {
            return "http://www.google.com/mapfiles/transparent.gif";
        }
        if ((x < 0) || (ya - 1 < x)) {
            x = x % ya;
            if (x < 0) {
                x += ya;
            }
        }

        StringBuffer str = new StringBuffer();
        str.append('t');
        for (int i = 16; i >= zoom; i--) {
            ya = ya / 2;
            if (y < ya) {
                if (x < ya) {
                    str.append('q');
                } else {
                    str.append('r');
                    x -= ya;
                }
            } else {
                if (x < ya) {
                    str.append('t');
                    y -= ya;
                } else {
                    str.append('s');
                    x -= ya;
                    y -= ya;
                }
            }
        }
        return "http://kh.google.com/kh?v=1&t=" + str;
    }
}