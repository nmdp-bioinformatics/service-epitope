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

import static com.google.common.collect.ObjectArrays.concat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.anAllele;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestDbiManager;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestGlClient;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group0Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group1Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group2Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group3Alleles;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.service.epitope.db.DbiManager;

@RunWith(MockitoJUnitRunner.class)
public class EpitopeServiceImplTest {

	@Mock
	private GlClient glClient;

	@Mock
	private Function<String, String> glStringFilter;

	private EpitopeServiceImpl service;

	private DbiManager dbiManager;

	@Before
	public void setUp() throws Exception {
		dbiManager = getTestDbiManager();
		glClient = getTestGlClient();
		service = new EpitopeServiceImpl(glClient, glStringFilter, dbiManager);
		service.buildMaps();
	}

	@Test
	public void testGetGroupForAllele() throws Exception {
		assertThat(service.getGroupForAllele(group1Alleles().get(0)), equalTo(1));
		assertThat(service.getGroupForAllele(group2Alleles().get(0)), equalTo(2));
		assertThat(service.getGroupForAllele(group3Alleles().get(0)), equalTo(3));
	}

	@Test
	public void testGetAllGroups() throws Exception {
		Map<Integer, List<Allele>> allGroups = service.getAllGroups();
		assertThat(allGroups.size(), equalTo(4));
		assertThat(allGroups.keySet(), contains(0, 1, 2, 3));
        assertThat(allGroups.get(0), containsInAnyOrder(group0Alleles().toArray()));
        assertThat(allGroups.get(1), containsInAnyOrder(group1Alleles().toArray()));
		assertThat(allGroups.get(2), containsInAnyOrder(group2Alleles().toArray()));
		assertThat(allGroups.get(3), containsInAnyOrder(group3Alleles().toArray()));
	}

	@Test
	public void testGetGroupsForAllAlleles() throws Exception {
		Map<Allele, Integer> map = service.getGroupsForAllAlleles();
        assertThat(map.get(group0Alleles().get(0)), equalTo(0));
        assertThat(map.get(group1Alleles().get(0)), equalTo(1));
		assertThat(map.get(group2Alleles().get(0)), equalTo(2));
		assertThat(map.get(group3Alleles().get(0)), equalTo(3));
	}

	@Test
	public void testGetAllAlleles() throws Exception {
		List<Allele> list = service.getAllAlleles();
		assertThat(list.size(), equalTo(8));
		Allele[] a0 = new Allele[0];
		// Stream.concat(group1Alleles(), group2Alleles(), group3Alleles()).toArray(Allele[]::new);
		assertThat(list, containsInAnyOrder(concat(concat(concat(
				        group0Alleles().toArray(a0), group1Alleles().toArray(a0), Allele.class), 
				        group2Alleles().toArray(a0), Allele.class),
						group3Alleles().toArray(a0), Allele.class)));
	}

	@Test
	public void testGetAllelesForGroup() throws Exception {
		List<Allele> group1Alleles = service.getAllelesForGroup(1);
		assertThat(group1Alleles, containsInAnyOrder(group1Alleles().toArray()));
	}

	@Test
	public void testGetEffectiveAllele() throws Exception {
		when(glStringFilter.apply("foo")).thenReturn("bar");
		Allele expect = anAllele("bar");
		when(glClient.createAllele("bar")).thenReturn(expect);
		Allele test = service.getEffectiveAllele(anAllele("foo"));
		assertThat(test, equalTo(expect));
	}

}
