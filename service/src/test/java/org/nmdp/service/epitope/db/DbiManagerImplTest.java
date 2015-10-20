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

import org.junit.BeforeClass;
import org.skife.jdbi.v2.DBI;


public class DbiManagerImplTest {

	protected static DBI dbi;
	protected static DbiManagerImpl dbiManager;

	@BeforeClass
	public static void setupClass() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("failed to load driver", e);
		}
		dbi = new DBI("jdbc:sqlite:../dropwizard/epitope-service.db");
		dbiManager = new DbiManagerImpl(dbi); 
	}

//	@Test
//	public void testGetGGroupAllelesForAllele() throws Exception {
//		dbiManager.getGGroupAllelesForAllele("HLA-DPB1*01:01:01");
//	}

//	@Test
//	public void testGetGroupForAllele() throws Exception {
//		Integer group = db.getGroupForAllele("01:01");
//		assertThat(group, notNullValue());
//		assertThat(group, equalTo(3));
//	}
//
//	@Test
//	public void testGetAllelesForGroup() throws Exception {
//		List<String> alleleList = db.getAllelesForGroup(3);
//		assertThat(alleleList, notNullValue());
//		assertThat(alleleList, not(empty()));
//	}

}
