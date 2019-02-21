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

package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.domain.LoginNameSupplier;
import io.github.cloudiator.domain.DiscoveryItemState;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
class ImageModel extends ResourceModel implements LoginNameSupplier {

  @Nullable
  private String loginUsernameOverride;
  @Nullable
  private String loginPasswordOverride;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DiscoveryItemState state;

  /**
   * Owned relations
   */
  @OneToOne(optional = false, orphanRemoval = true)
  @Cascade(CascadeType.DELETE)
  private OperatingSystemModel operatingSystemModel;

  /**
   * Empty constructor for hibernate.
   */
  protected ImageModel() {
  }

  public ImageModel(String cloudUniqueId, String providerId, String name,
      CloudModel cloudModel, @Nullable LocationModel locationModel,
      @Nullable String loginUsernameOverride,
      @Nullable String loginPasswordOverride,
      OperatingSystemModel operatingSystemModel, DiscoveryItemState state) {
    super(cloudUniqueId, providerId, name, cloudModel, locationModel);
    this.loginUsernameOverride = loginUsernameOverride;
    this.loginPasswordOverride = loginPasswordOverride;

    checkNotNull(operatingSystemModel, "operatingSystemModel is null");
    this.operatingSystemModel = operatingSystemModel;

    checkNotNull(state, "state is null");
    this.state = state;
  }

  public OperatingSystemModel operatingSystem() {
    return operatingSystemModel;
  }

  @Override
  public String loginName() {
    if (loginUsernameOverride != null) {
      return loginUsernameOverride;
    }
    return operatingSystemModel.operatingSystemFamily().loginName();
  }

  public Optional<String> getLoginPasswordOverride() {
    return Optional.ofNullable(loginPasswordOverride);
  }

  public DiscoveryItemState getState() {
    return state;
  }
}
