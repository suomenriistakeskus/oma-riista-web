package fi.riista.feature.gamediary.mobile;

import static fi.riista.util.Asserts.assertEmpty;
import static fi.riista.util.TestUtils.createList;
import static fi.riista.util.TestUtils.times;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTO;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.mobile.MobileHarvestDTOTransformer;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.VersionedTestExecutionSupport;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.HashMap;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    private static void assertPresenceOfMooselikeFields(
            final List<HarvestSpecimenDTO> specimens, final boolean allNotNull) {

        specimens.forEach(specimen -> {

            final Object[] fields = {
                specimen.getWeightEstimated(), specimen.getWeightMeasured(), specimen.getFitnessClass(),
                specimen.getAntlersType(), specimen.getAntlersWidth(), specimen.getAntlerPointsLeft(),
                specimen.getAntlerPointsRight(), specimen.getNotEdible(), specimen.getAdditionalInfo()
            };

            assertFalse(allNotNull ? F.anyNull(fields) : F.anyNonNull(fields));
        });
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
                verifyFieldsNotDerivedFromCollections(harvest, dto);

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
                verifyFieldsNotDerivedFromCollections(harvest, dto);

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
                final Harvest harvest = pairs.get(i)._1();
                final MobileHarvestDTO dto = dtos.get(i);

                assertNotNull(dto);
                verifyFieldsNotDerivedFromCollections(harvest, dto);

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertImageIds(pairs.get(i)._2(), dto);

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

                    final Tuple2<String, String> permitNumberAndType =
                            harvestIdToPairOfPermitNumberAndType.get(dto.getId()).get();

                    assertEquals(permitNumberAndType._1, dto.getPermitNumber());
                    assertEquals(permitNumberAndType._2, dto.getPermitType());
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

            final Harvest harvest = model().newMobileHarvest();
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
    public void testWithUndeletedHarvestReport() {
        forEachVersion(specVersion -> {

            final HarvestReport.State state = HarvestReport.State.PROPOSED;
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
                verifyFieldsNotDerivedFromCollections(harvest, dto);

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertEmpty(dto.getImageIds());

                assertTrue(dto.isHarvestReportDone());
                assertEquals(state, dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testWithDeletedHarvestReport() {
        forEachVersion(specVersion -> {

            final HarvestReport.State state = HarvestReport.State.DELETED;
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
                verifyFieldsNotDerivedFromCollections(harvest, dto);
                assertEmpty(dto.getImageIds());

                assertNotNull(dto.getAmount());
                assertEquals(1, dto.getAmount().intValue());

                assertFalse(dto.isHarvestReportDone());
                assertNull(dto.getHarvestReportState());
            }
        });
    }

    @Test
    public void testHarvestAcceptedToPermit_AsContactPerson() {
        doTestCanEdit(true, StateAcceptedToHarvestPermit.ACCEPTED, true);
    }

    @Test
    public void testHarvestAcceptedToPermit() {
        doTestCanEdit(false, StateAcceptedToHarvestPermit.ACCEPTED, false);
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
    public void testHarvestAcceptedToPermit_withNotDeletedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(
                false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReport.State.APPROVED, true);
    }

    @Test
    public void testHarvestAcceptedToPermit_withNotDeletedHarvestReport() {
        doTestCanEditWithReport(
                false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReport.State.APPROVED, false);
    }

    @Test
    public void testHarvestProposedToPermit_withNotDeletedHarvestReport() {
        doTestCanEditWithReport(
                false, StateAcceptedToHarvestPermit.PROPOSED, HarvestReport.State.SENT_FOR_APPROVAL, false);
    }

    @Test
    public void testHarvestRejectedToPermit_withNotDeletedHarvestReport() {
        doTestCanEditWithReport(
                false, StateAcceptedToHarvestPermit.REJECTED, HarvestReport.State.SENT_FOR_APPROVAL, false);
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
            model().newHarvestSpecimen(harvest, specVersion);

            persistAndAuthenticateWithNewUser(true);

            final List<MobileHarvestDTO> dtos = transformer.apply(singletonList(harvest), specVersion);

            assertNotNull(dtos);
            assertEquals(1, dtos.size());

            final MobileHarvestDTO dto = dtos.iterator().next();
            assertNotNull(dto);
            assertPresenceOfMooselikeFields(dto.getSpecimens(), specVersion.supportsExtendedFieldsForMoose());
        });
    }

    @Test
    public void testQueryCountWithCompleteEntityGraph() {
        forEachVersion(specVersion -> withRhy(rhy -> {

            final List<Harvest> harvests = createList(10, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy, true);
                final Harvest harvest =
                        newHarvestWithStateAcceptedToPermit(permit, some(StateAcceptedToHarvestPermit.class));

                times(5).run(() -> model().newHarvestSpecimen(harvest));
                times(5).run(() -> model().newGameDiaryImage(harvest));

                newHarvestReport(some(HarvestReport.State.class), harvest);

                return harvest;
            });

            persistAndAuthenticateWithNewUser(true);

            transformer.apply(harvests, specVersion);

            // Manual programmatic statistics check because of versioned test execution.
            final long expectedMaxQueryCount = 7;
            final long queryCount = getHibernateStatistics().getQueryExecutionCount();
            assertTrue(
                    String.format("Expected max %d queries but was: %d", expectedMaxQueryCount, queryCount),
                    queryCount <= expectedMaxQueryCount);
        }));
    }

    private static void verifyFieldsNotDerivedFromCollections(final Harvest harvest, final MobileHarvestDTO dto) {

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
    }

    private static void assertImageIds(final Collection<GameDiaryImage> images, final MobileHarvestDTO dto) {
        assertEquals(getUniqueImageUuids(images), new HashSet<>(dto.getImageIds()));
    }

    private static Set<UUID> getUniqueImageUuids(final Collection<GameDiaryImage> images) {
        return F.mapNonNullsToSet(images, Functions.idOf(GameDiaryImage::getFileMetadata));
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

    private HarvestReport newHarvestReport(final HarvestReport.State state, final Harvest harvest) {
        final HarvestReportFields fields = model().newHarvestReportFields(harvest.getSpecies(), false);
        return model().newHarvestReport(fields, state, harvest);
    }

    private List<Harvest> newMobileHarvestsWithHarvestReport(final int numHarvests, final HarvestReport.State state) {
        return createList(numHarvests, () -> {
            final Harvest harvest = model().newMobileHarvest();
            newHarvestReport(state, harvest);
            return harvest;
        });
    }

    private Harvest newHarvestWithStateAcceptedToPermit(
            final HarvestPermit permit, final StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newMobileHarvest();
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        harvest.setRhy(permit.getRhy());

        permit.addHarvest(harvest);

        return harvest;
    }

    private void doTestCanEdit(
            final boolean canEdit, final StateAcceptedToHarvestPermit state, final boolean isContactPerson) {

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
                    assertEquals(canEdit, dto.isCanEdit());
                    assertFalse(dto.isHarvestReportDone());
                    assertNull(dto.getHarvestReportState());
                });
            });
        })));
    }

    private void doTestCanEditWithReport(
            final boolean canEdit,
            final StateAcceptedToHarvestPermit state,
            final HarvestReport.State reportState,
            final boolean isContactPerson) {

        forEachVersion(specVersion -> withPerson(person -> withRhy(rhy -> {

            final List<Harvest> harvests = createList(5, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy, true);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                }

                final Harvest harvest = newHarvestWithStateAcceptedToPermit(permit, state);
                model().newHarvestReport(harvest, reportState);
                return harvest;
            });

            onSavedAndAuthenticated(createUser(person), () -> {
                final List<MobileHarvestDTO> dtos = transformer.apply(harvests, specVersion);
                assertNotNull(dtos);
                assertEquals(harvests.size(), dtos.size());

                dtos.forEach(dto -> {
                    assertEquals(canEdit, dto.isCanEdit());
                    assertFalse(dto.isHarvestReportDone());
                    assertNull(dto.getHarvestReportState());
                });
            });
        })));
    }

}
