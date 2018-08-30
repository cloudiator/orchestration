package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.NodeCandidate;
import io.github.cloudiator.domain.NodeCandidateBuilder;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class NodeCandidateConverter implements
    OneWayConverter<MatchmakingEntities.NodeCandidate, NodeCandidate> {

  public static final NodeCandidateConverter INSTANCE = new NodeCandidateConverter();

  private static final ImageMessageToImageConverter IMAGE_CONVERTER = ImageMessageToImageConverter.INSTANCE;
  private static final HardwareMessageToHardwareConverter HARDWARE_CONVERTER = HardwareMessageToHardwareConverter.INSTANCE;
  private static final LocationMessageToLocationConverter LOCATION_CONVERTER = LocationMessageToLocationConverter.INSTANCE;
  private static final CloudMessageToCloudConverter CLOUD_CONVERTER = CloudMessageToCloudConverter.INSTANCE;

  private NodeCandidateConverter() {
  }

  @Override
  public NodeCandidate apply(MatchmakingEntities.NodeCandidate nodeCandidate) {

    return NodeCandidateBuilder.create()
        .cloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .image(IMAGE_CONVERTER.apply(nodeCandidate.getImage()))
        .location(LOCATION_CONVERTER.apply(nodeCandidate.getLocation()))
        .hardware(HARDWARE_CONVERTER.apply(nodeCandidate.getHardwareFlavor()))
        .price(nodeCandidate.getPrice())
        .build();
  }
}
