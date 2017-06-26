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

package org.nmdp.service.epitope.gl;

import com.google.inject.Inject;
import org.nmdp.gl.GenotypeList;
import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.GlClientException;

import java.util.function.Function;

/**
 * Implementation of GlResolver that resolves GenotypeLists from a GlClient.
 */
public class GenotypeListResolver implements Function<String, GenotypeList> {

	final private GlClient glClient;

	@Inject
	public GenotypeListResolver(GlClient glClient) {
		this.glClient = glClient;
	}

	/**
	 * Parse the glstring and resolve to a GenotypeList.
	 */
	@Override
	public GenotypeList apply(String glstring) {
		try {
			return glClient.createGenotypeList(glstring);
		} catch (GlClientException e) {
			throw new RuntimeException("failed to parse glstring: " + glstring, e);
		}
	}
}
