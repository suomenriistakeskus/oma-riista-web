package fi.riista.feature.permit.application;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.members.HuntingClubContactService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.permit.PermitNumberUtil;
import fi.riista.feature.permit.application.email.HarvestPermitApplicationNotificationService;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionAmendUpdater;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.action.PermitDecisionActionRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class HarvestPermitApplicationFeature {
    private static final Logger LOG = LoggerFactory.getLogger(HarvestPermitApplicationFeature.class);

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HarvestPermitApplicationLockedCondition harvestPermitApplicationLockedCondition;

    @Resource
    private HarvestPermitApplicationNotificationService harvestPermitApplicationNotificationService;

    @Resource
    private HarvestPermitApplicationNumberService harvestPermitNumberService;

    @Resource
    private PermitDecisionAmendUpdater permitDecisionAmendUpdater;

    @Resource
    private HuntingClubContactService huntingClubContactService;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private HarvestPermitAreaRepository harvestPermitAreaRepository;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private SecureRandom secureRandom;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public HarvestPermitApplicationBasicDetailsDTO getBasicDetails(final long applicationId) {
        return new HarvestPermitApplicationBasicDetailsDTO(readApplication(applicationId));
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationSummaryDTO getAllDetails(final long applicationId) {
        final HarvestPermitApplication application = readApplication(applicationId);

        final Map<Long, List<Person>> contactPersonMapping =
                huntingClubContactService.getContactPersonsSorted(application.getPermitPartners());

        final Map<Long, String> moderatorIndex = userRepository.getModeratorFullNames(Collections.singletonList(application));
        final String moderatorName = moderatorIndex.get(application.getCreatedByUserId());
        return HarvestPermitApplicationSummaryDTO.create(application, contactPersonMapping, moderatorName);
    }

    private HarvestPermitApplication readApplication(final long applicationId) {
        return requireEntityService.requireHarvestPermitApplication(applicationId, EntityPermission.READ);
    }

    private HarvestPermitApplication updateApplication(final long applicationId) {
        final HarvestPermitApplication result = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);
        harvestPermitApplicationLockedCondition.assertCanUpdate(result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<HuntingClubDTO> listAvailablePermitHolders(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                applicationId, EntityPermission.UPDATE);

        return F.mapNonNullsToList(findClubsAvailableForApplicationPermitHolder(application),
                c -> HuntingClubDTO.create(c, true, null, null));
    }

    private List<HuntingClub> findClubsAvailableForApplicationPermitHolder(final HarvestPermitApplication application) {
        final List<Long> contactPersonClubIds = occupationRepository.findActiveByPersonAndOrganisationTypes(
                application.getContactPerson(), EnumSet.of(OrganisationType.CLUB)).stream()
                .filter(occ -> occ.getOccupationType() == OccupationType.SEURAN_YHDYSHENKILO)
                .map(Occupation::getOrganisation)
                .map(Organisation::getId)
                .collect(Collectors.toList());

        final QHuntingClub CLUB = QHuntingClub.huntingClub;

        final Long currentPermitHolderId = F.getId(application.getPermitHolder());

        final Predicate predicate = new BooleanBuilder()
                // Club is a person and equal to application contact person
                .or(CLUB.clubPerson.eq(application.getContactPerson()))
                // Application contact person is club contact person
                .or(CLUB.id.in(contactPersonClubIds))
                // Always include currently selected permitHolder
                .or(currentPermitHolderId != null ? CLUB.id.eq(currentPermitHolderId) : null);

        return huntingClubRepository.findAllAsList(predicate);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public HuntingClubDTO findClubByOfficialCode(final String officialCode) {
        final HuntingClub club = huntingClubRepository.findByOfficialCode(officialCode);
        if (club == null) {
            throw new NotFoundException("Club not found by officialCode:" + officialCode);
        }
        return HuntingClubDTO.create(club, true, null, null);
    }

    @Transactional
    public void updatePermitHolder(final long applicationId, final HuntingClubDTO permitHolder) {
        final HarvestPermitApplication application = updateApplication(applicationId);

        application.setPermitHolder(F.getId(permitHolder) != null
                ? huntingClubRepository.getOne(permitHolder.getId())
                : null);
    }

    @Transactional(readOnly = true)
    public HarvestPermitApplicationShooterCountDTO getShooterCounts(final long applicationId) {
        final HarvestPermitApplication application = readApplication(applicationId);
        return HarvestPermitApplicationShooterCountDTO.create(application);
    }

    @Transactional
    public void updateShooterCounts(final long applicationId,
                                    final HarvestPermitApplicationShooterCountDTO dto) {
        final HarvestPermitApplication application = updateApplication(applicationId);

        application.setShooterOnlyClub(dto.getShooterOnlyClub());
        application.setShooterOtherClubActive(dto.getShooterOtherClubActive());
        application.setShooterOtherClubPassive(dto.getShooterOtherClubPassive());
    }

    @Transactional
    public void updateAdditionalData(final long applicationId, final HarvestPermitApplicationAdditionalDataDTO dto) {
        final HarvestPermitApplication application = updateApplication(applicationId);

        application.setDeliveryByMail(dto.isDeliveryByMail());
        application.setEmail1(dto.getEmail1());
        application.setEmail2(dto.getEmail2());
    }

    @Transactional
    public HarvestPermitApplicationBasicDetailsDTO create(final HarvestPermitApplicationCreateDTO dto) {
        final GISZone zone = gisZoneRepository.save(new GISZone());

        final HarvestPermitArea permitArea = new HarvestPermitArea();
        permitArea.setHuntingYear(dto.getHuntingYear());
        permitArea.setZone(zone);
        permitArea.generateAndStoreExternalId(secureRandom);
        harvestPermitAreaRepository.save(permitArea);

        final HarvestPermitApplication application = new HarvestPermitApplication();
        application.setArea(permitArea);
        application.setPermitTypeCode(dto.getPermitTypeCode());
        application.setApplicationName(dto.getApplicationName());
        application.setContactPerson(resolveContactPerson(dto));
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setHuntingYear(dto.getHuntingYear());

        harvestPermitApplicationRepository.saveAndFlush(application);

        return new HarvestPermitApplicationBasicDetailsDTO(application);
    }

    private Person resolveContactPerson(final HarvestPermitApplicationCreateDTO dto) {
        if (activeUserService.isModeratorOrAdmin()) {
            return personRepository.getOne(dto.getPersonId());
        }
        return activeUserService.requireActivePerson();
    }

    @Transactional(readOnly = true)
    public void validate(final long applicationId) {
        new HarvestPermitApplicationValidator(readApplication(applicationId)).validateContent();
    }

    @Transactional
    public void sendApplication(final HarvestPermitApplicationSendDTO dto) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                dto.getId(), EntityPermission.UPDATE);

        // validate
        new HarvestPermitApplicationValidator(application).validateForSending();

        final int applicationNumber = harvestPermitNumberService.getNextApplicationNumber();
        final String permitNumber = PermitNumberUtil.createPermitNumber(
                application.getHuntingYear(), 1, applicationNumber);

        application.setStatus(HarvestPermitApplication.Status.ACTIVE);
        application.setApplicationNumber(applicationNumber);
        application.setPermitNumber(permitNumber);
        application.setSubmitDate(activeUserService.isModeratorOrAdmin() && dto.getSubmitDate() != null
                ? dto.getSubmitDate().toDateTime(new LocalTime(12, 0))
                : DateUtil.now());
        application.getArea().setStatusLocked();
    }

    @Transactional
    public void startAmendApplication(final long applicationId) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(applicationId,
                HarvestPermitApplicationAuthorization.Permission.AMEND);
        final PermitDecision permitDecision = requireDecision(application);
        permitDecision.assertStatus(PermitDecision.Status.DRAFT);

        application.startAmending();

        if (application.getArea() != null && application.getArea().getStatus() == HarvestPermitArea.StatusCode.LOCKED) {
            application.getArea().setStatusUnlocked();
        }
    }

    @Transactional
    public void stopAmendApplication(final HarvestPermitApplicationAmendDTO dto) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(dto.getId(),
                HarvestPermitApplicationAuthorization.Permission.AMEND);
        final PermitDecision permitDecision = requireDecision(application);

        // validate
        new HarvestPermitApplicationValidator(application).validateForAmend();
        application.stopAmending();

        if (application.getArea() != null) {
            application.getArea().setStatusLocked();
        }

        if (dto.getSubmitDate() != null) {
            final LocalTime originalLocalTime = application.getSubmitDate().toLocalTime();
            application.setSubmitDate(dto.getSubmitDate().toDateTime(originalLocalTime));
        }

        final PermitDecisionAction action = new PermitDecisionAction();
        action.setPermitDecision(permitDecision);
        action.setPointOfTime(DateUtil.now());
        action.setText(dto.getChangeReason());
        action.setActionType(PermitDecisionAction.ActionType.TAYDENNYS);

        permitDecisionActionRepository.save(action);

        permitDecisionAmendUpdater.updateDecision(permitDecision);
    }

    private PermitDecision requireDecision(final HarvestPermitApplication application) {
        final PermitDecision decision = permitDecisionRepository.findOneByApplication(application);

        return Objects.requireNonNull(decision, "Decision not available for application");
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asyncSendNotification(final long applicationId) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(applicationId);

        final Long createdByUserId = application.getAuditFields().getCreatedByUserId();

        if (createdByUserId != null && userRepository.isModeratorOrAdmin(createdByUserId)) {
            LOG.warn("Not sending notification email for application id={} created by moderator", applicationId);
            return;
        }

        harvestPermitApplicationNotificationService.sendNotification(application);
    }
}
