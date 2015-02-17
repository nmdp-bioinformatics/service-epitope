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

package org.nmdp.service.epitope.freq;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aHeterozygousAllelePair;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aHomozygousAllelePair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.freq.DbiFrequencyResolver;

@RunWith(MockitoJUnitRunner.class)
public class DbiFrequencyResolverTest {

	@Mock
	private DbiManager dbi;
	
	@InjectMocks
	private DbiFrequencyResolver resolver;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStripLocus() throws Exception {
		String test = resolver.stripLocus("HLA-DPB1*01:01");
		assertThat(test, equalTo("01:01"));
	}

	@Test
	public void testApply_Homozygous() throws Exception {
		when(dbi.getFrequency(anyString(), any(DetailRace.class))).thenReturn(.01);
		Double test = resolver.apply(aHomozygousAllelePair());
		verify(dbi).getFrequency(anyString(), any(DetailRace.class));
		assertThat(test, org.hamcrest.Matchers.closeTo(0.0001, 0.0000001));
	}

	@Test
	public void testApply_Heterozygous() throws Exception {
		when(dbi.getFrequency(anyString(), any(DetailRace.class))).thenReturn(.02).thenReturn(.03);
		Double test = resolver.apply(aHeterozygousAllelePair());
		verify(dbi, times(2)).getFrequency(anyString(), any(DetailRace.class));
		assertThat(test, org.hamcrest.Matchers.closeTo(0.0012, 0.0000001));
	}

}
