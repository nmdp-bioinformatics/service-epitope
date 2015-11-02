package org.nmdp.service.epitope.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.domain.DetailRace;
import org.nmdp.service.epitope.guice.ConfigurationBindings.BaselineAlleleFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FrequencyServiceImpl implements FrequencyService {

    private Double baselineFrequency;
    Map<DetailRace, Map<String, Double>> raceAlleleFreqMap;
	private DbiManager dbi;
	Logger logger = LoggerFactory.getLogger(getClass());
    
    @Inject
    public FrequencyServiceImpl(DbiManager dbi, @BaselineAlleleFrequency Double baselineFrequency) {
        this.dbi = dbi;
    	this.baselineFrequency = baselineFrequency;
    }
    
	@Override
	public void buildFrequencyMap() {
		logger.info("building frequency map");
		Map<DetailRace, Map<String, Double>> map = dbi.getRaceAlleleFrequencyMap();
		List<String> alleles = dbi.getAllelesForLocus("HLA-DPB1");
		for (Map.Entry<DetailRace, Map<String, Double>> entry: map.entrySet()) {
			Map.Entry<DetailRace, Map<String, Double>> e = entry;
			alleles.stream().forEach(a -> addAlleleToMap(e.getValue(), a));
		}
		this.raceAlleleFreqMap = map;
		logger.debug("done building frequency map");
	}
	
    /**
	 * Add the ARS version of the allele to the frequency map
	 */
	private void addAlleleToMap(Map<String, Double> map, String allele) {
		if (map.containsKey(allele)) return;
		int to = 0;
		for (int i = 0; i < 2; i++) {
			to = allele.indexOf(":", to+1);
		}
		if (to == -1) return;
		Double arsFreq = map.get(allele.substring(0, to));
		if (null != arsFreq) map.put(allele, arsFreq);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public double getFrequency(DetailRace race, String allele) {
    	if (!raceAlleleFreqMap.containsKey(race)) {
    		return baselineFrequency;
    	}
    	return Optional.ofNullable(raceAlleleFreqMap.get(race).get(allele)).orElse(0.0);
    }

}
