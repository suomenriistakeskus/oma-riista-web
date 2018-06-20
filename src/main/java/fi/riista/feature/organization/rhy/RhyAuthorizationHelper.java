package fi.riista.feature.organization.rhy;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestOfficialRepository;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static java.util.stream.Collectors.toSet;

@Component
public class RhyAuthorizationHelper {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private ShootingTestOfficialRepository shootingTestOfficialRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean isPermittedAsAssignedOfficial(final ShootingTestEvent event, final UserInfo userInfo) {
        return event != null && event.hasOccurredWithinLastWeek() && userAuthorizationHelper.getPerson(userInfo)
                .map(person -> {
                    return shootingTestOfficialRepository.findByShootingTestEvent(event)
                            .stream()
                            .anyMatch(official -> official.getOccupation().getPerson().getId().equals(person.getId()));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void collectAllRhyRoles(@Nullable final Organisation organisation,
                                   @Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final UserInfo userInfo) {

        Optional.ofNullable(organisation)
                .flatMap(org -> userAuthorizationHelper.getPerson(userInfo))
                .ifPresent(person -> {

                    final Set<OccupationType> rhyRoles = occupationRepository
                            .findActiveByOrganisationAndPerson(organisation, person)
                            .stream()
                            .map(Occupation::getOccupationType)
                            .collect(toSet());

                    collector.addAuthorizationRole(TOIMINNANOHJAAJA, () -> rhyRoles.contains(TOIMINNANOHJAAJA));
                    collector.addAuthorizationRole(SRVA_YHTEYSHENKILO, () -> rhyRoles.contains(SRVA_YHTEYSHENKILO));
                    collector.addAuthorizationRole(AMPUMAKOKEEN_VASTAANOTTAJA, () -> rhyRoles.contains(AMPUMAKOKEEN_VASTAANOTTAJA));
                });
    }
}
