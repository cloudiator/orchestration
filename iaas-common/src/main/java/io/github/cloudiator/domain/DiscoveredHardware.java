/*
 * Copyright (c) 2014-2019 University of Ulm
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

package io.github.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import java.util.Optional;

public class DiscoveredHardware implements HardwareFlavor, DiscoveryItem {

  private final HardwareFlavor delegate;
  private DiscoveryItemState state;
  private final String userId;

  public DiscoveredHardware(HardwareFlavor delegate,
      DiscoveryItemState state, String userId) {

    checkNotNull(delegate, "delegate is null");
    checkNotNull(state, "state is null");
    checkNotNull(userId, "userId is null");

    this.delegate = delegate;
    this.state = state;
    this.userId = userId;
  }

  @Override
  public int numberOfCores() {
    return delegate.numberOfCores();
  }

  @Override
  public long mbRam() {
    return delegate.mbRam();
  }

  @Override
  public Optional<Double> gbDisk() {
    return delegate.gbDisk();
  }

  @Override
  public String providerId() {
    return delegate.providerId();
  }

  @Override
  public String id() {
    return delegate.id();
  }

  @Override
  @JsonProperty
  public String name() {
    return delegate.name();
  }

  @Override
  public Optional<Location> location() {

    if (delegate.location().isPresent()) {
      return Optional
          .of(new DiscoveredLocation(delegate.location().get(), DiscoveryItemState.UNKNOWN,
              userId));
    }

    return Optional.empty();
  }

  @Override
  public Optional<String> locationId() {
    return delegate.locationId();
  }

  @Override
  public DiscoveryItemState state() {
    return state;
  }

  @Override
  public void setState(DiscoveryItemState state) {
    this.state = state;
  }

  @Override
  public String userId() {
    return this.userId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("delegate", delegate).add("state", state)
        .add("userId", userId).toString();
  }
}
