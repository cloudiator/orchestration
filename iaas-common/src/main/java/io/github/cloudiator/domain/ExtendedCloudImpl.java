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
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import java.util.Optional;
import javax.annotation.Nullable;

public class ExtendedCloudImpl implements ExtendedCloud {

  private final Cloud delegate;
  private final String userId;
  private final CloudState cloudState;
  @Nullable
  private final String diagnostic;

  ExtendedCloudImpl(Cloud delegate, String userId, CloudState cloudState,
      @Nullable String diagnostic) {
    this.delegate = delegate;
    this.userId = userId;
    this.cloudState = cloudState;
    this.diagnostic = diagnostic;
  }

  @Override
  public CloudState state() {
    return cloudState;
  }

  @Override
  public Optional<String> diagnostic() {
    return Optional.ofNullable(diagnostic);
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public String id() {
    return delegate.id();
  }

  @Override
  public Api api() {
    return delegate.api();
  }

  @Override
  public Optional<String> endpoint() {
    return delegate.endpoint();
  }

  @Override
  public CloudCredential credential() {
    return delegate.credential();
  }

  @Override
  public Configuration configuration() {
    return delegate.configuration();
  }

  @Nullable
  @Override
  public CloudType cloudType() {
    return delegate.cloudType();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("cloud", delegate).add("userId", userId)
        .add("state", cloudState).add("diagnostic", diagnostic).toString();
  }
}
