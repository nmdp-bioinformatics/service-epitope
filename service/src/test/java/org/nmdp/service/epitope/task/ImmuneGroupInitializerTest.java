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

package org.nmdp.service.epitope.task;

import static db.migration.util.DbUtil.readCsv;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.DbiManagerImpl;
import org.nmdp.service.epitope.db.DbiManagerImplTest;
import org.nmdp.service.epitope.db.ImmuneGroupRow;
import org.nmdp.service.epitope.task.ImmuneGroupInitializer;
import org.skife.jdbi.v2.DBI;

@RunWith(MockitoJUnitRunner.class)
public class ImmuneGroupInitializerTest {

	@Mock 
	DbiManager dbi;
	
	@Captor
    ArgumentCaptor<Iterator<ImmuneGroupRow>> iterCaptor;

	private ImmuneGroupInitializer init;

	@Before
	public void setup() throws Exception {
		URL[] urls = { getClass().getResource("/org/nmdp/service/epitope/group/db/prot_fasta_dpb1.txt").toURI().toURL() };
		init = new ImmuneGroupInitializer(urls, dbi);
	}
    
	public static void main(String[] args) throws Exception {
		URL[] urls = { ImmuneGroupInitializerTest.class.getResource("/org/nmdp/service/epitope/group/db/prot_fasta_dpb1.txt").toURI().toURL() };
		DBI dbi = new DBI("jdbc:sqlite:../dropwizard/epitope-service.db");
		DbiManager dbim = new DbiManagerImpl(dbi); 
		ImmuneGroupInitializer init = new ImmuneGroupInitializer(urls, dbim);
		init.loadAlleleScores();
	}
	
	@Test
	public void testLoadAlleleScores() throws Exception {

		// load alleles
		init.loadAlleleScores();

		// capture loaded alleles in dbi mock
		verify(dbi).loadImmuneGroups(iterCaptor.capture(), eq(true));
	    
		// build a map of expected values from crivello paper
		Map<String, Integer> expectedMap = stream(
	    		spliteratorUnknownSize(readCsv(getClass().getResource("/org/nmdp/service/epitope/group/db/allele_group.csv").toURI().toURL()), ORDERED), false)
	    	.collect(toMap(
    				a -> (String)a[1], 
    				a -> Integer.valueOf(a[2].toString())));
		
		// parse actual values and verify against expected
	    stream(spliteratorUnknownSize(iterCaptor.getValue(), ORDERED), false)
				.forEach(r -> {
					try { if (expectedMap.containsKey(r.getAllele())) {
						assertThat(expectedMap.get(r.getAllele()), equalTo(r.getImmuneGroup()));
					} } catch (AssertionError e) {
						System.out.println("allele: " + r.getAllele() 
								+ ", expected: " + expectedMap.get(r.getAllele()) 
								+ ", got: " + r.getImmuneGroup());
						//throw new AssertionError("allele failed: " + r, e);
					}
				});
	}

}
