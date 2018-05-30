package org.cloudiator.iaas.node.config;

import com.google.inject.AbstractModule;
import org.cloudiator.iaas.node.Init;


/**
 * Created by daniel on 31.05.17.
 */
public class NodeModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(Init.class).asEagerSingleton();
  }
}
