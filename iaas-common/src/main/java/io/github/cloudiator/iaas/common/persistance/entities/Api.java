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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.persistance.entities.Model;import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 31.10.14.
 */
@Entity
public class Api extends Model {

  @Column(nullable = false)
  private String internalProviderName;
  @OneToMany(mappedBy = "api")
  private List<CloudModel> cloudModels;

  /**
   * Empty constructor for hibernate.
   */
  protected Api() {
  }

  public Api(String internalProviderName) {
    checkNotNull(internalProviderName);
    checkArgument(!internalProviderName.isEmpty());
    this.internalProviderName = internalProviderName;
  }

  public String getInternalProviderName() {
    return internalProviderName;
  }

  public void setInternalProviderName(String internalProviderName) {
    checkNotNull(internalProviderName);
    checkArgument(!internalProviderName.isEmpty());
    this.internalProviderName = internalProviderName;
  }

  public List<CloudModel> getCloudModels() {
    return cloudModels;
  }

  public void setCloudModels(List<CloudModel> cloudModels) {
    this.cloudModels = cloudModels;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("internalProviderName", internalProviderName);
  }
}
