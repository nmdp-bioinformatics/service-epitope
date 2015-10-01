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

import static com.google.common.base.Functions.compose;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.nmdp.gl.Allele;
import org.nmdp.gl.GenotypeList;
import org.nmdp.service.epitope.allelecode.AlleleCodeResolver;
import org.nmdp.service.epitope.allelecode.NmdpV3AlleleCodeResolver;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.DbiManagerImpl;
import org.nmdp.service.epitope.ggroup.DbiGGroupResolver;
import org.nmdp.service.epitope.ggroup.GGroupResolver;
import org.nmdp.service.epitope.gl.GlResolver;
import org.nmdp.service.epitope.gl.GlStringResolver;
import org.nmdp.service.epitope.gl.LocalGlClientModule;
import org.nmdp.service.epitope.gl.filter.AlleleCodeFilter;
import org.nmdp.service.epitope.gl.filter.ArsAlleleFilter;
import org.nmdp.service.epitope.gl.filter.GGroupFilter;
import org.nmdp.service.epitope.gl.filter.GlStringFilter;
import org.nmdp.service.epitope.gl.filter.PermissiveAlleleFilter;
import org.nmdp.service.epitope.group.DbiGroupResolver;
import org.nmdp.service.epitope.group.GroupResolver;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.AlleleCodeCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.FrequencyCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GGroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GlCacheSize;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GroupCacheMillis;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAlleleUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.HlaAmbigUrls;
import org.nmdp.service.epitope.guice.ConfigurationBindings.NmdpV3AlleleCodeUrls;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.EpitopeServiceImpl;
import org.nmdp.service.epitope.service.FrequencyResolver;
import org.nmdp.service.epitope.service.FrequencyService;
import org.nmdp.service.epitope.service.FrequencyServiceImpl;
import org.nmdp.service.epitope.service.MatchService;
import org.nmdp.service.epitope.service.MatchServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class LocalServiceModule extends AbstractModule {

    Logger log = LoggerFactory.getLogger(getClass());
    
	@Override
	protected void configure() {
		
		// local gl client
		install(new LocalGlClientModule());
		
		// standard implementations
		bind(DbiManager.class).to(DbiManagerImpl.class);
		bind(EpitopeService.class).to(EpitopeServiceImpl.class);
		bind(MatchService.class).to(MatchServiceImpl.class);
		bind(FrequencyService.class).to(FrequencyServiceImpl.class);
	}

	@Provides
	@Singleton
    public FrequencyResolver getFrequencyResolver(final DbiManager dbiManager, @FrequencyCacheMillis long duration, @FrequencyCacheSize long size) {
	    final CachingFunction<AlleleRace, Double> cf = 
	            new CachingFunction<AlleleRace, Double>(ar -> dbiManager.getFrequency(ar.getAllele(), ar.getRace()), duration, duration, size);
	    return (allele, race) -> cf.apply(new AlleleRace(allele, race));
    }
	
    @Provides
    @NmdpV3AlleleCodeUrls 
    public URL[] getNmdpV3AlleleCodeUrls(@NmdpV3AlleleCodeUrls String[] urls) {
        return getUrls(urls);
    }
    
    @Provides
    @HlaAmbigUrls 
    public URL[] getHlaAmbigUrls(@HlaAmbigUrls String[] urls) {
        return getUrls(urls);
    }
    
    @Provides
    @HlaAlleleUrls 
    public URL[] getHlaAlleleUrls(@HlaAlleleUrls String[] urls) {
        return getUrls(urls);
    }

    private URL[] getUrls(String[] urls) {
        return Arrays.stream(urls).map(url -> { 
            try {
                File f = new File(url);
                if (f.isFile()) return f.toURI().toURL();
                else return new URL(url);
            } catch (Exception e) {
                log.error("failed to handle url: " + url, e);
                return null; 
            }
        }).filter(u -> u != null).collect(Collectors.toList()).toArray(new URL[0]);
    }
    
	/**
	 * resolve allele codes from alpha.v3.zip file
	 */
	@Provides
	@Singleton
	@AlleleCodeResolver
	public Function<String, String> getAlleleCodeResolver(NmdpV3AlleleCodeResolver resolver, @AlleleCodeCacheMillis long duration, @AlleleCodeCacheSize long size) {
		return new CachingFunction<String, String>(resolver, duration, duration, size);
	}
	
	/**
	 * resolve groups from internal sqlite db
	 */
	@Provides
	@Singleton
	@GroupResolver
	public Function<Integer, List<Allele>> getGroupResolver(DbiGroupResolver resolver, @GroupCacheMillis long duration) {
		return new CachingFunction<Integer, List<Allele>>(resolver, duration, duration, 3);
	}
	
	/**
	 * resolve G groups from internal sqlite db
	 */
	@Provides
	@Singleton
	@GGroupResolver
	public Function<Allele, List<Allele>> getGGroupResolver(DbiGGroupResolver resolver, @GGroupCacheMillis long duration) {
		return new CachingFunction<Allele, List<Allele>>(resolver, duration, duration, 3);
	}

	/**
	 * resolve gl strings using supplied glclient (see glclient bindings)
	 */
	@Provides
	@Singleton
	@GlResolver
	public Function<String, GenotypeList> getGlResolver(GlStringResolver resolver, @GlCacheMillis long duration, @GlCacheSize long size) {
		return new CachingFunction<String, GenotypeList>(resolver, duration, duration, size);
	}
	
	/**
	 * be permissive about prefixes and allele codes and truncate all alleles to two fields
	 */
	@Provides
	@GlStringFilter
	public Function<String, String> getGlStringFilter(ArsAlleleFilter arsAlleleFilter, GGroupFilter gGroupFilter, AlleleCodeFilter alleleCodeFilter, PermissiveAlleleFilter permissiveAlleleFilter) {
	    // todo: check validity
	    return compose(arsAlleleFilter, compose(gGroupFilter, compose(alleleCodeFilter, permissiveAlleleFilter)));
	}
	
}
