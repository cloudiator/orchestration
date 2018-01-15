package io.github.cloudiator.orchestration.installer.tools;

import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.Set;

/**
 * Created by daniel on 08.02.17.
 */
public class JavaBundle implements Bundle {

  private final static String LINUX_DOWNLOAD_PATH = "tools.linux.java.download";
  private final static String WINDOWS_DOWNLOAD_PATH = "tools.windows.java.download";
  private final static String JAVA_ARCHIVE = "jre8.tar.gz";
  private final static String JAVA_EXE = "jre8.exe";
  private final static String JAVA_DIR = "jre8";

  @Override
  public String name() {
    return toString();
  }

  @Override
  public Set<String> dependencies(Environment environment) {
    return Collections.emptySet();
  }

  @Override
  public Set<Integer> inboundPorts() {
    return Collections.emptySet();
  }

  @Override
  public Set<Download> downloads(Environment environment) {
    switch (environment.os().operatingSystemFamily().operatingSystemType()) {
      case WINDOWS:
        return Collections.singleton(Downloads.of(WINDOWS_DOWNLOAD_PATH, JAVA_EXE));
      case LINUX:
        return Collections.singleton(Downloads.of(LINUX_DOWNLOAD_PATH, JAVA_ARCHIVE));
      default:
        throw new UnsupportedOperationException(
            String.format("%s is not supported in environment %s.", this, environment));
    }
  }

  @Override
  public StatementList statements(Environment environment) {
    switch (environment.os().operatingSystemFamily().operatingSystemType()) {
      case WINDOWS:
        Statements.of(String.format("powershell -command %s\\%s /s INSTALLDIR=%s\\%s",
            environment.homeDir(), JAVA_EXE, environment.homeDir(), JAVA_DIR), String
                .format("SETX PATH %%PATH%%;%s\\%s\\bin /m", environment.homeDir(), JAVA_DIR),
            String.format("SETX JAVA_HOME %s\\%s /m", environment.homeDir(), JAVA_DIR));
        return Statements.empty();
      case LINUX:
        return Statements.of(String.format("mkdir %s", JAVA_DIR), String
            .format("tar zxvf %s -C %s --strip-components=1", JAVA_ARCHIVE, JAVA_DIR));
      default:
        throw new UnsupportedOperationException(
            String.format("%s is not supported in environment %s", this, environment));
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
