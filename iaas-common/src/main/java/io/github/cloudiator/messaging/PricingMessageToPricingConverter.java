package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.PricingDimensions;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.DiscoveredPricing;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Price.Builder;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class PricingMessageToPricingConverter implements OneWayConverter<DiscoveredPricing, IaasEntities.Price> {
    public static final PricingMessageToPricingConverter INSTANCE = new PricingMessageToPricingConverter();

    private PricingMessageToPricingConverter() {
    }

    @Nullable
    @Override
    public IaasEntities.Price apply(@Nullable DiscoveredPricing discoveredPricing) {
        if(discoveredPricing != null) {
            return IaasEntities.Price.newBuilder()
                    .setCloudAPIProviderName(discoveredPricing.getApi().providerName())
                    .setCurrency(discoveredPricing.getCurrency())
                    .setHardwareProviderId(discoveredPricing.getInstanceName())
                    .setId(discoveredPricing.id())
                    .setLocationProviderId(discoveredPricing.getLocationProviderId())
                    .setOsArchitecture(discoveredPricing.getOperatingSystem().operatingSystemArchitecture().name())
                    .setOsFamily(discoveredPricing.getOperatingSystem().operatingSystemFamily().name())
                    .setPrice(discoveredPricing.getPricingOnDemandTerms()
                            .stream()
                            .findFirst()
                            .flatMap(pricingTerms ->
                                    pricingTerms.getPricingDimensions()
                                            .stream()
                                            .findFirst()
                                            .map(PricingDimensions::getPricePerUnit)
                            )
                            .map(BigDecimal::doubleValue)
                            .orElse(-1d)
                    )
                    .build();
        }
        return null;
    }
}
