package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.domain.LocationScope;
import de.uniulm.omi.cloudiator.sword.domain.HardwareFlavor;
import de.uniulm.omi.cloudiator.sword.domain.Image;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.Resource;
import org.cloudiator.messages.Common;
import org.cloudiator.messages.Discovery;
import org.cloudiator.messages.Iaas;

import java.util.function.Function;

/**
 * Created by daniel on 03.03.17.
 */
public class ResourceToDiscoveryEvent implements Function<Resource, Discovery.DiscoveryEvent> {

    private final ImageToImage imageConverter = new ImageToImage();
    private final LocationToLocation locationConverter = new LocationToLocation();
    private final HardwareToHardware hardwareConverter = new HardwareToHardware();

    @Override public Discovery.DiscoveryEvent apply(Resource resource) {
        if (resource instanceof Image) {
            return Discovery.DiscoveryEvent.newBuilder()
                .setImage(imageConverter.apply((Image) resource)).build();
        } else if (resource instanceof Location) {
            return Discovery.DiscoveryEvent.newBuilder()
                .setLocation(locationConverter.apply((Location) resource)).build();
        } else if (resource instanceof HardwareFlavor) {
            return Discovery.DiscoveryEvent.newBuilder()
                .setHardware(hardwareConverter.apply((HardwareFlavor) resource)).build();
        } else {
            throw new IllegalArgumentException("Unsupported resource " + resource);
        }
    }

    private final static class ImageToImage implements Function<Image, Iaas.Image> {

        private final LocationToLocation locationConverter = new LocationToLocation();

        @Override public Iaas.Image apply(Image image) {
            final Iaas.Image.Builder builder =
                Iaas.Image.newBuilder().setId(image.id()).setName(image.name())
                    .setProviderId(image.providerId());
            if (image.location().isPresent()) {
                builder.setLocation(locationConverter.apply(image.location().get()));
            }
            return builder.build();
        }
    }


    private final static class HardwareToHardware
        implements Function<HardwareFlavor, Iaas.HardwareFlavor> {

        private final LocationToLocation locationConverter = new LocationToLocation();

        @Override public Iaas.HardwareFlavor apply(HardwareFlavor hardwareFlavor) {
            final Iaas.HardwareFlavor.Builder builder =
                Iaas.HardwareFlavor.newBuilder().setId(hardwareFlavor.id())
                    .setProviderId(hardwareFlavor.providerId()).setName(hardwareFlavor.name())
                    .setCores(hardwareFlavor.numberOfCores()).setRam(hardwareFlavor.mbRam());
            if (hardwareFlavor.location().isPresent()) {
                builder.setLocation(locationConverter.apply(hardwareFlavor.location().get()));
            }
            if (hardwareFlavor.gbDisk().isPresent()) {
                builder.setDisk(hardwareFlavor.gbDisk().get());
            }
            return builder.build();
        }
    }


    private final static class LocationToLocation implements Function<Location, Iaas.Location> {

        private final LocationScopeToLocationScope locationScopeConverter =
            new LocationScopeToLocationScope();

        @Override public Iaas.Location apply(Location location) {
            final Iaas.Location.Builder builder =
                Iaas.Location.newBuilder().setId(location.id()).setProviderId(location.providerId())
                    .setName(location.name())
                    .setLocationScope(locationScopeConverter.apply(location.locationScope()));
            if (location.parent().isPresent()) {
                builder.setParent(this.apply(location.parent().get()));
            }
            return builder.build();
        }
    }


    private final static class LocationScopeToLocationScope
        implements Function<LocationScope, Common.LocationScope> {

        @Override public Common.LocationScope apply(LocationScope locationScope) {
            switch (locationScope) {
                case HOST:
                    return Common.LocationScope.HOST;
                case PROVIDER:
                    return Common.LocationScope.PROVIDER;
                case REGION:
                    return Common.LocationScope.REGION;
                case ZONE:
                    return Common.LocationScope.ZONE;
                default:
                    throw new AssertionError(
                        String.format("Unknown location scope %s supplied.", locationScope));
            }
        }
    }

}
