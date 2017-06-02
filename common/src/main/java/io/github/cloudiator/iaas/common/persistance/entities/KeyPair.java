/*
 * Copyright (c) 2014-2017 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.iaas.common.persistance.entities;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * Created by daniel on 18.05.15.
 */
@Entity
public class KeyPair extends ResourceModel {

  @Lob
  private String privateKey;
  @Lob
  @Nullable
  @Column(nullable = true)
  private String publicKey;
  @Nullable
  @ManyToOne(optional = true)
  private VirtualMachine virtualMachine;

  /**
   * No-args constructor for hibernate
   */
  protected KeyPair() {
  }


  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  @Nullable
  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(@Nullable String publicKey) {
    this.publicKey = publicKey;
  }


  public Optional<VirtualMachine> virtualMachine() {
    return Optional.ofNullable(virtualMachine);
  }
}
