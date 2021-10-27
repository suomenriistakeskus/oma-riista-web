package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.util.F;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MobileHarvestService {

    public void assertHarvestDTOIsValid(final MobileHarvestDTO dto) {
        if (dto.getGeoLocation().getSource() == null) {
            throw new MessageExposableValidationException("geoLocation.source is null");
        }

        if (dto.getId() == null && dto.getMobileClientRefId() == null) {
            throw new MessageExposableValidationException("mobileClientRefId must not be null");
        }

        // Specimens are allowed to be null on creation.
        if (F.hasId(dto) && dto.getSpecimens() == null) {
            throw new MessageExposableValidationException("specimens must not be null");
        }
    }

    public void fixNonNullAntlerFieldsIfNotAdultMale(final MobileHarvestDTO dto) {
        Optional.ofNullable(dto.getSpecimens())
                .ifPresent(specimens -> specimens.forEach(specimen -> {
                    if (!specimen.isAdultMale()) {
                        specimen.clearAllAntlerFields();
                    }
                }));
    }

}
