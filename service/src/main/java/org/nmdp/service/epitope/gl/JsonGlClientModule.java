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

import org.nmdp.gl.client.GlClient;
import org.nmdp.gl.client.cache.CacheGlClientModule;
import org.nmdp.gl.client.http.HttpClient;
import org.nmdp.gl.client.http.restassured.RestAssuredHttpClient;
import org.nmdp.gl.client.json.JsonGlClient;
import org.nmdp.gl.service.Namespace;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice module that binds a JsonGlClient implementation.
 */
public class JsonGlClientModule extends AbstractModule {

	final String namespace;
	
	public JsonGlClientModule() {
		this.namespace = "https://gl.immunogenomics.org/imgt-hla/3.18.0/";
	}

	public JsonGlClientModule(String namespace) {
		this.namespace = namespace;
	}
	
    @Override
    protected void configure() {
		install(new CacheGlClientModule());
        bind(String.class).annotatedWith(Namespace.class).toInstance(namespace);
		bind(JsonFactory.class).to(JsonFactory.class).in(Singleton.class);
        bind(HttpClient.class).to(RestAssuredHttpClient.class);
        bind(GlClient.class).to(JsonGlClient.class);
    }

}
