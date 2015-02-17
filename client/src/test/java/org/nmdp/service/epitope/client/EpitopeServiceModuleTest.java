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

package org.nmdp.service.epitope.client;

import static org.junit.Assert.assertNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Test;
import org.nmdp.service.epitope.client.EndpointUrl;
import org.nmdp.service.epitope.client.EpitopeService;
import org.nmdp.service.epitope.client.EpitopeServiceModule;

/**
 * Unit test for EpitopeServiceModule.
 */
public final class EpitopeServiceModuleTest {
    private EpitopeServiceModule module;

    @Before
    public void setUp() {
        module = new EpitopeServiceModule();
    }

    @Test
    public void testConstructor() {
        assertNotNull(module);
    }

    @Test
    public void testEpitopeServiceModule() {
        Injector injector = Guice.createInjector(new TestModule(), module);
        EpitopeService epitopeService = injector.getInstance(EpitopeService.class);
        assertNotNull(epitopeService);
    }

    /**
     * Test module.
     */
    static final class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).annotatedWith(EndpointUrl.class).toInstance("http://localhost:8080");
        }
    }
}
