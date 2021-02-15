package fi.riista.feature.pub.municipality;

import com.google.common.collect.ImmutableList;
import fi.riista.feature.common.entity.Municipality;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PublicMunicipalityDTO {
    public static PublicMunicipalityDTO from(@Nonnull final Municipality municipality,
                                             @Nonnull final Locale locale) {
        requireNonNull(municipality);
        return new PublicMunicipalityDTO(municipality.getOfficialCode(),
                municipality.getNameLocalisation().getTranslation(locale),
                municipality.getRhyIds());
    }

    private final String officialCode;
    private final String name;
    private final Collection<String> rhyIds;

    public String getOfficialCode() {
        return officialCode;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getRhyIds() { return rhyIds; }

    private PublicMunicipalityDTO(final String officialCode, final String name,  final Set<String> rhyIds) {
        this.officialCode = officialCode;
        this.name = name;
        this.rhyIds = ImmutableList.copyOf(rhyIds);
    }
}
