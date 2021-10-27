package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.metadata.ObservationFieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static fi.riista.feature.gamediary.observation.ObservationCategory.DEER_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.MOOSE_HUNTING;
import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;

@Component
public class MobileObservationService {

    private static final Logger LOG = LoggerFactory.getLogger(MobileObservationService.class);

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void fixObservationCategoryIfNeeded(final MobileObservationDTO dto) {
        if (!dto.getObservationSpecVersion().supportsCategory()) {
            dto.setObservationCategory(Boolean.TRUE.equals(dto.getWithinMooseHunting()) ? MOOSE_HUNTING : NORMAL);
            dto.setWithinMooseHunting(null);
        }
    }

    // Done on behalf of mobile app. Would not be needed if there was not a bug in at least Android app.
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void clearAmountWithinDeerHunting(final MobileObservationDTO dto) {
        // only clear amounts if observation is made within deer hunting. It is possible to observe
        // large carnivore within moose hunting and for those amount field needs to be present
        if (dto.getObservationCategory() == DEER_HUNTING && dto.getAmount() != null) {
            dto.setAmount(null);

            // Log illegal amount in order to get traces how often does this bug occur.
            LOG.debug("Amount not null while observation category is {}.", dto.getObservationCategory());
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void fixMooseCalfAmountIfNeeded(final Observation observation,
                                                   final ObservationSpecVersion dtoSpecVersion,
                                                   final ObservationFieldValidator validator) {

        if (!dtoSpecVersion.supportsMooselikeCalfAmount()
                && validator.getContextSensitiveFields().getMooselikeCalfAmount().nonNullValueRequired()
                && observation.getMooselikeCalfAmount() == null) {

            observation.setMooselikeCalfAmount(0);
        }
    }
}
