package fi.riista.feature.search;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.Locales;
import fi.riista.validation.FinnishHunterNumberValidator;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class SiteSearchFeature {
    private static final FinnishSocialSecurityNumberValidator SSN_VALIDATOR = new FinnishSocialSecurityNumberValidator();
    private static final FinnishHunterNumberValidator HUNTER_NUMBER_VALIDATOR = new FinnishHunterNumberValidator();

    public static final int MAX_RESULT_ORGANISATION_RHY = 5;
    public static final int MAX_RESULT_ORGANISATION_OTHER = 5;
    public static final int MAX_RESULT_PERSON = 20;

    // Distance is 0.0 (closest) - 1.0 (furthest)
    public static final double MAX_FUZZY_DISTANCE_ORGANISATION_NAME = 0.9;
    public static final double MAX_FUZZY_DISTANCE_PERSON_NAME = 0.7;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public SearchResultsDTO search(@NotNull final String searchTerm, Locale userLocale) {
        Objects.requireNonNull(searchTerm, "Null cannot be used as search term");

        // Prefer user-given locale and fallback to request and system locales
        final Locale locale = Optional.ofNullable(userLocale).orElse(LocaleContextHolder.getLocale());

        final PersonSearchResultMapper personMapper = PersonSearchResultMapper.create(locale);
        final OrganisationSearchResultMapper organisationMapper = OrganisationSearchResultMapper.create(locale);

        final SearchResultsDTO results = new SearchResultsDTO();

        results.addResults(SearchResultType.RHY, searchRHYs(searchTerm, locale), organisationMapper);
        results.addResults(SearchResultType.PERSON, searchPersons(searchTerm), personMapper);
        results.addResults(SearchResultType.ORG, searchOrganisations(searchTerm, locale), organisationMapper);
        results.addResults(SearchResultType.CLUB, searchClubs(searchTerm, locale), organisationMapper);

        return results;
    }

    private List<Person> searchPersons(final String searchTerm) {
        if (SSN_VALIDATOR.isValid(searchTerm, null)) {
            return personRepository.findBySsn(searchTerm)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        }

        if (HUNTER_NUMBER_VALIDATOR.isValid(searchTerm, null)) {
            return personRepository.findByHunterNumber(searchTerm)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        }

        return personRepository.findByFuzzyFullNameMatch(
                searchTerm, MAX_FUZZY_DISTANCE_PERSON_NAME, new PageRequest(0, MAX_RESULT_PERSON));
    }

    private List<Organisation> searchRHYs(final String searchTerm, final Locale locale) {
        final EnumSet<OrganisationType> organisationTypes = EnumSet.of(OrganisationType.RHY);

        return searchOrganisations(searchTerm, locale, MAX_RESULT_ORGANISATION_RHY, organisationTypes);
    }

    private List<Organisation> searchOrganisations(final String searchTerm, final Locale locale) {
        final EnumSet<OrganisationType> organisationTypes = EnumSet
                .complementOf(EnumSet.of(OrganisationType.RHY, OrganisationType.CLUB, OrganisationType.CLUBGROUP));

        return searchOrganisations(searchTerm, locale, MAX_RESULT_ORGANISATION_OTHER, organisationTypes);
    }

    private List<Organisation> searchClubs(final String searchTerm, final Locale locale) {
        if (Pattern.matches("^\\d+", searchTerm)) {
            final Organisation huntingClub = organisationRepository.findByTypeAndOfficialCode(OrganisationType.CLUB, searchTerm);
            return huntingClub != null ? singletonList(huntingClub) : emptyList();
        }

        final EnumSet<OrganisationType> organisationTypes = EnumSet.of(OrganisationType.CLUB);

        return searchOrganisations(searchTerm, locale, MAX_RESULT_ORGANISATION_OTHER, organisationTypes);
    }

    private List<Organisation> searchOrganisations(final String searchTerm,
                                                   final Locale locale,
                                                   final int maxResults,
                                                   final EnumSet<OrganisationType> organisationTypes) {
        final double maxDistance = MAX_FUZZY_DISTANCE_ORGANISATION_NAME;
        final PageRequest pageRequest = new PageRequest(0, maxResults);

        if (Locales.isSwedish(locale)) {
            return organisationRepository.findBySwedishFuzzyNameMatch(
                    searchTerm, organisationTypes, maxDistance, pageRequest);
        }
        return organisationRepository.findByFinnishFuzzyNameMatch(
                searchTerm, organisationTypes, maxDistance, pageRequest);
    }
}
