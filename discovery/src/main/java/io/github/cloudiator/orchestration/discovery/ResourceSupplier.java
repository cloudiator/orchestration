package io.github.cloudiator.orchestration.discovery;

import com.google.common.base.Supplier;
import de.uniulm.omi.cloudiator.sword.domain.Resource;

/**
 * Created by daniel on 25.01.17.
 */
public interface ResourceSupplier extends Supplier<Iterable<? extends Resource>> {
}
