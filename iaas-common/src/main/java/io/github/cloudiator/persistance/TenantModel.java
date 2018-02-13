package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by daniel on 30.05.17.
 */
@Entity
class TenantModel extends Model {

  @Column(nullable = false)
  private String userId;

  /**
   * Empty constructor for hibernate.
   */
  protected TenantModel() {

  }

  public TenantModel(String userId) {
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