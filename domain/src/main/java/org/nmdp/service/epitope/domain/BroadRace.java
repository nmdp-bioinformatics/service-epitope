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

package org.nmdp.service.epitope.domain;

/**
 * Broad race groups
 */
public enum BroadRace {
	AFA("African American"),
	API("Asian Pacific Islander"),
	CAU("Caucasian"),
	HAWI("Hawaiian or other Pacific Islander Unspecified"),
	HIS("Hispanic"),
	NAM("North American Indian"),
	MULTI("Multiple Race"),
	OTH("Other"),
	DEC("Declines or No Race Selected"),
	UNK("Unknown/Question Not Asked");
	
	private String description;
	
	BroadRace(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
