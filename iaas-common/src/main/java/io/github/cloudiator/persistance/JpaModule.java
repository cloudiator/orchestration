package io.github.cloudiator.persistance;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.util.JpaContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 31.05.17.
 */
public class JpaModule extends AbstractModule {

  private final String jpaUnit;
  private final JpaContext jpaContext;

  public JpaModule(String jpaUnit, JpaContext jpaContext) {
    this.jpaUnit = jpaUnit;
    this.jpaContext = jpaContext;
  }

  @Override
  protected void configure() {

    install(buildPersistModule());

    bind(ApiModelRepository.class).to(ApiModelRepositoryJpa.class);

    bind(CloudModelRepository.class).to(CloudModelRepositoryJpa.class);

    bind(CloudConfigurationModelRepository.class).to(CloudConfigurationModelRepositoryJpa.class);
    bind(new TypeLiteral<ModelRepository<CloudConfigurationModel>>() {
    }).to(CloudConfigurationModelRepositoryJpa.class);

    bind(CloudCredentialModelRepository.class).to(CloudCredentialModelRepositoryJpa.class);

    bind(TenantModelRepository.class).to(TenantModelRepositoryJpa.class);

    bind(GeoLocationModelRepository.class).to(GeoLocationModelRepositoryJpa.class);

    bind(OperatingSystemModelRepository.class).to(OperatingSystemModelRepositoryJpa.class);

    bind(new TypeLiteral<ModelRepository<HardwareModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<HardwareModel>>() {
    });
    bind(new TypeLiteral<ResourceRepository<HardwareModel>>() {
    }).to(new TypeLiteral<BaseResourceRepositoryJpa<HardwareModel>>() {
    });

    bind(new TypeLiteral<ModelRepository<ImageModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<ImageModel>>() {
    });
    bind(new TypeLiteral<ResourceRepository<ImageModel>>() {
    }).to(new TypeLiteral<BaseResourceRepositoryJpa<ImageModel>>() {
    });

    bind(LocationModelRepository.class).to(LocationModelRepositoryJpa.class);

    bind(new TypeLiteral<ModelRepository<OperatingSystemModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<OperatingSystemModel>>() {
    });

    bind(HardwareOfferModelRepository.class).to(HardwareOfferModelRepositoryJpa.class);

    bind(NodeModelRepository.class).to(NodeModelRepositoryJpa.class);

    bind(NodePropertiesModelRepository.class).to(NodePropertiesModelRepositoryJpa.class);

    bind(IpAddressModelRepository.class).to(IpAddressModelRepositoryJpa.class);

    bind(IpGroupModelRepository.class).to(IpGroupModelRepositoryJpa.class);

    bind(NodeGroupModelRepository.class).to(NodeGroupModelRepositoryJpa.class);

    bind(LoginCredentialModelRepository.class).to(LoginCredentialModelRepositoryJpa.class);

  }

  private JpaPersistModule buildPersistModule() {
    final JpaPersistModule jpaPersistModule = new JpaPersistModule(jpaUnit);
    Map<String, String> config = new HashMap<>();
    config.put("hibernate.dialect", jpaContext.dialect());
    config.put("javax.persistence.jdbc.driver", jpaContext.driver());
    config.put("javax.persistence.jdbc.url", jpaContext.url());
    config.put("javax.persistence.jdbc.user", jpaContext.user());
    config.put("javax.persistence.jdbc.password", jpaContext.password());
    jpaPersistModule.properties(config);
    return jpaPersistModule;
  }
}
