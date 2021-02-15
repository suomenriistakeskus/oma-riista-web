package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;

import java.util.List;

public interface WeaponTransportationVehicleRepository extends BaseRepository<WeaponTransportationVehicle, Long> {

    List<WeaponTransportationVehicle> findByWeaponTransportationPermitApplicationOrderById(final WeaponTransportationPermitApplication application);
    void deleteByWeaponTransportationPermitApplication(final WeaponTransportationPermitApplication application);

}
