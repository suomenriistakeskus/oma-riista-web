package fi.riista.feature.search;

import static java.util.EnumSet.of;

import com.google.common.base.Joiner;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.Locales;
import fi.riista.validation.Validators;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SiteSearchFeature {
    private static final int MAX_RESULT_ORGANISATION_RHY = 5;
    private static final int MAX_RESULT_ORGANISATION_OTHER = 5;
    private static final int MAX_RESULT_PERSON = 20;

    // Distance is 0.0 (closest) - 1.0 (furthest)
    private static final double MAX_FUZZY_DISTANCE_ORGANISATION_NAME = 0.9;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public SearchResultsDTO search(final SearchDTO dto) {
        final String searchTerm = dto.getTerm();

        if (searchTerm.length() <= 3) {
            return SearchResultsDTO.EMPTY;
        }

        final SearchResultsDTO.Builder builder = new SearchResultsDTO.Builder(dto.getLocale());

        if (Validators.isValidHuntingClubOfficialCode(searchTerm)) {
            return builder.setClubs(organisationRepository.findByTypeAndOfficialCode(OrganisationType.CLUB, searchTerm)).build();
        }

        if (Validators.isValidPermitNumber(searchTerm)) {
            return builder.setPermit(harvestPermitRepository.findByPermitNumber(searchTerm)).build();
        }

        if (searchTerm.indexOf('@') != -1) {
            return builder.setPersons(personRepository.findByUsernameIgnoreCase(searchTerm)).build();
        }

        if (Validators.isValidSsn(searchTerm)) {
            return builder.setPersons(personRepository.findBySsn(searchTerm)).build();
        }

        if (Validators.isValidHunterNumber(searchTerm)) {
            return builder.setPersons(personRepository.findByHunterNumber(searchTerm)).build();
        }

        final List<Person> personWithCitySearch = searchUsingPersonAndCity(searchTerm);

        if (personWithCitySearch != null) {
            return builder.setPersons(personWithCitySearch).build();
        }

        builder.setRhy(searchOrganisations(searchTerm, builder.getLocale(), MAX_RESULT_ORGANISATION_RHY,
                of(OrganisationType.RHY)));

        builder.setPersons(personRepository.findAllPersonsByFuzzyFullNameMatch(
                searchTerm, PageRequest.of(0, MAX_RESULT_PERSON)));

        builder.setOtherOrganisations(searchOrganisations(searchTerm, builder.getLocale(), MAX_RESULT_ORGANISATION_OTHER,
                of(OrganisationType.RK, OrganisationType.RKA, OrganisationType.ARN, OrganisationType.VRN)));

        builder.setClubs(searchOrganisations(searchTerm, builder.getLocale(), MAX_RESULT_ORGANISATION_OTHER,
                of(OrganisationType.CLUB)));

        return builder.build();
    }

    private List<Person> searchUsingPersonAndCity(final String query) {
        final Tuple2<String, String> municipalityAndSearchTerm = extractCityAndSearchTerm(query);

        if (municipalityAndSearchTerm == null) {
            return null;
        }

        // Increase fuzziness result count for post-processing
        final PageRequest pageRequest = PageRequest.of(0, 100);

        final String city = municipalityAndSearchTerm._1;
        final String searchTerm = municipalityAndSearchTerm._2;

        return personRepository.findByFuzzyFullNameDistance(searchTerm, 0.9, pageRequest)
                .stream()
                .filter(p -> {
                    final String addressCity = p.getAddress() != null ? p.getAddress().getCity() : null;
                    return addressCity != null && addressCity.toLowerCase().startsWith(city);
                })
                .collect(Collectors.toList());
    }

    private static Tuple2<String, String> extractCityAndSearchTerm(final String input) {
        final String cmdPrefix = "kunta:";
        final String[] parts = input.split("\\s+");

        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];

            if (part.toLowerCase().startsWith(cmdPrefix)) {
                final List<String> resultParts = new LinkedList<>();

                for (int j = 0; j < parts.length; j++) {
                    if (i != j) {
                        resultParts.add(parts[j]);
                    }
                }

                final String city = part.substring(cmdPrefix.length()).trim().toLowerCase();
                final String searchTerm = Joiner.on(' ').join(resultParts);

                return Tuple.of(city, searchTerm);
            }
        }

        return null;
    }

    private List<Organisation> searchOrganisations(final String searchTerm, final Locale locale, final int maxResults,
                                                   final EnumSet<OrganisationType> organisationTypes) {
        final PageRequest pageRequest = PageRequest.of(0, maxResults);

        if (Locales.isSwedish(locale)) {
            return organisationRepository.findBySwedishFuzzyNameMatch(
                    searchTerm, organisationTypes, MAX_FUZZY_DISTANCE_ORGANISATION_NAME, pageRequest);
        }

        return organisationRepository.findByFuzzyFullNameMatch(
                searchTerm, organisationTypes, MAX_FUZZY_DISTANCE_ORGANISATION_NAME, pageRequest);
    }
}
