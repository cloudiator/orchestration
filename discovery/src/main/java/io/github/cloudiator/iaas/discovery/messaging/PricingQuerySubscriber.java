package io.github.cloudiator.iaas.discovery.messaging;

import io.github.cloudiator.messaging.PricingMessageToPricingConverter;
import io.github.cloudiator.persistance.PricingDomainRepository;
import org.cloudiator.messages.General;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cloudiator.messages.Pricing.PricingQueryRequest;
import org.cloudiator.messages.Pricing.PricingQueryResponse;

import com.google.inject.Inject;

import java.util.stream.Collectors;

public class PricingQuerySubscriber implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PricingQuerySubscriber.class);
    private static final PricingMessageToPricingConverter PRICING_CONVERTER = PricingMessageToPricingConverter.INSTANCE;
    private final MessageInterface messageInterface;
    private final PricingDomainRepository pricingDomainRepository;

    @Inject
    public PricingQuerySubscriber(MessageInterface messageInterface, PricingDomainRepository pricingDomainRepository) {
        this.messageInterface = messageInterface;
        this.pricingDomainRepository = pricingDomainRepository;
    }

    @Override
    public void run() {
        Subscription subscription = messageInterface.subscribe(PricingQueryRequest.class, PricingQueryRequest.parser(),
                (requestId, pricingQueryRequest)  -> {

                    try {
                        decideAndReply(requestId, pricingQueryRequest);
                    } catch (Exception e) {
                        LOGGER.error(String
                                .format("Caught exception %s during execution of %s", e.getMessage(), this), e);
                    }
                });
    }

    private void decideAndReply(String requestId, PricingQueryRequest request) {
        if (request.getCloudAPIProviderName().isEmpty()
            || request.getUserId().isEmpty()) {
            replyErrorNotAllParamsSupplied(requestId);
            return;
        }

        replyForAllParams(requestId, request);
    }

    private void replyErrorNotAllParamsSupplied(String requestId) {
        messageInterface.reply(PricingQueryResponse.class, requestId,
                General.Error.newBuilder().setCode(500).setMessage("All request parameters have to be supplied.")
                        .build());
    }

    private void replyForAllParams(String requestId, PricingQueryRequest request) {
        try {
            PricingQueryResponse pricingQueryResponse = PricingQueryResponse.newBuilder()
                    .addAllPrices(
                            pricingDomainRepository.getPrices(request.getCloudAPIProviderName()).stream().map(
                                    PRICING_CONVERTER::apply).collect(Collectors.toList())).build();
            messageInterface.reply(requestId, pricingQueryResponse);
        } catch (Exception e) {
            /*messageInterface.reply(PricingQueryResponse.class, requestId,
                    General.Error.newBuilder().setCode(500).setMessage(String
                            .format("Error while retrieving the price: %s", e.getMessage()))
                            .build());
            return;*/
            LOGGER.error(e.getMessage());
        }
    }
}
