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

package org.nmdp.service.epitope.resource.impl;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.anAlleleList;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestEpitopeService;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestGlClient;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestGlStringFilter;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group0Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group1Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group2Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group3Alleles;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.resource.AlleleListRequest;
import org.nmdp.service.epitope.resource.GroupView;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.FrequencyService;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class GroupResourceTest {
	
	private EpitopeService epitopeService;

	private GlClient glClient;

	private Function<String, String> glStringFilter;

	private GroupResource resource;

	@Mock
    private FrequencyService freqService;	
	
	@Before
	public void setUp() throws Exception {
		epitopeService = getTestEpitopeService();
		glClient = getTestGlClient();
		glStringFilter = getTestGlStringFilter();
		resource = new GroupResource(epitopeService, glClient, glStringFilter, freqService);
	}

	
	public List<String> allelesToStrings(List<Allele> alleleList) {
		return Lists.transform(alleleList, new Function<Allele, String>() {
			@Override public String apply(Allele input) { return input.getGlstring(); }});
	}

	public ImmutableListMultimap<Integer, GroupView> getGroupMap(List<GroupView> groups) {
		return FluentIterable.from(groups).index(g -> g.getGroup());
	}

	@Test
	public void testGetGroups_NoInputs() throws Exception {
		List<GroupView> groups = resource.getGroups(null, null, null);
		assertThat(groups.size(), equalTo(4));
		ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
		assertThat(map.keySet(), containsInAnyOrder(0, 1, 2, 3));
        assertThat(map.get(0).size(), equalTo(1));
        assertThat(map.get(1).size(), equalTo(1));
		assertThat(map.get(2).size(), equalTo(1));
		assertThat(map.get(3).size(), equalTo(1));
        assertThat(map.get(0).get(0).getAlleleList(), 
                containsInAnyOrder(allelesToStrings(group0Alleles()).toArray()));
        assertThat(map.get(1).get(0).getAlleleList(), 
                containsInAnyOrder(allelesToStrings(group1Alleles()).toArray()));
		assertThat(map.get(2).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group2Alleles()).toArray()));
		assertThat(map.get(3).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group3Alleles()).toArray()));
	}

	@Test
	public void testGetGroups_Alleles() throws Exception {
		List<Allele> al = anAlleleList().getAlleles();
		String gls = Joiner.on(",").join(al);
		List<GroupView> groups = resource.getGroups(gls, null, null);
		assertThat(groups.size(), equalTo(3));
		ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
		assertThat(map.keySet(), containsInAnyOrder(1, 2, 3));
		assertThat(map.get(1).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(1).get(0).getAlleleList().get(0), equalTo(group1Alleles().get(0).getGlstring()));
		assertThat(map.get(2).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(2).get(0).getAlleleList().get(0), equalTo(group2Alleles().get(0).getGlstring()));
		assertThat(map.get(3).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(3).get(0).getAlleleList().get(0), equalTo(group3Alleles().get(0).getGlstring()));
	}

	@Test
	public void testGetGroups_Groups() throws Exception {
		List<GroupView> groups = resource.getGroups(null, "1,2", null);
		assertThat(groups.size(), equalTo(2));
		ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
		assertThat(map.keySet(), containsInAnyOrder(1, 2));
		assertThat(map.get(1).size(), equalTo(1));
		assertThat(map.get(2).size(), equalTo(1));
		assertThat(map.get(1).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group1Alleles()).toArray()));
		assertThat(map.get(2).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group2Alleles()).toArray()));
	}

	@Test
	public void testGetGroups_AlleleListRequest_NoInputs() {
		AlleleListRequest request = new AlleleListRequest(null, null, null);
		List<GroupView> groups = resource.getGroups(request);
		assertThat(groups, emptyIterable());
	}
	
	@Test
	public void testGetGroups_AlleleListRequest_Alleles() {
		List<Allele> al = anAlleleList().getAlleles();
		AlleleListRequest request = new AlleleListRequest(allelesToStrings(al), null, null);
		List<GroupView> groups = resource.getGroups(request);
		assertThat(groups.size(), equalTo(3));
		ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
		assertThat(map.keySet(), containsInAnyOrder(1, 2, 3));
		assertThat(map.get(1).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(1).get(0).getAlleleList().get(0), equalTo(group1Alleles().get(0).getGlstring()));
		assertThat(map.get(2).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(2).get(0).getAlleleList().get(0), equalTo(group2Alleles().get(0).getGlstring()));
		assertThat(map.get(3).get(0).getAlleleList().size(), equalTo(1));
		assertThat(map.get(3).get(0).getAlleleList().get(0), equalTo(group3Alleles().get(0).getGlstring()));
	}
	
	@Test
	public void testGetGroups_AlleleListRequest_Groups() throws Exception {
		AlleleListRequest request = new AlleleListRequest(null, Arrays.asList(1, 2), null);
		List<GroupView> groups = resource.getGroups(request);
		assertThat(groups.size(), equalTo(2));
		ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
		assertThat(map.keySet(), containsInAnyOrder(1, 2));
		assertThat(map.get(1).size(), equalTo(1));
		assertThat(map.get(2).size(), equalTo(1));
		assertThat(map.get(1).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group1Alleles()).toArray()));
		assertThat(map.get(2).get(0).getAlleleList(), 
				containsInAnyOrder(allelesToStrings(group2Alleles()).toArray()));
	}	
	
	@Test
	public void GetGroups_AlleleListQuest_Alleles_Race() throws Exception {
        List<Allele> al = anAlleleList().getAlleles();  // 09:01(1), 03:01(2), 01:01(3)
        DetailRace race = DetailRace.API;
        AlleleListRequest request = new AlleleListRequest(allelesToStrings(al), null, race);
        when(freqService.getFrequency(al.get(0).getGlstring(), race)).thenReturn(0.1);
        when(freqService.getFrequency(al.get(1).getGlstring(), race)).thenReturn(0.2);
        when(freqService.getFrequency(al.get(2).getGlstring(), race)).thenReturn(0.4);
        List<GroupView> groups = resource.getGroups(request);
        assertThat(groups.size(), equalTo(3));
        ImmutableListMultimap<Integer, GroupView> map = getGroupMap(groups);
        assertThat(map.keySet(), containsInAnyOrder(1, 2, 3));
        assertThat(map.get(1).get(0).getProbability(), closeTo(1.0/7, 0.001));
        assertThat(map.get(2).get(0).getProbability(), closeTo(2.0/7, 0.001));
        assertThat(map.get(3).get(0).getProbability(), closeTo(4.0/7, 0.001));
	}
	
	@Test
	public void testGetGroup() throws Exception {
		GroupView group = resource.getGroup("1");
		assertThat(group.getAlleleList(), containsInAnyOrder(allelesToStrings(group1Alleles()).toArray()));
	}
	
}
