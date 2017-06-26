package org.nmdp.service.epitope.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nmdp.service.epitope.domain.DetailRace;

public interface DbiManager {

    Map<String, Integer> getAlleleGroupMap();

    List<String> getAllelesForImmuneGroup(Integer group);

    Map<Integer, List<String>> getGroupAlleleMap();

    List<String> getAllelesForCode(String locus, String code);

    Double getFrequency(String allele, DetailRace race);

    Set<DetailRace> getRacesWithFrequencies();

    void loadPGroups(Iterator<GroupRow<String>> rowIter, boolean reload);

    void loadGGroups(Iterator<GroupRow<String>> rowIter, boolean reload);

    Long getDatasetDate(String dataset);

    void updateDatasetDate(String dataset, Long date);

    String getGGroupForAllele(String allele);

    String getPGroupForAllele(String allele);

    List<String> getGGroupAllelesForAllele(String allele);

    List<String> getPGroupAllelesForAllele(String allele);

    Map<String, Set<String>> getFamilyAlleleMap();

	void loadAlleles(Iterator<AlleleRow> rowIter, boolean reload);

	void loadAlleleCodes(Iterator<AlleleCodeRow> rowIter, boolean reload);

	List<String> getAllelesForLocus(String string);

	Map<DetailRace, Map<String, Double>> getRaceAlleleFrequencyMap();

	void loadImmuneGroups(Iterator<GroupRow<Integer>> rowIter, boolean reload);

	Iterator<AlleleCodeRow> getAlleleCodes();

}
