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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel("Filter for alleles or groups")
public class AlleleListRequest {

	private String allele;
	private List<String> alleles;
	private Integer group;
	private List<Integer> groups;

    @JsonCreator
    public AlleleListRequest(final @JsonProperty("allele") String allele,
                             final @JsonProperty("alleles") List<String> alleles,
                             final @JsonProperty("group") Integer group,
                             final @JsonProperty("groups") List<Integer> groups) {

        this.allele = allele;
        this.alleles = alleles;
        this.group = group;
        this.groups = groups;
    }

	@ApiModelProperty("GL string for a single allele") 
    public String getAllele() {
        return allele;
    }

	@ApiModelProperty("List of alleles") 
    public List<String> getAlleles() {
        return alleles;
    }
	
	@ApiModelProperty("Integer representation for a single immunogenicity group")
    public Integer getGroup() {
        return group;
    }
	
	@ApiModelProperty("List of immunogenicity groups")
    public List<Integer> getGroups() {
        return groups;
    }
}
