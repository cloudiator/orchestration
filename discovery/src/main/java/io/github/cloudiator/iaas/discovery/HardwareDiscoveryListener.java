package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import io.github.cloudiator.iaas.common.persistance.domain.HardwareDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 02.06.17.
 */
public class HardwareDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final HardwareDomainRepository hardwareDomainRepository;

  @Inject
  public HardwareDiscoveryListener(
      HardwareDomainRepository hardwareDomainRepository) {
    this.hardwareDomainRepository = hardwareDomainRepository;
  }

  @Override
  public Class<?> interestedIn() {
    return HardwareFlavor.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {
    HardwareFlavor hardwareFlavor = (HardwareFlavor) o;

    hardwareDomainRepository.save(hardwareFlavor);
  }
}
