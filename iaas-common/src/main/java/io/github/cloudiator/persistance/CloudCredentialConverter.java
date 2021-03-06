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

import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CredentialsBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class CloudCredentialConverter implements
    OneWayConverter<CloudCredentialModel, CloudCredential> {

  @Nullable
  @Override
  public CloudCredential apply(@Nullable CloudCredentialModel cloudCredentialModel) {
    if (cloudCredentialModel == null) {
      return null;
    }

    return CredentialsBuilder.newBuilder()
        .user(cloudCredentialModel.getUser())
        .password(cloudCredentialModel.getPassword()).build();
  }
}
