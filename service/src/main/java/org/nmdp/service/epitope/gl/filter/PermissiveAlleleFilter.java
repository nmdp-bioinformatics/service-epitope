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

package org.nmdp.service.epitope.gl.filter;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * GlStringFilter that formalizes glstrings that aren't strictly formatted.  That is, HLA- prefix is added
 * to all alleles, and locus names are added if the filter is configured to do so.  
 */
public class PermissiveAlleleFilter implements Function<String, String> {

	private static final Pattern ALLELE_PAT = Pattern.compile("(?<=^|[/~+|^]+)(?:(HLA-)?([a-zA-Z0-9]+\\*))?(\\d+(?::[\\dA-Z]+)*)(?=$|[/~+|^])", CASE_INSENSITIVE);
	
	private String defaultLocus;
	
	/**
	 * Create with defaultLocus of "DPB1"
	 */
	public PermissiveAlleleFilter() {
		this.defaultLocus = "DPB1";
	}
	
	/**
	 * Create with specified defaultLocus.
	 *  
	 * @param defaultLocus default locus to apply, if locus not included in allele names.  
	 * defaultLocus should omit the "HLA-" prefix.  If defaultLocus is null, defaultLocus is not supported
	 */
	public PermissiveAlleleFilter(String defaultLocus) {
		this.defaultLocus = defaultLocus;
	}
	
	/**
	 * adds "HLA-" to locus names in GL string if omitted.  Also adds default locus if omitted in alleles, if properly configured. 
	 */
	@Override
	public String apply(String gl) {
		Matcher m = ALLELE_PAT.matcher(gl);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (m.find()) {
			if (m.start() > last) sb.append(gl.substring(last, m.start())); 
			sb.append("HLA-"); // regardless if group(1) is null or not
			if (m.group(2) == null) {
				if (defaultLocus == null) {
					throw new RuntimeException("default locus in GL string not supported");
				} else {
					sb.append(defaultLocus).append("*");
				}
			} else {
				sb.append(m.group(2));
			}
			sb.append(m.group(3));
			last = m.end();
		}
		if (last < gl.length()) sb.append(gl.substring(last, gl.length()));
		return sb.toString();
	}
}
