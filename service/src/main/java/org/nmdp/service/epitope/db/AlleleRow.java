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

public class AlleleRow {
    String locus;
    String allele;
    public AlleleRow(String allele) {
        int i = allele.indexOf('*');
        if (i < 0) throw new IllegalArgumentException("malformed allele: " + allele);
        this.locus = allele.substring(0, i); 
        if (!this.locus.startsWith("HLA-")) this.locus = "HLA-" + this.locus;
        this.allele = allele.substring(i+1);
    }
    public AlleleRow(String locus, String allele) {
        super();
        this.locus = locus;
        if (!this.locus.startsWith("HLA-")) this.locus = "HLA-" + this.locus;
        this.allele = allele;
    }
    public String getLocus() {
        return locus;
    }
    public String getAllele() {
        return allele;
    }
}
