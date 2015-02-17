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

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("test/")
@Produces(MediaType.APPLICATION_JSON)
@Api(value="Test", description="For testing purposes")
public class TestResource {

	@GET
	@Path("errorWithCode")
	@ApiOperation(value="Test method to demonstrate error handling with exception and error code", response=String.class)
	public String errorWithCode() {
		// demonstrate exception handling
		throw new WebApplicationException(new RuntimeException("errorWithCode"), 442);
	}
	
	@GET
	@Path("errorWithStatus")
	@ApiOperation(value="Test method to demonstrate error handling with exception and status enum", response=String.class)
	public String errorWithStatus() {
		// demonstrate exception handling
		throw new WebApplicationException(new RuntimeException("errorWithStatus"), INTERNAL_SERVER_ERROR);
	}

	@GET
	@Path("errorWithResponse")
	@ApiOperation(value="Test method to demonstrate error handling with exception and status enum", response=String.class)
	public String errorWithResponse() {
		throw createWebApplicationException(new RuntimeException("errorWithResponse"), 542);
	}
	
	private WebApplicationException createWebApplicationException(Exception e, int code) {
		StringBuilder sb = new StringBuilder();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		sb.append("<html><head/><body><h2>Server Error</h2><h3>Message:</h3><p>")
			.append(e.getMessage())
			.append("</p><h3>Trace:</h3><pre>")
			.append(sw.toString())
			.append("</pre></body></html>");
		Response response = Response.status(code)
				.type("text/html")
				.entity(sb.toString())
				.build();
		return new WebApplicationException(response);
	}
	
}
