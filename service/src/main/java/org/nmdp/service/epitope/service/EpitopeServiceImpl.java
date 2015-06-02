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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.group.GroupResolver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;

/**
 * implementation of EpitopeService, 
 */
public class EpitopeServiceImpl implements EpitopeService {

	private Function<Integer, List<Allele>> groupResolver;
	private ImmutableListMultimap<Integer, Allele> groupAlleleMap;
	private ImmutableListMultimap<Allele, Integer> alleleGroupMap;
	private GlClient glClient;
	private Function<String, String> glStringFilter;

	@Inject
	public EpitopeServiceImpl(@GroupResolver Function<Integer, List<Allele>> groups, GlClient glClient, @GlStringFilter Function<String, String> glStringFilter) {
		this.groupResolver = groups;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
		
		// todo: refresh cache on underlying changes
		// see cacheresolver.addListener()
		buildMaps();
	}
	
	public void buildMaps() {
		ImmutableListMultimap.Builder<Integer, Allele> builder = ImmutableListMultimap.builder();
		builder.orderKeysBy(Ordering.natural());
		builder.orderValuesBy(new Comparator<Allele>() {
			@Override public int compare(Allele o1, Allele o2) {
				return o1.getGlstring().compareTo(o2.getGlstring());
			}
		});
		for (int i = 0; i <= 3; i++) {
			Integer group = i;
			List<Allele> alleles = groupResolver.apply(group);
			if (null == alleles) {
			    throw new IllegalStateException("failed to resolve alleles in group: " + group);
			}
		    for (Allele allele : alleles) builder.put(group, allele);
		}
		groupAlleleMap = builder.build();
		alleleGroupMap = groupAlleleMap.inverse();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Allele getEffectiveAllele(Allele allele) {
		String alleleStr = allele.getGlstring();
		String filterAlleleStr = glStringFilter.apply(alleleStr);
		if (!alleleStr.equals(filterAlleleStr)) {
			try {
				allele = glClient.createAllele(filterAlleleStr);
			} catch (GlClientException e) {
				throw new RuntimeException("failed to create allele for glstring: " + filterAlleleStr, e);
			}
		}
		return allele;
	}
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getGroupForAllele(Allele allele) {
		ImmutableList<Integer> list = alleleGroupMap.get(allele);
		if (list.size() == 0) return null; // throw new RuntimeException("no group for allele: " + allele);
		return list.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getGroupForAllele(String glstring) {
		glstring = glStringFilter.apply(glstring);
		try {
			Allele allele = glClient.createAllele(glstring);
			return getGroupForAllele(allele);
		} catch (GlClientException e) {
			throw new RuntimeException("failed to resolve allele: " + glstring, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, List<Allele>> getAllGroups() {
		Map<Integer, List<Allele>> map = new HashMap<>();
		for (Integer key : groupAlleleMap.keySet()) {
			map.put(key, groupAlleleMap.get(key));
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Allele, Integer> getGroupsForAllAlleles() {
		return Maps.transformValues(Multimaps.asMap(alleleGroupMap), l -> l.get(0));
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
		return groupAlleleMap.get(group);
	}

}
