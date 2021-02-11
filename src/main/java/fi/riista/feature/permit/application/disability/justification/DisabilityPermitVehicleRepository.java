package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;

import java.util.List;

public interface DisabilityPermitVehicleRepository extends BaseRepository<DisabilityPermitVehicle, Long> {

    List<DisabilityPermitVehicle> findByDisabilityPermitApplicationOrderById(final DisabilityPermitApplication application);
    void deleteByDisabilityPermitApplication(final DisabilityPermitApplication application);

}
