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

package org.nmdp.service.epitope.resource.impl;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aGenotype;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aGenotypeList;
import static org.nmdp.service.epitope.EpitopeServiceTestData.anAllele;
import static org.nmdp.service.epitope.EpitopeServiceTestData.anAlleleList;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group1Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group2Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group3Alleles;
import static org.nmdp.service.epitope.domain.DetailRace.AFA;
import static org.nmdp.service.epitope.domain.DetailRace.CAU;

import java.util.Arrays;
import java.util.List;

import org.immunogenomics.gl.Genotype;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.domain.MatchResult;
import org.nmdp.service.epitope.resource.MatchRequest;
import org.nmdp.service.epitope.resource.MatchResponse;
import org.nmdp.service.epitope.service.MatchService;

@RunWith(MockitoJUnitRunner.class)
public class MatchResourceTest {

	@Mock
	private MatchService matchService;

	@InjectMocks
	private MatchResource resource;

	@Test
	public void testGetMatches_PessimisticMatchGrade() throws Exception {
		String rgl = "01:01+02:01";
		String dgl = "03:01+04:01";
		MatchRequest request = new MatchRequest(rgl, null, dgl, null, null);
		MatchResult result = new MatchResult(MatchGrade.GVH_NONPERMISSIVE);
		when(matchService.getMatch(anyString(), any(DetailRace.class), anyString(), any(DetailRace.class))).thenReturn(result);
		List<MatchResponse> test = resource.getMatches(Arrays.asList(request));
		assertThat(test.size(), equalTo(1));
		assertThat(test.get(0).getToken(), nullValue());
		assertThat(test.get(0).getRecipient(), equalTo(rgl));
		assertThat(test.get(0).getDonor(), equalTo(dgl));
		assertThat(test.get(0).getRecipientRace(), nullValue());
		assertThat(test.get(0).getDonorRace(), nullValue());
		assertThat(test.get(0).getMatchProbability(), nullValue());
		assertThat(test.get(0).getPermissiveMismatchProbability(), nullValue());
		assertThat(test.get(0).getHvgNonPermissiveMismatchProbability(), nullValue());
		assertThat(test.get(0).getGvhNonPermissiveMismatchProbability(), nullValue());
		assertThat(test.get(0).getUnknownProbability(), nullValue());
		assertThat(test.get(0).getPessimisticMatchGrade(), equalTo(MatchGrade.GVH_NONPERMISSIVE));
	}

	@Test
	public void testGetMatches_TokenPresent() throws Exception {
		MatchRequest request = new MatchRequest("test", null, "test", null, "testToken");
		MatchResult result = new MatchResult(MatchGrade.GVH_NONPERMISSIVE);
		when(matchService.getMatch(anyString(), any(DetailRace.class), anyString(), any(DetailRace.class))).thenReturn(result);
		List<MatchResponse> test = resource.getMatches(Arrays.asList(request));
		assertThat(test.size(), equalTo(1));
		assertThat(test.get(0).getToken(), equalTo("testToken"));
		assertThat(test.get(0).getRecipient(), nullValue());
		assertThat(test.get(0).getRecipientRace(), nullValue());
		assertThat(test.get(0).getDonor(), nullValue());
		assertThat(test.get(0).getDonorRace(), nullValue());
	}

	@Test
	public void testGetMatches_Probabilities() throws Exception {
		String rgl = "01:01+02:01";
		String dgl = "03:01+04:01";
		MatchRequest request = new MatchRequest(rgl, DetailRace.CAU, dgl, DetailRace.AFA, null);
		MatchResult result = new MatchResult(0.01, 0.02, 0.03, 0.04, 0.05);
		when(matchService.getMatch(anyString(), any(DetailRace.class), anyString(), any(DetailRace.class))).thenReturn(result);
		List<MatchResponse> test = resource.getMatches(Arrays.asList(request));
		assertThat(test.size(), equalTo(1));
		assertThat(test.get(0).getToken(), nullValue());
		assertThat(test.get(0).getRecipient(), equalTo(rgl));
		assertThat(test.get(0).getDonor(), equalTo(dgl));
		assertThat(test.get(0).getRecipientRace(), equalTo(CAU));
		assertThat(test.get(0).getDonorRace(), equalTo(AFA));
		assertThat(test.get(0).getMatchProbability(), equalTo(result.getMatchProbability()));
		assertThat(test.get(0).getPermissiveMismatchProbability(), 
				equalTo(result.getPermissiveMismatchProbability()));
		assertThat(test.get(0).getHvgNonPermissiveMismatchProbability(), 
				equalTo(result.getHvgNonPermissiveMismatchProbability()));
		assertThat(test.get(0).getGvhNonPermissiveMismatchProbability(), 
				equalTo(result.getGvhNonPermissiveMismatchProbability()));
		assertThat(test.get(0).getUnknownProbability(), 
				equalTo(result.getUnknownProbability()));
		assertThat(test.get(0).getPessimisticMatchGrade(), nullValue());
	}

}
