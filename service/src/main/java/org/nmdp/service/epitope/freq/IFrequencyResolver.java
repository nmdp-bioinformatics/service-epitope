package org.nmdp.service.epitope.freq;

import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.service.AllelePair;

import com.google.common.base.Function;

public interface IFrequencyResolver extends Function<AllelePair, Double> {

    public Double apply(AllelePair pair);
    public Double getFrequency(String allele, DetailRace race);

}
