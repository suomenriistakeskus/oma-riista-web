package fi.riista.feature.harvestpermit.list;

import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.permit.decision.PermitDecision;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class ListDecisionDTO implements HasID<Long> {

    @Nonnull
    public static ListDecisionDTO create(final @Nonnull PermitDecision decision) {
        requireNonNull(decision, "decision must not be null");

        return new ListDecisionDTO(decision.getId(), decision.getPermitTypeCode(), decision.createPermitNumber());
    }

    private ListDecisionDTO(final @Nonnull Long id,
                            final @Nonnull String permitTypeCode,
                            final @Nonnull String permitNumber) {
        this.id = requireNonNull(id);
        this.permitTypeCode = requireNonNull(permitTypeCode);
        this.permitNumber = requireNonNull(permitNumber);
    }

    private final Long id;
    private final String permitTypeCode;
    private final String permitNumber;

    @Override
    public Long getId() {
        return id;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public String getPermitNumber() {
        return permitNumber;
    }
}
