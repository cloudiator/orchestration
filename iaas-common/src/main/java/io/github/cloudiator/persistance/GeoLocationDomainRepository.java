package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.sword.domain.GeoLocation;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class GeoLocationDomainRepository {

  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();
  private final GeoLocationModelRepository geoLocationModelRepository;

  @Inject
  public GeoLocationDomainRepository(
      GeoLocationModelRepository geoLocationModelRepository) {
    this.geoLocationModelRepository = geoLocationModelRepository;
  }

  public void save(GeoLocation domain) {
    saveAndGet(domain);
  }

  GeoLocationModel saveAndGet(GeoLocation domain) {
    final GeoLocationModel model = createModel(domain);
    geoLocationModelRepository.save(model);
    return model;
  }

  void update(GeoLocation domain, GeoLocationModel model) {
    updateModel(domain, model);
    geoLocationModelRepository.save(model);
  }

  private GeoLocationModel createModel(GeoLocation domain) {
    return new GeoLocationModel(domain.city().orElse(null), domain.country().orElse(null),
        domain.latitude().orElse(null),
        domain.longitude().orElse(null));
  }


  public List<GeoLocation> findAll() {
    return geoLocationModelRepository.findAll().stream().map(GEO_LOCATION_CONVERTER).collect(
        Collectors.toList());
  }


  private void updateModel(GeoLocation domain, GeoLocationModel model) {
    model.setCity(domain.city().orElse(null));
    model.setCountry(domain.country().orElse(null));
    model.setLocationLatitude(domain.latitude().orElse(null));
    model.setLocationLongitude(domain.longitude().orElse(null));

  }
}
