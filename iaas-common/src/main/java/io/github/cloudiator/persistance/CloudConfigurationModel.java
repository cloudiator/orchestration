package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class CloudConfigurationModel extends Model {

  @Column(nullable = false, updatable = false)
  private String nodeGroup;
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "cloudConfigurationModel")
  private List<PropertyModel> properties;

  /**
   * Empty constructor for hibernate.
   */
  protected CloudConfigurationModel() {

  }

  public CloudConfigurationModel(String nodeGroup) {
    checkNotNull(nodeGroup, "nodeGroup is null");
    checkArgument(!nodeGroup.isEmpty(), "nodeGroup is empty");
    this.nodeGroup = nodeGroup;
  }

  public void addProperty(String key, String value) {
    PropertyModel propertyModel = new PropertyModel(this, key, value);
    addProperty(propertyModel);
  }

  public void addProperty(PropertyModel propertyModel) {
    checkNotNull(propertyModel, "propertyModel is null");
    if (properties == null) {
      properties = new ArrayList<>();
    }
    properties.add(propertyModel);
  }

  public String getNodeGroup() {
    return nodeGroup;
  }

  public List<PropertyModel> getProperties() {
    return properties;
  }
}
