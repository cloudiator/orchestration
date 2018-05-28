package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.Node;
import javax.annotation.Nullable;

class NodeConverter implements OneWayConverter<NodeModel, Node> {

  @Nullable
  @Override
  public Node apply(@Nullable NodeModel nodeModel) {
    if (nodeModel == null) {
      return null;
    }

    throw new UnsupportedOperationException();

  }
}
