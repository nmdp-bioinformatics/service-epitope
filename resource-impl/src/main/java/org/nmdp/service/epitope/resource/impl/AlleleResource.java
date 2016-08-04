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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.gl.filter.GlstringFilter;
import org.nmdp.service.epitope.resource.AlleleListRequest;
import org.nmdp.service.epitope.resource.AlleleView;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.FrequencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("alleles/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="Alleles", description="Returns alleles along with their associated immunogenicity groups")
public class AlleleResource {

	Logger log = LoggerFactory.getLogger(getClass());
	private EpitopeService epitopeService;
	private GlClient glClient;
	private Function<String, String> glStringFilter;
	private FrequencyService freqService;

	@Inject
	public AlleleResource(EpitopeService epitopeService, GlClient glClient, @GlstringFilter Function<String, String> glStringFilter, FrequencyService freqService) {
		this.epitopeService = epitopeService;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
		this.freqService = freqService;
    }
	
	@GET
	@ApiOperation(value="Returns alleles with their associated immunogenicity groups",
			response = AlleleView.class,
		    responseContainer = "List")
	public List<AlleleView> getAlleles(
			@QueryParam("alleles") 
			@ApiParam("List of alleles, separated by \",\" or \"/\"") 
			String alleles, 
			@QueryParam("groups") 
			@ApiParam("List of immunogenicity groups, separated by \",\"")
			String groups,
            @QueryParam("race") 
            @ApiParam("Race code for which to determine allele frequencies")
            final DetailRace race)
	{
		if (null == alleles && null == groups) 
		{
			return epitopeService.getAllAlleles().stream().map(a -> getAlleleView(a, race)).collect(Collectors.toList());
		}
		List<AlleleView> returnList = new ArrayList<>();
		if (alleles != null) addToList(returnList, getAlleleViews(alleles, race));
		if (groups != null) {
			addToList(returnList, getAllelesForGroupStrings(Splitter.on(",").split(groups), race));
		}
		return returnList;
	}

	@POST
    @Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Returns alleles with their associated immunogenicity groups",
			response = AlleleView.class,
		    responseContainer = "List")
	@ApiImplicitParam(paramType="body", dataType="AlleleListRequest")
	public List<AlleleView> getAlleles(
			@ApiParam("Request filter") // needed for description (ignored if placed above)
			AlleleListRequest request) 
	{
		List<AlleleView> returnList = new ArrayList<>();
		if (request.getAlleles() != null) {
			addToList(returnList, getAlleleViews(request.getAlleles(), request.getRace()));
		}
		if (request.getGroups() != null) {
			addToList(returnList, getAllelesForGroups(request.getGroups(), request.getRace()));
		}
		return returnList;
	}

	@GET
    @Path("{allele}")
	@ApiOperation(value="Returns allele with its associated immunogenicity group", response=AlleleView.class)
	public AlleleView getAllele(
			@PathParam("allele") 
			@ApiParam("GL string for an allele")
			String allele,
            @QueryParam("race") 
            @ApiParam("Race code for which to determine allele frequencies")
            DetailRace race) 
	{
		return getAlleleView(allele, race);
	}

	private AlleleView getAlleleView(String glString, Integer group, DetailRace race, String error) {
        Double frequency = (null == race) ? null : freqService.getFrequency(race, glString);
        if (null != frequency) frequency = round(frequency);
        return new AlleleView(glString, group, race, frequency, error);
	}
	 
    private double round(double d) {
        return (double)Math.round(d * 100000) / 100000;
    }
    
	private AlleleView getAlleleView(String glstring, DetailRace race) {
		Allele allele = null;
		try {
			allele = glClient.createAllele(glStringFilter.apply(glstring));
		} catch (GlClientException e) {
			throw new RuntimeException("failed to create allele: " + glstring, e);
		}
		return getAlleleView(allele, race);
	}

	private AlleleView getAlleleView(Allele allele, DetailRace race) {
        Integer group = null;
        String error = null;
		try {
			group = epitopeService.getGroupForAllele(allele);
		} catch (Exception e) {
			error = e.getMessage();
		}
        // permit null groups in the epitope service to indicate that no tce group is known.
        // note: this is different from a tce group of 0, used to indicate null alleles.
        //
        // if (null == group) {
		//     throw new RuntimeException("unknown group for allele: " + allele);
		// }
        return getAlleleView(allele.getGlstring(), group, race, error);
	}
	
	private Iterable<AlleleView> getAlleleViews(String alleles, final DetailRace race) {
	    alleles = alleles.replace(',', '/');
	    List<Allele> al;
	    try {
	        al = glClient.createAlleleList(glStringFilter.apply(alleles)).getAlleles();
	    } catch (GlClientException e) {
	        throw new RuntimeException("failed to create allele list: " + glStringFilter.apply(alleles), e);
	    }
	    return Iterables.transform(al, a -> getAlleleView(a.getGlstring(), race));
	}
	
    private Iterable<AlleleView> getAlleleViews(Iterable<String> alleles, DetailRace race) {
        List<AlleleView> alleleViewList = new ArrayList<>();
        for(String allele : alleles) {
            addToList(alleleViewList, getAlleleViews(allele, race));            
        }
        return alleleViewList;
    }
    
	private List<AlleleView> getAllelesForGroupStrings(Iterable<String> groups, DetailRace race) {
		Iterable<Integer> groupInts = Iterables.transform(groups, s -> new Integer(s));
		return getAllelesForGroups(groupInts, race);
	}

	private List<AlleleView> getAllelesForGroups(Iterable<Integer> groups, DetailRace race) {
		return StreamSupport.stream(groups.spliterator(), false)
		        .map(group -> 
		        	epitopeService.getAllelesForGroup(group).stream()
		        		.map(a -> getAlleleView(a.getGlstring(), group, race, null)))
		        .flatMap(s -> s)
		        .collect(Collectors.toList());
	}

	private static void addToList(List<AlleleView> list, AlleleView t) {
		if (null != t && !list.contains(t)) list.add(t);
	}

	private static void addToList(List<AlleleView> list, Iterable<AlleleView> ai) {
		for (AlleleView a : ai) {
			addToList(list, a);
		}
	}
		
}
