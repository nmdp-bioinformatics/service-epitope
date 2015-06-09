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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nmdp.gl.Allele;
import org.nmdp.gl.Genotype;
import org.nmdp.gl.GenotypeList;
import org.nmdp.gl.Haplotype;
import org.nmdp.gl.Locus;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.domain.MatchResult;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;
import org.nmdp.service.epitope.guice.ConfigurationBindings.MatchGradeThreshold;
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
    private FrequencyService freqService;
	private Function<String, String> glStringFilter;
	private double matchGradeThreshold;
	Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public MatchServiceImpl(
			EpitopeService epitopeService, 
			@GlResolver Function<String, GenotypeList> glResolver, 
			GlClient glClient, 
			@GlStringFilter Function<String, String> glStringFilter, 
			FrequencyService freqService,
			@MatchGradeThreshold Double matchGradeThreshold,
	        @BaselineAlleleFrequency Double baselineFreq) 
	{
		this.epitopeService = epitopeService;
		this.glResolver = glResolver;
		this.glClient = glClient;
		this.glStringFilter = glStringFilter;
        this.freqService = freqService;
		this.matchGradeThreshold = (null != matchGradeThreshold) ? matchGradeThreshold : 0.01;
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
		} else {
	        if (0 == recipLow) recipLow = recipAllelePair.getHighG(); // special handling for null alleles
	        if (0 == donorLow) donorLow = donorAllelePair.getHighG(); // special handling for null alleles
		    if (recipLow.compareTo(donorLow) == 0) {
		        matchGrade = MatchGrade.PERMISSIVE;
    		} else {
    			matchGrade = (recipLow.compareTo(donorLow) < 0) 
    					? MatchGrade.GVH_NONPERMISSIVE : MatchGrade.HVG_NONPERMISSIVE;
    		}
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
		if (Trace.isEnabled()) Trace.setContext("r:");
		Map<AllelePair, Double> ralps = getAllelePairs(recipientGl, recipRace);
        if (Trace.isEnabled()) Trace.setContext("d:");
		Map<AllelePair, Double> dalps = getAllelePairs(donorGl, donorRace);
        if (Trace.isEnabled()) Trace.setContext("m:");
		return getMatch(ralps, dalps);
	}

    private String getMatchTrace(AllelePair rp, double rprob, AllelePair dp, double dprob, MatchGrade mg, Double p) {
        StringBuffer sb = new StringBuffer("r:")
            .append(rp.getA1().getGlstring()).append("(g:").append(rp.getG1()).append(")+")
            .append(rp.getA2().getGlstring()).append("(g:").append(rp.getG2()).append("):p:").append(rprob)
            .append(",d:")
            .append(dp.getA1().getGlstring()).append("(g:").append(dp.getG1()).append(")+")
            .append(dp.getA2().getGlstring()).append("(g:").append(dp.getG2()).append("):p:").append(dprob)
            .append(",m:")
            .append(mg)
            .append("(p:")
            .append(p)
            .append(")");
        return sb.toString();
    }

    class DoubleContainer {
        double d = 0;
        public double get() { return d; }
        public void set(double d) { this.d = d; }
        public void add(double d) { this.d += d; }
    }
    
	MatchResult getMatch(Map<AllelePair, Double> ralps, Map<AllelePair, Double> dalps) {
	    EnumMap<MatchGrade, DoubleContainer> pmap = new EnumMap<MatchGrade, DoubleContainer>(MatchGrade.class);
	    for (MatchGrade grade : MatchGrade.values()) {
	        pmap.put(grade, new DoubleContainer());
	    }
		MatchGrade grade = null;
		for (Map.Entry<AllelePair, Double> rp : ralps.entrySet()) {
			for (Map.Entry<AllelePair, Double> dp : dalps.entrySet()) {
				grade = getMatchGrade(rp.getKey(), dp.getKey());
				double f = rp.getValue() * dp.getValue();
				if (Trace.isEnabled()) Trace.add(getMatchTrace(rp.getKey(), rp.getValue(), dp.getKey(), dp.getValue(), grade, f));
				pmap.get(grade).add(f);
                //if (logger.isTraceEnabled()) {
                //	logger.trace(grade + ":rp:" + rp + ",dp:" + dp + " -> " + rf + "*" + df + " -> " + (rf * df));
                //}
			}
		}
		// normalize/round probabilities (should i do this?)
        Double total = pmap.values().stream().map(dc -> dc.get()).collect(Collectors.summingDouble(d -> d));
        pmap.values().forEach(dc -> dc.set((double)Math.round(dc.get() / total * 1000) / 1000));

        logger.debug("finished with: " + pmap);
		grade = UNKNOWN;
        if (pmap.get(GVH_NONPERMISSIVE).get() >= matchGradeThreshold) grade = GVH_NONPERMISSIVE;
        if (pmap.get(HVG_NONPERMISSIVE).get() >= matchGradeThreshold) grade = HVG_NONPERMISSIVE;
        if (pmap.get(PERMISSIVE).get() >= matchGradeThreshold) grade = PERMISSIVE;
        if (pmap.get(MATCH).get() >= matchGradeThreshold) grade = MATCH;
		// return both probabilities and match grade
		return new MatchResult(
		        pmap.get(MATCH).get(),
		        pmap.get(PERMISSIVE).get(),
		        pmap.get(HVG_NONPERMISSIVE).get(),
		        pmap.get(GVH_NONPERMISSIVE).get(),
		        pmap.get(UNKNOWN).get(),
		        grade);
	}
	
	public Stream<Allele> getLocusAlleles(Locus locus, Haplotype h) {
	    return h.getAlleleLists().stream()
	            .flatMap(al -> al.getAlleles().stream())
	            .filter(a -> a.getLocus().equals(locus));
	}
	
    Map<AllelePair, Double> getAllelePairs(GenotypeList gl, DetailRace race) {
		Locus dpb1;
		try {
			dpb1 = glClient.createLocus("HLA-DPB1");
		} catch (GlClientException e) {
			throw new RuntimeException("unable to create DPB1 locus", e);
		}
		Map<AllelePair, Double> pm = new HashMap<>();
		for (Genotype g : gl.getGenotypes()) {
			List<Haplotype> hl = g.getHaplotypes();
			Haplotype h1, h2;
			switch (hl.size()) {
			case 2:
				h1 = hl.get(0);
				h2 = hl.get(1);
				break;
			case 1:
				h1 = hl.get(0);
				h2 = hl.get(0);
				break;
            case 0:
                throw new RuntimeException("no haplotypes found for gl: " + gl);
			default:
				throw new RuntimeException("only expecting 2 haplotypes for gl: " + gl);
			}
			boolean a1hi = getLocusAlleles(dpb1, h1).count() == 1;
            boolean a2hi = getLocusAlleles(dpb1, h2).count() == 1;
            Set<Allele> dropTraceSet = new HashSet<>();
            getLocusAlleles(dpb1, h1).forEach(a1 -> {
                double a1f = a1hi ? 1.0 : freqService.getFrequency(a1.getGlstring(), race);
                if (0.0 == a1f) {
                    if (Trace.isEnabled() && !dropTraceSet.contains(a1)) {
                        Trace.add(a1.getGlstring() + "(p:0.0,dropped)");
                        dropTraceSet.add(a1);
                    }
                    return;
                }
                getLocusAlleles(dpb1, h2).forEach(a2 -> {
                    double a2f = a2hi ? 1.0 : freqService.getFrequency(a2.getGlstring(), race);
                    if (0.0 == a2f) {
                        if (Trace.isEnabled() && !dropTraceSet.contains(a2)) {
                            Trace.add(a2.getGlstring() + "(p:0.0,dropped)");
                            dropTraceSet.add(a2);
                        }
                        return;
                    }
                    double f = a1f * a2f;
                    if (h1 != h2) f *= 2;
                    Integer g1 = epitopeService.getGroupForAllele(a1);
                    Integer g2 = epitopeService.getGroupForAllele(a2); 
                    if (Trace.isEnabled()) {
                        Trace.add(a1.getGlstring() + "(g:" + g1 + ",p:" + a1f + ")+"
                                + a2.getGlstring() + "(g:" + g2 + ",p:" + a2f + ")");
                    }
                    AllelePair ap = new AllelePair(a1, g1, a2, g2, race);
                    pm.put(ap, f);
                    //Double existing = pm.put(ap, f);
                    //if (existing != null) {
                    //    pm.put(ap, existing + f); // eriktodo: verify genotype probability
                    //}
                });
            });
            // normalize
            Double total = pm.values().stream().collect(Collectors.summingDouble(d -> d));
            pm.entrySet().forEach(e -> e.setValue(e.getValue() / total));
		}
		return pm;
	}

}
