package io.github.cloudiator.iaas.common.persistance.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by daniel on 09.06.17.
 */
public class CloudDomainRepository implements DomainRepository<Cloud> {

  private final CloudModelRepository cloudModelRepository;
  private final CloudRegistry cloudRegistry;

  @Inject
  public CloudDomainRepository(
      CloudModelRepository cloudModelRepository,
      CloudRegistry cloudRegistry) {
    this.cloudModelRepository = cloudModelRepository;
    this.cloudRegistry = cloudRegistry;
  }

  @Override
  public Cloud findById(String id) {
    checkNotNull(id, "id is null");
    return cloudRegistry.getCloud(id);
  }

  @Override
  public void save(Cloud cloud) {
    //todo implement and refactor CloudAddedSubscriber with the logic
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(Cloud cloud) {
    //todo implement and implement CloudRemovedSubscriber
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Cloud> findAll() {
    return Lists.newArrayList(cloudRegistry.list());
  }

  public List<Cloud> findByUser(String userId) {
    return cloudModelRepository.getByTenant(userId).stream()
        .map(CloudModel::getCloudId).map(cloudRegistry::getCloud).collect(Collectors.toList());
  }
}
