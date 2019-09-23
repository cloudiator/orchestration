package io.github.cloudiator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.Location;
import de.uniulm.omi.cloudiator.sword.domain.Pricing;
import de.uniulm.omi.cloudiator.sword.domain.PricingTerms;

import java.util.List;
import java.util.Optional;

public class DiscoveredPricing implements Pricing {
    private final Pricing delegate;
    private final String userId;

    public DiscoveredPricing(Pricing delegate, String userId) {
        this.delegate = delegate;
        this.userId = userId;
    }

    @Override
    public String getCurrency() {
        return delegate.getCurrency();
    }

    @Override
    public String getTenancy() {
        return delegate.getTenancy();
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return delegate.getOperatingSystem();
    }

    @Override
    public List<PricingTerms> getPricingOnDemandTerms() {
        return delegate.getPricingOnDemandTerms();
    }

    @Override
    public List<PricingTerms> getPricingReservedTerms() {
        return delegate.getPricingReservedTerms();
    }

    @Override
    public String getCloudServiceProviderName() {
        return delegate.getCloudServiceProviderName();
    }

    @Override
    public String getInstanceName() {
        return delegate.getInstanceName();
    }

    @Override
    public String getLicenseModel() {
        return delegate.getLicenseModel();
    }

    @Override
    public String providerId() {
        return delegate.providerId();
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    @JsonProperty
    public String name() {
        return delegate.name();
    }

    @Override
    public Optional<String> locationId() {
        return delegate.locationId();
    }

    @Override
    public Optional<Location> location() {
        if (delegate.location().isPresent()) {
            return Optional
                    .of(new DiscoveredLocation(delegate.location().get(), DiscoveryItemState.UNKNOWN,
                            userId));
        }

        return Optional.empty();
    }

    public String userId() {
        return this.userId;
    }

    @Override
    public String getLocationProviderId() {
        return delegate.getLocationProviderId();
    }

    @Override
    public Api getApi() {
        return delegate.getApi();
    }

    @Override
    public String getProductFamily() {
        return delegate.getProductFamily();
    }

    @Override
    public String getPreInstalledSw() {
        return delegate.getPreInstalledSw();
    }

    @Override
    public String getCapacityStatus() {
        return delegate.getCapacityStatus();
    }

    @Override
    public String getOperation() {
        return delegate.getOperation();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", this.id()).add("delegate", this.delegate).toString();
    }
}
