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

package org.nmdp.service.epitope.gl.filter;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nmdp.service.epitope.db.DbiManager;

import com.google.common.base.Function;
import com.google.inject.Inject;

public class GGroupFilter implements Function<String, String> {

    // locus is not optional
    private static final Pattern ALLELE_PAT = Pattern.compile("(?<=^|[/~+|^])([^/~+|^*]*)\\*(\\d+:\\d+(?=:[A-Z0-9]+)*)(?=$|[/~+|^])", CASE_INSENSITIVE);

    private DbiManager dbi;

    @Inject
    public GGroupFilter(DbiManager dbi) {
        this.dbi = dbi;
    }
    
    /**
     * Replace all alleles with g group names, if applicable
     */
    @Override
    public String apply(String gl) {
        Matcher m = ALLELE_PAT.matcher(gl);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) sb.append(gl.substring(last, m.start()));
            String gg = dbi.getGGroupForAllele(m.group(1), m.group(2));
            if (null == gg) {
                sb.append(m.group());
            } else {
                sb.append(m.group(1) + "*" + gg);
            }
            last = m.end();
        }
        if (last < gl.length()) sb.append(gl.substring(last, gl.length()));
        return sb.toString();
    }

}
