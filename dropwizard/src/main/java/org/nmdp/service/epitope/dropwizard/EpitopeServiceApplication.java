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

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jdbi.bundles.DBIExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.flywaydb.core.Flyway;
import org.nmdp.service.common.domain.ConfigurationModule;
import org.nmdp.service.common.dropwizard.CommonServiceApplication;
import org.nmdp.service.epitope.guice.ConfigurationBindings;
import org.nmdp.service.epitope.guice.LocalServiceModule;
import org.nmdp.service.epitope.resource.impl.AlleleResource;
import org.nmdp.service.epitope.resource.impl.GroupResource;
import org.nmdp.service.epitope.resource.impl.MatchResource;
import org.nmdp.service.epitope.resource.impl.ResourceModule;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.model.ApiInfo;

/**
 * Dropwizard main application wrapper
 */
public class EpitopeServiceApplication extends CommonServiceApplication<EpitopeServiceConfiguration> {
	
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
