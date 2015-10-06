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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.domain.DetailRace;

@RunWith(MockitoJUnitRunner.class)
public class FrequencyServiceImplTest {
	
	@Mock
	private DbiManager dbiManager;

	@Mock
	private EpitopeService epitopeService;
	
	private FrequencyServiceImpl service;
	
	@Before
	public void setUp() throws Exception {
	    service = new FrequencyServiceImpl(dbiManager, 1.0E-5);
	    Map<DetailRace, Map<String, Double>> map = new HashMap<>();
	    map.put(DetailRace.CAU, new HashMap<>());
	    when(dbiManager.getRaceAlleleFrequencyMap()).thenReturn(map);
	    service.buildFrequencyMap();
	}

	@Test
	public void testGetFrequency() throws Exception {
		dbiManager.getRaceAlleleFrequencyMap().get(DetailRace.CAU).put("HLA-DPB1*01:01", 0.02);
		service.buildFrequencyMap();
		Double test = service.getFrequency(DetailRace.CAU, "HLA-DPB1*01:01");
		assertThat(test, org.hamcrest.Matchers.closeTo(0.02, 0.0000001));
	}

	@Test
	public void testGetFrequency_UnknownFreqKnownRace() throws Exception {
		Double test = service.getFrequency(DetailRace.CAU, "HLA-DPB1*01:01");
		assertThat(test, org.hamcrest.Matchers.equalTo(0.0));
	}

	@Test
	public void testGetFrequency_UnknownFreqUnknownRace() throws Exception {
		Double test = service.getFrequency(DetailRace.API, "HLA-DPB1*01:01");
		assertThat(test, org.hamcrest.Matchers.equalTo(1.0E-5));
	}

}
