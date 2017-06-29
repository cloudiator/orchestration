/*
 * Copyright (c) 2014-2017 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.iaas.common.persistance.entities;


import de.uniulm.omi.cloudiator.domain.LoginNameSupplier;
import de.uniulm.omi.cloudiator.domain.RemotePortProvider;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 31.10.14.
 */
@Entity
public class VirtualMachine extends ResourceModel implements LoginNameSupplier {

  @Nullable
  @Column(nullable = true)
  private String generatedLoginUsername;
  @Nullable
  @Column(nullable = true)
  private String generatedLoginPassword;
  @Nullable
  @Lob
  @Column()
  private String generatedPrivateKey;

  @Nullable
  @ManyToOne()
  private ImageModel imageModel;
  @Nullable
  @ManyToOne()
  private HardwareModel hardwareModel;
  /**
   * Use set to avoid duplicate entries due to hibernate bug
   * https://hibernate.atlassian.net/browse/HHH-7404
   */
  @OneToMany(mappedBy = "virtualMachine", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<IpAddress> ipAddresses;

  /**
   * Empty constructor for hibernate.
   */
  protected VirtualMachine() {
  }

  public VirtualMachine(String cloudUniqueId, String providerId, String name,
      CloudModel cloudModel, @Nullable LocationModel locationModel, String generatedLoginUsername,
      String generatedLoginPassword, String generatedPrivateKey,
      ImageModel imageModel, HardwareModel hardwareModel) {
    super(cloudUniqueId, providerId, name, cloudModel, locationModel);
    this.generatedLoginUsername = generatedLoginUsername;
    this.generatedLoginPassword = generatedLoginPassword;
    this.generatedPrivateKey = generatedPrivateKey;
    this.imageModel = imageModel;
    this.hardwareModel = hardwareModel;
  }

  public Optional<ImageModel> image() {
    return Optional.ofNullable(imageModel);
  }

  public Optional<HardwareModel> hardware() {
    return Optional.ofNullable(hardwareModel);
  }

  public void addIpAddress(IpAddress ipAddress) {
    if (ipAddresses == null) {
      this.ipAddresses = new HashSet<>();
    }
    this.ipAddresses.add(ipAddress);
  }

  public void removeIpAddress(IpAddress ipAddress) {
    if (ipAddresses == null) {
      return;
    }
    ipAddresses.remove(ipAddress);
  }

  public void removeIpAddresses() {
    if (ipAddresses == null) {
      return;
    }
    ipAddresses.clear();
  }

  public Optional<IpAddress> publicIpAddress() {
    return ipAddresses.stream().filter(ipAddress -> ipAddress.getIpType().equals(IpType.PUBLIC))
        .findAny();
  }

  public Optional<IpAddress> privateIpAddress(boolean fallbackToPublic) {

    final Optional<IpAddress> any =
        ipAddresses.stream().filter(ipAddress -> ipAddress.getIpType().equals(IpType.PRIVATE))
            .findAny();

    if (!any.isPresent() && fallbackToPublic) {
      return publicIpAddress();
    }
    return any;
  }

  public Optional<String> loginPrivateKey() {
    return Optional.ofNullable(generatedPrivateKey);
  }

  public int remotePort() {
    if (imageModel == null) {
      throw new RemotePortProvider.UnknownRemotePortException(
          "Remote port is unknown as image is no longer known.");
    }
    return imageModel.operatingSystem().operatingSystemFamily().remotePort();
  }

  public void setGeneratedLoginUsername(@Nullable String generatedLoginUsername) {
    if (this.generatedLoginUsername != null) {
      throw new IllegalStateException("Changing generatedLoginUsername not permitted.");
    }
    this.generatedLoginUsername = generatedLoginUsername;
  }

  public void setGeneratedLoginPassword(@Nullable String generatedLoginPassword) {
    if (this.generatedLoginPassword != null) {
      throw new IllegalStateException("Changing generatedLoginPassword not permitted.");
    }
    this.generatedLoginPassword = generatedLoginPassword;
  }

  public void setGeneratedPrivateKey(@Nullable String generatedPrivateKey) {
    if (this.generatedPrivateKey != null) {
      throw new IllegalStateException("Changing generatedPrivateKey not permitted.");
    }
    this.generatedPrivateKey = generatedPrivateKey;
  }

  public String loginName() {
    if (generatedLoginUsername != null) {
      return generatedLoginUsername;
    }
    if (imageModel == null) {
      throw new UnknownLoginNameException(
          "Login name is unknown as image is not longer known.");
    }
    return imageModel.loginName();
  }

  public Optional<String> loginPassword() {
    if (generatedLoginPassword != null) {
      return Optional.of(generatedLoginPassword);
    }
    if (imageModel == null) {
      return Optional.empty();
    } else {
      return imageModel.getLoginPasswordOverride();
    }
  }


  public OperatingSystemModel operatingSystem() {
    if (imageModel == null) {
      throw new IllegalStateException("Image is not longer known.");
    }
    return imageModel.operatingSystem();
  }
}
