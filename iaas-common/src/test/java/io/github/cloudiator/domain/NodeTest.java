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

    final Node node = NodeBuilder.newBuilder().nodeType(NodeType.VM).nodeProperties(nodeProperties)
        .ipAddresses(ipAddresses).build();

    assertThat(node.publicIpAddresses(), contains(publicIp));
    assertThat(node.privateIpAddresses(), contains(privateIp));

    assertThat(node.connectTo(), equalTo(publicIp));

  }
}
