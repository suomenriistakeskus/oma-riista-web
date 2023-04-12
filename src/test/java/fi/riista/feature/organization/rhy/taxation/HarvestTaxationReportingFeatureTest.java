package fi.riista.feature.organization.rhy.taxation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.LocalisedString;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class HarvestTaxationReportingFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private HarvestTaxationReportingFeature feature;

    @Resource
    private HarvestTaxationRepository repository;

    @Resource
    private HarvestTaxationReportDTOTransformer harvestTaxationReportDTOTransformer;

    private Riistanhoitoyhdistys rhy;

    private GISHirvitalousalue hta;
    private GISHirvitalousalue anotherHta;

    private GameSpecies species;

    private HarvestTaxationReportDTO dto;
    private HarvestTaxationReport report;

    private SystemUser coordinator;
    private SystemUser admin;
    private SystemUser moderator;

    private Validator validator;

    @Before
    public void setUp() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        final LocalDate today = new LocalDate(2021, 3, 30);
        MockTimeProvider.mockTime(today.toDate().getTime());

        hta = model().newGISHirvitalousalue();
        anotherHta = model().newGISHirvitalousalue();  // unattached hta

        rhy = model().newRiistanhoitoyhdistys();
        model().newRhyHirvitalousalue(rhy, hta, 1234654.0);

        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        model().newRhyHirvitalousalue(rhy2, hta, 2346546.0);

        species = model().newGameSpeciesMoose();
        model().newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);

        coordinator = createUserWithPerson();
        model().newOccupation(rhy, coordinator.getPerson(), TOIMINNANOHJAAJA);

        admin = createNewAdmin();
        moderator = createNewModerator();

        persistInNewTransaction();

        final HarvestTaxationReport harvestTaxationReport = new HarvestTaxationReport();
        harvestTaxationReport.setHuntingYear(2021);
        harvestTaxationReport.setSpecies(species);
        harvestTaxationReport.setRhy(rhy);
        harvestTaxationReport.setHta(hta);
        harvestTaxationReport.setHasTaxationPlanning(true);
        harvestTaxationReport.setPlannedRemainingPopulation(2.5);
        harvestTaxationReport.setGenderDistribution(1.5);
        harvestTaxationReport.setYoungPercent(100);
        harvestTaxationReport.setPlannedUtilizationRateOfThePermits(100);
        harvestTaxationReport.setShareOfBankingPermits(0);
        harvestTaxationReport.setPlannedPermitMin(1);
        harvestTaxationReport.setPlannedPermitMax(400);
        harvestTaxationReport.setPlannedCatchMin(1);
        harvestTaxationReport.setPlannedCatchMax(200);
        harvestTaxationReport.setPlannedPermitDensityMin(12.0);
        harvestTaxationReport.setPlannedPermitDensityMax(13.0);
        harvestTaxationReport.setPlannedPreyDensityMin(1.0);
        harvestTaxationReport.setPlannedPreyDensityMax(200.0);
        harvestTaxationReport.setPlannedCatchYoungPercent(23);
        harvestTaxationReport.setPlannedCatchMalePercent(53);
        harvestTaxationReport.setStakeholdersConsulted(today().minusDays(1));
        harvestTaxationReport.setApprovedAtTheBoardMeeting(today().minusDays(1));
        harvestTaxationReport.setJustification("");
        harvestTaxationReport.setHarvestTaxationReportState(HarvestTaxationReportState.CONFIRMED);
        report = repository.save(harvestTaxationReport);

        final HarvestTaxationReport harvestTaxationReport2 = new HarvestTaxationReport();
        harvestTaxationReport2.setHuntingYear(2020);
        harvestTaxationReport2.setSpecies(species);
        harvestTaxationReport2.setRhy(rhy2);
        harvestTaxationReport2.setHta(hta);
        harvestTaxationReport2.setHasTaxationPlanning(true);
        harvestTaxationReport2.setPlannedRemainingPopulation(2.5);
        harvestTaxationReport2.setGenderDistribution(1.5);
        harvestTaxationReport2.setYoungPercent(100);
        harvestTaxationReport2.setPlannedUtilizationRateOfThePermits(100);
        harvestTaxationReport2.setShareOfBankingPermits(0);
        harvestTaxationReport2.setPlannedPermitMin(1);
        harvestTaxationReport2.setPlannedPermitMax(400);
        harvestTaxationReport2.setPlannedCatchMin(1);
        harvestTaxationReport2.setPlannedCatchMax(200);
        harvestTaxationReport2.setPlannedPermitDensityMin(12.0);
        harvestTaxationReport2.setPlannedPermitDensityMax(13.0);
        harvestTaxationReport2.setPlannedPreyDensityMin(1.0);
        harvestTaxationReport2.setPlannedPreyDensityMax(200.0);
        harvestTaxationReport2.setPlannedCatchYoungPercent(23);
        harvestTaxationReport2.setPlannedCatchMalePercent(53);
        harvestTaxationReport2.setStakeholdersConsulted(today().minusDays(1));
        harvestTaxationReport2.setApprovedAtTheBoardMeeting(today().minusDays(1));
        harvestTaxationReport2.setJustification("");
        harvestTaxationReport2.setHarvestTaxationReportState(HarvestTaxationReportState.DRAFT);
        repository.save(harvestTaxationReport2);

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
        dto.setApprovedAtTheBoardMeeting(today().minusDays(1));
        dto.setJustification("");
        dto.setState(HarvestTaxationReportState.DRAFT);
        dto.setAttachments(new ArrayList<>());

    }

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testGetMooseAreas_successfully() {
        authenticate(coordinator);
        final Map<Integer, LocalisedString> htas = feature.getMooseAreas(rhy.getId());

        assertThat(htas.size(), is(1));
        assertThat(htas.get(hta.getId()).getFinnish(), is(hta.getNameFinnish()));
        assertThat(htas.get(hta.getId()).getSwedish(), is(hta.getNameSwedish()));
    }

    @Test
    public void testGetTaxationReportYears_successfully() {
        authenticate(coordinator);
        final List<Integer> years = feature.getTaxationReportYears(rhy.getId());

        assertThat(years.size(), is(1));
        assertThat(years.get(0), is(2021));
    }

    @Test
    public void testGetTaxationReportBySpeciesAndHuntingYear_successfully() {
        authenticate(coordinator);
        final HarvestTaxationReportDTO report = feature.getTaxationReportDTOBySpeciesAndHuntingYear(hta.getId(), rhy.getId(), species.getOfficialCode(), 2021);
        assertThat(report, is(notNullValue()));
        assertThat(report.getId(), is(notNullValue()));
    }

    @Test
    public void testGetTaxationReportBySpeciesAndHuntingYear_butItDoesNotExist() {
        authenticate(coordinator);
        final HarvestTaxationReportDTO report = feature.getTaxationReportDTOBySpeciesAndHuntingYear(hta.getId(), rhy.getId(), species.getOfficialCode(), 2019);
        assertThat(report, is(nullValue()));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsDraft_withTaxationPlanning() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.DRAFT);
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> {
            assertThat(responseDTO.getId(), is(notNullValue()));
            assertThat(responseDTO.getPlannedRemainingPopulation(), is(dto.getPlannedRemainingPopulation()));
            assertThat(responseDTO.getGenderDistribution(), is(dto.getGenderDistribution()));
            assertThat(responseDTO.getYoungPercent(), is(dto.getYoungPercent()));
            assertThat(responseDTO.getPlannedUtilizationRateOfThePermits(), is(dto.getPlannedUtilizationRateOfThePermits()));
            assertThat(responseDTO.getShareOfBankingPermits(), is(dto.getShareOfBankingPermits()));
            assertThat(responseDTO.getPlannedPermitMin(), is(dto.getPlannedPermitMin()));
            assertThat(responseDTO.getPlannedPermitMax(), is(dto.getPlannedPermitMax()));
            assertThat(responseDTO.getPlannedCatchMin(), is(dto.getPlannedCatchMin()));
            assertThat(responseDTO.getPlannedCatchMax(), is(dto.getPlannedCatchMax()));
            assertThat(responseDTO.getPlannedPreyDensityMin(), is(dto.getPlannedPreyDensityMin()));
            assertThat(responseDTO.getPlannedPreyDensityMax(), is(dto.getPlannedPreyDensityMax()));
            assertThat(responseDTO.getPlannedPermitDensityMin(), is(dto.getPlannedPermitDensityMin()));
            assertThat(responseDTO.getPlannedPermitDensityMax(), is(dto.getPlannedPermitDensityMax()));
            assertThat(responseDTO.getPlannedCatchYoungPercent(), is(dto.getPlannedCatchYoungPercent()));
            assertThat(responseDTO.getPlannedCatchMalePercent(), is(dto.getPlannedCatchMalePercent()));
            assertThat(responseDTO.getStakeholdersConsulted(), is(dto.getStakeholdersConsulted()));
            assertThat(responseDTO.getApprovedAtTheBoardMeeting(), is(dto.getApprovedAtTheBoardMeeting()));
            assertThat(responseDTO.getJustification(), is(dto.getJustification()));
        });
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsDraft_noTaxationPlanning() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.DRAFT);
        dto.setHasTaxationPlanning(false);
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> {
            assertThat(responseDTO.getId(), is(notNullValue()));

            assertNull(responseDTO.getGenderDistribution());
            assertNull(responseDTO.getYoungPercent());
            assertNull(responseDTO.getPlannedUtilizationRateOfThePermits());
            assertNull(responseDTO.getShareOfBankingPermits());
            assertNull(responseDTO.getPlannedPermitMin());
            assertNull(responseDTO.getPlannedPermitMax());
            assertNull(responseDTO.getPlannedCatchMin());
            assertNull(responseDTO.getPlannedCatchMax());
            assertNull(responseDTO.getPlannedPreyDensityMin());
            assertNull(responseDTO.getPlannedPreyDensityMax());
            assertNull(responseDTO.getPlannedPermitDensityMin());
            assertNull(responseDTO.getPlannedPermitDensityMax());
            assertNull(responseDTO.getPlannedCatchYoungPercent());
            assertNull(responseDTO.getPlannedCatchMalePercent());

            assertThat(responseDTO.getStakeholdersConsulted(), is(dto.getStakeholdersConsulted()));
            assertThat(responseDTO.getApprovedAtTheBoardMeeting(), is(dto.getApprovedAtTheBoardMeeting()));
            assertThat(responseDTO.getJustification(), is(dto.getJustification()));
        });
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidYear() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setHuntingYear(today().getYear() - 3);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidHTA() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setHtaId(-3);

        assertThrows(IllegalArgumentException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidRhy() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setHtaId(anotherHta.getId());  // this HTA doesn't contain given RHY

        assertThrows(IllegalArgumentException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidSpecies() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_ROE_DEER);  // invalid game species

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidBoardMeetingDate() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setApprovedAtTheBoardMeeting(today().plusDays(3));
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidStakeHolderConsultedDate() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setStakeholdersConsulted(today().plusDays(3));
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_approvedInBoardMeetingBeforeStakeHolderConsultedDate() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setApprovedAtTheBoardMeeting(today().minusDays(3));
        dto.setStakeholdersConsulted(today().minusDays(1));
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedRemainingPopulation() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedRemainingPopulation(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidGenderDistribution() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setGenderDistribution(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooSmallYoungPercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setYoungPercent(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooBigYoungPercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setYoungPercent(101);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooSmallPlannedUtilizationRateOfThePermits() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedUtilizationRateOfThePermits(-1);

        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooBigPlannedUtilizationRateOfThePermits() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedUtilizationRateOfThePermits(101);

        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooSmallShareOfBankingPermits() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setShareOfBankingPermits(-1);

        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooBigShareOfBankingPermits() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setShareOfBankingPermits(101);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPermitMin() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitMin(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPermitMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitMax(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedCatchMin() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchMin(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedCatchMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchMax(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPreyDensityMin() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPreyDensityMin(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPreyDensityMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPreyDensityMax(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPermitDensityMin() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitDensityMin(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_invalidPlannedPermitDensityMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitDensityMax(-1.0);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_plannedPermitMinGreaterThanMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitMin(10);
        dto.setPlannedPermitMax(9);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_plannedCatchMinGreaterThanMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchMin(10);
        dto.setPlannedCatchMax(9);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_plannedPreyDensityMinGreaterThanMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPreyDensityMin(10.0);
        dto.setPlannedPreyDensityMax(9.0);

        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_plannedPermitDensityMinGreaterThanMax() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedPermitDensityMin(10.0);
        dto.setPlannedPermitDensityMax(9.0);

        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooSmallPlannedCatchYoungPercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchYoungPercent(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooBigPlannedCatchYoungPercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchYoungPercent(101);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooSmallPlannedCatchMalePercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchMalePercent(-1);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning_tooBigPlannedCatchMalePercent() {
        authenticate(coordinator);

        dto.setState(HarvestTaxationReportState.CONFIRMED);
        dto.setPlannedCatchMalePercent(101);
        dto.setHuntingYear(2019);

        assertFalse(validator.validate(dto).isEmpty());

        assertThrows(ConstraintViolationException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_overdue() {
        final LocalDate today = new LocalDate(2021, 12, 30);
        MockTimeProvider.mockTime(today.toDate().getTime());

        authenticate(coordinator);
        dto.setState(HarvestTaxationReportState.CONFIRMED);
        assertThrows(IllegalFillingDateException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_overdue_asAdmin() {
        final LocalDate today = new LocalDate(2021, 12, 30);
        MockTimeProvider.mockTime(today.toDate().getTime());

        authenticate(admin);
        dto.setState(HarvestTaxationReportState.CONFIRMED);
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> assertThat(responseDTO.getId(), is(notNullValue())));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReport_alreadyConfirmed() {
        authenticate(coordinator);
        final HarvestTaxationReportDTO dto = harvestTaxationReportDTOTransformer.transform(report);
        final Integer initialPercent = dto.getYoungPercent();
        assertThat(initialPercent, is(100));  // just make sure that value truly changes
        dto.setYoungPercent(8);

        assertThrows(IllegalStateException.class, () -> feature.saveOrUpdateTaxationReport(dto));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReport_alreadyConfirmed_asAdmin() {
        authenticate(admin);
        final HarvestTaxationReportDTO dto = harvestTaxationReportDTOTransformer.transform(report);
        final Integer initialPercent = dto.getYoungPercent();
        assertThat(initialPercent, is(100));  // just make sure that value truly changes
        dto.setYoungPercent(8);

        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> assertThat(responseDTO.getYoungPercent(), is(8)));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_overdue_asModerator() {
        final LocalDate today = new LocalDate(2021, 12, 30);
        MockTimeProvider.mockTime(today.toDate().getTime());

        authenticate(moderator);
        dto.setState(HarvestTaxationReportState.CONFIRMED);
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> assertThat(responseDTO.getId(), is(notNullValue())));
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withNoTaxationPlanning() {
        authenticate(coordinator);
        dto = new HarvestTaxationReportDTO();
        dto.setHuntingYear(2021);
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setRhyId(rhy.getId());
        dto.setHtaId(hta.getId());
        dto.setHasTaxationPlanning(false);
        dto.setState(HarvestTaxationReportState.CONFIRMED);

        assertTrue(validator.validate(dto).isEmpty());

        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> {
            assertThat(responseDTO.getId(), is(notNullValue()));
            assertThat(responseDTO.getPlannedRemainingPopulation(), is(dto.getPlannedRemainingPopulation()));
            assertThat(responseDTO.getGenderDistribution(), is(dto.getGenderDistribution()));
            assertThat(responseDTO.getYoungPercent(), is(dto.getYoungPercent()));
            assertThat(responseDTO.getPlannedUtilizationRateOfThePermits(), is(dto.getPlannedUtilizationRateOfThePermits()));
            assertThat(responseDTO.getShareOfBankingPermits(), is(dto.getShareOfBankingPermits()));
            assertThat(responseDTO.getPlannedPermitMin(), is(dto.getPlannedPermitMin()));
            assertThat(responseDTO.getPlannedPermitMax(), is(dto.getPlannedPermitMax()));
            assertThat(responseDTO.getPlannedCatchMin(), is(dto.getPlannedCatchMin()));
            assertThat(responseDTO.getPlannedCatchMax(), is(dto.getPlannedCatchMax()));
            assertThat(responseDTO.getPlannedPreyDensityMin(), is(dto.getPlannedPreyDensityMin()));
            assertThat(responseDTO.getPlannedPreyDensityMax(), is(dto.getPlannedPreyDensityMax()));
            assertThat(responseDTO.getPlannedPermitDensityMin(), is(dto.getPlannedPermitDensityMin()));
            assertThat(responseDTO.getPlannedPermitDensityMax(), is(dto.getPlannedPermitDensityMax()));
            assertThat(responseDTO.getPlannedCatchYoungPercent(), is(dto.getPlannedCatchYoungPercent()));
            assertThat(responseDTO.getPlannedCatchMalePercent(), is(dto.getPlannedCatchMalePercent()));
            assertThat(responseDTO.getStakeholdersConsulted(), is(dto.getStakeholdersConsulted()));
            assertThat(responseDTO.getApprovedAtTheBoardMeeting(), is(dto.getApprovedAtTheBoardMeeting()));
            assertThat(responseDTO.getJustification(), is(dto.getJustification()));
        });

    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsConfirmed_withTaxationPlanning() {
        authenticate(coordinator);
        dto.setState(HarvestTaxationReportState.CONFIRMED);
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto);
        runInTransaction(() -> {
            assertThat(responseDTO.getId(), is(notNullValue()));
            assertThat(responseDTO.getPlannedRemainingPopulation(), is(dto.getPlannedRemainingPopulation()));
            assertThat(responseDTO.getGenderDistribution(), is(dto.getGenderDistribution()));
            assertThat(responseDTO.getYoungPercent(), is(dto.getYoungPercent()));
            assertThat(responseDTO.getPlannedUtilizationRateOfThePermits(), is(dto.getPlannedUtilizationRateOfThePermits()));
            assertThat(responseDTO.getShareOfBankingPermits(), is(dto.getShareOfBankingPermits()));
            assertThat(responseDTO.getPlannedPermitMin(), is(dto.getPlannedPermitMin()));
            assertThat(responseDTO.getPlannedPermitMax(), is(dto.getPlannedPermitMax()));
            assertThat(responseDTO.getPlannedCatchMin(), is(dto.getPlannedCatchMin()));
            assertThat(responseDTO.getPlannedCatchMax(), is(dto.getPlannedCatchMax()));
            assertThat(responseDTO.getPlannedPreyDensityMin(), is(dto.getPlannedPreyDensityMin()));
            assertThat(responseDTO.getPlannedPreyDensityMax(), is(dto.getPlannedPreyDensityMax()));
            assertThat(responseDTO.getPlannedPermitDensityMin(), is(dto.getPlannedPermitDensityMin()));
            assertThat(responseDTO.getPlannedPermitDensityMax(), is(dto.getPlannedPermitDensityMax()));
            assertThat(responseDTO.getPlannedCatchYoungPercent(), is(dto.getPlannedCatchYoungPercent()));
            assertThat(responseDTO.getPlannedCatchMalePercent(), is(dto.getPlannedCatchMalePercent()));
            assertThat(responseDTO.getStakeholdersConsulted(), is(dto.getStakeholdersConsulted()));
            assertThat(responseDTO.getApprovedAtTheBoardMeeting(), is(dto.getApprovedAtTheBoardMeeting()));
            assertThat(responseDTO.getJustification(), is(dto.getJustification()));
        });
    }

    @Test
    public void testSaveOrUpdateTaxationReport_saveReportAsDraft_withAttachments_andTheyExists() throws IOException {
        authenticate(coordinator);

        final MultipartFile file = newAttachment();
        final HarvestTaxationReportDTO responseDTO = feature.saveOrUpdateTaxationReport(dto, Collections.singletonList(file));
        runInTransaction(() -> {
            assertThat(responseDTO.getId(), is(notNullValue()));
            Asserts.assertThat(responseDTO.getAttachments(), hasSize(1));
            Asserts.assertThat(responseDTO.getAttachments().get(0).getFilename(), equalTo(file.getOriginalFilename()));
        });
    }

    @Test
    public void deleteAttachment_andItsGone() throws IOException {
        authenticate(coordinator);
        // Create item with an attachment.
        final MultipartFile file = newAttachment();

        final HarvestTaxationReportDTO saved = feature.saveOrUpdateTaxationReport(dto, Collections.singletonList(file));
        persistInNewTransaction();

        feature.deleteAttachment(saved.getAttachments().get(0).getId());
        persistInNewTransaction();

        runInTransaction(() -> {
            final HarvestTaxationReport entity = repository.findById(saved.getId()).orElseThrow(NotFoundException::new);
            Asserts.assertThat(entity.getAttachments(), hasSize(0));
        });
    }

    @Test(expected = NotFoundException.class)
    public void getAttachment_butItDoesNotExist() throws IOException {
        authenticate(coordinator);
        feature.getAttachment(1);
    }

    @Test
    public void getAttachment_successfully() throws IOException {
        authenticate(coordinator);
        // Create item with an attachment.
        final MultipartFile file = newAttachment();

        final HarvestTaxationReportDTO saved = feature.saveOrUpdateTaxationReport(dto, Collections.singletonList(file));
        persistInNewTransaction();

        final ResponseEntity<byte[]> downloadedFile = feature.getAttachment(saved.getAttachments().get(0).getId());
        Asserts.assertThat(downloadedFile.getBody(), equalTo(file.getBytes()));
    }

    @Test
    public void export_successfully() {
        authenticate(coordinator);

        final HarvestTaxationExcelDTO dto = new HarvestTaxationExcelDTO();
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setHuntingYear(2021);

        final List<HarvestTaxationReportExcelRowDTO> exportedData = feature.exportDataForTest(dto);

        assertThat(exportedData.size(), is(1));
        assertThat(exportedData.get(0).getRhyName().getFinnish(), is(rhy.getNameFinnish()));
    }

    @Test
    public void export_withNoResults() {
        authenticate(coordinator);

        final HarvestTaxationExcelDTO dto = new HarvestTaxationExcelDTO();
        dto.setGameSpeciesCode(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER);

        final List<HarvestTaxationReportExcelRowDTO> exportedData = feature.exportDataForTest(dto);

        assertThat(exportedData.size(), is(0));
    }

    private MultipartFile newAttachment() {
        final byte[] attachmentData = new byte[4096];
        new Random().nextBytes(attachmentData);
        final String filename = "test" + nextLong() + ".png";
        return new MockMultipartFile(filename, "//test/" + filename, "image/png", attachmentData);
    }
}
