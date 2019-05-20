package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import de.uniulm.omi.cloudiator.sword.domain.LoginCredential;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.AbstractNode;
import io.github.cloudiator.domain.AbstractNodeBuilder;
import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.domain.ByonNodeBuilder;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.domain.NodeBuilder;
import io.github.cloudiator.domain.NodeProperties;
import io.github.cloudiator.domain.NodeType;
import java.util.Set;

public class ByonNodeToNodeConverter implements TwoWayConverter<ByonNode, Node> {

  @Override
  public ByonNode applyBack(Node node) {
    CommonFieldsWrapper wrapper = new CommonFieldsWrapper(node);
    boolean allocated = false;

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
