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

package org.nmdp.service.epitope.service;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.gl.filter.GlstringFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * implementation of EpitopeService, 
 */
public class EpitopeServiceImpl implements EpitopeService {

	private ImmutableListMultimap<Optional<Integer>, Allele> groupAlleleMap;
	private ImmutableListMultimap<Allele, Optional<Integer>> alleleGroupMap;
	private GlClient glClient;
	private Function<String, String> glStringFilter;
	private DbiManager dbi;
	private Map<String, List<String>> familyAlleleMap;

	private java.util.function.Function<String, Allele> createAllele = allele -> {
		try { 
			return glClient.createAllele(allele); 
		} catch (GlClientException e) { 
			throw new RuntimeException("failed to create allele: " + allele, e); 
		}
	};
	
	@Inject
	public EpitopeServiceImpl(GlClient glClient, @GlstringFilter Function<String, String> glStringFilter, DbiManager dbi) {
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
		this.dbi = dbi;
		
		// todo: refresh cache on underlying changes
		// see cacheresolver.addListener()

		// now explicitly initialized - see EpitopeServiceInitializer
		//buildMaps();
	}
	
	@Override
	public void buildMaps() {
		ImmutableListMultimap.Builder<Allele, Optional<Integer>> builder = ImmutableListMultimap.builder();
		builder.orderKeysBy(new Comparator<Allele>() {
			@Override public int compare(Allele o1, Allele o2) {
				return o1.getGlstring().compareTo(o2.getGlstring());
			}
		});
		builder.orderValuesBy((lhs, rhs) -> {
			if (!lhs.isPresent() && !rhs.isPresent()) return 0;
			if (!lhs.isPresent()) return 1;
			if (!rhs.isPresent()) return -1;
			return (lhs.get().compareTo(rhs.get()));
		});
		Map<String, Integer> groupLookup = dbi.getAlleleGroupMap();
		dbi.getAllelesForLocus("HLA-DPB1").stream()
				.forEach(allele -> builder.put(createAllele.apply(allele), findGroup(groupLookup, allele)));
		Set<String> keySet = builder.build().keySet().stream().map(a -> a.getGlstring()).collect(toSet());
		groupLookup.entrySet().stream()
				.filter(e -> !keySet.contains(e.getKey()))
				.forEach(e -> builder.put(createAllele.apply(e.getKey()), Optional.of(e.getValue())));
		alleleGroupMap = builder.build();
		groupAlleleMap = alleleGroupMap.inverse();
	}
	
    /**
	 * Check for prefixed alleles with assigned TCE groups, use that group if found
	 */
	private Optional<Integer> findGroup(Map<String, Integer> groupLookup, String allele) {
		if (allele.endsWith("N")) return Optional.of(0);
		int last = 0;
		while (true) {
			Integer group = groupLookup.get(allele);
			if (group != null) { return Optional.of(group); }
			last = allele.lastIndexOf(':');
			if (last < 0) {
				return Optional.empty();
			}
			allele = allele.substring(0, last);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Allele getEffectiveAllele(Allele allele) {
		String alleleStr = allele.getGlstring();
		String filterAlleleStr = glStringFilter.apply(alleleStr);
		if (!alleleStr.equals(filterAlleleStr)) {
			allele = createAllele.apply(filterAlleleStr);
		}
		return allele;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getGroupForAllele(Allele allele) {
		ImmutableList<Optional<Integer>> list = alleleGroupMap.get(allele);
		if (list.size() == 0) throw new RuntimeException("unknown allele: " + allele);
		return list.get(0).orElse(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getGroupForAllele(String glstring) {
		glstring = glStringFilter.apply(glstring);
		Allele allele = createAllele.apply(glstring);
		return getGroupForAllele(allele);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, List<Allele>> getAllGroups() {
		Map<Integer, List<Allele>> map = new HashMap<>();
		for (Optional<Integer> key : groupAlleleMap.keySet()) {
			if (key.isPresent()) map.put(key.get(), groupAlleleMap.get(key));
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Allele, Integer> getGroupsForAllAlleles() {
		return alleleGroupMap.asMap().entrySet()
				.stream()
				.filter(e -> !e.getValue().isEmpty() && e.getValue().iterator().next().isPresent())
				.collect(toMap(e -> e.getKey(), e -> e.getValue().iterator().next().get()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Allele> getAllAlleles() {
		return Lists.newArrayList(alleleGroupMap.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Allele> getAllelesForGroup(Integer group) {
		return groupAlleleMap.get(Optional.of(group));
	}

}
