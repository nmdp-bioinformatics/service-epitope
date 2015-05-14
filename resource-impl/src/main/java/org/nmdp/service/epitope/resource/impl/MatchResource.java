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

package org.nmdp.service.epitope.resource.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.nmdp.service.epitope.resource.MatchRequest;
import org.nmdp.service.epitope.resource.MatchResponse;
import org.nmdp.service.epitope.service.MatchService;
import org.nmdp.service.epitope.trace.Trace;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("matches/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="Matches", description="Returns DPB1 matches based on recipient and donor genotypes.")
public class MatchResource {

	MatchService matchService;

	@Inject
	public MatchResource(MatchService matchService) {
		this.matchService = matchService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Returns list of match results corrolated by token (or genotypes, if no token is provided).", 
			notes = "The reported match grade is the most optimistic outcome possible with a probability greater than 0.01.",
			response = MatchResponse.class,
		    responseContainer = "List")
	@ApiImplicitParams({
	    @ApiImplicitParam(paramType="body", dataType="MatchRequest", value="list of match requests"), // need something to generate items in spec
	    @ApiImplicitParam(paramType="query", name="trace", value="trace detail", required=false, dataType = "boolean")	
	})
	public List<MatchResponse> getMatches(
			@ApiParam(value="List of match requests for which to create match results") // needed for description (ignored if placed above)
			List<MatchRequest> matchRequestList,
	        @ApiParam(value="Optional request for result trace detail", required=false) 
			@QueryParam("trace") 
            Boolean traceEnabled) 
	{
	    List<MatchResponse> matchResultList = new ArrayList<>();
	    if (null != traceEnabled && traceEnabled == Boolean.TRUE) {
	        Trace.enable();
	    }
	    try {
    		for (MatchRequest request : matchRequestList) {
    			org.nmdp.service.epitope.domain.MatchResult matchResult = matchService.getMatch(request.getRecipient(), request.getRecipientRace(), request.getDonor(), request.getDonorRace());
    			List<String> trace = Trace.getTrace();
    			Trace.reset();
    			if (request.getToken() == null || request.getToken().equals("")) {
    				matchResultList.add(new MatchResponse(
    						request.getRecipient(), 
    						request.getRecipientRace(),
    						request.getDonor(), 
    						request.getDonorRace(),
    						matchResult,
    						trace));
    			} else {
    				matchResultList.add(new MatchResponse(request.getToken(), matchResult, trace));
    			}
    		}
	    } finally {
	        Trace.disable();
	    }
		return matchResultList;
	}
}
