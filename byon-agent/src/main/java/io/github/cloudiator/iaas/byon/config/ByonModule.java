package io.github.cloudiator.iaas.byon.config;

import com.google.inject.AbstractModule;
import io.github.cloudiator.iaas.byon.messaging.AddByonNodeSubscriber;
import io.github.cloudiator.iaas.byon.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonModule  extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonModule.class);

  @Override
  protected void configure() {
    bind(Init.class).asEagerSingleton();
  }
}
