package fi.riista.feature.huntingclub.permit.statistics;

import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsGroupBy;
import fi.riista.feature.harvestpermit.statistics.MoosePermitStatisticsPermitInfo;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PermitAndLocationId {
    private final Long permitId;
    private final Long rhyId;
    private final Long rkaId;
    private final Integer mooseAreaId;

    public PermitAndLocationId(final Long rhyId,
                               final Long rkaId,
                               final Integer mooseAreaId,
                               final Long permitId) {
        this.rhyId = rhyId;
        this.rkaId = rkaId;
        this.mooseAreaId = mooseAreaId;
        this.permitId = permitId;
    }

    public PermitAndLocationId(final @Nonnull MoosePermitStatisticsPermitInfo permitInfo) {
        this(permitInfo.getRhyId(), permitInfo.getRkaId(), permitInfo.getMooseAreaId(), permitInfo.getPermitId());
    }

    public PermitAndLocationId getGroupByValue(final @Nonnull MoosePermitStatisticsGroupBy groupBy) {
        switch (groupBy) {
            case RKA:
                return new PermitAndLocationId(null, rkaId, null, null);
            case RHY:
                return new PermitAndLocationId(rhyId, rkaId, null, null);
            case RHY_PERMIT:
                return new PermitAndLocationId(rhyId, rkaId, null, permitId);
            case HTA:
                return new PermitAndLocationId(null, null, mooseAreaId, null);
            case HTA_PERMIT:
                return new PermitAndLocationId(null, null, mooseAreaId, permitId);
            case HTA_RHY:
                return new PermitAndLocationId(rhyId, rkaId, mooseAreaId, null);
            default:
                return new PermitAndLocationId(null, null, null, permitId);
        }
    }

    public Long getRhyId() {
        return rhyId;
    }

    public Long getRkaId() {
        return rkaId;
    }

    public Long getPermitId() {
        return permitId;
    }

    public Integer getMooseAreaId() {
        return mooseAreaId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PermitAndLocationId that = (PermitAndLocationId) o;
        return Objects.equals(permitId, that.permitId) &&
                Objects.equals(rhyId, that.rhyId) &&
                Objects.equals(rkaId, that.rkaId) &&
                Objects.equals(mooseAreaId, that.mooseAreaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(permitId, rhyId, rkaId, mooseAreaId);
    }
}
