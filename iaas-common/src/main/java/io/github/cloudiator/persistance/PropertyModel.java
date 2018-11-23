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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
class PropertyModel extends Model {

  @Column(nullable = false)
  private String propertyKey;
  @Column(nullable = false)
  private String propertyValue;
  @ManyToOne
  private CloudConfigurationModel cloudConfigurationModel;

  /**
   * Empty constructor for hibernate
   */
  protected PropertyModel() {

  }

  public PropertyModel(CloudConfigurationModel cloudConfigurationModel, String key, String value) {
    checkNotNull(cloudConfigurationModel);
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    checkArgument(!key.isEmpty(), "key is empty");
    checkArgument(!value.isEmpty(), "value is empty");
    this.cloudConfigurationModel = cloudConfigurationModel;
    this.propertyKey = key;
    this.propertyValue = value;
  }

  public String getKey() {
    return propertyKey;
  }

  public String getValue() {
    return propertyValue;
  }
}
