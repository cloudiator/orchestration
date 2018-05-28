package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import java.util.Collection;
import java.util.Collections;

public class IpAddressDomainRepository {

  private final IpGroupModelRepository ipGroupModelRepository;

  @Inject
  public IpAddressDomainRepository(
      IpGroupModelRepository ipGroupModelRepository) {
    this.ipGroupModelRepository = ipGroupModelRepository;
  }

  IpGroupModel saveAndGet(Collection<IpAddress> ipAddresses) {
    return create(ipAddresses);
  }

  IpGroupModel saveAndGet(IpAddress ipAddress) {
    return saveAndGet(Collections.singleton(ipAddress));
  }

  private IpGroupModel create(Collection<IpAddress> ipAddresses) {

    IpGroupModel ipGroupModel = new IpGroupModel();
    ipGroupModelRepository.save(ipGroupModel);

    for (IpAddress ipAddress : ipAddresses) {
      IpAddressModel ipAddressModel = new IpAddressModel(ipGroupModel, ipAddress.ip(),
          ipAddress.version(), ipAddress.type());
      ipGroupModel.addIpAddress(ipAddressModel);
    }

    ipGroupModelRepository.save(ipGroupModel);

    return ipGroupModel;
  }


}
