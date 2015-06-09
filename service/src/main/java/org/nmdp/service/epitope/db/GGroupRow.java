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

public class GGroupRow {
    String gGroup;
    String locus;
    String allele;
    public GGroupRow(String gGroup, String allele) {
        int i = allele.indexOf('*');
        int j = gGroup.indexOf('*');
        if (i < 0) throw new IllegalArgumentException("malformed allele: " + allele);
        if (j < 0) throw new IllegalArgumentException("malformed g-group: " + gGroup);
        if (i != j) throw new IllegalArgumentException("locus doesn't match between gGroup and allele (group: " + gGroup + ", allele: " + allele + ")");
        this.gGroup = gGroup.substring(j+1);
        this.locus = allele.substring(0, i); 
        if (!this.locus.startsWith("HLA-")) this.locus = "HLA-" + this.locus;
        this.allele = allele.substring(i+1);
    }
    public GGroupRow(String gGroup, String locus, String allele) {
        super();
        int j = gGroup.indexOf('*');
        if (j < 0) throw new IllegalArgumentException("malformed g-group: " + gGroup);
        if (!locus.equals(gGroup.substring(0, j))) throw new IllegalArgumentException("specified locus doesn't match gGroup (group: " + gGroup + ", locus: " + locus + ")");
        this.gGroup = gGroup.substring(j+1);
        this.locus = locus;
        if (!this.locus.startsWith("HLA-")) this.locus = "HLA-" + this.locus;
        this.allele = allele;
    }
    public String getGGroup() {
        return gGroup;
    }
    public String getLocus() {
        return locus;
    }
    public String getAllele() {
        return allele;
    }
}
