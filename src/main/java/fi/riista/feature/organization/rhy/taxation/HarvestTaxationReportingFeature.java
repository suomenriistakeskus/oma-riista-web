package fi.riista.feature.organization.rhy.taxation;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.gis.hta.HirvitalousalueService;
import fi.riista.feature.gis.hta.RHYHirvitalousalueId;
import fi.riista.feature.gis.hta.RHYHirvitalousalueRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.LocalisedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;
import static fi.riista.util.DateUtil.today;
import static java.util.stream.Collectors.toList;


@Service
public class HarvestTaxationReportingFeature extends AbstractCrudFeature<Long, HarvestTaxationReport, HarvestTaxationReportDTO> {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestTaxationReportingFeature.class);

    public static final int LAST_FILLING_MONTH = 4;
    public static final int LAST_FILLING_DAY_OF_MONTH = 30;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HarvestTaxationRepository harvestTaxationRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private HarvestTaxationReportDTOTransformer harvestTaxationReportDTOTransformer;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestTaxationReportAttachmentService attachmentService;

    @Resource
    private HarvestTaxationReportAttachmentRepository attachmentRepository;

    @Resource
    private HirvitalousalueService hirvitalousalueService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private MessageSource messageSource;

    @Resource
    private RHYHirvitalousalueRepository rhyHirvitalousalueRepository;

    @Transactional(readOnly = true)
    public Map<Integer, LocalisedString> getMooseAreas(final long rhyId) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, READ);

        return hirvitalousalueService.findByRHY(rhy);
    }

    @Transactional(readOnly = true)
    public List<Integer> getTaxationReportYears(
            final long rhyId) {
        LOG.info("Get taxation report years. RHY: " + rhyId);

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, READ);
        return harvestTaxationRepository.listTaxationReportYears(rhy);
    }

    @Transactional(readOnly = true)
    public HarvestTaxationReportDTO getTaxationReportDTOBySpeciesAndHuntingYear(
            final int htaId,
            final long rhyId,
            final int gameSpeciesCode,
            final int huntingYear) {
        final Optional<HarvestTaxationReport> report = getTaxationReportBySpeciesAndHuntingYear(htaId, rhyId, gameSpeciesCode, huntingYear);

        return report.map(harvestTaxationReport -> harvestTaxationReportDTOTransformer.transform(harvestTaxationReport)).orElse(null);
    }

    private Optional<HarvestTaxationReport> getTaxationReportBySpeciesAndHuntingYear(
            final int htaId,
            final long rhyId,
            final int gameSpeciesCode,
            final int huntingYear) {
        LOG.info(
                String.format("Get taxation report by species and hunting year. HTA: %d, RHY: %d, Game Species code: %d, Hunting year: %d",
                        htaId, rhyId, gameSpeciesCode, huntingYear));

        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, READ);

        final GISHirvitalousalue hta = hirvitalousalueService.getOne(htaId);
        if (hta == null) {
            throw new IllegalArgumentException("Invalid hta: " + htaId);
        }

        final QHarvestTaxationReport TAX = QHarvestTaxationReport.harvestTaxationReport;

        final BooleanExpression predicate = TAX.rhy.eq(rhy)
                .and(TAX.huntingYear.eq(huntingYear))
                .and(TAX.species.officialCode.eq(gameSpeciesCode))
                .and(TAX.hta.eq(hta));

        return harvestTaxationRepository.findOne(predicate);
    }

    @Transactional(rollbackFor = IOException.class)
    public HarvestTaxationReportDTO saveOrUpdateTaxationReport(final HarvestTaxationReportDTO taxationReportDTO,
                                                               final List<MultipartFile> attachments) throws IOException {
        LOG.info("save or update with attachments. ID: " + taxationReportDTO.getId());

        final HarvestTaxationReport report = saveOrUpdate(taxationReportDTO);
        attachmentService.addAttachments(report, attachments);

        report.forceRevisionUpdate();
        return harvestTaxationReportDTOTransformer.transform(report);
    }

    @Transactional
    public HarvestTaxationReportDTO saveOrUpdateTaxationReport(final HarvestTaxationReportDTO taxationReportDTO) {
        LOG.info("save or update. ID: " + taxationReportDTO.getId());
        return harvestTaxationReportDTOTransformer.transform(saveOrUpdate(taxationReportDTO));
    }

    @Transactional(rollbackFor = IOException.class)
    public void deleteAttachment(final long id) {
        LOG.info("delete attachment. ID: " + id);

        final HarvestTaxationReportAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HarvestTaxationReportAttachment not found, id:" + id));

        final Organisation rhy = attachment.getHarvestTaxationReport().getRhy();
        activeUserService.assertHasPermission(rhy, UPDATE);

        attachmentService.delete(attachment);
        attachment.getHarvestTaxationReport().forceRevisionUpdate();
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public ResponseEntity<byte[]> getAttachment(final long id) throws IOException {
        LOG.info("get attachment. ID: " + id);

        final HarvestTaxationReportAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HarvestTaxationReportAttachment not found, id:" + id));

        final Organisation rhy = attachment.getHarvestTaxationReport().getRhy();
        activeUserService.assertHasPermission(rhy, READ);

        return attachmentService.getAttachment(attachment);
    }

    @Transactional(readOnly = true)
    public HarvestTaxationReportExportView export(
            final HarvestTaxationExcelDTO dto) {

        Riistanhoitoyhdistys rhy = null;
        if (dto.getRhyId() != null) {  // if rhyId is not set we search whole country
            rhy = requireEntityService.requireRiistanhoitoyhdistys(dto.getRhyId(), READ);
        } else {
            userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();
        }

        final GameSpecies species = gameSpeciesRepository.findByOfficialCode(dto.getGameSpeciesCode()).orElseThrow(
                () -> new IllegalArgumentException("Given game species doesn't exist.")
        );

        final List<HarvestTaxationReportExcelRowDTO> data = exportData(
                dto.getGameSpeciesCode(),
                dto.getHuntingYear(),
                rhy);

        return new HarvestTaxationReportExportView(new EnumLocaliser(messageSource), species, dto.getHuntingYear(), rhy, data);
    }

    @Transactional(readOnly = true)
    public List<HarvestTaxationReportExcelRowDTO> exportDataForTest(final HarvestTaxationExcelDTO dto) {
        Riistanhoitoyhdistys rhy = null;
        if (dto.getRhyId() != null) {  // if rhyId is not set we search whole country
            rhy = requireEntityService.requireRiistanhoitoyhdistys(dto.getRhyId(), READ);
        } else {
            userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();
        }

        gameSpeciesRepository.findByOfficialCode(dto.getGameSpeciesCode()).orElseThrow(
                () -> new IllegalArgumentException("Given game species doesn't exist.")
        );

        return exportData(
                dto.getGameSpeciesCode(),
                dto.getHuntingYear(),
                rhy);
    }

    private List<HarvestTaxationReportExcelRowDTO> exportData(final int gameSpeciesCode,
                                                              final int huntingYear,
                                                              final Riistanhoitoyhdistys rhy) {
        final QHarvestTaxationReport TAX = QHarvestTaxationReport.harvestTaxationReport;
        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        BooleanExpression predicate =
                TAX.huntingYear.eq(huntingYear)
                        .and(TAX.species.officialCode.eq(gameSpeciesCode));

        if (rhy != null) {  // if rhyId is not set we search whole country
            predicate = predicate.and(TAX.rhy.eq(rhy));
        }

        final Map<RHYHirvitalousalueId, Double> landAreaSizes = rhyHirvitalousalueRepository.getLandAreaSizes();

        return jpqlQueryFactory.selectFrom(TAX)
                .innerJoin(TAX.rhy, RHY)
                .where(predicate)
                .orderBy(TAX.hta.id.asc(), RHY.nameFinnish.asc()).fetch().stream().map(t -> new HarvestTaxationReportExcelRowDTO(t, landAreaSizes.get(new RHYHirvitalousalueId(t.getRhy().getId(), t.getHta().getId()))
                )).collect(toList());
    }

    @Transactional
    public HarvestTaxationReport saveOrUpdate(final HarvestTaxationReportDTO taxationReportDTO) {
        final int todayYear = today().getYear();
        final org.joda.time.LocalDate lastFillingDateOfTheYear = new org.joda.time.LocalDate(todayYear, LAST_FILLING_MONTH, LAST_FILLING_DAY_OF_MONTH);

        final SystemUser user = activeUserService.requireActiveUser();

        // admin and moderator has permission to bypass filling date validation
        if (!user.getRole().isModeratorOrAdmin() && today().isAfter(lastFillingDateOfTheYear)) {
            // Reporting time period is closed
            throw new IllegalFillingDateException();
        }

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(taxationReportDTO.getRhyId());
        activeUserService.assertHasPermission(rhy, UPDATE);

        validateThatRhyBelongsToGivenHta(taxationReportDTO);

        final HarvestTaxationReport harvestTaxationReport;
        if (taxationReportDTO.getId() != null) {
            harvestTaxationReport = harvestTaxationRepository.getOne(taxationReportDTO.getId());
            if (!user.getRole().isModeratorOrAdmin() &&
                    harvestTaxationReport.getHarvestTaxationReportState().equals(HarvestTaxationReportState.CONFIRMED)) {
                throw new IllegalStateException("Taxation report has already been confirmed");
            }
        } else {
            final Optional<HarvestTaxationReport> report = getTaxationReportBySpeciesAndHuntingYear(taxationReportDTO.getHtaId(),
                    taxationReportDTO.getRhyId(),
                    taxationReportDTO.getGameSpeciesCode(),
                    taxationReportDTO.getHuntingYear());
            harvestTaxationReport = report.orElseGet(HarvestTaxationReport::new);
        }

        updateEntity(harvestTaxationReport, taxationReportDTO);
        return harvestTaxationRepository.save(harvestTaxationReport);
    }

    private void validateThatRhyBelongsToGivenHta(final HarvestTaxationReportDTO taxationReportDTO) {
        final GISHirvitalousalue hta = hirvitalousalueService.getOne(taxationReportDTO.getHtaId());
        if (hta == null) {
            throw new IllegalArgumentException("Invalid hta: " + taxationReportDTO.getHtaId());
        }

        final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
        final BooleanExpression predicate = RHY.relatedHtas.contains(hta).and(RHY.id.eq(taxationReportDTO.getRhyId()));
        riistanhoitoyhdistysRepository.findOne(predicate).orElseThrow(() -> new IllegalArgumentException("Invalid rhy: " + taxationReportDTO.getRhyId()));
    }

    @Override
    protected JpaRepository<HarvestTaxationReport, Long> getRepository() {
        return harvestTaxationRepository;
    }

    @Override
    protected void updateEntity(final HarvestTaxationReport entity, final HarvestTaxationReportDTO dto) {
        final GISHirvitalousalue hta = hirvitalousalueService.getOne(dto.getHtaId());
        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(dto.getRhyId());

        entity.setHuntingYear(dto.getHuntingYear());

        gameSpeciesRepository.findByOfficialCode(dto.getGameSpeciesCode()).ifPresent(entity::setSpecies);

        entity.setRhy(rhy);
        entity.setHta(hta);

        entity.setHasTaxationPlanning(dto.getHasTaxationPlanning());

        if (dto.getHasTaxationPlanning()) {
            entity.setPlanningBasisPopulation(dto.getPlanningBasisPopulation());
            entity.setPlannedRemainingPopulation(dto.getPlannedRemainingPopulation());
            entity.setGenderDistribution(dto.getGenderDistribution());
            entity.setYoungPercent(dto.getYoungPercent());
            entity.setPlannedUtilizationRateOfThePermits(dto.getPlannedUtilizationRateOfThePermits());
            entity.setShareOfBankingPermits(dto.getShareOfBankingPermits());
            entity.setPlannedPermitMin(dto.getPlannedPermitMin());
            entity.setPlannedPermitMax(dto.getPlannedPermitMax());
            entity.setPlannedCatchMin(dto.getPlannedCatchMin());
            entity.setPlannedCatchMax(dto.getPlannedCatchMax());
            entity.setPlannedPreyDensityMin(dto.getPlannedPreyDensityMin());
            entity.setPlannedPreyDensityMax(dto.getPlannedPreyDensityMax());
            entity.setPlannedPermitDensityMin(dto.getPlannedPermitDensityMin());
            entity.setPlannedPermitDensityMax(dto.getPlannedPermitDensityMax());
            entity.setPlannedCatchYoungPercent(dto.getPlannedCatchYoungPercent());
            entity.setPlannedCatchMalePercent(dto.getPlannedCatchMalePercent());
        } else {
            entity.setPlanningBasisPopulation(null);
            entity.setPlannedRemainingPopulation(null);
            entity.setGenderDistribution(null);
            entity.setYoungPercent(null);
            entity.setPlannedUtilizationRateOfThePermits(null);
            entity.setShareOfBankingPermits(null);
            entity.setPlannedPermitMin(null);
            entity.setPlannedPermitMax(null);
            entity.setPlannedCatchMin(null);
            entity.setPlannedCatchMax(null);
            entity.setPlannedPreyDensityMin(null);
            entity.setPlannedPreyDensityMax(null);
            entity.setPlannedPermitDensityMin(null);
            entity.setPlannedPermitDensityMax(null);
            entity.setPlannedCatchYoungPercent(null);
            entity.setPlannedCatchMalePercent(null);
        }
        entity.setStakeholdersConsulted(dto.getStakeholdersConsulted());
        entity.setApprovedAtTheBoardMeeting(dto.getApprovedAtTheBoardMeeting());
        entity.setJustification(dto.getJustification());

        entity.setHarvestTaxationReportState(dto.getState());
    }

    @Override
    protected HarvestTaxationReportDTO toDTO(@Nonnull final HarvestTaxationReport entity) {
        return harvestTaxationReportDTOTransformer.transform(entity);
    }
}
