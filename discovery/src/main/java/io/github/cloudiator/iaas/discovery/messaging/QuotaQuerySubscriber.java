/*
 * Copyright (c) 2014-2019 University of Ulm
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

package io.github.cloudiator.iaas.discovery.messaging;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.sword.service.ComputeService;
import io.github.cloudiator.messaging.QuotaConverter;
import org.cloudiator.messages.Cloud.QuotaQueryRequest;
import org.cloudiator.messages.Cloud.QuotaQueryResponse;
import org.cloudiator.messages.Cloud.QuotaQueryResponse.Builder;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotaQuerySubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuotaQuerySubscriber.class);
  private final MessageInterface messageInterface;
  private final ComputeService computeService;
  private final CloudRegistry cloudRegistry;
  private static final QuotaConverter QUOTA_CONVERTER = QuotaConverter.INSTANCE;

  @Inject
  public QuotaQuerySubscriber(MessageInterface messageInterface,
      ComputeService computeService,
      CloudRegistry cloudRegistry) {
    this.messageInterface = messageInterface;
    this.computeService = computeService;
    this.cloudRegistry = cloudRegistry;
  }

  @Override
  public void run() {
    messageInterface.subscribe(QuotaQueryRequest.class, QuotaQueryRequest.parser(),
        new MessageCallback<QuotaQueryRequest>() {
          @Override
          public void accept(String id, QuotaQueryRequest content) {

            try {
              final String userId = content.getUserId();

              if (Strings.isNullOrEmpty(userId)) {
                messageInterface.reply(QuotaQueryResponse.class, id,
                    Error.newBuilder().setCode(400).setMessage("UserId is empty or null").build());
                return;
              }

              QuotaSet quotaSet;
              if (!computeService.quotaExtension().isPresent()) {
                quotaSet = QuotaSet.EMPTY;
              } else {
                quotaSet = computeService.quotaExtension().get().quotas();
              }

              final Builder responseBuilder = QuotaQueryResponse.newBuilder();
              quotaSet.quotaSet().stream().map(QUOTA_CONVERTER::applyBack)
                  .forEach(responseBuilder::addQuotas);

              messageInterface.reply(id, responseBuilder.build());
            } catch (Exception e) {
              LOGGER.error("Unexpected exception while replying to query request.", e);
              messageInterface.reply(QuotaQueryResponse.class, id,
                  Error.newBuilder().setCode(500).setMessage("Unexpected error " + e.getMessage())
                      .build());
            }


          }
        });
  }
}
