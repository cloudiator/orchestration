package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.persistence.EntityManager;

public class NodePropertiesModelRepositoryJpa extends BaseModelRepositoryJpa<NodePropertiesModel> implements NodePropertiesModelRepository{

  @Inject
  protected NodePropertiesModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<NodePropertiesModel> type) {
    super(entityManager, type);
  }
}
