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

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.nmdp.service.epitope.db.AlleleCodeRow;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.task.URLProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DbiAlleleCodeResolver implements Function<String, String> {

	private static class AlleleCodeExpansion {
		private Set<String> alleleSet;
		private boolean generic;
		public AlleleCodeExpansion(Set<String> alleleSet) {
			this.alleleSet = alleleSet;
			this.generic = !alleleSet.iterator().next().contains(":");
		}
		public AlleleCodeExpansion(Set<String> alleleSet, boolean generic) {
			this.generic = generic;
			this.alleleSet = alleleSet;
		}
		public Set<String> getAlleleSet() {
			return alleleSet;
		}
		public boolean isGeneric() {
			return generic;
		}
//		public AlleleCodeExpansion include(AlleleCodeExpansion other) {
//			this.alleleSet.addAll(other.alleleSet);
//			this.familyIncluded = this.familyIncluded || other.familyIncluded;
//			return this;
//		}
	}
	
	private static final Pattern ALLELE_CODE_PAT = Pattern.compile("((?:HLA-)?[A-Z0-9]+\\*)?(\\d+):([A-Z]+)", CASE_INSENSITIVE);
	Map<String, AlleleCodeExpansion> alleleCodeMap = new HashMap<>();
	URLProcessor urlProcessor;
	long lastModified = 0;
	static Logger logger = LoggerFactory.getLogger(DbiAlleleCodeResolver.class);

	private DbiManager dbi;

	@Inject
	public DbiAlleleCodeResolver(DbiManager dbi) {
		this.dbi = dbi;
	}

	public void buildAlleleCodeMap(Iterator<AlleleCodeRow> alleleCodeIter) {
        try {
//        	List<AlleleCodeRow> alleleCodeIter = dbi.getAllelesCodes();

//        	Map<String, AlleleCodeExpansion> newMap = stream(spliteratorUnknownSize(alleleCodeIter, ORDERED), false)
//        			.collect(groupingBy(r -> r.getCode(), Collectors.reducing(
//							new AlleleCodeExpansion(), 
//							r -> new AlleleCodeExpansion(newHashSet(r.getAllele()), r.isFamilyIncluded()),
//							(e1, e2) -> e1.include(e2))));

        	Map<String, AlleleCodeExpansion> newMap = 
        			stream(spliteratorUnknownSize(alleleCodeIter, ORDERED), false)
		        			.collect(groupingBy(r -> r.getCode(), mapping(r -> r.getAllele(),
		        					collectingAndThen(toSet(), s -> new AlleleCodeExpansion(s)))));
        					
        					
//							new AlleleCodeExpansion(), 
//							r -> new AlleleCodeExpansion(newHashSet(r.getAllele()), r.isFamilyIncluded()),
//							(e1, e2) -> e1.include(e2))));

        	alleleCodeMap = newMap;
		} catch (RuntimeException e) {
			throw (RuntimeException)e;
		} catch (Exception e) {
			throw new RuntimeException("exception while refreshing allele codes", e);
		}
	}
	
	@Override
	public String apply(String alleleCode) {
		Matcher matcher = ALLELE_CODE_PAT.matcher(alleleCode);
		if (!matcher.matches()) {
			throw new RuntimeException("unrecognized allele code format: " + alleleCode);
		}
		String prefix = matcher.group(1) == null ? "" : matcher.group(1);
		String family = matcher.group(2);
		String code = matcher.group(3);
		AlleleCodeExpansion expansion = null;
		if (code.equals("XX")) {
			Set<String> alleles = dbi.getFamilyAlleleMap().get(family);
			expansion = new AlleleCodeExpansion(alleles, false);
		} else {
			expansion = alleleCodeMap.get(code);
		}
		if (null == expansion) {
			throw new RuntimeException("unrecognized allele code: " + alleleCode);
		}
		Set<String> alleleSet = expansion.getAlleleSet();
		Stream<String> alleleStream = alleleSet.stream();
		if (expansion.isGeneric()) alleleStream = alleleStream.map(a -> family + ":" + a);
		if (null != prefix) alleleStream = alleleStream.map(a -> prefix + a);
		return alleleStream.collect(Collectors.joining("/"));
	}

}
