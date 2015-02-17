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

package org.nmdp.service.epitope.resource.impl;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * See https://github.com/jersey/jersey/blob/2.5.1/examples/json-jackson/src/main/java/org/glassfish/jersey/examples/jackson/MyObjectMapperProvider.java
 * 
 */
@Provider
public class JerseyObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

	final ObjectMapper defaultObjectMapper;

	public JerseyObjectMapperContextResolver() {
		defaultObjectMapper = createObjectMapper();
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.setSerializationInclusion(Include.NON_NULL);
		return mapper;
	}
		
}
