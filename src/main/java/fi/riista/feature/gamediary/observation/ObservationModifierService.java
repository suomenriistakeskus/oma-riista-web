package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class ObservationModifierService {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Transactional(readOnly = true)
    public ObservationModifierInfo getObservationCreatorInfo(@Nonnull final LocalDate observationDate) {
        requireNonNull(observationDate);

        final SystemUser activeUser = activeUserService.requireActiveUser();

        final boolean isAuthorOrObserver = !activeUser.isModeratorOrAdmin();

        final boolean isCarnivoreContactPersonInAnyRhy =
                isAssociatedWithCarnivoreContactPersonInAnyRhy(activeUser, observationDate);

        return ObservationModifierInfo.builder()
                .withActiveUser(activeUser)
                .withAuthorOrObserver(isAuthorOrObserver)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(isCarnivoreContactPersonInAnyRhy)
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public ObservationModifierInfo getObservationModifierInfo(@Nonnull final Observation observation,
                                                              @Nonnull final LocalDate updatedObservationDate) {
        requireNonNull(observation);
        requireNonNull(updatedObservationDate);

        final SystemUser activeUser = activeUserService.requireActiveUser();

        final boolean isAuthorOrObserver;

        if (activeUser.isModeratorOrAdmin()) {
            isAuthorOrObserver = false;
        } else {
            final Person modifierPerson = activeUser.getPerson();

            if (modifierPerson == null) {
                // This should never occur.
                throw new IllegalStateException("Person does not exist for user " + activeUser.getUsername());
            }

            isAuthorOrObserver = observation.isAuthorOrActor(modifierPerson);
        }

        final boolean isCarnivoreContactPersonInAnyRhy =
                isAssociatedWithCarnivoreContactPersonInAnyRhy(activeUser, updatedObservationDate);

        return ObservationModifierInfo.builder()
                .withActiveUser(activeUser)
                .withAuthorOrObserver(isAuthorOrObserver)
                .withCarnivoreAuthorityInAnyRhyAtObservationDate(isCarnivoreContactPersonInAnyRhy)
                .build();
    }

    private boolean isAssociatedWithCarnivoreContactPersonInAnyRhy(final SystemUser user, final LocalDate date) {
        return !user.isModeratorOrAdmin() && userAuthorizationHelper.isCarnivoreContactPersonAnywhere(date);
    }
}
