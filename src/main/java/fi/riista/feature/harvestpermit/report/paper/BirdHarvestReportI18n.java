package fi.riista.feature.harvestpermit.report.paper;

import fi.riista.util.LocalisedString;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BirdHarvestReportI18n {
    private final Map<Integer, LocalisedString> speciesNameIndex;
    private final Locale locale;

    public BirdHarvestReportI18n(final Map<Integer, LocalisedString> speciesNameIndex, final Locale locale) {
        this.speciesNameIndex = requireNonNull(speciesNameIndex);
        this.locale = requireNonNull(locale);
    }

    public String getGameSpeciesName(final int gameSpeciesCode) {
        return Optional.ofNullable(speciesNameIndex.get(gameSpeciesCode))
                .map(localisedString -> localisedString.getTranslation(locale))
                .map(StringUtils::capitalize)
                .orElse("-");
    }

    @Nonnull
    public String getLocalisedString(final LocalisedString localisedString) {
        return requireNonNull(localisedString).getTranslation(locale);
    }

    @Nonnull
    public String getLocalisedString(final String finnish, final String swedish) {
        return getLocalisedString(new LocalisedString(finnish, swedish));
    }

}
