/*
 * Copyright (c) 2014-2018 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.domain.OperatingSystemArchitecture;
import de.uniulm.omi.cloudiator.domain.OperatingSystemBuilder;
import de.uniulm.omi.cloudiator.domain.OperatingSystemFamily;
import de.uniulm.omi.cloudiator.domain.OperatingSystemVersions;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystem.Builder;

/**
 * Created by daniel on 08.06.17.
 */
public class OperatingSystemConverter implements
    TwoWayConverter<CommonEntities.OperatingSystem, OperatingSystem> {

  private OperatingSystemFamilyConverter osFamilyConverter = new OperatingSystemFamilyConverter();
  private OperatingSystemArchitectureConverter osArchConverter = new OperatingSystemArchitectureConverter();

  @Override
  public CommonEntities.OperatingSystem applyBack(OperatingSystem operatingSystem) {
    if (operatingSystem == null) {
      return null;
    }
    final Builder builder = CommonEntities.OperatingSystem.newBuilder()
        .setOperatingSystemArchitecture(
            osArchConverter.applyBack(operatingSystem.operatingSystemArchitecture()))
        .setOperatingSystemFamily(
            osFamilyConverter.applyBack(operatingSystem.operatingSystemFamily()));

    if (operatingSystem.operatingSystemVersion().version() != null) {
      builder.setOperatingSystemVersion(CommonEntities.OperatingSystemVersion.newBuilder()
          .setVersion(operatingSystem.operatingSystemVersion().asInt()).build());
    }

    return builder.build();


  }

  @Override
  public OperatingSystem apply(CommonEntities.OperatingSystem operatingSystem) {
    if (operatingSystem == null) {
      return null;
    }

    OperatingSystemFamily operatingSystemFamily = osFamilyConverter
        .apply(operatingSystem.getOperatingSystemFamily());

    final OperatingSystemBuilder builder = OperatingSystemBuilder.newBuilder().architecture(
        osArchConverter.apply(operatingSystem.getOperatingSystemArchitecture()))
        .family(operatingSystemFamily);

    if (operatingSystem.hasOperatingSystemVersion()) {
      builder.version(OperatingSystemVersions
          .ofVersionAndFormat(operatingSystem.getOperatingSystemVersion().getVersion(),
              operatingSystemFamily.operatingSystemVersionFormat()));
    } else {
      builder.version(OperatingSystemVersions.unknown());
    }

    return builder.build();

  }

  private class OperatingSystemFamilyConverter implements
      TwoWayConverter<CommonEntities.OperatingSystemFamily, OperatingSystemFamily> {

    @Override
    public CommonEntities.OperatingSystemFamily applyBack(
        OperatingSystemFamily operatingSystemFamily) {
      switch (operatingSystemFamily) {
        case AIX:
          return CommonEntities.OperatingSystemFamily.AIX;
        case CEL:
          return CommonEntities.OperatingSystemFamily.CEL;
        case ESX:
          return CommonEntities.OperatingSystemFamily.ESX;
        case OEL:
          return CommonEntities.OperatingSystemFamily.OEL;
        case ARCH:
          return CommonEntities.OperatingSystemFamily.ARCH;
        case HPUX:
          return CommonEntities.OperatingSystemFamily.HPUX;
        case RHEL:
          return CommonEntities.OperatingSystemFamily.RHEL;
        case SUSE:
          return CommonEntities.OperatingSystemFamily.SUSE;
        case CENTOS:
          return CommonEntities.OperatingSystemFamily.CENTOS;
        case COREOS:
          return CommonEntities.OperatingSystemFamily.COREOS;
        case DARWIN:
          return CommonEntities.OperatingSystemFamily.DARWIN;
        case DEBIAN:
          return CommonEntities.OperatingSystemFamily.DEBIAN;
        case FEDORA:
          return CommonEntities.OperatingSystemFamily.FEDORA;
        case GENTOO:
          return CommonEntities.OperatingSystemFamily.GENTOO;
        case NETBSD:
          return CommonEntities.OperatingSystemFamily.NETBSD;
        case UBUNTU:
          return CommonEntities.OperatingSystemFamily.UBUNTU;
        case FREEBSD:
          return CommonEntities.OperatingSystemFamily.FREEBSD;
        case OPENBSD:
          return CommonEntities.OperatingSystemFamily.OPENBSD;
        case SOLARIS:
          return CommonEntities.OperatingSystemFamily.SOLARIS;
        case WINDOWS:
          return CommonEntities.OperatingSystemFamily.WINDOWS;
        case MANDRIVA:
          return CommonEntities.OperatingSystemFamily.MANDRIVA;
        case SLACKWARE:
          return CommonEntities.OperatingSystemFamily.SLACKWARE;
        case AMZN_LINUX:
          return CommonEntities.OperatingSystemFamily.AMZN_LINUX;
        case SCIENTIFIC:
          return CommonEntities.OperatingSystemFamily.SCIENTIFIC;
        case TURBOLINUX:
          return CommonEntities.OperatingSystemFamily.TURBOLINUX;
        case CLOUD_LINUX:
          return CommonEntities.OperatingSystemFamily.CLOUD_LINUX;
        case UNKNOWN:
          return CommonEntities.OperatingSystemFamily.UNKOWN_OS_FAMILY;
        default:
          throw new AssertionError(operatingSystemFamily + "is unknown.");
      }
    }

    @Override
    public OperatingSystemFamily apply(CommonEntities.OperatingSystemFamily operatingSystemFamily) {
      switch (operatingSystemFamily) {
        case AIX:
          return OperatingSystemFamily.AIX;
        case CEL:
          return OperatingSystemFamily.CEL;
        case ESX:
          return OperatingSystemFamily.ESX;
        case OEL:
          return OperatingSystemFamily.OEL;
        case ARCH:
          return OperatingSystemFamily.ARCH;
        case HPUX:
          return OperatingSystemFamily.HPUX;
        case RHEL:
          return OperatingSystemFamily.RHEL;
        case SUSE:
          return OperatingSystemFamily.SUSE;
        case CENTOS:
          return OperatingSystemFamily.CENTOS;
        case COREOS:
          return OperatingSystemFamily.COREOS;
        case DARWIN:
          return OperatingSystemFamily.DARWIN;
        case DEBIAN:
          return OperatingSystemFamily.DEBIAN;
        case FEDORA:
          return OperatingSystemFamily.FEDORA;
        case GENTOO:
          return OperatingSystemFamily.GENTOO;
        case NETBSD:
          return OperatingSystemFamily.NETBSD;
        case UBUNTU:
          return OperatingSystemFamily.UBUNTU;
        case FREEBSD:
          return OperatingSystemFamily.FREEBSD;
        case OPENBSD:
          return OperatingSystemFamily.OPENBSD;
        case SOLARIS:
          return OperatingSystemFamily.SOLARIS;
        case WINDOWS:
          return OperatingSystemFamily.WINDOWS;
        case MANDRIVA:
          return OperatingSystemFamily.MANDRIVA;
        case SLACKWARE:
          return OperatingSystemFamily.SLACKWARE;
        case AMZN_LINUX:
          return OperatingSystemFamily.AMZN_LINUX;
        case SCIENTIFIC:
          return OperatingSystemFamily.SCIENTIFIC;
        case TURBOLINUX:
          return OperatingSystemFamily.TURBOLINUX;
        case CLOUD_LINUX:
          return OperatingSystemFamily.CLOUD_LINUX;
        case UNKOWN_OS_FAMILY:
          return OperatingSystemFamily.UNKNOWN;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(operatingSystemFamily + "is unknown.");
      }
    }
  }

  private class OperatingSystemArchitectureConverter implements
      TwoWayConverter<CommonEntities.OperatingSystemArchitecture, OperatingSystemArchitecture> {

    @Override
    public CommonEntities.OperatingSystemArchitecture applyBack(
        OperatingSystemArchitecture operatingSystemArchitecture) {
      switch (operatingSystemArchitecture) {
        case I368:
          return CommonEntities.OperatingSystemArchitecture.I386;
        case AMD64:
          return CommonEntities.OperatingSystemArchitecture.AMD64;
        case ARM:
          return CommonEntities.OperatingSystemArchitecture.ARM;
        case UNKNOWN:
          return CommonEntities.OperatingSystemArchitecture.UNKOWN_OS_ARCH;
        default:
          throw new AssertionError(operatingSystemArchitecture + "is not known.");
      }
    }

    @Override
    public OperatingSystemArchitecture apply(
        CommonEntities.OperatingSystemArchitecture operatingSystemArchitecture) {
      switch (operatingSystemArchitecture) {
        case UNKOWN_OS_ARCH:
          return OperatingSystemArchitecture.UNKNOWN;
        case AMD64:
          return OperatingSystemArchitecture.AMD64;
        case I386:
          return OperatingSystemArchitecture.I368;
        case ARM:
          return OperatingSystemArchitecture.ARM;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(operatingSystemArchitecture + "is not known.");
      }
    }
  }
}
