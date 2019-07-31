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
    bind(ByonNodeModelRepository.class).to(ByonNodeModelRepositoryJpa.class);

    bind(NodePropertiesModelRepository.class).to(NodePropertiesModelRepositoryJpa.class);

    bind(IpAddressModelRepository.class).to(IpAddressModelRepositoryJpa.class);

    bind(IpGroupModelRepository.class).to(IpGroupModelRepositoryJpa.class);

    bind(LoginCredentialModelRepository.class).to(LoginCredentialModelRepositoryJpa.class);

    bind(VirtualMachineModelRepository.class).to(VirtualMachineModelRepositoryJpa.class);

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
