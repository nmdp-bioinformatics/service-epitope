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

import static org.nmdp.service.epitope.domain.DetailRace.UNK;
import static org.nmdp.service.epitope.domain.MatchGrade.GVH_NONPERMISSIVE;
import static org.nmdp.service.epitope.domain.MatchGrade.HVG_NONPERMISSIVE;
import static org.nmdp.service.epitope.domain.MatchGrade.MATCH;
import static org.nmdp.service.epitope.domain.MatchGrade.PERMISSIVE;
import static org.nmdp.service.epitope.domain.MatchGrade.UNKNOWN;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.AlleleList;
import org.immunogenomics.gl.Genotype;
import org.immunogenomics.gl.GenotypeList;
import org.immunogenomics.gl.Haplotype;
import org.immunogenomics.gl.Locus;
import org.immunogenomics.gl.client.GlClient;
import org.immunogenomics.gl.client.GlClientException;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.domain.MatchResult;
import org.nmdp.service.epitope.freq.DbiFrequencyResolver;
import org.nmdp.service.epitope.freq.FrequencyResolver;
import org.nmdp.service.epitope.freq.IFrequencyResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.trace.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * Primary implementation of MatchService interface
 */
public class MatchServiceImpl implements MatchService {

	private EpitopeService epitopeService;
	private GlClient glClient;
	private Function<String, GenotypeList> glResolver;
    private Function<AllelePair, Double> freqResolver;
    private IFrequencyResolver freqResolverImpl;
	private Function<String, String> glStringFilter;
	Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public MatchServiceImpl(
			EpitopeService epitopeService, 
			@GlResolver Function<String, GenotypeList> glResolver, 
			GlClient glClient, 
			@GlStringFilter Function<String, String> glStringFilter, 
			@FrequencyResolver Function<AllelePair, Double> freqResolver, // supports caching
			IFrequencyResolver freqResolverImpl) 
	{
		this.epitopeService = epitopeService;
		this.glResolver = glResolver;
        this.freqResolver = freqResolver;
        this.freqResolverImpl = freqResolverImpl;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
	}
	
	MatchGrade getMatchGrade(AllelePair recipAllelePair, AllelePair donorAllelePair) {
		// check for exact match
		if (recipAllelePair.typeEquals(donorAllelePair)) {
			return MatchGrade.MATCH;
		}
		Integer recipLow = recipAllelePair.getLowG();
		Integer donorLow = donorAllelePair.getLowG();
		MatchGrade matchGrade = null;
		if (recipLow == null || donorLow == null) {
			matchGrade = MatchGrade.UNKNOWN;
		} else if (recipLow.compareTo(donorLow) == 0) {
			matchGrade = MatchGrade.PERMISSIVE;
		} else {
			matchGrade = (recipLow.compareTo(donorLow) < 0) 
					? MatchGrade.GVH_NONPERMISSIVE : MatchGrade.HVG_NONPERMISSIVE;
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Matched:rp:" + recipAllelePair + ",dp:" + donorAllelePair 
					+ " -> " + recipLow + "/" + donorLow + " -> " + matchGrade);
		}
		return matchGrade;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MatchResult getMatch(String recipientGl, DetailRace recipientRace,
			String donorGl, DetailRace donorRace) 
	{
		GenotypeList rgl = glResolver.apply(glStringFilter.apply(recipientGl));
		GenotypeList dgl = glResolver.apply(glStringFilter.apply(donorGl));
		return getMatch(rgl, recipientRace, dgl, donorRace);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MatchResult getMatch(GenotypeList recipientGl, DetailRace recipRace,
			GenotypeList donorGl, DetailRace donorRace) 
	{
		if (recipRace == null) recipRace = UNK;
		if (donorRace == null) donorRace = UNK;
		Set<AllelePair> ralps = getAllelePairs(recipientGl, recipRace);
		Set<AllelePair> dalps = getAllelePairs(donorGl, donorRace);
		return getMatch(ralps, dalps);
	}

//    private void traceAllelePair(String label, AllelePair p, Double freq) {
//        boolean ist = Trace.isEnabled();
//        StringBuffer sb = new StringBuffer(label);
//        sb.append(p.getA1()).append(":")
//            .append(freqResolver.getFrequency(p.getA1().getGlstring(), p.getRace()))
//            .append("+")
//            .append(p.getA1()).append(":")
//            .append(freqResolver.getFrequency(p.getA2().getGlstring(), p.getRace()))
//            .append(" -> ")
//            .append(freq);
//        Trace.add(sb.toString());
//    }

    private String getAllelePairTrace(AllelePair p) {
        StringBuffer sb = new StringBuffer()
            .append(p.getA1()).append("(g:").append(p.getG1()).append(",p:")
            .append(freqResolverImpl.getFrequency(p.getA1().getGlstring(), p.getRace()))
            .append(")+")
            .append(p.getA2()).append("(g:").append(p.getG2()).append(",p:")
            .append(freqResolverImpl.getFrequency(p.getA2().getGlstring(), p.getRace()))
            .append(")");
        return sb.toString();
    }
    
    private String getMatchTrace(AllelePair rp, AllelePair dp, MatchGrade mg, Double p) {
        StringBuffer sb = new StringBuffer("r:")
            .append(getAllelePairTrace(rp))
            .append(",d:")
            .append(getAllelePairTrace(dp))
            .append(",m:")
            .append(mg)
            .append("(p:")
            .append(p)
            .append(")");
        return sb.toString();
    }
	
	MatchResult getMatch(Set<AllelePair> ralps, Set<AllelePair> dalps) {
	    EnumMap<MatchGrade, Double> pmap = new EnumMap<MatchGrade, Double>(MatchGrade.class);
	    for (MatchGrade grade : MatchGrade.values()) {
	        pmap.put(grade, 0.0);
	    }
		MatchGrade grade = null;
		for (AllelePair rp : ralps) {
			Double rf = freqResolver.apply(rp);
			if (rf == 0.0) {
			    if (Trace.isEnabled()) Trace.add("r:" + getAllelePairTrace(rp) + " (dropped)");
			    continue;
			}
			for (AllelePair dp : dalps) {
				Double df = freqResolver.apply(dp);
				if (df == 0.0) {
	                if (Trace.isEnabled()) Trace.add("d:" + getAllelePairTrace(dp) + " (dropped)");
				    continue;
				}
				grade = getMatchGrade(rp, dp);
				if (Trace.isEnabled()) Trace.add(getMatchTrace(rp, dp, grade, rf*df));
				Double p = pmap.get(grade);
				p = (null == p) ? new Double(p) : new Double(rf * df + p);
				pmap.put(grade, p);
//				if (logger.isTraceEnabled()) {
//					logger.trace(grade + ":rp:" + rp + ",dp:" + dp + " -> " + rf + "*" + df + " -> " + (rf * df));
//				}
			}
		}
		logger.debug("finished with: " + pmap);
		grade = UNKNOWN;
        if (pmap.get(MATCH) > 0.0) grade = MATCH;
        if (pmap.get(PERMISSIVE) > 0.0) grade = PERMISSIVE;
        if (pmap.get(HVG_NONPERMISSIVE) > 0.0) grade = HVG_NONPERMISSIVE;
        if (pmap.get(GVH_NONPERMISSIVE) > 0.0) grade = GVH_NONPERMISSIVE;
		// return both probabilities and match grade
		return new MatchResult(
		        pmap.get(MATCH),
		        pmap.get(PERMISSIVE),
		        pmap.get(HVG_NONPERMISSIVE),
		        pmap.get(GVH_NONPERMISSIVE),
		        pmap.get(UNKNOWN),
		        grade);
	}

	Set<AllelePair> getAllelePairs(GenotypeList gl, DetailRace race) {
		Locus dpb1 = null;
		try {
			dpb1 = glClient.createLocus("HLA-DPB1");
		} catch (GlClientException e) {
			throw new RuntimeException("unable to create DPB1 locus", e);
		}
		Set<AllelePair> pl = new HashSet<>();
		for (Genotype g : gl.getGenotypes()) {
			List<Haplotype> hl = g.getHaplotypes();
			Haplotype h1 = null, h2 = null;
			switch (hl.size()) {
			case 0:
				throw new RuntimeException("no haplotypes found for gl: " + gl);
			case 2:
				h1 = hl.get(0);
				h2 = hl.get(1);
				break;
			case 1:
				h1 = hl.get(0);
				h2 = hl.get(0);
				break;
			default:
				throw new RuntimeException("only expecting 2 haplotypes for gl: " + gl);
			}
			for (AlleleList al1 : h1.getAlleleLists()) {
				for(Allele a1 : al1.getAlleles()) {
					if (!a1.getLocus().equals(dpb1)) continue;
					for (AlleleList al2 : h2.getAlleleLists()) {
						for (Allele a2 : al2.getAlleles()) {
							if (!a2.getLocus().equals(dpb1)) continue;
							pl.add(new AllelePair(a1, epitopeService.getGroupForAllele(a1), a2, epitopeService.getGroupForAllele(a2), race));
						}
					}
				}
			}
		}
		return pl;
	}

}
