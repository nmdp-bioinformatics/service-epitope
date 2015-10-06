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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

import org.junit.Test;

public class CachingFunctionTest {
	
	@Test
	public void testApply() throws Exception {
		Function<String, String> resolver = mock(Function.class);
		CachingFunction<String, String> cachingResolver = new CachingFunction<String, String>(resolver, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
		when(resolver.apply("key")).thenReturn("value1").thenReturn("value2");
		String test1 = cachingResolver.apply("key");
		String test2 = cachingResolver.apply("key");
		assertThat(test1, equalTo(test2));
		verify(resolver).apply("key");
	}
	
}
