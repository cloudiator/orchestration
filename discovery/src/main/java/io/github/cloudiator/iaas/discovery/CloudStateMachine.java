package io.github.cloudiator.iaas.discovery;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import de.uniulm.omi.cloudiator.sword.multicloud.service.CloudRegistry;
import de.uniulm.omi.cloudiator.util.function.ThrowingFunction;
import de.uniulm.omi.cloudiator.util.stateMachine.State;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachine;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineBuilder;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineHook;
import de.uniulm.omi.cloudiator.util.stateMachine.StateMachineImpl;
import de.uniulm.omi.cloudiator.util.stateMachine.TransitionBuilder;
import io.github.cloudiator.domain.CloudState;
import io.github.cloudiator.domain.ExtendedCloud;
import io.github.cloudiator.domain.ExtendedCloudBuilder;
import io.github.cloudiator.domain.ExtendedCloudImpl;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter;
import io.github.cloudiator.messaging.CloudMessageToCloudConverter.CloudStateConverter;
import io.github.cloudiator.persistance.CloudDomainRepository;
import java.util.concurrent.ExecutionException;
import org.cloudiator.messages.Cloud.CloudEvent;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStateMachine implements StateMachine<ExtendedCloud> {

  private final StateMachine<ExtendedCloud> stateMachine;
  private final CloudDomainRepository cloudDomainRepository;
  private final CloudRegistry cloudRegistry;
  private final CloudService cloudService;

  private static final Logger LOGGER = LoggerFactory.getLogger(CloudStateMachine.class);

  @Inject
  public CloudStateMachine(
      CloudDomainRepository cloudDomainRepository,
      CloudRegistry cloudRegistry, CloudService cloudService) {
    this.cloudDomainRepository = cloudDomainRepository;
    this.cloudRegistry = cloudRegistry;
    this.cloudService = cloudService;

    stateMachine = StateMachineBuilder.<ExtendedCloud>builder().errorState(CloudState.ERROR)
        .addTransition(
            TransitionBuilder.<ExtendedCloud>newBuilder().from(CloudState.NEW).to(CloudState.OK)
                .action(newToOk())
                .build())
        .addTransition(
            TransitionBuilder.<ExtendedCloud>newBuilder().from(CloudState.OK).to(CloudState.DELETED)
                .action(delete())
                .build())
        .addHook(new StateMachineHook<ExtendedCloud>() {
          @Override
          public void pre(ExtendedCloud cloud, State to) {
            //intentionally left empty
          }

          @Override
          public void post(State from, ExtendedCloud cloud) {
            final CloudEvent cloudEvent = CloudEvent.newBuilder().setCloud(
                CloudMessageToCloudConverter.INSTANCE.applyBack(cloud))
                .setFrom(CloudStateConverter.INSTANCE.applyBack(
                    (CloudState) from)).setTo(CloudStateConverter.INSTANCE.applyBack(cloud.state()))
                .build();
            LOGGER.debug(String.format("Executing post hook to announce cloud changed event %s."),
                cloudEvent);
            CloudStateMachine.this.cloudService.announceEvent(cloudEvent);
          }
        })
        .build();

  }

  @Transactional
  void save(ExtendedCloud extendedCloud) {
    cloudDomainRepository.save(extendedCloud);
  }

  @Transactional
  void delete(ExtendedCloud extendedCloud) {
    cloudDomainRepository.delete(extendedCloud.id(), extendedCloud.userId());
  }

  @Override
  public StateMachineImpl<ExtendedCloud> apply(ExtendedCloud object, State to)
      throws ExecutionException {
    return stateMachine.apply(object, to);
  }

  private ThrowingFunction<ExtendedCloud, ExtendedCloud> newToOk() {

    return extendedCloud -> {

      final ExtendedCloudImpl ok = ExtendedCloudBuilder.of(extendedCloud).state(CloudState.OK)
          .build();
      save(ok);
      cloudRegistry.register(ok);

      return ok;
    };

  }

  private ThrowingFunction<ExtendedCloud, ExtendedCloud> delete() {

    return extendedCloud -> {

      final ExtendedCloudImpl ok = ExtendedCloudBuilder.of(extendedCloud)
          .state(CloudState.DELETED)
          .build();

      delete(extendedCloud);

      cloudRegistry.unregister(extendedCloud.id());

      return ok;
    };

  }
}
