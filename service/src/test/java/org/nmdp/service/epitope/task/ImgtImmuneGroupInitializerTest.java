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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.DbiManagerImpl;
import org.nmdp.service.epitope.db.ImmuneGroupRow;
import org.skife.jdbi.v2.DBI;

@RunWith(MockitoJUnitRunner.class)
public class ImgtImmuneGroupInitializerTest {

	@Mock 
	DbiManager dbi;

	@Captor
	ArgumentCaptor<Iterator<ImmuneGroupRow>> iterCaptor;

	private ImgtImmuneGroupInitializer init;

	@Before
	public void setup() throws Exception {
		URL[] urls = URLProcessor.getUrls("/org/nmdp/service/epitope/group/db/prot_fasta_dpb1.txt");
		init = new ImgtImmuneGroupInitializer(urls, dbi);
	}

	public static void main(String[] args) throws Exception {
		URL[] urls = URLProcessor.getUrls("/org/nmdp/service/epitope/group/db/prot_fasta_dpb1.txt");
		DBI dbi = new DBI("jdbc:sqlite:../dropwizard/epitope-service.db");
		DbiManager dbim = new DbiManagerImpl(dbi); 
		ImgtImmuneGroupInitializer init = new ImgtImmuneGroupInitializer(urls, dbim);
		init.loadImmuneGroups();
	}

	@Test
	@Ignore
	public void testLoadImmuneGroups() throws Exception {

		// load alleles
		init.loadImmuneGroups();

		// capture loaded alleles in dbi mock
		verify(dbi).loadImmuneGroups(iterCaptor.capture(), eq(true));

		// test map
		Map<String, Integer> map = stream(spliteratorUnknownSize(iterCaptor.getValue(), ORDERED), false)
				.collect(Collectors.toMap(r -> r.getAllele(), r -> r.getImmuneGroup()));
		
		// build a map of expected values from crivello paper
		Map<String, Integer> expectedMap = stream(
				spliteratorUnknownSize(readCsv(getClass().getResource("/org/nmdp/service/epitope/group/db/allele_group.csv").toURI().toURL()), ORDERED), false)
				.collect(toMap(
						a -> (String)a[1], 
						a -> Integer.valueOf(a[2].toString())));

		//map.put("26:01", 3); // broke, got 1
		//map.put("37:01", 2); // broke, got 1
		
		// assert (expected and actual values are inverted in error messages)
		assertThat(expectedMap.entrySet(), everyItem(isIn(map.entrySet())));

	}
}
