package io.github.cloudiator.iaas.byon.util;

import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeBuilder;
import io.github.cloudiator.domain.NodeType;
import io.github.cloudiator.messaging.IpAddressMessageToIpAddress;
import io.github.cloudiator.messaging.LoginCredentialMessageToLoginCredentialConverter;
import io.github.cloudiator.messaging.NodePropertiesMessageToNodePropertiesConverter;
import java.util.stream.Collectors;
import org.cloudiator.messages.Byon.ByonData;

public class DomainOperations {
  private static final NodePropertiesMessageToNodePropertiesConverter PROP_CONVERTER =
      new NodePropertiesMessageToNodePropertiesConverter();
  private static final IpAddressMessageToIpAddress IP_ADDR_CONVERTER =
      new IpAddressMessageToIpAddress();
  private static final LoginCredentialMessageToLoginCredentialConverter
      LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;

  // do not instantiate
  private DomainOperations() {
  }

  public static ByonNode transformToDomain(ByonData byonRequest, boolean allocated) {
    final String nodeId = IdCreator.createId(byonRequest);
    ByonNodeBuilder builder = ByonNodeBuilder.newBuilder().id(nodeId)
        .allocated(allocated)
        .nodeProperties(PROP_CONVERTER.apply(byonRequest.getProperties()))
        //change hier
        //.nodeCandidate()
        //.diagnostic()
        //.name()
        //.reason()
        .nodeType(io.github.cloudiator.domain.NodeType.BYON)
        .ipAddresses(byonRequest.getIpAddressList().stream().map(IP_ADDR_CONVERTER)
            .collect(Collectors.toSet()));

    if (byonRequest.hasLoginCredentials()) {
      builder.loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(byonRequest.getLoginCredentials()));
    }

    final ByonNode node = builder.build();
    return node;
  }
}
