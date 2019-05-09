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

package io.github.cloudiator.domain;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import java.util.Objects;

public class NodeCandidateImpl implements NodeCandidate {

  private final String id;
  private final NodeCandidateType type;
  private final ExtendedCloud cloud;
  private final DiscoveredImage image;
  private final DiscoveredHardware hardwareFlavor;
  private final DiscoveredLocation location;
  private final double price;
  private final double pricePerInvocation;
  private final double memoryPrice;
  private final Environment environment;

  NodeCandidateImpl(String id, NodeCandidateType type, ExtendedCloud cloud, DiscoveredImage image,
      DiscoveredHardware hardwareFlavor, DiscoveredLocation location, double price,
      double pricePerInvocation, double memoryPrice, Environment environment) {
    this.id = id;
    this.type = type;
    this.cloud = cloud;
    this.image = image;
    this.hardwareFlavor = hardwareFlavor;
    this.location = location;
    this.price = price;
    this.pricePerInvocation = pricePerInvocation;
    this.memoryPrice = memoryPrice;
    this.environment = environment;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public NodeCandidateType type() {
    return type;
  }

  @Override
  public ExtendedCloud cloud() {
    return cloud;
  }

  @Override
  public DiscoveredImage image() {
    return image;
  }

  @Override
  public DiscoveredHardware hardware() {
    return hardwareFlavor;
  }

  @Override
  public DiscoveredLocation location() {
    return location;
  }

  @Override
  public double price() {
    return price;
  }

  @Override
  public double pricePerInvocation() {
    return pricePerInvocation;
  }

  @Override
  public double memoryPrice() {
    return memoryPrice;
  }

  @Override
  public Environment environment() {
    return environment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeCandidateImpl that = (NodeCandidateImpl) o;
    return Double.compare(that.price, price) == 0 &&
        Objects.equals(cloud, that.cloud) &&
        Objects.equals(image, that.image) &&
        Objects.equals(hardwareFlavor, that.hardwareFlavor) &&
        Objects.equals(location, that.location) &&
        Objects.equals(type, that.type) &&
        Objects.equals(pricePerInvocation, that.pricePerInvocation) &&
        Objects.equals(memoryPrice, that.memoryPrice) &&
        Objects.equals(environment, that.environment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, cloud, image, hardwareFlavor, location,
        price, pricePerInvocation, memoryPrice, environment);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", type)
        .add("cloud", cloud)
        .add("image", image)
        .add("hardware", hardwareFlavor)
        .add("location", location)
        .add("price", price)
        .add("pricePerInvocation", pricePerInvocation)
        .add("memoryPrice", memoryPrice)
        .add("environment", environment)
        .toString();
  }
}
