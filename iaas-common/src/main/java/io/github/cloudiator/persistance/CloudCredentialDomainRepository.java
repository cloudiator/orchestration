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
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;

public class CloudCredentialDomainRepository {

  private final CloudCredentialModelRepository cloudCredentialModelRepository;

  @Inject
  public CloudCredentialDomainRepository(
      CloudCredentialModelRepository cloudCredentialModelRepository) {
    this.cloudCredentialModelRepository = cloudCredentialModelRepository;
  }


  public void save(CloudCredential domain) {
    checkNotNull(domain, "domain is null");
    saveAndGet(domain);
  }

  CloudCredentialModel saveAndGet(CloudCredential domain) {
    checkNotNull(domain, "domain is null");
    final CloudCredentialModel model = createModel(domain);
    cloudCredentialModelRepository.save(model);
    return model;
  }

  void update(CloudCredential domain, CloudCredentialModel model) {
    updateModel(domain, model);
    cloudCredentialModelRepository.save(model);
  }

  private CloudCredentialModel createModel(CloudCredential domain) {
    return new CloudCredentialModel(domain.user(), domain.password());
  }


  private void updateModel(CloudCredential domain, CloudCredentialModel model) {
    checkState(domain.user().equals(model.getUser()), "updating user not permitted.");
    model.setPassword(domain.password());
  }
}
