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
import de.uniulm.omi.cloudiator.sword.domain.Configuration;

class CloudConfigurationDomainRepository {

  private final ModelRepository<CloudConfigurationModel> cloudConfigurationModelModelRepository;

  @Inject
  CloudConfigurationDomainRepository(
      ModelRepository<CloudConfigurationModel> cloudConfigurationModelModelRepository) {
    this.cloudConfigurationModelModelRepository = cloudConfigurationModelModelRepository;
  }

  public void save(Configuration domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  CloudConfigurationModel saveAndGet(Configuration domain) {
    checkNotNull(domain, "domain is null");
    final CloudConfigurationModel model = createModel(domain);
    cloudConfigurationModelModelRepository.save(model);
    return model;
  }

  void update(Configuration domain, CloudConfigurationModel model) {
    updateModel(domain, model);
    cloudConfigurationModelModelRepository.save(model);
  }

  private CloudConfigurationModel createModel(Configuration domain) {

    final CloudConfigurationModel cloudConfigurationModel = new CloudConfigurationModel(
        domain.nodeGroup());
    setProperties(cloudConfigurationModel, domain);

    return cloudConfigurationModel;
  }

  private void setProperties(CloudConfigurationModel cloudConfigurationModel,
      Configuration domain) {
    domain.properties().getProperties().forEach((key, value) -> cloudConfigurationModel
        .addProperty(new PropertyModel(cloudConfigurationModel, key, value)));
  }

  private void updateModel(Configuration domain, CloudConfigurationModel model) {

    checkState(domain.nodeGroup().equals(model.getNodeGroup()),
        "Updating node group is not allowed");
    model.getProperties().clear();

    setProperties(model, domain);
  }
}
