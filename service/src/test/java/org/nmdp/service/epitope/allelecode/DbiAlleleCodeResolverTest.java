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

package org.nmdp.service.epitope.allelecode;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.allelecode.DbiAlleleCodeResolver;
import org.nmdp.service.epitope.db.DbiManager;

@RunWith(MockitoJUnitRunner.class)
public class DbiAlleleCodeResolverTest {

	@Mock
	private DbiManager dbi;
	
	@InjectMocks
	private DbiAlleleCodeResolver resolver;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testApply_InvalidCode_ReturnsError() throws Exception {
		when(dbi.getAllelesForCode("HLA-DPB1", "01:ABCD")).thenReturn(Arrays.asList("HLA-DPB1*01:01", "HLA-DPB1*02:01", "HLA-DPB1*03:01"));
		String test = resolver.apply("HLA-DPB1*01:ABCD");
		assertThat(test, equalTo("HLA-DPB1*01:01/HLA-DPB1*02:01/HLA-DPB1*03:01"));
	}

}
