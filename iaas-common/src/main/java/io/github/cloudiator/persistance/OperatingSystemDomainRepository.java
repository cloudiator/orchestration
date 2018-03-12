package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;

/**
 * Created by daniel on 02.06.17.
 */
public class OperatingSystemDomainRepository {

  private final OperatingSystemModelRepository operatingSystemModelRepository;

  @Inject
  public OperatingSystemDomainRepository(
      OperatingSystemModelRepository operatingSystemModelRepository) {
    this.operatingSystemModelRepository = operatingSystemModelRepository;
  }

  public void save(OperatingSystem domain) {
    saveAndGet(domain);
  }

  OperatingSystemModel saveAndGet(OperatingSystem domain) {
    final OperatingSystemModel model = createModel(domain);
    operatingSystemModelRepository.save(model);
    return model;
  }

  void update(OperatingSystem domain, OperatingSystemModel model) {
    updateModel(domain, model);
    operatingSystemModelRepository.save(model);
  }

  private OperatingSystemModel createModel(OperatingSystem domain) {

    return new OperatingSystemModel(
        domain.operatingSystemArchitecture(), domain.operatingSystemFamily(),
        String.valueOf(domain.operatingSystemVersion().version()));
  }

  private void updateModel(OperatingSystem domain, OperatingSystemModel model) {

    model.setOperatingSystemArchitecture(domain.operatingSystemArchitecture());
    model.setOperatingSystemFamily(domain.operatingSystemFamily());
    model.setVersion(String.valueOf(domain.operatingSystemVersion().version()));

  }
}
