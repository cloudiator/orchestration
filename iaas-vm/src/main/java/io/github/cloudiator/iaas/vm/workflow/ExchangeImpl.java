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

package io.github.cloudiator.iaas.vm.workflow;

import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Created by daniel on 07.02.17.
 */
public class ExchangeImpl implements Exchange {

  private final static String ILLEGAL_CLASS = "Caller expected class %s but object %s was of type %s.";
  @Nullable
  private final Object data;

  public ExchangeImpl(@Nullable Object data) {
    this.data = data;
  }

  @Override
  public Optional<Object> getData() {
    return Optional.ofNullable(data);
  }

  @Override
  public <T> Optional<T> getData(Class<T> clazz) {

    if (data == null) {
      return Optional.empty();
    }

    try {
      final T cast = clazz.cast(data);
      return Optional.of(cast);
    } catch (ClassCastException e) {
      throw new IllegalStateException(String.format(ILLEGAL_CLASS, clazz, data, data.getClass()),
          e);
    }
  }
}
