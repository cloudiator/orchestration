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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import java.util.Collection;
import java.util.Collections;

public class IpAddressDomainRepository {

  private final IpGroupModelRepository ipGroupModelRepository;

  @Inject
  public IpAddressDomainRepository(
      IpGroupModelRepository ipGroupModelRepository) {
    this.ipGroupModelRepository = ipGroupModelRepository;
  }

  IpGroupModel saveAndGet(Collection<IpAddress> ipAddresses) {
    return create(ipAddresses);
  }

  IpGroupModel saveAndGet(IpAddress ipAddress) {
    return saveAndGet(Collections.singleton(ipAddress));
  }

  private IpGroupModel create(Collection<IpAddress> ipAddresses) {

    IpGroupModel ipGroupModel = new IpGroupModel();
    ipGroupModelRepository.save(ipGroupModel);

    for (IpAddress ipAddress : ipAddresses) {
      IpAddressModel ipAddressModel = new IpAddressModel(ipGroupModel, ipAddress.ip(),
          ipAddress.version(), ipAddress.type());
      ipGroupModel.addIpAddress(ipAddressModel);
    }

    ipGroupModelRepository.save(ipGroupModel);

    return ipGroupModel;
  }


}
