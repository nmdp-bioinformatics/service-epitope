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

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.GroupRow;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaProtUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class AlignedImmuneGroupInitializer implements ImmuneGroupInitializer {

	private URL[] urls;
	private DbiManager dbi;
	Logger logger = LoggerFactory.getLogger(getClass());
	int[] offsets = { 8, 9, 11, 35, 55, 56, 57, 69, 76, 84 };
	Map<Integer, Map<Character, Double>> scoreMap = getScoreMap();

	@Inject
	public AlignedImmuneGroupInitializer(@HlaProtUrls URL[] urls, DbiManager dbi) {
		this.dbi = dbi;
		this.urls = urls;
	}

	private Map<Integer, Map<Character, Double>> getScoreMap() {
		Map<Integer, Map<Character, Double>> map = new HashMap<>(); 
		Map<Character, Double> pmap = new HashMap<>(1);
		map.put(8, pmap);
		pmap.put('L', -0.06);
		pmap = new HashMap<>(2);
		map.put(9, pmap);
		pmap.put('F', 0.69);
		pmap.put('Y', 0.87);
		pmap = new HashMap<>(1);
		map.put(11, pmap);
		pmap.put('G', 0.96);
		pmap = new HashMap<>(2);
		map.put(35, pmap);
		pmap.put('L', 0.05);
		pmap.put('Y', 0.85);
		pmap = new HashMap<>(1);
		map.put(55, pmap);
		pmap.put('A', 0.73);
		pmap = new HashMap<>(1);
		map.put(56, pmap);
		pmap.put('A', -0.04);
		pmap = new HashMap<>(1);
		map.put(57, pmap);
		pmap.put('E', -0.12);
		pmap = new HashMap<>(1);
		map.put(69, pmap);
		pmap.put('K', 0.95);
		pmap = new HashMap<>(1);
		map.put(76, pmap);
		pmap.put('M', 0.54);
		pmap = new HashMap<>(1);
		map.put(84, pmap);
		pmap.put('G', 0.93);
		return map;
	}

	/** @return map, if newer than last version, else empty
	 */
	public void loadImmuneGroups() {
		logger.info("loading immune groups");
		URLProcessor urlProcessor = new URLProcessor(urls, false);
		Long datasetDate = dbi.getDatasetDate("immune_group");
		if (null == datasetDate) datasetDate = 0L;
		datasetDate = urlProcessor.process(is -> {
			Map<String, String> alleleProteinMap = getAlleleProteinMap(is);
			Iterator<GroupRow<Integer>> iter = alleleProteinMap.entrySet().stream()
					.map(e -> new GroupRow<Integer>(e.getKey(), scoreAllele(e)))
					.iterator();
			dbi.loadImmuneGroups(iter, true);
		}, datasetDate);
		dbi.updateDatasetDate("immune_group", datasetDate);
		logger.debug("done loading immune groups");
	}

	/**
	 * @param is {@link InputStream} to read from
	 * @return map of allele name to protein sequence
	 */
	private Map<String, String> getAlleleProteinMap(InputStream is) {
		try (InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) 
		{
			// match ARS level, null expression alleles don't match on purpose
			Pattern allelePattern = Pattern.compile("DPB1\\s+((\\d+:\\d+)(?:[0-9:]+)?)\\s+(\\S+)"); 
			Map<String, String> map = br.lines()
					.map(s -> allelePattern.matcher(s))
					.filter(m -> m.find())
					.collect(toMap(m -> "HLA-DPB1*" + m.group(2), m -> m.group(3), (m1, m2) -> m1));
			return map;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("failed to load immune groups", e);
		}
	}

	private Integer scoreAllele(Map.Entry<String, String> e) {
		double total = 0.0;
		int offset = -1;
		for (int protOffset : offsets) {
			try {
				Double score = scoreMap.get(protOffset).get(e.getValue().charAt(offset + protOffset));
				if (null == score) {
					//logger.error("missing protein on allele: " + e.getKey() + " (size: " + e.getValue().length() 
					//		+ ", protein: " + e.getValue().charAt(offset + protOffset) 
					//		+ " at offset: " + (offset == 0 ? "" : + offset + " + ") + protOffset + ")");
					score = 0.0; // unknown variants don't influence score
				}
				total += score;
			} catch (Exception ex) {
				logger.error("error on allele: " + e.getKey() + " (size: " + e.getValue().length() 
						+ ", protein: " + e.getValue().charAt(offset + protOffset) 
						+ " at offset: " + (offset == 0 ? "" : + offset + " + ") + protOffset + ")", ex);
			}
		}
		int group = 1;
		if (total > 0.6) group = 2;
		if (total > 2) group = 3;
		//logger.debug("scoring " + e.getKey() + ": " + group);
		return group;
	}

}

