package io.github.cloudiator.persistance;

import javax.annotation.Nullable;

/**
 * Created by daniel on 02.06.17.
 */
interface HardwareOfferModelRepository extends ModelRepository<HardwareOfferModel> {

  HardwareOfferModel findByCpuRamDisk(int numberOfCores, long mbOfRam, @Nullable Double diskSpace);


}
