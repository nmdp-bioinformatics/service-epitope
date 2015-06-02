package org.nmdp.service.epitope.guice;

import org.nmdp.service.epitope.domain.DetailRace;

class AlleleRace {
    
    private String allele;
    private DetailRace race;
    
    AlleleRace(String allele, DetailRace race) { 
        this.allele = allele; 
        this.race = race; 
    }
    
    public String getAllele() {
        return allele;
    }
    
    public DetailRace getRace() {
        return race;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allele == null) ? 0 : allele.hashCode());
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
        AlleleRace other = (AlleleRace) obj;
        if (allele == null) {
            if (other.allele != null)
                return false;
        } else if (!allele.equals(other.allele))
            return false;
        if (race != other.race)
            return false;
        return true;
    }
}