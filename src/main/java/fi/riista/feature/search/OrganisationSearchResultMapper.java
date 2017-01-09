package fi.riista.feature.search;

import fi.riista.feature.organization.Organisation;

import java.util.Locale;
import java.util.function.Function;

public class OrganisationSearchResultMapper implements Function<Organisation, SearchResultsDTO.Result> {

    public static OrganisationSearchResultMapper create(Locale locale) {
        return new OrganisationSearchResultMapper(locale);
    }

    private final Locale locale;

    public OrganisationSearchResultMapper(Locale locale) {
        this.locale = locale;
    }

    @Override
    public SearchResultsDTO.Result apply(Organisation org) {
        return SearchResultsDTO.createResult(org.getId(), org.getNameLocalisation().getAnyTranslation(locale));
    }
}
