package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersion;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import de.uniulm.omi.cloudiator.sword.domain.Api;
import io.github.cloudiator.domain.DiscoveredPricing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class PricingDomainRepository {
    private static final PricingConverter PRICING_CONVERTER = new PricingConverter();
    private final PricingModelRepository pricingModelRepository;
    private final ApiDomainRepository apiDomainRepository;
    private final OperatingSystemDomainRepository operatingSystemDomainRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingDomainRepository.class);

    @Inject
    public PricingDomainRepository(PricingModelRepository pricingModelRepository, ApiDomainRepository apiDomainRepository, OperatingSystemDomainRepository operatingSystemDomainRepository) {
        this.pricingModelRepository = pricingModelRepository;
        this.apiDomainRepository = apiDomainRepository;
        this.operatingSystemDomainRepository = operatingSystemDomainRepository;
    }

    public DiscoveredPricing findById(String id) {
        return PRICING_CONVERTER.apply(pricingModelRepository.findByCloudUniqueId(id));
    }

    public double findPrice(String cloudProviderName, String locationProviderId, String hardwareProviderId, String osArchitecture, String osFamily) throws Exception {
        ApiModel api = apiDomainRepository.findModelByProviderName(cloudProviderName);

        OperatingSystemArchitecture operatingSystemArchitecture = OperatingSystemArchitecture.valueOf(osArchitecture);
        OperatingSystemArchitecture operatingSystemArchitectureFind;
        switch (operatingSystemArchitecture) {
            case AMD64:
            case UNKNOWN: {
                operatingSystemArchitectureFind = OperatingSystemArchitecture.AMD64;
                break;
            }
            case I368: {
                operatingSystemArchitectureFind = OperatingSystemArchitecture.I368;
                break;
            }
            default: {
                throw new Exception(String.format("No data for Operating System Architecture %s", osArchitecture));
            }
        }

        OperatingSystemFamily operatingSystemFamily = OperatingSystemFamily.valueOf(osFamily);
        OperatingSystemFamily operatingSystemFamilyFind;
        switch (operatingSystemFamily) {
            case RHEL: {
                operatingSystemFamilyFind = OperatingSystemFamily.RHEL;
                break;
            }
            case SUSE: {
                operatingSystemFamilyFind = OperatingSystemFamily.SUSE;
                break;
            }
            case WINDOWS: {
                operatingSystemFamilyFind = OperatingSystemFamily.WINDOWS;
                break;
            }
            case CENTOS:
            case AMZN_LINUX:
            case DEBIAN:
            case UNKNOWN:
            case UBUNTU: {
                operatingSystemFamilyFind = OperatingSystemFamily.UBUNTU;
                break;
            }
            default: {
                throw new Exception(String.format("No data for Operating System Family %s", osFamily));
            }
        }

        OperatingSystemModel operatingSystem = operatingSystemDomainRepository.findByArchitectureFamilyVersion(
                operatingSystemArchitectureFind,
                operatingSystemFamilyFind,
                OperatingSystemVersions.unknown());

        PricingModel pricingModel = pricingModelRepository.findByCSPHardwareLocationOS(api.getId(), locationProviderId, hardwareProviderId, operatingSystem.getId());

        if(pricingModel != null) {
            return pricingModel.getPricingTermsModels().get(0).getPricingPriceDimensionsModels().get(0).getPricePerUnit().doubleValue();
        } else {
            throw new Exception(String.format("No pricing data found for: CSP: %s, Location: %s, Instance: %s, OS: %s, %s", cloudProviderName, locationProviderId, hardwareProviderId, osArchitecture, osFamily));
        }
    }

    public List<DiscoveredPricing> getPrices(String cloudAPIProviderName) throws Exception {
        ApiModel api = apiDomainRepository.findModelByProviderName(cloudAPIProviderName);

        List<PricingModel> pricingModels = pricingModelRepository.getPrices(api.getId());

        if(pricingModels != null) {
            return pricingModels.stream().map(PRICING_CONVERTER::apply).collect(Collectors.toList());
        } else {
            throw new Exception(String.format("No pricing data found for Cloud API Provider Name: %s", cloudAPIProviderName));
        }
    }

    public void save(DiscoveredPricing domain) {
        checkNotNull(domain, "domain is null");
        saveAndGet(domain);
    }

    PricingModel saveAndGet(DiscoveredPricing domain) {
        checkNotNull(domain, "domain is null");

        PricingModel model = pricingModelRepository.findByCloudUniqueId(domain.id());
        if (model == null) {
            model = createModel(domain);
        } else {
            //TODO updating pricing model ?
            LOGGER.info("pricing model found in Pricing Model Repository, CloudUniqueId = {}", domain.id());
        }
        pricingModelRepository.save(model);

        return model;
    }

    private PricingModel createModel(DiscoveredPricing domain) {
        final ApiModel apiModel = getApiModel(domain);

        checkState(apiModel != null, String
                .format("Cannot save pricing %s as related apiModel is missing.",
                        domain));

        OperatingSystemModel operatingSystemModel = operatingSystemDomainRepository
                .saveOrGet(domain.getOperatingSystem());

        PricingModel pricingModel = new PricingModel(domain.id(), domain.providerId(), domain.name(),
                domain.getInstanceName(), domain.getCloudServiceProviderName(), domain.getLicenseModel(),
                domain.getLocationProviderId(), domain.getCurrency(), domain.getTenancy(), operatingSystemModel,
                null,
                apiModel, domain.getProductFamily(), domain.getPreInstalledSw(), domain.getCapacityStatus(), domain.getOperation());

        List<PricingTermsModel> pricingOnDemandTermsModels = domain.getPricingOnDemandTerms().stream()
                .map(term -> new PricingTermsModel(term.getLeaseContractLength(), term.getOfferingClass(),
                        term.getPurchaseOption(),
                        term.getPricingDimensions().stream()
                                .map(dim -> new PricingPriceDimensionsModel(dim.getPricePerUnit(), dim.getUnit(), dim.getDescription(), dim.getBeginRange(), dim.getEndRange()))
                                .collect(Collectors.toList()),
                        "OnDemand", pricingModel))
                .collect(Collectors.toList());

        pricingOnDemandTermsModels.forEach(pricingTermsModel ->
                pricingTermsModel.getPricingPriceDimensionsModels().forEach(pricingPriceDimensionsModel ->
                        pricingPriceDimensionsModel.setPricingTermsModel(pricingTermsModel)));

        List<PricingTermsModel> pricingReservedTermsModels = domain.getPricingReservedTerms().stream()
                .map(term -> new PricingTermsModel(term.getLeaseContractLength(), term.getOfferingClass(),
                        term.getPurchaseOption(),
                        term.getPricingDimensions().stream()
                                .map(dim -> new PricingPriceDimensionsModel(dim.getPricePerUnit(), dim.getUnit(), dim.getDescription(), dim.getBeginRange(), dim.getEndRange()))
                                .collect(Collectors.toList()),
                        "Reserved", pricingModel))
                .collect(Collectors.toList());

        pricingReservedTermsModels.forEach(pricingTermsModel ->
                pricingTermsModel.getPricingPriceDimensionsModels().forEach(pricingPriceDimensionsModel ->
                        pricingPriceDimensionsModel.setPricingTermsModel(pricingTermsModel)));

        pricingModel.setPricingTermsModels(Stream.concat(pricingOnDemandTermsModels.stream(), pricingReservedTermsModels.stream()).collect(Collectors.toList()));

        return pricingModel;
    }

    private ApiModel getApiModel(DiscoveredPricing domain) {
        List<ApiModel> apiModels = apiDomainRepository.findAll();
        Optional<ApiModel> apiModel = apiModels.stream().filter(model -> model.getProviderName().matches("(?i).*aws.*")).findAny();
        if(apiModel.isPresent()) {
            return apiModel.get();
        }
        else {
            return apiDomainRepository.saveOrGet(domain.getApi());
        }
    }

}
