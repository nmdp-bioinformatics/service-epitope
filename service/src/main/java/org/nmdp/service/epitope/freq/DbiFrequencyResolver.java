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

package org.nmdp.service.epitope.freq;

import java.util.Set;

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;
import org.nmdp.service.epitope.service.AllelePair;

import com.google.common.base.Function;
import com.google.inject.Inject;

public class DbiFrequencyResolver implements IFrequencyResolver {

	private DbiManager dbi;
	private Double baselineFrequency;
    private Set<DetailRace> raceSet;
	
	@Inject
	public DbiFrequencyResolver(DbiManager dbi, @BaselineAlleleFrequency Double baselineFrequency) {
		this.dbi = dbi;
		this.baselineFrequency = baselineFrequency;
        this.raceSet = this.dbi.getRacesWithFrequencies();
	}
	
	String stripLocus(String allele) {
		int i = allele.indexOf('*');
		if (i < 0) return allele;
		return allele.substring(i+1);
	}
	
	@Override
	public Double apply(AllelePair pair) {
		if (null == pair.getRace()) return null;
		Double a1f = getFrequency(pair.getA1().getGlstring(), pair.getRace());
		if (pair.getA1().equals(pair.getA2())) {
			return a1f*a1f;
		} else {
		    Double a2f = getFrequency(pair.getA2().getGlstring(), pair.getRace());
			return a1f*a2f*2;
		}
	}
	
	public Double getFrequency(String allele, DetailRace race) {
        boolean r = raceSet.contains(race);
        Double f = dbi.getFrequency(stripLocus(allele), race);
        if (null == f) f = r ? 0 : baselineFrequency;
        return f;
	}

}
