package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Pricing;
import de.uniulm.omi.cloudiator.sword.service.PricingService;
import io.github.cloudiator.iaas.discovery.error.DiscoveryErrorHandler;
import io.github.cloudiator.persistance.PricingDomainRepository;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PricingDiscoveryWorker extends AbstractDiscoveryWorker<Pricing> {

    private final PricingDomainRepository pricingDomainRepository;
    private final PricingService pricingService;

    @Inject
    public PricingDiscoveryWorker(DiscoveryQueue discoveryQueue,
                                  PricingService pricingService, DiscoveryErrorHandler discoveryErrorHandler,
                                  PricingDomainRepository pricingDomainRepository) {
        super(discoveryQueue, discoveryErrorHandler);
        this.pricingService = pricingService;
        this.pricingDomainRepository = pricingDomainRepository;
    }

    @Override
    protected Iterable<Pricing> resources() {
        return pricingService.listPricing();
    }

    @Override
    protected Predicate<Pricing> filter() {
        return new Predicate<Pricing>() {
            @Override
            public boolean test(Pricing pricing) {
                return pricingDomainRepository.findById(pricing.id()) == null;
            }
        };
    }

    @Override
    public long period() {
        return 2;
    }

    @Override
    public TimeUnit timeUnit() {
        return TimeUnit.MINUTES;
    }
}