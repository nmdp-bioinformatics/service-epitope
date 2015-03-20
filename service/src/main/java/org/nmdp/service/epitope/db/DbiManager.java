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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nmdp.service.epitope.domain.DetailRace;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Folder2;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.logging.SLF4JLog;
import org.skife.jdbi.v2.logging.SLF4JLog.Level;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.util.DoubleMapper;
import org.skife.jdbi.v2.util.IntegerMapper;
import org.skife.jdbi.v2.util.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DbiManager {

	private final DBI dbi;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public DbiManager(DBI dbi) {
		this.dbi = dbi;
		dbi.setSQLLog(new SLF4JLog(logger, Level.TRACE));
	}
	
	ResultSetMapper<String> LOCUS_ALLELE = new ResultSetMapper<String>() {
		@Override public String map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			return r.getString("locus") + "*" + r.getString("allele");
		}};
	
	
	public Map<String, Integer> getAlleleGroupMap() {
		try (Handle handle = dbi.open()) {
			Map<String, Integer> alleleGroupMap = new HashMap<>();
			Query<Map<String, Object>> query = handle.createQuery(
					"select locus, allele, immune_group from allele_group");
			for (Map<String, Object> row : query) {
				alleleGroupMap.put((String)row.get("locus") + "*" + row.get("allele"), (Integer)row.get("immune_group"));
			}
			return alleleGroupMap;
		}
	}		
	
	public Integer getGroupForAllele(String allele) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select immune_group from allele_group where allele = :allele")
					.bind("allele", allele)
					.map(IntegerMapper.FIRST)
					.first();
 		}
	}
	
	public List<String> getAllelesForGroup(Integer group) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select locus, allele from allele_group where immune_group = :group")
					.bind("group", group)
					.map(LOCUS_ALLELE)
					.list();
 		}
	}
	
	public Map<Integer, List<String>> getGroupAlleleMap() {
		// todo simpler with ordered list
		try (Handle handle = dbi.open()) {
			Map<Integer, List<String>> groupAlleleMap = new HashMap<>();
			Query<Map<String, Object>> query = handle.createQuery(
					"select immune_group, locus, allele from allele_group order by immune_group, allele");
			query.fold(groupAlleleMap, new Folder2<Map<Integer, List<String>>>() {
				@Override
				public Map<Integer, List<String>> fold(
						Map<Integer, List<String>> groupAlleleMap,
						ResultSet rs,
						StatementContext ctx) throws SQLException 
				{
					Integer group = rs.getInt("immune_group");
					List<String> alleleList = groupAlleleMap.get(group);
					if (null == alleleList) {
						alleleList = new ArrayList<>();
						groupAlleleMap.put(group, alleleList);
					}
					alleleList.add(rs.getString("locus") + "*" + rs.getString("allele"));
					return groupAlleleMap;
				}
			});
			return groupAlleleMap;
		}
	}
	
	public String getBroadRaceForDetailRace(String detailRace) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select broad_race from detail_race where detail_race = :detail_race")
					.bind("detail_race", detailRace)
					.map(StringMapper.FIRST)
					.first();
 		}
	}
	
	public Map<String, Double> getAlleleFrequenciesForDetailRace(String detailRace) {
		Map<String, Double> alleleFrequencies = getAlleleFrequenciesForRace(detailRace);
		if (alleleFrequencies.isEmpty()) {
			String broadRace = getBroadRaceForDetailRace(detailRace);
			alleleFrequencies = getAlleleFrequenciesForRace(broadRace);
		}
		return alleleFrequencies;
	}
	
	private Map<String, Double> getAlleleFrequenciesForRace(String race) {
		try (Handle handle = dbi.open()) {
			Map<String, Double> alleleFreqMap = new HashMap<>();
			Query<Map<String, Object>> query = handle.createQuery(
					"select locus, allele, frequency from race_freq where detail_race = :detail_race");
			for (Map<String, Object> row : query) {
				alleleFreqMap.put((String)row.get("locus") + "*" + (String)row.get("allele"), (Double)row.get("frequency"));
			}
			return alleleFreqMap;
		}
	}
	
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
	
	public Double getFrequency(String allele, DetailRace race) {
		try (Handle handle = dbi.open()) {
			return handle.createQuery(
					"select ifnull(drf.frequency, brf.frequency)"
					+ " from detail_race dr"
					+ " join race_freq brf on dr.broad_race = brf.detail_race"
					+ " left join race_freq drf on dr.detail_race = drf.detail_race"
					+ " where dr.detail_race = :race"
					+ " and brf.allele = :allele"
					+ " and (drf.detail_race is null or drf.detail_race = brf.detail_race)"
					+ " and (drf.allele is null or drf.allele = :allele);")
					.bind("allele", allele)
					.bind("race", race)
					.map(DoubleMapper.FIRST)
					.first();
		}
	}
	
}
