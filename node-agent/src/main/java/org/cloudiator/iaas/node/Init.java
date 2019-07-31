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

package org.cloudiator.iaas.node;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 31.05.17.
 */
public class Init {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(Init.class);

  private final PersistService persistService;

  @Inject
  Init(PersistService persistService) {
    LOGGER.info("Initializing");
    this.persistService = persistService;
    run();
  }

  private void run() {
    LOGGER.info("Starting persistence service.");
    startPersistService();
  }

  private void startPersistService() {
    persistService.start();
  }
}
