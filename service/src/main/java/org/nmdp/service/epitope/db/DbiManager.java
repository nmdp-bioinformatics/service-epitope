package org.nmdp.service.epitope.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nmdp.service.epitope.domain.DetailRace;

public interface DbiManager {

    Map<String, Integer> getAlleleGroupMap();

    Integer getGroupForAllele(String allele);

    List<String> getAllelesForGroup(Integer group);

    Map<Integer, List<String>> getGroupAlleleMap();

    String getBroadRaceForDetailRace(String detailRace);

    Map<String, Double> getAlleleFrequenciesForDetailRace(String detailRace);

    List<String> getAllelesForCode(String locus, String code);

    Double getFrequency(String allele, DetailRace race);

    Set<DetailRace> getRacesWithFrequencies();

    void loadGGroups(Iterator<GGroupRow> rowIter, boolean reload);

    Long getDatasetDate(String dataset);

    void updateDatasetDate(String dataset, Long date);

    String getGGroupForAllele(String locus, String allele);

    Map<String, List<String>> getFamilyAlleleMap();

	void loadAlleles(Iterator<AlleleRow> rowIter, boolean reload);

}
