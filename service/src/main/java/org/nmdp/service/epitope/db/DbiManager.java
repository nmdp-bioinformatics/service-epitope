package org.nmdp.service.epitope.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nmdp.service.epitope.domain.DetailRace;

public interface DbiManager {

    public abstract Map<String, Integer> getAlleleGroupMap();

    public abstract Integer getGroupForAllele(String allele);

    public abstract List<String> getAllelesForGroup(Integer group);

    public abstract Map<Integer, List<String>> getGroupAlleleMap();

    public abstract String getBroadRaceForDetailRace(String detailRace);

    public abstract Map<String, Double> getAlleleFrequenciesForDetailRace(String detailRace);

    public abstract List<String> getAllelesForCode(String locus, String code);

    public abstract Double getFrequency(String allele, DetailRace race);

    public abstract Set<DetailRace> getRacesWithFrequencies();

    public void loadGGroups(Iterator<GGroupRow> rowIter, boolean reload);

    Long getDatasetDate(String dataset);

    void updateDatasetDate(String dataset, Long date);

    String getGGroupForAllele(String locus, String allele);

}
