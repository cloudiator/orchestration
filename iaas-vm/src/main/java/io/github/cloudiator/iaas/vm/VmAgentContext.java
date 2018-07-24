package io.github.cloudiator.iaas.vm;

import static io.github.cloudiator.iaas.vm.Constants.VM_PARALLEL_STARTS;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;

public class VmAgentContext {

  private final Config config;

  public VmAgentContext() {
    this(Configuration.conf());
  }

  public VmAgentContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "vm");
  }

  public int parallelVMStarts() {
    return config.getInt(VM_PARALLEL_STARTS);
  }

}
