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
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.ByonNode;
import java.util.List;
import java.util.stream.Collectors;


class ByonNodeDomainRepository extends AbstractNodeDomainRepository {

  private final ByonNodeModelRepository byonNodeModelRepository;
  private ByonNodeConverter byonNodeConverter = new ByonNodeConverter();

  @Inject
  public ByonNodeDomainRepository(
      ByonNodeModelRepository byonNodeModelRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository,
      NodePropertiesModelRepository nodePropertiesModelRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository) {
    super(operatingSystemDomainRepository, geoLocationDomainRepository,
        nodePropertiesModelRepository, loginCredentialDomainRepository,
        ipAddressDomainRepository);
    this.byonNodeModelRepository = byonNodeModelRepository;
  }

  public List<ByonNode> find() {
    return byonNodeModelRepository.get().stream().map(byonNodeConverter)
        .collect(Collectors.toList());
  }

  ByonNodeModel saveAndGet(ByonNode domain) {
    boolean found = false;
    List<ByonNode> byonNodes = find();

    ByonNodeModel byonNodeModel = null;
    //equality not determined via userId and id, see NodeDomainrepository,
    //but on ByonNode object level
    for(ByonNode node: byonNodes) {
      if(node.equals(domain)) {
        byonNodeModel = createModel(node);
        found = true;
      }
    }

    if(found == false) {
      byonNodeModel = createModel(domain);
    } else {
      byonNodeModel = updateModel(domain, byonNodeModel);
    }

    byonNodeModelRepository.save(byonNodeModel);
    return byonNodeModel;
  }

  public void save(ByonNode domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  public void delete(String id) {

    //todo: delete group when last member leaves?

    checkNotNull(id, "id is null");
    ByonNodeModel byDomainId = byonNodeModelRepository.getByDomainId(id);

    checkState(byDomainId != null, "Node with the id %s does not exist.", id);
    byonNodeModelRepository.delete(byDomainId);
  }

  private ByonNodeModel createModel(ByonNode domain) {

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    return new ByonNodeModel(domain.id(), domain.name(), nodePropertiesModel,
        loginCredentialModel, ipGroupModel, domain.nodeCandidate().orElse(null),
        domain.diagnostic().orElse(null), domain.reason().orElse(null));
  }

  private ByonNodeModel updateModel(ByonNode domain, ByonNodeModel byonNodeModel) {

    checkState(domain.id().equals(byonNodeModel.getDomainId()), "domain id does not match");

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    byonNodeModel.setName(domain.name());
    byonNodeModel.setNodeProperties(nodePropertiesModel);
    byonNodeModel.setLoginCredential(loginCredentialModel);
    byonNodeModel.setIpGroup(ipGroupModel);
    byonNodeModel.setDiagnostic(domain.diagnostic().orElse(null));
    byonNodeModel.setReason(domain.reason().orElse(null));
    byonNodeModel.setNodeCandidate(domain.nodeCandidate().orElse(null));

    return byonNodeModel;
  }
}
