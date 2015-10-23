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

package org.nmdp.service.epitope.guice;

import java.util.List;
import java.util.function.Function;

import org.nmdp.gl.Allele;
import org.nmdp.gl.GenotypeList;
import org.nmdp.service.epitope.allelecode.AlleleCodeResolver;
import org.nmdp.service.epitope.allelecode.DbiAlleleCodeResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.GlStringResolver;
import org.nmdp.service.epitope.gl.JsonGlClientModule;
import org.nmdp.service.epitope.gl.filter.GlstringFilter;
import org.nmdp.service.epitope.group.DbiGroupResolver;
import org.nmdp.service.epitope.group.GroupResolver;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.EpitopeServiceImpl;
import org.nmdp.service.epitope.service.FrequencyService;
import org.nmdp.service.epitope.service.FrequencyServiceImpl;
import org.nmdp.service.epitope.service.MatchService;
import org.nmdp.service.epitope.service.MatchServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

public class StrictServiceModule extends AbstractModule {

	@Override
	protected void configure() {

		// no rewriting of incoming glstrings
		bind(Key.get(new TypeLiteral<Function<String, String>>(){}, GlstringFilter.class)).toInstance(a -> a);

		// strict gl client
		install(new JsonGlClientModule());
		
		// standard implementations
		bind(EpitopeService.class).to(EpitopeServiceImpl.class);
		bind(MatchService.class).to(MatchServiceImpl.class);
        bind(FrequencyService.class).to(FrequencyServiceImpl.class);
	}
	
	/**
	 * caching resolver backed by nmdp v3 alpha file download
	 */
	@Provides
	@AlleleCodeResolver
	public Function<String, String> getAlleleCodeResolver(DbiAlleleCodeResolver resolver, @AlleleCodeCacheMillis long duration, @AlleleCodeCacheSize long size) {
		return new CachingFunction<String, String>(resolver, duration, duration, size);
	}

	/**
	 * resolve groups from internal sqlite db
	 */
	@Provides
	@GroupResolver
	public Function<Integer, List<Allele>> getGroupResolver(DbiGroupResolver resolver, @GroupCacheMillis long duration) {
		return new CachingFunction<Integer, List<Allele>>(resolver, duration, duration, 3);
	}

	/**
	 * resolve gl strings using supplied glclient (see glclient bindings)
	 */
	@Provides
	@GlResolver
	public Function<String, GenotypeList> getGlResolver(GlStringResolver resolver, @GlCacheMillis long duration, @GlCacheSize long size) {
		return new CachingFunction<String, GenotypeList>(resolver, duration, duration, size);
	}
	
}
