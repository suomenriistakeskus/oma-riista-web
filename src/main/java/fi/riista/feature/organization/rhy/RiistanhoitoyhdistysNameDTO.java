package fi.riista.feature.organization.rhy;

import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static fi.riista.feature.organization.RiistakeskuksenAlue.shortenRkaPrefixFi;
import static fi.riista.feature.organization.RiistakeskuksenAlue.shortenRkaPrefixSv;
import static fi.riista.feature.organization.rhy.Riistanhoitoyhdistys.shortenRhySuffixFi;
import static fi.riista.feature.organization.rhy.Riistanhoitoyhdistys.shortenRhySuffixSv;
import static java.util.Objects.requireNonNull;

public class RiistanhoitoyhdistysNameDTO {

    private final long rhyId;
    private final long rkaId;
    private final String rhyOfficialCode;
    private final String rkaOfficialCode;
    private final LocalisedString rhyName;
    private final LocalisedString rkaName;

    public RiistanhoitoyhdistysNameDTO(final @Nonnull Long rhyId, final @Nonnull Long rkaId,
                                       final @Nonnull String rhyOfficialCode, final @Nonnull String rkaOfficialCode,
                                       final @Nonnull String rhyNameFi, final @Nonnull String rhyNameSv,
                                       final @Nonnull String rkaNameFi, final @Nonnull String rkaNameSv) {
        this.rhyId = requireNonNull(rhyId);
        this.rkaId = requireNonNull(rkaId);
        this.rhyOfficialCode = requireNonNull(rhyOfficialCode);
        this.rkaOfficialCode = requireNonNull(rkaOfficialCode);
        this.rhyName = LocalisedString.of(shortenRhySuffixFi(rhyNameFi), shortenRhySuffixSv(rhyNameSv));
        this.rkaName = LocalisedString.of(shortenRkaPrefixFi(rkaNameFi), shortenRkaPrefixSv(rkaNameSv));
    }

    public long getRhyId() {
        return rhyId;
    }

    public long getRkaId() {
        return rkaId;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public String getRkaOfficialCode() {
        return rkaOfficialCode;
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public LocalisedString getRkaName() {
        return rkaName;
    }
}
