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

package org.nmdp.service.epitope.resource;

import org.nmdp.service.epitope.domain.DetailRace;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel("Request to match a recipient to a donor")
public class MatchRequest {

	private String recipient;
	private DetailRace recipientRace;
	private String donor;
	private DetailRace donorRace;
	private String token;

	@JsonCreator
	public MatchRequest(
			@JsonProperty("recipient") String recipient, 
			@JsonProperty("recipientRace") DetailRace recipientRace,
			@JsonProperty("donor") String donor, 
			@JsonProperty("donorRace") DetailRace donorRace, 
			@JsonProperty("token") String token) 
	{
		this.recipient = recipient;
		this.recipientRace = recipientRace;
		this.donor = donor;
		this.donorRace = donorRace ;
		this.token = token;
	}

	@ApiModelProperty(
			value="Recipient genotype list as a GL string, optionally taking allele codes (e.g. HLA-DPB1*ABCD+HLA-DPB1*DEFG)", 
			required=true)
	public String getRecipient() {
		return recipient;
	}

	@ApiModelProperty(
			value="Recipient race", 
			required=false)
	public DetailRace getRecipientRace() {
		return recipientRace;
	}

	@ApiModelProperty(
			value="Donor genotype list as a GL string, optionally taking allele codes (e.g. HLA-DPB1*ABCD+HLA-DPB1*DEFG)", 
			required=true)
	public String getDonor() {
		return donor;
	}

	@ApiModelProperty(
			value="Donor race", 
			required=false)
	public DetailRace getDonorRace() {
		return donorRace;
	}

	@ApiModelProperty(
			value="Arbitrary string token to corrolate a reponse with a request", 
			required=false)
	public String getToken() {
		return token;
	}
	
}
