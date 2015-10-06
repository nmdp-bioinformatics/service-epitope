package org.nmdp.service.epitope.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nmdp.service.epitope.domain.DetailRace;

public interface DbiManager {

    Map<String, Integer> getAlleleGroupMap();

    List<String> getAllelesForGroup(Integer group);

    Map<Integer, List<String>> getGroupAlleleMap();

    List<String> getAllelesForCode(String locus, String code);

    Double getFrequency(String allele, DetailRace race);

    Set<DetailRace> getRacesWithFrequencies();

    void loadGGroups(Iterator<GGroupRow> rowIter, boolean reload);

    Long getDatasetDate(String dataset);

    void updateDatasetDate(String dataset, Long date);

    String getGGroupForAllele(String allele);

    List<String> getGGroupAllelesForAllele(String allele);

    Map<String, List<String>> getFamilyAlleleMap();

	void loadAlleles(Iterator<AlleleRow> rowIter, boolean reload);

	List<String> getAllelesForLocus(String string);

	Map<DetailRace, Map<String, Double>> getRaceAlleleFrequencyMap();

}
