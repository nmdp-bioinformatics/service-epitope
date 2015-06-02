package org.nmdp.service.epitope.service;

import org.nmdp.service.epitope.domain.DetailRace;

/**
 * The Frequency Service provides frequencies.  
 */
public interface FrequencyService {
    
    /**
     * Returns frequency for an allele within its population.
     * @param allele the glstring of the allele for which to return frequency.
     * @param race population for which to return frequency.
     * @return frequency of the allele within the specified population.
     */
    public double getFrequency(String allele, DetailRace race);
}
