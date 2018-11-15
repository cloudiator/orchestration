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

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import javax.annotation.Nullable;

class CloudModelConverter implements OneWayConverter<CloudModel, ExtendedCloud> {

  private final static CloudCredentialConverter CLOUD_CREDENTIAL_CONVERTER = new CloudCredentialConverter();
  private final static CloudConfigurationConverter CLOUD_CONFIGURATION_CONVERTER = new CloudConfigurationConverter();

  @Nullable
  @Override
  public ExtendedCloud apply(@Nullable CloudModel cloudModel) {

    if (cloudModel == null) {
      return null;
    }

    final Api api = ApiBuilder.newBuilder().providerName(cloudModel.getApiModel().getProviderName())
        .build();
    final CloudCredential cloudCredential = CLOUD_CREDENTIAL_CONVERTER
        .apply(cloudModel.getCloudCredential());

    final Configuration configuration = CLOUD_CONFIGURATION_CONVERTER
        .apply(cloudModel.getCloudConfiguration());

    return ExtendedCloudBuilder.newBuilder().api(api).cloudType(cloudModel.getCloudType())
        .configuration(configuration).credentials(cloudCredential)
        .endpoint(cloudModel.getEndpoint()).state(cloudModel.getCloudState())
        .diagnostic(cloudModel.getDiagnostic()).userId(cloudModel.getTenantModel().getUserId())
        .build();
  }
}
