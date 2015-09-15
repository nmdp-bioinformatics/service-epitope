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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aGenotype;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aGenotypeList;
import static org.nmdp.service.epitope.EpitopeServiceTestData.aLocus;
import static org.nmdp.service.epitope.EpitopeServiceTestData.anAlleleList;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestEpitopeService;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestGlClient;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getTestGlStringFilter;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group1Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group2Alleles;
import static org.nmdp.service.epitope.EpitopeServiceTestData.group3Alleles;
import static org.nmdp.service.epitope.domain.DetailRace.CAU;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.gl.GenotypeList;
import org.nmdp.gl.client.GlClient;
import org.nmdp.service.epitope.EpitopeServiceTestData;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.domain.MatchResult;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class MatchServiceImplTest {

	@Mock
	private FrequencyService freqService;
	
	private GlClient glClient;

	@Mock
	private Function<String, GenotypeList> glResolver;

	private Function<String, String> glStringFilter;
	
	private MatchServiceImpl service;

	@Before
	public void setUp() throws Exception {
		glClient = getTestGlClient();
		glStringFilter = getTestGlStringFilter();
		service = new MatchServiceImpl(getTestEpitopeService(), glResolver, glClient, glStringFilter, freqService, 0.01, 1.0E-5);
		when(glClient.createLocus("HLA-DPB1")).thenReturn(aLocus());
		when(freqService.getFrequency(anyString(), any(DetailRace.class))).thenReturn(1E-5);
	}
		
	@Test
	public void testGetLowGroup() throws Exception {
		AllelePair pair = EpitopeServiceTestData.aHeterozygousAllelePair();
		assertThat(pair.getLowG(), equalTo(1));
	}
	
	@Test
	public void testGetLowGroup_Homo() throws Exception {
		AllelePair pair = EpitopeServiceTestData.aHomozygousAllelePair();
		assertThat(pair.getLowG(), equalTo(1));
	}
	
	@Test
	public void testGetAllelePairs() throws Exception {
		Map<AllelePair, Double> allelePairs = service.getAllelePairs(aGenotypeList(), CAU);
		// aGenotypeList contains 3 types on each side with no overlaps 
		assertThat(allelePairs.size(), equalTo(9));
	}
	
	@Test
	public void testGetMatchGrade() throws Exception {
		AllelePair rp = new AllelePair(group1Alleles().get(0), 1, group2Alleles().get(0), 2, CAU);
		AllelePair dp = new AllelePair(group2Alleles().get(0), 2, group3Alleles().get(0), 3, CAU);
		service = new MatchServiceImpl(getTestEpitopeService(), glResolver, glClient, glStringFilter, freqService, 0.01, 1.0E-5);
		assertThat(service.getMatchGrade(rp, dp), equalTo(MatchGrade.GVH_NONPERMISSIVE));
	}

	@Test
	public void testGetMatch_Ambig_P_HVG_GVH() throws Exception {
		GenotypeList rgl = new GenotypeList("1", aGenotype(
				anAlleleList(
						group1Alleles().get(0), 
						group2Alleles().get(0)),
				anAlleleList(
						group2Alleles().get(0), 
						group3Alleles().get(0))));
		GenotypeList dgl = new GenotypeList("1", aGenotype( 
				anAlleleList(
						group1Alleles().get(1), 
						group2Alleles().get(1)),
				anAlleleList(
						group2Alleles().get(1), 
						group3Alleles().get(1))));
		when(freqService.getFrequency(anyString(), any(DetailRace.class))).thenReturn(1.0E-5);
		MatchResult test = service.getMatch(rgl, null, dgl, null);
		assertThat(test.getMatchGrade(), equalTo(MatchGrade.POTENTIAL));
	}
	
	@Test
	public void testGetMatch_Ambig_A_P_HVG_GVH() throws Exception {
		GenotypeList rgl = new GenotypeList("1", aGenotype(
				anAlleleList(
						group1Alleles().get(0),
						group3Alleles().get(0)),
				anAlleleList(
						group1Alleles().get(0), 
						group2Alleles().get(0))));
		GenotypeList dgl = new GenotypeList("1", aGenotype( 
				anAlleleList(
						group3Alleles().get(0)), 
				anAlleleList(
						group1Alleles().get(0), 
						group2Alleles().get(0))));
		when(freqService.getFrequency(anyString(), any(DetailRace.class))).thenReturn(1.0E-5);
		MatchResult test = service.getMatch(rgl, null, dgl, null);
		assertThat(test.getMatchGrade(), equalTo(MatchGrade.POTENTIAL));
	}
	
	@Test
	public void testGetMatch_HVG_GVH() throws Exception {
		GenotypeList rgl = new GenotypeList("1", aGenotype(
				anAlleleList(
						group3Alleles().get(0)),
				anAlleleList(
						group1Alleles().get(0), 
						group3Alleles().get(0))));
		GenotypeList dgl = new GenotypeList("1", aGenotype( 
				anAlleleList(
						group2Alleles().get(0)), 
				anAlleleList(
						group2Alleles().get(0))));
		when(freqService.getFrequency(anyString(), any(DetailRace.class))).thenReturn(1.0E-5);
		MatchResult test = service.getMatch(rgl, null, dgl, null);
		assertThat(test.getMatchGrade(), equalTo(MatchGrade.NONPERMISSIVE_UNDEFINED));
	}
	
}
