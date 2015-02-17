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

package org.nmdp.service.epitope.group;

import java.util.List;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.AlleleList;
import org.immunogenomics.gl.client.GlClient;
import org.nmdp.service.epitope.guice.ConfigurationBindings;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * Implementation of GroupResolver that resolves TCE group membership based on allele lists retrieved from a GlClient. 
 */
public class GlServiceGroupResolver implements Function<Integer, List<Allele>> {

	private GlClient glClient;
	private String namespace;
	private String group1Suffix;
	private String group2Suffix;
	private String group3Suffix;

	@Inject
	public GlServiceGroupResolver(GlClient glClient, 
			@ConfigurationBindings.NamespaceUrl String namespace,
			@ConfigurationBindings.Group1Suffix String group1Suffix,
			@ConfigurationBindings.Group2Suffix String group2Suffix,
			@ConfigurationBindings.Group3Suffix String group3Suffix) 
	{
		this.glClient = glClient;
		this.namespace = namespace;
		this.group1Suffix = group1Suffix;
		this.group2Suffix = group2Suffix;
		this.group3Suffix = group3Suffix;
	}
	
	public List<Allele> apply(Integer group) {
		String url = null;
		switch (group) {
		case 1:
			url = namespace + group1Suffix;
			break;
		case 2:
			url = namespace + group2Suffix;
			break;
		case 3:
			url = namespace + group3Suffix;
			break;
		default:
			throw new RuntimeException("invalid immunogenicity group: " + group);
		}
		AlleleList alleleList = glClient.getAlleleList(url);
		return alleleList.getAlleles();
	}
}
