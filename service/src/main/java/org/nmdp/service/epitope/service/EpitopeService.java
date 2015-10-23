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

import java.util.List;
import java.util.Map;

import org.nmdp.gl.Allele;

/**
 * Interface for Epitope Service
 */
public interface EpitopeService {

	/**
	 * @param allele Allele object
	 * @return TCE group of the given allele, if known
	 */
	public Integer getGroupForAllele(Allele allele);

	/**
	 * @param allele Glstring for an allele
	 * @return TCE group for the given allele, if known
	 */
	public Integer getGroupForAllele(String allele);
	
	/**
	 * @return a Map of TCE group to list of alleles within each group.  
	 * All groups (1, 2, and 3) are included.
	 */
	public Map<Integer, List<Allele>> getAllGroups();
	
	/**
	 * @return map of Allele to the TCE group of the allele for all known alleles.
	 */
	public Map<Allele, Integer> getGroupsForAllAlleles();

	/**
	 * @param allele an Allele object
	 * @return the effective allele for the given allele, used to resolve the TCE group.
	 */
	public boolean isValidAllele(Allele allele);

	/**
	 * @return list of all known alleles
	 */
	public List<Allele> getAllAlleles();

	/**
	 * @param group a TCE group
	 * @return list of alleles for the given TCE group
	 */
	public List<Allele> getAllelesForGroup(Integer group);

	/**
	 * build TCE group/allele maps
	 */
	void buildAlleleGroupMaps();

}
