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

package org.nmdp.service.epitope.guice;

import static com.google.common.base.Functions.compose;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.immunogenomics.gl.Allele;
import org.immunogenomics.gl.GenotypeList;
import org.nmdp.service.epitope.allelecode.AlleleCodeResolver;
import org.nmdp.service.epitope.allelecode.NmdpV3AlleleCodeResolver;
import org.nmdp.service.epitope.freq.DbiFrequencyResolver;
import org.nmdp.service.epitope.freq.FrequencyResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.GlStringResolver;
import org.nmdp.service.epitope.gl.LocalGlClientModule;
import org.nmdp.service.epitope.gl.filter.AlleleCodeFilter;
import org.nmdp.service.epitope.gl.filter.ArsAlleleFilter;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.gl.filter.PermissiveAlleleFilter;
import org.nmdp.service.epitope.group.DbiGroupResolver;
import org.nmdp.service.epitope.group.GroupResolver;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrl;
import org.nmdp.service.epitope.service.AllelePair;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.EpitopeServiceImpl;
import org.nmdp.service.epitope.service.MatchService;
import org.nmdp.service.epitope.service.MatchServiceImpl;

import com.google.common.base.Function;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class LocalServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		
		// local gl client
		install(new LocalGlClientModule());
		
		// standard implementations
		bind(EpitopeService.class).to(EpitopeServiceImpl.class);
		bind(MatchService.class).to(MatchServiceImpl.class);
	}
	
	@Provides
	@NmdpV3AlleleCodeUrl 
	public URL getNmdpV3AlleleCodeUrl(@NmdpV3AlleleCodeUrl String urlSpec) throws MalformedURLException {
		return new URL(urlSpec);
	}
	
	/**
	 * resolve frequencies from internal sqlite db
	 */
	@Provides
	@FrequencyResolver
	public Function<AllelePair, Double> getFrequencyResolver(DbiFrequencyResolver resolver, @FrequencyCacheMillis long duration, @FrequencyCacheSize long size) {
		return new CachingResolver<AllelePair, Double>(resolver, duration, duration, size);
	}

	/**
	 * resolve allele codes from alpha.v3.zip file
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
	
	/**
	 * be permissive about prefixes and allele codes and truncate all alleles to two fields
	 */
	@Provides
	@GlStringFilter
	public Function<String, String> getGlStringFilter(ArsAlleleFilter arsAlleleFilter, AlleleCodeFilter alleleCodeFilter, PermissiveAlleleFilter permissiveAlleleFilter) {
		return compose(arsAlleleFilter, compose(alleleCodeFilter, permissiveAlleleFilter));
	}
	
}
