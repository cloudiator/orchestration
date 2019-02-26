/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.util;

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
