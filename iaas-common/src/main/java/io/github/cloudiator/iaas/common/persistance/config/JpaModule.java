package io.github.cloudiator.iaas.common.persistance.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareModel;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.entities.OperatingSystemModel;
import io.github.cloudiator.iaas.common.persistance.repositories.ApiModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ApiModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.BaseResourceRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudConfigurationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudConfigurationModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudCredentialModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudCredentialModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.HardwareOfferRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.HardwareOfferRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepositoryJpa;

/**
 * Created by daniel on 31.05.17.
 */
public class JpaModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(ApiModelRepository.class).to(ApiModelRepositoryJpa.class);

    bind(CloudModelRepository.class).to(CloudModelRepositoryJpa.class);

    bind(CloudConfigurationModelRepository.class).to(CloudConfigurationModelRepositoryJpa.class);

    bind(CloudCredentialModelRepository.class).to(CloudCredentialModelRepositoryJpa.class);

    bind(TenantModelRepository.class).to(TenantModelRepositoryJpa.class);

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

    bind(HardwareOfferRepository.class).to(HardwareOfferRepositoryJpa.class);


  }
}
