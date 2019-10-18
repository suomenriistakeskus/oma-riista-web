package fi.riista.feature.organization;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.EmailSanitizer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class EmailResolver {

    @Resource
    private OccupationRepository occupationRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Set<String> findEmailsOfOccupiedPersons(@Nonnull final Organisation organisation,
                                                   @Nonnull final OccupationType occupationType) {

        requireNonNull(organisation);
        requireNonNull(occupationType);

        final List<String> emails = streamPersonEmailOfOccupations(organisation, occupationType).collect(toList());
        return EmailSanitizer.sanitize(emails);
    }

    private Stream<String> streamPersonEmailOfOccupations(final Organisation organisation, final OccupationType type) {
        return occupationRepository
                .findActiveByOrganisationAndOccupationType(organisation, type)
                .stream()
                .map(Occupation::getPerson)
                .map(Person::getEmail);
    }
}
