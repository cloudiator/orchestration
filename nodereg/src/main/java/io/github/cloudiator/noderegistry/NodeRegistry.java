package io.github.cloudiator.noderegistry;

import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;

public interface NodeRegistry {

  void remove(String vmId) throws RegistryException;

  void put(String vmId, VirtualMachine virtualMachine) throws RegistryException;

}
