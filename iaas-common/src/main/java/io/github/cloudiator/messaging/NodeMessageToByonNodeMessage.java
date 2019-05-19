package io.github.cloudiator.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.BaseNode;
import io.github.cloudiator.domain.BaseNodeBuilder;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonData;
import org.cloudiator.messages.Byon.ByonNode;
import org.cloudiator.messages.NodeEntities;
import org.cloudiator.messages.NodeEntities.NodeState;

public class NodeMessageToByonNodeMessage  implements TwoWayConverter<NodeEntities.Node, ByonNode> {
  private static final IpAddressMessageToIpAddress IP_ADDRESS_CONVERTER = new IpAddressMessageToIpAddress();
  private static final NodeTypeToNodeTypeMessage NODE_TYPE_CONVERTER = new NodeTypeToNodeTypeMessage();
  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  private static final LoginCredentialMessageToLoginCredentialConverter LOGIN_CREDENTIAL_CONVERTER = LoginCredentialMessageToLoginCredentialConverter.INSTANCE;

  public static final NodeStateConverter NODE_STATE_CONVERTER = new NodeStateConverter();

  @Override
  public NodeEntities.Node applyBack(ByonNode byonNode) {
    final ByonData data = byonNode.getNodeData();
    //change hier, id-Feld aus ByonNode kicken
    final NodeBuilder builder = NodeBuilder.newBuilder().id(byonNode.getId())
        //change hier, Feld fehlt
        .name("<unknown>")
        .nodeProperties(NODE_PROPERTIES_CONVERTER.apply(data.getProperties()))
        .nodeType(NODE_TYPE_CONVERTER.applyBack(NodeEntities.NodeType.BYON)).ipAddresses(
            data.getIpAddressList().stream().map(IP_ADDRESS_CONVERTER)
                .collect(Collectors.toSet()))
        .state(NODE_STATE_CONVERTER.applyBack(NodeState.UNRECOGNIZED));

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

    if (!Strings.isNullOrEmpty(node.getOriginId())) {
      nodeBuilder.originId(node.getOriginId());
    }
    */

    final Node node = builder.build();

    return NodeToNodeMessageConverter.INSTANCE.apply(node);
  }

  @Override
  public ByonNode apply(NodeEntities.Node node) {
    final BaseNodeBuilder builder = BaseNodeBuilder.newBuilder().name(node.getName())
        .nodeProperties(NODE_PROPERTIES_CONVERTER.apply(node.getNodeProperties()))
        //change hier, inkonsistent
        .nodeType(NODE_TYPE_CONVERTER.applyBack(node.getNodeType()))
        .ipAddresses(node.getIpAddressesList().stream().map(IP_ADDRESS_CONVERTER)
        .collect(Collectors.toSet()));

    if (!Strings.isNullOrEmpty(node.getReason())) {
      builder.reason(node.getReason());
    }

    if (!Strings.isNullOrEmpty(node.getDiagnostic())) {
      builder.diagnostic(node.getDiagnostic());
    }

    if (!Strings.isNullOrEmpty(node.getNodeCandidate())) {
      builder.nodeCandidate(node.getNodeCandidate());
    }

    if (!Strings.isNullOrEmpty(node.getOriginId())) {
      builder.id(node.getId());
    }

    final BaseNode baseNode = builder.build();

    return BaseNodeToByonNode.INSTANCE.apply(baseNode);
  }

  private static class BaseNodeToByonNode implements
      OneWayConverter<BaseNode, ByonNode> {
    private static final BaseNodeToByonNode INSTANCE = new BaseNodeToByonNode();

    private BaseNodeToByonNode() {
    }

    @Nullable
    @Override
    public ByonNode apply(@Nullable BaseNode baseNode) {
      /* change hier setze Name
       */
      ByonData.Builder builder = ByonData.newBuilder()
          .addAllIpAddress(
              baseNode.ipAddresses().stream().map(IP_ADDRESS_CONVERTER::applyBack)
                  .collect(Collectors.toList()))
          .setProperties(NODE_PROPERTIES_CONVERTER.applyBack(baseNode.nodeProperties()));

      /* change hier
        builder.reason(baseNode.reason().orElse(null));
        builder.diagnostic(baseNode.diagnostic().orElse(null));
        builder.nodeCandidate(baseNode.nodeCandidate().orElse(null));
        builder.originId(baseNode.originId().orElse(null));
      */

      ByonData data = builder.build();
      /* change hier kicke id
       */
      ByonNode byonNode = ByonNode.newBuilder().setId("<unknown>")
          .setNodeData(data).build();

      return byonNode;
    }
  }
}
