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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by daniel on 31.10.14.
 */
@Entity
class ApiModel extends Model {

  @Column(nullable = false, updatable = false)
  private String providerName;

  /**
   * Empty constructor for hibernate.
   */
  protected ApiModel() {
  }

  public ApiModel(String providerName) {
    checkNotNull(providerName);
    checkArgument(!providerName.isEmpty());
    this.providerName = providerName;
  }

  public String getProviderName() {
    return providerName;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("providerName", providerName);
  }
}
