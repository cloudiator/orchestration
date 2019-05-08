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

package io.github.cloudiator.iaas.vm;

import static io.github.cloudiator.iaas.vm.Constants.VM_CLEANUP_ENABLED;
import static io.github.cloudiator.iaas.vm.Constants.VM_PARALLEL_STARTS;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;

public class VmAgentContext {

  private final Config config;

  public VmAgentContext() {
    this(Configuration.conf());
  }

  public VmAgentContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "vm");
  }

  public int parallelVMStarts() {
    return config.getInt(VM_PARALLEL_STARTS);
  }

  public boolean cleanupEnabled() {
    return config.getBoolean(VM_CLEANUP_ENABLED);
  }

}
