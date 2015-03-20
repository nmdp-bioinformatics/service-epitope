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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel("Immunogenicity group along with requested alleles that are associated")
public class GroupView {

	private Integer group;
	private List<String> alleleList;

    @JsonCreator
    public GroupView(final @JsonProperty("group") Integer group, final @JsonProperty("alleleList") List<String> alleleList) {
		this.group = group; 
		this.alleleList = alleleList;
	}

	@ApiModelProperty(value="Integer representation of an immunogenicity group", required=true)
	public Integer getGroup() {
		return group;
	}
	
	@ApiModelProperty(value="List of requested alleles that are associated with it", required=true)
	public List<String> getAlleleList() {
		return alleleList;
	}
}
