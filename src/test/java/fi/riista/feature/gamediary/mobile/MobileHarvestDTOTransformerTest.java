package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenAssertionBuilder;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.BiConsumer;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020;
import static fi.riista.feature.gamediary.image.GameDiaryImage.getUniqueImageIds;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.test.TestUtils.times;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class MobileHarvestDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private MobileHarvestDTOTransformer transformer;

    @Theory
    public void testWithoutCreatingPluralAssociations(final HarvestSpecVersion specVersion) {
        final List<Harvest> harvests = createList(5, model()::newMobileHarvest);

        // Generate extra harvest that is not included in input and thus should not affect output either.
        model().newMobileHarvest();

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);

        assertNotNull(dtos);
        assertEquals(harvests.size(), dtos.size());

        for (int i = 0; i < harvests.size(); i++) {
            final Harvest harvest = harvests.get(i);
            final MobileHarvestDTO dto = dtos.get(i);

            assertNotNull(dto);
            verifyFieldsNotDerivedFromCollections(harvest, dto, specVersion);

            assertEmpty(dto.getImageIds());

            assertNotNull(dto.getSpecimens());
            assertEmpty(dto.getSpecimens());

            assertEquals(harvest.getAmount(), dto.getAmount());

            assertFalse(dto.isHarvestReportDone());
            assertNull(dto.getHarvestReportState());
        }
    }

    @Theory
    public void testSpecimens(final HarvestSpecVersion specVersion) {
        final List<Tuple2<Harvest, List<HarvestSpecimen>>> pairs =
                createList(5, () -> newMobileHarvestWithSpecimens(5));

        // Generate extra harvest that is not included in input and thus should not affect output either.
        newMobileHarvestWithSpecimens(5);

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(F.nonNullKeys(pairs), specVersion);

        assertNotNull(dtos);
        assertEquals(pairs.size(), dtos.size());

        for (int i = 0; i < pairs.size(); i++) {
            final Harvest harvest = pairs.get(i)._1();
            final MobileHarvestDTO dto = dtos.get(i);

            assertNotNull(dto);
            verifyFieldsNotDerivedFromCollections(harvest, dto, specVersion);

            assertEquals(harvest.getAmount(), dto.getAmount());

            assertEmpty(dto.getImageIds());
            assertFalse(dto.isHarvestReportDone());
            assertNull(dto.getHarvestReportState());
        }
    }

    @Theory
    public void testImages(final HarvestSpecVersion specVersion) {
        final List<Tuple2<Harvest, List<GameDiaryImage>>> pairs =
                createList(5, () -> newMobileHarvestWithImages(5));

        // Generate extra harvest that is not included in input and thus should not affect output either.
        newMobileHarvestWithImages(5);

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(F.nonNullKeys(pairs), specVersion);

        assertNotNull(dtos);
        assertEquals(pairs.size(), dtos.size());

        for (int i = 0; i < pairs.size(); i++) {
            final Harvest harvest = pairs.get(i)._1;
            final MobileHarvestDTO dto = dtos.get(i);

            assertNotNull(dto);
            verifyFieldsNotDerivedFromCollections(harvest, dto, specVersion);

            assertEquals(1, dto.getAmount());

            assertThat(dto.getImageIds(), containsInAnyOrder(getUniqueImageIds(pairs.get(i)._2).toArray()));

            assertFalse(dto.isHarvestReportDone());
            assertNull(dto.getHarvestReportState());
        }
    }

    @Theory
    public void testWithPermit(final HarvestSpecVersion specVersion) {
        withRhy(rhy -> {

            final List<Harvest> harvests = createList(5, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy);
                final Harvest harvest = model().newMobileHarvest();
                harvest.setHarvestPermit(permit);
                harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);
                harvest.setRhy(permit.getRhy());
                return harvest;
            });

            persistAndAuthenticateWithNewUser(true);

            final HashMap<Long, Tuple2<String, String>> harvestIdToPairOfPermitNumberAndType = HashMap.ofEntries(
                    harvests.stream()
                            .map(harvest -> Tuple.of(harvest.getId(), Tuple.of(
                                    harvest.getHarvestPermit().getPermitNumber(),
                                    harvest.getHarvestPermit().getPermitType())))
                            .collect(toList()));

            final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);
            assertNotNull(dtos);
            assertEquals(harvests.size(), dtos.size());

            dtos.forEach(dto -> {
                assertNotNull(dto.getPermitNumber());
                assertNotNull(dto.getPermitType());

                assertEquals(
                        Tuple.of(dto.getPermitNumber(), dto.getPermitType()),
                        harvestIdToPairOfPermitNumberAndType.get(dto.getId()).get());
            });
        });
    }

    @Theory
    public void testStateAcceptedToPermit(final HarvestSpecVersion specVersion) {
        final HarvestPermit permit = model().newHarvestPermit();
        final Harvest harvest = model().newMobileHarvest();
        harvest.setHarvestPermit(permit);
        harvest.setRhy(permit.getRhy());
        harvest.setStateAcceptedToHarvestPermit(StateAcceptedToHarvestPermit.ACCEPTED);

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);
        assertNotNull(dtos);
        assertEquals(1, dtos.size());

        dtos.forEach(dto -> {
            assertEquals(harvest.getStateAcceptedToHarvestPermit(), dto.getStateAcceptedToHarvestPermit());
        });
    }

    @Theory
    public void testWithHarvestReport(final HarvestSpecVersion specVersion) {
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final List<Harvest> harvests = newMobileHarvestsWithHarvestReport(5, state);

        // Generate extra harvest that is not included in input and thus should not affect output either.
        newMobileHarvestsWithHarvestReport(5, state);

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);

        assertNotNull(dtos);
        assertEquals(harvests.size(), dtos.size());

        for (int i = 0; i < harvests.size(); i++) {
            final Harvest harvest = harvests.get(i);
            final MobileHarvestDTO dto = dtos.get(i);

            assertNotNull(dto);
            verifyFieldsNotDerivedFromCollections(harvest, dto, specVersion);

            assertEquals(1, dto.getAmount());

            assertEmpty(dto.getImageIds());

            assertTrue(dto.isHarvestReportDone());
            assertEquals(state, dto.getHarvestReportState());
        }
    }

    @Theory
    public void testWithRejectedHarvestReport(final HarvestSpecVersion specVersion) {
        final HarvestReportState state = HarvestReportState.REJECTED;
        final List<Harvest> harvests = newMobileHarvestsWithHarvestReport(5, state);

        // Generate extra harvest that is not included in input and thus should not affect output either.
        newMobileHarvestsWithHarvestReport(5, state);

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);

        assertNotNull(dtos);
        assertEquals(harvests.size(), dtos.size());

        for (int i = 0; i < harvests.size(); i++) {
            final Harvest harvest = harvests.get(i);
            final MobileHarvestDTO dto = dtos.get(i);

            assertNotNull(dto);
            verifyFieldsNotDerivedFromCollections(harvest, dto, specVersion);
            assertEmpty(dto.getImageIds());

            assertEquals(1, dto.getAmount());

            assertTrue(dto.isHarvestReportDone());
            assertEquals(state, dto.getHarvestReportState());
        }
    }

    @Theory
    public void testHarvestAcceptedToPermit_AsContactPerson(final HarvestSpecVersion specVersion) {
        doTestCanEdit(StateAcceptedToHarvestPermit.ACCEPTED, true, specVersion);
    }

    @Theory
    public void testHarvestAcceptedToPermit(final HarvestSpecVersion specVersion) {
        doTestCanEdit(StateAcceptedToHarvestPermit.ACCEPTED, false, specVersion);
    }

    @Theory
    public void testHarvestProposedToPermit(final HarvestSpecVersion specVersion) {
        doTestCanEdit(StateAcceptedToHarvestPermit.PROPOSED, false, specVersion);
    }

    @Theory
    public void testHarvestRejectedToPermit(final HarvestSpecVersion specVersion) {
        doTestCanEdit(StateAcceptedToHarvestPermit.REJECTED, false, specVersion);
    }

    @Theory
    public void testHarvestAcceptedToPermit_withProposedHarvestReport(final HarvestSpecVersion specVersion) {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, false, specVersion);
    }

    @Theory
    public void testHarvestAcceptedToPermit_withProposedHarvestReport_asContactPerson(final HarvestSpecVersion specVersion) {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, true, specVersion);
    }

    @Theory
    public void testHarvestAcceptedToPermit_withApprovedHarvestReport(final HarvestSpecVersion specVersion) {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.APPROVED, false, specVersion);
    }

    @Theory
    public void testHarvestProposedToPermit_withRejectedHarvestReport(final HarvestSpecVersion specVersion) {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, false, specVersion);
    }

    @Theory
    public void testHarvestProposedToPermit_withRejectedHarvestReport_asContactPerson(final HarvestSpecVersion specVersion) {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, true, specVersion);
    }

    @Theory
    public void testApiVersion(final HarvestSpecVersion specVersion) {
        final Harvest harvest = model().newMobileHarvest();
        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);

        assertNotNull(dtos);
        assertEquals(1, dtos.size());

        final MobileHarvestDTO dto = dtos.iterator().next();
        assertNotNull(dto);

        assertEquals(Integer.valueOf(2), dto.getApiVersion());
    }

    @Theory
    public void testHarvestSpecVersion(final HarvestSpecVersion specVersion) {
        final Harvest harvest = model().newMobileHarvest();

        persistAndAuthenticateWithNewUser(true);

        final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);

        assertNotNull(dtos);
        assertEquals(1, dtos.size());

        dtos.forEach(dto -> assertEquals(specVersion, dto.getHarvestSpecVersion()));
    }

    @Theory
    public void testMooseExtensionFields_preSpecVersion8(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.lessThan(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testMooseExtensionFields(specVersion, 2020, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(entity, specVersion)
                    .verify(dto);
        });
    }

    @Theory
    public void testMooseExtensionFields_specVersion8_2020(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testMooseExtensionFields(specVersion, 2020, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2020Present()
                    .mooseFields2020EqualTo(entity)
                    .verify(dto);
        });
    }

    @Theory
    public void testMooseExtensionFields_specVersion8_2019(final HarvestSpecVersion specVersion) {
        assumeTrue(specVersion.greaterThanOrEqualTo(LOWEST_VERSION_SUPPORTING_ANTLER_FIELDS_2020));

        testMooseExtensionFields(specVersion, 2019, (entity, dto) -> {

            HarvestSpecimenAssertionBuilder.builder()
                    .mooseAdultMaleFields2015Present()
                    .mooseFields2017EqualTo(entity, specVersion)
                    .verify(dto);
        });
    }

    private void testMooseExtensionFields(final HarvestSpecVersion specVersion,
                                          final int huntingYear,
                                          final BiConsumer<HarvestSpecimen, HarvestSpecimenDTO> consumer) {

        final GameSpecies species = model().newGameSpeciesMoose();

        final Harvest harvest = model().newMobileHarvest(species);
        harvest.setPointOfTime(DateUtil.huntingYearBeginDate(huntingYear).toDateTime(LocalTime.now()));

        final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, ADULT_MALE, specVersion);

        persistAndAuthenticateWithNewUser(true);

        final MobileHarvestDTO dto = transformer.apply(harvest, specVersion);
        assertNotNull(dto);

        final List<HarvestSpecimenDTO> resultSpecimens = dto.getSpecimens();
        assertEquals(1, resultSpecimens.size());

        consumer.accept(specimen, resultSpecimens.get(0));
    }

    @Theory
    public void testQueryCountWithCompleteEntityGraph(final HarvestSpecVersion specVersion) {
        withRhy(rhy -> {

            final List<Harvest> harvests = createList(10, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy, true);
                final Harvest harvest =
                        newHarvestWithStateAcceptedToPermit(permit, StateAcceptedToHarvestPermit.ACCEPTED);

                times(5).run(() -> model().newHarvestSpecimen(harvest));
                times(5).run(() -> model().newGameDiaryImage(harvest));

                harvest.setHarvestReportState(some(HarvestReportState.class));
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());

                return harvest;
            });

            persistAndAuthenticateWithNewUser(true);

            // Manual programmatic statistics check because of versioned test execution.
            assertMaxQueryCount(8, () -> transformer.apply(harvests, specVersion));
        });
    }

    private static void verifyFieldsNotDerivedFromCollections(final Harvest harvest,
                                                              final MobileHarvestDTO dto,
                                                              final HarvestSpecVersion specVersion) {

        assumeNotNull(harvest.getId());
        assumeNotNull(harvest.getConsistencyVersion());

        assertEquals(harvest.getId(), dto.getId());
        assertEquals(harvest.getConsistencyVersion(), dto.getRev());
        Assert.assertEquals(GameDiaryEntryType.HARVEST, dto.getType());
        assertEquals(harvest.getSpecies().getOfficialCode(), dto.getGameSpeciesCode());
        assertEquals(harvest.getGeoLocation(), dto.getGeoLocation());
        assertEquals(harvest.getPointOfTime().toLocalDateTime(), dto.getPointOfTime());
        assertEquals(harvest.getDescription(), dto.getDescription());
        assertEquals(harvest.getMobileClientRefId(), dto.getMobileClientRefId());

        if (specVersion.supportsHarvestReport()) {
            assertEquals(harvest.getHuntingAreaType(), dto.getHuntingAreaType());
            assertEquals(harvest.getHuntingAreaSize(), dto.getHuntingAreaSize());
            assertEquals(harvest.getHuntingParty(), dto.getHuntingParty());
            assertEquals(harvest.getHuntingMethod(), dto.getHuntingMethod());
            assertEquals(harvest.getReportedWithPhoneCall(), dto.getReportedWithPhoneCall());
            assertEquals(harvest.getFeedingPlace(), dto.getFeedingPlace());
            assertEquals(harvest.getTaigaBeanGoose(), dto.getTaigaBeanGoose());

        } else {
            assertNull(dto.getHuntingAreaType());
            assertNull(dto.getHuntingAreaSize());
            assertNull(dto.getHuntingParty());
            assertNull(dto.getHuntingMethod());
            assertNull(dto.getReportedWithPhoneCall());
            assertNull(dto.getFeedingPlace());
            assertNull(dto.getTaigaBeanGoose());
        }
    }

    private Tuple2<Harvest, List<HarvestSpecimen>> newMobileHarvestWithSpecimens(final int numSpecimens) {
        final Harvest harvest = model().newMobileHarvest();

        // With one undefined specimen
        harvest.setAmount(numSpecimens + 1);

        return Tuple.of(harvest, createList(numSpecimens, () -> model().newHarvestSpecimen(harvest)));
    }

    private Tuple2<Harvest, List<GameDiaryImage>> newMobileHarvestWithImages(final int numImages) {
        final Harvest harvest = model().newMobileHarvest();
        return Tuple.of(harvest, createList(numImages, () -> model().newGameDiaryImage(harvest)));
    }

    private List<Harvest> newMobileHarvestsWithHarvestReport(final int numHarvests, final HarvestReportState state) {
        return createList(numHarvests, () -> {
            final Harvest harvest = model().newMobileHarvest();
            harvest.setHarvestReportState(state);
            harvest.setHarvestReportAuthor(harvest.getAuthor());
            harvest.setHarvestReportDate(DateUtil.now());

            // Populate all harvest report fields
            harvest.setHuntingAreaType(some(HuntingAreaType.class));
            harvest.setHuntingAreaSize((double) nextLong());
            harvest.setHuntingParty("party-" + nextLong());
            harvest.setHuntingMethod(some(HuntingMethod.class));
            harvest.setReportedWithPhoneCall(Boolean.TRUE);
            harvest.setFeedingPlace(Boolean.TRUE);
            harvest.setTaigaBeanGoose(Boolean.TRUE);

            return harvest;
        });
    }

    private Harvest newHarvestWithStateAcceptedToPermit(final HarvestPermit permit,
                                                        final StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newMobileHarvest();
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        harvest.setRhy(permit.getRhy());

        permit.addHarvest(harvest);

        return harvest;
    }

    private void doTestCanEdit(final StateAcceptedToHarvestPermit state,
                               final boolean isContactPerson,
                               final HarvestSpecVersion specVersion) {

        withPerson(person -> withRhy(rhy -> {

            final List<Harvest> harvests = createList(5, () -> {

                final HarvestPermit permit = model().newHarvestPermit(rhy, true);
                final Harvest harvest = newHarvestWithStateAcceptedToPermit(permit, state);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                } else {
                    harvest.setAuthor(person);
                }

                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);
                assertNotNull(dtos);
                assertEquals(harvests.size(), dtos.size());

                dtos.forEach(dto -> {
                    assertEquals(state != StateAcceptedToHarvestPermit.ACCEPTED || isContactPerson, dto.isCanEdit());
                    assertEquals(state, dto.getStateAcceptedToHarvestPermit());
                    assertNotNull(dto.getPermitNumber());
                    assertNotNull(dto.getPermitType());

                    assertFalse(dto.isHarvestReportDone());
                    assertNull(dto.getHarvestReportState());
                });
            });
        }));
    }

    private void doTestCanEditWithReport(final StateAcceptedToHarvestPermit state,
                                         final HarvestReportState reportState,
                                         final boolean isContactPerson,
                                         final HarvestSpecVersion specVersion) {

        withPerson(person -> withRhy(rhy -> {

            final List<Harvest> harvests = createList(5, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy, true);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                }

                final Harvest harvest = newHarvestWithStateAcceptedToPermit(permit, state);
                harvest.setHarvestReportState(reportState);
                harvest.setHarvestReportAuthor(harvest.getAuthor());
                harvest.setHarvestReportDate(DateUtil.now());
                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);
                assertNotNull(dtos);
                assertEquals(harvests.size(), dtos.size());

                dtos.forEach(dto -> {
                    if (isContactPerson && reportState == HarvestReportState.SENT_FOR_APPROVAL) {
                        assertTrue(dto.isCanEdit());
                    } else {
                        assertFalse(dto.isCanEdit());
                    }
                    assertTrue(dto.isHarvestReportDone());
                    assertEquals(reportState, dto.getHarvestReportState());
                });
            });
        }));
    }
}
