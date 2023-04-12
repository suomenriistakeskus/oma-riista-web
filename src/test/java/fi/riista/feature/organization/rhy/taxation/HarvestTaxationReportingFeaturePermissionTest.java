package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.METSASTYKSENVALVOJA;
import static fi.riista.feature.organization.occupation.OccupationType.PETOYHDYSHENKILO;
import static fi.riista.feature.organization.occupation.OccupationType.SRVA_YHTEYSHENKILO;
import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertThrows;

@RunWith(Theories.class)
public class HarvestTaxationReportingFeaturePermissionTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @DataPoints("OccupationTypes")
    public static final List<OccupationType> OCCUPATION_TYPES = Arrays.asList(
            SRVA_YHTEYSHENKILO,
            PETOYHDYSHENKILO,
            METSASTYKSENVALVOJA,
            METSASTAJATUTKINNON_VASTAANOTTAJA,
            AMPUMAKOKEEN_VASTAANOTTAJA
    );

    @Resource
    private HarvestTaxationReportingFeature feature;

    @Resource
    private HarvestTaxationRepository repository;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }


    private Riistanhoitoyhdistys rhy;

    private GISHirvitalousalue hta;

    private GameSpecies species;

    private HarvestTaxationReportDTO dto;


    private MultipartFile file;
    private HarvestTaxationReportDTO saved;

    @Before
    public void setUp() throws IOException {

        final LocalDate today = new LocalDate(2021, 3, 30);
        MockTimeProvider.mockTime(today.toDate().getTime());

        hta = model().newGISHirvitalousalue();

        rhy = model().newRiistanhoitoyhdistys();
        model().newRhyHirvitalousalue(rhy, hta, 1234654.0);

        species = model().newGameSpeciesMoose();

        final SystemUser admin = createNewAdmin();

        persistInNewTransaction();

        final HarvestTaxationReport harvestTaxationReport = new HarvestTaxationReport();
        harvestTaxationReport.setHuntingYear(2021);
        harvestTaxationReport.setSpecies(species);
        harvestTaxationReport.setRhy(rhy);
        harvestTaxationReport.setHta(hta);
        harvestTaxationReport.setHasTaxationPlanning(false);
        harvestTaxationReport.setGenderDistribution(1.5);
        harvestTaxationReport.setYoungPercent(100);
        harvestTaxationReport.setPlannedUtilizationRateOfThePermits(100);
        harvestTaxationReport.setShareOfBankingPermits(0);
        harvestTaxationReport.setHarvestTaxationReportState(HarvestTaxationReportState.DRAFT);
        repository.save(harvestTaxationReport);

        persistInNewTransaction();

        dto = new HarvestTaxationReportDTO();
        dto.setHuntingYear(2021);
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setRhyId(rhy.getId());
        dto.setHtaId(hta.getId());
        dto.setHasTaxationPlanning(true);
        dto.setPlanningBasisPopulation(5000);
        dto.setPlannedRemainingPopulation(2.5);
        dto.setGenderDistribution(80.0);
        dto.setYoungPercent(10);
        dto.setPlannedUtilizationRateOfThePermits(100);
        dto.setShareOfBankingPermits(10);
        dto.setPlannedPermitMin(1);
        dto.setPlannedPermitMax(400);
        dto.setPlannedCatchMin(1);
        dto.setPlannedCatchMax(200);
        dto.setPlannedPermitDensityMin(12.0);
        dto.setPlannedPermitDensityMax(13.0);
        dto.setPlannedPreyDensityMin(1.0);
        dto.setPlannedPreyDensityMax(200.0);
        dto.setPlannedCatchYoungPercent(15);
        dto.setPlannedCatchMalePercent(60);
        dto.setStakeholdersConsulted(today().minusDays(1));

        dto.setJustification("");
        dto.setState(HarvestTaxationReportState.DRAFT);
        dto.setAttachments(new ArrayList<>());

        file = newAttachment();

        final MultipartFile file2 = newAttachment();

        authenticate(admin);

        saved = feature.saveOrUpdateTaxationReport(dto, Collections.singletonList(file2));
        persistInNewTransaction();

    }


    @Theory
    public void testGetMooseAreas_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.getMooseAreas(rhy.getId()));
    }

    @Theory
    public void testExportWithRHY_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);

        final HarvestTaxationExcelDTO dto = new HarvestTaxationExcelDTO();
        dto.setRhyId(rhy.getId());
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setHuntingYear(2021);

        assertThrows(AccessDeniedException.class, () -> feature.export(dto));
    }

    @Theory
    public void testExportWithoutRHY_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);

        final HarvestTaxationExcelDTO dto = new HarvestTaxationExcelDTO();
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setHuntingYear(2021);

        assertThrows(AccessDeniedException.class, () -> feature.export(dto));
    }

    @Theory
    public void testGetTaxationReportYears_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.getTaxationReportYears(rhy.getId()));
    }

    @Theory
    public void testGetTaxationReportBySpeciesAndHuntingYear_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.getTaxationReportDTOBySpeciesAndHuntingYear(hta.getId(), rhy.getId(), species.getOfficialCode(), 2021));
    }

    @Theory
    public void testSaveOrUpdateTaxationReport_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Theory
    public void testSaveOrUpdateTaxationReport_withAttachments_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.saveOrUpdateTaxationReport(dto, Collections.singletonList(file)));
    }

    @Theory
    public void getAttachment_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.getAttachment(saved.getAttachments().get(0).getId()));
    }

    @Theory
    public void deleteAttachment_permissionDenied(final OccupationType occupationType) {
        final SystemUser user = createUserWithPerson();
        model().newOccupation(rhy, user.getPerson(), occupationType);
        persistInNewTransaction();

        authenticate(user);
        assertThrows(AccessDeniedException.class, () -> feature.deleteAttachment(saved.getAttachments().get(0).getId()));
    }

    private MultipartFile newAttachment() {
        final byte[] attachmentData = new byte[4096];
        new Random().nextBytes(attachmentData);
        final String filename = "test" + nextLong() + ".png";
        return new MockMultipartFile(filename, "//test/" + filename, "image/png", attachmentData);
    }
}
