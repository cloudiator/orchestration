package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class PropertyModel extends Model {

  @Column(nullable = false)
  private String propertyKey;
  @Column(nullable = false)
  private String propertyValue;
  @ManyToOne
  private CloudConfigurationModel cloudConfigurationModel;

  /**
   * Empty constructor for hibernate
   */
  protected PropertyModel() {

  }

  public PropertyModel(CloudConfigurationModel cloudConfigurationModel, String key, String value) {
    checkNotNull(cloudConfigurationModel);
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    checkArgument(!key.isEmpty(), "key is empty");
    checkArgument(!value.isEmpty(), "value is empty");
    this.cloudConfigurationModel = cloudConfigurationModel;
    this.propertyKey = key;
    this.propertyValue = value;
  }

  public String getKey() {
    return propertyKey;
  }

  public String getValue() {
    return propertyValue;
  }
}
