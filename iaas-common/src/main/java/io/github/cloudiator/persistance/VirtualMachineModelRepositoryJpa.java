package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

class VirtualMachineModelRepositoryJpa extends
    BaseModelRepositoryJpa<VirtualMachineModel> implements VirtualMachineModelRepository {

  @Inject
  public VirtualMachineModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<VirtualMachineModel> type) {
    super(entityManager, type);
  }

  @Nullable
  @Override
  public VirtualMachineModel findByCloudUniqueId(String cloudUniqueId) {
    checkNotNull(cloudUniqueId, "cloudUniqueId is null");
    String queryString = String
        .format("select vm from %s vm where vm.cloudUniqueId=:id", type.getName());
    Query query = em().createQuery(queryString).setParameter("id", cloudUniqueId);
    try {
      //noinspection unchecked
      return (VirtualMachineModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<VirtualMachineModel> findByTenant(String tenant) {
    checkNotNull(tenant, "tenant is null");
    String queryString = String.format(
        "select vm from %s vm inner join vm.tenantModel tenant where tenant.userId=:tenant",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant);
    //noinspection unchecked
    return (List<VirtualMachineModel>) query.getResultList();
  }

  @Override
  public VirtualMachineModel findByCloudUniqueIdAndTenant(String tenant, String cloudUniqueId) {
    checkNotNull(tenant, "tenant is null");
    checkNotNull(cloudUniqueId, "id is null");
    String queryString = String.format(
        "select vm from %s vm inner join vm.tenantModel tenant where tenant.userId=:tenant and vm.cloudUniqueId = :id",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant)
        .setParameter("id", cloudUniqueId);
    //noinspection unchecked
    try {
      //noinspection unchecked
      return (VirtualMachineModel) query.getSingleResult();
    } catch (NoResultException ignored) {
      return null;
    }
  }

  @Override
  public List<VirtualMachineModel> findByTenantAndCloud(String tenant, String cloudId) {
    checkNotNull(tenant, "tenant is null");
    checkNotNull(cloudId, "cloudId is null");
    String queryString = String.format(
        "select vm from %s vm inner inner join vm.tenantModel tenant where tenant.userId=:tenant and vm.cloudId = :cloudId",
        type.getName());
    Query query = em().createQuery(queryString).setParameter("tenant", tenant)
        .setParameter("cloudId", cloudId);
    //noinspection unchecked
    return (List<VirtualMachineModel>) query.getResultList();
  }


}
