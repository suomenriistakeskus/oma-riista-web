package fi.riista.util;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CountryCode {

    private static final Map<String, String> CODE_TO_ENGLISH_NAME;

    static {
        final ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();

        for (String iso : Locale.getISOCountries()) {
            final Locale l = new Locale("", iso);
            mapBuilder.put(iso, l.getDisplayCountry(Locale.ENGLISH));
        }

        CODE_TO_ENGLISH_NAME = mapBuilder.build();
    }

    public static Optional<String> getCountryName(String countryCode) {
        Objects.requireNonNull(countryCode);
        return Optional.ofNullable(CODE_TO_ENGLISH_NAME.get(countryCode));
    }

    public static void main(String[] args) {
        CODE_TO_ENGLISH_NAME.forEach((key, value) -> System.out.println("iso=" + key + " name=" + value));
    }

    private CountryCode() {
        throw new AssertionError();
    }
}
