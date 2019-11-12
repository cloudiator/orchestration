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

package io.github.cloudiator.domain;

import de.uniulm.omi.cloudiator.domain.OperatingSystem;
import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonIdCreator {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ByonIdCreator.class);

  // do not instantiate
  private ByonIdCreator(){
  }

  public final static String createId(int numbOfCores, long memory, double disk
      , OperatingSystem os, GeoLocation geoLocation) {
    NodePropertiesBuilder builder = NodePropertiesBuilder.newBuilder()
        .numberOfCores(numbOfCores)
        .memory(memory)
        .disk(disk)
        .os(os)
        .geoLocation(geoLocation);

    return createId(builder.build());
  }


  public final static String createId(NodeProperties props) {
    String result = "";
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      ByonDigestRessource.digestHardware(md, props);
      ByonDigestRessource.digestOS(md, props.operatingSystem());
      ByonDigestRessource.digestLocation(md, props.geoLocation().orElse(null));
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1, digest);
      result = bigInt.toString(16);
    } catch (NoSuchAlgorithmException ex) {
      LOGGER.error("cannot digest ressource due to failing algorithm, using random integer ", ex);
      result = UUID.randomUUID().toString();
    } catch (UnsupportedEncodingException ex) {
      LOGGER.error("cannot digest ressource due to failing encoding, using random integer ", ex);
      result = UUID.randomUUID().toString();
    }
    return result;
  }

  public final static String generateId() {
    return UUID.randomUUID().toString();
  }
}
