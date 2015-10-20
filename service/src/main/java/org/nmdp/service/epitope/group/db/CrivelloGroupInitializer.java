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

package org.nmdp.service.epitope.group.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.ImmuneGroupRow;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAlleleUrls;
import org.nmdp.service.epitope.task.URLProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class CrivelloGroupInitializer {

    private URL[] urls;
    private DbiManager dbi;
	Logger logger = LoggerFactory.getLogger(getClass());

    int[] offsets = { 8, 9, 11, 35, 55, 56, 57, 69, 76, 84 };
    Map<Integer, Integer> offsetMap = getOffsetMap(); 	// apply these offsets (by size of protein list) 
    													// + offset from array above for each considered protein
    Map<Integer, Map<Character, Double>> scoreMap = getScoreMap();

	@Inject
    public CrivelloGroupInitializer(@HlaAlleleUrls URL[] urls, DbiManager dbi) {
        this.urls = urls;
        this.dbi = dbi;
    }

	private Map<Integer, Integer> getOffsetMap() {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(79, -8); // 57:01
		map.put(83, -8); // 08:01
		map.put(84, -6); // 102:01
		map.put(85, -6); // 11:01:02
		map.put(87, -6); // 01:01:03
		map.put(181, -6); // 02:01:08
        map.put(218, -6); // 138:01
		map.put(224, -6); // 03:01:08
		map.put(258, 28); // 01:01:01
		return map;
	}

	private Map<Integer, Map<Character, Double>> getScoreMap() {
		Map<Integer, Map<Character, Double>> map = new HashMap<>(); 
		Map<Character, Double> pmap = new HashMap<>(3);
		map.put(8, pmap);
		pmap.put('V', 0.0);
		pmap.put('L', -0.06);
		pmap.put('M', 0.0);
		pmap = new HashMap<>(6);
		map.put(9, pmap);
		pmap.put('H', 0.0);
		pmap.put('F', 0.69);
		pmap.put('Y', 0.87);
		pmap.put('D', 0.0);
		pmap.put('L', 0.0);
		pmap.put('S', 0.0);
		pmap = new HashMap<>(2);
		map.put(11, pmap);
		pmap.put('L', 0.0);
		pmap.put('G', 0.96);
		pmap = new HashMap<>(3);
		map.put(35, pmap);
		pmap.put('L', 0.05);
		pmap.put('F', 0.0);
		pmap.put('Y', 0.85);
		pmap = new HashMap<>(3);
		map.put(55, pmap);
		pmap.put('A', 0.73);
		pmap.put('D', 0.0);
		pmap.put('E', 0.0);
		pmap = new HashMap<>(2);
		map.put(56, pmap);
		pmap.put('A', -0.04);
		pmap.put('E', 0.0);
		pmap = new HashMap<>(2);
		map.put(57, pmap);
		pmap.put('E', -0.12);
		pmap.put('D', 0.0);
		pmap = new HashMap<>(3);
		map.put(69, pmap);
		pmap.put('E', 0.0);
		pmap.put('K', 0.95);
		pmap.put('R', 0.0);
		pmap = new HashMap<>(4);
		map.put(76, pmap);
		pmap.put('M', 0.54);
		pmap.put('V', 0.0);
		pmap.put('I', 0.0);
		pmap.put('R', 0.0);
		pmap = new HashMap<>(4);
		map.put(84, pmap);
		pmap.put('D', 0.0);
		pmap.put('G', 0.93);
		pmap.put('V', 0.0);
		pmap.put('N', 0.0);
		return map;
	}

    public void loadAlleleScores() {
    	Optional<Map<String,String>> alleleProteinMap = getAlleleProteinMap();
    	if (alleleProteinMap.isPresent()) {
    		// update if present, if not then protein file has not been updated
        	Iterator<ImmuneGroupRow> iter = alleleProteinMap.get().entrySet().stream()
        		.map(e -> new ImmuneGroupRow(e.getKey(), scoreAllele(e)))
        		.iterator();
        	dbi.loadImmuneGroups(iter, true);
    	}
    }
    
	private Integer scoreAllele(Map.Entry<String, String> e) {
		if (e.getKey().endsWith("N")) { 
//			logger.debug("scoring " + e.getKey() + ": " + 0);
			return 0; 
		}
		double total = 0.0;
		int sizeOffset = offsetMap.get(e.getValue().length()); 
		for (int protOffset : offsets) {
			Double score = scoreMap.get(protOffset).get(e.getValue().charAt(sizeOffset + protOffset));
			if (null == score) {
				logger.error("missing protein on allele: " + e.getKey() + " (size: " + e.getValue().length() 
						+ ", protein: " + e.getValue().charAt(sizeOffset + protOffset) 
						+ " at offset: " + sizeOffset + " + " + protOffset + ")");
				score = 2.0; // push towards 3 -- FIXME placeholder logic - push towards score of 3
			}
			total += score;
		}
		int group = 1;
		if (total > 0.6) group = 2;
		if (total > 2) group = 3;
//		logger.debug("scoring " + e.getKey() + ": " + group);
		return group;
	}

	/** @return map, if newer than last version, else empty
	 */
    private Optional<Map<String, String>> getAlleleProteinMap() {
    	Pattern allelePattern = Pattern.compile(">HLA:HLA\\d+ DPB1\\*(\\S+) (\\d+)");
    	Map<String, String> map = new HashMap<>();
    	URLProcessor urlProcessor = new URLProcessor(urls, false);
    	Long datasetDate = dbi.getDatasetDate("allele_group");
        if (null == datasetDate) datasetDate = 0L;
        Long newDate = urlProcessor.process(is -> {
        	try (InputStreamReader isr = new InputStreamReader(is);
        			BufferedReader br = new BufferedReader(isr)) 
        	{
        		String allele = null;
        		int length = 0;
    			StringBuilder sb = new StringBuilder();
        		while (true) {
	        		String s = br.readLine();
	        		if (s == null || s.startsWith(">")) {
        				// new allele
        				if (null != allele) { 
    						if (sb.length() != length) {
    							throw new IllegalStateException("failed to match reported to actual length (r:" 
    									+ length + ", a:" + sb.length() + ")");
    						}
        					map.put(allele, sb.toString());
        				}
        				if (s == null) break; // last iteration
    					allele = null;
    					sb = new StringBuilder();
    					Matcher matcher = allelePattern.matcher(s);
    					if (matcher.find()) {
    						allele = "HLA-DPB1*" + matcher.group(1);
    						length = Integer.valueOf(matcher.group(2));
    					}
	        		} else {
        				// add string to allele
        				sb.append(s);
        			}
        		}
        	} catch (Exception e) {
        		throw new RuntimeException("failed to load allele file", e);
        	}
//        }, datasetDate);
        }, 0L);
        dbi.updateDatasetDate("allele_group", datasetDate);
        return (datasetDate == newDate) ? Optional.empty() : Optional.of(map);
    }

}
