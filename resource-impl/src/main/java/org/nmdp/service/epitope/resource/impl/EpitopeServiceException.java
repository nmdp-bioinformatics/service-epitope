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

import javax.ws.rs.core.Response.Status;

/**
 * Base exception for epitope service that cooperates with ExceptionMapper 
 */
public class EpitopeServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	static final int DEFAULT_CODE = Status.INTERNAL_SERVER_ERROR.getStatusCode();
	
	final int code;
	
	/**
	 * construct exception 
	 */
	public EpitopeServiceException() {
		this(DEFAULT_CODE);
	}

	/**
	 * construct exception with specified message, cause, suppression, and writableStackTrace
	 * 
	 * @param message message of the exception
	 * @param cause cause of the exception
	 * @param enableSuppression enable suppression
	 * @param writableStackTrace whether the stack trace should be writable
	 */
	public EpitopeServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		this(DEFAULT_CODE, message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * construct exception with specified message and cause
	 * @param message message of the exception
	 * @param cause cause of the exception
	 */
	public EpitopeServiceException(String message, Throwable cause) {
		this(DEFAULT_CODE, message, cause);
	}

	/**
	 * construct exception with the specified message
	 * @param message message of the exception
	 */
	public EpitopeServiceException(String message) {
		this(DEFAULT_CODE, message);
	}

	/**
	 * construct exception cause by the specified throwable
	 * @param cause causing Throwable
	 */
	public EpitopeServiceException(Throwable cause) {
		this(DEFAULT_CODE, cause);
	}
	
	/**
	 * construct exception with specified HTTP status code
	 * @param code HTTP status code
	 */
	public EpitopeServiceException(int code) {
		super();
		this.code = code;
	}

	/**
	 * construct exception with specified status code, message, cause, suppression, and writableStackTrace
	 * 
	 * @param code HTTP status code
	 * @param message message of the exception
	 * @param cause cause of the exception
	 * @param enableSuppression enable suppression
	 * @param writableStackTrace whether the stack trace should be writable
	 */
	public EpitopeServiceException(int code, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.code = code;
	}

	/**
	 * construct exception with specified status code, message, cause
	 * 
	 * @param code HTTP status code
	 * @param message message of the exception
	 * @param cause cause of the exception
	 */
	public EpitopeServiceException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	/**
	 * construct exception with specified status code and message
	 * 
	 * @param code HTTP status code
	 * @param message message of the exception
	 */
	public EpitopeServiceException(int code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * construct exception with specified status code and cause
	 * 
	 * @param code HTTP status code
	 * @param cause cause of the exception
	 */
	public EpitopeServiceException(int code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	/**
	 * @return HTTP status code for the exception
	 */
	public int getCode() {
		return this.code;
	}
}
