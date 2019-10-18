package fi.riista.feature.shootingtest.official;

import fi.riista.feature.organization.calendar.CalendarEvent;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static java.util.stream.Collectors.toList;

import fi.riista.feature.RuntimeEnvironmentUtil;

@Component
public class ShootingTestOfficialService {

    @Resource
    private ShootingTestOfficialRepository officialRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RuntimeEnvironmentUtil runtimeEnvironmentUtil;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<ShootingTestOfficial> assignOfficials(final ShootingTestOfficialsDTO dto,
                                                      final ShootingTestEvent event) {

        checkArgument(dto.getOccupationIds().size() >= 2, "Shooting test event must have at least 2 officials");

        final List<ShootingTestOfficial> officials = dto.getOccupationIds()
                .stream()
                .map(occupationId -> addNewOfficial(occupationId, event))
                .collect(toList());

        if (!runtimeEnvironmentUtil.isProductionEnvironment()) {
            final List<ShootingTestOfficial> responsibleOfficials = officials
                    .stream()
                    .filter(official -> official.getOccupation().getId().equals(dto.getResponsibleOccupationId()))
                    .collect(toList());
            checkArgument(responsibleOfficials.size() < 2, "Shooting test can have only 1 responsible official");

            ShootingTestOfficial responsibleOfficial;
            if (responsibleOfficials.size() == 1) {
                responsibleOfficial = responsibleOfficials.get(0);
            } else {
                responsibleOfficial = officials.get(0);
            }

            responsibleOfficial.setShootingTestResponsible(Boolean.TRUE);
        }

        return officials;
    }

    private ShootingTestOfficial addNewOfficial(final long occupationId, final ShootingTestEvent event) {
        final Occupation occupation = getAndAssertOccupation(occupationId, event.getCalendarEvent());
        return officialRepository.save(new ShootingTestOfficial(event, occupation));
    }

    private Occupation getAndAssertOccupation(final long occupationId, final CalendarEvent calendarEvent) {
        final Occupation occupation = occupationRepository.getOne(occupationId);
        final LocalDate eventDate = calendarEvent.getDateAsLocalDate();

        checkArgument(occupation.isActiveWithinPeriod(eventDate, eventDate), "Occupation must be active on event date");
        checkArgument(occupation.getOccupationType() == AMPUMAKOKEEN_VASTAANOTTAJA,
                "Occupation must be of type " + AMPUMAKOKEEN_VASTAANOTTAJA.name());
        checkArgument(Objects.equals(occupation.getOrganisation().getId(), calendarEvent.getOrganisation().getId()),
                "Occupation organisation should match to shootingTestEvent's organisation");

        return occupation;
    }
}
