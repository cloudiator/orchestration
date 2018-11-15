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
import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.CloudBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;

public class ExtendedCloudBuilder {

  private final CloudBuilder delegateBuilder = CloudBuilder.newBuilder();
  private CloudState cloudState;
  private String diagnostic;
  private String userId;

  private ExtendedCloudBuilder() {
  }

  private ExtendedCloudBuilder(ExtendedCloud cloud) {

    delegateBuilder.api(cloud.api()).endpoint(cloud.endpoint().orElse(null))
        .credentials(cloud.credential()).cloudType(cloud.cloudType())
        .configuration(cloud.configuration());
    this.cloudState = cloud.state();
    this.diagnostic = cloud.diagnostic().orElse(null);
    this.userId = cloud.userId();
  }

  public static ExtendedCloudBuilder newBuilder() {
    return new ExtendedCloudBuilder();
  }

  public static ExtendedCloudBuilder of(ExtendedCloud cloud) {
    return new ExtendedCloudBuilder(cloud);
  }

  public ExtendedCloudBuilder api(Api api) {
    delegateBuilder.api(api);
    return this;
  }

  public ExtendedCloudBuilder endpoint(String endpoint) {
    delegateBuilder.endpoint(endpoint);
    return this;
  }

  public ExtendedCloudBuilder credentials(CloudCredential cloudCredential) {
    delegateBuilder.credentials(cloudCredential);
    return this;
  }

  public ExtendedCloudBuilder configuration(Configuration configuration) {
    delegateBuilder.configuration(configuration);
    return this;
  }

  public ExtendedCloudBuilder cloudType(CloudType cloudType) {
    delegateBuilder.cloudType(cloudType);
    return this;
  }

  public ExtendedCloudBuilder state(CloudState state) {
    this.cloudState = state;
    return this;
  }

  public ExtendedCloudBuilder diagnostic(String diagnostic) {
    this.diagnostic = diagnostic;
    return this;
  }

  public ExtendedCloudBuilder userId(String userId) {
    this.userId = userId;
    return this;
  }


  public ExtendedCloudImpl build() {
    return new ExtendedCloudImpl(delegateBuilder.build(), userId, cloudState, diagnostic);
  }

  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

}
