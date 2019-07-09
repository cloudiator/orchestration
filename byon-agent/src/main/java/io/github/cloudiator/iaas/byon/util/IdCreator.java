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

import io.github.cloudiator.domain.NodeProperties;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.cloudiator.messages.Byon.ByonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdCreator {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(IdCreator.class);

  // do not instantiate
  private IdCreator(){
  }

  public final static String createId(NodeProperties props) {
    String result = "";
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      DigestRessource.digestHardware(md, props);
      DigestRessource.digestOS(md, props.operatingSystem());
      DigestRessource.digestLocation(md, props.geoLocation().orElse(null));
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
}
