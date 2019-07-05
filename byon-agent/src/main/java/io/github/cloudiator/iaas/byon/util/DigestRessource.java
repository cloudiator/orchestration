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

package io.github.cloudiator.iaas.byon.util;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import io.github.cloudiator.domain.NodeProperties;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Optional;

public class DigestRessource {

  // Do not initiate
  private DigestRessource() {
  }

  public static final void digestHardware(MessageDigest md, NodeProperties props)
      throws UnsupportedEncodingException {
    md.update(String.valueOf(props.numberOfCores()).getBytes("UTF-8"));
    md.update(String.valueOf(props.memory()).getBytes("UTF-8"));
    md.update(String.valueOf(props.disk()).getBytes("UTF-8"));
  }

  public static final void digestLocation(MessageDigest md, GeoLocation loc)
      throws UnsupportedEncodingException {
    if(loc == null) {
      return;
    }

    if (loc.city() != null) {
      md.update(String.valueOf(loc.city()).getBytes("UTF-8"));
    }
    if (loc.country() != null) {
      md.update(String.valueOf(loc.country()).getBytes("UTF-8"));
    }
    Integer latitude = loc.latitude().orElse(null).intValue();
    if (latitude != null) {
      md.update(String.valueOf(Integer.toString(latitude)).getBytes("UTF-8"));
    }
    Integer longitude = loc.longitude().orElse(null).intValue();
    if (longitude != null) {
      md.update(String.valueOf(Integer.toString(longitude)).getBytes("UTF-8"));
    }
  }

  public static final void digestOS(MessageDigest md, Optional<OperatingSystem> operatingSystem)
      throws UnsupportedEncodingException {
    if(operatingSystem == null) {
      return;
    }

    md.update(String.valueOf(operatingSystem.toString()).getBytes("UTF-8"));
  }
}
