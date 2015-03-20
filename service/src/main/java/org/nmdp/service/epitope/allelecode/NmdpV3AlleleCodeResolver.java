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

package org.nmdp.service.epitope.allelecode;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeRefreshMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NmdpV3AlleleCodeResolver implements Function<String, String> {

	private static class AlleleCodeExpansion {
		private List<String> alleleList;
		private boolean familyIncluded;
		public AlleleCodeExpansion(String alleles, boolean prefixIncluded) {
			this.familyIncluded = prefixIncluded;
			this.alleleList = Splitter.on("/").splitToList(alleles);
		}
		public List<String> getAlleleList() {
			return alleleList;
		}
		public boolean isFamilyIncluded() {
			return familyIncluded;
		}
	}
	
	private static final Pattern ALLELE_CODE_PAT = Pattern.compile("((?:HLA-)?[A-Z0-9]+\\*)?(\\d+:)([A-Z]+)", CASE_INSENSITIVE);
	
	Map<String, AlleleCodeExpansion> alleleCodeMap = new HashMap<>();
	long lastModified = 0;
	private URL[] urls;
	static Logger logger = LoggerFactory.getLogger(NmdpV3AlleleCodeResolver.class);

	@Inject
	public NmdpV3AlleleCodeResolver(@NmdpV3AlleleCodeUrls URL[] urls, @NmdpV3AlleleCodeRefreshMillis final long refreshMillis) {
		this.urls = urls;
		refresh();
		if (refreshMillis > 0) {
			new Timer("NmdpV3AlleleCodeResolverRefreshThread", true).schedule(new TimerTask() {
				@Override public void run() {
					refresh();
				}
			}, refreshMillis, refreshMillis);
		}
	}

	private void refresh() {
		boolean success = false;
		for (URL url : urls) {
			try {
				refreshFromUrl(url);
				success = true;
				break;
			} catch (Exception e) {
				logger.error("failed to refresh allele codes from url: " + url, e);
			}
		}
		if (!success) {
			throw new RuntimeException("failed to refersh allele codes from sources (see log for further exceptions)");
		}
	}
	
	private void refreshFromUrl(URL url) throws MalformedURLException, IOException {
		final URLConnection urlConnection = url.openConnection();
		urlConnection.connect();
		long sourceLastModified = urlConnection.getLastModified();  
		logger.debug("checking resource modification date: " + sourceLastModified);
		if (sourceLastModified == 0) {
			logger.warn("cache modified is not set, ignoring...");
		} else if (sourceLastModified < lastModified) {
			logger.warn("cache is is newer than source (source: " + sourceLastModified + ", cache: " + lastModified + "), leaving it");
			return;
		} else if (sourceLastModified == lastModified) {
			logger.debug("cache is current");
			return;
		} else {
			logger.warn("cache is older than source, refreshing (source: " + sourceLastModified + ", cache: " + lastModified + ")");
		}

		InputStream inputStream = getInputStream(urlConnection);
		Map<String, AlleleCodeExpansion> map = buildAlleleCodeMapFromStream(inputStream);
		synchronized(this.alleleCodeMap) {
			this.alleleCodeMap = map;
		}
		this.lastModified = sourceLastModified;
		logger.info("reload AlleleCodeMap complete");
	}

	private static InputStream getInputStream(URLConnection urlConnection) throws IOException {
		InputStream inputStream = urlConnection.getInputStream();
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		InputStreamReader ir = new InputStreamReader(zipInputStream);
		ZipEntry entry = zipInputStream.getNextEntry();
		BufferedReader br = new BufferedReader(ir);
		logger.info("reading zip entry {} from {}", entry.getName(), urlConnection.getURL());
		return zipInputStream;
	}

	Map<String, AlleleCodeExpansion> buildAlleleCodeMapFromStream(final InputStream inputStream) {
        try {
			Map<String, AlleleCodeExpansion> newMap = new HashMap<>();
			int i = 0;
			Splitter splitter = Splitter.on("\t");
			try (	InputStreamReader ir = new InputStreamReader(inputStream); 
			        BufferedReader br = new BufferedReader(ir)) 
			{
				while (br.readLine().length() != 0) continue;	// skip header
				String s = null;
				while ((s = br.readLine()) != null) {
					List<String> parsed = splitter.splitToList(s);
					if (parsed.size() != 3) {
						throw new RuntimeException("failed to parse file on line " + i + ", expected 3 fields: " + s);
					}
					newMap.put(parsed.get(1), new AlleleCodeExpansion(parsed.get(2), ("*".equals(parsed.get(0)))));
				}
			} finally {
				inputStream.close();
			}
			return newMap;
		} catch (RuntimeException e) {
			throw (RuntimeException)e;
		} catch (Exception e) {
			throw new RuntimeException("exception while refreshing allele codes", e);
		}
	}
	
	@Override
	public String apply(String alleleCode) {
		Matcher matcher = ALLELE_CODE_PAT.matcher(alleleCode);
		if (!matcher.matches()) {
			throw new RuntimeException("unrecognized allele code format: " + alleleCode);
		}
		String prefix = matcher.group(1) == null ? "" : matcher.group(1);
		String family = matcher.group(2);
		String code = matcher.group(3);
		AlleleCodeExpansion expansion = null;
		synchronized (this.alleleCodeMap) {
			expansion = alleleCodeMap.get(code);
		}
		if (null == expansion) {
			throw new RuntimeException("unrecognized allele code: " + alleleCode);
		}
		List<String> alleleList = expansion.getAlleleList();
		boolean includeFamily = !expansion.isFamilyIncluded();
		StringBuilder sb = new StringBuilder();
		if (null != prefix) sb.append(prefix);
		if (!expansion.isFamilyIncluded()) sb.append(family);
		sb.append(alleleList.get(0));
		for (String allele : alleleList.subList(1,  alleleList.size())) {
			sb.append("/");
			if (null != prefix) sb.append(prefix);
			if (includeFamily) sb.append(family);
			sb.append(allele);
		}
		return sb.toString();
	}

}
