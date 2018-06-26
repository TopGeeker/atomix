/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.core.collection.list.impl;

import io.atomix.core.collection.impl.CachingAsyncDistributedCollection;
import io.atomix.core.collection.list.AsyncDistributedList;
import io.atomix.core.collection.list.DistributedList;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Caching asynchronous distributed list.
 */
public class CachingAsyncDistributedList<E> extends CachingAsyncDistributedCollection<E> implements AsyncDistributedList<E> {
  private final AsyncDistributedList<E> backingList;

  public CachingAsyncDistributedList(AsyncDistributedList<E> backingCollection) {
    super(backingCollection);
    this.backingList = backingCollection;
  }

  public CachingAsyncDistributedList(AsyncDistributedList<E> backingCollection, int cacheSize) {
    super(backingCollection, cacheSize);
    this.backingList = backingCollection;
  }

  @Override
  public CompletableFuture<Boolean> addAll(int index, Collection<? extends E> c) {
    return backingList.addAll(index, c).thenApply(result -> {
      cache.invalidateAll();
      return result;
    });
  }

  @Override
  public CompletableFuture<E> get(int index) {
    return backingList.get(index);
  }

  @Override
  public CompletableFuture<E> set(int index, E element) {
    return backingList.set(index, element).thenApply(result -> {
      cache.invalidate(element);
      return result;
    });
  }

  @Override
  public CompletableFuture<Void> add(int index, E element) {
    return backingList.add(index, element).thenApply(result -> {
      cache.invalidate(element);
      return result;
    });
  }

  @Override
  public CompletableFuture<E> remove(int index) {
    return backingList.remove(index).thenApply(result -> {
      cache.invalidate(result);
      return result;
    });
  }

  @Override
  public CompletableFuture<Integer> indexOf(Object o) {
    return backingList.indexOf(o);
  }

  @Override
  public CompletableFuture<Integer> lastIndexOf(Object o) {
    return backingList.lastIndexOf(o);
  }

  @Override
  public DistributedList<E> sync(Duration operationTimeout) {
    return new BlockingDistributedList<>(this, operationTimeout.toMillis());
  }
}