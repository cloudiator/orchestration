package io.github.cloudiator.messaging;

import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.sword.domain.ConfigurationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import java.util.function.BiConsumer;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Configuration.Builder;

/**
 * Created by daniel on 31.05.17.
 */
class ConfigurationMessageToConfiguration
    implements TwoWayConverter<IaasEntities.Configuration, Configuration> {

  public static final ConfigurationMessageToConfiguration INSTANCE = new ConfigurationMessageToConfiguration();

  private ConfigurationMessageToConfiguration() {
  }

  @Override
  public Configuration apply(IaasEntities.Configuration configuration) {
    final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
    configuration.getPropertyList().forEach(
        property -> propertiesBuilder.putProperty(property.getKey(), property.getValue()));
    return ConfigurationBuilder.newBuilder().nodeGroup(configuration.getNodeGroup())
        .properties(propertiesBuilder.build()).build();
  }

  @Override
  public IaasEntities.Configuration applyBack(Configuration configuration) {
    final Builder builder = IaasEntities.Configuration.newBuilder();
    configuration.properties().getProperties().forEach(new BiConsumer<String, String>() {
      @Override
      public void accept(String key, String value) {
        builder.addPropertyBuilder().setKey(key).setValue(value);
      }
    });
    builder.setNodeGroup(configuration.nodeGroup());
    return builder.build();
  }
}
