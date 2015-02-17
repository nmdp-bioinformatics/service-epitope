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

package org.nmdp.service.epitope.guice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Function;

import edu.umd.cs.mtc.MultithreadedTestCase;

public class CachingResolverListenerTest extends MultithreadedTestCase {

	int resolverClock;
	int cacheClock;
	ReentrantLock clockLock;

	@Override 
	public void initialize() {
		resolverClock = 0;
		cacheClock = 0; 
		clockLock = new ReentrantLock(true);
	}

//	public int getAndIncrementResolverClock() {
//		clockLock.lock();
//		try {
////			if (cacheClock != resolverClock - 1) {
////				fail("expected cache clock behind resolver clock (cc: " + cacheClock + ", rc: " + resolverClock + ")");
////			}
//			int clock = resolverClock++;
//			cacheClock = clock;
//			return clock;
//		} finally {
//			clockLock.unlock();
//		}
//	}
//
//	public int setResolverClock(int newValue) {
//		clockLock.lock();
//		try {
////			if (cacheClock != resolverClock - 1) {
////				fail("expected cache clock behind resolver clock (cc: " + cacheClock + ", rc: " + resolverClock + ")");
////			}
//			int clock = resolverClock++;
//			cacheClock = clock;
//			return clock;
//		} finally {
//			clockLock.unlock();
//		}
//	}
//	
//	public int getCacheClock() {
//		clockLock.lock();
//		try {
//			return cacheClock;
//		} finally {
//			clockLock.unlock();
//		}
//	}
	
//	public void thread1() throws Exception {
//		Function<String, String> resolver = new Function<String, String>() {
//			@Override
//			public String apply(String s) {
//				assertTick(getAndIncrementResolverClock());
//				return s;
//			}
//		};
//		CachingResolver<String, String> cachingResolver = new CachingResolver<>(resolver, 1, Long.MAX_VALUE);
//		String test = cachingResolver.apply("test");
//		assertThat(test, equalTo("test"));
//		assertTick(getCacheClock());
//		
//		try { Thread.sleep(500); } catch (InterruptedException e) {}
//		verify(resolver, times(2)).apply("key");
//	}
//

}
