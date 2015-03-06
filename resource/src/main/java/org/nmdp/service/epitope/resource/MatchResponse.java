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

package org.nmdp.service.epitope.resource;

import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchGrade;
import org.nmdp.service.epitope.domain.MatchResult;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value="Result of match of recipient and donor.  If races is provided in the"
			+ " request, the probabilities of each match outcome are provided based on likelihood of occurance"
			+ " of each allele for that race group, otherwise the most pessimistic match grade is returned." )
public class MatchResponse {

	private String recipient;
	private DetailRace recipientRace;
	private String donor;
	private DetailRace donorRace;
	private String token;
	private MatchResult matchResult;

	@JsonCreator
	public MatchResponse(
			final @JsonProperty("recipient") String recipient,
			final @JsonProperty("recipientRace") DetailRace recipientRace,
			final @JsonProperty("donor") String donor,
			final @JsonProperty("donorRace") DetailRace donorRace,
			final @JsonProperty("token") String token,
			final @JsonProperty("matchProbability") Double matchProbability,
			final @JsonProperty("permissiveMismatchProbability") Double permissiveMismatchProbability,
			final @JsonProperty("hvgNonPermissiveMismatchProbability") Double hvgNonPermissiveMismatchProbability,
			final @JsonProperty("gvhNonPermissiveMismatchProbability") Double gvhNonPermissiveMismatchProbability,
			final @JsonProperty("unknownProbability") Double unknownProbability,
			final @JsonProperty("matchGrade") MatchGrade matchGrade)
	{
		this.recipient = recipient;
		this.recipientRace = recipientRace;
		this.donor = donor;
		this.donorRace = donorRace;
		this.token = token;
		this.matchResult = new MatchResult(
				matchProbability, 
				permissiveMismatchProbability, 
				hvgNonPermissiveMismatchProbability, 
				gvhNonPermissiveMismatchProbability, 
				unknownProbability,
				matchGrade);
	}

	public MatchResponse(String recipient,
			DetailRace recipientRace,
			String donor,
			DetailRace donorRace,
			MatchResult matchResult) 
	{
		this.recipient = recipient;
		this.recipientRace = recipientRace;
		this.donor = donor;
		this.donorRace = donorRace;
		this.matchResult = matchResult;
	}

	public MatchResponse(String token, MatchResult matchResult) {
		this.token = token;
		this.matchResult = matchResult;
	}

	@ApiModelProperty(
			value="Recipient genotype as provided in request, included if request token is null", 
			required=true)
	public String getRecipient() {
		return recipient;
	}

	@ApiModelProperty(
			value="Recipient race as provided in request, included if request token is null", 
			required=false)
	public DetailRace getRecipientRace() {
		return recipientRace;
	}

	@ApiModelProperty(
			value="Donor genotype as provided in request, included if request token is null", 
			required=true)
	public String getDonor() {
		return donor;
	}

	@ApiModelProperty(
			value="Donor race as provided in request, included if request token is null", 
			required=false)
	public DetailRace getDonorRace() {
		return donorRace;
	}

	@ApiModelProperty(
			value="Token as provided in request, if present", 
			required=false)
	public String getToken() {
		return token;
	}

	@ApiModelProperty(
			value="Probability of match", 
			required=false)
	public Double getMatchProbability() {
		return matchResult.getMatchProbability();
	}

	@ApiModelProperty(
			value="Probability of permissive mismatch", 
			required=false)
	public Double getPermissiveMismatchProbability() {
		return matchResult.getPermissiveMismatchProbability();
	}
	
	@ApiModelProperty(
			value="Probability of host vs. graft non-permissive mismatch", 
			required=false)
	public Double getHvgNonPermissiveMismatchProbability() {
		return matchResult.getHvgNonPermissiveMismatchProbability();
	}

	@ApiModelProperty(
			value="Probability of graft vs. host non-permissive mismatch", 
			required=false)
	public Double getGvhNonPermissiveMismatchProbability() {
		return matchResult.getGvhNonPermissiveMismatchProbability();
	}

	@ApiModelProperty(
			value="Probability of type with unknown immunogenicity group", 
			required=false)
	public Double getUnknownProbability() {
		return matchResult.getUnknownProbability();
	}

	@ApiModelProperty(
			value="Most pessimistic possible match grade if races are not provided"
					+ " or if likelihood of occurance of any alleles can't be determined", 
			required=false)
	public MatchGrade getMatchGrade() {
		return matchResult.getMatchGrade();
	}
}
