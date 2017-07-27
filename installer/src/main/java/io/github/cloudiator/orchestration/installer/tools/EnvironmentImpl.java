package io.github.cloudiator.orchestration.installer.tools;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.domain.OperatingSystem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 08.02.17.
 */
public class EnvironmentImpl implements Environment {

    private final String homeDir;
    private final OperatingSystem os;
    private final String publicIp;
    private final String privateIp;

    EnvironmentImpl(String homeDir, OperatingSystem os, String publicIp, String privateIp) {
        checkNotNull(homeDir, "CLOUDIATOR_DIR is null.");
        checkArgument(!homeDir.isEmpty(), "CLOUDIATOR_DIR is empty.");
        this.homeDir = homeDir;
        checkNotNull(os, "os is null.");
        this.os = os;
        checkNotNull(publicIp, "publicIp is null.");
        checkArgument(!publicIp.isEmpty(), "publicIp is empty.");
        this.publicIp = publicIp;
        checkNotNull(privateIp, "privateIp is null");
        checkArgument(!privateIp.isEmpty(), "privateIp is empty.");
        this.privateIp = privateIp;
    }

    @Override public String homeDir() {
        return homeDir;
    }

    @Override public OperatingSystem os() {
        return os;
    }

    @Override public String publicIp() {
        return publicIp;
    }

    @Override public String privateIp() {
        return privateIp;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("CLOUDIATOR_DIR", homeDir).add("os", os)
            .add("publicIp", publicIp).add("privateIp", privateIp).toString();
    }
}
