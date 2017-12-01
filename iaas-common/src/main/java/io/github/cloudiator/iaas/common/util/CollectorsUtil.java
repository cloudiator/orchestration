package io.github.cloudiator.iaas.common.util;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorsUtil {

  private CollectorsUtil() {
    throw new AssertionError("Do not instantiate.");
  }

  /**
   * Collector for getting exactly one result from a list.
   *
   * Replacement for MoreCollectors.onlyElement() {@see https://google.github.io/guava/releases/21.0/api/docs/com/google/common/collect/MoreCollectors.html}
   * that can not be used due to jclouds not supporting Guava 21.
   *
   * @param <T> type of the element
   * @return the single element
   * @throws IllegalStateException if there is no or more than one elements in the list.
   */
  public static <T> Collector<T, ?, T> singletonCollector() {
    return Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
          if (list.size() != 1) {
            throw new IllegalStateException(
                String.format("SingletonCollector expected exactly one element but got %s.",
                    list.size()));
          }
          return list.get(0);
        }
    );
  }

}
