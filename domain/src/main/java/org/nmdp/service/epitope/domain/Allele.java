package org.nmdp.service.epitope.domain;

public class Allele {
    org.nmdp.gl.Allele allele;
    Integer group;
    DetailRace race;
    Double frequency;
    public Allele(org.nmdp.gl.Allele allele, Integer group, DetailRace race, Double frequency) {
        super();
        this.allele = allele;
        this.group = group;
        this.race = race;
        this.frequency = frequency;
    }
    public org.nmdp.gl.Allele getAllele() {
        return allele;
    }
    public Integer getGroup() {
        return group;
    }
    public DetailRace getRace() {
        return race;
    }
    public Double getFrequency() {
        return frequency;
    }
}
