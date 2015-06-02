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

package org.nmdp.service.epitope.service;

import org.nmdp.service.epitope.domain.DetailRace;

/**
 * Operation that returns a frequency for the given allele and {@link DetailRace}. 
 * <p>
 * This is a {@link FunctionalInterface} whose functional method is {@link #getFrequency(String, DetailRace)}.
 */
@FunctionalInterface
public interface FrequencyResolver {

    /**
     * Returns a frequency for the given allele and {@link DetailRace}.
     * @param allele the allele for which to return a frequency.
     * @param race the population for which to return a frequency.
     * @return a frequency for the given allele and {@link DetailRace} if known, or null otherwise.
     */
    public Double getFrequency(String allele, DetailRace race);    
}
