package io.github.cloudiator.iaas.common.persistance.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import de.uniulm.omi.cloudiator.persistance.repositories.BaseModelRepositoryJpa;
import io.github.cloudiator.iaas.common.persistance.entities.GeoLocationModel;
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
