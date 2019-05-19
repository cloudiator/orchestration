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
import io.github.cloudiator.domain.BaseNode;
import io.github.cloudiator.domain.NodeProperties;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;


class BaseNodeDomainRepository extends AbstractNodeDomainRepository {

  private final BaseNodeModelRepository baseNodeModelRepository;
  private BaseNodeConverter baseNodeConverter = new BaseNodeConverter();

  @Inject
  public BaseNodeDomainRepository(
      BaseNodeModelRepository baseNodeModelRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository,
      NodePropertiesModelRepository nodePropertiesModelRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository) {
    super(operatingSystemDomainRepository, geoLocationDomainRepository,
        nodePropertiesModelRepository, loginCredentialDomainRepository,
        ipAddressDomainRepository);
    this.baseNodeModelRepository = baseNodeModelRepository;
  }

  public List<BaseNode> find() {
    return baseNodeModelRepository.get().stream().map(baseNodeConverter)
        .collect(Collectors.toList());
  }

  BaseNodeModel saveAndGet(BaseNode domain) {
    boolean found = false;
    List<BaseNode> baseNodes = find();

    BaseNodeModel baseNodeModel = null;
    //equality not determined via userId and id, see NodeDomainrepository,
    //but on BaseNode object level
    for(BaseNode node: baseNodes) {
      if(node.equals(domain)) {
        baseNodeModel = createModel(node);
        found = true;
      }
    }

    if(found == false) {
      baseNodeModel = createModel(domain);
    } else {
      baseNodeModel = updateModel(domain, baseNodeModel);
    }

    baseNodeModelRepository.save(baseNodeModel);
    return baseNodeModel;
  }

  public void save(BaseNode domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  public void delete(String id) {

    //todo: delete group when last member leaves?

    checkNotNull(id, "id is null");
    BaseNodeModel byDomainId = baseNodeModelRepository.getByDomainId(id);

    checkState(byDomainId != null, "Node with the id %s does not exist.", id);
    baseNodeModelRepository.delete(byDomainId);
  }

  private BaseNodeModel createModel(BaseNode domain) {

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    return new BaseNodeModel(domain.id(), domain.name(), nodePropertiesModel,
        loginCredentialModel, ipGroupModel, domain.nodeCandidate().orElse(null),
        domain.diagnostic().orElse(null), domain.reason().orElse(null));
  }

  private BaseNodeModel updateModel(BaseNode domain, BaseNodeModel baseNodeModel) {

    checkState(domain.id().equals(baseNodeModel.getDomainId()), "domain id does not match");

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    baseNodeModel.setName(domain.name());
    baseNodeModel.setNodeProperties(nodePropertiesModel);
    baseNodeModel.setLoginCredential(loginCredentialModel);
    baseNodeModel.setIpGroup(ipGroupModel);
    baseNodeModel.setDiagnostic(domain.diagnostic().orElse(null));
    baseNodeModel.setReason(domain.reason().orElse(null));
    baseNodeModel.setNodeCandidate(domain.nodeCandidate().orElse(null));

    return baseNodeModel;
  }
}
