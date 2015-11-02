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

package org.nmdp.service.epitope.dropwizard;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;
import org.nmdp.service.common.domain.ConfigurationModule;
import org.nmdp.service.common.dropwizard.CommonServiceApplication;
import org.nmdp.service.epitope.guice.ConfigurationBindings;
import org.nmdp.service.epitope.guice.ConfigurationBindings.RefreshMillis;
import org.nmdp.service.epitope.guice.LocalServiceModule;
import org.nmdp.service.epitope.resource.impl.AlleleResource;
import org.nmdp.service.epitope.resource.impl.GroupResource;
import org.nmdp.service.epitope.resource.impl.MatchResource;
import org.nmdp.service.epitope.resource.impl.ResourceModule;
import org.nmdp.service.epitope.service.EpitopeService;
import org.nmdp.service.epitope.service.FrequencyService;
import org.nmdp.service.epitope.task.AlleleCodeInitializer;
import org.nmdp.service.epitope.task.AlleleInitializer;
import org.nmdp.service.epitope.task.GGroupInitializer;
import org.nmdp.service.epitope.task.ImmuneGroupInitializer;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.model.ApiInfo;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Dropwizard main application wrapper
 */
public class EpitopeServiceApplication extends CommonServiceApplication<EpitopeServiceConfiguration> {
	
	Logger log = Logger.getLogger(getClass()); 
	
	/**
	 * Application main method
	 */
	public static void main(String[] args) throws Exception {
        new EpitopeServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "EpitopeService";
    }

    /**
     * Dropwizard application initialization
     */
    @Override
    public void initializeService(Bootstrap<EpitopeServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new FlywayMigrationBundle<EpitopeServiceConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(EpitopeServiceConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    	bootstrap.addBundle(new DBIExceptionsBundle());
    	bootstrap.addBundle(new FlywayBundle<EpitopeServiceConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(EpitopeServiceConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
            @Override
            public FlywayFactory getFlywayFactory(EpitopeServiceConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });
    }

    /**
     * Dropwizard service runner.  Application resources are registered here.
     */
	@Override
	public void runService(final EpitopeServiceConfiguration configuration, final Environment environment) throws Exception {
		
	    Injector injector = Guice.createInjector(
    			new ConfigurationModule(ConfigurationBindings.class, configuration), 
    			new LocalServiceModule(), 
    			new ResourceModule(),
    			new AbstractModule() {
					@Override protected void configure() {
						DBI dbi = new DBIFactory().build(environment, configuration.getDataSourceFactory(), "sqlite");
						bind(DBI.class).toInstance(dbi);
					}});

	    environment.getObjectMapper()
    			.enable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(Include.NON_NULL)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

	    // todo: generalize dependency graph (latches?)
		//Runnable initializers = serial(
		//    	parallel(
		//    		() -> injector.getInstance(GGroupInitializer.class).loadGGroups(),
		//    		() -> injector.getInstance(AlleleCodeInitializer.class).loadAlleleCodes(),
		//    		serial(
		//    			() -> injector.getInstance(AlleleInitializer.class).loadAlleles()),
		//    			() -> injector.getInstance(ImgtImmuneGroupInitializer.class).loadAlleleScores()),
		//    	parallel(
		//    		() -> injector.getInstance(EpitopeService.class).buildMaps(),
		//    		() -> injector.getInstance(FrequencyService.class).buildFrequencyMap(),
		//    		() -> injector.getInstance(DbiAlleleCodeResolver.class).buildAlleleCodeMap()));

	    Runnable initializers = serial(
	        		() -> injector.getInstance(GGroupInitializer.class).loadGGroups(),
	        		() -> injector.getInstance(AlleleCodeInitializer.class).loadAlleleCodes(),
        			() -> injector.getInstance(AlleleInitializer.class).loadAlleles(),
        			() -> injector.getInstance(ImmuneGroupInitializer.class).loadImmuneGroups(),
	        		() -> injector.getInstance(EpitopeService.class).buildAlleleGroupMaps(),
	        		() -> injector.getInstance(FrequencyService.class).buildFrequencyMap());
//	        		() -> injector.getInstance(DbiAlleleCodeResolver.class).buildAlleleCodeMap());

	    long refreshMillis = injector.getInstance(Key.get(Long.class, RefreshMillis.class));
	    
	    environment.lifecycle().manage(new Managed() {
	    	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
	    		Thread t = new Thread(r, "InitializerThread");
	    		t.setDaemon(true);
	    		return t;
	    	});
	    	@Override public void stop() throws Exception {
	    		scheduler.shutdownNow();
	    	}
	    	@Override public void start() throws Exception {
	    		Future<?> init = scheduler.submit(initializers);
	    		init.get();
	    		scheduler.scheduleAtFixedRate(initializers, refreshMillis, refreshMillis, MILLISECONDS);
	    	}
	    });

    	final AlleleResource alleleResource = injector.getInstance(AlleleResource.class);
    	environment.jersey().register(alleleResource);
    	
    	final GroupResource groupResource = injector.getInstance(GroupResource.class);
    	environment.jersey().register(groupResource);
    	
    	final MatchResource matchResource = injector.getInstance(MatchResource.class);
    	environment.jersey().register(matchResource);

    	environment.jersey().register(new org.nmdp.service.epitope.resource.impl.ExceptionMapper());
    	
    	// eriktodo: multibinder for health checks
    	final GlClientHealthCheck glClientHealthCheck = injector.getInstance(GlClientHealthCheck.class);
    	environment.healthChecks().register("glClient",  glClientHealthCheck);
    }
//	Managed() {
//    @Override public void stop() throws Exception {}
//    @Override public void start() throws Exception {
//    }
	
	public Runnable serial(Runnable... hooks) {
        return () -> { Arrays.stream(hooks).forEachOrdered(hook -> {
        	try { 
        		log.debug("running hook (in serial): " + hook.getClass().getSimpleName());
        		hook.run(); 
        	} catch (Exception e) { 
        		log.error("hook failed", e); 
        	}
        }); };
	}
	
	public Runnable parallel(Runnable... hooks) {
		return () -> { Arrays.stream(hooks).parallel().forEach(hook -> {
        	try { 
        		log.debug("running hook (in parallel): " + hook.getClass().getSimpleName());
        		hook.run(); 
        	} catch (Exception e) { 
        		log.error("hook failed", e); 
        	}
		}); };
	}
	
	@Override
	public void configureSwagger(SwaggerConfig config) {
	    config.setApiVersion("1.0");
	  	config.setApiInfo(new ApiInfo("Epitope Service", 
	  			"This service reports on alleles and their associated immunogenicity groups and provides matching functions.",
	  			null, // terms of service url 
	  			"epearson@nmdp.org", // contact
	  			null, // license
	  			null)); // license url
	}
}
