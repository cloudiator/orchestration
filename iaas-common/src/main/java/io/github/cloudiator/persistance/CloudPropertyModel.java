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

package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by daniel on 08.09.15.
 */
@Entity
class CloudPropertyModel extends Model {

  @Column(nullable = false, name = "cloudproperty_key")
  private String key;
  @Column(nullable = false)
  private String value;
  @ManyToOne(optional = false)
  private CloudModel cloudModel;

  public CloudPropertyModel(String key, String value, CloudModel cloudModel) {
    this.key = key;
    this.value = value;
    this.cloudModel = cloudModel;
  }

  protected CloudPropertyModel() {
  }

  public String key() {
    return key;
  }

  public String value() {
    return value;
  }

  public void value(String value) {
    this.value = value;
  }

  public CloudModel cloud() {
    return cloudModel;
  }
}
