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

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersion;

import static com.google.common.base.Preconditions.checkNotNull;

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

  OperatingSystemModel saveOrGet(OperatingSystem domain) {
    checkNotNull(domain, "domain is null");
    OperatingSystemModel operatingSystemModel = operatingSystemModelRepository.findByArchitectureFamilyVersion(domain.operatingSystemArchitecture(), domain.operatingSystemFamily(), domain.operatingSystemVersion());
    if (operatingSystemModel == null) {
      operatingSystemModel = saveAndGet(domain);
    }
    return operatingSystemModel;
  }

  public OperatingSystemModel findByArchitectureFamilyVersion(OperatingSystemArchitecture architecture, OperatingSystemFamily family, OperatingSystemVersion version) {
    return operatingSystemModelRepository.findByArchitectureFamilyVersion(architecture, family, version);
  }

  void update(OperatingSystem domain, OperatingSystemModel model) {
    updateModel(domain, model);
    operatingSystemModelRepository.save(model);
  }

  private OperatingSystemModel createModel(OperatingSystem domain) {

    return new OperatingSystemModel(
        domain.operatingSystemArchitecture(), domain.operatingSystemFamily(),
        domain.operatingSystemVersion().version());
  }

  private void updateModel(OperatingSystem domain, OperatingSystemModel model) {

    model.setOperatingSystemArchitecture(domain.operatingSystemArchitecture());
    model.setOperatingSystemFamily(domain.operatingSystemFamily());
    model.setVersion(domain.operatingSystemVersion().version());

  }
}
