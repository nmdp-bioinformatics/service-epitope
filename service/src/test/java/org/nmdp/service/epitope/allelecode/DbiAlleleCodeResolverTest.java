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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.AlleleCodeRow;
import org.nmdp.service.epitope.db.DbiManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class DbiAlleleCodeResolverTest {

	private DbiAlleleCodeResolver resolver;

	@Mock
	DbiManager dbi = null;
	
	@Before
	public void setUp() throws Exception {
		when(dbi.getAlleleCodes()).thenReturn(Arrays.asList(
				new AlleleCodeRow("AB", "01", true),
				new AlleleCodeRow("AB", "02", true),
				new AlleleCodeRow("AFC", "01:01", false),
				new AlleleCodeRow("AFC", "02:01", false),
				new AlleleCodeRow("AFC", "02:02", false),
				new AlleleCodeRow("AFC", "03:01", false)
		).iterator());
		resolver = new DbiAlleleCodeResolver(dbi);
		resolver.buildAlleleCodeMap(dbi.getAlleleCodes());
	}

	@Test
	public void testApply() throws Exception {
		String resolved = resolver.apply("HLA-DPB1*01:AFC");
		List<String> test = Splitter.on("/").splitToList(resolved);
		assertThat(test, containsInAnyOrder("HLA-DPB1*01:01", "HLA-DPB1*02:01", "HLA-DPB1*02:02", "HLA-DPB1*03:01"));
	}

	@Test
	public void testApply_XXCode() throws Exception {
		when(dbi.getFamilyAlleleMap()).thenReturn(ImmutableMap.of("04", ImmutableSet.of("04:01", "04:02", "04:03")));
		String resolved = resolver.apply("HLA-DPB1*04:XX");
		List<String> test = Splitter.on("/").splitToList(resolved);
		assertThat(test, containsInAnyOrder("HLA-DPB1*04:01", "HLA-DPB1*04:02", "HLA-DPB1*04:03"));
	}
	
	@Test
	public void testApply_Generic() throws Exception {
		String resolved = resolver.apply("HLA-DPB1*04:AB");
		List<String> test = Splitter.on("/").splitToList(resolved);
		assertThat(test, containsInAnyOrder("HLA-DPB1*04:01", "HLA-DPB1*04:02"));
	}
}
