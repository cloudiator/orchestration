package io.github.cloudiator.iaas.discovery;

import com.google.common.base.MoreObjects;

/**
 * Created by daniel on 01.06.17.
 */
public final class Discovery {

  private final Object discovery;

  public Discovery(Object discovery) {
    this.discovery = discovery;
  }

  public Class<?> getType() {
    return discovery.getClass();
  }

  public Object discovery() {
    return discovery;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("discovery", discovery).toString();
  }
}
