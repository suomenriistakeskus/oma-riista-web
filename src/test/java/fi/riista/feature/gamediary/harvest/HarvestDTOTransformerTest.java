package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.image.GameDiaryImage;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.Functions;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fi.riista.util.EqualityHelper.equalIdAndContent;
import static fi.riista.util.TestUtils.createList;
import static fi.riista.util.TestUtils.times;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
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
                    assertFieldsNotDerivedFromCollections(pairs.get(i)._1(), dto);

                    verifyImageIds(pairs.get(i)._2(), dto);

                    assertTrue(dto.isCanEdit());
                    assertNull(dto.getHarvestReportState());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testWithUndeletedHarvestReport() {
        withPerson(author -> {
            final HarvestReport.State state = HarvestReport.State.PROPOSED;
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
    public void testWithDeletedHarvestReport() {
        withPerson(author -> {
            final HarvestReport.State state = HarvestReport.State.DELETED;
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

                    assertTrue(dto.isCanEdit());
                    assertNull(dto.getHarvestReportState());
                    assertFalse(dto.isHarvestReportRequired());
                }
            });
        });
    }

    @Test
    public void testWithMultipleHarvestReports() {
        withPerson(author -> {
            final GameSpecies species = model().newGameSpecies();
            final HarvestReportFields fields = model().newHarvestReportFields(species, false);
            final HarvestReport.State state = HarvestReport.State.PROPOSED;

            final List<Harvest> harvests = createList(5, () -> {
                final Harvest harvest = model().newHarvest(author);

                model().newHarvestReport(fields, state, harvest);
                model().newHarvestReport(fields, state, harvest);

                return harvest;
            });

            onSavedAndAuthenticated(createUser(author), () -> transformer.apply(harvests));
        });
    }

    @Test
    public void testHarvestAcceptedToPermit_AsContactPerson() {
        final SystemUser author = createUserWithPerson("author");
        final SystemUser contact = createUserWithPerson("contact");
        doTestCanEdit(true, StateAcceptedToHarvestPermit.ACCEPTED, contact, author.getPerson(), contact.getPerson());
    }

    @Test
    public void testHarvestAcceptedToPermit() {
        final SystemUser author = createUserWithPerson("author");
        final Person contact = createUserWithPerson("contact").getPerson();
        doTestCanEdit(false, StateAcceptedToHarvestPermit.ACCEPTED, author, author.getPerson(), contact);
    }

    @Test
    public void testHarvestProposedToPermit() {
        final SystemUser author = createUserWithPerson("author");
        final Person contact = createUserWithPerson("contact").getPerson();
        doTestCanEdit(true, StateAcceptedToHarvestPermit.PROPOSED, author, author.getPerson(), contact);
    }

    @Test
    public void testHarvestRejectedToPermit() {
        final SystemUser author = createUserWithPerson("author");
        final Person contact = createUserWithPerson("contact").getPerson();
        doTestCanEdit(true, StateAcceptedToHarvestPermit.REJECTED, author, author.getPerson(), contact);
    }

    @Test
    public void testHarvestAcceptedToPermit_withNotDeletedHarvestReport_asContactPerson() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReport.State.APPROVED, true);
    }

    @Test
    public void testHarvestAcceptedToPermit_withNotDeletedHarvestReport() {
        doTestCanEditWithReport(false, StateAcceptedToHarvestPermit.ACCEPTED, HarvestReport.State.APPROVED, false);
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
    @HibernateStatisticsAssertions(maxQueries = 13)
    public void testQueryCountWithCompleteEntityGraph() {
        withRhy(rhy -> withPerson(hunter -> {
            final List<Harvest> harvests = createList(10, () -> {

                final HarvestQuota quota = model().newHarvestQuota(
                        model().newHarvestSeason(DateUtil.today()), model().newHarvestArea(rhy), 100);

                final HarvestPermit permit = model().newHarvestPermit(rhy);
                final Harvest harvest =
                        newHarvestWithStateAcceptedToPermit(permit, some(StateAcceptedToHarvestPermit.class));
                harvest.setActualShooter(hunter);
                harvest.setHarvestQuota(quota);

                times(5).run(() -> model().newHarvestSpecimen(harvest));
                times(5).run(() -> model().newGameDiaryImage(harvest));

                newHarvestReport(some(HarvestReport.State.class), harvest);

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
    }

    private static void verifyImageIds(final Collection<GameDiaryImage> images, final HarvestDTO dto) {
        assertEquals(getUniqueImageUuids(images), new HashSet<>(dto.getImageIds()));
    }

    private static Set<UUID> getUniqueImageUuids(final Collection<GameDiaryImage> images) {
        return F.mapNonNullsToSet(images, Functions.idOf(GameDiaryImage::getFileMetadata));
    }

    private void doTestCanEdit(
            final boolean canEdit,
            final StateAcceptedToHarvestPermit state,
            final SystemUser user,
            final Person author,
            final Person contactPerson) {

        withRhy(rhy -> {
            final List<Harvest> harvests = createList(5, () -> {
                final HarvestPermit permit = model().newHarvestPermit(rhy);
                permit.setOriginalContactPerson(contactPerson);

                return newHarvestWithStateAcceptedToPermit(permit, state, author);
            });

            onSavedAndAuthenticated(user, () -> {
                final List<HarvestDTO> dtos = transformer.apply(harvests);
                assertEquals(harvests.size(), dtos.size());
                dtos.forEach(dto -> assertEquals(canEdit, dto.isCanEdit()));
            });
        });
    }

    private void doTestCanEditWithReport(final boolean canEdit,
                                         final StateAcceptedToHarvestPermit state,
                                         final HarvestReport.State reportState,
                                         final boolean isContactPerson) {

        withRhy(rhy -> withPerson(person -> {

            final List<Harvest> harvests = createList(5, () -> {

                final HarvestPermit permit = model().newHarvestPermit(rhy);

                if (isContactPerson) {
                    permit.setOriginalContactPerson(person);
                }

                final Harvest harvest = newHarvestWithStateAcceptedToPermit(permit, state);
                model().newHarvestReport(harvest, reportState);

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

    private Tuple2<Harvest, List<HarvestSpecimen>> newHarvestWithSpecimens(
            final int numSpecimens, final Person author) {

        final Harvest harvest = model().newHarvest(author);

        final List<HarvestSpecimen> specimens = createList(numSpecimens, () -> model().newHarvestSpecimen(harvest));

        // With one undefined specimen
        harvest.setAmount(numSpecimens + 1);

        return Tuple.of(harvest, specimens);
    }

    private HarvestReport newHarvestReport(final HarvestReport.State state, final Harvest harvest) {
        final HarvestReportFields fields = model().newHarvestReportFields(harvest.getSpecies(), false);
        return model().newHarvestReport(fields, state, harvest);
    }

    private List<Harvest> newHarvestsWithHarvestReport(
            final int numHarvests, final HarvestReport.State state, final Person author) {

        return createList(numHarvests, () -> {
            final Harvest harvest = model().newHarvest(author);
            newHarvestReport(state, harvest);
            return harvest;
        });
    }

    private Harvest newHarvestWithStateAcceptedToPermit(
            final HarvestPermit permit, final StateAcceptedToHarvestPermit state, final Person author) {

        final Harvest harvest = newHarvestWithStateAcceptedToPermit(permit, state);
        harvest.setAuthor(author);
        return harvest;
    }

    private Harvest newHarvestWithStateAcceptedToPermit(
            final HarvestPermit permit, final StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newHarvest(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        return harvest;
    }

}
