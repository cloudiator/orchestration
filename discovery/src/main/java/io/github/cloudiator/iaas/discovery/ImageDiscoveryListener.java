package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import io.github.cloudiator.iaas.common.persistance.domain.ImageDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 01.06.17.
 */
public class ImageDiscoveryListener implements DiscoveryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageDiscoveryListener.class);
  private final ImageDomainRepository imageDomainRepository;

  @Inject
  public ImageDiscoveryListener(
      ImageDomainRepository imageDomainRepository) {
    this.imageDomainRepository = imageDomainRepository;
  }

  @Override
  public Class<?> interestedIn() {
    return Image.class;
  }

  @Override
  @Transactional
  public void handle(Object o) {
    Image image = (Image) o;

    imageDomainRepository.save(image);
  }
}
