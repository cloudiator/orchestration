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

public class NodeCandidateBuilder {

  private String id;
  private NodeCandidateType type;
  private ExtendedCloud cloud;
  private DiscoveredImage image;
  private DiscoveredHardware hardwareFlavor;
  private DiscoveredLocation location;
  private double price;
  private double pricePerInvocation;
  private double memoryPrice;
  private Environment environment;

  private NodeCandidateBuilder() {
  }

  public static NodeCandidateBuilder create() {
    return new NodeCandidateBuilder();
  }

  public NodeCandidateBuilder id(String id) {
    this.id = id;
    return this;
  }

  public NodeCandidateBuilder type(NodeCandidateType type) {
    this.type = type;
    return this;
  }

  public NodeCandidateBuilder cloud(ExtendedCloud cloud) {
    this.cloud = cloud;
    return this;
  }

  public NodeCandidateBuilder image(DiscoveredImage image) {
    this.image = image;
    return this;
  }

  public NodeCandidateBuilder hardware(DiscoveredHardware hardwareFlavor) {
    this.hardwareFlavor = hardwareFlavor;
    return this;
  }

  public NodeCandidateBuilder location(DiscoveredLocation location) {
    this.location = location;
    return this;
  }

  public NodeCandidateBuilder price(double price) {
    this.price = price;
    return this;
  }

  public NodeCandidateBuilder pricePerInvocation(double pricePerInvocation) {
    this.pricePerInvocation = pricePerInvocation;
    return this;
  }

  public NodeCandidateBuilder memoryPrice(double memoryPrice) {
    this.memoryPrice = memoryPrice;
    return this;
  }

  public NodeCandidateBuilder environment(Environment environment) {
    this.environment = environment;
    return this;
  }

  public NodeCandidate build() {
    return new NodeCandidateImpl(id, type, cloud, image, hardwareFlavor, location, price,
        pricePerInvocation, memoryPrice, environment);
  }
}
