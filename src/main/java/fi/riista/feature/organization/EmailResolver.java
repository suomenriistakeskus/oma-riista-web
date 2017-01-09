package fi.riista.feature.organization;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class EmailResolver {

    @Resource
    private OccupationRepository occupationRepository;

    public Set<String> findClubContactEmails(Organisation club) {
        return sanitizeAndCollectToSet(streamPersonEmailOfOccupationType(club, OccupationType.SEURAN_YHDYSHENKILO));
    }

    public Set<String> findRhyContactEmails(Organisation rhy) {
        return sanitizeAndCollectToSet(findRhyContactEmailStream(rhy));
    }

    private Stream<String> findRhyContactEmailStream(Organisation rhy) {
        if (rhy.getEmail() != null) {
            return Stream.of(rhy.getEmail());
        }
        return streamPersonEmailOfOccupationType(rhy, OccupationType.TOIMINNANOHJAAJA);
    }

    private Stream<String> streamPersonEmailOfOccupationType(Organisation club, OccupationType type) {
        return occupationRepository.findActiveByOrganisationAndOccupationType(club, type)
                .stream()
                .map(Occupation::getPerson)
                .map(Person::getEmail);
    }

    private static Set<String> sanitizeAndCollectToSet(Stream<String> stream) {
        return stream.map(EmailResolver::sanitizeEmail)
                .filter(StringUtils::hasText)
                .collect(toSet());
    }

    public static String sanitizeEmail(final String email) {
        return email != null && email.contains("@") ? email.trim().toLowerCase() : null;
    }

}
