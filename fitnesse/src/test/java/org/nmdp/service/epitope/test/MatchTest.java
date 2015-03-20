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

package org.nmdp.service.epitope.test;

import java.util.Arrays;

import org.nmdp.service.epitope.client.EndpointUrl;
import org.nmdp.service.epitope.client.EpitopeService;
import org.nmdp.service.epitope.client.EpitopeServiceModule;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.resource.MatchRequest;
import org.nmdp.service.epitope.resource.MatchResponse;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MatchTest {

	org.nmdp.service.epitope.client.EpitopeService service;

	String recipientGl;
	DetailRace recipientRace;
	String donorGl;
	DetailRace donorRace;
	
	MatchRequest matchRequest;
	MatchResponse matchResponse;
	
	public MatchTest() {
		this("http://epearsonone:8080/matches");
	}
	
	public MatchTest(final String endpointUrl) {
        Injector injector = Guice.createInjector(new EpitopeServiceModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class).annotatedWith(EndpointUrl.class).toInstance(endpointUrl);
            }
        });
        this.service = injector.getInstance(EpitopeService.class);
	}
	
    private DetailRace getDetailRace(String s) {
    	return s == null || s.equals("") ? null : DetailRace.valueOf(s);
    }
 	
	public void setRecipGl(String recipientGl) {
		this.recipientGl = recipientGl;
	}

	public void setRecipRace(String recipientRace) {
		this.recipientRace = getDetailRace(recipientRace);
	}
	
	public void setDonorGl(String donorGl) {
		this.donorGl = donorGl;
	}

	public void setDonorRace(String donorRace) {
		this.donorRace = getDetailRace(donorRace);
	}

	public void execute() {
		matchRequest = new MatchRequest(recipientGl, recipientRace, donorGl, donorRace, null);
		matchResponse = service.getMatches(Arrays.asList(matchRequest)).get(0);
	}

	public void reset() {
		this.recipientGl = null;
		this.recipientRace = null;
		this.donorGl = null;
		this.donorRace = null;
		this.matchRequest = null;
		this.matchResponse = null;
	}
	
	public MatchGrade matchGrade() {
		return null == matchResponse ? null : matchResponse.getMatchGrade();
	}
	
	public Double matchPct() {
		return null == matchResponse ? null : matchResponse.getMatchProbability();
	}
	
	public Double permPct() {
		return null == matchResponse ? null : matchResponse.getPermissiveMismatchProbability();
	}
	
	public Double hvgPct() {
		return null == matchResponse ? null : matchResponse.getHvgNonPermissiveMismatchProbability();
	}
	
	public Double gvhPct() {
		return null == matchResponse ? null : matchResponse.getGvhNonPermissiveMismatchProbability();
	}
	
	public Double unknownPct() {
		return null == matchResponse ? null : matchResponse.getUnknownProbability();
	}
	
}
