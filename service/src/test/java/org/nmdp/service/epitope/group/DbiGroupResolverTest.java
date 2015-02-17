/*

    epitope-service  T-cell epitope group matching service for DPB1 locus.
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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.client.GlClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.EpitopeServiceTestData;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.group.DbiGroupResolver;

@RunWith(MockitoJUnitRunner.class)
public class DbiGroupResolverTest {
	@Mock
	private DbiManager dbiManager;

	@Mock
	private GlClient glClient;
	
	@InjectMocks
	private DbiGroupResolver resolver;

	@Test
	public void testApply() throws Exception {
		Allele a1 = EpitopeServiceTestData.group1Alleles().get(0);
		Allele a2 = EpitopeServiceTestData.group1Alleles().get(1);
		when(dbiManager.getAllelesForGroup(1)).thenReturn(Arrays.asList(a1.getGlstring(), a2.getGlstring()));
		when(glClient.createAllele(a1.getGlstring())).thenReturn(a1);
		when(glClient.createAllele(a2.getGlstring())).thenReturn(a2);
		List<Allele> test = resolver.apply(1);
		assertThat(test, containsInAnyOrder(a1, a2));
	}

}
