package io.github.cloudiator.orchestration.discovery;

import de.uniulm.omi.cloudiator.sword.domain.*;
import org.cloudiator.messages.Iaas;

import java.util.function.Function;

/**
 * Created by daniel on 15.03.17.
 */
public class CloudAddedEventToCloud
    implements Function<org.cloudiator.messages.Cloud.CloudAdded, Cloud> {

    private ApiMessageToApi apiConverter = new ApiMessageToApi();
    private ConfigurationToConfiguration configurationConverter =
        new ConfigurationToConfiguration();
    private CredentialToCredential credentialConverter = new CredentialToCredential();

    @Override public Cloud apply(org.cloudiator.messages.Cloud.CloudAdded cloudAdded) {
        return CloudBuilder.newBuilder()
            .credentials(credentialConverter.apply(cloudAdded.getCloud().getCredential()))
            .api(apiConverter.apply(cloudAdded.getCloud().getApi()))
            .configuration(configurationConverter.apply(cloudAdded.getCloud().getConfiguration()))
            .endpoint(cloudAdded.getCloud().getEndpoint()).build();
    }

    private class ApiMessageToApi implements Function<Iaas.Api, Api> {

        @Override public Api apply(Iaas.Api api) {
            return ApiBuilder.newBuilder().providerName(api.getProviderName()).build();
        }
    }


    private class ConfigurationToConfiguration
        implements Function<Iaas.Configuration, Configuration> {

        @Override public Configuration apply(Iaas.Configuration configuration) {
            final PropertiesBuilder propertiesBuilder = PropertiesBuilder.newBuilder();
            configuration.getPropertyList().forEach(
                property -> propertiesBuilder.putProperty(property.getKey(), property.getValue()));
            return ConfigurationBuilder.newBuilder().nodeGroup(configuration.getNodeGroup())
                .properties(propertiesBuilder.build()).build();
        }
    }


    private class CredentialToCredential implements Function<Iaas.Credential, CloudCredential> {

        @Override public CloudCredential apply(Iaas.Credential credential) {
            return CredentialsBuilder.newBuilder().user(credential.getUser())
                .password(credential.getSecret()).build();
        }
    }

}
