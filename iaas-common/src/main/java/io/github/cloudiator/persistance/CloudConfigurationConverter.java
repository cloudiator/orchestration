package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.Configuration;
import de.uniulm.omi.cloudiator.sword.domain.ConfigurationBuilder;
import de.uniulm.omi.cloudiator.sword.domain.Properties;
import de.uniulm.omi.cloudiator.sword.domain.PropertiesBuilder;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;

class CloudConfigurationConverter implements
    OneWayConverter<CloudConfigurationModel, Configuration> {

  @Nullable
  @Override
  public Configuration apply(@Nullable CloudConfigurationModel cloudConfigurationModel) {

    if (cloudConfigurationModel == null) {
      return null;
    }

    final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
    cloudConfigurationModel.getProperties().forEach(
        propertyModel -> propertiesBuilder
            .putProperty(propertyModel.getKey(), propertyModel.getValue()));
    Properties properties = propertiesBuilder.build();

    return ConfigurationBuilder.newBuilder()
        .nodeGroup(cloudConfigurationModel.getNodeGroup()).properties(properties)
        .build();
  }
}
