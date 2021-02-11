package fi.riista.feature.permit.application.weapontransportation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;

public interface WeaponTransportationPermitApplicationRepository extends BaseRepository<WeaponTransportationPermitApplication, Long> {

    WeaponTransportationPermitApplication findByHarvestPermitApplication(final HarvestPermitApplication application);

    void deleteByHarvestPermitApplication(final HarvestPermitApplication application);
}
