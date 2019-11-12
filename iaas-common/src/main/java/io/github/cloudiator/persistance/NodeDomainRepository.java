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
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.stream.Collectors;

public class NodeDomainRepository extends AbstractNodeDomainRepository {

  private final NodeModelRepository nodeModelRepository;
  private NodeConverter nodeConverter = new NodeConverter();

  @Inject
  public NodeDomainRepository(
      NodeModelRepository nodeModelRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository,
      NodePropertiesModelRepository nodePropertiesModelRepository,
      TenantModelRepository tenantModelRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository) {
    super(tenantModelRepository, operatingSystemDomainRepository, geoLocationDomainRepository,
        nodePropertiesModelRepository, loginCredentialDomainRepository,
        ipAddressDomainRepository);
    this.nodeModelRepository = nodeModelRepository;
  }

  public Node findByTenantAndId(String userId, String nodeId) {
    return nodeConverter.apply(nodeModelRepository.getByTenantAndDomainId(userId, nodeId));
  }

  public List<Node> findByTenant(String userId) {
    return nodeModelRepository.getByTenant(userId).stream().map(nodeConverter)
        .collect(Collectors.toList());
  }

  NodeModel saveAndGet(Node domain) {
    NodeModel nodeModel = nodeModelRepository.getByTenantAndDomainId(domain.userId(), domain.id());

    if (nodeModel == null) {
      nodeModel = createModel(domain);
    } else {
      nodeModel = updateModel(domain, nodeModel);
    }
    nodeModelRepository.save(nodeModel);

    return nodeModel;
  }

  public void save(Node domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  public void delete(String id) {

    //todo: delete group when last member leaves?

    checkNotNull(id, "id is null");

    NodeModel byDomainId = nodeModelRepository.getByDomainId(id);

    checkState(byDomainId != null, String.format("Node with the id %s does not exist.", id));
    nodeModelRepository.delete(byDomainId);
  }

  private NodeModel updateModel(Node domain, NodeModel nodeModel) {

    checkState(domain.id().equals(nodeModel.getDomainId()), "domain id does not match");
    checkState(
        domain.userId().equals(nodeModel.getTenantModel().getUserId()), "user id does not match");

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    if (nodeModel.getOriginId() != null) {
      checkState(domain.originId().isPresent(), "model has a originId but domain object does not.");
      checkState(domain.originId().get().equals(nodeModel.getOriginId()),
          "origin id does not match");

    }
    nodeModel.setOriginId(domain.originId().orElse(null));
    nodeModel.setName(domain.name());
    nodeModel.setNodeProperties(nodePropertiesModel);
    nodeModel.setLoginCredential(loginCredentialModel);
    nodeModel.setType(domain.type());
    nodeModel.setIpGroup(ipGroupModel);
    nodeModel.setNodeState(domain.state());
    nodeModel.setDiagnostic(domain.diagnostic().orElse(null));
    nodeModel.setReason(domain.reason().orElse(null));
    nodeModel.setNodeCandidate(domain.nodeCandidate().orElse(null));

    return nodeModel;
  }

  private NodeModel createModel(Node domain) {

    final TenantModel tenantModel = tenantModelRepository.createOrGet(domain.userId());

    final NodePropertiesModel nodePropertiesModel = generateNodeProperties(domain);
    final LoginCredentialModel loginCredentialModel = generateLoginCredential(domain);
    final IpGroupModel ipGroupModel = generateIpModel(domain);

    return new NodeModel(domain.id(), domain.originId().orElse(null), domain.name(), tenantModel,
        nodePropertiesModel,
        loginCredentialModel, domain.type(), ipGroupModel, domain.state(),
        domain.nodeCandidate().orElse(null),
        domain.diagnostic().orElse(null), domain.reason().orElse(null));

  }
}
