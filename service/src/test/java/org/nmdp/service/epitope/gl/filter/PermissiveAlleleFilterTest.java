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

package org.nmdp.service.epitope.gl.filter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.nmdp.service.epitope.gl.filter.PermissiveAlleleFilter;

@RunWith(MockitoJUnitRunner.class)
public class PermissiveAlleleFilterTest {

	@Test
	public void testApply() throws Exception {
		String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
		PermissiveAlleleFilter filter = new PermissiveAlleleFilter();
		String test = filter.apply(gl);
		assertThat(test, equalTo("HLA-B*01:01:01+HLA-A*01:ABCD/HLA-DPB1*02:01:01:02"));
	}

	@Test
	public void testApply_DefaultLocus() throws Exception {
		String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
		PermissiveAlleleFilter filter = new PermissiveAlleleFilter("FOO");
		String test = filter.apply(gl);
		assertThat(test, equalTo("HLA-B*01:01:01+HLA-A*01:ABCD/HLA-FOO*02:01:01:02"));
	}

	@Test(expected=RuntimeException.class)
	public void testApply_NoLocus() throws Exception {
		String gl = "B*01:01:01+HLA-A*01:ABCD/02:01:01:02";
		PermissiveAlleleFilter filter = new PermissiveAlleleFilter(null);
		filter.apply(gl);
	}
	
}
