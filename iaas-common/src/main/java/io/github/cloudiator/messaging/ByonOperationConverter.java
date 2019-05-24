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
      case REMOVE:
        return ByonIO.REMOVE;
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
      case REMOVE:
        return CacheOperation.REMOVE;
      default:
        return CacheOperation.UNRECOGNIZED;
    }
  }
}
