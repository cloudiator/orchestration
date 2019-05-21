package io.github.cloudiator.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeBuilder;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeState;

public class NodeMessageToByonNodeMessage  implements TwoWayConverter<NodeEntities.Node, Byon.ByonNode> {
  private static final IpAddressMessageToIpAddress IP_ADDRESS_CONVERTER = new IpAddressMessageToIpAddress();
  private static final NodeTypeToNodeTypeMessage NODE_TYPE_CONVERTER = new NodeTypeToNodeTypeMessage();
  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;

  public static final NodeStateConverter NODE_STATE_CONVERTER = new NodeStateConverter();

  @Override
  public NodeEntities.Node applyBack(Byon.ByonNode byonNode) {
    final ByonData data = byonNode.getNodeData();
    NodeState state = (data.getAllocated() == true) ? NodeState.NODE_STATE_RUNNING
        : NodeState.UNRECOGNIZED;
    final NodeBuilder builder = NodeBuilder.newBuilder().id(byonNode.getId())
        //change hier, Feld fehlt
        .name("<unknown>")
        .nodeProperties(NODE_PROPERTIES_CONVERTER.apply(data.getProperties()))
        .nodeType(NODE_TYPE_CONVERTER.applyBack(NodeEntities.NodeType.BYON)).ipAddresses(
            data.getIpAddressList().stream().map(IP_ADDRESS_CONVERTER)
                .collect(Collectors.toSet()))
        .state(NODE_STATE_CONVERTER.applyBack(state));

    if (data.hasLoginCredentials()) {
      builder.loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(data.getLoginCredentials()));
    }

    /* change hier
    if (!Strings.isNullOrEmpty(data.getReason())) {
      builder.reason(data.getReason());
    }

    if (!Strings.isNullOrEmpty(data.getDiagnostic())) {
      builder.diagnostic(data.getDiagnostic());
    }

    if (!Strings.isNullOrEmpty(data.getNodeCandidate())) {
      builder.dataCandidate(data.getNodeCandidate());
    }
    */

    final Node node = builder.build();

    return NodeToNodeMessageConverter.INSTANCE.apply(node);
  }

  @Override
  public Byon.ByonNode apply(NodeEntities.Node node) {
    boolean allocated = (node.getState() == NodeState.NODE_STATE_RUNNING) ?
        true : false;
    final ByonNodeBuilder builder = ByonNodeBuilder.newBuilder()
        .id(node.getId())
        .name(node.getName())
        .allocated(allocated)
        .nodeProperties(NODE_PROPERTIES_CONVERTER.apply(node.getNodeProperties()))
        .ipAddresses(node.getIpAddressesList().stream().map(IP_ADDRESS_CONVERTER)
          .collect(Collectors.toSet()))
        .loginCredential(LOGIN_CREDENTIAL_CONVERTER.apply(node.getLoginCredential()))
        // change hier: Inkonsistenz?
        .nodeType(NODE_TYPE_CONVERTER.applyBack(node.getNodeType()));

    if (!Strings.isNullOrEmpty(node.getReason())) {
      builder.reason(node.getReason());
    }

    if (!Strings.isNullOrEmpty(node.getDiagnostic())) {
      builder.diagnostic(node.getDiagnostic());
    }

    if (!Strings.isNullOrEmpty(node.getNodeCandidate())) {
      builder.nodeCandidate(node.getNodeCandidate());
    }

    final ByonNode byonNode = builder.build();

    return ByonToByonMessageConverter.INSTANCE.apply(byonNode);
  }
}
