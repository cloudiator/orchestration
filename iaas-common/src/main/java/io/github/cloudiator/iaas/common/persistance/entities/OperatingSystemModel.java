/*
 * Copyright (c) 2014-2017 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.iaas.common.persistance.entities;


import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersion;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import de.uniulm.omi.cloudiator.persistance.entities.Model;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by daniel on 04.11.14.
 */
@Entity
public class OperatingSystemModel extends Model
    implements de.uniulm.omi.cloudiator.domain.OperatingSystem {

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OperatingSystemArchitecture
      operatingSystemArchitecture;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OperatingSystemFamily
      operatingSystemFamily;

  @Nullable
  private String version;

  /**
   * Empty constructor for hibernate.
   */
  protected OperatingSystemModel() {
  }

  public OperatingSystemModel(OperatingSystemArchitecture operatingSystemArchitecture,
      OperatingSystemFamily operatingSystemFamily, @Nullable String version) {

    checkNotNull(operatingSystemArchitecture);
    checkNotNull(operatingSystemFamily);

    this.operatingSystemArchitecture = operatingSystemArchitecture;
    this.operatingSystemFamily = operatingSystemFamily;
    this.version = version;
  }

  public OperatingSystemModel(de.uniulm.omi.cloudiator.domain.OperatingSystem operatingSystem) {
    checkNotNull(operatingSystem);
    this.operatingSystemArchitecture = operatingSystem.operatingSystemArchitecture();
    this.operatingSystemFamily = operatingSystem.operatingSystemFamily();
    this.version = operatingSystem.operatingSystemVersion().name().orElse(null);
  }

  @Override
  public OperatingSystemFamily operatingSystemFamily() {
    return operatingSystemFamily;
  }

  @Override
  public OperatingSystemArchitecture operatingSystemArchitecture() {
    return operatingSystemArchitecture;
  }

  @Override
  public OperatingSystemVersion operatingSystemVersion() {
    if (version == null) {
      return OperatingSystemVersions.unknown();
    }
    return OperatingSystemVersions
        .of(version, operatingSystemFamily.operatingSystemVersionFormat());
  }
}
