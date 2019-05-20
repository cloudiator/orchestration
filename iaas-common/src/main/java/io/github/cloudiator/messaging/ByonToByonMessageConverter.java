package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.Node;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.NodeEntities;

/** Class basically wrapps NodeToNodeMessageConverter.
 * Uses its functionality while mapping between ByonNode and Node
 * @author Florian Held
 */
public class ByonToByonMessageConverter implements TwoWayConverter<ByonNode, Byon.ByonNode> {
  public static final ByonToByonMessageConverter INSTANCE = new ByonToByonMessageConverter();
  private static final NodeMessageToByonNodeMessage NODE_MESSAGE_TO_BYON_NODE_MESSAGE = new NodeMessageToByonNodeMessage();
  private static final ByonNodeToNodeConverter BYONNODE_TO_NODE_CONVERTER = new ByonNodeToNodeConverter();

  private ByonToByonMessageConverter() {
  }

  @Override
  public ByonNode applyBack(Byon.ByonNode byonNode) {
    NodeEntities.Node entityNode = NODE_MESSAGE_TO_BYON_NODE_MESSAGE.applyBack(byonNode);
    Node node = NodeToNodeMessageConverter.INSTANCE.applyBack(entityNode);
    return BYONNODE_TO_NODE_CONVERTER.applyBack(node);
  }

  @Override
  public Byon.ByonNode apply(ByonNode byonNode) {
    Node node = BYONNODE_TO_NODE_CONVERTER.apply(byonNode);
    NodeEntities.Node entityNode = NodeToNodeMessageConverter.INSTANCE.apply(node);
    return NODE_MESSAGE_TO_BYON_NODE_MESSAGE.apply(entityNode);
  }
}
