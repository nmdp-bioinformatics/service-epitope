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

package org.nmdp.service.epitope.group;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.nmdp.gl.Allele;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;
import org.nmdp.service.epitope.db.DbiManager;

import com.google.inject.Inject;

/**
 * GroupResolver implementation that resolves alleles that belong to a group based the supplied DB manager.
 */
public class DbiImmuneGroupResolver implements Function<Integer, List<Allele>> {
    
	DbiManager dbiManager;
	GlClient glClient;

	@Inject
	public DbiImmuneGroupResolver(DbiManager dbiManager, GlClient glClient) {
		this.dbiManager = dbiManager;
		this.glClient = glClient;
	}

	@Override
	public List<Allele> apply(final Integer group) {
		List<String> alleleStringList = dbiManager.getAllelesForImmuneGroup(group);
		return alleleStringList.stream().map(name -> {
			try {
				return glClient.createAllele(name);
			} catch (GlClientException e) {
				throw new RuntimeException("failed to resolve allele: " + name, e);
			}
		}).collect(Collectors.toList());
	}

}
