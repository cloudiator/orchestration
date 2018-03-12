package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.Api;

class ApiDomainRepository {

  private static final ApiConverter API_CONVERTER = new ApiConverter();
  private final ApiModelRepository apiModelRepository;

  @Inject
  ApiDomainRepository(ApiModelRepository apiModelRepository) {
    this.apiModelRepository = apiModelRepository;
  }

  public Api findByProviderName(String providerName) {
    checkNotNull(providerName, "providerName is null");
    return API_CONVERTER.apply(apiModelRepository.findByProviderName(providerName));
  }

  public void save(Api domain) {
    checkNotNull(domain, "domain is null");
    saveOrGet(domain);
  }

  ApiModel saveOrGet(Api domain) {
    checkNotNull(domain, "domain is null");
    ApiModel apiModel = apiModelRepository.findByProviderName(domain.providerName());
    if (apiModel == null) {
      apiModel = createModel(domain);
      apiModelRepository.save(apiModel);
    }
    return apiModel;
  }

  private ApiModel createModel(Api domain) {
    return new ApiModel(domain.providerName());
  }
}
