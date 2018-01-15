package io.github.cloudiator.orchestration.installer.tools;

import javax.annotation.Nullable;

/**
 * Created by daniel on 08.02.17.
 */
public class Downloads {

  public static Download of(String url, @Nullable String fileName) {
    return new DownloadImpl(url, fileName);
  }

  public static Download of(String url) {
    return new DownloadImpl(url, null);
  }

}
