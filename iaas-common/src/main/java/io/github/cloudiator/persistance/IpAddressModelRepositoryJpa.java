package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

class IpAddressModelRepositoryJpa extends BaseModelRepositoryJpa<IpAddressModel> implements IpAddressModelRepository {

  @Inject
  protected IpAddressModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<IpAddressModel> type) {
    super(entityManager, type);
  }
}
