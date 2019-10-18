package fi.riista.feature.harvestpermit.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ApplicationDecisionPermitListDTO {
    private ListPermitApplicationDTO application;
    private ListDecisionDTO decision;
    private final List<ListHarvestPermitDTO> permits = new LinkedList<>();

    public ApplicationDecisionPermitListDTO(final @Nonnull ListPermitApplicationDTO application) {
        this.application = requireNonNull(application);
        this.decision = null;
    }

    public ApplicationDecisionPermitListDTO(final @Nonnull ListPermitApplicationDTO application,
                                            final @Nonnull ListDecisionDTO decision) {
        this.application = requireNonNull(application);
        this.decision = requireNonNull(decision);
    }

    public ApplicationDecisionPermitListDTO(final @Nonnull ListHarvestPermitDTO permit) {
        this.application = null;
        this.decision = null;
        this.permits.add(permit);
    }

    @JsonIgnore
    boolean equalsApplication(final @Nonnull HarvestPermitApplication application) {
        return this.application != null && Objects.equals(this.application.getId(), application.getId());
    }

    @JsonIgnore
    boolean equalsDecision(final @Nonnull PermitDecision decision) {
        return this.decision != null && Objects.equals(this.decision.getId(), decision.getId());
    }

    @JsonIgnore
    boolean containsPermit(final @Nonnull HarvestPermit permit) {
        return this.permits.stream().anyMatch(dto -> dto.getPermitNumber().equals(permit.getPermitNumber()));
    }

    public ListPermitApplicationDTO getApplication() {
        return application;
    }

    public void setApplication(final ListPermitApplicationDTO application) {
        this.application = application;
    }

    public ListDecisionDTO getDecision() {
        return decision;
    }

    public void setDecision(final ListDecisionDTO decision) {
        this.decision = decision;
    }

    public List<ListHarvestPermitDTO> getPermits() {
        return permits;
    }

    public void addPermit(final ListHarvestPermitDTO permit) {
        this.permits.add(permit);
    }
}
