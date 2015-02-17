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

package org.nmdp.service.epitope.service;

import org.immunogenomics.gl.GenotypeList;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.domain.MatchResult;

/**
 * Service responsible for matching recipient and donor.
 */
public interface MatchService {

	/**
	 * Calculates a match result based on the given recipient and donor info
	 * @param recipientGl GenotypeList object of the recipient
	 * @param recipientRace DetailRace of the recipient
	 * @param donorGl GenotypeList object of the donor
	 * @param donorRace DetailRace of the donor
	 * @return the MatchResult of the recipient and donor
	 */
	MatchResult getMatch(GenotypeList recipientGl, DetailRace recipientRace, GenotypeList donorGl, DetailRace donorRace);

	/**
	 * Calculates a match result based on the given recipient and donor info
	 * @param recipientGl Glstring of the recipient
	 * @param recipientRace DetailRace of the recipient
	 * @param donorGl Glstring of the donor
	 * @param donorRace DetailRace of the donor
	 * @return the MatchResult of the recipient and donor
	 */
	MatchResult getMatch(String recipientGl, DetailRace recipientRace, String donorGl, DetailRace donorRace);
}
