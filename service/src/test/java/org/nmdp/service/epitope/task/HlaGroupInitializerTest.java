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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nmdp.service.epitope.EpitopeServiceTestData.getMockUrl;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.db.DbiManager;
import org.nmdp.service.epitope.db.GroupRow;

@RunWith(MockitoJUnitRunner.class)
public class HlaGroupInitializerTest {

    @Mock
    private DbiManager dbiManager;
    
    @InjectMocks
    private HlaGroupInitializer hlaGroupInitializer;

    @Captor
    ArgumentCaptor<Iterator<GroupRow<String>>> ggCaptor;

    @Captor
    ArgumentCaptor<Iterator<GroupRow<String>>> pgCaptor;

    @Before
    public void setup() throws Exception {
        URL url = getMockUrl(getClass().getResourceAsStream("hla.xml"), true);
        hlaGroupInitializer = new HlaGroupInitializer(new URL[] { url }, dbiManager);
    }
    
    @Test
    public void testLoadGGroups() throws Exception {
        when(dbiManager.getDatasetDate("hla_g_group")).thenReturn(42L);
        hlaGroupInitializer.loadGroups();
        verify(dbiManager).updateDatasetDate(eq("hla_g_group"), anyLong());
        verify(dbiManager).updateDatasetDate(eq("hla_p_group"), anyLong());
        verify(dbiManager).loadGGroups(ggCaptor.capture(), eq(true));
        verify(dbiManager).loadPGroups(pgCaptor.capture(), eq(true));
        assertThat(ggCaptor.getValue(), not(nullValue()));
        List<GroupRow<String>> rowList = iterToList(ggCaptor.getValue());
        assertThat(rowList.size(), equalTo(16));
        assertThat(rowList.get(0).getAllele(), equalTo("01:01:01:01"));
        assertThat(rowList.get(0).getGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(1).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(1).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(2).getAllele(), equalTo("01:01:01:02"));
        assertThat(rowList.get(2).getGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(3).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(3).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(4).getAllele(), equalTo("01:01:01:03"));
        assertThat(rowList.get(4).getGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(5).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(5).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(6).getAllele(), equalTo("01:01:01:04"));
        assertThat(rowList.get(6).getGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(7).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(7).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(8).getAllele(), equalTo("01:01:02:01"));
        assertThat(rowList.get(8).getGroup(), equalTo("01:01:02G"));
        assertThat(rowList.get(9).getAllele(), equalTo("01:01:02"));
        assertThat(rowList.get(9).getGroup(), equalTo("01:01:02G"));

        assertThat(rowList.get(10).getAllele(), equalTo("01:01:02:02"));
        assertThat(rowList.get(10).getGroup(), equalTo("01:01:02G"));
        assertThat(rowList.get(11).getAllele(), equalTo("01:01:02"));
        assertThat(rowList.get(11).getGroup(), equalTo("01:01:02G"));

        assertThat(rowList.get(12).getAllele(), equalTo("162:01"));
        assertThat(rowList.get(12).getGroup(), equalTo("01:01:02G"));

        assertThat(rowList.get(13).getAllele(), equalTo("417:01"));
        assertThat(rowList.get(13).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(14).getAllele(), equalTo("462:01"));
        assertThat(rowList.get(14).getGroup(), equalTo("01:01:01G"));

        assertThat(rowList.get(15).getAllele(), equalTo("616:01"));
        assertThat(rowList.get(15).getGroup(), equalTo("01:01:01G"));

        assertThat(pgCaptor.getValue(), not(nullValue()));
        rowList = iterToList(pgCaptor.getValue());
        assertThat(rowList.size(), equalTo(30));

        assertThat(rowList.get(0).getAllele(), equalTo("01:01:01:01"));
        assertThat(rowList.get(0).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(1).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(1).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(2).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(2).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(3).getAllele(), equalTo("01:01:01:02"));
        assertThat(rowList.get(3).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(4).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(4).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(5).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(5).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(6).getAllele(), equalTo("01:01:01:03"));
        assertThat(rowList.get(6).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(7).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(7).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(8).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(8).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(9).getAllele(), equalTo("01:01:01:04"));
        assertThat(rowList.get(9).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(10).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(10).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(11).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(11).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(12).getAllele(), equalTo("01:01:02:01"));
        assertThat(rowList.get(12).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(13).getAllele(), equalTo("01:01:02"));
        assertThat(rowList.get(13).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(14).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(14).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(15).getAllele(), equalTo("01:01:02:02"));
        assertThat(rowList.get(15).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(16).getAllele(), equalTo("01:01:02"));
        assertThat(rowList.get(16).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(17).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(17).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(18).getAllele(), equalTo("01:01:03"));
        assertThat(rowList.get(18).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(19).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(19).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(20).getAllele(), equalTo("01:01:04"));
        assertThat(rowList.get(20).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(21).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(21).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(22).getAllele(), equalTo("01:01:05"));
        assertThat(rowList.get(22).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(23).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(23).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(24).getAllele(), equalTo("01:01:06"));
        assertThat(rowList.get(24).getGroup(), equalTo("01:01P"));
        assertThat(rowList.get(25).getAllele(), equalTo("01:01"));
        assertThat(rowList.get(25).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(26).getAllele(), equalTo("162:01"));
        assertThat(rowList.get(26).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(27).getAllele(), equalTo("417:01"));
        assertThat(rowList.get(27).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(28).getAllele(), equalTo("462:01"));
        assertThat(rowList.get(28).getGroup(), equalTo("01:01P"));

        assertThat(rowList.get(29).getAllele(), equalTo("616:01"));
        assertThat(rowList.get(29).getGroup(), equalTo("01:01P"));
    }

    public <T> List<T> iterToList(Iterator<T> iter) {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false)
                .collect(Collectors.toList());
    }

}
