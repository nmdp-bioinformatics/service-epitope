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

package org.nmdp.service.epitope.gl.filter;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;

/**
 * GlStringFilter that shortens all alleles in the glstring to two fields (e.g. HLA-DPB1*01:01:01 to HLA-DPB1*01:01)
 */
public class ArsAlleleFilter implements Function<String, String> {

	private static final Pattern ALLELE_PAT = Pattern.compile("(?<=^|\\*)(\\d+:\\d+)(:[A-Z0-9]+)*(?=$|[/~+|^])", CASE_INSENSITIVE);

	/**
	 * Shorten all alleles in the provided glstring to two fields
	 */
	@Override
	public String apply(String glstring) {
		Matcher matcher = ALLELE_PAT.matcher(glstring);
		return matcher.replaceAll("$1");
	}

}
