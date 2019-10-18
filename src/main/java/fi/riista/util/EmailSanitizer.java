package fi.riista.util;

import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Streams.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toCollection;

public final class EmailSanitizer {

    @Nullable
    public static String getSanitizedOrNull(@Nullable final String email) {
        return email != null && email.contains("@") ? email.trim().toLowerCase() : null;
    }

    @Nonnull
    public static Optional<String> sanitize(@Nullable final String email) {
        return Optional.ofNullable(getSanitizedOrNull(email));
    }

    // Preserves iteration order.
    @Nonnull
    public static Set<String> sanitize(@Nullable final Iterable<String> emails) {
        if (emails == null || Iterables.isEmpty(emails)) {
            return emptySet();
        }

        return stream(emails)
                .map(EmailSanitizer::getSanitizedOrNull)
                .filter(Objects::nonNull)
                .collect(toCollection(LinkedHashSet::new));
    }

    private EmailSanitizer() {
        throw new AssertionError();
    }
}
