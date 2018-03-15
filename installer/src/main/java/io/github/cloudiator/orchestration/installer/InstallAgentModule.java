package io.github.cloudiator.orchestration.installer;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Created by Daniel Seybold on 15.03.2018.
 */
public class InstallAgentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(InstallEventSubscriber.class).in(Singleton.class);
  }
}
