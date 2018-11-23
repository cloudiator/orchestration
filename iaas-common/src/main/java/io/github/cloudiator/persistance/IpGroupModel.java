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

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class IpGroupModel extends Model {

  /**
   * Use set to avoid duplicate entries due to hibernate bug https://hibernate.atlassian.net/browse/HHH-7404
   */
  @OneToMany(mappedBy = "ipGroup", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<IpAddressModel> ipAddressModels;

  IpGroupModel() {

  }

  public void addIpAddress(IpAddressModel ipAddressModel) {
    if (ipAddressModels == null) {
      this.ipAddressModels = new HashSet<>();
    }
    ipAddressModels.add(ipAddressModel);
  }

  public Set<IpAddressModel> getIpAddresses() {
    return ImmutableSet.copyOf(ipAddressModels);
  }


}
