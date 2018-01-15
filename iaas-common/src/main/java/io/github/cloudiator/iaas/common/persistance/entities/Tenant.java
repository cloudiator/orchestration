package io.github.cloudiator.iaas.common.persistance.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.persistance.entities.Model;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by daniel on 30.05.17.
 */
@Entity
public class Tenant extends Model {

  @Column(nullable = false)
  private String userId;

  /**
   * Empty constructor for hibernate.
   */
  protected Tenant() {

  }

  public Tenant(String userId) {
    checkNotNull(userId, "userId is null");
    checkArgument(!userId.isEmpty(), "userId is empty");
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("userId", userId);
  }
}
