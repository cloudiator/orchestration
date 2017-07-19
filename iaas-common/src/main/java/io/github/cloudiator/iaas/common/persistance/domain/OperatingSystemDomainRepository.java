package io.github.cloudiator.iaas.common.persistance.domain;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import io.github.cloudiator.iaas.common.persistance.entities.OperatingSystemModel;
import io.github.cloudiator.iaas.common.persistance.repositories.ModelRepository;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by daniel on 02.06.17.
 */
public class OperatingSystemDomainRepository implements DomainRepository<OperatingSystem> {

  private final ModelRepository<OperatingSystemModel> operatingSystemModelRepository;

  public OperatingSystemDomainRepository(
      ModelRepository<OperatingSystemModel> operatingSystemModelRepository) {
    this.operatingSystemModelRepository = operatingSystemModelRepository;
  }

  @Override
  public OperatingSystem findById(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void save(OperatingSystem operatingSystem) {
    OperatingSystemModel operatingSystemModel = new OperatingSystemModel(
        operatingSystem.operatingSystemArchitecture(), operatingSystem.operatingSystemFamily(),
        String.valueOf(operatingSystem.operatingSystemVersion().version()));
    operatingSystemModelRepository.save(operatingSystemModel);
  }

  @Override
  public void delete(OperatingSystem operatingSystem) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<OperatingSystem> findAll() {
    return operatingSystemModelRepository.findAll().stream().map(
        (Function<OperatingSystemModel, OperatingSystem>) operatingSystemModel -> operatingSystemModel)
        .collect(Collectors.toList());
  }
}
