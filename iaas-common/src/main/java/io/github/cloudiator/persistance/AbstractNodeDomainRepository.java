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
import io.github.cloudiator.domain.AbstractNode;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.NodeProperties;
import javax.annotation.Nullable;

abstract class AbstractNodeDomainRepository {

  protected final TenantModelRepository tenantModelRepository;
  protected final OperatingSystemDomainRepository operatingSystemDomainRepository;
  protected final GeoLocationDomainRepository geoLocationDomainRepository;
  protected final NodePropertiesModelRepository nodePropertiesModelRepository;
  protected final LoginCredentialDomainRepository loginCredentialDomainRepository;
  protected final IpAddressDomainRepository ipAddressDomainRepository;

  @Inject
  public AbstractNodeDomainRepository() {
    this.tenantModelRepository = null;
    this.operatingSystemDomainRepository = null;
    this.geoLocationDomainRepository = null;
    this.nodePropertiesModelRepository = null;
    this.loginCredentialDomainRepository = null;
    this.ipAddressDomainRepository = null;
  }

  @Inject
  public AbstractNodeDomainRepository(
      TenantModelRepository tenantModelRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository,
      NodePropertiesModelRepository nodePropertiesModelRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository) {
    this.tenantModelRepository = tenantModelRepository;
    this.operatingSystemDomainRepository = operatingSystemDomainRepository;
    this.geoLocationDomainRepository = geoLocationDomainRepository;
    this.nodePropertiesModelRepository = nodePropertiesModelRepository;
    this.loginCredentialDomainRepository = loginCredentialDomainRepository;
    this.ipAddressDomainRepository = ipAddressDomainRepository;
  }

  @Nullable
  protected IpGroupModel generateIpModel(AbstractNode domain) {
    IpGroupModel ipGroupModel = null;
    if (!domain.ipAddresses().isEmpty()) {
      ipGroupModel = ipAddressDomainRepository.saveAndGet(domain.ipAddresses());
    }
    return ipGroupModel;
  }

  @Nullable
  protected LoginCredentialModel generateLoginCredential(AbstractNode domain) {
    LoginCredentialModel loginCredentialModel = null;
    if (domain.loginCredential().isPresent()) {
      loginCredentialModel = loginCredentialDomainRepository
          .saveAndGet(domain.loginCredential().get());
    }
    return loginCredentialModel;
  }

  protected NodePropertiesModel generateNodeProperties(AbstractNode domain) {

    final NodeProperties nodeProperties = domain.nodeProperties();

    OperatingSystemModel operatingSystemModel = null;
    if (nodeProperties.operatingSystem().isPresent()) {
      operatingSystemModel = operatingSystemDomainRepository
          .saveAndGet(nodeProperties.operatingSystem().get());
    }

    GeoLocationModel geoLocationModel = null;
    if (nodeProperties.geoLocation().isPresent()) {
      geoLocationModel = geoLocationDomainRepository.saveAndGet(nodeProperties.geoLocation().get());
    }

    final NodePropertiesModel nodePropertiesModel = new NodePropertiesModel(
        nodeProperties.providerId(),
        nodeProperties.numberOfCores().orElse(null), nodeProperties.memory().orElse(null),
        nodeProperties.disk().orElse(null),
        operatingSystemModel, geoLocationModel);

    nodePropertiesModelRepository.save(nodePropertiesModel);

    return nodePropertiesModel;

  }
}
