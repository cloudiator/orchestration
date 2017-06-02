package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import de.uniulm.omi.cloudiator.util.execution.ExecutionService;
import java.util.Set;

/**
 * Created by daniel on 31.05.17.
 */
public class Init {

  @Inject
  Init(PersistService persistService, ExecutionService executionService,
      Set<AbstractDiscoveryWorker> discoveryWorkerSet, DiscoveryListenerWorker discoveryListenerWorker) {

    persistService.start();
    discoveryWorkerSet.forEach(
        executionService::schedule);
    executionService.execute(discoveryListenerWorker);
  }

}
