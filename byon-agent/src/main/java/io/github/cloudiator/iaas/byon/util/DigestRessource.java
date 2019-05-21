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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import org.cloudiator.messages.NodeEntities.NodeProperties;
import org.cloudiator.messages.entities.IaasEntities.GeoLocation;

public class DigestRessource {

  // Do not initiate
  private DigestRessource() {
  }

  public static final void digestHardware(MessageDigest md, NodeProperties prop)
      throws UnsupportedEncodingException {
    md.update(String.valueOf(prop.getNumberOfCores()).getBytes("UTF-8"));
    md.update(String.valueOf(prop.getMemory()).getBytes("UTF-8"));
    md.update(String.valueOf(prop.getDisk()).getBytes("UTF-8"));
  }

  public static final void digestLocation(MessageDigest md, GeoLocation loc)
      throws UnsupportedEncodingException {
    if (loc.getCity() != null) {
      md.update(String.valueOf(loc.getCity()).getBytes("UTF-8"));
    }
    if (loc.getCountry() != null) {
      md.update(String.valueOf(loc.getCountry()).getBytes("UTF-8"));
    }
    Integer latitude = (int) loc.getLatitude();
    if (latitude != null) {
      md.update(String.valueOf(Integer.toString(latitude)).getBytes("UTF-8"));
    }
    Integer longitude = (int) loc.getLongitude();
    if (longitude != null) {
      md.update(String.valueOf(Integer.toString(longitude)).getBytes("UTF-8"));
    }
  }
}
