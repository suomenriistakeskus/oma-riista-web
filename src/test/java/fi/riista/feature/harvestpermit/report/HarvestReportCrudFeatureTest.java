package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HuntingAreaType;
import fi.riista.feature.gamediary.harvest.HuntingMethod;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HarvestReportCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportCrudFeature feature;

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Test
    public void testSmokeSingleHarvestReport() {
        final HarvestReportSingleHarvestDTO dto = createDTO();
        final HarvestReportDTOBase res = feature.create(dto);
        assertNotNull(res);
    }

    @Test(expected = HarvestReportAlreadyDoneException.class)
    public void testPreventDuplicateSingleHarvestReports() {
        final HarvestReportSingleHarvestDTO dto = createDTO();
        feature.create(dto);
        feature.create(dto);
    }

    @Test
    public void testSingleHarvestReportUpdateOk() {
        final HarvestReportSingleHarvestDTO dto = createDTO();
        final HarvestReportDTOBase createdDTO = feature.create(dto);
        feature.update(createdDTO);
    }

    private HarvestReportSingleHarvestDTO createDTO() {
        final HarvestReportSingleHarvestDTO dto = new HarvestReportSingleHarvestDTO();

        withPerson(person -> withRhy(rhy -> {
            final GameSpecies species = model().newGameSpecies(false);
            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final HarvestReportFields fields = model().newHarvestReportFields(species, true);
            final Harvest harvest = model().newHarvest(person);

            final SystemUser user = createUser(person);

            persistInNewTransaction();
            authenticate(user);

            dto.setRhyId(rhy.getId());
            dto.setFields(HarvestReportFieldsDTO.create(fields));
            dto.setPermitNumber(permit.getPermitNumber());
            dto.setGameDiaryEntryId(harvest.getId());
            dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));
            dto.setGeoLocation(harvest.getGeoLocation());
            dto.setHuntingAreaSize(1.0);
            dto.setHuntingAreaType(HuntingAreaType.PROPERTY);
            dto.setHuntingMethod(HuntingMethod.SHOT);
            dto.setReportedWithPhoneCall(false);
            dto.setGender(GameGender.FEMALE);
            dto.setAge(GameAge.ADULT);
            dto.setWeight(2.0);
        }));

        return dto;
    }

    @Test
    public void testCreateForPermit() {
        withPerson(person -> {
            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = createPermit(person, species);

            final Harvest acceptedHarvest =
                    createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.REJECTED);

            onSavedAndAuthenticated(createUser(person), () -> {
                final Long reportId = feature.createForListPermit(permit.getId(), permit.getConsistencyVersion());

                runInTransaction(() -> {
                    HarvestReport report = harvestReportRepository.getOne(reportId);
                    assertEquals(person, report.getAuthor());
                    assertEquals(permit, report.getHarvestPermit());

                    assertEquals(1, report.getHarvests().size());
                    assertTrue(report.getHarvests().contains(acceptedHarvest));

                    Harvest accepted = report.getHarvests().iterator().next();
                    assertEquals(report, accepted.getHarvestReport());
                    assertEquals(permit, accepted.getHarvestPermit());
                });
            });
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateForPermit_failsIfProposed() {
        withPerson(person -> {

            final GameSpecies species = model().newGameSpecies(true);
            final HarvestPermit permit = createPermit(person, species);

            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
            createHarvest(species, permit, Harvest.StateAcceptedToHarvestPermit.PROPOSED);

            onSavedAndAuthenticated(createUser(person), () -> {
                feature.createForListPermit(permit.getId(), permit.getConsistencyVersion());
            });
        });
    }

    private Harvest createHarvest(
            final GameSpecies species, final HarvestPermit permit, final Harvest.StateAcceptedToHarvestPermit state) {

        final Harvest harvest = model().newHarvest(species);
        harvest.setHarvestPermit(permit);
        harvest.setStateAcceptedToHarvestPermit(state);
        harvest.setRhy(permit.getRhy());
        return harvest;
    }

    private HarvestPermit createPermit(final Person person, final GameSpecies species) {
        final HarvestPermit permit = model().newHarvestPermit(true);
        permit.setOriginalContactPerson(person);
        model().newHarvestPermitSpeciesAmount(permit, species);
        return permit;
    }

}
