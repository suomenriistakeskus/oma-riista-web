package fi.riista.feature.shootingtest.official;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.joda.time.LocalDate;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShootingTestOfficialFeatureTest extends EmbeddedDatabaseTest implements ShootingTestFixtureMixin {

    @Resource
    private ShootingTestOfficialFeature feature;

    @Resource
    private ShootingTestOfficialRepository officialRepository;

    @Test
    public void testListAvailableOfficials() {
        // Different than current date for better testing.
        final LocalDate date = today().minusDays(3);

        withRhyAndCoordinator((rhy, coordinator) -> {

            final List<Occupation> qualifyingOfficials = createQualifyingOfficialOccupations(rhy, date);

            // Occupations below should not be included in the result list.
            createUnqualifyingOfficialOccupations(rhy, date);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final List<ShootingTestOfficialOccupationDTO> results =
                        feature.listAvailableOfficials(rhy.getId(), date);

                final Set<Long> resultOccupationIds =
                        results.stream().map(dto -> dto.getOccupationId()).collect(toSet());

                assertEquals(F.getUniqueIds(qualifyingOfficials), resultOccupationIds);
            });
        });
    }

    @Test
    public void testListQualifyingOfficials() {
        // Different than current date for better testing.
        final LocalDate date = today().minusDays(3);

        withRhyAndCoordinator((rhy, coordinator) -> {

            final ShootingTestEvent event = model().newShootingTestEvent(rhy, date);

            final ShootingTestOfficial official1 = createOfficial(model().newPerson(), event);
            final ShootingTestOfficial official2 = createOfficial(model().newPerson(), event);

            final List<Occupation> qualifyingOfficials = F.concat(
                    createQualifyingOfficialOccupations(rhy, date),
                    asList(official1.getOccupation(), official2.getOccupation()));

            // Occupations below should not be included in the result list.
            createUnqualifyingOfficialOccupations(rhy, date);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final List<ShootingTestOfficialOccupationDTO> results = feature.listQualifyingOfficials(event.getId());

                final Set<Long> resultOccupationIds =
                        results.stream().map(dto -> dto.getOccupationId()).collect(toSet());

                assertEquals(F.getUniqueIds(qualifyingOfficials), resultOccupationIds);
            });
        });
    }

    @Test
    public void testListAssignedOfficials() {
        // Different than current date for better testing.
        final LocalDate date = today().minusDays(3);

        withRhyAndCoordinator((rhy, coordinator) -> {

            final ShootingTestEvent event = model().newShootingTestEvent(rhy, date);

            final ShootingTestOfficial official1 = createOfficial(model().newPerson(), event);
            final ShootingTestOfficial official2 = createOfficial(model().newPerson(), event);
            official2.setShootingTestResponsible(Boolean.TRUE);

            final List<Occupation> assignedOfficials = asList(official1.getOccupation(), official2.getOccupation());

            // Occupations below should not be included in the result list.
            createQualifyingOfficialOccupations(rhy, date);
            createUnqualifyingOfficialOccupations(rhy, date);

            onSavedAndAuthenticated(createUser(coordinator), () -> {

                final List<ShootingTestOfficialOccupationDTO> results = feature.listAssignedOfficials(event.getId());

                final Set<Long> resultOccupationIds =
                        results.stream().map(dto -> dto.getOccupationId()).collect(toSet());

                assertEquals(F.getUniqueIds(assignedOfficials), resultOccupationIds);

                final ShootingTestOfficialOccupationDTO responsibleDTO = results
                        .stream()
                        .filter(dto -> Boolean.TRUE.equals(dto.getShootingTestResponsible()))
                        .findAny()
                        .orElse(null);
                assertNotNull(responsibleDTO);
                assertEquals(responsibleDTO.getOccupationId(), official2.getOccupation().getId().longValue());
            });
        });
    }

    @Test
    public void testAssignResponsibleOfficialByOrder() {
        // Different than current date for better testing.
        final LocalDate date = today().minusDays(3);

        withRhyAndCoordinator((rhy, coordinator) -> {

            final ShootingTestEvent event = model().newShootingTestEvent(rhy, date);

            final LocalDate beginDate = today().minusDays(5);
            final Occupation officialOccupation1 = newOfficialOccupation(rhy, beginDate, today());
            final Occupation officialOccupation2 = newOfficialOccupation(rhy, beginDate, today());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final ShootingTestOfficialsDTO officialsDTO = new ShootingTestOfficialsDTO();
                officialsDTO.setShootingTestEventId(event.getId());
                final List<Long> occupationIds = asList(officialOccupation1.getId(), officialOccupation2.getId());
                officialsDTO.setOccupationIds(occupationIds);

                feature.assignOfficials(officialsDTO);

                final ShootingTestOfficial responsibleOfficial = officialRepository.findByShootingTestEvent(event)
                        .stream()
                        .filter(official -> Boolean.TRUE.equals(official.getShootingTestResponsible()))
                        .findAny()
                        .orElse(null);
                assertNotNull(responsibleOfficial);
                assertEquals(responsibleOfficial.getOccupation().getId(), officialOccupation1.getId());
            });
        });
    }

    @Test
    public void testAssignResponsibleOfficial() {
        // Different than current date for better testing.
        final LocalDate date = today().minusDays(3);

        withRhyAndCoordinator((rhy, coordinator) -> {

            final ShootingTestEvent event = model().newShootingTestEvent(rhy, date);

            final LocalDate beginDate = today().minusDays(5);
            final Occupation officialOccupation1 = newOfficialOccupation(rhy, beginDate, today());
            final Occupation officialOccupation2 = newOfficialOccupation(rhy, beginDate, today());

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final ShootingTestOfficialsDTO officialsDTO = new ShootingTestOfficialsDTO();
                officialsDTO.setShootingTestEventId(event.getId());
                final List<Long> occupationIds = asList(officialOccupation1.getId(), officialOccupation2.getId());
                officialsDTO.setOccupationIds(occupationIds);
                officialsDTO.setResponsibleOccupationId(officialOccupation2.getId());

                feature.assignOfficials(officialsDTO);

                final ShootingTestOfficial responsibleOfficial = officialRepository.findByShootingTestEvent(event)
                        .stream()
                        .filter(official -> Boolean.TRUE.equals(official.getShootingTestResponsible()))
                        .findAny()
                        .orElse(null);
                assertNotNull(responsibleOfficial);
                assertEquals(responsibleOfficial.getOccupation().getId(), officialOccupation2.getId());
            });
        });
    }

    private List<Occupation> createQualifyingOfficialOccupations(final Riistanhoitoyhdistys rhy,
                                                                 final LocalDate eventDate) {
        return asList(
                newOfficialOccupation(rhy, eventDate, eventDate),
                newOfficialOccupation(rhy, eventDate, null),
                newOfficialOccupation(rhy, null, eventDate),
                newOfficialOccupation(rhy, eventDate.minusDays(1), null),
                newOfficialOccupation(rhy, null, eventDate.plusDays(1)),
                newOfficialOccupation(rhy, null, null));
    }

    private List<Occupation> createUnqualifyingOfficialOccupations(final Riistanhoitoyhdistys rhy,
                                                                   final LocalDate eventDate) {
        return asList(
                newOfficialOccupation(rhy, eventDate.plusDays(1), eventDate.plusDays(1)),
                newOfficialOccupation(rhy, eventDate.plusDays(1), null),
                newOfficialOccupation(rhy, eventDate.minusDays(1), eventDate.minusDays(1)),
                newOfficialOccupation(rhy, null, eventDate.minusDays(1)),
                newOfficialOccupation(model().newRiistanhoitoyhdistys(), null, null),
                model().newOccupation(rhy, model().newPerson(), TOIMINNANOHJAAJA, null, null));
    }

    private Occupation newOfficialOccupation(final Riistanhoitoyhdistys rhy,
                                             final LocalDate beginDate,
                                             final LocalDate endDate) {
     
        return model().newOccupation(rhy, model().newPerson(), AMPUMAKOKEEN_VASTAANOTTAJA, beginDate, endDate);
    }
}
