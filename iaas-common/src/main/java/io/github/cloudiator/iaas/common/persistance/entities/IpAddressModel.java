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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import de.uniulm.omi.cloudiator.persistance.entities.Model;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpAddressType;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress.IpVersion;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

/**
 * Created by daniel on 12.03.15.
 */
@Entity
public class IpAddressModel extends Model {

  @Column(updatable = false)
  private String ip;
  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  private IpVersion version;
  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  private IpAddressType type;

  @ManyToOne(optional = false)
  private VirtualMachineModel virtualMachineModel;

  /**
   * Empty constructor for hibernate.
   */
  protected IpAddressModel() {
  }

  public IpAddressModel(VirtualMachineModel virtualMachineModel, String ip, IpVersion version,
      IpAddressType type) {

    checkNotNull(virtualMachineModel);
    checkNotNull(ip);
    checkArgument(!ip.isEmpty());
    checkNotNull(version);
    checkNotNull(type);

    this.virtualMachineModel = virtualMachineModel;
    this.ip = ip;
    this.version = version;
    this.type = type;

  }

  public String getIp() {
    return ip;
  }

  public IpVersion getVersion() {
    return version;
  }

  public IpAddressType getType() {
    return type;
  }

  public VirtualMachineModel getVirtualMachineModel() {
    return virtualMachineModel;
  }

  @Override
  public String toString() {
    return ip;
  }
}
