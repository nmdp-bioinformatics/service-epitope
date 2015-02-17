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

package org.nmdp.epitope.db.common;

import java.sql.Connection;
import java.util.Iterator;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.PreparedBatch;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

public class DbUtil {

	public static void loadCsv(Connection conn, String sql, String csvFile) throws Exception {
		try (Handle handle = DBI.open(conn)) {
			PreparedBatch batch = handle.prepareBatch(sql);
			Iterator<String[]> it = readCsv(csvFile);
			while (it.hasNext()) {
				batch.add(it.next());
			}
			batch.execute();
		}
	}
	
	public static Iterator<String[]> readCsv(String url) throws Exception {
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		return mapper.reader(String[].class).readValues(url);
	}
	
}
