package io.github.cloudiator.persistance;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class IpGroupModel extends Model {

  /**
   * Use set to avoid duplicate entries due to hibernate bug https://hibernate.atlassian.net/browse/HHH-7404
   */
  @OneToMany(mappedBy = "ipGroup", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<IpAddressModel> ipAddressModels;

  IpGroupModel() {

  }

  public void addIpAddress(IpAddressModel ipAddressModel) {
    if (ipAddressModels == null) {
      this.ipAddressModels = new HashSet<>();
    }
    ipAddressModels.add(ipAddressModel);
  }


}
