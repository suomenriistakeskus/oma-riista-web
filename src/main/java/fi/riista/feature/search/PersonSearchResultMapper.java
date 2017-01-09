package fi.riista.feature.search;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.function.Function;

public class PersonSearchResultMapper implements Function<Person, SearchResultsDTO.Result> {
    private final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("d.M.yyyy")
            .toFormatter();

    public static PersonSearchResultMapper create(Locale locale) {
        return new PersonSearchResultMapper(locale);
    }

    private final Locale locale;

    public PersonSearchResultMapper(Locale locale) {
        this.locale = locale;
    }

    @Override
    public SearchResultsDTO.Result apply(Person person) {
        String description = getDescription(person);
        return SearchResultsDTO.createResult(person.getId(), description);
    }

    public String getDescription(Person person) {
        final StringBuilder sb = new StringBuilder();

        if (person.getFirstName() != null) {
            sb.append(person.getFirstName());
            sb.append(' ');
        }

        if (person.getLastName() != null) {
            sb.append(person.getLastName());
            sb.append(' ');
        }

        LocalDate dateOfBirth = person.parseDateOfBirth();
        if (dateOfBirth != null) {
            sb.append(new LocalisedString("s. ", "f. ", "b. ").getAnyTranslation(locale));
            sb.append(DATE_FORMAT.print(dateOfBirth));
        }

        Address address = person.getAddress();
        if (address != null) {
            if (StringUtils.hasText(address.getCity())) {
                sb.append(" - ");
                sb.append(address.getCity().toUpperCase());
            }
        }

        return sb.toString();
    }
}
