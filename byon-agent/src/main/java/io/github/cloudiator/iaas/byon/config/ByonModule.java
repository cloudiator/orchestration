package io.github.cloudiator.iaas.byon.config;

import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonModule  extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonModule.class);

  @Override
  protected void configure() {
    LOGGER.error("set bindings");
  }
}
