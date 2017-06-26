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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlstringTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;


/**
 * implementation of EpitopeService, 
 */
public class EpitopeServiceImpl implements EpitopeService {

	private ImmutableListMultimap<Optional<Integer>, Allele> groupAlleleMap;
	private ImmutableListMultimap<Allele, Optional<Integer>> alleleGroupMap;
	private GlClient glClient;
	private Function<String, String> alleleTransformer;
	private DbiManager dbi;
	Logger logger = LoggerFactory.getLogger(getClass());

	private Allele createAllele(String allele) {
		try {
			return glClient.createAllele(allele);
		} catch (GlClientException e) {
			throw new RuntimeException("failed to create allele: " + allele, e);
		}
	}

	@Inject
	public EpitopeServiceImpl(GlClient glClient, @GlstringTransformer Function<String, String> alleleTransformer, DbiManager dbi) {
		this.glClient = glClient;
		this.alleleTransformer = alleleTransformer;
		this.dbi = dbi;
		
		// todo: refresh cache on underlying changes
		// see cacheresolver.addListener()

		// now explicitly initialized - see EpitopeServiceInitializer
		//buildMaps();
	}
	
	@Override
	public void buildImmuneGroupMaps() {
		logger.info("building allele <-> immune group maps");
		ImmutableListMultimap.Builder<Allele, Optional<Integer>> builder = ImmutableListMultimap.builder();
		builder.orderKeysBy(Comparator.comparing(Allele::getGlstring));
		builder.orderValuesBy((lhs, rhs) -> lhs.map(l -> rhs.map(l::compareTo).orElse(-1)).orElse(rhs.map(r -> 1).orElse(0)));
		Map<String, Integer> groupLookup = dbi.getAlleleGroupMap();
		// add all known imgt alleles
		dbi.getAllelesForLocus("HLA-DPB1")
				.forEach(allele -> builder.put(createAllele(allele), findImmuneGroup(groupLookup, allele)));
		// add all alleles missing from imgt that we have group definitions for
		Set<String> keySet = builder.build().keySet().stream().map(Allele::getGlstring).collect(toSet());
		groupLookup.entrySet().stream()
				.filter(e -> !keySet.contains(e.getKey()))
				.forEach(e -> builder.put(createAllele(e.getKey()), Optional.of(e.getValue())));
		alleleGroupMap = builder.build();
		groupAlleleMap = alleleGroupMap.inverse();
		logger.debug("done building allele <-> immune group maps");
	}
	
    /**
	 * Called by buildImmuneGroupMaps(), checks for prefixed alleles with assigned TCE groups, use that group if found
	 */
	private Optional<Integer> findImmuneGroup(Map<String, Integer> immuneGroupLookup, String allele) {
		if (allele.endsWith("N")) return Optional.of(0);
		int last = 0;
		while (true) {
			Integer group = immuneGroupLookup.get(allele);
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
	public boolean isValidAllele(Allele allele) {
		return alleleGroupMap.containsKey(allele);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getImmuneGroupForAllele(Allele allele) {
		ImmutableList<Optional<Integer>> list = alleleGroupMap.get(allele);
		if (list.size() == 0) throw new RuntimeException("unknown allele: " + allele);
		return list.get(0).orElse(null);
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public Integer getImmuneGroupForAllele(String allele) {
//		return getImmuneGroupForAllele(createAllele(allele));
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, List<Allele>> getAllImmuneGroups() {
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
	public Map<Allele, Integer> getImmuneGroupsForAllAlleles() {
		return alleleGroupMap.asMap().entrySet()
				.stream()
				.filter(e -> !e.getValue().isEmpty() && e.getValue().iterator().next().isPresent())
				.collect(toMap(Entry::getKey, e -> e.getValue().iterator().next().get()));
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
	public List<Allele> getAllelesForImmuneGroup(Integer group) {
		return groupAlleleMap.get(Optional.of(group));
	}

}
