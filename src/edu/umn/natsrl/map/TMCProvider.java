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

import edu.umn.natsrl.infra.InfraConstants;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

/**
 *
 * @author Chongmyung Park
 */
public class TMCProvider {

    private static final int minZoom = 1;
    private static final int maxZoom = 7;
    private static final int mapZoom = 17;
    private static final int tileSize = 256;
    private static final boolean xr2l = true;
    private static final boolean yt2b = true;
    private static final String externalBaseURL = "http://data.dot.state.mn.us/osm/tiles";
    private static final String internalBaseURL = "http://tms-iris.dot.state.mn.us/osm/tiles";

    public static TileFactory getTileFactory() {

        //new File(InfraConstants.CACHE_MAP_DIR).mkdir();

        try {
            URL url = new URL(internalBaseURL);
            URLConnection conn = url.openConnection();
            conn.connect();
            return getInternalTileFactory();
        } catch (Exception e) {
            return getExternalTileFactory();
        }
    }

    public static TileFactory getInternalTileFactory() {
        return new DefaultTileFactory(new TMCTileFactoryInfo(internalBaseURL, minZoom, maxZoom, mapZoom, tileSize, xr2l, yt2b));
    }

    public static TileFactory getExternalTileFactory() {
        return new DefaultTileFactory(new TMCTileFactoryInfo(externalBaseURL, minZoom, maxZoom, mapZoom, tileSize, xr2l, yt2b));
    }

    public static class TMCTileFactoryInfo extends TileFactoryInfo {

        String baseURL = "";

        public TMCTileFactoryInfo(String baseURL, int minimumZoomLevel, int maximumZoomLevel, int totalMapZoom,
                int tileSize, boolean xr2l, boolean yt2b) {
            super(minimumZoomLevel, maximumZoomLevel, totalMapZoom, tileSize, xr2l, yt2b, null, null, null, null);
            this.baseURL = baseURL;
        }

        @Override
        public String getTileUrl(int x, int y, int zoom) {
            return getMapUrl(x, y, zoom);
        }

        protected String getMapUrl(int x, int y, int zoom) {
            int z = mapZoom - zoom;

            String url = baseURL
                    + "/" + z
                    + "/" + x
                    + "/" + y
                    + ".png";
            return url;
            //return cache(url, x, y, z);
        }

        private String cache(final String url, final int x, final int y, final int z) {
            try {
                final String cacheFile = InfraConstants.CACHE_MAP_DIR + File.separator + z + File.separator + x + File.separator + y + ".png";
                final String cacheFileDownload = cacheFile + ".download";

                // if there is cached file
                if (new File(cacheFile).exists()) {
                    return new File(cacheFile).toURI().toString();
                }
                
                // if downloading...
                if (new File(cacheFileDownload).exists()) {
                    return url;
                }
                
                // create path
                new File(InfraConstants.CACHE_MAP_DIR + File.separator + z).mkdir();
                new File(InfraConstants.CACHE_MAP_DIR + File.separator + z + File.separator + x).mkdir();
                
                // start downloading thread
                new CachingThread(url, cacheFile).start();
                
                // return original url
                return url;

            } catch (Exception ex) {
                ex.printStackTrace();
                return url;
            }
        }
    }

    static class CachingThread extends Thread {

        String url;
        String localFile;

        public CachingThread(String url, String localFile) {
            this.url = url;
            this.localFile = localFile;
        }

        @Override
        public void run() {
            
            // temporary downloading file
            final String cacheFileDownload = this.localFile + ".download";

            try {
                
                URL u = new URL(url);
                URLConnection uc = u.openConnection();
                int contentLength = uc.getContentLength();
                InputStream raw = uc.getInputStream();
                InputStream in = new BufferedInputStream(raw);
                byte[] data = new byte[contentLength];
                int bytesRead = 0;
                int offset = 0;
                while (offset < contentLength) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                }
                in.close();
                FileOutputStream out = new FileOutputStream(cacheFileDownload);
                out.write(data);
                out.flush();
                out.close();
                 


//                URL u = new URL(url);
//                URLConnection con = u.openConnection();                            
//                con.setConnectTimeout(3*60*1000);   // 3 minute
//                con.setReadTimeout(3*60*1000);
//                ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
//                FileOutputStream fos = new FileOutputStream(cacheFileDownload);
//                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
//                rbc.close();
//                fos.flush();
//                fos.close();
                 
                new File(cacheFileDownload).renameTo(new File(localFile));

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (java.net.ConnectException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                new File(cacheFileDownload).delete();
            }
        }
    }
}
