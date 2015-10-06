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

package org.nmdp.service.epitope.guice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;

/**
 * A generic wrapping resolver that takes values from a delegate and caches them, refreshing them asynchronously on the specified period.  
 * Supports listeners that are notified on updates to cached values.
 * @param <K> the key type to be cached
 * @param <V> the value type to be cached
 */
public class CachingFunction<K, V> implements Function<K, V> {

	private LoadingCache<K, Optional<V>> cache;
	
	private List<CachingFunctionListener<K, V>> listenerList = new ArrayList<>();
	
	/**
	 * construct a CachingResolver with the specified resolver and cache parameters
	 * @param resolver the resolver to delegate to to populate the cache
	 * @param duration duration of time to cache for before expiring
	 * @param period period of time between cache refreshes
	 * @param cacheCapacity max capacity of the cache
	 */
	@Inject
	public CachingFunction(final Function<K, V> delegate, final @CacheDuration long duration, final @CachePeriod long period, final @CacheCapacity long cacheCapacity) {
		final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
		cache = CacheBuilder.newBuilder()
				.refreshAfterWrite(period, TimeUnit.MILLISECONDS)
				.expireAfterAccess(duration, TimeUnit.MILLISECONDS)
				.initialCapacity(3)
				.maximumSize(cacheCapacity)
				.build(new CacheLoader<K, Optional<V>>() {
					public Optional<V> load(K key) {
						return Optional.fromNullable(delegate.apply(key));
					}
					@Override
					public ListenableFuture<Optional<V>> reload(final K key, final Optional<V> oldValue) throws Exception {
						final ListenableFuture<Optional<V>> future = executor.submit(() -> load(key));
						notifyListeners(future, key, oldValue);
						return future;
					}
				});
	}

	/**
	 * add a listener to the cache, to be notified of refreshes to cache content
	 * @param listener
	 */
	public void addListener(CachingFunctionListener<K, V> listener) {
		listenerList.add(listener);
	}

	/**
	 * remove a listener from the cache
	 * @param listener
	 */
	public void removeListener(CachingFunctionListener<K, V> listener) {
		listenerList.remove(listener);
	}

	/**
	 * notify listeners about a cache refresh
	 * @param future the future of the async cache refresh
	 * @param key the key of the refresh
	 * @param oldValue the old value
	 */
	private void notifyListeners(final ListenableFuture<Optional<V>> future, final K key, final Optional<V> oldValue) {
		for (final CachingFunctionListener<K, V> listener : listenerList) {
			future.addListener(() -> {
			    try {
			        listener.reloaded(key,  oldValue.orNull(), future.get().orNull());
			    } catch (Exception e) {
			        throw new RuntimeException("caught exception while notifying listener (key: " 
			                + key + ", oldValue: " + oldValue + ")");
			    }
			}, MoreExecutors.directExecutor());
		}
	}
	
	/** 
	 * return value from cache
	 */
	@Override
	public V apply(K k) {
		try {
			return cache.get(k).orNull();
		} catch (ExecutionException e) {
			throw new RuntimeException("failed to resolve: " +  k, e.getCause());
		}
	}
	
}
