package io.github.cloudiator.persistance;

import javax.annotation.Nullable;
import javax.persistence.Entity;

@Entity
class LoginCredentialModel extends Model {

  @Nullable
  private String username;

  @Nullable
  private String password;

  @Nullable
  private String privateKey;

  /**
   * Hibernate constructor
   */
  protected LoginCredentialModel() {

  }

  LoginCredentialModel(@Nullable String username, @Nullable String password,
      @Nullable String privateKey) {
    this.username = username;
    this.password = password;
    this.privateKey = privateKey;
  }

  @Nullable
  public String getUsername() {
    return username;
  }

  @Nullable
  public String getPassword() {
    return password;
  }

  @Nullable
  public String getPrivateKey() {
    return privateKey;
  }
}
