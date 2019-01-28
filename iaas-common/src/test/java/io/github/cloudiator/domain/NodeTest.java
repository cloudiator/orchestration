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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.domain.OperatingSystems;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.IpAddresses;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;

public class NodeTest {

  @Test
  public void connectToPreferPublic() {

    final NodeProperties nodeProperties = NodePropertiesBuilder.newBuilder().disk(50d)
        .geoLocation(GeoLocationBuilder.newBuilder().city("ulm").country("de").build())
        .memory(1024L).numberOfCores(4).os(
            OperatingSystems.unknown()).build();

    final IpAddress publicIp = IpAddresses.of("8.8.8.8");
    final IpAddress privateIp = IpAddresses.of("192.168.1.3");

    Set<IpAddress> ipAddresses = Sets
        .newHashSet(publicIp, privateIp);

    final Node node = NodeBuilder.newBuilder().id(UUID.randomUUID().toString())
        .state(NodeState.RUNNING).userId("userId")
        .nodeType(NodeType.VM).nodeProperties(nodeProperties)
        .ipAddresses(ipAddresses).build();

    assertThat(node.publicIpAddresses(), contains(publicIp));
    assertThat(node.privateIpAddresses(), contains(privateIp));

    assertThat(node.connectTo(), equalTo(publicIp));

  }
}
