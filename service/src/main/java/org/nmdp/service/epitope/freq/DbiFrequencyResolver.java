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

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.service.AllelePair;

import com.google.common.base.Function;
import com.google.inject.Inject;

public class DbiFrequencyResolver implements Function<AllelePair, Double> {

	private DbiManager dbi;
	
	@Inject
	public DbiFrequencyResolver(DbiManager dbi) {
		this.dbi = dbi;
	}
	
	public String stripLocus(String allele) {
		int i = allele.indexOf('*');
		if (i < 0) return allele;
		return allele.substring(i+1);
	}
	
	@Override
	public Double apply(AllelePair pair) {
		if (null == pair.getRace()) return null;
		
		Double a1f = dbi.getFrequency(stripLocus(pair.getA1().getGlstring()), pair.getRace());
		if (null == a1f) return null;
		if (pair.getA1().equals(pair.getA2())) {
			return a1f*a1f;
		} else {
			Double a2f = dbi.getFrequency(stripLocus(pair.getA2().getGlstring()), pair.getRace());
			if (null == a2f) return null;
			return a1f*a2f*2;
		}
	}

}
