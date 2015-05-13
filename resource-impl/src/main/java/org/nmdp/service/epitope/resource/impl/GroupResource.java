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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.nmdp.service.epitope.freq.IFrequencyResolver;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.resource.AlleleListRequest;
import org.nmdp.service.epitope.resource.AlleleView;
import org.nmdp.service.epitope.resource.GroupView;
import org.nmdp.service.epitope.service.EpitopeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("groups/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="Groups", description="Returns immunogenicity groups along with associated alleles")
public class GroupResource {

	Logger log = LoggerFactory.getLogger(getClass());
	private EpitopeService epitopeService;
	private GlClient glClient;
	private Function<String, String> glStringFilter;
    private IFrequencyResolver freqResolverImpl;
    
	@Inject
	public GroupResource(EpitopeService epitopeService, GlClient glClient, @GlStringFilter Function<String, String> glStringFilter, IFrequencyResolver freqResolverImpl) {
		this.epitopeService = epitopeService;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
		this.freqResolverImpl = freqResolverImpl;
	}
	
	private AlleleView getAlleleView(String glstring) {
		glstring = glStringFilter.apply(glstring);
		Allele allele;
		try {
			allele = glClient.createAllele(glstring);
		} catch (GlClientException e) {
			throw new RuntimeException("failed to create allele: " + glstring, e);
		}
		return new AlleleView(glstring, epitopeService.getGroupForAllele(allele), null, null);
	}
	
	private AlleleView getAlleleView(Allele allele) {
		return new AlleleView(glStringFilter.apply(allele.getGlstring()), epitopeService.getGroupForAllele(allele), null, null);
	}
	
	private List<String> convertAlleleListToStringList(List<Allele> alleleList) {
		return Lists.transform(alleleList, new Function<Allele, String>() {
			@Override public String apply(Allele allele) { 
				return allele.getGlstring(); 
			}});		
	}
	
	@GET
	@ApiOperation(value="Returns immunogenicity groups with their associated alleles",
			response = GroupView.class,
		    responseContainer = "List")
	public List<GroupView> getGroups(
			@QueryParam("alleles") 
			@ApiParam("List of alleles, separated by \",\" or \"/\"") 
			String alleles, 
            @QueryParam("groups") 
            @ApiParam("List of immunogenicity groups, separated by \",\"")
            String groups,
            @QueryParam("race") 
            @ApiParam("Race code for which to determine group probability")
            DetailRace race)
	{
		log.debug("getGroups(" + alleles + ", " + groups + ")");
		if (null == alleles && null == groups) 
		{
		    if (null != race) {
		        throw new IllegalStateException("race argument requires alleles to be provided");
		    }
			return FluentIterable.from(epitopeService.getAllGroups().entrySet())
					.transform(new Function<Map.Entry<Integer, List<Allele>>, GroupView>() {
						@Override
						public GroupView apply(Entry<Integer, List<Allele>> entry) {
							return new GroupView(entry.getKey(), null, null, convertAlleleListToStringList(entry.getValue()));
						}})
					.toList();
		}
		List<String> alleleList = new ArrayList<>();
		if (alleles != null) addToList(alleleList, parseAlleles(alleles));
		List<Integer> groupList = new ArrayList<>();
		if (groups != null) {
			for (String g : Splitter.on(",").splitToList(groups)) {
				groupList.add(new Integer(g));
			}
		}
		return getGroups(alleleList, groupList, race);
	}

    private static void addToList(List<String> l, String s) {
        if (null != s && !l.contains(s)) l.add(s);
    }

    private static void addToList(List<String> l, Iterable<String> i) {
        for (String s : i) {
            addToList(l, s);
        }
    }
    
	private Iterable<String> parseAlleles(String alleles) {
		if (alleles.contains(",")) { 
			return Splitter.on(",").splitToList(alleles);
		} else if (alleles.contains("/")) {
			return Splitter.on("/").splitToList(alleles);
		}
		return Arrays.asList(alleles);
	}
		
	
	@POST
	@ApiOperation(value="Returns immunogenicity groups with their associated alleles",
			response = GroupView.class,
		    responseContainer = "List")
	@ApiImplicitParam(paramType="body", dataType="AlleleListRequest")
    @Consumes(MediaType.APPLICATION_JSON)
	public List<GroupView> getGroups(
			@ApiParam(value="request filter") // needed for description (ignored if placed above)
			AlleleListRequest request) 
	{
		List<String> alleleList = new ArrayList<>();
		if (request.getAlleles() != null) alleleList.addAll(request.getAlleles());
		List<Integer> groupList = new ArrayList<>();
		if (request.getGroups() != null) groupList.addAll(request.getGroups());
		return getGroups(alleleList, groupList, request.getRace());
	}
	
	private List<GroupView> getGroups(Iterable<String> alleles, Iterable<Integer> groups, DetailRace race) {
		log.debug("getGroups(" + alleles + ", " + groups + ")");
		List<AlleleView> returnAlleleList = new ArrayList<>();
		returnAlleleList.addAll(getAllelesByAllele(alleles));
		log.debug("after alleles: " + returnAlleleList);
		returnAlleleList.addAll(getAllelesByGroup(groups));
		log.debug("after groups: " + returnAlleleList);
		return convertAlleleListToGroupList(returnAlleleList, race);		
	}
	
	private List<AlleleView> getAllelesByAllele(Iterable<String> alleleList) {
		List<AlleleView> returnList = new ArrayList<>();
		for (String allele : alleleList) {
			returnList.add(getAlleleView(allele));
		}
		log.debug("getAllelesByAllele(" + alleleList + "): " + returnList);
		return returnList;
	}
	
	private List<AlleleView> getAllelesByAllele2(List<Allele> alleleList) {
		List<AlleleView> returnList = new ArrayList<>();
		for (Allele allele : alleleList) {
			returnList.add(getAlleleView(allele));
		}
		log.debug("getAllelesByAllele(" + alleleList + "): " + returnList);
		return returnList;
	}

	private List<AlleleView> getAllelesByGroup(Iterable<Integer> groupList) {
		List<AlleleView> returnList = new ArrayList<>();
		for (Integer group : groupList) {
			returnList.addAll(getAllelesByAllele2(epitopeService.getAllelesForGroup(group)));
		}
		log.debug("getAllelesByGroup(" + groupList + "): " + returnList);
		return returnList;
	}

	private List<GroupView> convertAlleleListToGroupList(List<AlleleView> alleleList, DetailRace race) {
		Map<Integer, GroupView> groupMap = new HashMap<>();
		for (AlleleView allele : alleleList) {
			Integer id = allele.getGroup();
			GroupView group = groupMap.get(id);
			if (group == null) {
				group = new GroupView(id, race, null, new ArrayList<String>());
				groupMap.put(id, group);
			}
			if (!group.getAlleleList().contains(allele.getAllele())) group.getAlleleList().add(allele.getAllele());
		}
        if (race != null) {
            groupMap = calculateGroupProbabilities(groupMap);
        }
		return new ArrayList<>(groupMap.values());
	}
	
	class DoubleContainer {
	    double d;
        public double get() { return d; }
        public void set(double d) { this.d = d; }
	    public void add(double d) { this.d += d; }
	    public void subtract(double d) { this.d -= d; }
	}
	
	private Map<Integer, GroupView> calculateGroupProbabilities(Map<Integer, GroupView> groupMap) {
	    Map<Integer, DoubleContainer> groupFreqMap = new HashMap<>();
	    for (GroupView groupView : groupMap.values()) {
	        DoubleContainer dc = new DoubleContainer();
	        groupFreqMap.put(groupView.getGroup(), dc);
	        if (groupView.getRace() == null) {
	            log.debug("race missing for group (skipping probabilities: " + groupView.getGroup());
	            return groupMap;
	        }
	        for (String a : groupView.getAlleleList()) {
	            dc.add(freqResolverImpl.getFrequency(a, groupView.getRace()));
	        }
	    }
	    double total = 0; 
	    for (DoubleContainer dc : groupFreqMap.values()) {
	        total += dc.get();
	    }
        Map<Integer, GroupView> newMap = new HashMap<>();
	    for (GroupView groupView : groupMap.values()) {
	        double probability = groupFreqMap.get(groupView.getGroup()).get() / total;
	        probability = round(probability);
	        newMap.put(groupView.getGroup(), new GroupView(groupView.getGroup(), groupView.getRace(), probability, groupView.getAlleleList()));
	    }
        return newMap;
    }

	private double round(double d) {
	    return (double)Math.round(d * 1000) / 1000;
	}
	
    @GET
    @Path("{group}")
	@ApiOperation(value="Returns immunogenicity group with its associated alleles", response=GroupView.class)
	public GroupView getGroup(@PathParam("group") String group) {
		Integer gid = new Integer(group);
		List<String> alleleList = convertAlleleListToStringList(epitopeService.getAllelesForGroup(new Integer(group))); 
		return new GroupView(gid, null, null, alleleList);
	}

}
