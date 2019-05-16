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

import static com.google.common.base.Preconditions.checkNotNull;
import de.uniulm.omi.cloudiator.sword.domain.VirtualMachine;

public class BaseNodeBuilder extends AbstractNodeBuilder<BaseNodeBuilder> {

  private BaseNodeBuilder() {
    super();
  }

  private BaseNodeBuilder(Node node) {
    super(node);
  }

  private BaseNodeBuilder(VirtualMachine virtualMachine) {
    super(virtualMachine);
  }

  public static BaseNodeBuilder of(Node node) {
    checkNotNull(node, "node is null");
    return new BaseNodeBuilder(node);
  }

  public static BaseNodeBuilder of(VirtualMachine virtualMachine) {
    return new BaseNodeBuilder(virtualMachine);
  }

  @Override
  protected BaseNodeBuilder self() {
    return this;
  }

  @Override
  public BaseNode build() {
    return new BaseNodeImpl(nodeProperties, loginCredential, nodeType, ipAddresses, name,
        diagnostic, reason, nodeCandidate, originId);
  }
}
