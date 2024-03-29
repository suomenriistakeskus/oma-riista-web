package fi.riista.feature.organization.rhy.gamedamageinspection;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.RhyEventTimeException;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysDTO;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.util.DateUtil.toLocalDateNullSafe;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class GameDamageInspectionEventCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private GameDamageInspectionEventCrudFeature feature;

    @Resource
    private GameDamageInspectionEventRepository repository;

    private GameDamageInspectionEventDTO createDTO(final Riistanhoitoyhdistys rhy) {
        return createDTO(rhy, today());
    }

    private GameDamageInspectionEventDTO createDTO(final Riistanhoitoyhdistys rhy, final LocalDate date) {
        final LocalTime beginTime = new LocalTime(12, 0);
        final LocalTime endTime = new LocalTime(13, 0);

        return createDTO(rhy, date, beginTime, endTime);
    }

    private GameDamageInspectionEventDTO createDTO(final Riistanhoitoyhdistys rhy,
                                                   final LocalDate date,
                                                   final LocalTime beginTime,
                                                   final LocalTime endTime) {
        final Person inspector = model().newPerson(rhy);
        model().newOccupation(rhy, inspector, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, date, date);
        persistInNewTransaction();

        final GameDamageInspectionEventDTO dto = new GameDamageInspectionEventDTO();

        dto.setRhy(RiistanhoitoyhdistysDTO.create(rhy));
        dto.setGameSpeciesCode(OFFICIAL_CODE_MOOSE);

        dto.setInspector(PersonContactInfoDTO.create(inspector));

        dto.setGeoLocation(geoLocation());

        dto.setDate(date);
        dto.setBeginTime(beginTime);
        dto.setEndTime(endTime);

        dto.setDescription("Description");
        dto.setHourlyExpensesUnit(BigDecimal.valueOf(1));
        dto.setDailyAllowance(BigDecimal.valueOf(4));

        final GameDamageInspectionKmExpenseDTO expenseDTO = new GameDamageInspectionKmExpenseDTO();
        expenseDTO.setKilometers(2);
        expenseDTO.setExpenseUnit(BigDecimal.valueOf(3));
        expenseDTO.setExpenseType(GameDamageInspectionExpenseType.AUTO);
        expenseDTO.setAdditionalInfo("Additional Info");

        dto.setGameDamageInspectionKmExpenses(Arrays.asList(expenseDTO));

        dto.setExpensesIncluded(true);

        return dto;
    }

    private static void assertEvent(final GameDamageInspectionEvent event,
                                    final GameDamageInspectionEventDTO dto,
                                    final boolean lockedAsPastStatistics) {

        assertEvent(event, dto, dto.getRhy(), lockedAsPastStatistics);
    }

    private static void assertEvent(final GameDamageInspectionEvent event,
                                    final GameDamageInspectionEventDTO dto,
                                    final RiistanhoitoyhdistysDTO rhy,
                                    final boolean lockedAsPastStatistics) {
        assertNotNull(event);

        assertEquals(rhy.getId(), event.getRhy().getId());
        assertEquals(dto.getGameSpeciesCode(), event.getGameSpecies().getOfficialCode());

        final PersonContactInfoDTO dtoInspector = dto.getInspector();
        final Person inspector = event.getInspector();
        assertEquals(dtoInspector.getId(), inspector.getId());
        assertEquals(dtoInspector.getFirstName(), inspector.getFirstName());
        assertEquals(dtoInspector.getLastName(), inspector.getLastName());

        assertEquals(dto.getInspectorName(), event.getInspectorName());

        assertEquals(dto.getGeoLocation().getLatitude(), event.getGeoLocation().getLatitude());
        assertEquals(dto.getGeoLocation().getLongitude(), event.getGeoLocation().getLongitude());
        assertEquals(dto.getGeoLocation().getSource(), event.getGeoLocation().getSource());

        assertEquals(dto.getDate(), toLocalDateNullSafe(event.getDate()));
        assertEquals(dto.getBeginTime(), event.getBeginTime());
        assertEquals(dto.getEndTime(), event.getEndTime());

        assertEquals(dto.getDescription(), event.getDescription());

        if (event.getExpensesIncluded()) {
            assertEquals(dto.getHourlyExpensesUnit().doubleValue(), event.getHourlyExpensesUnit().doubleValue(), 0.001);
            assertEquals(dto.getDailyAllowance().doubleValue(), event.getDailyAllowance().doubleValue(), 0.001);
        } else {
            assertNull(event.getHourlyExpensesUnit());
            assertNull(event.getDailyAllowance());
        }
        assertEquals(lockedAsPastStatistics, event.isLockedAsPastStatistics());

        assertEquals(dto.isExpensesIncluded(), event.getExpensesIncluded());
    }

    private static void assertExpenses(final GameDamageInspectionEvent event, final GameDamageInspectionEventDTO dto) {
        final Set<GameDamageInspectionKmExpense> expenses = event.getGameDamageInspectionKmExpenses();
        assertNotNull(expenses);
        assertEquals(1, expenses.size());
        final GameDamageInspectionKmExpense expense = expenses.iterator().next();
        final GameDamageInspectionKmExpenseDTO expenseDTO = dto.getGameDamageInspectionKmExpenses().get(0);
        assertEquals(expenseDTO.getKilometers(), expense.getKilometers());
        assertEquals(expenseDTO.getExpenseUnit().doubleValue(), expense.getExpenseUnit().doubleValue(), 0.001);
        assertEquals(expenseDTO.getExpenseType(), expense.getExpenseType());
        assertEquals(expenseDTO.getAdditionalInfo(), expense.getAdditionalInfo());
    }

    @Test
    public void testCreate() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            model().newGameSpecies(OFFICIAL_CODE_MOOSE);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO inputDTO = createDTO(rhy);

                final GameDamageInspectionEventDTO outputDTO = feature.create(inputDTO);

                runInTransaction(() -> {
                    final GameDamageInspectionEvent event = repository.getOne(outputDTO.getId());

                    assertEvent(event, inputDTO, false);
                    assertExpenses(event, inputDTO);
                });
            });
        });
    }

    @Test(expected = RhyEventTimeException.class)
    public void testCreateTwoYearsPastAsCoordinator() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO dto = createDTO(rhy, today().minusYears(2));
                feature.create(dto);
            });
        });
    }

    @Test
    public void testCreateTwoYearsPastAsModerator() {
        withRhy(rhy -> {
            model().newGameSpecies(OFFICIAL_CODE_MOOSE);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final GameDamageInspectionEventDTO inputDTO = createDTO(rhy, today().minusYears(2));

                final GameDamageInspectionEventDTO outputDTO = feature.create(inputDTO);

                runInTransaction(() -> {
                    final GameDamageInspectionEvent event = repository.getOne(outputDTO.getId());

                    assertEvent(event, inputDTO, true);
                    assertExpenses(event, inputDTO);
                });
            });
        });
    }

    @Test(expected = RhyEventTimeException.class)
    public void testCreateDateInTheFuture() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO dto = createDTO(rhy, today().plusDays(1));
                feature.create(dto);
            });
        });
    }

    @Test(expected = RhyEventTimeException.class)
    public void testCreateEndTimeBeforeBeginTime() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO dto = createDTO(rhy, today(), new LocalTime(10, 0), new LocalTime(9, 0));
                feature.create(dto);
            });
        });
    }

    @Test
    public void testUpdate() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {
            final Person inspector = model().newPerson(rhy);
            model().newOccupation(rhy, inspector, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, today(), today());

            final GameSpecies moose = model().newGameSpecies(OFFICIAL_CODE_MOOSE);
            final GameSpecies bear = model().newGameSpecies(OFFICIAL_CODE_BEAR);
            final GameDamageInspectionEvent event = model().newGameDamageInspectionEvent(rhy, moose);
            event.setExpensesIncluded(true);

            event.setInspector(inspector);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final Person newInspector = model().newPerson(anotherRhy);
                model().newOccupation(rhy, newInspector, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, today().minusDays(1), today().minusDays(1));

                persistInNewTransaction();

                final GameDamageInspectionKmExpense expense = model().newGameDamageInspectionKmExpense(event);

                final GameDamageInspectionEventDTO mutated =
                        GameDamageInspectionEventDTO.create(event,
                                anotherRhy,
                                Arrays.asList(GameDamageInspectionKmExpenseDTO.create(expense)));
                mutated.setGameSpeciesCode(bear.getOfficialCode());

                mutated.setInspector(PersonContactInfoDTO.create(newInspector));

                mutated.setDate(mutated.getDate().minusDays(1));
                mutated.setBeginTime(mutated.getBeginTime().plusHours(1));
                mutated.setEndTime(mutated.getEndTime().plusHours(1));
                mutated.setDescription(mutated.getDescription() + "_mutated");
                mutated.setHourlyExpensesUnit(mutated.getHourlyExpensesUnit().add(BigDecimal.ONE));
                mutated.setDailyAllowance(mutated.getDailyAllowance().add(BigDecimal.ONE));
                mutated.setExpensesIncluded(true);

                final GameDamageInspectionKmExpenseDTO mutatedExpense = mutated.getGameDamageInspectionKmExpenses().get(0);
                mutatedExpense.setKilometers(mutatedExpense.getKilometers() + 1);
                mutatedExpense.setExpenseUnit(mutatedExpense.getExpenseUnit().add(BigDecimal.ONE));
                mutatedExpense.setExpenseType(GameDamageInspectionExpenseType.MOOTTORIKELKKA);
                mutatedExpense.setAdditionalInfo(mutatedExpense.getAdditionalInfo() + "_mutated");

                feature.update(mutated);

                runInTransaction(() -> {
                    final GameDamageInspectionEvent reloaded = repository.getOne(event.getId());

                    assertEvent(reloaded, mutated, RiistanhoitoyhdistysDTO.create(rhy), false);
                    assertExpenses(reloaded, mutated);
                });
            });
        }));
    }

    @Test
    public void testUpdate_removeExpenses() {
        withRhyAndCoordinator((rhy, coordinator) -> withRhy(anotherRhy -> {
            final Person inspector = model().newPerson(rhy);
            model().newOccupation(rhy, inspector, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA, today(), today());

            final GameSpecies moose = model().newGameSpecies(OFFICIAL_CODE_MOOSE);
            final GameDamageInspectionEvent event = model().newGameDamageInspectionEvent(rhy, moose);
            event.setExpensesIncluded(true);

            event.setInspector(inspector);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO mutated =
                        GameDamageInspectionEventDTO.create(event, anotherRhy, null);
                mutated.setHourlyExpensesUnit(null);
                mutated.setDailyAllowance(null);
                mutated.setExpensesIncluded(false);

                feature.update(mutated);

                runInTransaction(() -> {
                    final GameDamageInspectionEvent reloaded = repository.getOne(event.getId());

                    assertEvent(reloaded, mutated, RiistanhoitoyhdistysDTO.create(rhy), false);
                    assertThat(reloaded.getGameDamageInspectionKmExpenses(), hasSize(0));
                });
            });
        }));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreate_noOccupationForInspector() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            model().newGameSpecies(OFFICIAL_CODE_MOOSE);

            onSavedAndAuthenticated(createUser(coordinator), () -> {
                final GameDamageInspectionEventDTO inputDTO = createDTO(rhy);
                final Person invalidInspector = model().newPerson(rhy);

                persistInNewTransaction();

                inputDTO.setInspector(PersonContactInfoDTO.create(invalidInspector));

                feature.create(inputDTO);
            });
        });
    }
}
