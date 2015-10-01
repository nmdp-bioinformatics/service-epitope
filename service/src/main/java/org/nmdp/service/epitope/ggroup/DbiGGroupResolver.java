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

package org.nmdp.service.epitope.ggroup;

import java.util.List;
import java.util.stream.Collectors;

import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.db.DbiManager;

import com.google.common.base.Function;
import com.google.inject.Inject;

/**
 * DbiGGroupResolver implementation that resolves alleles that belong to the same g group based the supplied DB manager.
 */
public class DbiGGroupResolver implements Function<Allele, List<Allele>> {
    
	DbiManager dbiManager;
	GlClient glClient;

	@Inject
	public DbiGGroupResolver(DbiManager dbiManager, GlClient glClient) {
		this.dbiManager = dbiManager;
		this.glClient = glClient;
	}

	@Override
	public List<Allele> apply(final Allele allele) {
		List<String> alleleStringList = dbiManager.getGGroupAllelesForAllele(allele.getGlstring());
		List<Allele> alleleList = alleleStringList.stream()
				.map(name -> {
					try { return glClient.createAllele(name); } 
					catch (GlClientException e) { throw new RuntimeException("GlClient failed to create allele", e); }})
				.collect(Collectors.toList());
		return alleleList;
	}

}
