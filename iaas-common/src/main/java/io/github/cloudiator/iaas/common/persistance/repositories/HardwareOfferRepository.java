package io.github.cloudiator.iaas.common.persistance.repositories;

import de.uniulm.omi.cloudiator.persistance.repositories.ModelRepository;
import io.github.cloudiator.iaas.common.persistance.entities.HardwareOffer;
import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
public interface HardwareOfferRepository extends ModelRepository<HardwareOffer> {

  HardwareOffer findByCpuRamDisk(int numberOfCores, long mbOfRam, @Nullable Float diskSpace);


}
