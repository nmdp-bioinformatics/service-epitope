package org.nmdp.service.epitope.dropwizard;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.flywaydb.core.Flyway;

public abstract class FlywayMigrationBundle<T extends io.dropwizard.Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public abstract DataSourceFactory getDataSourceFactory(T configuration);

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        DataSourceFactory dataSourceFactory = getDataSourceFactory(configuration);

        // Create the Flyway instance using the builder pattern
        Flyway flyway = Flyway.configure()
                .dataSource(
                        dataSourceFactory.getUrl(),
                        dataSourceFactory.getUser(),
                        dataSourceFactory.getPassword()
                )
                .load();

        flyway.migrate();
    }
}
