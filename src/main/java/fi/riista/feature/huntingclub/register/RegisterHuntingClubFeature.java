package fi.riista.feature.huntingclub.register;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.lupahallinta.LHOrganisationRepository;
import fi.riista.feature.organization.lupahallinta.LHOrganisationSearchDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import javaslang.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.util.jpa.JpaSpecs.equal;
import static java.util.stream.Collectors.toList;

@Component
public class RegisterHuntingClubFeature {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterHuntingClubFeature.class);

    public static final double MAX_FUZZY_DISTANCE_ORGANISATION_NAME = 0.7;
    public static final int MAX_RESULT_ORGANISATION = 5;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RegisterHuntingClubMailService registerHuntingClubMailService;

    @Resource
    private LHOrganisationRepository lhOrganisationRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private RegisterHuntingClubService registerHuntingClubService;

    @Resource
    private RiistanhoitoyhdistysRepository riistanhoitoyhdistysRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<LHOrganisationSearchDTO> findByName(final String queryString) {
        final List<LHOrganisation> finnishNameMatches = lhOrganisationRepository.findFuzzyFinnishName(
                queryString, MAX_FUZZY_DISTANCE_ORGANISATION_NAME,
                new PageRequest(0, MAX_RESULT_ORGANISATION));

        final List<LHOrganisation> swedishNameMatches = lhOrganisationRepository.findFuzzySwedishName(
                queryString, MAX_FUZZY_DISTANCE_ORGANISATION_NAME,
                new PageRequest(0, MAX_RESULT_ORGANISATION));

        return processSearchResults(F.concat(finnishNameMatches, swedishNameMatches));
    }

    @Transactional(readOnly = true)
    public List<LHOrganisationSearchDTO> findByOfficialCode(final String queryString) {
        final List<LHOrganisation> byOfficialCode = lhOrganisationRepository.findByOfficialCode(queryString);

        return processSearchResults(byOfficialCode);
    }

    private List<LHOrganisationSearchDTO> processSearchResults(final List<LHOrganisation> matches) {
        final List<String> officialCodeList = matches.stream()
                .map(LHOrganisation::getOfficialCode)
                .filter(StringUtils::hasText)
                .collect(toList());

        // Lookup table: clubOfficialCode -> contactPerson
        final Map<String, Person> activeClubs = findClubOfficialCodesWithActiveContactPerson(officialCodeList);

        return matches.stream()
                // Sanity check: filter invalid results which should not exist
                .filter(club -> club.getNameFinnish() != null && StringUtils.hasText(club.getOfficialCode()) &&
                        (club.getRhyOfficialCode() != null || club.getContactPersonRhy() != null) &&
                        rhyExists(club.getRhyOfficialCode()))
                .map(club -> {
                    final LHOrganisationSearchDTO dto = LHOrganisationSearchDTO.create(club);

                    // Enrich: Use locally managed information for club contactPerson
                    final Person existingContactPerson = activeClubs.get(dto.getOfficialCode());
                    if (existingContactPerson != null) {
                        dto.setHasActiveContactPerson(true);
                        dto.setContactPersonName(existingContactPerson.getFullName());
                    }

                    return dto;
                })
                .collect(toList());
    }

    private boolean rhyExists(final String rhyOfficialCode) {
        return riistanhoitoyhdistysRepository.count(equal(Organisation_.officialCode, rhyOfficialCode)) > 0;
    }

    private Map<String, Person> findClubOfficialCodesWithActiveContactPerson(final Collection<String> officialCodes) {
        final QOccupation occupation = QOccupation.occupation;
        final QOrganisation organisation = QOrganisation.organisation;

        final BooleanExpression clubOfficialCodeMatches = organisation.officialCode.in(officialCodes);
        final BooleanExpression isContactPerson = occupation.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO);

        // NOTE: If multiple occupation exist with same callOrder -> selects contactPerson randomly
        return new JPAQuery<>(entityManager)
                .from(occupation)
                .join(occupation.organisation, organisation)
                .where(occupation.validAndNotDeleted()
                        .and(clubOfficialCodeMatches)
                        .and(isContactPerson))
                .select(organisation.officialCode, occupation.person)
                .orderBy(occupation.callOrder.asc().nullsLast())
                .transform(GroupBy.groupBy(organisation.officialCode).as(occupation.person));
    }

    @Transactional
    public Map<String, Object> register(final LHOrganisationSearchDTO dto) {
        final SystemUser activeUser = activeUserService.getActiveUser();

        if (activeUser.getPerson() == null) {
            throw new IllegalStateException("No person associated with active user");
        }

        final LHOrganisation lhOrganisation = findLhOrg(dto);
        final String officialCode = lhOrganisation.getOfficialCode();

        final Tuple3<HuntingClub, Organisation, Boolean> t =
                registerHuntingClubService.findExistingOrCreate(officialCode, lhOrganisation);
        final HuntingClub club = t._1;
        final Organisation rhy = t._2;
        final boolean clubExists = t._3;

        if (clubExists) {
            final Optional<Occupation> activeContactPersons = occupationRepository.findActiveByOrganisationAndOccupationType(
                    club, OccupationType.SEURAN_YHDYSHENKILO).stream()
                    .findFirst();

            if (activeContactPersons.isPresent()) {
                return ImmutableMap.<String, Object> builder()
                        .put("result", "exists")
                        .put("contactPersonName", activeContactPersons.get().getPerson().getFullName())
                        .build();
            }
        }

        final Occupation contactPerson = createContactPerson(activeUser.getPerson(), club);

        sendNotificationEmail(club, activeUser, contactPerson, rhy);

        return Collections.singletonMap("result", "success");
    }

    private LHOrganisation findLhOrg(LHOrganisationSearchDTO dto) {
        final List<LHOrganisation> orgs = lhOrganisationRepository.findByOfficialCode(dto.getOfficialCode());
        if (orgs.isEmpty()) {
            throw new NotFoundException("LHOrganisation by officialCode:" + dto.getOfficialCode() + " is not found.");
        }
        return orgs.get(0);
    }

    @Nonnull
    private Occupation createContactPerson(final Person person, final HuntingClub club) {
        final Optional<Occupation> hasExistingOccupation = occupationRepository
                .findActiveByOrganisationAndPerson(club, person).stream()
                .filter(o -> EnumSet.of(
                        OccupationType.SEURAN_JASEN,
                        OccupationType.SEURAN_YHDYSHENKILO).contains(o.getOccupationType()))
                .findFirst();

        if (hasExistingOccupation.isPresent()) {
            final Occupation existingOccupation = hasExistingOccupation.get();
            existingOccupation.setEndDate(null);
            existingOccupation.setOrganisationAndOccupationType(club, OccupationType.SEURAN_YHDYSHENKILO);
            existingOccupation.getLifecycleFields().setDeletionTime(null);

            return existingOccupation;
        }

        final Occupation contactPerson = new Occupation(person, club, OccupationType.SEURAN_YHDYSHENKILO);
        contactPerson.setBeginDate(OrganisationType.CLUB.getBeginDateForNewOccupation());
        contactPerson.setContactInfoShare(ContactInfoShare.ALL_MEMBERS);

        return occupationRepository.save(contactPerson);
    }

    private void sendNotificationEmail(final HuntingClub club,
                                       final SystemUser activeUser,
                                       final Occupation contactPerson,
                                       final Organisation rhy) {
        // Send notification email after transaction has committed successfully
        final HuntingClubDTO clubDTO = HuntingClubDTO.create(club, false, null, null);
        final OccupationDTO occupationDTO = OccupationDTO.createWithPerson(contactPerson);
        final OrganisationNameDTO rhyDTO = OrganisationNameDTO.create(rhy);

        final String contactPersonEmail = activeUser.getEmail();
        final Iterable<String> rhyContactEmails = registerHuntingClubMailService.getRhyContactEmails(rhy);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    // NOTE: Must invoke through proxy to make sure new transaction is started
                    registerHuntingClubMailService.sendNotificationEmail(
                            clubDTO, occupationDTO, rhyDTO, rhyContactEmails, contactPersonEmail);

                } catch (RuntimeException ex) {
                    // Exception should be handled, so that HTTP status code is not altered
                    LOG.error("Error occured while sending notification emails", ex);
                }
            }
        });
    }
}
