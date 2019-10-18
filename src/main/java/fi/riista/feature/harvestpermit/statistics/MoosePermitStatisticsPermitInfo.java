package fi.riista.feature.harvestpermit.statistics;

import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class MoosePermitStatisticsPermitInfo {
    private final long permitId;
    private final String permitNumber;
    private final LocalisedString permitHolderName;
    private final String permitHolderOfficialCode;
    private final int partnerCount;
    private final Integer permitAreaSize;
    private final long rhyId;
    private final long rkaId;
    private final Integer mooseAreaId;

    public MoosePermitStatisticsPermitInfo(final @Nonnull Long permitId,
                                           final @Nonnull String permitNumber,
                                           final @Nonnull LocalisedString permitHolderName,
                                           final @Nonnull String permitHolderOfficialCode,
                                           final int partnerCount,
                                           final @Nonnull Integer permitAreaSize,
                                           final @Nonnull Long rhyId,
                                           final @Nonnull Long rkaId,
                                           final Integer mooseAreaId) {
        this.permitId = requireNonNull(permitId);
        this.permitNumber = requireNonNull(permitNumber);
        this.permitHolderName = requireNonNull(permitHolderName);
        this.permitHolderOfficialCode = requireNonNull(permitHolderOfficialCode);
        this.partnerCount = partnerCount;
        this.permitAreaSize = requireNonNull(permitAreaSize);
        this.rhyId = requireNonNull(rhyId);
        this.rkaId = requireNonNull(rkaId);
        this.mooseAreaId = mooseAreaId;
    }

    public long getPermitId() {
        return permitId;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public LocalisedString getPermitHolderName() {
        return permitHolderName;
    }

    public String getPermitHolderOfficialCode() {
        return permitHolderOfficialCode;
    }

    public int getPartnerCount() {
        return partnerCount;
    }

    public Integer getPermitAreaSize() {
        return permitAreaSize;
    }

    public long getRhyId() {
        return rhyId;
    }

    public long getRkaId() {
        return rkaId;
    }

    public Integer getMooseAreaId() {
        return mooseAreaId;
    }
}
