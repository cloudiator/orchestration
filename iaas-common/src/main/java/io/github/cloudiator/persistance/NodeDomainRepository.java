package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeGroup;
import io.github.cloudiator.domain.NodeProperties;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class NodeDomainRepository {

  private final NodeModelRepository nodeModelRepository;
  private final OperatingSystemDomainRepository operatingSystemDomainRepository;
  private final GeoLocationDomainRepository geoLocationDomainRepository;
  private final NodePropertiesModelRepository nodePropertiesModelRepository;
  private final TenantModelRepository tenantModelRepository;
  private final LoginCredentialDomainRepository loginCredentialDomainRepository;
  private final IpAddressDomainRepository ipAddressDomainRepository;
  private final NodeGroupModelRepository nodeGroupModelRepository;
  private NodeConverter nodeConverter = new NodeConverter();
  private NodeGroupConverter nodeGroupConverter = new NodeGroupConverter();

  @Inject
  public NodeDomainRepository(
      NodeModelRepository nodeModelRepository,
      OperatingSystemDomainRepository operatingSystemDomainRepository,
      GeoLocationDomainRepository geoLocationDomainRepository,
      NodePropertiesModelRepository nodePropertiesModelRepository,
      TenantModelRepository tenantModelRepository,
      LoginCredentialDomainRepository loginCredentialDomainRepository,
      IpAddressDomainRepository ipAddressDomainRepository,
      NodeGroupModelRepository nodeGroupModelRepository) {
    this.nodeModelRepository = nodeModelRepository;
    this.operatingSystemDomainRepository = operatingSystemDomainRepository;
    this.geoLocationDomainRepository = geoLocationDomainRepository;
    this.nodePropertiesModelRepository = nodePropertiesModelRepository;
    this.tenantModelRepository = tenantModelRepository;
    this.loginCredentialDomainRepository = loginCredentialDomainRepository;
    this.ipAddressDomainRepository = ipAddressDomainRepository;
    this.nodeGroupModelRepository = nodeGroupModelRepository;
  }

  public Node findByTenantAndId(String userId, String nodeId) {
    return nodeConverter.apply(nodeModelRepository.getByTenantAndDomainId(userId, nodeId));
  }

  public List<Node> findByTenant(String userId) {
    return nodeModelRepository.getByTenant(userId).stream().map(nodeConverter)
        .collect(Collectors.toList());
  }

  public NodeGroup findGroupByTenantAndId(String userId, String nodeGroupId) {
    return nodeGroupConverter
        .apply(nodeGroupModelRepository.findByTenantAndDomainId(userId, nodeGroupId));
  }

  public List<NodeGroup> findGroupsByTenant(String userId) {
    return nodeGroupModelRepository.findByTenant(userId).stream().map(nodeGroupConverter)
        .collect(Collectors.toList());
  }

  public void save(NodeGroup nodeGroup, String userId) {
    checkNotNull(nodeGroup, "nodeGroup is null");
    checkNotNull(userId, "userId is null");

    final TenantModel tenantModel = tenantModelRepository.createOrGet(userId);

    final NodeGroupModel nodeGroupModel = new NodeGroupModel(nodeGroup.id(), tenantModel);
    nodeGroupModelRepository.save(nodeGroupModel);

    for (Node node : nodeGroup.getNodes()) {
      final NodeModel nodeModel = saveAndGet(node, userId);
      nodeGroupModel.addNode(nodeModel);
      nodeModel.assignGroup(nodeGroupModel);
      nodeModelRepository.save(nodeModel);
    }

    nodeGroupModelRepository.save(nodeGroupModel);
  }

  NodeModel saveAndGet(Node domain, String userId) {
    NodeModel nodeModel = nodeModelRepository.getByTenantAndDomainId(userId, domain.id());

    if (nodeModel == null) {
      nodeModel = createModel(domain, userId, null);
    } else {
      nodeModel = updateModel(domain, nodeModel, userId);
    }
    nodeModelRepository.save(nodeModel);

    return nodeModel;
  }

  public void save(Node domain, String userId) {
    checkNotNull(domain, "domain is null");
    checkNotNull(userId, "userId is null");

    saveAndGet(domain, userId);
  }

  public void delete(String id, String userId) {
    checkNotNull(id, "id is null");
    checkNotNull(userId, "userId is null");

    NodeModel byTenantAndDomainId = nodeModelRepository.getByTenantAndDomainId(userId, id);
    checkState(byTenantAndDomainId != null, "Node with the id %s does not exist.", id);
    nodeModelRepository.delete(byTenantAndDomainId);
  }

  private NodeModel updateModel(Node domain, NodeModel nodeModel, String userId) {
    return null;
  }

  private NodeModel createModel(Node domain, String userId,
      @Nullable NodeGroupModel nodeGroupModel) {

    final TenantModel tenantModel = tenantModelRepository.createOrGet(userId);

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

    LoginCredentialModel loginCredentialModel = null;
    if (domain.loginCredential().isPresent()) {
      loginCredentialModel = loginCredentialDomainRepository
          .saveAndGet(domain.loginCredential().get());
    }

    final NodePropertiesModel nodePropertiesModel = new NodePropertiesModel(
        nodeProperties.providerId(),
        nodeProperties.numberOfCores(), nodeProperties.memory(), nodeProperties.disk().orElse(null),
        operatingSystemModel, geoLocationModel);

    nodePropertiesModelRepository.save(nodePropertiesModel);

    IpGroupModel ipGroupModel = null;
    if (!domain.ipAddresses().isEmpty()) {
      ipGroupModel = ipAddressDomainRepository.saveAndGet(domain.ipAddresses());
    }

    return new NodeModel(domain.id(), domain.name(), tenantModel, nodePropertiesModel,
        loginCredentialModel, domain.type(), ipGroupModel, nodeGroupModel);

  }

}
