package com.unhappyrobot.entities;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.unhappyrobot.utils.Random;

import java.util.HashSet;
import java.util.List;

public class GuavaSet<T> extends HashSet<T> {
  public GuavaSet() {

  }

  public GuavaSet(int initialCapacity) {
    super(initialCapacity);
  }

  public void filterBy(Predicate<T> predicate) {
    Iterables.filter(this, predicate);
  }

  public T getRandomEntry() {
    if (!isEmpty()) {
      return Iterables.get(this, Random.randomInt(size() - 1));
    }

    return null;
  }

  public T last() {
    return Iterables.getLast(this);
  }

  public T first() {
    return Iterables.getFirst(this, null);
  }

  public List<T> sortedBy(Function function) {
    return Ordering.natural().onResultOf(function).sortedCopy(this);
  }
}
