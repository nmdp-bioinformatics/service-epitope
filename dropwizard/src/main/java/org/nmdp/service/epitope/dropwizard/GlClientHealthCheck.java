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

package org.nmdp.service.epitope.dropwizard;

import org.immunogenomics.gl.Locus;
import org.immunogenomics.gl.client.GlClient;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Health check for gl client, checkes availability
 */
public class GlClientHealthCheck extends HealthCheck {

	@Inject
	private Injector injector;
	
	/**
	 * check availability of the GlClient
	 */
	@Override
	protected Result check() throws Exception {
		// unwrap caching client wrapper
		GlClient client = injector.getInstance(GlClient.class);
		if (null == client) return Result.unhealthy("no binding for GlClient");
		Locus locus = null;
		try {
			locus = client.getLocus("HLA-A");
		} catch (Exception e) {
			return Result.unhealthy(e);
		}
		if (null == locus) return Result.unhealthy("failed to retrieve locus from GlClient");
		return Result.healthy();
	}

}
