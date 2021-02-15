package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;

import java.util.List;

public interface DisabilityPermitHuntingTypeInfoRepository extends BaseRepository<DisabilityPermitHuntingTypeInfo, Long> {

    List<DisabilityPermitHuntingTypeInfo> findByDisabilityPermitApplicationOrderById(final DisabilityPermitApplication application);
    void deleteByDisabilityPermitApplication(final DisabilityPermitApplication application);

}
