package io.github.cloudiator.persistance;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
class LoginCredentialModel extends Model {

  @Nullable
  @Lob
  private String username;

  @Nullable
  @Lob
  private String password;

  @Nullable
  @Lob
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
