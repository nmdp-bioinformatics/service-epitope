/*

    epitope-service  T-cell epitope group matching service for HLA-DPB1 locus.
    Copyright (c) 2014-2015 National Marrow Donor Program (NMDP)
    
    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.
    
    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.
    
    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
    
    > http://www.gnu.org/licenses/lgpl.html

*/

package org.nmdp.service.epitope.db;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.nmdp.service.epitope.domain.DetailRace;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.PreparedBatch;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.logging.SLF4JLog;
import org.skife.jdbi.v2.logging.SLF4JLog.Level;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.DoubleMapper;
import org.skife.jdbi.v2.util.LongMapper;
import org.skife.jdbi.v2.util.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.inject.Inject;

/*
 * All string versions of alleles passed into or out of methods include locus.
 */
public class DbiManagerImpl implements DbiManager {

	private final DBI dbi;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public DbiManagerImpl(DBI dbi) {
		this.dbi = dbi;
		dbi.setSQLLog(new SLF4JLog(logger, Level.TRACE));
	}
	
	ResultSetMapper<String> LOCUS_ALLELE = (i, r, c) -> r.getString("locus") + "*" + r.getString("allele");

	Pattern ALLELE_PATTERN = Pattern.compile("(?<locus>[^*]+)\\*(?<allele>.*)");

    /** 
     * {@inheritDoc} 
     */
	@Override
    public Map<String, Integer> getAlleleGroupMap() {
		try (Handle handle = dbi.open()) {
			Query<Map<String, Object>> query = handle.createQuery(
					"select locus, allele, immune_group from allele_group");
			return StreamSupport.stream(query.spliterator(), false)
			        .collect(Collectors.<Map<String, Object>, String, Integer>toMap(
			        m -> (String)m.get("locus") + "*" + m.get("allele"),
			        m -> (Integer)m.get("immune_group")));
		}
	}		
	
    /** 
     * {@inheritDoc} 
     */
	@Override
    public List<String> getAllelesForImmuneGroup(Integer group) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select locus, allele from allele_group where immune_group = :group")
					.bind("group", group)
					.map(LOCUS_ALLELE)
					.list();
 		}
	}
	
    /** 
     * {@inheritDoc} 
     */
	@Override
    public Map<Integer, List<String>> getGroupAlleleMap() {
		// todo simpler with ordered list
		try (Handle handle = dbi.open()) {
			Query<Map<String, Object>> query = handle.createQuery(
					"select immune_group, locus, allele from allele_group order by immune_group, allele");
			return StreamSupport.stream(query.spliterator(), false)
			        .collect(groupingBy(m -> (Integer)m.get("immune_group"),
			                mapping(n -> n.get("locus") + "*" + n.get("allele"), 
			                        Collectors.toList())));
		}
	}
	
	@Override
	public Map<DetailRace, Map<String, Double>> getRaceAlleleFrequencyMap() {
		try (Handle handle = dbi.open()) {
			Query<Map<String, Object>> query = handle.createQuery(
					"select brf.locus, dr.detail_race, brf.allele, ifnull(drf.frequency, brf.frequency) freq"
					//+ " ifnull(drf.detail_race, 'broad:' || brf.detail_race)"
					+ " from detail_race dr"
					+ " join race_freq brf on dr.broad_race = brf.detail_race"
					+ " left join race_freq drf on dr.detail_race = drf.detail_race"
					+ "     and brf.locus = drf.locus"
					+ "     and brf.allele = drf.allele"
					+ " where brf.locus = 'HLA-DPB1'");
			return StreamSupport.stream(query.spliterator(), false).collect(
					Collectors.groupingBy(
							m -> DetailRace.valueOf((String)m.get("detail_race")), 
			        		Collectors.toMap(
			        				m -> (String)m.get("locus") + "*" + (String)m.get("allele"), 
			        				m -> (Double)m.get("freq"))));
		}
	}
	
    /** 
     * {@inheritDoc} 
     */
	@Override
    public List<String> getAllelesForCode(String locus, String code) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select locus, allele from allele_code_map where locus = :locus and code = :code")
					.bind("locus", locus)
					.bind("code", code)
					.map(LOCUS_ALLELE)
					.list();
		}
	}
	
    /** 
     * {@inheritDoc} 
     */
	@Override
    public Double getFrequency(String allele, DetailRace race) {
		try (Handle handle = dbi.open()) {
        	List<String> alleleParts = Splitter.on('*').splitToList(allele);
			return handle.createQuery(
					"select ifnull(drf.frequency, brf.frequency)"
					+ " from detail_race dr"
					+ " join race_freq brf on dr.broad_race = brf.detail_race"
					+ " left join race_freq drf on dr.detail_race = drf.detail_race"
					+ " where dr.detail_race = :race"
					+ " and brf.locus = :locus"
					+ " and brf.allele = :allele"
					+ " and (drf.allele is null or "
					+ "     (drf.allele = :allele and drf.locus = :locus));")
					.bind("locus", alleleParts.get(0))
					.bind("allele", alleleParts.get(1))
					.bind("race", race)
					.map(DoubleMapper.FIRST)
					.first();
		}
	}

	@Override
    public Set<DetailRace> getRacesWithFrequencies() {
        try (Handle handle = dbi.open()) {
            return handle.createQuery("select distinct detail_race from race_freq")
                .map(StringMapper.FIRST)
                .list()
                .stream()
                .map(r -> DetailRace.valueOf(r))
                .collect(Collectors.toSet());
        }
    }

    @Override
    public Map<String, Set<String>> getFamilyAlleleMap() {
        try (Handle handle = dbi.open()) {
            return StreamSupport.stream(handle.createQuery("select distinct allele from ("
            		+ " select allele from race_freq where locus='HLA-DPB1'"
            		+ " union select allele from allele_group where locus='HLA-DPB1'"
            		+ " union select allele from hla_g_group where locus='HLA-DPB1')")
                .map(StringMapper.FIRST)
                .spliterator(), false)
                .collect(Collectors.groupingBy(a -> a.substring(0, a.indexOf(":")), Collectors.toSet()));
        }
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public Long getDatasetDate(String dataset) {
        try (Handle handle = dbi.open()) {
            return handle.createQuery("select modification_date from dataset_date where dataset = :dataset")
                    .bind("dataset", dataset)
                    .map(LongMapper.FIRST)
                    .first();
        }
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public void updateDatasetDate(String dataset, Long date) {
        try (Handle handle = dbi.open()) {
            handle.createStatement("insert or replace into dataset_date (dataset, modification_date) values (:dataset, :date)")
                .bind("dataset", dataset)
                .bind("date", date)
                .execute();
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadPGroups(Iterator<GroupRow<String>> rowIter, boolean reload) {
		try (Handle handle = dbi.open()) {
			if (reload) {
				handle.createStatement("delete from hla_p_group").execute();
			}
			PreparedBatch batch = handle.prepareBatch("insert or ignore into hla_p_group(p_group, locus, allele) values (?, ?, ?)");
			rowIter.forEachRemaining(g -> batch.add(g.getGroup(), g.getLocus(), g.getAllele()));
			batch.execute();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadGGroups(Iterator<GroupRow<String>> rowIter, boolean reload) {
		try (Handle handle = dbi.open()) {
			if (reload) {
				handle.createStatement("delete from hla_g_group").execute();
			}
			PreparedBatch batch = handle.prepareBatch("insert or ignore into hla_g_group(g_group, locus, allele) values (?, ?, ?)");
			rowIter.forEachRemaining(g -> batch.add(g.getGroup(), g.getLocus(), g.getAllele()));
			batch.execute();
		}
	}

	/**
     * {@inheritDoc} 
     */
    @Override
    public void loadAlleles(Iterator<AlleleRow> rowIter, boolean reload) {
        try (Handle handle = dbi.open()) {
            if (reload) {
                handle.createStatement("delete from hla_allele").execute();
            }
            PreparedBatch batch = handle.prepareBatch("insert or ignore into hla_allele(locus, allele) values (?, ?)");
            rowIter.forEachRemaining(g -> batch.add(g.getLocus(), g.getAllele()));
            batch.execute();
        }
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public void loadImmuneGroups(Iterator<GroupRow<Integer>> rowIter, boolean reload) {
        try (Handle handle = dbi.open()) {
            if (reload) {
                handle.createStatement("delete from allele_group").execute();
            }
            PreparedBatch batch = handle.prepareBatch("insert or ignore into allele_group(locus, allele, immune_group) values (?, ?, ?)");
            rowIter.forEachRemaining(g -> batch.add(g.getLocus(), g.getAllele(), g.getGroup()));
            batch.execute();
        }
    }
    
    /** 
     * {@inheritDoc} 
     */
    @Override
    public String getGGroupForAllele(String allele) {
        try (Handle handle = dbi.open()) {
			Matcher m = ALLELE_PATTERN.matcher(allele);
			if (!m.matches()) return null;
			String group = handle.createQuery("select g_group from hla_g_group where locus = :locus and allele = :allele")
                    .bind("locus", m.group("locus"))
                    .bind("allele", m.group("allele"))
                    .map(StringMapper.FIRST)
                    .first();
			return (group == null) ? null : m.group("locus") + "*" + group;
        }
    }

	@Override
	public String getPGroupForAllele(String allele) {
		try (Handle handle = dbi.open()) {
			Matcher m = ALLELE_PATTERN.matcher(allele);
			if (!m.matches()) return null;
			String group = handle.createQuery("select p_group from hla_p_group where locus = :locus and allele = :allele")
					.bind("locus", m.group("locus"))
					.bind("allele", m.group("allele"))
					.map(StringMapper.FIRST)
					.first();
			return (group == null) ? null : m.group("locus") + "*" + group;
		}
	}

	@Override
	public List<String> getGGroupAllelesForAllele(String allele) {
        try (Handle handle = dbi.open()) {
        	List<String> alleleParts = Splitter.on('*').splitToList(allele);
            return handle.createQuery(
            		"select a.locus, a.allele from hla_g_group a"
            		+ " join hla_g_group g on a.g_group = g.g_group and a.locus = g.locus"
            		+ " where g.locus = :locus and g.allele = :allele")
                    .bind("locus", alleleParts.get(0))
                    .bind("allele", alleleParts.get(1))
					.map(LOCUS_ALLELE)
                    .list();
        }
	}

	@Override
	public List<String> getPGroupAllelesForAllele(String allele) {
		try (Handle handle = dbi.open()) {
			List<String> alleleParts = Splitter.on('*').splitToList(allele);
			return handle.createQuery(
					"select a.locus, a.allele from hla_p_group a"
							+ " join hla_p_group p on a.p_group = p.p_group and a.locus = p.locus"
							+ " where p.locus = :locus and p.allele = :allele")
					.bind("locus", alleleParts.get(0))
					.bind("allele", alleleParts.get(1))
					.map(LOCUS_ALLELE)
					.list();
		}
	}

	@Override
	public List<String> getAllelesForLocus(String locus) {
        try (Handle handle = dbi.open()) {
            return handle.createQuery("select locus, allele from hla_allele where locus = :locus")
                    .bind("locus", locus)
                    .map(LOCUS_ALLELE)
                    .list();
        }
	}

	@Override
	public void loadAlleleCodes(Iterator<AlleleCodeRow> rowIter, boolean reload) {
        try (Handle handle = dbi.open()) {
            if (reload) {
                handle.createStatement("delete from allele_code").execute();
            }
            PreparedBatch batch = handle.prepareBatch("insert or ignore into allele_code(allele_code, allele, family_included) values (?, ?, ?)");
            long i = 0;
            while (rowIter.hasNext()) {
            	AlleleCodeRow g = rowIter.next();
            	batch.add(g.getCode(), g.getAllele(), g.isGeneric());
            	if (0 == ++i % 10000L) {
            		logger.debug("committing " + i + " rows...");
            		batch.execute();
            	}
            }
            if (0 != i % 1000L) batch.execute();
        }
	}

	@Override
	public Iterator<AlleleCodeRow> getAlleleCodes() {
        try (Handle handle = dbi.open()) {
            return handle.createQuery("select allele_code, allele, family_included from allele_code")
                    .map((i, r, c) -> new AlleleCodeRow(
                    		r.getString("allele_code"), 
                    		r.getString("allele"), 
                    		r.getBoolean("family_included")))
                    .iterator();
        }
	}
  		
}
