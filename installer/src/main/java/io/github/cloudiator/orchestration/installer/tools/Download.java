package io.github.cloudiator.orchestration.installer.tools;

import java.util.Optional;

/**
 * Created by daniel on 08.02.17.
 */
public interface Download {

  String url();

  Optional<String> fileName();

}