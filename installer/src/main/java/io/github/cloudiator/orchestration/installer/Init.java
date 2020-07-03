package io.github.cloudiator.orchestration.installer;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;

public class Init {

    private final PersistService persistService;

    @Inject
    public Init(PersistService persistService) {
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
