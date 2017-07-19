package io.github.cloudiator.noderegistry;

public interface NodeRegistry<T> {

  void remove(String key) throws RegistryException;

  void put(String key, T value) throws RegistryException;

}
