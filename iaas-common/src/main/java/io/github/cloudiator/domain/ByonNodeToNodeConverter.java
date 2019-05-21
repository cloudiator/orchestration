package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import java.util.Set;

public class ByonNodeToNodeConverter implements TwoWayConverter<ByonNode, Node> {
  public static final ByonNodeToNodeConverter INSTANCE = new ByonNodeToNodeConverter();

  private ByonNodeToNodeConverter(){
  }

  @Override
  public ByonNode applyBack(Node node) {
    CommonFieldsWrapper wrapper = new CommonFieldsWrapper(node);
    boolean allocated = node.state().equals(NodeState.RUNNING);

    ByonNodeBuilder builder = ByonNodeBuilder.newBuilder();
    ByonNode byonNode =
        builder
            .nodeProperties(wrapper.nodeProperties)
            .loginCredential(wrapper.loginCredential)
            .nodeType(wrapper.type)
            .ipAddresses(wrapper.ipAddresses)
            .name(wrapper.name)
            .diagnostic(wrapper.diagnostic)
            .reason(wrapper.reason)
            .nodeCandidate(wrapper.nodeCandidate)
            .id(wrapper.id)
            .allocated(allocated)
            .build();

    return byonNode;
  }

  @Override
  public Node apply(ByonNode byonNode) {
    CommonFieldsWrapper wrapper = new CommonFieldsWrapper(byonNode);
    boolean allocated = byonNode.allocated();
    NodeState state = allocated ? NodeState.RUNNING : NodeState.DELETED;

    NodeBuilder builder = NodeBuilder.newBuilder();
    Node node =
        builder
            .nodeProperties(wrapper.nodeProperties)
            .loginCredential(wrapper.loginCredential)
            .nodeType(wrapper.type)
            .ipAddresses(wrapper.ipAddresses)
            .name(wrapper.name)
            .diagnostic(wrapper.diagnostic)
            .reason(wrapper.reason)
            .nodeCandidate(wrapper.nodeCandidate)
            .id(wrapper.id)
            .state(state)
            .build();

    return node;
  }

  private static class CommonFieldsWrapper {
    private final NodeProperties nodeProperties;
    private final LoginCredential loginCredential;
    private final NodeType type;
    private final Set<IpAddress> ipAddresses;
    private final String name;
    private final String diagnostic;
    private final String reason;
    private final String nodeCandidate;

    private final  String id;
    private CommonFieldsWrapper(AbstractNode aNode) {
      nodeProperties = aNode.nodeProperties();
      loginCredential = aNode.loginCredential().orElse(null);
      type = aNode.type();
      ipAddresses = aNode.ipAddresses();
      name = aNode.name();
      diagnostic = aNode.diagnostic().orElse(null);
      reason = aNode.reason().orElse(null);
      nodeCandidate = aNode.nodeCandidate().orElse(null);
      id = aNode.id();
    }
  }
}
