package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Cloud;

public interface ErrorAwareCloud extends Cloud {

  LocalCloudState localState();

  String diagnostic();

}
