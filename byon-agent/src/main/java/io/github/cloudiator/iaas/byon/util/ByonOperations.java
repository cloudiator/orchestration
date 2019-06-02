package io.github.cloudiator.iaas.byon.util;

import io.github.cloudiator.domain.ByonNode;
import io.github.cloudiator.persistance.ByonNodeDomainRepository;
import org.cloudiator.messages.Byon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonOperations {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonOperations.class);

  // do not instantiate
  private ByonOperations() {
  }

  public static Byon.ByonNode buildMessageNode(String userId, Byon.ByonData data) {
    return Byon.ByonNode.newBuilder()
        .setId(IdCreator.createId(data))
        .setUserId(userId)
        .setNodeData(data)
        .build();
  }


  public static boolean isAllocated(ByonNodeDomainRepository repository, String id, String userId) {
    ByonNode node = repository.findByTenantAndId(userId, id);

    if(node == null) {
      LOGGER.error(String.format("Cannot check if node is allocated,"
          + "as no node could be queried for id: %s", id));
      return false;
    }

    return node.allocated();
  }

  public static boolean allocatedStateChanges(ByonNodeDomainRepository repository, String id, String userId,
      boolean newStateIsAllocated) {
    final boolean allocated = isAllocated(repository, id, userId);

    return newStateIsAllocated != allocated;
  }

  public static String wrongStateChangeMessage(boolean newStateIsAllocated, String id) {
    final String errorMessage = newStateIsAllocated ?
        String.format("Cannot allocate node %s as it is already allocated", id) :
        String.format("Cannot delete node %s as it is already deleted", id);

    return errorMessage;
  }
}
