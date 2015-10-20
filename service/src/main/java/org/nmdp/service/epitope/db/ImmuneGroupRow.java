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

package org.nmdp.service.epitope.db;

public class ImmuneGroupRow extends AlleleRow {
    Integer immuneGroup;
    public ImmuneGroupRow(String allele, Integer immuneGroup) {
    	super(allele);
        this.immuneGroup = immuneGroup;
    }
    public ImmuneGroupRow(String locus, String allele, Integer immuneGroup) {
        super(locus, allele);
        this.immuneGroup = immuneGroup;
    }
    public Integer getImmuneGroup() {
        return immuneGroup;
    }
	@Override
	public String toString() {
		return "ImmuneGroupRow [locus=" + locus + ", allele=" + allele + ", immuneGroup=" + immuneGroup + "]";
	}
}
