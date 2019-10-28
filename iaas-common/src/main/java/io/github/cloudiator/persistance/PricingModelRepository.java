package io.github.cloudiator.persistance;

import javax.annotation.Nullable;
import java.util.List;

public interface PricingModelRepository extends ModelRepository<PricingModel> {
    @Nullable
    PricingModel findByCloudUniqueId(String cloudUniqueId);

    @Nullable
    PricingModel findByCSPHardwareLocationOS(Long apiId, String locationProviderId, String hardwareProviderId, Long operatingSystemId);

    @Nullable
    List<PricingModel> getPrices(Long apiId);
}
