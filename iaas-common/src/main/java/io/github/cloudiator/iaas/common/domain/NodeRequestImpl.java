package io.github.cloudiator.iaas.common.domain;

import com.google.common.collect.ImmutableList;
import de.uniulm.omi.cloudiator.domain.Requirement;
import java.util.List;

public class NodeRequestImpl implements NodeRequest {

  private final List<Requirement> requirements;

  public NodeRequestImpl(List<Requirement> requirements) {
    this.requirements = ImmutableList.copyOf(requirements);
  }

  @Override
  public List<Requirement> requirements() {
    return requirements;
  }
}
