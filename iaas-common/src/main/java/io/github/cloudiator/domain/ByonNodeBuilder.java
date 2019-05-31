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

public class ByonNodeBuilder extends AbstractNodeBuilder<ByonNodeBuilder> {
  private boolean allocated;

  private ByonNodeBuilder() {
    super();
  }

  private ByonNodeBuilder(ByonNode node) {
    super(node);
    allocated = node.allocated();
  }

  private ByonNodeBuilder(VirtualMachine virtualMachine) {
    super(virtualMachine, true);
  }

  public static ByonNodeBuilder newBuilder() {
    return new ByonNodeBuilder();
  }

  public static ByonNodeBuilder of(ByonNode node) {
    checkNotNull(node, "node is null");
    return new ByonNodeBuilder(node);
  }

  public static ByonNodeBuilder of(VirtualMachine virtualMachine) {
    return new ByonNodeBuilder(virtualMachine);
  }

  public ByonNodeBuilder allocated(boolean allocated) {
    this.allocated = allocated;
    return this;
  }

  @Override
  protected ByonNodeBuilder self() {
    return this;
  }

  @Override
  public ByonNodeBuilder nodeType(NodeType nodeType) {
    throw new IllegalArgumentException("No type is set constant to BYON");
  }

  @Override
  public ByonNode build() {
    return new ByonNodeImpl(nodeProperties, userId, loginCredential, ipAddresses, id,
        name, diagnostic, reason, nodeCandidate, allocated);
  }
}
