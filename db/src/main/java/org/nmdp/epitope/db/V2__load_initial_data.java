/*

    epitope-service  T-cell epitope group matching service for DPB1 locus.
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

package org.nmdp.epitope.db;

import static org.nmdp.epitope.db.common.DbUtil.loadCsv;

import java.sql.Connection;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;

public class V2__load_initial_data implements JdbcMigration {

	@Override
	public void migrate(Connection conn) throws Exception {
		loadCsv(conn, "insert into allele_group (locus, allele, immune_group) values (?, ?, ?);", "../allele_group.csv");
		loadCsv(conn, "insert into detail_race (detail_race, broad_race, description) values (?, ?, ?);", "../allele_group.csv");
		loadCsv(conn, "insert into race_freq (locus, detail_race, allele, frequency) values (?, ?, ?, ?);", "../allele_group.csv");
	}

}
