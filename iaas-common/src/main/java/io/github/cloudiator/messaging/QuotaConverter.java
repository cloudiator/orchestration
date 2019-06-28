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

package io.github.cloudiator.messaging;

import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import de.uniulm.omi.cloudiator.sword.domain.Quotas;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import java.math.BigDecimal;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.OfferType;

public class QuotaConverter implements TwoWayConverter<IaasEntities.Quota, Quota> {

  public static final QuotaConverter INSTANCE = new QuotaConverter();

  private QuotaConverter() {
  }

  @Override
  public IaasEntities.Quota applyBack(Quota quota) {

    if (quota instanceof AttributeQuota) {
      return IaasEntities.Quota.newBuilder()
          .setLocationId(Strings.nullToEmpty(quota.locationId().orElse(null)))
          .setRemaining(quota.remaining().toString())
          .setAttributeQuota(IaasEntities.AttributeQuota.newBuilder().setAttribute(
              IaasEntities.Attribute.valueOf(((AttributeQuota) quota).attribute().name())).build())
          .build();
    } else if (quota instanceof OfferQuota) {
      return IaasEntities.Quota.newBuilder()
          .setLocationId(Strings.nullToEmpty(quota.locationId().orElse(null)))
          .setRemaining(quota.remaining().toString())
          .setOfferQuota(IaasEntities.OfferQuota.newBuilder()
              .setOfferType(OfferType.valueOf(((OfferQuota) quota).type().name()))
              .setId(((OfferQuota) quota).id()).build())
          .build();
    } else {
      throw new AssertionError("Unknown type of quota " + quota.getClass().getName());
    }
  }

  @Override
  public Quota apply(IaasEntities.Quota quota) {

    switch (quota.getQuotaCase()) {
      case OFFERQUOTA:
        return Quotas.offerQuota(quota.getOfferQuota().getId(),
            OfferQuota.OfferType.valueOf(quota.getOfferQuota().getOfferType().name()),
            new BigDecimal(quota.getRemaining()),
            Strings.emptyToNull(quota.getLocationId()));
      case ATTRIBUTEQUOTA:
        return Quotas.attributeQuota(
            AttributeQuota.Attribute.valueOf(quota.getAttributeQuota().getAttribute().name()),
            new BigDecimal(quota.getRemaining()), Strings.emptyToNull(quota.getLocationId()));
      case QUOTA_NOT_SET:
      default:
        throw new AssertionError("Illegal quota case " + quota.getQuotaCase());
    }
  }
}
