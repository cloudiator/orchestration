package io.github.cloudiator.iaas.byon.util;

import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.iaas.byon.UsageException;
import io.github.cloudiator.messaging.NodePropertiesMessageToNodePropertiesConverter;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import java.util.Map;
import org.cloudiator.messages.Byon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonOperations {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonOperations.class);
  private static final NodePropertiesMessageToNodePropertiesConverter NODE_PROPERTIES_CONVERTER = new NodePropertiesMessageToNodePropertiesConverter();
  // todo: this is just a temporary solution, that is employed as long as the hibernate: read -> update deadlock is present
  private static volatile Map<String, Boolean> statesBucket;

  // do not instantiate
  private ByonOperations() {
  }

  public static Byon.ByonNode buildMessageNode(String userId, Byon.ByonData data) {
    return Byon.ByonNode.newBuilder()
        .setId(IdCreator.createId(NODE_PROPERTIES_CONVERTER.apply(data.getProperties())))
        .setUserId(userId)
        .setNodeData(data)
        .build();
  }

  public static boolean isAllocated(String id, String userId) {
    Boolean found = statesBucket.get(new ByonKey(id, userId));

    if(found == null) {
      LOGGER.error(String.format("Cannot check if node is allocated,"
          + "as no node could be queried for id: %s", id));
      return false;
    }

    return found;
  }

  public static boolean allocatedStateChanges(String id, String userId,
      boolean newStateIsAllocated) {
    final boolean allocated = isAllocated(id, userId);

    return newStateIsAllocated != allocated;
  }

  public static void isAllocatable(ByonNode foundNode) throws UsageException {

    if(foundNode == null) {
      throw new UsageException(String.format("Cannot allocate node, as no node with id %s"
          + " and userId %s is known to the system", foundNode.id(), foundNode.userId()));
    }

    if(foundNode.allocated()) {
      throw new UsageException(String.format("Cannot allocate node, as node"
          + " %s is already allocated", foundNode.id()));
    }
  }

  public static void isDeletable(ByonNode foundNode) throws UsageException {
    if(foundNode == null) {
      throw new UsageException(String.format("Cannot delete node, as no node with id %s"
          + " and userId %s is known to the system", foundNode.id(), foundNode.userId()));
    }

    if(!foundNode.allocated() ) {
      throw new UsageException(String.format("Cannot delete node, as node "
          + "is already deleted.", foundNode.id()));
    }
  }

  public static String wrongStateChangeMessage(boolean newStateIsAllocated, String id) {
    final String errorMessage = newStateIsAllocated ?
        String.format("Cannot allocate node %s as it is already allocated", id) :
        String.format("Cannot delete node %s as it is already deleted", id);

    return errorMessage;
  }

  private static class ByonKey {
    private final String id;
    private final String userId;

    ByonKey(String id, String userId) {
      this.id = id;
      this.userId = userId;
    }
  }
}
