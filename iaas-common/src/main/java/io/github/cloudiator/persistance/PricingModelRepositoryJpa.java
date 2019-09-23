package io.github.cloudiator.persistance;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.Api;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PricingModelRepositoryJpa extends BaseModelRepositoryJpa<PricingModel> implements PricingModelRepository {
    @Inject
    public PricingModelRepositoryJpa(Provider<EntityManager> entityManager, TypeLiteral<PricingModel> type) {
        super(entityManager, type);
    }

    @Nullable
    @Override
    public PricingModel findByCloudUniqueId(String cloudUniqueId) {
        checkNotNull(cloudUniqueId, "cloudUniqueId is null");
        String queryString = String
                .format("from %s where cloudUniqueId=:cloudUniqueId", type.getName());
        Query query = em().createQuery(queryString).setParameter("cloudUniqueId", cloudUniqueId);
        try {
            //noinspection unchecked
            return (PricingModel) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public PricingModel findByCSPHardwareLocationOS(Long apiId, String locationProviderId, String hardwareProviderId, Long operatingSystemId) {
        checkNotNull(apiId, "apiId is null");
        checkNotNull(locationProviderId, "locationProviderId is null");
        checkNotNull(hardwareProviderId, "hardwareProviderId is null");
        checkNotNull(operatingSystemId, "operatingSystemId is null");

        String queryString = String.format(
                "select pm from %s pm inner join pm.pricingTermsModels ptm inner join ptm.pricingPriceDimensionsModels pdm " +
                        "where pm.instanceName = :hardwareProviderId and pm.locationProviderId = :locationProviderId and preInstalledSw = 'NA' " +
                        "and tenancy = 'Shared' and licenseModel = 'No License required' and operatingSystemModel_id = :operatingSystemId " +
                        "and termsType = 'OnDemand' and apiModel_id = :apiId",
                type.getName());

        Query query = em().createQuery(queryString)
                .setParameter("hardwareProviderId", hardwareProviderId)
                .setParameter("locationProviderId", locationProviderId)
                .setParameter("operatingSystemId", operatingSystemId)
                .setParameter("apiId", apiId);

        @SuppressWarnings("unchecked") List<PricingModel> pricingModels = query.getResultList();
        return pricingModels.stream().findFirst().orElse(null);
    }

    @Nullable
    @Override
    public List<PricingModel> getPrices(Long apiId) {
        checkNotNull(apiId, "apiId is null");

        String queryString = String.format(
                "select pm from %s pm inner join pm.pricingTermsModels ptm inner join ptm.pricingPriceDimensionsModels pdm " +
                        "where preInstalledSw = 'NA' and tenancy = 'Shared' and licenseModel = 'No License required' and termsType = 'OnDemand' and apiModel_id = :apiId",
                type.getName());

        Query query = em().createQuery(queryString)
                .setParameter("apiId", apiId);

        @SuppressWarnings("unchecked") List<PricingModel> pricingModels = query.getResultList();

        return pricingModels;
    }
}
