package io.github.cloudiator.noderegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.cloudiator.messages.entities.IaasEntities.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NodeRegistryFileImpl implements NodeRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistrySubscriber.class);
  private final File storage;

  public NodeRegistryFileImpl() {
    storage = new File(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
        "node.registry." + NodeRegistryFileImpl.class.getCanonicalName().hashCode());

  }

  @Override
  public synchronized void remove(String vmId) throws RegistryException {
    HashMap<String, byte[]> map = readFromFile();
    byte[] b = map.remove(vmId);
    if(b == null) {
      LOGGER.debug("cannot delete vm with ID '" + vmId + "'. Id not known.");
    } else {
      LOGGER.info("deleted vm with ID '" + vmId + "'.");
    }
    writeToFile(map);
  }

  @Override
  public synchronized void put(String vmId, VirtualMachine virtualMachine)
      throws RegistryException {
    HashMap<String, byte[]> map = readFromFile();
    byte[] b = map.put(vmId, virtualMachine.toByteArray());
    if(b != null) {
      LOGGER.debug("created entry for vm with ID '" + vmId + "', but Id already exists.");
    } else {
      LOGGER.info("created entry for vm with ID '" + vmId + "'.");
    }
    writeToFile(map);
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, byte[]> readFromFile() throws RegistryException {
    if (storage.exists()) {
      try (FileInputStream fin = new FileInputStream(storage)) {
        ObjectInputStream in = new ObjectInputStream(fin);
        return (HashMap<String, byte[]>) in.readObject();
      } catch (Exception ex) {
        throw new RegistryException(ex);
      }
    } else {
      return new HashMap<>();
    }
  }

  private void writeToFile(HashMap<String, byte[]> map) throws RegistryException {
    try (FileOutputStream fout = new FileOutputStream(storage)) {
      ObjectOutputStream out = new ObjectOutputStream(fout);
      out.writeObject(map);
    } catch (Exception ex) {
      throw new RegistryException(ex);
    }
  }
}
