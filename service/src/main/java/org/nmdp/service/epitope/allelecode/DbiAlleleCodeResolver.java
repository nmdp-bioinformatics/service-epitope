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

package org.nmdp.service.epitope.allelecode;

import java.util.List;

import org.nmdp.service.epitope.db.DbiManager;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * AlleleCodeResolver that resolves based
 */
public class DbiAlleleCodeResolver implements Function<String, String> {

	private DbiManager dbi;

	@Inject
	public DbiAlleleCodeResolver(DbiManager dbi) {
		this.dbi = dbi;
	}
	
	/**
	 * requires allele codes in the form <locus>*<allele code>, where <locus> is a complete and valid locus
	 */
	@Override
	public String apply(String alleleCode) {
		
		int sep = alleleCode.indexOf("*");
		if (sep == -1) {
			throw new RuntimeException("invalid allele code, can't determine locus: " + alleleCode);
		}
		String locus = alleleCode.substring(0, sep);
		String code = alleleCode.substring(sep+1, alleleCode.length());
		List<String> al = dbi.getAllelesForCode(locus, code);
		if (al == null || al.isEmpty()) {
			throw new RuntimeException("unrecognized allele code: " + locus + "*" + code);
		}
		StringBuilder sb = null;
		if (al.size() == 1) { return al.get(0); }
		else { sb = new StringBuilder(al.get(0)); }
		for (String a : al.subList(1, al.size())) {
			sb.append("/").append(a);
		}
		return sb.toString();
	}

}
