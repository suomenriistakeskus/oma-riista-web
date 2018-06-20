package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.test.rules.HibernateStatisticsAssertions;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.gamediary.image.GameDiaryImage.getUniqueImageIds;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.test.TestUtils.times;
import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HarvestDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestDTOTransformer transformer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUserNotAuthenticated() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("User id not available in security context");

        final List<Harvest> harvests = createList(5, model()::newHarvest);

        persistInNewTransaction();

        transformer.apply(harvests);
    }

    @Test
    public void testWithoutCreatingPluralAssociations() {
        withPerson(author -> {
            final List<Harvest> harvests = createList(5, () -> model().newHarvest(author));

            // Generate extra harvest that is not included in input and thus should not affect output either.
            model().newHarvest(author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);

                assertNotNull(dtos);
                assertEquals(harvests.size(), dtos.size());

                for (int i = 0; i < harvests.size(); i++) {
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(harvests.get(i), dto);

                    assertTrue(dto.getImageIds().isEmpty());
                    assertTrue(dto.getSpecimens().isEmpty());

                    assertTrue(dto.isCanEdit());
                    assertNull(dto.getHarvestReportState());
                    assertNull(dto.getHarvestReportDate());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testWithSpecimens() {
        withPerson(author -> {
            final List<Tuple2<Harvest, List<HarvestSpecimen>>> pairs =
                    createList(5, () -> newHarvestWithSpecimens(10, author));

            // Generate extra harvest that is not included in input and thus should not affect output either.
            newHarvestWithSpecimens(5, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final List<HarvestDTO> dtos = transformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final Harvest harvest = pairs.get(i)._1;
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(harvest, dto);

                    assertTrue(equalIdAndContent(pairs.get(i)._2, dto.getSpecimens(), dto.specimenOps()::equalContent));

                    assertTrue(dto.getImageIds().isEmpty());
                    assertTrue(dto.isCanEdit());
                    assertNull(dto.getHarvestReportState());
                    assertNull(dto.getHarvestReportDate());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testWithHarvestPermit() {
        withRhy(rhy -> withPerson(person -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final List<Harvest> harvests = createList(5, () -> {
                final PermittedMethod permittedMethod = new PermittedMethod();
                permittedMethod.setTraps(true);
                permittedMethod.setTapeRecorders(true);
                permittedMethod.setOther(false);

                final Harvest harvest = model().newHarvest(species, person, person);
                harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());
                harvest.setHarvestReportMemo("memo-" + nextLong());

                harvest.setHarvestPermit(permit);
                harvest.setRhy(rhy);
                harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                harvest.setPermittedMethod(permittedMethod);

                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);
                assertEquals(harvests.size(), dtos.size());

                for (int i = 0; i < harvests.size(); i++) {
                    final Harvest harvest = harvests.get(i);
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(harvest, dto);
                    assertNull(dto.getHarvestReportMemo());

                    assertEquals(rhy.getId(), dto.getRhyId());
                    assertEquals(permit.getPermitNumber(), dto.getPermitNumber());
                    assertEquals(permit.getPermitType(), dto.getPermitType());

                    assertNull(dto.getHarvestArea());
                }
            });
        }));
    }

    @Test
    public void testWithHarvestSeason() {
        withRhy(rhy -> withPerson(person -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestSeason season = model().newHarvestSeason(species);
            final HarvestArea harvestArea = model().newHarvestArea(rhy);
            final HarvestQuota quota = model().newHarvestQuota(season, harvestArea, 1);

            final List<Harvest> harvests = createList(5, () -> {
                final Harvest harvest = model().newHarvest(species, person, person);
                harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());
                harvest.setHarvestReportMemo("memo-" + nextLong());

                harvest.setSpecies(species);
                harvest.setRhy(rhy);
                harvest.setHarvestSeason(season);
                harvest.setHarvestQuota(quota);
                harvest.setHuntingAreaType(some(HuntingAreaType.class));
                harvest.setHuntingAreaSize((double) nextLong());
                harvest.setHuntingParty("party-" + nextLong());
                harvest.setHuntingMethod(some(HuntingMethod.class));
                harvest.setReportedWithPhoneCall(Boolean.TRUE);
                harvest.setFeedingPlace(Boolean.TRUE);
                harvest.setTaigaBeanGoose(Boolean.TRUE);
                harvest.setPropertyIdentifier("11122233334444");
                harvest.setLukeStatus(HarvestLukeStatus.CONFIRMED_NOT_ALPHA_1TO2Y);

                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);
                assertEquals(harvests.size(), dtos.size());

                for (int i = 0; i < harvests.size(); i++) {
                    final Harvest harvest = harvests.get(i);
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(harvest, dto);
                    assertNull(dto.getHarvestReportMemo());

                    assertNotNull(dto.getHarvestArea());
                    assertEquals(harvestArea.getId(), dto.getHarvestArea().getId());
                    assertEquals(harvestArea.getNameFinnish(), dto.getHarvestArea().getNameFI());
                    assertEquals(harvestArea.getNameSwedish(), dto.getHarvestArea().getNameSV());
                    assertEquals(rhy.getId(), dto.getRhyId());

                    assertNull(dto.getPermitNumber());
                    assertNull(dto.getPermitType());
                    assertNull(dto.getPermittedMethod());
                    assertNull(dto.getStateAcceptedToHarvestPermit());
                }
            });
        }));
    }

    @Test
    public void testWithHarvestReportMemo_asModerator() {
        withRhy(rhy -> withPerson(person -> {
            final Harvest harvest = model().newHarvest();
            harvest.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());
            harvest.setHarvestReportMemo("memo-" + nextLong());

            harvest.setDescription("description-" + nextLong());
            model().newGameDiaryImage(harvest);

            onSavedAndAuthenticated(createNewUser(SystemUser.Role.ROLE_MODERATOR), () -> {
                final List<HarvestDTO> dtos = transformer.apply(Collections.singletonList(harvest));
                assertEquals(1, dtos.size());

                final HarvestDTO dto = dtos.get(0);
                assertEquals(harvest.getHarvestReportMemo(), dto.getHarvestReportMemo());
                assertNull(dto.getDescription());
                assertEquals(0, dto.getImageIds().size());
            });
        }));
    }

    @Test
    public void testWithImages() {
        withPerson(author -> {
            final List<Tuple2<Harvest, List<GameDiaryImage>>> pairs =
                    createList(5, () -> newHarvestWithImages(5, author));

            // Generate extra harvest that is not included in input and thus should not affect output either.
            newHarvestWithImages(5, author);

            onSavedAndAuthenticated(createUser(author), () -> {
                final List<HarvestDTO> dtos = transformer.apply(F.nonNullKeys(pairs));

                assertNotNull(dtos);
                assertEquals(pairs.size(), dtos.size());

                for (int i = 0; i < pairs.size(); i++) {
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(pairs.get(i)._1, dto);

                    assertThat(dto.getImageIds(), containsInAnyOrder(getUniqueImageIds(pairs.get(i)._2).toArray()));

                    assertTrue(dto.isCanEdit());
                    assertNull(dto.getHarvestReportState());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testWithImages_asModerator() {
        withPerson(author -> {
            final Harvest harvest = model().newHarvest(author);
            harvest.setDescription("description-" + nextLong());
            createList(5, () -> model().newGameDiaryImage(harvest));

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<HarvestDTO> dtos = transformer.apply(singletonList(harvest));
                assertEquals(1, dtos.size());

                final HarvestDTO dto = dtos.get(0);
                assertThat(dto.getImageIds(), hasSize(0));
                assertNull(dto.getDescription());
            });
        });
    }

    @Test
    public void testWithImages_asPermitContactPerson() {
        withPerson(contactPerson -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestPermit permit = model().newHarvestPermit(contactPerson);
            model().newHarvestPermitSpeciesAmount(permit, species);

            final Harvest harvest = model().newHarvest(permit, species);
            harvest.setDescription("description-" + nextLong());

            createList(5, () -> model().newGameDiaryImage(harvest));

            onSavedAndAuthenticated(createUser(contactPerson), () -> {
                final List<HarvestDTO> dtos = transformer.apply(singletonList(harvest));
                assertEquals(1, dtos.size());

                final HarvestDTO dto = dtos.get(0);
                assertThat(dto.getImageIds(), hasSize(0));
                assertNull(dto.getDescription());
            });
        });
    }

    @Test
    public void testWithApprovedHarvestReport() {
        testWithHarvestReportState(HarvestReportState.APPROVED);
    }

    @Test
    public void testWithRejectedHarvestReport() {
        testWithHarvestReportState(HarvestReportState.REJECTED);
    }

    private void testWithHarvestReportState(final HarvestReportState state) {
        withPerson(author -> {
            final List<Harvest> harvests = newHarvestsWithHarvestReport(5, state, author);

            // Generate extra harvest that is not included in input and thus should not affect output either.
            newHarvestsWithHarvestReport(5, state, author);

            onSavedAndAuthenticated(createUser(author), () -> {

                final List<HarvestDTO> dtos = transformer.apply(harvests);

                assertNotNull(dtos);
                assertEquals(harvests.size(), dtos.size());

                for (int i = 0; i < harvests.size(); i++) {
                    final HarvestDTO dto = dtos.get(i);

                    assertNotNull(dto);
                    assertFieldsNotDerivedFromCollections(harvests.get(i), dto);
                    assertTrue(dto.getImageIds().isEmpty());

                    assertFalse(dto.isCanEdit());
                    assertEquals(state, dto.getHarvestReportState());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testHarvestAcceptedToPermit() {
        doTestCanEdit(false, StateAcceptedToHarvestPermit.ACCEPTED, false);
    }

    @Test
    public void testHarvestAcceptedToPermit_AsContactPerson() {
        doTestCanEdit(true, StateAcceptedToHarvestPermit.ACCEPTED, true);
    }

    @Test
    public void testHarvestProposedToPermit() {
        doTestCanEdit(true, StateAcceptedToHarvestPermit.PROPOSED, false);
    }

    @Test
    public void testHarvestRejectedToPermit() {
        doTestCanEdit(true, StateAcceptedToHarvestPermit.REJECTED, false);
    }

    @Test
    public void testHarvestAcceptedToPermit_withProposedHarvestReport() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, false);
    }

    @Test
    public void testHarvestAcceptedToPermit_withProposedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(true, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, true);
    }

    @Test
    public void testHarvestProposedToPermit_withApprovedHarvestReport() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.APPROVED, false);
    }

    @Test
    public void testHarvestRejectedToPermit_withRejectedHarvestReport() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, false);
    }

    @Test
    public void testHarvestRejectedToPermit_withRejectedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, true);
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 10)
    public void testQueryCountWithSeasonHarvest() {
        withRhy(rhy -> withPerson(hunter -> {
            final List<Harvest> harvests = createList(10, () -> {
                final GameSpecies species = model().newGameSpecies(true);
                final LocalDate seasonBegin = DateUtil.today();
                final LocalDate seasonEnd = seasonBegin.plusYears(1);
                final LocalDate reportingDeadline = seasonEnd.plusMonths(1);
                final HarvestSeason harvestSeason = model().newHarvestSeason(species, seasonBegin, seasonEnd, reportingDeadline);
                final HarvestArea harvestArea = model().newHarvestArea(rhy);
                final HarvestQuota quota = model().newHarvestQuota(harvestSeason, harvestArea, 100);

                final Harvest harvest = model().newHarvest(species, hunter);
                harvest.setHarvestQuota(quota);
                harvest.setHarvestSeason(harvestSeason);

                times(5).run(() -> model().newHarvestSpecimen(harvest));
                times(5).run(() -> model().newGameDiaryImage(harvest));

                harvest.setHarvestReportState(HarvestReportState.APPROVED);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());

                return harvest;
            });

            persistAndAuthenticateWithNewUser(true);

            transformer.apply(harvests);
        }));
    }

    @Test
    @HibernateStatisticsAssertions(maxQueries = 10)
    public void testQueryCountWithPermitHarvest() {
        withRhy(rhy -> withPerson(hunter -> {
            final List<Harvest> harvests = createList(10, () -> {
                final GameSpecies species = model().newGameSpecies(true);

                final HarvestPermit permit = model().newHarvestPermit(rhy);
                final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

                final Harvest harvest = model().newHarvest(species, hunter);
                harvest.setActualShooter(hunter);
                harvest.setHarvestPermit(permit);
                harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                harvest.setRhy(permit.getRhy());

                times(5).run(() -> model().newHarvestSpecimen(harvest));
                times(5).run(() -> model().newGameDiaryImage(harvest));

                harvest.setHarvestReportState(HarvestReportState.APPROVED);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());

                return harvest;
            });

            persistAndAuthenticateWithNewUser(true);

            transformer.apply(harvests);
        }));
    }

    private static void assertFieldsNotDerivedFromCollections(final Harvest harvest, final HarvestDTO dto) {

        // To verify integrity of test fixture.
        assertNotNull(harvest.getId());
        assertNotNull(harvest.getConsistencyVersion());

        assertEquals(harvest.getId(), dto.getId());
        assertEquals(harvest.getConsistencyVersion(), dto.getRev());
        Assert.assertEquals(GameDiaryEntryType.HARVEST, dto.getType());
        assertEquals(harvest.getSpecies().getOfficialCode(), dto.getGameSpeciesCode());
        assertEquals(harvest.getGeoLocation(), dto.getGeoLocation());
        assertEquals(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()), dto.getPointOfTime());
        assertEquals(harvest.getDescription(), dto.getDescription());
        assertEquals(harvest.getAmount(), dto.getAmount());
        assertEquals(harvest.getHuntingAreaType(), dto.getHuntingAreaType());
        assertEquals(harvest.getHuntingAreaSize(), dto.getHuntingAreaSize());
        assertEquals(harvest.getHuntingParty(), dto.getHuntingParty());
        assertEquals(harvest.getHuntingMethod(), dto.getHuntingMethod());
        assertEquals(harvest.getReportedWithPhoneCall(), dto.getReportedWithPhoneCall());
        assertEquals(harvest.getFeedingPlace(), dto.getFeedingPlace());
        assertEquals(harvest.getTaigaBeanGoose(), dto.getTaigaBeanGoose());
        assertEquals(harvest.getLukeStatus(), dto.getLukeStatus());
        assertEquals(harvest.getHarvestReportState(), dto.getHarvestReportState());
        assertEquals(harvest.getStateAcceptedToHarvestPermit(), dto.getStateAcceptedToHarvestPermit());

        if (harvest.getHarvestReportDate() != null) {
            assertEquals(harvest.getHarvestReportDate().toLocalDateTime(), dto.getHarvestReportDate());
        } else {
            assertNull(dto.getHarvestReportDate());
        }

        if (harvest.getPropertyIdentifier() != null) {
            assertEquals(harvest.getPropertyIdentifier().getDelimitedValue(), dto.getPropertyIdentifier());
        } else {
            assertNull(dto.getPropertyIdentifier());
        }

        if (harvest.getPermittedMethod() != null) {
            assertNotNull(dto.getPermittedMethod());
            assertEquals(harvest.getPermittedMethod().isTraps(), dto.getPermittedMethod().isTraps());
            assertEquals(harvest.getPermittedMethod().isOther(), dto.getPermittedMethod().isOther());
            assertEquals(harvest.getPermittedMethod().isTapeRecorders(), dto.getPermittedMethod().isTapeRecorders());

            if (harvest.getPermittedMethod().isOther()) {
                assertEquals(harvest.getPermittedMethod().getDescription(), dto.getPermittedMethod().getDescription());
            } else {
                assertNull(dto.getPermittedMethod().getDescription());
            }
        } else {
            assertNull(dto.getPermittedMethod());
        }
    }

    private void doTestCanEdit(final boolean canEdit,
                               final StateAcceptedToHarvestPermit state,
                               final boolean isContactPerson) {

        withRhy(rhy -> withPerson(person -> {
            final List<Harvest> harvests = createList(5, () -> {
                final GameSpecies species = model().newGameSpecies();
                final HarvestPermit permit = model().newHarvestPermit(rhy);
                model().newHarvestPermitSpeciesAmount(permit, species);

                final Harvest harvest = model().newHarvest(permit, species);
                harvest.setStateAcceptedToHarvestPermit(state);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                } else {
                    harvest.setAuthor(person);
                }

                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);
                assertEquals(harvests.size(), dtos.size());
                dtos.forEach(dto -> assertEquals(canEdit, dto.isCanEdit()));
            });
        }));
    }

    private void doTestCanEditWithReport(final boolean canEdit,
                                         final StateAcceptedToHarvestPermit state,
                                         final HarvestReportState reportState,
                                         final boolean isContactPerson) {

        withRhy(rhy -> withPerson(person -> {

            final List<Harvest> harvests = createList(5, () -> {

                final GameSpecies species = model().newGameSpecies();
                final HarvestPermit permit = model().newHarvestPermit(rhy);
                final HarvestPermitSpeciesAmount speciesAmount = model().newHarvestPermitSpeciesAmount(permit, species);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                }

                final Harvest harvest = model().newHarvest(speciesAmount.getHarvestPermit(), speciesAmount.getGameSpecies());
                harvest.setStateAcceptedToHarvestPermit(state);
                harvest.setHarvestReportState(reportState);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());

                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);
                assertEquals(harvests.size(), dtos.size());
                dtos.forEach(dto -> assertEquals(canEdit, dto.isCanEdit()));
            });
        }));
    }

    private Tuple2<Harvest, List<GameDiaryImage>> newHarvestWithImages(final int numImages, final Person author) {
        final Harvest harvest = model().newHarvest(author);
        return Tuple.of(harvest, createList(numImages, () -> model().newGameDiaryImage(harvest)));
    }

    private Tuple2<Harvest, List<HarvestSpecimen>> newHarvestWithSpecimens(final int numSpecimens,
                                                                           final Person author) {

        final Harvest harvest = model().newHarvest(author);

        final List<HarvestSpecimen> specimens = createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));

        // With one undefined specimen
        harvest.setAmount(numSpecimens + 1);

        return Tuple.of(harvest, specimens);
    }

    private List<Harvest> newHarvestsWithHarvestReport(
            final int numHarvests, final HarvestReportState state, final Person author) {

        return createList(numHarvests, () -> {
            final Harvest harvest = model().newHarvest(author);
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(author);
            harvest.setHarvestReportDate(DateUtil.now());

            return harvest;
        });
    }
}
