package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.domain.Identifiable;
import java.util.List;

public interface NodeGroup extends Identifiable {

  List<Node> getNodes();

}
