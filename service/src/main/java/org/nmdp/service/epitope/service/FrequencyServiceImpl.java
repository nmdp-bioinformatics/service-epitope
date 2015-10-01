package org.nmdp.service.epitope.service;

import java.util.Set;

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;

import com.google.inject.Inject;

public class FrequencyServiceImpl implements FrequencyService {

    private FrequencyResolver resolver;
    private Set<DetailRace> raceFreqs;
    private Double baselineFrequency;
    
    @Inject
    public FrequencyServiceImpl(FrequencyResolver resolver, DbiManager dbiManager, @BaselineAlleleFrequency Double baselineFrequency) {
        this.resolver = resolver;
        this.baselineFrequency = baselineFrequency;
        this.raceFreqs = dbiManager.getRacesWithFrequencies();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getFrequency(String allele, DetailRace race) {
        Double d = resolver.getFrequency(allele, race);
        if (null != d) {
            return d;
        } else {
            if (raceFreqs.contains(race)) {
                return 0;
            } else {
                return baselineFrequency;
            }
        }
    }

}
