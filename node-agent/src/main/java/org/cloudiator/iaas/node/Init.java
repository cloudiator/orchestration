package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
public class Init {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(Init.class);

  private final PersistService persistService;

  @Inject
  Init(PersistService persistService) {
    LOGGER.info("Initializing");
    this.persistService = persistService;
    run();
  }

  private void run() {
    LOGGER.info("Starting persistence service.");
    startPersistService();
  }

  private void startPersistService() {
    persistService.start();
  }


}
