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

package org.nmdp.service.epitope.gl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.gl.filter.AlleleCodeFilter;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class AlleleCodeFilterTest {

	@Mock
	private Function<String, String> resolver;
	
	@Captor
	private ArgumentCaptor<String> gl;
	
	private AlleleCodeFilter alleleCodeFilter;

	@Before
	public void setUp() throws Exception {
		alleleCodeFilter = new AlleleCodeFilter(resolver);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testResolve_SingleCode() throws Exception {
		when(resolver.apply("HLA-A*01:ABCD")).thenReturn("HLA-A*01:01/HLA-A*01:02");
		String filtered = alleleCodeFilter.apply("HLA-A*01:ABCD");
		assertThat(filtered, equalTo("HLA-A*01:01/HLA-A*01:02"));
	}

	@Test
	public void testResolve_Genotype() throws Exception {
		when(resolver.apply("HLA-A*01:ABCD")).thenReturn("HLA-A*01:01/HLA-A*01:02");
		when(resolver.apply("HLA-A*01:EFGH")).thenReturn("HLA-A*01:03/HLA-A*01:04");
		String filtered = alleleCodeFilter.apply("HLA-A*01:ABCD+HLA-A*01:EFGH");
		assertThat(filtered, equalTo("HLA-A*01:01/HLA-A*01:02+HLA-A*01:03/HLA-A*01:04"));
	}

}
