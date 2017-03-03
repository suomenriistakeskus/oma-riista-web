package fi.riista.feature.harvestpermit.report;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenRepository;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenService;
import fi.riista.feature.gis.GISPoint;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.kiinteisto.CoordinatePropertyLookupFeature;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsRepository;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateHistory;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateHistoryRepository;
import fi.riista.feature.harvestpermit.season.HarvestQuota;
import fi.riista.feature.harvestpermit.season.HarvestQuotaRepository;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.harvestpermit.season.HarvestSeasonDTO;
import fi.riista.feature.harvestpermit.season.HarvestSeasonRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class HarvestReportCrudFeature extends AbstractCrudFeature<Long, HarvestReport, HarvestReportDTOBase> {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestReportCrudFeature.class);

    @Resource
    private HarvestReportRepository harvestReportRepository;

    @Resource
    private HarvestReportFieldsRepository harvestReportFieldsRepository;

    @Resource
    private HarvestReportStateHistoryRepository harvestReportStateHistoryRepository;

    @Resource
    private HarvestSeasonRepository harvestSeasonRepository;

    @Resource
    private HarvestQuotaRepository harvestQuotaRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestSpecimenRepository harvestSpecimenRepository;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private CoordinatePropertyLookupFeature coordinatePropertyLookupFeature;

    @Resource
    private HarvestSpecimenService specimenService;

    @Resource
    private HarvestDTOTransformer dtoTransformer;

    @Resource
    private HarvestReportRequirementsService harvestReportRequirementsService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected JpaRepository<HarvestReport, Long> getRepository() {
        return harvestReportRepository;
    }

    @Override
    protected HarvestReportDTOBase toDTO(@Nonnull final HarvestReport entity) {
        return entityToDTOFunction(Collections.emptyMap(), false, null).apply(entity);
    }

    public Function<HarvestReport, HarvestReportDTOBase> entityToDTOFunction(
            final Map<Long, SystemUser> moderatorCreators,
            final boolean includeHarvests,
            final Predicate<Harvest> harvestFilter) {

        return new Function<HarvestReport, HarvestReportDTOBase>() {
            @Nullable
            @Override
            public HarvestReportDTOBase apply(@Nullable HarvestReport input) {
                if (input == null) {
                    return null;
                }
                if (input.getHarvestPermit() != null && input.getHarvestPermit().isHarvestsAsList()) {
                    return HarvestReportForListPermitDTO.create(input, activeUserService.getActiveUser(),
                            moderatorCreators, includeHarvests, dtoTransformer, harvestFilter);
                }
                return HarvestReportSingleHarvestDTO.create(input, activeUserService.getActiveUser(), moderatorCreators);
            }
        };
    }

    @Override
    protected HarvestReport requireEntity(Long id, Enum<?> permission) {
        final HarvestReport harvestReport = super.requireEntity(id, permission);

        if (harvestReport.getState() == HarvestReport.State.DELETED) {
            throw new IllegalStateException("Cannot update deleted row");
        }

        return harvestReport;
    }

    @Override
    protected void delete(HarvestReport entity) {
        entity.setState(HarvestReport.State.DELETED);
        entity.softDelete();
        for (Harvest h : entity.getHarvests()) {
            h.setHarvestReport(null);
        }

        // if this is the end of hunt report, set it to null in permit
        HarvestPermit permit = entity.getHarvestPermit();
        if (permit != null) {
            HarvestReport eohr = permit.getEndOfHuntingReport();
            if (eohr != null && entity.getId().equals(eohr.getId())) {
                permit.setEndOfHuntingReport(null);
            }
        }
    }

    @Override
    protected void updateEntity(HarvestReport report, HarvestReportDTOBase dto) {
        if (dto instanceof HarvestReportSingleHarvestDTO) {
            updateSingleHarvestEntity(report, (HarvestReportSingleHarvestDTO) dto);
        } else {
            throw new RuntimeException("Only HarvestReportSingleHarvestDTO is supported, not supported:" + dto.getClass().getCanonicalName());
        }
    }

    private void updateSingleHarvestEntity(HarvestReport report, HarvestReportSingleHarvestDTO dto) {
        final HarvestPermit originalPermit = report.getHarvestPermit();
        final HarvestReportFields originalFields = report.getHarvests().isEmpty()
                ? null
                : report.getHarvests().iterator().next().getHarvestReportFields();

        final Riistanhoitoyhdistys rhy = riistanhoitoyhdistysRepository.getOne(dto.getRhyId());
        final HarvestReportFields fields = harvestReportFieldsRepository.getOne(dto.getFields().getId());
        final HarvestPermit permit = findPermitIfRequired(fields.getPermitNumber(), dto.getPermitNumber());

        // Current active user, role and optional person
        final SystemUser activeUser = activeUserService.getActiveUser();
        final Person activePerson = activeUser.getPerson();
        final SystemUser.Role role = activeUser.getRole();

        final GameSpecies species = fields.getSpecies();
        final Person author = findAuthor(report, dto, activePerson, role);
        final Person shooter = findShooter(author, dto);

        final Harvest harvest = findOrCreateHarvest(report, species, author, dto, permit);

        report.setAuthor(author);
        harvest.setAuthor(author);
        harvest.setActualShooter(shooter);

        updateBasicFields(fields, report, harvest, rhy, permit, role, dto);
        updateQuotaAndSeason(harvest, fields, rhy, dto.getHarvestSeason());


        // To be sure that report is consistent with harvest, we write these fields to harvest
//        updateHarvest(report, fields.getSpecies(), role, dto, harvest);

        HarvestReportAlreadyDoneException.assertHarvestReportNotDone(harvest, report);

        // If you set this before, hibernate will issue flush before selects
        // and throw exception because report might not be persisted yet
        harvest.setHarvestReport(report);
        report.addHarvest(harvest);

        // now when everything is set up, we can as ask report to initialise its state
        final boolean fieldsIsChanged = originalFields != null && !originalFields.equals(fields);
        final boolean permitChanged = originalPermit != null && !originalPermit.equals(permit);

        // when moderator is updating, we do not update the initialized state
        // otherwise, check if permit has changed
        if (report.isNew() || fieldsIsChanged || (!role.isModeratorOrAdmin() && permitChanged)) {
            report.initState(activeUser);
        }
    }


    private void updateBasicFields(final HarvestReportFields fields,
                                   final HarvestReport report,
                                   final Harvest harvest,
                                   final Riistanhoitoyhdistys rhy,
                                   final HarvestPermit harvestPermit,
                                   final SystemUser.Role role,
                                   final HarvestReportSingleHarvestDTO dto) {
        harvest.setHarvestReportFields(Objects.requireNonNull(fields, "fields is empty"));
        harvest.setSpecies(Objects.requireNonNull(fields.getSpecies(), "species is empty"));
        harvest.setRhy(Objects.requireNonNull(rhy, "rhy is empty"));
        harvest.setHarvestPermit(harvestPermit);
        report.setHarvestPermit(harvestPermit);

        harvest.setPointOfTime(dto.getPointOfTime().toDate());
        harvest.updateGeoLocation(dto.getGeoLocation(), gisQueryService);

        harvest.setHuntingAreaSize(fields.getHuntingAreaSize().nullifyIfNeeded(
                dto.getHuntingAreaSize(), "huntingAreaSize"));

        harvest.setHuntingMethod(fields.getHuntingMethod().nullifyIfNeeded(
                dto.getHuntingMethod(), "huntingMethod"));

        harvest.setReportedWithPhoneCall(fields.getReportedWithPhoneCall().nullifyIfNeeded(
                dto.getReportedWithPhoneCall(), "reportedWithPhoneCall"));

        harvest.setHuntingAreaType(fields.getHuntingAreaType().nullifyIfNeeded(
                dto.getHuntingAreaType(), "huntingAreaType"));

        if (StringUtils.isNotBlank(dto.getPropertyIdentifier())) {
            harvest.setPropertyIdentifier(dto.getPropertyIdentifier());
        }


        harvest.setHuntingParty(fields.getHuntingParty()
                .validateHuntingParty(dto.getHuntingParty(), harvest.getHuntingAreaType()));

        if (role.isModeratorOrAdmin()) {
            report.setDescription(dto.getDescription());
            harvest.setLukeStatus(dto.getLukeStatus());
        }

        final HarvestSpecimenDTO specimenDTO = new HarvestSpecimenDTO(
                fields.getGender().nullifyIfNeeded(dto.getGender(), "gender"),
                fields.getAge().nullifyIfNeeded(dto.getAge(), "age"),
                fields.getWeight().validateWeight(dto.getWeight(), harvest.getHuntingMethod())
        );
        final List<HarvestSpecimen> existingSpecimens = harvestSpecimenRepository.findByHarvest(harvest);

        if (!existingSpecimens.isEmpty()) {
            if (existingSpecimens.size() > 1) {
                throw new IllegalStateException(
                        "Harvest for which report is begin done should not have multiple specimens");
            }
            DtoUtil.copyBaseFields(existingSpecimens.iterator().next(), specimenDTO);
        }

        specimenService.setSpecimens(
                harvest, 1, Collections.singletonList(specimenDTO), HarvestSpecVersion.MOST_RECENT);
    }

    private Person findAuthor(HarvestReport report, HarvestReportSingleHarvestDTO dto, Person activePerson, SystemUser.Role role) {
        if (role.isModeratorOrAdmin()) {
            return personRepository.getOne(dto.getAuthorInfo().getId());
        } else if (report.isNew()) {
            return activePerson;
        }
        return report.getAuthor();
    }

    private Person findShooter(Person author, HarvestReportSingleHarvestDTO dto) {
        return F.hasId(dto.getHunterInfo())
                ? personRepository.getOne(dto.getHunterInfo().getId())
                : author;
    }

    private void updateQuotaAndSeason(final Harvest harvest,
                                      final HarvestReportFields fields,
                                      final Riistanhoitoyhdistys rhy,
                                      final HarvestSeasonDTO seasonDTO) {
        if (seasonDTO != null) {
            final HarvestSeason harvestSeason = harvestSeasonRepository.getOne(seasonDTO.getId());
            final HarvestQuota harvestQuota = harvestQuotaRepository.findByHarvestSeasonAndRhy(harvestSeason, rhy);

            if (harvestSeason.hasQuotas() && harvestQuota == null) {
                throw new IllegalArgumentException("Rhy not in harvest season");
            }

            harvest.setHarvestSeason(harvestSeason);
            harvest.setHarvestQuota(harvestQuota);

        } else {
            if (!fields.isUsedWithPermit()) {
                throw new IllegalStateException("Season is required for report when not used with permit");
            }

            harvest.setHarvestSeason(null);
            harvest.setHarvestQuota(null);
        }
    }

    private Harvest findOrCreateHarvest(
            final HarvestReport report,
            final GameSpecies species,
            final Person author,
            final HarvestReportSingleHarvestDTO dto,
            final HarvestPermit permit) {

        final Harvest harvest;

        if (report.isNew() && dto.getGameDiaryEntryId() != null) {
            // Lookup user specified harvest
            harvest = harvestRepository.getOne(dto.getGameDiaryEntryId());

        } else if (report.getHarvests().isEmpty()) {
            // Create missing Harvest
            final Harvest newHarvest = new Harvest(author, dto.getGeoLocation(), dto.getPointOfTime(), species, 1);
            newHarvest.setHarvestReportRequired(harvestReportRequirementsService.isHarvestReportRequired(
                    species, dto.getPointOfTime().toLocalDate(), dto.getGeoLocation(), permit));
            harvest = harvestRepository.save(newHarvest);

        } else if (report.getHarvests().size() == 1) {
            harvest = report.getHarvests().iterator().next();

        } else {
            throw new IllegalStateException("Report does not have harvest and cannot create");
        }

        return harvest;
    }

    private HarvestPermit findPermitIfRequired(final Required required, final String input) {
        final String permitNumber = required.nullifyIfNeeded(input, "permitNumber");

        if (required == Required.NO) {
            return null;
        }

        if (StringUtils.isBlank(permitNumber)) {
            throw new IllegalStateException("Permit number is blank");
        }

        final HarvestPermit permit = harvestPermitRepository.findByPermitNumber(permitNumber);

        if (permit == null) {
            throw new IllegalStateException("Harvest permit not found for permit number:" + permitNumber);
        }

        return permit;
    }

    @Transactional
    public HarvestReportDTOBase create(HarvestReportDTOBase dto, String reason) {
        return saveReasonForChangeIfGiven(reason, create(dto));
    }

    @Transactional
    public HarvestReportDTOBase update(HarvestReportDTOBase dto, String reason) {
        return saveReasonForChangeIfGiven(reason, update(dto));
    }

    private HarvestReportDTOBase saveReasonForChangeIfGiven(String reason, HarvestReportDTOBase newDto) {
        if (activeUserService.isModeratorOrAdmin()) {
            Preconditions.checkState(StringUtils.isNotBlank(reason), "Reason is required for moderator/admin");
        }

        if (StringUtils.isNotBlank(reason)) {
            final HarvestReport report = harvestReportRepository.getOne(newDto.getId());

            harvestReportStateHistoryRepository.save(new HarvestReportStateHistory(report, report.getState(), reason));
        }

        return read(newDto.getId());
    }

    @Transactional
    public void changeState(Long id, Integer rev, HarvestReport.State to, String reason, String propertyIdentifier) {
        final HarvestReport report = requireEntity(id, EntityPermission.UPDATE);

        DtoUtil.assertNoVersionConflict(report, rev);

        if (activeUserService.isModeratorOrAdmin() && to.requiresReason()) {
            Preconditions.checkState(StringUtils.isNotBlank(reason), "Reason is required for moderator/admin");
        }

        if (activeUserService.isModeratorOrAdmin() && to.requiresPropertyIdentifier()) {
            updatePropertyIdentifiers(report);
        }

        final HarvestReportStateHistory h = report.changeState(activeUserService.getActiveUser(), to);

        h.setMessage(reason);

        harvestReportStateHistoryRepository.save(h);
    }

    private void updatePropertyIdentifiers(HarvestReport report) {
        for (Harvest harvest : report.getHarvests()) {
            final GISPoint gisPoint = GISPoint.create(harvest.getGeoLocation());
            final List<MMLRekisteriyksikonTietoja> result = coordinatePropertyLookupFeature.findByPosition(gisPoint);

            if (result.isEmpty()) {
                LOG.warn("Could not find property identifier for point {}", harvest.getGeoLocation());
                continue;
            }

            final String selectedIdentifier = result.get(0).getPropertyIdentifier();

            if (result.size() > 1) {
                LOG.warn("Found multiple zones for location {}, extracting property identifier {} from the first one",
                        harvest.getGeoLocation(), selectedIdentifier);
            }

            harvest.setPropertyIdentifier(selectedIdentifier);
        }
    }

    @Transactional(readOnly = true)
    public List<HarvestReportDTOBase> listMine(Long personId) {
        Person person = activeUserService.getActiveUser().getPerson();
        if (person == null) {
            if (personId != null && activeUserService.isModeratorOrAdmin()) {
                person = personRepository.getOne(personId);
            } else {
                // should never get here
                return Collections.emptyList();
            }
        }
        final QHarvestReport harvestReport = QHarvestReport.harvestReport;
        final QHarvest harvest = QHarvest.harvest;
        final QHarvestPermit harvestPermit = QHarvestPermit.harvestPermit;

        final BooleanExpression harvestExistsAsShooter = JPAExpressions.selectFrom(harvest)
                .leftJoin(harvest.harvestPermit, harvestPermit)
                .where(harvest.harvestReport.id.eq(harvestReport.id)
                        .and(harvest.actualShooter.eq(person))
                        .and(harvestPermit.isNull().or(harvestPermit.harvestsAsList.eq(false)))
                )
                .exists();
        final List<HarvestReport> res = new JPAQuery<>(entityManager).from(harvestReport)
                .where(harvestReport.author.eq(person)
                        .or(harvestExistsAsShooter)
                        .and(harvestReport.state.ne(HarvestReport.State.DELETED)))
                .select(harvestReport)
                .fetch();
        return F.mapNonNullsToList(res, this::toDTO);
    }

    @Transactional
    public Long createForListPermit(Long permitId, Integer permitRev) {
        final HarvestPermit permit = harvestPermitRepository.getOne(permitId);

        DtoUtil.assertNoVersionConflict(permit, permitRev);
        Preconditions.checkState(permit.isHarvestsAsList(), "Harvest report can be created only to permit which is reported as list");
        activeUserService.assertHasPermission(permit, EntityPermission.UPDATE);

        assertEndOfHuntingReportNotDone(permit);
        assertNoHarvestsProposed(permit);

        final HarvestReport report = createEndOfHuntReportForPermit(permit);

        final Set<Harvest> harvests = filterHarvests(permit, Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        for (Harvest h : harvests) {
            h.setHarvestReport(report);
        }
        report.setHarvests(harvests);

        harvestReportRepository.saveAndFlush(report);

        return report.getId();
    }

    private HarvestReport createEndOfHuntReportForPermit(HarvestPermit permit) {
        final HarvestReport report = new HarvestReport();

        final SystemUser activeUser = activeUserService.getActiveUser();
        if (activeUser.getPerson() != null) {
            report.setAuthor(activeUser.getPerson());
        } else if (activeUserService.isModeratorOrAdmin()) {
            report.setAuthor(permit.getOriginalContactPerson());
        } else {
            throw new IllegalStateException("Current person is null but not moderator or admin.");
        }
        report.initState(activeUser);
        report.setHarvestPermit(permit);
        permit.setEndOfHuntingReport(report);
        return report;
    }

    private static void assertNoHarvestsProposed(HarvestPermit permit) {
        Set<Harvest> proposed = filterHarvests(permit, Harvest.StateAcceptedToHarvestPermit.PROPOSED);
        Preconditions.checkState(proposed.isEmpty(), "Can not create report to permit which has proposed harvests.");
    }

    private static Set<Harvest> filterHarvests(
            final HarvestPermit permit, final Harvest.StateAcceptedToHarvestPermit state) {

        return F.filterToSet(permit.getHarvests(), h -> h.getStateAcceptedToHarvestPermit() == state);
    }

    @Transactional
    public Long createEndOfHuntingReport(Long permitId, Integer permitRev) {
        final HarvestPermit permit = harvestPermitRepository.getOne(permitId);

        DtoUtil.assertNoVersionConflict(permit, permitRev);

        Preconditions.checkState(!permit.isHarvestsAsList(), "End of hunting report can be created only to permit which is NOT reported as list");
        assertEndOfHuntingReportNotDone(permit);
        activeUserService.assertHasPermission(permit, EntityPermission.UPDATE);

        final HarvestReport report = createEndOfHuntReportForPermit(permit);

        harvestReportRepository.saveAndFlush(report);
        return report.getId();
    }

    private static void assertEndOfHuntingReportNotDone(HarvestPermit permit) {
        Preconditions.checkState(permit.getEndOfHuntingReport() == null,
                "End of hunting report already exists, permitId:%s, eohr.id:%s",
                permit.getId(),
                permit.getEndOfHuntingReport() != null ? permit.getEndOfHuntingReport().getId() : null);
    }
}
