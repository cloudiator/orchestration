package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * Created by daniel on 02.06.17.
 */
class HardwareOfferModelRepositoryJpa extends BaseModelRepositoryJpa<HardwareOfferModel> implements
    HardwareOfferModelRepository {

  @Inject
  HardwareOfferModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<HardwareOfferModel> type) {
    super(entityManager, type);
  }

  @Override
  public HardwareOfferModel findByCpuRamDisk(int numberOfCores, long mbOfRam,
      @Nullable Double diskSpace) {
    //todo: check correctness of query
    String queryStringWithDiskSpace = String
        .format(
            "from %s where numberOfCores=:numberOfCores and mbOfRam=:mbOfRam and diskSpace=:diskSpace",
            type.getName());
    String queryStringWithOutDiskSpace = String
        .format(
            "from %s where numberOfCores=:numberOfCores and mbOfRam=:mbOfRam and diskSpace is null",
            type.getName());

    Query query;
    if (diskSpace == null) {
      query = em().createQuery(queryStringWithOutDiskSpace);
    } else {
      query = em().createQuery(queryStringWithDiskSpace);
      query.setParameter("diskSpace", diskSpace);
    }

    query
        .setParameter("numberOfCores", numberOfCores)
        .setParameter("mbOfRam", mbOfRam);
    try {
      //noinspection unchecked
      return (HardwareOfferModel) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }

  }
}
