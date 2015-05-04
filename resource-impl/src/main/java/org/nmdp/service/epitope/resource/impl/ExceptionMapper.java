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

package org.nmdp.service.epitope.resource.impl;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.nmdp.service.epitope.resource.impl.mime.MIMEParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.html.HtmlEscapers;

/**
 * Implementation of ExceptionMapper that handles all RuntimeExceptions 
 * and generates responses based on mime type negotation.
 */
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<RuntimeException> {

	static Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);
	
	private static final Escaper JSON_ESCAPER = Escapers.builder()
            .addEscape('"', "\\\"")
            .addEscape('\\', "\\\\")
            .addEscape('\\', "\\\\")
            .addEscape('/', "\\/")
            .addEscape('\n', "\\n")
            .addEscape('\b', "\\b")
            .addEscape('\t', "\\t")
            .addEscape('\f', "\\f")
            .addEscape('\r', "\\r")
            .build();
	private static final Escaper HTML_ESCAPER = HtmlEscapers.htmlEscaper();
	
	@Context 
	HttpServletRequest request;
	
	@Context
	HttpServletResponse response;
	
	static class ExceptionContext {
		private Integer code;
		private String message;
		private Exception exception;
		private String trace;
		public ExceptionContext(Integer code, String message, Exception exception) {
			this.code = code;
			this.message = message;
			this.exception = exception;
			if (exception != null) {
				StringBuilder sb = new StringBuilder();
				StringWriter sw = new StringWriter();
				exception.printStackTrace(new PrintWriter(sw));
				this.trace = sw.toString();
			}
		}
		public Integer getCode() {
			return code;
		}
		public String getMessage() {
			return getMessage(null);
		}
		public String getMessage(Escaper escaper) {
			return (null == escaper) ? message : escaper.escape(message);
		}
		public Exception getException() {
			return exception;
		}
		public String getTrace() {
			return getTrace(null);
		}
		public String getTrace(Escaper escaper) {
			return (null == escaper) ? trace : escaper.escape(trace);
		}
	}
	
	static final Map<String, Function<ExceptionContext, String>> entityBuilderMap;
	static {
		entityBuilderMap = new HashMap<>(); 
		entityBuilderMap.put("text/plain", new Function<ExceptionContext, String>() {
			@Override
			public String apply(ExceptionContext context) {
				return context.getMessage();
			}
		});
		entityBuilderMap.put("application/json", new Function<ExceptionContext, String>() { 
			@Override
			public String apply(ExceptionContext context) {
				return "{\"code\": \"" + context.getCode() + "\", \"message\": \"" 
						+ context.getMessage(JSON_ESCAPER) + "\"}";
			}
		});
		entityBuilderMap.put("text/html", new Function<ExceptionContext, String>() { 
			@Override
			public String apply(ExceptionContext context) {
				StringBuilder sb = new StringBuilder("<html><head/><body>")
					.append("<h2>Server Error:</h2>")
					.append("<h3>").append(context.getMessage(HTML_ESCAPER)).append("</h3>");
				if (null != context.getException()) {
					sb.append("<h3>trace</h3>")
						.append("<pre>").append(context.getTrace(HTML_ESCAPER)).append("</pre>");
				}
				sb.append("</body></html>");
				return sb.toString();
			}
		});
	}
	
	static String getEntity(String type, int code, String message, Exception ex) {
		Function<ExceptionContext, String> function = entityBuilderMap.get(type);
		if (null == function) {
			// fall back to text/plain, shouldn't happen
			logger.error("couldn't find template for mime type: " + type);
			function = entityBuilderMap.get("text/plain");
		}
		return function.apply(new ExceptionContext(code, message, ex));
	}

	/**
	 * Generate response for RuntimeException
	 * @return Response the response
	 */
	@Override
	public Response toResponse(RuntimeException exception) {
		logger.error("handling exception", exception);
		logger.debug("accepted response types: " + request.getHeader("Accept"));
		String matchedType = MIMEParse.bestMatch(entityBuilderMap.keySet(), request.getHeader("Accept"));
		if (null == matchedType || matchedType.equals("")) {
			// fall back to text/plain
			matchedType = "text/plain";
		}
		logger.debug("best matched response type: " + matchedType);
		int code = INTERNAL_SERVER_ERROR.getStatusCode();
		if (exception instanceof EpitopeServiceException) {
			code = ((EpitopeServiceException)exception).getCode();
		}
		String message = exception.getMessage();
		Response response = Response.status(code)
				.type(matchedType)
				.entity(getEntity(matchedType, code, message, exception))
				.build();
		return response;
	}
}
