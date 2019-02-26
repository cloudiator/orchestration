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

package io.github.cloudiator.messaging;

import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import org.cloudiator.messages.entities.IaasEntities.NewCloud;

/**
 * Created by daniel on 15.03.17.
 */
public class InitializeCloudFromNewCloud {

  public static final InitializeCloudFromNewCloud INSTANCE = new InitializeCloudFromNewCloud();

  private static final ApiMessageToApi API_CONVERTER = ApiMessageToApi.INSTANCE;
  private static final ConfigurationMessageToConfiguration CONFIGURATION_CONVERTER =
      ConfigurationMessageToConfiguration.INSTANCE;
  private static final CredentialMessageToCredential CREDENTIAL_CONVERTER = CredentialMessageToCredential.INSTANCE;
  private static final CloudTypeMessageToCloudType CLOUD_TYPE_CONVERTER = CloudTypeMessageToCloudType.INSTANCE;

  private InitializeCloudFromNewCloud() {

  }

  public ExtendedCloud apply(NewCloud newCloud, String userId) {
    final ExtendedCloudBuilder cloudBuilder = ExtendedCloudBuilder.newBuilder()
        .credentials(CREDENTIAL_CONVERTER.apply(newCloud.getCredential()))
        .api(API_CONVERTER.apply(newCloud.getApi()))
        .configuration(CONFIGURATION_CONVERTER.apply(newCloud.getConfiguration()))
        .cloudType(CLOUD_TYPE_CONVERTER.apply(newCloud.getCloudType())
        );

    if (newCloud.getEndpoint() == null || newCloud.getEndpoint().equals("")) {
      cloudBuilder.endpoint(null);
    } else {
      cloudBuilder.endpoint(newCloud.getEndpoint());
    }

    cloudBuilder.state(CloudState.NEW);
    cloudBuilder.userId(userId);

    return cloudBuilder.build();
  }


}
