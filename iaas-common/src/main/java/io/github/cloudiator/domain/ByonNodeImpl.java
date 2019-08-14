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

package io.github.cloudiator.domain;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

public class ByonNodeImpl extends AbstractNodeImpl implements ByonNode {

  private volatile boolean allocated;

  ByonNodeImpl(NodeProperties nodeProperties, String userId,
      @Nullable LoginCredential loginCredential,
      Set<IpAddress> ipAddresses, String id,
      String name, @Nullable String diagnostic,
      @Nullable String reason,
      @Nullable String nodeCandidate,
      boolean allocated) {

    super(nodeProperties, userId, loginCredential, NodeType.BYON, ipAddresses, id, name,
        diagnostic, reason, nodeCandidate);
    this.allocated = allocated;
  }

  @Override
  synchronized public boolean allocated() {
    return allocated;
  }

  @Override
  synchronized public void setAllocated(boolean allocated) {
    this.allocated = allocated;
  }

  @Override
  synchronized public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ByonNode that = (ByonNode) o;
    return super.equals(o) &&
        Objects.equals(allocated, that.allocated());
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper().add("allocated", allocated);
  }
}
