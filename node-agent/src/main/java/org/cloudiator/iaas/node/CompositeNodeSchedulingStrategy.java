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

package org.cloudiator.iaas.node;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeCandidate;
import java.util.Set;

public class CompositeNodeSchedulingStrategy implements NodeSchedulingStrategy {


  @Override
  public boolean canSchedule(Node pending) {
    return false;
  }

  @Override
  public Node schedule(Node pending) throws NodeSchedulingException {
    return null;
  }
}
