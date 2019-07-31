package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.domain.ByonIO;
import org.cloudiator.messages.entities.ByonEntities;
import org.cloudiator.messages.entities.ByonEntities.CacheOperation;

public class ByonOperationConverter implements TwoWayConverter<ByonIO, ByonEntities.CacheOperation> {

  @Override
  public ByonIO applyBack(CacheOperation cacheOperation) {
    switch(cacheOperation) {
      case ADD:
        return ByonIO.ADD;
      case EVICT:
        return ByonIO.EVICT;
      case UPDATE:
        return ByonIO.UPDATE;
      default:
       throw new IllegalArgumentException(String.format("operation %s can not be converted"
       , cacheOperation));
    }
  }

  @Override
  public CacheOperation apply(ByonIO byonIO) {
    switch(byonIO) {
      case ADD:
        return CacheOperation.ADD;
      case EVICT:
        return CacheOperation.EVICT;
      case UPDATE:
        return CacheOperation.UPDATE;
      default:
        return CacheOperation.UNRECOGNIZED;
    }
  }
}
