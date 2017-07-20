package io.github.cloudiator.orchestration.installer.tools;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;

/**
 * Created by daniel on 08.02.17.
 */
public interface Environment {

    String homeDir();

    OperatingSystem os();

    String publicIp();

    String privateIp();
}
