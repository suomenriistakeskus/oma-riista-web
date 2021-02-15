package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;

import java.util.List;

public interface TransportedWeaponRepository extends BaseRepository<TransportedWeapon, Long> {

    List<TransportedWeapon> findByWeaponTransportationPermitApplicationOrderById(final WeaponTransportationPermitApplication application);

    void deleteByWeaponTransportationPermitApplication(final WeaponTransportationPermitApplication application);
}
