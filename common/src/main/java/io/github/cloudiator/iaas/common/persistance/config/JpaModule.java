package io.github.cloudiator.iaas.common.persistance.config;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.iaas.common.persistance.entities.CloudModel;
import io.github.cloudiator.iaas.common.persistance.entities.CloudCredential;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareModel;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareOffer;
import io.github.cloudiator.iaas.common.persistance.entities.ImageModel;
import io.github.cloudiator.iaas.common.persistance.entities.LocationModel;
import io.github.cloudiator.iaas.common.persistance.entities.OperatingSystemModel;
import io.github.cloudiator.iaas.common.persistance.entities.Tenant;
import io.github.cloudiator.iaas.common.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.BaseResourceRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.CloudModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.HardwareOfferRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.HardwareOfferRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.LocationModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.ResourceRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepository;
import io.github.cloudiator.iaas.common.persistance.repositories.TenantModelRepositoryJpa;

/**
 * Created by daniel on 31.05.17.
 */
public class JpaModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(new TypeLiteral<ModelRepository<CloudModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<CloudModel>>() {
    });
    bind(CloudModelRepository.class).to(CloudModelRepositoryJpa.class);

    bind(new TypeLiteral<ModelRepository<CloudCredential>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<CloudCredential>>() {
    });

    bind(new TypeLiteral<ModelRepository<Tenant>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<Tenant>>() {
    });
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
    bind(new TypeLiteral<ModelRepository<ImageModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<ImageModel>>() {
    });
    bind(new TypeLiteral<ResourceRepository<ImageModel>>() {
    }).to(new TypeLiteral<BaseResourceRepositoryJpa<ImageModel>>() {
    });

    bind(new TypeLiteral<ModelRepository<LocationModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<LocationModel>>() {
    });
    bind(LocationModelRepository.class).to(LocationModelRepositoryJpa.class);

    bind(new TypeLiteral<ModelRepository<OperatingSystemModel>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<OperatingSystemModel>>() {
    });

    bind(new TypeLiteral<ModelRepository<HardwareOffer>>() {
    }).to(new TypeLiteral<BaseModelRepositoryJpa<HardwareOffer>>() {
    });
    bind(HardwareOfferRepository.class).to(HardwareOfferRepositoryJpa.class);


  }
}
