package io.github.cloudiator.iaas.common.persistance.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.ApiModel;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

public class ApiModelRepositoryJpa extends BaseModelRepositoryJpa<ApiModel> implements
    ApiModelRepository {

  @Inject
  protected ApiModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<ApiModel> type) {
    super(entityManager, type);
  }

  @Override
  @Nullable public ApiModel findByProviderName(String providerName) {
    String query = String.format("from %s where providerName=:p", type.getName());
    @SuppressWarnings("unchecked") List<ApiModel> models = em().createQuery(query)
        .setParameter("p", providerName).getResultList();
    if (models.isEmpty()) {
      return null;
    }
    return models.get(0);
  }
}
