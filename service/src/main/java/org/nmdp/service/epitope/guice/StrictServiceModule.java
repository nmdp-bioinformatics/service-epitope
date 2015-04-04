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

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.GenotypeList;
import org.nmdp.service.epitope.allelecode.AlleleCodeResolver;
import org.nmdp.service.epitope.allelecode.NmdpV3AlleleCodeResolver;
import org.nmdp.service.epitope.freq.DbiFrequencyResolver;
import org.nmdp.service.epitope.freq.FrequencyResolver;
import org.nmdp.service.epitope.freq.IFrequencyResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.GlStringResolver;
import org.nmdp.service.epitope.gl.JsonGlClientModule;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.group.DbiGroupResolver;
import org.nmdp.service.epitope.group.GroupResolver;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.service.AllelePair;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.EpitopeServiceImpl;
import org.nmdp.service.epitope.service.MatchService;
import org.nmdp.service.epitope.service.MatchServiceImpl;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

public class StrictServiceModule extends AbstractModule {

	@Override
	protected void configure() {

		// no rewriting of incoming glstrings
		bind(Key.get(new TypeLiteral<Function<String, String>>(){}, GlStringFilter.class)).toInstance(Functions.<String>identity());

		// strict gl client
		install(new JsonGlClientModule());
		
		// standard implementations
		bind(EpitopeService.class).to(EpitopeServiceImpl.class);
		bind(MatchService.class).to(MatchServiceImpl.class);
		bind(IFrequencyResolver.class).to(DbiFrequencyResolver.class);
	}
	
	/**
	 * resolve frequencies from internal sqlite db
	 * @param resolver
	 * @param duration
	 * @param size
	 * @return
	 */
	@Provides
	@FrequencyResolver
	public Function<AllelePair, Double> getFrequencyResolver(IFrequencyResolver resolver, @FrequencyCacheMillis long duration, @FrequencyCacheSize long size) {
		return new CachingResolver<AllelePair, Double>(resolver, duration, duration, size);
	}

	/**
	 * caching resolver backed by nmdp v3 alpha file download
	 */
	@Provides
	@AlleleCodeResolver
	public Function<String, String> getAlleleCodeResolver(NmdpV3AlleleCodeResolver resolver, @AlleleCodeCacheMillis long duration, @AlleleCodeCacheSize long size) {
		return new CachingResolver<String, String>(resolver, duration, duration, size);
	}

	/**
	 * resolve groups from internal sqlite db
	 */
	@Provides
	@GroupResolver
	public Function<Integer, List<Allele>> getGroupResolver(DbiGroupResolver resolver, @GroupCacheMillis long duration) {
		return new CachingResolver<Integer, List<Allele>>(resolver, duration, duration, 3);
	}

	/**
	 * resolve gl strings using supplied glclient (see glclient bindings)
	 */
	@Provides
	@GlResolver
	public Function<String, GenotypeList> getGlResolver(GlStringResolver resolver, @GlCacheMillis long duration, @GlCacheSize long size) {
		return new CachingResolver<String, GenotypeList>(resolver, duration, duration, size);
	}
	
}
