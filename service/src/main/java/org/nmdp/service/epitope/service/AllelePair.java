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

import org.nmdp.gl.Allele;
import org.nmdp.service.epitope.domain.DetailRace;

/**
 * Represents a pair of copied alleles (as in of a genotype), along with the DetailRace of the subject. 
 * Generally used to represent a possible combination of alleles for within a GenotypeList, with which
 * a MatchGrade and probability is associated.
 */
public class AllelePair {

	private Allele a1;
	private Integer g1;
	private Allele a2;
	private Integer g2;
	private DetailRace race;

	/**
	 * construct AllelePair with specified alleles and race.
	 */
	public AllelePair(Allele a1, Integer g1, Allele a2, Integer g2, DetailRace race) {
		// normalize order
		if (a1.getGlstring().compareTo(a2.getGlstring()) < 0) {
		    this.a1 = a1;
		    this.g1 = g1;
		    this.a2 = a2;
		    this.g2 = g2;
		} else {
		    this.a1 = a2;
		    this.g1 = g2;
		    this.a2 = a1;
		    this.g2 = g1;
		}
		this.race = race;
	}

    @Override
	public String toString() {
		return "AllelePair [a1=" + a1 + "(g:" + g1 + "), a2=" + a2 + "(g:" + g2 + "), race=" + race + "]";
	}

	public Allele getA1() {
		return a1;
	}
	
	public Integer getG1() {
	    return g1;
	}

	public Allele getA2() {
		return a2;
	}

    public Integer getG2() {
        return g2;
    }

    public Integer getLowG() {
        if (g1 == null || g2 == null) return null;
        return (g1.compareTo(g2) < 0) ? g1 : g2;
    }
    
    public Integer getHighG() {
        if (g1 == null || g2 == null) return null;
        return (g1.compareTo(g2) > 0) ? g1 : g2;
    }
    
    public DetailRace getRace() {
		return race;
	}

    public boolean typeEquals(AllelePair other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (a1 == null) {
            if (other.a1 != null)
                return false;
        } else if (!a1.equals(other.a1))
            return false;
        if (a2 == null) {
            if (other.a2 != null)
                return false;
        } else if (!a2.equals(other.a2))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a1 == null) ? 0 : a1.hashCode());
        result = prime * result + ((a2 == null) ? 0 : a2.hashCode());
        result = prime * result + ((g1 == null) ? 0 : g1.hashCode());
        result = prime * result + ((g2 == null) ? 0 : g2.hashCode());
        result = prime * result + ((race == null) ? 0 : race.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AllelePair other = (AllelePair) obj;
        if (a1 == null) {
            if (other.a1 != null)
                return false;
        } else if (!a1.equals(other.a1))
            return false;
        if (a2 == null) {
            if (other.a2 != null)
                return false;
        } else if (!a2.equals(other.a2))
            return false;
        if (g1 == null) {
            if (other.g1 != null)
                return false;
        } else if (!g1.equals(other.g1))
            return false;
        if (g2 == null) {
            if (other.g2 != null)
                return false;
        } else if (!g2.equals(other.g2))
            return false;
        if (race != other.race)
            return false;
        return true;
    }
    
}
