package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.annotations.Base;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.BaseNode;
import io.github.cloudiator.domain.BaseNodeBuilder;
import io.github.cloudiator.domain.BaseNodeImpl;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodeType;
import java.util.Optional;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import java.util.Set;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.Byon.ByonNode;
import org.cloudiator.messages.NodeEntities;

/** Class basically wrapps NodeToNodeMessageConverter.
 * Uses its functionality while mapping between BaseNode and Node
 * @author Florian Held
 */
public class BaseNodetoByonNodeMessageConverter implements TwoWayConverter<BaseNode, Byon.ByonNode> {
  public static final BaseNodetoByonNodeMessageConverter INSTANCE = new BaseNodetoByonNodeMessageConverter ();
  private static final NodeMessageToByonNodeMessage NODE_MESSAGE_TO_BYON_NODE_MESSAGE = new NodeMessageToByonNodeMessage();
  private static final BaseNodeToNodeConverter BASENODE_TO_NODE_CONVERTER = new BaseNodeToNodeConverter();

  private BaseNodetoByonNodeMessageConverter () {
  }

  @Override
  public BaseNode applyBack(ByonNode byonNode) {
    NodeEntities.Node entityNode = NODE_MESSAGE_TO_BYON_NODE_MESSAGE.applyBack(byonNode);
    Node node = NodeToNodeMessageConverter.INSTANCE.applyBack(entityNode);
    return BASENODE_TO_NODE_CONVERTER.applyBack(node);
  }

  @Override
  public ByonNode apply(BaseNode baseNode) {
    Node node = BASENODE_TO_NODE_CONVERTER.apply(baseNode);
    NodeEntities.Node entityNode = NodeToNodeMessageConverter.INSTANCE.apply(node);
    return NODE_MESSAGE_TO_BYON_NODE_MESSAGE.apply(entityNode);
  }

  private static class BaseNodeToNodeConverter implements
      TwoWayConverter<BaseNode, Node> {

    @Override
    public BaseNode applyBack(Node node) {
      BaseNodeBuilder builder = BaseNodeBuilder.of(node);
      return builder.build();
    }

    @Override
    public Node apply(BaseNode baseNode) {
      NodeProperties nodeProperties = baseNode.nodeProperties();
      LoginCredential loginCredential = baseNode.loginCredential().orElse(null);
      NodeType type = baseNode.type();
      Set<IpAddress> ipAddresses = baseNode.ipAddresses();
      String name = baseNode.name();
      String diagnostic = baseNode.diagnostic().orElse(null);
      String reason = baseNode.reason().orElse(null);
      String nodeCandidate = baseNode.nodeCandidate().orElse(null);
      String originId = baseNode.originId().orElse(null);

      NodeBuilder builder = NodeBuilder.newBuilder();
      Node node = builder.nodeProperties(nodeProperties)
          .loginCredential(loginCredential)
          .nodeType(type)
          .ipAddresses(ipAddresses)
          .name(name)
          .diagnostic(diagnostic)
          .reason(reason)
          .nodeCandidate(nodeCandidate)
          .originId(originId).build();

      return node;
    }
  }
}
