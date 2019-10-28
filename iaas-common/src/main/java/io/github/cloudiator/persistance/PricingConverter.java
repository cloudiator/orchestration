package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.domain.OperatingSystemBuilder;
import de.uniulm.omi.cloudiator.sword.domain.ApiBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PricingBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PricingDimensions;
import de.uniulm.omi.cloudiator.sword.domain.PricingTerms;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.domain.DiscoveredPricing;
import io.github.cloudiator.messaging.OperatingSystemConverter;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class PricingConverter implements OneWayConverter<PricingModel, DiscoveredPricing> {
    @Nullable
    @Override
    public DiscoveredPricing apply(@Nullable PricingModel pricingModel) {
        if(pricingModel == null) {
            return null;
        }

        return new DiscoveredPricing(PricingBuilder.newBuilder()
        .name(pricingModel.getName())
        .providerId(pricingModel.getProviderId())
        .id(pricingModel.getCloudUniqueId())
        .location(null)
        .locationProviderId(pricingModel.getLocationProviderId())
        .cloudServiceProviderName(pricingModel.getCloudServiceProviderName())
        .currency(pricingModel.getCurrency())
        .operatingSystem(OperatingSystemBuilder.newBuilder()
                .architecture(pricingModel.getOperatingSystemModel().operatingSystemArchitecture())
                .family(pricingModel.getOperatingSystemModel().operatingSystemFamily())
                .version(pricingModel.getOperatingSystemModel().operatingSystemVersion())
                .build())
        .api(ApiBuilder.newBuilder().providerName(pricingModel.getApiModel().getProviderName()).build())
        .tenancy(pricingModel.getTenancy())
        .instanceName(pricingModel.getInstanceName())
        .licenseModel(pricingModel.getLicenseModel())
        .operation(pricingModel.getOperation())
        .productFamily(pricingModel.getProductFamily())
        .capacityStatus(pricingModel.getCapacityStatus())
        .preInstalledSw(pricingModel.getPreInstalledSw())
        .onDemandTerms(
                pricingModel.getPricingTermsModels().stream()
                .filter(term -> term.getTermsType().equals("OnDemand"))
                .map(term -> new PricingTerms(
                        term.getPricingPriceDimensionsModels().stream()
                        .map(priceDimension -> new PricingDimensions(priceDimension.getPricePerUnit(), priceDimension.getUnit(), priceDimension.getDescription(), priceDimension.getBeginRange(), priceDimension.getEndRange()))
                        .collect(Collectors.toList()),
                        term.getLeaseContractLength(),
                        term.getOfferingClass(),
                        term.getPurchaseOption())

                )
                .collect(Collectors.toList()))
        .reservedTerms(
                pricingModel.getPricingTermsModels().stream()
                        .filter(term -> term.getTermsType().equals("Reserved"))
                        .map(term -> new PricingTerms(
                                term.getPricingPriceDimensionsModels().stream()
                                        .map(priceDimension -> new PricingDimensions(priceDimension.getPricePerUnit(), priceDimension.getUnit(), priceDimension.getDescription(), priceDimension.getBeginRange(), priceDimension.getEndRange()))
                                        .collect(Collectors.toList()),
                                term.getLeaseContractLength(),
                                term.getOfferingClass(),
                                term.getPurchaseOption())

                        )
                        .collect(Collectors.toList()))
        .build(), "Internal-Pricing");
    }
}
