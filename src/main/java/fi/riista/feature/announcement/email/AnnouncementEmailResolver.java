package fi.riista.feature.announcement.email;

import com.google.common.base.Preconditions;
import fi.riista.feature.announcement.AnnouncementSubscriber;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

@Service
public class AnnouncementEmailResolver {

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<String> collectReceiverEmails(final Organisation fromOrganisation,
                                             final List<AnnouncementSubscriber> subscribers) {
        final EnumSet<OccupationType> targetEmailOccupationTypes = subscribers.stream()
                .map(AnnouncementSubscriber::getOccupationType)
                .collect(toCollection(() -> EnumSet.noneOf(OccupationType.class)));

        return collectReceiverEmails(fromOrganisation, targetEmailOccupationTypes);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<String> collectReceiverEmails(final Organisation fromOrganisation,
                                             final Set<OccupationType> occupationTypes) {
        final EnumSet<OccupationType> targetEmailOccupationTypes = EnumSet.copyOf(occupationTypes);

        // Make sure announcements targeted to all club members include contact persons
        if (targetEmailOccupationTypes.contains(OccupationType.SEURAN_JASEN)) {
            targetEmailOccupationTypes.add(OccupationType.SEURAN_YHDYSHENKILO);
        }

        return getOccupations(fromOrganisation).stream()
                .filter(occ -> targetEmailOccupationTypes.contains(occ.getOccupationType()))
                .map(Occupation::getPerson)
                .filter(p -> !p.isDeleted() && !p.isDeceased())
                .map(Person::getEmail)
                .filter(StringUtils::hasText)
                .filter(email -> email.contains("@"))
                .map(email -> email.trim().toLowerCase())
                // Duplicate persons are filtered using Set
                .collect(toSet());
    }

    private List<Occupation> getOccupations(final Organisation organisation) {
        switch (organisation.getOrganisationType()) {
            case CLUB:
                return getOccupationsForClub(organisation);
            default:
                return Collections.emptyList();
        }
    }

    private List<Occupation> getOccupationsForClub(final Organisation club) {
        Preconditions.checkArgument(club.getOrganisationType() == OrganisationType.CLUB);

        final List<Occupation> clubOccupations = occupationRepository.findActiveByOrganisation(club);
        final List<Occupation> groupOccupations = occupationRepository.findActiveByParentOrganisation(club);

        final Set<Person> clubPersons = clubOccupations.stream()
                .map(Occupation::getPerson)
                .collect(toSet());

        // Include group occupation only if valid club occupation is found.
        // This is done to exclude all invited group members who have not yet accepted club invitation.
        final List<Occupation> result = new LinkedList<>();
        result.addAll(clubOccupations);
        result.addAll(groupOccupations.stream()
                .filter(occ -> clubPersons.contains(occ.getPerson()))
                .collect(Collectors.toList()));

        return result;
    }
}
