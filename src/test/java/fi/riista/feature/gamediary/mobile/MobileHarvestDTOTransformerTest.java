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
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.VersionedTestExecutionSupport;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static fi.riista.feature.gamediary.image.GameDiaryImage.getUniqueImageIds;
import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.test.TestUtils.createList;
import static fi.riista.test.TestUtils.times;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

public class MobileHarvestDTOTransformerTest extends EmbeddedDatabaseTest
        implements VersionedTestExecutionSupport<HarvestSpecVersion> {

    private static void assertNoSpecimens(final MobileHarvestDTO dto) {
        if (dto.getHarvestSpecVersion().requiresSpecimenList()) {
            assertNotNull(dto.getSpecimens());
            assertEmpty(dto.getSpecimens());
        } else {
            assertNull(dto.getSpecimens());
        }
    }

    @Resource
    private MobileHarvestDTOTransformer transformer;

    @Override
    public List<HarvestSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(HarvestSpecVersion.class));
    }

    @Override
    public void onAfterVersionedTestExecution() {
        reset();
    }

    @Test
    public void testWithoutCreatingPluralAssociations() {
        forEachVersion(specVersion -> {

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
                assertNoSpecimens(dto);

                assertNotNull(dto.getAmount());
                assertEquals(harvest.getAmount(), dto.getAmount().intValue());

                assertFalse(dto.isHarvestReportDone());
                assertNull(dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testSpecimens() {
        forEachVersion(specVersion -> {

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

                assertNotNull(dto.getAmount());
                assertEquals(harvest.getAmount(), dto.getAmount().intValue());

                assertEmpty(dto.getImageIds());
                assertFalse(dto.isHarvestReportDone());
                assertNull(dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testImages() {
        forEachVersion(specVersion -> {

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

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertThat(dto.getImageIds(), containsInAnyOrder(getUniqueImageIds(pairs.get(i)._2).toArray()));

                assertFalse(dto.isHarvestReportDone());
                assertNull(dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testWithPermit() {
        forEachVersion(specVersion -> withRhy(rhy -> {

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
                if (specVersion.supportsHarvestPermitState()) {
                    assertNotNull(dto.getPermitNumber());
                    assertNotNull(dto.getPermitType());

                    assertEquals(
                            Tuple.of(dto.getPermitNumber(), dto.getPermitType()),
                            harvestIdToPairOfPermitNumberAndType.get(dto.getId()).get());
                } else {
                    assertNull(dto.getPermitNumber());
                    assertNull(dto.getPermitType());
                }
            });
        }));
    }

    @Test
    public void testStateAcceptedToPermit() {
        forEachVersion(specVersion -> {
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
                if (specVersion.supportsHarvestPermitState()) {
                    assertEquals(harvest.getStateAcceptedToHarvestPermit(), dto.getStateAcceptedToHarvestPermit());
                } else {
                    assertNull(dto.getStateAcceptedToHarvestPermit());
                }
            });
        });
    }

    @Test
    public void testWithHarvestReport() {
        forEachVersion(specVersion -> {

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

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertEmpty(dto.getImageIds());

                assertTrue(dto.isHarvestReportDone());
                assertEquals(state, dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testWithRejectedHarvestReport() {
        forEachVersion(specVersion -> {
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

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertTrue(dto.isHarvestReportDone());
                assertEquals(state, dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testHarvestAcceptedToPermit_AsContactPerson() {
        doTestCanEdit(StateAcceptedToHarvestPermit.ACCEPTED, true);
    }

    @Test
    public void testHarvestAcceptedToPermit() {
        doTestCanEdit(StateAcceptedToHarvestPermit.ACCEPTED, false);
    }

    @Test
    public void testHarvestProposedToPermit() {
        doTestCanEdit(StateAcceptedToHarvestPermit.PROPOSED, false);
    }

    @Test
    public void testHarvestRejectedToPermit() {
        doTestCanEdit(StateAcceptedToHarvestPermit.REJECTED, false);
    }

    @Test
    public void testHarvestAcceptedToPermit_withProposedHarvestReport() {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, false);
    }

    @Test
    public void testHarvestAcceptedToPermit_withProposedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.SENT_FOR_APPROVAL, true);
    }

    @Test
    public void testHarvestAcceptedToPermit_withApprovedHarvestReport() {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.APPROVED, false);
    }

    @Test
    public void testHarvestProposedToPermit_withRejectedHarvestReport() {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, false);
    }

    @Test
    public void testHarvestProposedToPermit_withRejectedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(StateAcceptedToHarvestPermit.ACCEPTED, HarvestReportState.REJECTED, true);
    }

    @Test
    public void testApiVersion() {
        forEachVersion(specVersion -> {

            final Harvest harvest = model().newMobileHarvest();
            persistAndAuthenticateWithNewUser(true);

            final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);

            assertNotNull(dtos);
            assertEquals(1, dtos.size());

            final MobileHarvestDTO dto = dtos.iterator().next();
            assertNotNull(dto);

            final Integer expectedApiVersion = specVersion.requiresDeprecatedApiParameter() ? Integer.valueOf(1) : null;
            assertEquals(expectedApiVersion, dto.getApiVersion());
        });
    }

    @Test
    public void testHarvestSpecVersion() {
        forEachVersion(specVersion -> {

            final Harvest harvest = model().newMobileHarvest();

            persistAndAuthenticateWithNewUser(true);

            final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);

            assertNotNull(dtos);
            assertEquals(1, dtos.size());

            dtos.forEach(dto -> assertEquals(specVersion, dto.getHarvestSpecVersion()));
        });
    }

    @Test
    public void testExtendedFieldsOfMoose() {
        forEachVersion(specVersion -> {

            final GameSpecies species = model().newGameSpeciesMoose();
            final Harvest harvest = model().newMobileHarvest(species);
            final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest);

            persistAndAuthenticateWithNewUser(true);

            final MobileHarvestDTO dto = transformer.apply(harvest, specVersion);
            assertNotNull(dto);
            assertEquals(1, dto.getSpecimens().size());

            final HarvestSpecimenAssertionBuilder assertionsBuilder = HarvestSpecimenAssertionBuilder.builder()
                    .withAgeAndGender(specimen.getAge(), specimen.getGender());

            if (specVersion.supportsExtendedFieldsForMoose()) {
                assertionsBuilder.allMooseFieldsPresent(specVersion);
            } else {
                assertionsBuilder.mooseFieldsAbsent();
            }

            assertionsBuilder.verify(dto.getSpecimens().get(0));
        });
    }

    @Test
    public void testQueryCountWithCompleteEntityGraph() {
        forEachVersion(specVersion -> withRhy(rhy -> {

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
        }));
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
        assertEquals(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()), dto.getPointOfTime());
        assertEquals(harvest.getDescription(), dto.getDescription());
        assertEquals(harvest.getMobileClientRefId(), dto.getMobileClientRefId());
        assertNotNull(dto.getAmount());

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

    private void doTestCanEdit(final StateAcceptedToHarvestPermit state, final boolean isContactPerson) {

        forEachVersion(specVersion -> withPerson(person -> withRhy(rhy -> {

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
                    if (specVersion.supportsHarvestPermitState()) {
                        assertEquals(state != StateAcceptedToHarvestPermit.ACCEPTED || isContactPerson, dto.isCanEdit());
                        assertEquals(state, dto.getStateAcceptedToHarvestPermit());
                        assertNotNull(dto.getPermitNumber());
                        assertNotNull(dto.getPermitType());
                    } else {
                        assertFalse(false);
                        assertNull(dto.getStateAcceptedToHarvestPermit());
                        assertNull(dto.getPermitNumber());
                        assertNull(dto.getPermitType());
                    }
                    assertFalse(dto.isHarvestReportDone());
                    assertNull(dto.getHarvestReportState());
                });
            });
        })));
    }

    private void doTestCanEditWithReport(final StateAcceptedToHarvestPermit state,
                                         final HarvestReportState reportState,
                                         final boolean isContactPerson) {

        forEachVersion(specVersion -> withPerson(person -> withRhy(rhy -> {

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
                    if (specVersion.supportsHarvestPermitState() && isContactPerson && reportState == HarvestReportState.SENT_FOR_APPROVAL) {
                        assertTrue(dto.isCanEdit());
                    } else {
                        assertFalse(dto.isCanEdit());
                    }
                    assertTrue(dto.isHarvestReportDone());
                    assertEquals(reportState, dto.getHarvestReportState());
                });
            });
        })));
    }
}
