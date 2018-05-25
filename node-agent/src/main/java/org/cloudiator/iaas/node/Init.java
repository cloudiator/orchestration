package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;

/**
 * Created by daniel on 31.05.17.
 */
public class Init {

  private final PersistService persistService;

  @Inject
  Init(PersistService persistService) {
    this.persistService = persistService;
    run();
  }

  private void run() {
    startPersistService();
  }

  private void startPersistService() {
    persistService.start();
  }


}
