package fi.riista.feature.harvestpermit.statistics;

import fi.riista.feature.huntingclub.permit.statistics.PermitAndLocationId;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysNameDTO;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MoosePermitStatisticsI18n {
    private final Locale locale;
    private final LocalisedString speciesName;
    private final Map<Long, RiistanhoitoyhdistysNameDTO> rhyNameMapping;
    private final Map<Integer, LocalisedString> mooseAreaNameMapping;

    MoosePermitStatisticsI18n(final Locale locale,
                              final LocalisedString speciesName,
                              final Map<Long, RiistanhoitoyhdistysNameDTO> rhyNameMapping,
                              final Map<Integer, LocalisedString> mooseAreaNameMapping) {
        this.locale = requireNonNull(locale);
        this.speciesName = requireNonNull(speciesName);
        this.rhyNameMapping = requireNonNull(rhyNameMapping);
        this.mooseAreaNameMapping = requireNonNull(mooseAreaNameMapping);
    }

    public String getAnyTranslation(final LocalisedString localisedString) {
        return localisedString != null ? localisedString.getAnyTranslation(locale) : null;
    }

    public LocalisedString getSpeciesName() {
        return speciesName;
    }

    @Nonnull
    public LocalisedString getRhyName(final PermitAndLocationId dto) {
        return Optional.ofNullable(dto.getRhyId())
                .map(rhyNameMapping::get)
                .map(RiistanhoitoyhdistysNameDTO::getRhyName)
                .orElse(LocalisedString.EMPTY);
    }

    @Nonnull
    public LocalisedString getRkaName(final PermitAndLocationId dto) {
        return findRkaName(dto)
                .map(RiistanhoitoyhdistysNameDTO::getRkaName)
                .orElse(LocalisedString.EMPTY);
    }

    private Optional<RiistanhoitoyhdistysNameDTO> findRkaName(final PermitAndLocationId dto) {
        if (dto.getRhyId() != null) {
            return findByRhyId(dto.getRhyId());

        } else if (dto.getRkaId() != null) {
            return findByRkaId(dto.getRkaId());

        } else {
            return Optional.empty();
        }
    }

    private Optional<RiistanhoitoyhdistysNameDTO> findByRhyId(final long rhyId) {
        return Optional.ofNullable(rhyNameMapping.get(rhyId));
    }

    private Optional<RiistanhoitoyhdistysNameDTO> findByRkaId(final long rkaId) {
        return rhyNameMapping.values().stream()
                .filter(item -> Objects.equals(item.getRkaId(), rkaId))
                .findFirst();
    }

    @Nonnull
    public LocalisedString getHtaName(final PermitAndLocationId dto) {
        return Optional.ofNullable(dto.getMooseAreaId())
                .map(mooseAreaNameMapping::get)
                .orElse(LocalisedString.EMPTY);
    }
}
