package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.sword.domain.Api;
import de.uniulm.omi.cloudiator.sword.domain.Cloud;
import de.uniulm.omi.cloudiator.sword.domain.CloudCredential;
import de.uniulm.omi.cloudiator.sword.domain.CloudType;
import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import java.util.Optional;
import javax.annotation.Nullable;

public class ExtendedCloudImpl implements ExtendedCloud {

  private final Cloud delegate;
  private final String userId;
  private final CloudState cloudState;
  @Nullable
  private final String diagnostic;

  ExtendedCloudImpl(Cloud delegate, String userId, CloudState cloudState,
      @Nullable String diagnostic) {
    this.delegate = delegate;
    this.userId = userId;
    this.cloudState = cloudState;
    this.diagnostic = diagnostic;
  }

  @Override
  public CloudState state() {
    return cloudState;
  }

  @Override
  public Optional<String> diagnostic() {
    return Optional.ofNullable(diagnostic);
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public String id() {
    return delegate.id();
  }

  @Override
  public Api api() {
    return delegate.api();
  }

  @Override
  public Optional<String> endpoint() {
    return delegate.endpoint();
  }

  @Override
  public CloudCredential credential() {
    return delegate.credential();
  }

  @Override
  public Configuration configuration() {
    return delegate.configuration();
  }

  @Nullable
  @Override
  public CloudType cloudType() {
    return delegate.cloudType();
  }
}
