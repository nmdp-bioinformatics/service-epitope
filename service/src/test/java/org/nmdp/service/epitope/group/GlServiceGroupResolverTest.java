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

package org.nmdp.service.epitope.group;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.AlleleList;
import org.immunogenomics.gl.client.GlClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.EpitopeServiceTestData;
import org.nmdp.service.epitope.group.GlServiceGroupResolver;

@RunWith(MockitoJUnitRunner.class)
public class GlServiceGroupResolverTest {

	@Mock
	private GlClient glClient;

	private String group1Suffix;

	private String group2Suffix;

	private String group3Suffix;

	private String namespace;

	private GlServiceGroupResolver resolver;

	@Before
	public void setUp() throws Exception {
		namespace = "/test";
		group1Suffix = "/group1";
		group1Suffix = "/group2";
		group1Suffix = "/group3";
		resolver = new GlServiceGroupResolver(glClient, namespace, group1Suffix, group2Suffix, group3Suffix);
		when(glClient.getAlleleList(namespace + group1Suffix)).thenReturn(new AlleleList("group1", EpitopeServiceTestData.group1Alleles()));
		when(glClient.getAlleleList(namespace + group2Suffix)).thenReturn(new AlleleList("group2", EpitopeServiceTestData.group2Alleles()));
		when(glClient.getAlleleList(namespace + group3Suffix)).thenReturn(new AlleleList("group3", EpitopeServiceTestData.group3Alleles()));
	}

	@Test
	public void testApply() throws Exception {
		List<Allele> test = resolver.apply(1);
		assertThat(test, containsInAnyOrder(EpitopeServiceTestData.group1Alleles().toArray()));
	}

}
