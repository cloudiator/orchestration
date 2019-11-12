package io.github.cloudiator.persistance;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PricingModelRepositoryJpa extends BaseModelRepositoryJpa<PricingModel> implements PricingModelRepository {
    private String queryString2findByCloudUniqueId;
    private String queryString2findByCSPHwLocOS;
    private String queryString2getPrices;

    @Inject
    public PricingModelRepositoryJpa(Provider<EntityManager> entityManager, TypeLiteral<PricingModel> type) {
        super(entityManager, type);
        queryString2findByCloudUniqueId = String
                .format("from %s where cloudUniqueId=:cloudUniqueId", super.type.getName());
        queryString2findByCSPHwLocOS = String.format(
                "select pm from %s pm inner join pm.pricingTermsModels ptm inner join ptm.pricingPriceDimensionsModels pdm " +
                        "where pm.instanceName = :hardwareProviderId and pm.locationProviderId = :locationProviderId and preInstalledSw = 'NA' " +
                        "and tenancy = 'Shared' and licenseModel = 'No License required' and operatingSystemModel_id = :operatingSystemId " +
                        "and termsType = 'OnDemand' and apiModel_id = :apiId",
                super.type.getName());
        queryString2getPrices = String.format(
                "select pm from %s pm inner join pm.pricingTermsModels ptm inner join ptm.pricingPriceDimensionsModels pdm " +
                        "where preInstalledSw = 'NA' and tenancy = 'Shared' and licenseModel = 'No License required' and termsType = 'OnDemand' and apiModel_id = :apiId",
                super.type.getName());
    }

    @Nullable
    @Override
    public PricingModel findByCloudUniqueId(String cloudUniqueId) {
        checkNotNull(cloudUniqueId, "cloudUniqueId is null");

        Query query = em().createQuery(queryString2findByCloudUniqueId).setParameter("cloudUniqueId", cloudUniqueId);
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

        Query query = em().createQuery(queryString2findByCSPHwLocOS)
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

        Query query = em().createQuery(queryString2getPrices)
                .setParameter("apiId", apiId);

        @SuppressWarnings("unchecked") List<PricingModel> pricingModels = query.getResultList();

        return pricingModels;
    }
}
