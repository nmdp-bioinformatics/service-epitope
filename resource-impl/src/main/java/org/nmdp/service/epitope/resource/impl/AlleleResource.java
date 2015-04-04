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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.client.GlClient;
import org.immunogenomics.gl.client.GlClientException;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.resource.AlleleListRequest;
import org.nmdp.service.epitope.resource.AlleleView;
import org.nmdp.service.epitope.service.EpitopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("alleles/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="Alleles", description="Returns alleles along with their associated immunogenicity groups")
public class AlleleResource {

	Logger log = LoggerFactory.getLogger(getClass());
	private EpitopeService epitopeService;
	private GlClient glClient;
	private Function<String, String> glStringFilter;

	@Inject
	public AlleleResource(EpitopeService epitopeService, GlClient glClient, @GlStringFilter Function<String, String> glStringFilter) {
		this.epitopeService = epitopeService;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
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
			String groups)
	{
		if (null == alleles && null == groups) 
		{
			return Lists.transform(epitopeService.getAllAlleles(), new Function<Allele, AlleleView>() {
				@Override public AlleleView apply(Allele allele) { return getAlleleView(allele); }
			});
		}
		List<AlleleView> returnList = new ArrayList<>();
		if (alleles != null) addToList(returnList, getAlleleViews(alleles));
		if (groups != null) {
			addToList(returnList, getAllelesForGroupStrings(Splitter.on(",").split(groups)));
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
			addToList(returnList, getAlleleViews(request.getAlleles()));
		}
		if (request.getGroups() != null) {
			addToList(returnList, getAllelesForGroups(request.getGroups()));
		}
		return returnList;
	}

	@GET
    @Path("{allele}")
	@ApiOperation(value="Returns allele with its associated immunogenicity group", response=AlleleView.class)
	public AlleleView getAllele(
			@PathParam("allele") 
			@ApiParam("GL string for an allele")
			String allele) 
	{
		return getAlleleView(allele);
	}

	private AlleleView getAlleleView(String glstring) {
		Allele allele = null;
		try {
			allele = glClient.createAllele(glStringFilter.apply(glstring));
		} catch (GlClientException e) {
			throw new RuntimeException("failed to create allele: " + glstring, e);
		}
		Integer group = epitopeService.getGroupForAllele(allele);
		if (null == group) {
			throw new RuntimeException("unknown group for allele: " + glstring);
		}
		return new AlleleView(allele.getGlstring(), group);
	}

	private AlleleView getAlleleView(Allele allele) {
		return new AlleleView(allele.getGlstring(), epitopeService.getGroupForAllele(allele));
	}
	
	private Iterable<AlleleView> getAlleleViews(String alleles) {
	    alleles = alleles.replace(',', '/');
	    List<Allele> al;
	    try {
	        al = glClient.createAlleleList(glStringFilter.apply(alleles)).getAlleles();
	    } catch (GlClientException e) {
	        throw new RuntimeException("failed to create allele list: " + glStringFilter.apply(alleles), e);
	    }
	    return Iterables.transform(al, new Function<Allele, AlleleView>() {
	        @Override public AlleleView apply(Allele allele) { return getAlleleView(allele.getGlstring()); }
	    });
	}
	
	private Iterable<AlleleView> getAlleleViews(Iterable<String> alleles) {
		List<AlleleView> alleleViewList = new ArrayList<>();
		for(String allele : alleles) {
			addToList(alleleViewList, getAlleleViews(allele));			
		}
		return alleleViewList;
	}
	
	private List<AlleleView> getAllelesForGroupStrings(Iterable<String> groups) {
		Iterable<Integer> groupInts = Iterables.transform(groups, new Function<String, Integer>() {
			@Override public Integer apply(String string) { return new Integer(string); }
		});
		return getAllelesForGroups(groupInts);
	}

	private List<AlleleView> getAllelesForGroups(Iterable<Integer> groups) {
		List<AlleleView> returnList = new ArrayList<>();
		for (Integer group : groups) {
			List<Allele> alleleList = epitopeService.getAllelesForGroup(group);
			for (Allele allele : alleleList) {
				returnList.add(new AlleleView(allele.getGlstring(), group));
			}
		}
		return returnList;
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
