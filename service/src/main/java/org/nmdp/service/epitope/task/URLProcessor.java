/*

    epitope-service  T-cell epitope group matching service for HLA-DPB1 locus.
    Copyright (c) 2014-2015 National Marrow Donor Program (NMDP)
    
    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.
    
    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.
    
    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
    
    > http://www.gnu.org/licenses/lgpl.html

*/

package org.nmdp.service.epitope.task;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLProcessor {

    long lastModified = 0;
    private URL[] urls;
    private boolean unzip;
    static Logger logger = LoggerFactory.getLogger(URLProcessor.class);
    
    public URLProcessor(URL[] urls, boolean unzip) {
        this.urls = urls;
        this.unzip = unzip;
    }
    
    public long process(Consumer<InputStream> consumer, long lastModified) {
        for (URL url : urls) {
            try {
                return refreshFromUrl(url, consumer, lastModified);
            } catch (Exception e) {
                logger.error("failed to process url: " + url, e);
            }
        }
        throw new RuntimeException("failed to process urls (see log for further exceptions)");
    }
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    private long refreshFromUrl(URL url, Consumer<InputStream> consumer, long lastModified) throws MalformedURLException, IOException {
        logger.debug("trying url: " + url);
        long sourceLastModified = 0;
        final URLConnection urlConnection = url.openConnection();
        if (url.getProtocol().equalsIgnoreCase("ftp")) {
        	sourceLastModified = getFtpLastModifiedTime(url);
        } else {
        	sourceLastModified = urlConnection.getLastModified();
        }
        String sourceLastModifiedStr = dateFormat.format(sourceLastModified);
        String lastModifiedStr = dateFormat.format(lastModified);
        if (sourceLastModified == 0) {
            logger.warn("resource has no modification date, forcing refresh...");
        } else if (sourceLastModified < lastModified) {
            logger.warn("resource is older than last modification date (source: " + sourceLastModifiedStr + ", last: " + lastModifiedStr + "), leaving it");
            return lastModified;
        } else if (sourceLastModified == lastModified) {
            logger.debug("resource is current (modified: " + lastModifiedStr + ")");
            return lastModified;
        } else {
            logger.info("resource is newer than last modification date, refreshing (source: " + sourceLastModifiedStr + ", cache: " + lastModifiedStr + ")");
        }
        urlConnection.connect();
        InputStream is = urlConnection.getInputStream();
        if (unzip) {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry = zis.getNextEntry();
            logger.info("unzipping, got zip entry: {}", entry.getName());
            is = zis;
        }
        consumer.accept(is);
        is.close();
        return sourceLastModified;
    }

    public long getFtpLastModifiedTime(URL url) {
    	FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
            ftpClient.login("anonymous", "anonymous");
            ftpClient.enterLocalPassiveMode();
            String filePath = url.getPath();
            String time = ftpClient.getModificationTime(filePath);
            //logger.debug("server replied: " + time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String timePart = time.split(" ")[1];
            Date modificationTime = dateFormat.parse(timePart);
            //logger.debug("parsed time: " + modificationTime);
            return modificationTime.getTime();
        } catch (Exception e) {
        	logger.error("failed to parse time for url: " + url, e);
        	return 0;
        } finally {
        	if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }    	
    }
    
}
