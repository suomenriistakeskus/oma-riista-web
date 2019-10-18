package fi.riista.feature.huntingclub.permit.statistics;

import java.util.Objects;

public final class PermitAndClubId {
    private final long permitId;
    private final long clubId;

    public PermitAndClubId(final long permitId, final long clubId) {
        this.permitId = permitId;
        this.clubId = clubId;
    }

    public long getPermitId() {
        return permitId;
    }

    public long getClubId() {
        return clubId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PermitAndClubId that = (PermitAndClubId) o;
        return permitId == that.permitId &&
                clubId == that.clubId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(permitId, clubId);
    }
}
