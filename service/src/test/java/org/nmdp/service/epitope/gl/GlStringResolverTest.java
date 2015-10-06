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

package org.nmdp.service.epitope.gl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.gl.GenotypeList;
import org.nmdp.gl.client.GlClient;
import org.nmdp.service.epitope.EpitopeServiceTestData;

@RunWith(MockitoJUnitRunner.class)
public class GlStringResolverTest {

	@Mock
	private GlClient glClient;

	@Mock
	private Function<String, String> filter;

	private GlStringResolver resolver;

	@Before
	public void setup() {
		resolver = new GlStringResolver(glClient, filter);
	}
	
	@Test
	public void testApply() throws Exception {
		String gl = "test";
		when(filter.apply(gl)).thenReturn(gl);
		GenotypeList gtl = EpitopeServiceTestData.aGenotypeList();
		when(glClient.createGenotypeList(gl)).thenReturn(gtl);
		GenotypeList test = resolver.apply(gl);
		verify(filter).apply(gl);
		verify(glClient).createGenotypeList(gl);
		assertThat(test, equalTo(gtl));
	}

}
