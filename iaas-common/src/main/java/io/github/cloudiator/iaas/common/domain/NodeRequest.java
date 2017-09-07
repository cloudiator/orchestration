package io.github.cloudiator.iaas.common.domain;

import de.uniulm.omi.cloudiator.domain.Requirement;
import java.util.List;

public interface NodeRequest {

  List<Requirement> requirements();

}
