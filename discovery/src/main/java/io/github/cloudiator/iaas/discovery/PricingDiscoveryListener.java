package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.domain.Pricing;
import io.github.cloudiator.domain.DiscoveredPricing;
import io.github.cloudiator.persistance.PricingDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingDiscoveryListener implements DiscoveryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PricingDiscoveryListener.class);
    private final PricingDomainRepository pricingDomainRepository;

    @Inject
    public PricingDiscoveryListener(
            PricingDomainRepository pricingDomainRepository) {
        this.pricingDomainRepository = pricingDomainRepository;
    }

    @Override
    public Class<?> interestedIn() {
        return Pricing.class;
    }

    @Override
    @Transactional
    public void handle(Object o) {

        final Pricing pricing = (Pricing) o;

        DiscoveredPricing discoveredPricing = new DiscoveredPricing(pricing, "Internal-Pricing");

        try {
            pricingDomainRepository.save(discoveredPricing);
        } catch (Exception e) {
            LOGGER.info("Exception caught while saving discovered pricing", e);
        }
    }
}
