package org.cloudiator.iaas.node;

import javax.annotation.Nullable;

public class NameGenerator {

  public static final NameGenerator INSTANCE = new NameGenerator();

  private NameGenerator() {

  }

  public String generate(@Nullable String groupName) {

    if (groupName == null || groupName.isEmpty()) {
      return "node";
    }

    return groupName;
  }

}
