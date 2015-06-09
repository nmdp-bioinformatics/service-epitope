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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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
import org.nmdp.service.epitope.db.GGroupRow;
import org.nmdp.service.epitope.task.GGroupInitializer;

@RunWith(MockitoJUnitRunner.class)
public class GGroupInitializerTest {

    @Mock
    private DbiManager dbiManager;
    
    @InjectMocks
    private GGroupInitializer gGroupInitializer;

    @Captor
    ArgumentCaptor<Iterator<GGroupRow>> iterCaptor;

    @Before
    public void setup() throws Exception {
        URL url = getMockUrl(getClass().getResourceAsStream("hla-ambig.xml"), true);
        gGroupInitializer = new GGroupInitializer(new URL[] { url }, dbiManager);
    }
    
    @Test
    public void testLoadGGroups() throws Exception {
        when(dbiManager.getDatasetDate("hla_g_group")).thenReturn(42L);
        gGroupInitializer.loadGGroups();
        verify(dbiManager).updateDatasetDate(eq("hla_g_group"), anyLong());
        verify(dbiManager).loadGGroups(iterCaptor.capture(), eq(true));
        Iterator<GGroupRow> iter = iterCaptor.getValue();
        assertThat(iter, not(nullValue()));
        List<GGroupRow> rowList = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false)
                .collect(Collectors.toList());
        assertThat(rowList.size(), equalTo(4));
        assertThat(rowList.get(0).getGGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(0).getAllele(), equalTo("01:01:01"));
        assertThat(rowList.get(1).getGGroup(), equalTo("01:01:01G"));
        assertThat(rowList.get(1).getAllele(), equalTo("417:01"));
        assertThat(rowList.get(2).getGGroup(), equalTo("01:01:02G"));
        assertThat(rowList.get(2).getAllele(), equalTo("01:01:02"));
        assertThat(rowList.get(3).getGGroup(), equalTo("01:01:02G"));
        assertThat(rowList.get(3).getAllele(), equalTo("162:01"));
    }

}
