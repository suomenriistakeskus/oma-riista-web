package fi.riista.feature;

import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.union.PersonalAreaUnion;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.announcement.Announcement;
import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.moosedatacard.MooseDataCardImport;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.Riistakeskus;
import fi.riista.feature.organization.RiistakeskusRepository;
import fi.riista.feature.organization.calendar.Venue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.invoice.Invoice;
import fi.riista.feature.permit.invoice.batch.PermitDecisionInvoiceBatch;
import fi.riista.feature.permit.invoice.payment.InvoicePaymentLine;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestParticipant;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Objects;

@Component
@Transactional(readOnly = true, propagation = Propagation.MANDATORY)
public class RequireEntityService {

    @Resource
    private RiistakeskusRepository riistakeskusRepository;

    @Resource
    private ActiveUserService activeUserService;

    @PersistenceContext
    private EntityManager entityManager;

    public Person requirePerson(final Long id, final Enum<?> permission) {
        return require(id, Person.class, permission);
    }

    public Organisation requireOrganisation(final Long id, final Enum<?> permission) {
        return require(id, Organisation.class, permission);
    }

    public Venue requireVenue(final Long id, final Enum<?> permission) {
        return require(id, Venue.class, permission);
    }

    public Riistakeskus requireRiistakeskus(final Enum<?> permission) {
        final Riistakeskus rk = riistakeskusRepository.get();
        assertPermission(rk, permission);
        return rk;
    }

    public RiistakeskuksenAlue requireRiistakeskuksenAlue(final Long id, final Enum<?> permission) {
        return require(id, RiistakeskuksenAlue.class, permission);
    }

    public Riistanhoitoyhdistys requireRiistanhoitoyhdistys(final Long id, final Enum<?> permission) {
        return require(id, Riistanhoitoyhdistys.class, permission);
    }

    public Harvest requireHarvest(final Long id, final Enum<?> permission) {
        return require(id, Harvest.class, permission);
    }

    public Observation requireObservation(final Long id, final Enum<?> permission) {
        return require(id, Observation.class, permission);
    }

    public SrvaEvent requireSrvaEvent(final Long id, final Enum<?> permission) {
        return require(id, SrvaEvent.class, permission);
    }

    public HarvestPermit requireHarvestPermit(final Long id, final Enum<?> permission) {
        return require(id, HarvestPermit.class, permission);
    }

    public PermitDecision requirePermitDecision(final Long id, final Enum<?> permission) {
        return require(id, PermitDecision.class, permission);
    }

    public PermitDecisionInvoiceBatch requirePermitDecisionInvoiceBatch(final Long id, final Enum<?> permission) {
        return require(id, PermitDecisionInvoiceBatch.class, permission);
    }

    public Invoice requireInvoice(final Long id, final Enum<?> permission) {
        return require(id, Invoice.class, permission);
    }

    public InvoicePaymentLine requireInvoicePaymentLine(final Long id, final Enum<?> permission) {
        return require(id, InvoicePaymentLine.class, permission);
    }

    public HuntingClub requireHuntingClub(final Long id, final Enum<?> permission) {
        return require(id, HuntingClub.class, permission);
    }

    public HuntingClubGroup requireHuntingGroup(final Long id, final Enum<?> permission) {
        return require(id, HuntingClubGroup.class, permission);
    }

    public GroupHuntingDay requireHuntingGroupHuntingDay(final Long id, final Enum<?> permission) {
        return require(id, GroupHuntingDay.class, permission);
    }

    public HuntingClubArea requireHuntingClubArea(final Long id, final Enum<?> permission) {
        return require(id, HuntingClubArea.class, permission);
    }

    public PersonalArea requirePersonalArea(final Long id, final Enum<?> permission) {
        return require(id, PersonalArea.class, permission);
    }

    public ModeratorArea requireModeratorArea(final Long id, final Enum<?> permission) {
        return require(id, ModeratorArea.class, permission);
    }

    public HarvestPermitApplication requireHarvestPermitApplication(final Long id, final Enum<?> permission) {
        return require(id, HarvestPermitApplication.class, permission);
    }

    public MooseDataCardImport requireMooseDataCardImport(final Long id, final Enum<?> permission) {
        return require(id, MooseDataCardImport.class, permission);
    }

    public Announcement requireAnnouncement(final Long id, final Enum<?> permission) {
        return require(id, Announcement.class, permission);
    }

    public MooseHuntingSummary requireMooseHuntingSummary(final Long id, final Enum<?> permission) {
        return require(id, MooseHuntingSummary.class, permission);
    }

    public BasicClubHuntingSummary requireBasicClubHuntingSummary(final Long id, final Enum<?> permission) {
        return require(id, BasicClubHuntingSummary.class, permission);
    }

    public ShootingTestEvent requireShootingTestEvent(final Long id, final Enum<?> permission) {
        return require(id, ShootingTestEvent.class, permission);
    }

    public ShootingTestParticipant requireParticipant(final Long id, final Enum<?> permission) {
        return require(id, ShootingTestParticipant.class, permission);
    }

    public ShootingTestAttempt requireShootingTestAttempt(final Long id, final Enum<?> permission) {
        return require(id, ShootingTestAttempt.class, permission);
    }

    public RhyAnnualStatistics requireRhyAnnualStatistics(final Long id, final Enum<?> permission) {
        return require(id, RhyAnnualStatistics.class, permission);
    }

    public PersonalAreaUnion requireAccountAreaUnion(final Long id, final Enum<?> permission) {
        return require(id, PersonalAreaUnion.class, permission);
    }

    private <T extends BaseEntity<ID>, ID extends java.io.Serializable> T require(final ID id,
                                                                                  final Class<T> persistentClass,
                                                                                  final Enum<?> permission) {

        Objects.requireNonNull(id, persistentClass.getSimpleName() + " primary key is required");
        Objects.requireNonNull(permission, "permission not specified");

        final T entity = entityManager.find(persistentClass, id);

        if (entity == null) {
            throw new NotFoundException(String.format("%s with id=%s does not exist!",
                    persistentClass.getSimpleName(), id));
        }

        assertPermission(entity, permission);

        return entity;
    }

    private void assertPermission(final BaseEntity<?> entity, final Enum<?> permission) {
        if (permission != EntityPermission.NONE) {
            // Permission check
            activeUserService.assertHasPermission(entity, permission);
        }
    }
}
