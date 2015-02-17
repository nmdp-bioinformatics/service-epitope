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

package org.nmdp.service.epitope.tools;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.concurrent.Callable;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.dishevelled.commandline.ArgumentList;
import org.dishevelled.commandline.CommandLine;
import org.dishevelled.commandline.CommandLineParseException;
import org.dishevelled.commandline.CommandLineParser;
import org.dishevelled.commandline.Switch;
import org.dishevelled.commandline.Usage;
import org.dishevelled.commandline.argument.StringArgument;
import org.nmdp.service.epitope.client.EndpointUrl;
import org.nmdp.service.epitope.client.EpitopeService;
import org.nmdp.service.epitope.client.EpitopeServiceModule;

/**
 * Abstract epitope command line tool.
 */
abstract class AbstractEpitopeTool implements Callable<Integer> {
    private final File inputFile;
    private final File outputFile;
    private final EpitopeService epitopeService;
    static final String DEFAULT_ENDPOINT_URL = "http://epearsonone:8080/";

    protected AbstractEpitopeTool(final String endpointUrl, final File inputFile, final File outputFile) {
        checkNotNull(endpointUrl);

        Injector injector = Guice.createInjector(new EpitopeServiceModule(), new AbstractModule() {
                @Override
                protected void configure() {
                    bind(String.class).annotatedWith(EndpointUrl.class).toInstance(endpointUrl);
                }
            });

        this.inputFile = inputFile;
        this.outputFile = outputFile;
        epitopeService = injector.getInstance(EpitopeService.class);
    }


    protected final EpitopeService getEpitopeService() {
        return epitopeService;
    }

    protected final File getInputFile() {
        return inputFile;
    }

    protected final File getOutputFile() {
        return outputFile;
    }

    protected static Switch createAboutArgument() {
        return new Switch("a", "about", "display about message");
    }

    protected static Switch createHelpArgument() {
        return new Switch("h", "help", "display help message");
    }

    protected static StringArgument createEndpointUrlArgument() {
        return new StringArgument("u", "endpoint-url", "endpoint URL, default " + DEFAULT_ENDPOINT_URL, false);
    }
}
