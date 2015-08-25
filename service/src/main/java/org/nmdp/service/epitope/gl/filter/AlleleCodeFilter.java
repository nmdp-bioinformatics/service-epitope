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

import org.nmdp.service.epitope.allelecode.AlleleCodeResolver;
import org.nmdp.service.epitope.db.DbiManager;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * GlStringFilter implementation that replaces allele codes with allele lists.
 */
public class AlleleCodeFilter implements Function<String, String> {

	private Function<String, String> alleleCodeResolver;

	private final Pattern ALLELE_GROUP_PAT = Pattern.compile("(?<=^|[/~+|^])[^/~+|^*]+\\*\\d+:[A-Z]+(?=$|[/~+|^])", CASE_INSENSITIVE);

	private DbiManager dbi;

	@Inject
	public AlleleCodeFilter(@AlleleCodeResolver Function<String, String> alleleCodeResolver, DbiManager dbi) {
		this.alleleCodeResolver = alleleCodeResolver;
		this.dbi = dbi;
	}

	/**
	 * Substitute allele code references with their allele list gl string representations.
	 */
	@Override
	public String apply(String glstring) {
		Matcher matcher = ALLELE_GROUP_PAT.matcher(glstring);
		StringBuilder sb = new StringBuilder();
		int last = 0;
		while (matcher.find()) {
			sb.append(glstring.substring(last, matcher.start()));
			String code = matcher.group();
			String codestring = alleleCodeResolver.apply(code);
			sb.append(codestring);
			last = matcher.end();
		}
		if (last < glstring.length()) sb.append(glstring.substring(last, glstring.length()));
		return sb.toString();
	}
	
}
