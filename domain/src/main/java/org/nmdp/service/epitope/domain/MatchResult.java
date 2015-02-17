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

package org.nmdp.service.epitope.domain;

/**
 * Result of a match between a recipient and donor.
 */
public class MatchResult {

	private Double matchProbability;
	private Double permissiveMismatchProbability;
	private Double hvgNonPermissiveMismatchProbability;
	private Double gvhNonPermissiveMismatchProbability;
	private Double unknownProbability;
	private MatchGrade pessimisticMatchGrade;

	/**
	 * Construct a new MatchResult with given likelihoods of various outcomes.
	 * @param matchProbability probability of a match.
	 * @param permissiveMismatchProbability probability of a mismatch.
	 * @param hvgNonPermissiveMismatchProbability probability of a host vs graft non-permissive mismatch.
	 * @param gvhNonPermissiveMismatchProbability probability of a graft vs host non-permissive mismatch.
	 * @param unknownProbability probability that the outcome is unknown, because the TCE group of one or more alleles is unknown.
	 */
	public MatchResult(
			Double matchProbability,
			Double permissiveMismatchProbability,
			Double hvgNonPermissiveMismatchProbability,
			Double gvhNonPermissiveMismatchProbability,
			Double unknownProbability)
	{
		double total = matchProbability 
				+ permissiveMismatchProbability 
				+ hvgNonPermissiveMismatchProbability 
				+ gvhNonPermissiveMismatchProbability 
				+ unknownProbability;
		this.matchProbability = round(matchProbability/total);
		this.permissiveMismatchProbability = round(permissiveMismatchProbability/total);
		this.hvgNonPermissiveMismatchProbability = round(hvgNonPermissiveMismatchProbability/total);
		this.gvhNonPermissiveMismatchProbability = round(gvhNonPermissiveMismatchProbability/total);
		this.unknownProbability = round(unknownProbability/total);
	}

	/**
	 * Construct a new MatchResult with the given most pessimistic match grade.  This match result is applicable in the case where 
	 * frequency information is unavailable for one or more alleles.  
	 * @param pessimisticMatchGrade most pessimistic match grade
	 */
	public MatchResult(MatchGrade pessimisticMatchGrade) {
		this.pessimisticMatchGrade = pessimisticMatchGrade;
	}

	private double round(double d) {
		return (double)Math.round(d * 1000) / 1000;
	}
	
	/**
	 * @return probability of a match.
	 */
	public Double getMatchProbability() {
		return matchProbability;
	}

	/**
	 * @return probability of a mismatch
	 */
	public Double getPermissiveMismatchProbability() {
		return permissiveMismatchProbability;
	}

	/**
	 * @return probability of a host vs graft non-permissive mismatch.
	 */
	public Double getHvgNonPermissiveMismatchProbability() {
		return hvgNonPermissiveMismatchProbability;
	}
	
	/**
	 * @return probability of a graft vs host non-permissive mismatch.
	 */
	public Double getGvhNonPermissiveMismatchProbability() {
		return gvhNonPermissiveMismatchProbability;
	}

	/**
	 * @return probability that the outcome is unknown, because the TCE group of one or more alleles is unknown.
	 */
	public Double getUnknownProbability() {
		return unknownProbability;
	}

	/**
	 * @return most pessimistic match grade 
	 */
	public MatchGrade getPessimisticMatchGrade() {
		return pessimisticMatchGrade;
	}

	@Override
	public String toString() {
		return "MatchResult [matchProbability=" + matchProbability
				+ ", permissiveMismatchProbability="
				+ permissiveMismatchProbability
				+ ", hvgNonPermissiveMismatchProbability="
				+ hvgNonPermissiveMismatchProbability
				+ ", gvhNonPermissiveMismatchProbability="
				+ gvhNonPermissiveMismatchProbability + ", unknownProbability="
				+ unknownProbability + ", pessimisticMatchGrade="
				+ pessimisticMatchGrade + "]";
	}
	
}
