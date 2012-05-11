/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.happydroids.droidtowers.utils.Random;

import java.util.HashSet;
import java.util.List;

public class GuavaSet<T> extends HashSet<T> {
  public GuavaSet() {

  }

  public GuavaSet(int initialCapacity) {
    super(initialCapacity);
  }

  public GuavaSet(Iterable<T> iterable) {
    this();

    for (T t : iterable) {
      add(t);
    }
  }

  public GuavaSet<T> filterBy(Predicate<T> predicate) {
    return new GuavaSet<T>(Iterables.filter(this, predicate));
  }

  public T randomEntry() {
    if (!isEmpty()) {
      return Iterables.get(this, Random.randomInt(size()), null);
    }

    return null;
  }

  public T last() {
    return Iterables.getLast(this);
  }

  public T first() {
    return Iterables.getFirst(this, null);
  }

  @SuppressWarnings("unchecked")
  public List<T> sortedBy(Function function) {
    return Ordering.natural().onResultOf(function).immutableSortedCopy(new GuavaSet(this));
  }
}
