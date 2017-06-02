package io.github.cloudiator.iaas.discovery;

/**
 * Created by daniel on 01.06.17.
 */
public class Discovery {

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

}
