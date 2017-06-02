package io.github.cloudiator.iaas.discovery;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.util.execution.SimpleBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by daniel on 25.01.17.
 */
public class DiscoveryQueue implements SimpleBlockingQueue<Discovery> {

  private final BlockingQueue<Discovery> blockingQueue;

  public DiscoveryQueue() {
    this.blockingQueue = new LinkedBlockingQueue<>();
  }

  @Override
  public void add(Discovery discovery) {
    this.blockingQueue.add(discovery);
  }

  @Override
  public Discovery take() throws InterruptedException {
    return this.blockingQueue.take();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
