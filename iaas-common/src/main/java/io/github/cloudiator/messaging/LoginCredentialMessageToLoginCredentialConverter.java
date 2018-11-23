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

import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredentialBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.LoginCredential.Builder;

public class LoginCredentialMessageToLoginCredentialConverter implements
    TwoWayConverter<IaasEntities.LoginCredential, LoginCredential> {

  @Override
  public IaasEntities.LoginCredential applyBack(LoginCredential loginCredential) {
    Builder builder = IaasEntities.LoginCredential.newBuilder();
    if (loginCredential.password().isPresent()) {
      builder.setPassword(loginCredential.password().get());
    }
    if (loginCredential.privateKey().isPresent()) {
      builder.setPrivateKey(loginCredential.privateKey().get());
    }
    if (loginCredential.username().isPresent()) {
      builder.setUsername(loginCredential.username().get());
    }
    return builder.build();
  }

  @Override
  public LoginCredential apply(IaasEntities.LoginCredential loginCredential) {

    LoginCredentialBuilder loginCredentialBuilder = LoginCredentialBuilder.newBuilder();

    if (loginCredential.getUsername() != null && !loginCredential.getUsername().isEmpty()) {
      loginCredentialBuilder.username(loginCredential.getUsername());
    }

    if (loginCredential.getPassword() != null && !loginCredential.getPassword().isEmpty()) {
      loginCredentialBuilder.password(loginCredential.getPassword());
    }

    if (loginCredential.getPrivateKey() != null && !loginCredential.getPrivateKey().isEmpty()) {
      loginCredentialBuilder.privateKey(loginCredential.getPrivateKey());
    }

    return loginCredentialBuilder.build();
  }
}
