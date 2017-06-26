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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.nmdp.gl.Allele;
import org.nmdp.gl.GenotypeList;
import org.nmdp.service.epitope.allelecode.DbiAlleleCodeResolver;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.DbiManagerImpl;
import org.nmdp.service.epitope.gl.*;
import org.nmdp.service.epitope.gl.transform.GlStringFunctions;
import org.nmdp.service.epitope.group.DbiImmuneGroupResolver;
import org.nmdp.service.epitope.guice.ConfigurationBindings.*;
import org.nmdp.service.epitope.guice.ConfigurationBindings.GenotypeListResolver;
import org.nmdp.service.epitope.service.*;
import org.nmdp.service.epitope.task.AlignedImmuneGroupInitializer;
import org.nmdp.service.epitope.task.ImmuneGroupInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static org.nmdp.service.epitope.task.URLProcessor.getUrls;

public class LocalServiceModule extends AbstractModule {

    Logger log = LoggerFactory.getLogger(getClass());
    
	@Override
	protected void configure() {
		
		// local gl client
		install(new LocalGlClientModule());
		
		// standard implementations
		bind(DbiManager.class).to(DbiManagerImpl.class);
		bind(EpitopeService.class).to(EpitopeServiceImpl.class).in(Singleton.class);
		bind(MatchService.class).to(MatchServiceImpl.class);
		bind(FrequencyService.class).to(FrequencyServiceImpl.class).in(Singleton.class);
		bind(ImmuneGroupInitializer.class).to(AlignedImmuneGroupInitializer.class);
	}
	
    @Provides
    @NmdpV3AlleleCodeUrls 
    public URL[] getNmdpV3AlleleCodeUrls(@NmdpV3AlleleCodeUrls String[] urls) {
        return getUrls(urls);
    }
    
    @Provides
    @ImgtHlaUrls
    public URL[] getImgtHlaUrls(@ImgtHlaUrls String[] urls) {
        return getUrls(urls);
    }
    
    @Provides
    @HlaAlleleUrls 
    public URL[] getHlaAlleleUrls(@HlaAlleleUrls String[] urls) {
        return getUrls(urls);
    }
    
    @Provides
    @HlaProtUrls 
    public URL[] getHlaProtUrls(@HlaProtUrls String[] urls) {
        return getUrls(urls);
    }

    private <T, R> Function<T, R> cache(Function<T, R> function, long duration, long period, long cacheCapacity) {
		//return new CachingFunction<>(function, duration, period, cacheCapacity);
		return function;
	}

	/**
	 * resolve allele codes from alpha.v3.zip file
	 */
	@Provides
	@Singleton
	@AlleleCodeResolver
	public Function<String, String> getAlleleCodeResolver(DbiAlleleCodeResolver resolver, @AlleleCodeCacheMillis long duration, @AlleleCodeCacheSize long size) {
		return cache(resolver, duration, duration, size);
	}
	
	/**
	 * resolve groups from internal sqlite db
	 */
	@Provides
	@Singleton
	@ImmuneGroupResolver
	public Function<Integer, List<Allele>> getImmuneGroupResolver(DbiImmuneGroupResolver resolver, @GroupCacheMillis long duration) {
		return cache(resolver, duration, duration, 3);
	}
	
	/**
	 * resolve gl strings using supplied glclient (see glclient bindings)
	 */
	@Provides
	@Singleton
	@GenotypeListResolver
	public Function<String, GenotypeList> getGenotypeListResolver(org.nmdp.service.epitope.gl.GenotypeListResolver resolver, @GlCacheMillis long duration, @GlCacheSize long size) {
		return cache(resolver, duration, duration, size);
	}
	
	/**
	 * performs transformation on GL string prior to converting to GenotypeList object
	 */
	@Provides
	@GlstringTransformer
	public Function<String, String> getGlstringTransformer(@AlleleCodeResolver Function<String, String> alleleCodeResolver) {
		return GlStringFunctions.normalizePrefixes("HLA-DPB1").andThen(
				GlStringFunctions.expandAlleleCodes(alleleCodeResolver));
	}
	
	/**
	 * for matching, also coalesce p-groups and truncate to ars resolution
	 */
	@Provides
	@MatchGlstringTransformer
	public Function<String, String> getMatchGlstringTransformer(
			@GlstringTransformer Function<String, String> glStringTransformer,
			DbiManager dbi)
	{
		return glStringTransformer
				.andThen(GlStringFunctions.normalizeGroups(dbi::getPGroupForAllele))
				.andThen(GlStringFunctions.trimAllelesToFields(2));
	}
	
}
