/*
 * Copyright (c) 2014-2019 University of Ulm
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

package org.cloudiator.iaas.node.config;

import static org.cloudiator.iaas.node.config.Constants.NODE_PARALLEL_STARTS;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;

public class NodeAgentContext {

  private final Config config;

  public NodeAgentContext() {
    this(Configuration.conf());
  }

  public NodeAgentContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "node");
  }

  public int parallelNodes() {
    return config.getInt(NODE_PARALLEL_STARTS);
  }

}
