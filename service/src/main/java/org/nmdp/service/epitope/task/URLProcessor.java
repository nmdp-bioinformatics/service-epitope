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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    
    private long refreshFromUrl(URL url, Consumer<InputStream> consumer, long lastModified) throws MalformedURLException, IOException {
        logger.debug("trying url: " + url);
        final URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        long sourceLastModified = urlConnection.getLastModified();
        if (sourceLastModified == 0) {
            logger.warn("resource has no modification date, forcing refresh...");
        } else if (sourceLastModified < lastModified) {
            logger.warn("resource is older than last modification date (source: " + sourceLastModified + ", last: " + lastModified + "), leaving it");
            return lastModified;
        } else if (sourceLastModified == lastModified) {
            logger.debug("resource is current");
            return lastModified;
        } else {
            logger.info("resource is newer than last modification date, refreshing (source: " + sourceLastModified + ", cache: " + lastModified + ")");
        }
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

}
