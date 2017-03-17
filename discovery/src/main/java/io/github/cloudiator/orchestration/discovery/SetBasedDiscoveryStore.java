package io.github.cloudiator.orchestration.discovery;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.sword.domain.Resource;

import java.util.Set;

/**
 * Created by daniel on 25.01.17.
 */
public class SetBasedDiscoveryStore implements DiscoveryStore {

    private final Set<Resource> store = Sets.newConcurrentHashSet();

    @Override public boolean store(Resource resource) {
        return store.add(resource);
    }
}
