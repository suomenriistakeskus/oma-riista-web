package fi.riista.feature.search;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

public class SearchResultsDTO {
    public static final SearchResultsDTO EMPTY = new SearchResultsDTO(emptyMap());

    static Result createResult(Long id, String description) {
        return new Result(id, description);
    }

    // Results grouped by category
    private final Map<SearchResultType, Collection<Result>> results;

    private SearchResultsDTO(final Map<SearchResultType, Collection<Result>> results) {
        this.results = Objects.requireNonNull(results);
    }

    public Map<SearchResultType, Collection<Result>> getResults() {
        return results;
    }

    public static class Result {
        private final Long id;
        private final String description;

        public Result(final Long id, final String description) {
            this.id = Objects.requireNonNull(id);
            this.description = Objects.requireNonNull(description);
        }

        public Long getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Builder {
        private final PersonSearchResultMapper personMapper;
        private final OrganisationSearchResultMapper organisationMapper;
        private final PermitSearchResultMapper permitMapper;
        private final Map<SearchResultType, Collection<Result>> results;
        private final Locale locale;

        public Builder(final Locale userLocale) {
            // Prefer user-given locale and fallback to request and system locales
            this.locale = Optional.ofNullable(userLocale).orElse(LocaleContextHolder.getLocale());
            this.personMapper = PersonSearchResultMapper.create(locale);
            this.organisationMapper = OrganisationSearchResultMapper.create(locale);
            this.permitMapper = PermitSearchResultMapper.create();
            this.results = new LinkedHashMap<>();
        }

        public Locale getLocale() {
            return locale;
        }

        public Builder setPersons(final Optional<Person> personMaybe) {
            addResult(SearchResultType.PERSON, personMaybe, personMapper);
            return this;
        }

        public Builder setPersons(final List<Person> personList) {
            addResults(SearchResultType.PERSON, personList, personMapper);
            return this;
        }

        public Builder setRhy(final List<Organisation> rhyList) {
            addResults(SearchResultType.RHY, rhyList, organisationMapper);
            return this;
        }

        public Builder setOtherOrganisations(final List<Organisation> orgList) {
            addResults(SearchResultType.ORG, orgList, organisationMapper);
            return this;
        }

        public Builder setClubs(final List<Organisation> clubList) {
            addResults(SearchResultType.CLUB, clubList, organisationMapper);
            return this;
        }

        public Builder setClubs(final Organisation club) {
            addResult(SearchResultType.CLUB, Optional.ofNullable(club), organisationMapper);
            return this;
        }

        public Builder setPermit(final HarvestPermit permit) {
            addResult(SearchResultType.PERMIT, Optional.ofNullable(permit), permitMapper);
            return this;
        }

        private <T> void addResult(@Nonnull final SearchResultType resultType, final Optional<T> itemMaybe,
                                   @Nonnull final Function<T, Result> transformer) {
            this.results.put(resultType, itemMaybe
                    .map(transformer)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList));
        }

        private <T> void addResults(@Nonnull final SearchResultType resultType,
                                    @Nonnull final List<? extends T> results,
                                    @Nonnull final Function<T, Result> transformer) {
            if (!results.isEmpty()) {
                this.results.put(resultType, F.mapNonNullsToList(results, transformer));
            }
        }

        public SearchResultsDTO build() {
            return new SearchResultsDTO(this.results);
        }
    }

}
