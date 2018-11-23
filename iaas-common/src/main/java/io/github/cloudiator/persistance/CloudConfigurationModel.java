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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class CloudConfigurationModel extends Model {

  @Column(nullable = false, updatable = false)
  private String nodeGroup;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "cloudConfigurationModel")
  private List<PropertyModel> properties;

  /**
   * Empty constructor for hibernate.
   */
  protected CloudConfigurationModel() {

  }

  public CloudConfigurationModel(String nodeGroup) {
    checkNotNull(nodeGroup, "nodeGroup is null");
    checkArgument(!nodeGroup.isEmpty(), "nodeGroup is empty");
    this.nodeGroup = nodeGroup;
  }

  public void addProperty(String key, String value) {
    PropertyModel propertyModel = new PropertyModel(this, key, value);
    addProperty(propertyModel);
  }

  public void addProperty(PropertyModel propertyModel) {
    checkNotNull(propertyModel, "propertyModel is null");
    if (properties == null) {
      properties = new ArrayList<>();
    }
    properties.add(propertyModel);
  }

  public String getNodeGroup() {
    return nodeGroup;
  }

  public List<PropertyModel> getProperties() {
    return properties;
  }
}
