package io.github.cloudiator.noderegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NodeRegistryFileImpl<T> implements NodeRegistry<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistrySubscriber.class);
  private final File storage;

  public NodeRegistryFileImpl() {
    storage = new File(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
        "node.registry." + NodeRegistryFileImpl.class.getCanonicalName().hashCode());

  }

  @Override
  public synchronized void remove(String key) throws RegistryException {
    HashMap<String, T> map = readFromFile();
    T b = map.remove(key);
    if (b == null) {
      LOGGER.debug("cannot delete vm with ID '" + key + "'. Id not known.");
    } else {
      LOGGER.info("deleted vm with ID '" + key + "'.");
    }
    writeToFile(map);
  }

  @Override
  public synchronized void put(String key, T t)
      throws RegistryException {
    HashMap<String, T> map = readFromFile();
    T b = map.put(key, t);
    if (b != null) {
      LOGGER.debug("created entry for vm with ID '" + key + "', but Id already exists.");
    } else {
      LOGGER.info("created entry for vm with ID '" + key + "'.");
    }
    writeToFile(map);
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, T> readFromFile() throws RegistryException {
    if (storage.exists()) {
      try (FileInputStream fin = new FileInputStream(storage)) {
        ObjectInputStream in = new ObjectInputStream(fin);
        return (HashMap<String, T>) in.readObject();
      } catch (Exception ex) {
        throw new RegistryException(ex);
      }
    } else {
      return new HashMap<>();
    }
  }

  private void writeToFile(HashMap<String, T> map) throws RegistryException {
    try (FileOutputStream fout = new FileOutputStream(storage)) {
      ObjectOutputStream out = new ObjectOutputStream(fout);
      out.writeObject(map);
    } catch (Exception ex) {
      throw new RegistryException(ex);
    }
  }
}
