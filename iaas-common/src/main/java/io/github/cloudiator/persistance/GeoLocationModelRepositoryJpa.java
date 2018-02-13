package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

public class GeoLocationModelRepositoryJpa extends BaseModelRepositoryJpa<GeoLocationModel> implements
    GeoLocationModelRepository {

  @Inject
  protected GeoLocationModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<GeoLocationModel> type) {
    super(entityManager, type);
  }
}
