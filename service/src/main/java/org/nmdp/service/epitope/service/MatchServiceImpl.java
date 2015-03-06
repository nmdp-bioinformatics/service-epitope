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

package org.nmdp.service.epitope.service;

import static org.nmdp.service.epitope.domain.DetailRace.UNK;
import static org.nmdp.service.epitope.domain.MatchGrade.GVH_NONPERMISSIVE;
import static org.nmdp.service.epitope.domain.MatchGrade.HVG_NONPERMISSIVE;
import static org.nmdp.service.epitope.domain.MatchGrade.MATCH;
import static org.nmdp.service.epitope.domain.MatchGrade.PERMISSIVE;

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
import org.nmdp.service.epitope.freq.FrequencyResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Primary implementation of MatchService interface
 */
public class MatchServiceImpl implements MatchService {

	private EpitopeService epitopeService;
	private GlClient glClient;
	private Function<String, GenotypeList> glResolver;
	private Function<AllelePair, Double> freqResolver;
	private Function<String, String> glStringFilter;
	Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public MatchServiceImpl(
			EpitopeService epitopeService, 
			@GlResolver Function<String, GenotypeList> glResolver, 
			GlClient glClient, 
			@GlStringFilter Function<String, String> glStringFilter, 
			@FrequencyResolver Function<AllelePair, Double> freqResolver) 
	{
		this.epitopeService = epitopeService;
		this.glResolver = glResolver;
		this.freqResolver = freqResolver;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
	}
	
	Integer getLowGroup(AllelePair pair) {
		Integer g1 = epitopeService.getGroupForAllele(pair.getA1());
		Integer g2 = epitopeService.getGroupForAllele(pair.getA2());
		if (logger.isTraceEnabled()) {
			logger.trace("Groups: " + pair + " -> " + g1 + "/" + g2);
		}
		if (g1 == null || g2 == null) return null;
		return (g1.compareTo(g2) < 0) ? g1 : g2;
	}
	
	MatchGrade getMatchGrade(AllelePair recipAllelePair, AllelePair donorAllelePair) {
		// check for exact match
		if (recipAllelePair.equals(donorAllelePair)) {
			return MatchGrade.MATCH;
		}
		Integer recipLow = getLowGroup(recipAllelePair);
		Integer donorLow = getLowGroup(donorAllelePair);
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
	
	MatchResult getMatch(Set<AllelePair> ralps, Set<AllelePair> dalps) {
		double mp = 0;
		double pp = 0;
		double gvhp = 0;
		double hvgp = 0;
		double up = 0;
		boolean unknown = false;
		Set<AllelePair> isct = Sets.intersection(ralps, dalps);
		for (AllelePair p : isct) {
			Double freq = freqResolver.apply(p);
			if (freq == null) { unknown = true; freq = 1.0; }
			mp += freq * freq;
			if (logger.isTraceEnabled()) {
				logger.trace("MATCH:p:" + p + " -> " + (freq*freq));
			}
		}
		for (AllelePair rp : ralps) {
			Double rf = freqResolver.apply(rp);
			if (rf == null) { unknown = true; rf = 1.0; }
			for (AllelePair dp : dalps) {
				Double df = freqResolver.apply(dp);
				if (df == null) { unknown = true; df = 1.0; }
				MatchGrade mg = getMatchGrade(rp, dp); 
				switch (mg) {
				case MATCH:
					continue;
				case PERMISSIVE:
					pp += rf * df;
					break;
				case GVH_NONPERMISSIVE:
					gvhp += rf * df;
					break;
				case HVG_NONPERMISSIVE:
					hvgp += rf * df;
					break;
				case UNKNOWN:
					up += rf * df;
					break;
				default:
					throw new RuntimeException("unexpected match grade for recipient/donor: " + rp + "/" + dp);
				}
				if (logger.isTraceEnabled()) {
					logger.trace(mg + ":rp:" + rp + ",dp:" + dp + " -> " + rf + "*" + df + " -> " + (rf * df));
				}
			}
		}
		logger.debug("finished with\n\tmp: " + mp + "\n\tpp: " + pp + "\n\thvgp: " + hvgp + "\n\tgvhp: " + gvhp + "\n\tup: " + up);
		if (unknown) {
			return new MatchResult(null, null, null, null, null, up + gvhp > 0 ? GVH_NONPERMISSIVE : hvgp > 0 ? HVG_NONPERMISSIVE : pp > 0 ? PERMISSIVE : MATCH);
		} else {
			return new MatchResult(mp, pp, hvgp, gvhp, up, null);
		}
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
							pl.add(new AllelePair(a1, a2, race));
						}
					}
				}
			}
		}
		return pl;
	}

}
