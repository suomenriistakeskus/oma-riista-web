package fi.riista.feature.harvestpermit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import fi.riista.feature.account.user.UserCrudFeature;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.SimpleAbstractCrudFeature;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.harvestpermit.search.HarvestPermitExistsDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitFeature;
import fi.riista.feature.huntingclub.permit.MooselikeHuntingYearDTO;
import fi.riista.feature.huntingclub.permit.MooselikePermitListingDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.jpa.domain.Specifications.where;

@Component
public class HarvestPermitCrudFeature extends SimpleAbstractCrudFeature<Long, HarvestPermit, HarvestPermitDTO> {

    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitCrudFeature.class);

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private UserCrudFeature userCrudFeature;

    @Resource
    private HuntingClubPermitFeature huntingClubPermitFeature;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected JpaRepository<HarvestPermit, Long> getRepository() {
        return harvestPermitRepository;
    }

    @Override
    protected void updateEntity(HarvestPermit entity, HarvestPermitDTO dto) {
    }

    @Transactional
    public void updateContactPersons(Long permitId, List<HarvestPermitContactPersonDTO> contactPersons) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.UPDATE);

        permit.getContactPersons().clear();
        for (HarvestPermitContactPersonDTO cp : contactPersons) {
            // dto:s contact person hunter number can be empty if the person is same as original contact person
            if (StringUtils.isNotBlank(cp.getHunterNumber())) {
                personRepository.findByHunterNumber(cp.getHunterNumber()).ifPresent(p -> {
                    if (!permit.getOriginalContactPerson().equals(p)) {
                        permit.getContactPersons().add(new HarvestPermitContactPerson(permit, p));
                    }
                });
            }
        }
    }

    @Override
    protected Function<HarvestPermit, HarvestPermitDTO> entityToDTOFunction() {
        final SystemUser currentUser = activeUserService.getActiveUser();
        return input -> {
            HarvestPermitDTO dto = HarvestPermitDTO.create(Objects.requireNonNull(input), currentUser,
                    EnumSet.of(HarvestPermitDTO.Inclusion.HARVEST_LIST,
                            HarvestPermitDTO.Inclusion.REPORT_LIST,
                            HarvestPermitDTO.Inclusion.END_OF_HUNTING_REPORT_REQUIRED));

            if (dto.getHarvests() != null && !dto.getHarvests().isEmpty()) {
                resolveHarvestCreators(input, dto);
            }
            return dto;
        };
    }

    private void resolveHarvestCreators(HarvestPermit permit, HarvestPermitDTO dto) {
        final Map<Long, SystemUser> moderatorCreators = userCrudFeature.getModeratorCreatorsGroupedById(permit.getHarvests());
        Map<Long, SystemUser> harvestIdToCreator = Maps.newHashMap();
        for (Harvest h : permit.getHarvests()) {
            SystemUser creator = moderatorCreators.get(h.getCreatedByUserId());
            if (creator != null) {
                harvestIdToCreator.put(h.getId(), creator);
            }
        }

        for (HarvestPermitDTO.HarvestStub h : dto.getHarvests()) {
            SystemUser creator = harvestIdToCreator.get(h.getId());
            if (creator != null) {
                h.setCreator(creator.getFullName());
            }
        }
    }

    @Transactional
    public void acceptHarvest(Long harvestId, Integer harvestRev, Harvest.StateAcceptedToHarvestPermit toState) {
        final Harvest harvest = harvestRepository.getOne(harvestId);
        DtoUtil.assertNoVersionConflict(harvest, harvestRev);

        if (harvest.getHarvestPermit() == null) {
            throw new IllegalStateException("Harvest does not have permit");
        }
        if (!harvest.getHarvestPermit().isHarvestsAsList()) {
            throw new IllegalStateException("Harvest can be accepted only to permit which is reported as list");
        }

        final HarvestPermit permit = harvest.getHarvestPermit();
        activeUserService.assertHasPermission(permit, EntityPermission.UPDATE);

        if (permit.isMooselikePermitType() || permit.isAmendmentPermit()) {
            throw new IllegalStateException(String.format(
                    "Harvest can be accepted only to permit which is not type:%s permitId:%d",
                    permit.getPermitTypeCode(), permit.getId()));
        }

        assertHarvestLinkedToPermit(harvest, permit);
        assertStateChangeAllowed(harvest, toState);

        harvest.setStateAcceptedToHarvestPermit(toState);

        permit.forceRevisionUpdate();
    }

    private static void assertStateChangeAllowed(Harvest harvest, Harvest.StateAcceptedToHarvestPermit toState) {
        final HarvestPermit permit = harvest.getHarvestPermit();
        if (!permit.getUndeletedHarvestReports().isEmpty()) {
            boolean allowedStateNow = harvest.getStateAcceptedToHarvestPermit() == Harvest.StateAcceptedToHarvestPermit.PROPOSED;
            boolean allowedToState = toState == Harvest.StateAcceptedToHarvestPermit.REJECTED;
            if (!allowedStateNow || !allowedToState) {
                throw new IllegalStateException("Because Harvestreport exists for permit, harvest can be only rejected if it is proposed.");
            }
        }
    }

    private static void assertHarvestLinkedToPermit(Harvest harvest, HarvestPermit permit) {
        Preconditions.checkArgument(permit.equals(harvest.getHarvestPermit()));
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitDTO> listMyPermits(Long personId) {
        final SystemUser currentUser = activeUserService.getActiveUser();
        final Person person = findPerson(personId);

        return harvestPermitRepository.findAll(where(HarvestPermitSpecs.isPermitContactPerson(person))
                .and(HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT)).stream()
                .map(permit -> HarvestPermitDTO.create(permit, currentUser,
                        EnumSet.of(HarvestPermitDTO.Inclusion.END_OF_HUNTING_REPORT_REQUIRED)))
                .sorted(comparingLong(HasID::getId))
                .collect(toList());
    }

    private Person findPerson(Long personId) {
        if (activeUserService.isModeratorOrAdmin()) {
            return personRepository.getOne(personId);
        }
        return activeUserService.getActiveUser().getPerson();
    }

    @Transactional(readOnly = true)
    public Page<HarvestPermitDTO> listByRhy(long rhyId, Pageable pageRequest) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, EntityPermission.READ);

        final Page<HarvestPermit> harvestPermits = harvestPermitRepository.findByRhy(rhy, pageRequest);

        return DtoUtil.toDTO(harvestPermits, pageRequest, input -> HarvestPermitDTO.create(
                Objects.requireNonNull(input), null, EnumSet.of(HarvestPermitDTO.Inclusion.REPORT_LIST)));
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public long countAllPermitsRequiringAction() {
        return Optional.ofNullable(activeUserService.getActiveUser().getPerson())
                .map(person -> harvestPermitRepository.countPermitsRequiringAction(person.getId()))
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public List<GameSpeciesDTO> findPermitSpecies() {
        // no authorization needed
        return GameSpeciesDTO.transformList(gameSpeciesRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<HarvestPermitExistsDTO> preloadPermits() {
        return F.mapNonNullsToList(preloadPermitEntities(), HarvestPermitExistsDTO::create);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public List<HarvestPermit> preloadPermitEntities() {
        final Person person = activeUserService.getActiveUser().getPerson();

        if (person == null) {
            return Collections.emptyList();
        }

        final Specification<HarvestPermit> harvestsAsListAndReportNotDone =
                where(equal(HarvestPermit_.harvestsAsList, Boolean.TRUE))
                        .and(HarvestPermitSpecs.harvestReportNotDone());
        final Specification<HarvestPermit> harvestsNotAsList = equal(HarvestPermit_.harvestsAsList, Boolean.FALSE);

        return harvestPermitRepository.findAll(JpaSpecs.and(
                where(HarvestPermitSpecs.isPermitContactPerson(person))
                        .or(HarvestPermitSpecs.withHarvestAuthor(person))
                        .and(JpaSpecs.or(harvestsAsListAndReportNotDone, harvestsNotAsList)),
                HarvestPermitSpecs.IS_NOT_ANY_MOOSELIKE_PERMIT));
    }

    @Transactional(readOnly = true)
    public List<MooselikePermitListingDTO> listPermits(Long personId, int year, int officialCodeMoose) {
        final Person person = findPerson(personId);

        return harvestPermitRepository.findAll(JpaSpecs.and(
                equal(HarvestPermit_.originalContactPerson, person),
                HarvestPermitSpecs.validWithinHuntingYear(year),
                HarvestPermitSpecs.withSpeciesCode(officialCodeMoose),
                HarvestPermitSpecs.IS_MOOSELIKE_PERMIT)).stream()
                .map(p -> huntingClubPermitFeature.getPermitListingDTOWithoutAuthorization(p, officialCodeMoose, null))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public HuntingClubPermitDTO getPermit(long permitId, int officialCodeMoose) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return huntingClubPermitFeature.getPermitWithoutAuthorization(permit, officialCodeMoose, null);
    }

    @Transactional(readOnly = true)
    public OrganisationNameDTO getRhyCode(long permitId) {
        final HarvestPermit permit = requireEntityService.requireHarvestPermit(permitId, EntityPermission.READ);
        return OrganisationNameDTO.createWithOfficialCode(permit.getRhy());
    }

    @Transactional(readOnly = true)
    public List<MooselikeHuntingYearDTO> listHuntingYears(Long personId) {
        final Specification<HarvestPermit> spec =
                where(equal(HarvestPermit_.originalContactPerson, findPerson(personId)))
                        .and(HarvestPermitSpecs.IS_MOOSELIKE_PERMIT);

        return MooselikeHuntingYearDTO.create(harvestPermitRepository.findAll(spec).stream()
                .map(HarvestPermit::getSpeciesAmounts)
                .flatMap(Collection::stream));
    }

    @Transactional(readOnly = true, rollbackFor = IOException.class)
    public URL getPdf(final String permitNumber) throws MalformedURLException {
        final HarvestPermit permit = harvestPermitRepository.findByPermitNumber(permitNumber);
        activeUserService.assertHasPermission(permit, EntityPermission.READ);

        LOG.info("userId:{} loading permitNumber:{} pdf", activeUserService.getActiveUserId(), permitNumber);

        return new URL(permit.getPrintingUrl());
    }
}
